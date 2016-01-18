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
package com.infobip.jira;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.commit.Commit;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lpandzic
 */
public class JiraVersionGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JiraVersionGenerator.class);

    private final JiraService jiraService;
    private final Commit releaseChangeset;
    private final Iterator<Commit> changesetIterator;
    private final CommitMessageVersionExtractor commitMessageVersionExtractor;

    public JiraVersionGenerator(@Nonnull JiraService jiraService,
                                @Nonnull Commit releaseChangeset,
                                @Nonnull Iterator<Commit> changesetIterator,
                                @Nonnull CommitMessageVersionExtractor commitMessageVersionExtractor) {

        this.releaseChangeset = releaseChangeset;

        this.jiraService = requireNonNull(jiraService);
        this.changesetIterator = requireNonNull(changesetIterator);
        this.commitMessageVersionExtractor = requireNonNull(commitMessageVersionExtractor);
    }

    public void generateJiraVersionAndLinkIssues(@Nonnull String jiraVersionPrefix, @Nonnull ProjectKey projectKey) throws IOException, CredentialsRequiredException, ResponseException {

        if (!changesetIterator.hasNext()) {
            logger.warn("no changesets in commit");
            return;
        }

        Optional<String> versionName = commitMessageVersionExtractor.extractVersionName(releaseChangeset.getMessage());

        if (!versionName.isPresent()) {
            return;
        }

        List<Commit> versionChangesets = getVersionChangesets();

        Version version = Version.of(jiraVersionPrefix + versionName.get(),
                                     projectKey,
                                     true,
                                     releaseChangeset.getAuthorTimestamp());

        try {
            if (jiraService.doesJiraVersionExist(version)) {
                logger.info(String.format("jira version %s already exists", version));
                return;
            }
        } catch (CredentialsRequiredException | IOException | ResponseException e) {
            logger.error(String.format("failed to verify if Jira version %s already exists", version), e);
            throw e;
        }

        try {
            jiraService.createJiraVersion(version);
        } catch (CredentialsRequiredException | IOException | ResponseException e) {
            logger.error("failed to create Jira version " + version, e);
            throw e;
        }

        List<IssueKey> issueKeys = getIssueKeys(versionChangesets, projectKey);

        try {
            jiraService.addVersionToIssues(version, issueKeys);
        } catch (CredentialsRequiredException | IOException | ResponseException e) {
            logger.error(String.format("failed to add version %s to issues %s", version, issueKeys), e);
            throw e;
        }

        try {
            jiraService.releaseVersion(version);
        } catch (CredentialsRequiredException | IOException | ResponseException e) {
            logger.error(String.format("Failed to release version %s. ", version), e);
            throw e;
        }

    }

    private List<IssueKey> getIssueKeys(List<Commit> versionChangesets, ProjectKey projectKey) {

        List<IssueKey> issueKeys = new ArrayList<>();

        for (Commit versionChangeset : versionChangesets) {
            Iterable<IssueKey> issueKeyIterable = IssueKey.of(versionChangeset);

            for (IssueKey issueKey : issueKeyIterable) {
                if (projectKey.equals(issueKey.getProjectKey())) {
                    issueKeys.add(issueKey);
                }
            }
        }

        return issueKeys;
    }

    private List<Commit> getVersionChangesets() {

        List<Commit> versionChangesets = new ArrayList<>();

        while (changesetIterator.hasNext()) {
            Commit changeset = changesetIterator.next();

            if (commitMessageVersionExtractor.extractVersionName(changeset.getMessage()).isPresent()) {
                break;
            }

            versionChangesets.add(changeset);
        }
        return versionChangesets;
    }
}
