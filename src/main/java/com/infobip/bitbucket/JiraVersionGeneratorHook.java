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

import java.util.Collection;
import java.util.Iterator;

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
        String branchName = refChange.getRef().getId();
        Settings settings = repositoryHookContext.getSettings();
        String jiraVersionPrefix = settings.getString("jira-version-prefix", "");

        ProjectKey projectKey;

        try {
            projectKey = new ProjectKey(settings.getString(ProjectKeyValidator.SETTINGS_KEY, ""));
        } catch (IllegalArgumentException e) {
            logger.error("failed to generate jira JIRA version and link issues", e);
            return;
        }

        Repository repository = repositoryHookContext.getRepository();

        Iterator<Commit> changesets;
        try {
            changesets = ChangesetPageCrawler.of(historyService, branchName, repository);
        } catch (NoSuchCommitException e) {
            // branch was deleted
            return;
        }

        Commit hookEventChangeset;

        try {
            hookEventChangeset = findHookEventChangeset(refChange.getToHash(), changesets);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return;
        }

        String repositoryName = repository.getName();
        CommitMessageVersionExtractor commitMessageVersionExtractor = new CommitMessageVersionExtractor(
                repositoryName, settings.getString(VersionPatternValidator.SETTINGS_KEY, ""));

        JiraVersionGenerator jiraVersionGenerator = new JiraVersionGenerator(jiraService,
                hookEventChangeset,
                changesets,
                commitMessageVersionExtractor,
                ClockFactory.getInstance());

        try {
            jiraVersionGenerator.generate(jiraVersionPrefix, projectKey);
        } catch (JiraServiceException e) {
            logger.error("Failed to generate Jira version for project " + projectKey, e);
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors settingsValidationErrors, Repository repository) {

        for (RepositorySettingsValidator settingsValidator : settingsValidators) {
            settingsValidator.validate(settings, settingsValidationErrors, repository);
        }
    }

    private Commit findHookEventChangeset(String id, Iterator<Commit> changesets) throws Exception {

        while (changesets.hasNext()) {

            Commit next = changesets.next();

            if (next.getId().equals(id)) {
                return next;
            }
        }

        throw new Exception(String.format("Changeset with id %s not found", id));
    }
}
