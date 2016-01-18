/**
 *# Copyright 2014 Infobip
 #
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 # http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 */
package com.infobip.stash;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseStatusException;
import com.atlassian.stash.commit.Commit;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.commit.NoSuchCommitException;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.exception.NoSuchChangesetException;
import com.atlassian.stash.history.HistoryService;
import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.common.collect.ImmutableList;
import com.infobip.jira.JiraService;
import com.infobip.jira.JiraVersionGenerator;
import com.infobip.jira.CommitMessageVersionExtractor;
import com.infobip.jira.ProjectKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lpandzic
 */
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
    public void postReceive(@Nonnull RepositoryHookContext repositoryHookContext,
                            @Nonnull Collection<RefChange> refChanges) {

        if (refChanges.size() != 1) {
            return;
        }

        RefChange refChange = refChanges.iterator().next();
        String branchName = refChange.getRefId();
        Settings settings = repositoryHookContext.getSettings();
        String jiraVersionPrefix = settings.getString("jira-version-prefix", "");

	    ProjectKey projectKey;

	    try {
		    projectKey = ProjectKey.of(settings.getString(ProjectKeyValidator.SETTINGS_KEY, ""));
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
                                                                             commitMessageVersionExtractor);

        try {
            jiraVersionGenerator.generateJiraVersionAndLinkIssues(jiraVersionPrefix, projectKey);
        } catch (ResponseStatusException e) {

            logger.error("failed to generate Jira version and link issues. Received response " +
                                 extractResponseMessage(e), e);

        } catch (IOException | CredentialsRequiredException | ResponseException e) {
            logger.error("failed to generate Jira version and link issues", e);
        }
    }

	@Override
	public void validate(@Nonnull Settings settings,
	                     @Nonnull SettingsValidationErrors settingsValidationErrors,
	                     @Nonnull Repository repository) {

        for (RepositorySettingsValidator settingsValidator : settingsValidators) {
            settingsValidator.validate(settings, settingsValidationErrors, repository);
        }
	}

	private Commit findHookEventChangeset(String id, Iterator<Commit> changesets) throws Exception {

        while(changesets.hasNext()) {

            Commit next = changesets.next();

            if(next.getId().equals(id)) {
                return next;
            }
        }

        throw new Exception(String.format("Changeset with id %s not found", id));
    }

    private String extractResponseMessage(ResponseStatusException e) {

        Response response = e.getResponse();
        return String.format("Received response: status: %s %s, headers: %s, body: %s",
                             response.getStatusCode(),
                             response.getStatusText(),
                             response.getHeaders(),
                             getResponseBodyAsString(response));
    }

    private String getResponseBodyAsString(Response response) {

        try {
            return response.getResponseBodyAsString();
        } catch (ResponseException e) {
            return "is empty";
        }

    }
}
