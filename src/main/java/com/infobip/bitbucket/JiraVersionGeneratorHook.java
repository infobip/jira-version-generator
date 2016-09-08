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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.infobip.infrastructure.ClockFactory;
import com.infobip.jira.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JiraVersionGeneratorHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private static final Logger logger = LoggerFactory.getLogger(JiraVersionGeneratorHook.class);

    private final CommitService commitService;
    private final JiraService jiraService;
    private final ImmutableList<RepositorySettingsValidator> settingsValidators;

    public JiraVersionGeneratorHook(CommitService commitService, JiraService jiraService) {

        this.commitService = commitService;
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

        ProjectKey projectKey = new ProjectKey(requireNonEmptySetting(repositoryHookContext, ProjectKeyValidator.SETTINGS_KEY));
        String jiraVersionPrefix = getNonEmptySetting(repositoryHookContext, "jira-version-prefix").orElse("");
        jiraVersionGenerator.generate(jiraVersionPrefix, projectKey);
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors settingsValidationErrors, Repository repository) {

        settingsValidators.forEach(validator -> validator.validate(settings, settingsValidationErrors, repository));
    }

    private Optional<String> getNonEmptySetting(RepositoryHookContext repositoryHookContext, String key) {
        String setting = Strings.emptyToNull(repositoryHookContext.getSettings().getString(key));
        return Optional.ofNullable(setting);
    }

    private String requireNonEmptySetting(RepositoryHookContext repositoryHookContext, String key) {
        String setting = repositoryHookContext.getSettings().getString(key);

        if(Strings.isNullOrEmpty(setting)) {
            String message = String.format("%s hook setting is not set to a non empty value", key);
            throw new IllegalStateException(message);
        }

        return setting;
    }

    private JiraVersionGenerator createJiraVersionGenerator(RepositoryHookContext repositoryHookContext,
                                                            RefChange refChange) {

        Iterator<Commit> commitIterator = CommitPageCrawler.of(commitService, repositoryHookContext.getRepository(), refChange);

        Commit releaseCommit = commitIterator.next();

        String repositoryName = repositoryHookContext.getRepository().getName();

        CommitMessageVersionExtractor commitMessageVersionExtractor = getNonEmptySetting(repositoryHookContext, VersionPatternValidator.SETTINGS_KEY)
                .map(versionPattern -> new CommitMessageVersionExtractor(repositoryName, versionPattern))
                .orElseGet(() -> new CommitMessageVersionExtractor(repositoryName));

        return new JiraVersionGenerator(jiraService,
                releaseCommit,
                commitIterator,
                commitMessageVersionExtractor,
                ClockFactory.getInstance());
    }
}
