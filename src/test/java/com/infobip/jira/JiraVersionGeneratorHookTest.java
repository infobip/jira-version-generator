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
package com.infobip.jira;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.*;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.sal.api.net.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.infobip.bitbucket.JiraVersionGeneratorHook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class JiraVersionGeneratorHookTest {

    private static final LocalDate START_OF_2016 = LocalDate.of(2016, 1, 1);

    @InjectMocks
    private JiraVersionGeneratorHook jiraVersionGeneratorHook;

    @Mock
    private CommitService historyService;

    @Mock
    private JiraService jiraService;

    @Mock
    private RepositoryHookContext repositoryHookContext;

    @Mock
    private Repository repository;

    @Mock
    private Page<Commit> changesetPage;

    @Mock
    private RefChange latestRefChange;

    @Mock
    private MinimalRef latestMinimalRef;

    @Mock
    private RefChange olderRefChange;

    @Mock
    private MinimalRef olderMinimalRef;

    @Mock
    private Settings settings;

    @Before
    public void setUp() throws Exception {

        given(repositoryHookContext.getSettings()).willReturn(settings);
        given(settings.getString(anyString(), eq(""))).willReturn("");
        given(repositoryHookContext.getRepository()).willReturn(repository);
    }

    @Test
    public void shouldGenerateVersionWithNoIssues() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("latest", "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016),
                new Changeset("older", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "branch");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.1", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.1");
    }

    @Test
    public void shouldGenerateJiraVersionWithAPrefix() throws IOException, CredentialsRequiredException, ResponseException {

        givenSetting("jira-version-prefix", "infobip-test-");
        givenSetting("jira-project-key", "TEST");
        givenRepositoryName("test-project");
        givenChangesets(new Changeset("latest",
                "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016),
                new Changeset("older", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "branch");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "infobip-test-1.0.1", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "infobip-test-1.0.1");
        thenGetChangesets(times(1), "master");
        thenGetChangesets(times(1), "branch");
    }

    @Test
    public void shouldGenerateJiraVersionWithACustomVersionPattern() throws IOException, CredentialsRequiredException, ResponseException {

        givenSetting("jira-project-key", "TEST");
        givenSetting("release-commit-version-pattern", "Release (?<version>.*)");
        givenRepositoryName("test-project");
        givenChangesets(new Changeset("latest", "Release 1.0.1", START_OF_2016),
                new Changeset("older", "Release test-project-1.0.0", START_OF_2016));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "branch");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.1", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.1");
        thenGetChangesets(times(1), "master");
        thenGetChangesets(times(1), "branch");
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssuesWhenHookEventOccursAfterAnotherCommit() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("latest",
                "[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                new Changeset("older", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                new Changeset("oldest", "TEST-1", START_OF_2016));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "master");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(olderRefChange);
        whenPostReceive(latestRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.0");
        thenGetChangesets(times(2), "master");
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssuesOnlyForLatestRefChange() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("latest",
                "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                new Changeset("older", "TEST-1", START_OF_2016));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "branch");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.0");
        thenGetChangesets(times(1), "master");
        thenGetChangesets(times(1), "branch");
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssueWhenThereAreCommitsWithNoIssueKeyInMessage() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("1", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                new Changeset("2", "Merge pull request #3 in TEST/test-project from test to master", START_OF_2016),
                new Changeset("3", "Merge pull request #2 in TEST/test-project from TEST-1", START_OF_2016),
                new Changeset("4", "Merge pull request #1 in TEST/test-project from TEST-2", START_OF_2016));
        givenLatestRefChange("1", "master");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);

        thenShouldCreateJiraVersion("1.0.0", "TEST");

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2"))));
    }

    @Test
    public void shouldNotLinkIssuesThatAreNotPartOfVersionProject() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("1", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                new Changeset("2",
                        "Merge pull request #298 in TEST/test-project from test to master", START_OF_2016),
                new Changeset("3", "Merge pull request #2 in TEST/test-project from TEST-1", START_OF_2016),
                new Changeset("3", "Merge pull request #295 in TEST/test-project from ABCD-1", START_OF_2016),
                new Changeset("4", "Merge pull request #1 in TEST/test-project from TEST-2", START_OF_2016));
        givenLatestRefChange("1", "master");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");


        whenPostReceive(latestRefChange);

        thenShouldCreateJiraVersion("1.0.0", "TEST");

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2"))));
    }

    @Test
    public void shouldLinkAllRelatedIssuesPresentInACommitMessage() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenChangesets(new Changeset("1", "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                new Changeset("2",
                        "Merge pull request #298 in TEST/test-project from test to master", START_OF_2016),
                new Changeset("3",
                        "Merge pull request #2 in TEST/test-project from TEST-1, TEST-2, TEST-3", START_OF_2016));
        givenLatestRefChange("1", "master");
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);

        thenShouldCreateJiraVersion("1.0.0", "TEST");

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2")), new IssueKey(new ProjectKey("TEST"), new IssueId("3"))));
    }

    private void givenCreatedVersion(String id, String name, String project) {
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion(id, name, project, null, false));
    }

    private void givenSetting(String key, String value) {

        given(settings.getString(eq(key), anyString())).willReturn(value);
    }

    private void givenJiraVersionDoesNotExist() throws CredentialsRequiredException, ResponseException, IOException {

        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
    }

    private void givenOlderRefChange(String hash, String branchName) {

        given(olderRefChange.getToHash()).willReturn(hash);
        given(olderRefChange.getRef()).willReturn(olderMinimalRef);
        given(olderMinimalRef.getId()).willReturn(branchName);
    }

    private void givenLatestRefChange(String hash, String branchName) {

        given(latestRefChange.getToHash()).willReturn(hash);
        given(latestRefChange.getRef()).willReturn(latestMinimalRef);
        given(latestMinimalRef.getId()).willReturn(branchName);
    }

    private void givenChangesets(Commit... changesets) {

        given(changesetPage.getValues()).willReturn(ImmutableList.copyOf(changesets));
        given(historyService.getCommits(any(CommitsRequest.class),
                any(PageRequest.class))).willReturn(changesetPage);
    }

    private void givenRepositoryName(String value) {

        given(repository.getName()).willReturn(value);
    }

    private void whenPostReceive(RefChange refChange) {

        jiraVersionGeneratorHook.postReceive(repositoryHookContext, ImmutableList.of(refChange));
    }

    private void thenShouldCreateJiraVersion(String name, String project) throws CredentialsRequiredException, ResponseException, JsonProcessingException {

        then(jiraService).should().createJiraVersion(new SerializedVersion(null, name, project, null, false));
    }

    private void thenGetChangesets(VerificationMode verificationMode, String branchName) {

        CommitsRequest request = new CommitsRequest.Builder(repository, branchName).build();

        then(historyService).should(verificationMode).getCommits(refEq(request),
                any(PageRequest.class));
    }

}