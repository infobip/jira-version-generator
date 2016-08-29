/**
 * # Copyright 2016 Infobip
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * # http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */
package com.infobip.bitbucket;

import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.*;
import com.google.common.collect.ImmutableList;
import com.infobip.infrastructure.ClockFactory;
import com.infobip.jira.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JiraVersionGeneratorHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private static final Logger logger = LoggerFactory.getLogger(JiraVersionGeneratorHook.class);

    private final CommitService historyService;
    private final JiraService jiraService;
    private final ImmutableList<RepositorySettingsValidator> settingsValidators;

    public JiraVersionGeneratorHook(CommitService historyService, JiraService jiraService) {

        this.historyService = historyService;
        this.jiraService = jiraService;

        settingsValidators = ImmutableList.of(new ProjectKeyValidator(), new VersionPatternValidator());
    }

    @Override
    public void postReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> refChanges) {

        if (refChanges.size() != 1) {
            return;
        }

        RefChange refChange = refChanges.iterator().next();

        try {
            postReceive(repositoryHookContext, refChange);
        } catch (NoSuchCommitException ignored) {
            // branch was deleted
        } catch (RuntimeException e) {
            logger.error("Failed to generate jira JIRA version and link issues", e);
        }
    }

    private void postReceive(RepositoryHookContext repositoryHookContext, RefChange refChange) {

        JiraVersionGenerator jiraVersionGenerator = createJiraVersionGenerator(repositoryHookContext, refChange);

        ProjectKey projectKey = new ProjectKey(getSetting(repositoryHookContext, ProjectKeyValidator.SETTINGS_KEY).orElse(""));
        String jiraVersionPrefix = getSetting(repositoryHookContext, "jira-version-prefix").orElse("");
        jiraVersionGenerator.generate(jiraVersionPrefix, projectKey);
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors settingsValidationErrors, Repository repository) {

        for (RepositorySettingsValidator settingsValidator : settingsValidators) {
            settingsValidator.validate(settings, settingsValidationErrors, repository);
        }
    }

    private Optional<String> getSetting(RepositoryHookContext repositoryHookContext, String key) {
        return Optional.ofNullable(repositoryHookContext.getSettings().getString(key));
    }

    private JiraVersionGenerator createJiraVersionGenerator(RepositoryHookContext repositoryHookContext,
                                                            RefChange refChange) {

        String branchName = refChange.getRef().getId();
        Iterator<Commit> commitIterator = CommitPageCrawler.of(historyService, branchName, repositoryHookContext.getRepository());

        Commit releaseCommit = getReleaseCommit(refChange.getToHash(), commitIterator);

        String repositoryName = repositoryHookContext.getRepository().getName();

        CommitMessageVersionExtractor commitMessageVersionExtractor = getSetting(repositoryHookContext, VersionPatternValidator.SETTINGS_KEY)
                .map(versionPattern -> new CommitMessageVersionExtractor(repositoryName, versionPattern))
                .orElseGet(() -> new CommitMessageVersionExtractor(repositoryName));

        return new JiraVersionGenerator(jiraService,
                releaseCommit,
                commitIterator,
                commitMessageVersionExtractor,
                ClockFactory.getInstance());
    }

    private Commit getReleaseCommit(String id, Iterator<Commit> commitIterator) {

        while (commitIterator.hasNext()) {

            Commit next = commitIterator.next();

            if (next.getId().equals(id)) {
                return next;
            }
        }

        throw new IllegalArgumentException(String.format("Commit with id %s not found", id));
    }
}
