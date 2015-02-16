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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.Date;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.history.HistoryService;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.infobip.stash.JiraVersionGeneratorHook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

@RunWith(MockitoJUnitRunner.class)
public class JiraVersionGeneratorHookTest {

	@InjectMocks
	private JiraVersionGeneratorHook jiraVersionGeneratorHook;

	@Mock
	private HistoryService historyService;

	@Mock
	private JiraService jiraService;

	@Mock
	private RepositoryHookContext repositoryHookContext;

	@Mock
	private Repository repository;

	@Mock
	private Page<com.atlassian.stash.content.Changeset> changesetPage;

	@Mock
	private RefChange latestRefChange;

	@Mock
	private RefChange olderRefChange;

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
		givenChangesets(Changeset.of("latest",
		                             "[maven-release-plugin] prepare release test-project-1.0.1"),
		                Changeset.of("older", "[maven-release-plugin] prepare release test-project-1.0.0"));

		givenLatestRefChange("latest", "master");
		givenOlderRefChange("older", "branch");

		whenPostReceive(latestRefChange);
		whenPostReceive(olderRefChange);

		thenDoesJiraVersionExist(Version.of("1.0.1", new ProjectKey("TEST"), true, new Date()));
	}

	@Test
	public void shouldGenerateJiraVersionWithAPrefix() throws IOException, CredentialsRequiredException, ResponseException {

		givenSetting("jira-version-prefix", "infobip-test-");
		givenSetting("jira-project-key", "TEST");
		givenRepositoryName("test-project");
		givenChangesets(Changeset.of("latest",
		                             "[maven-release-plugin] prepare release test-project-1.0.1"),
		                Changeset.of("older", "[maven-release-plugin] prepare release test-project-1.0.0"));

		givenLatestRefChange("latest", "master");
		givenOlderRefChange("older", "branch");

		whenPostReceive(latestRefChange);
		whenPostReceive(olderRefChange);

		thenDoesJiraVersionExist(Version.of("infobip-test-1.0.1", new ProjectKey("TEST"), true, new Date()));
		thenGetChangesets(times(1), "master");
		thenGetChangesets(times(1), "branch");
	}

    @Test
    public void shouldGenerateJiraVersionWithACustomVersionPattern() throws IOException, CredentialsRequiredException, ResponseException {

        givenSetting("jira-project-key", "TEST");
        givenSetting("release-commit-version-pattern", "Release (?<version>.*)");
        givenRepositoryName("test-project");
        givenChangesets(Changeset.of("latest", "Release 1.0.1"),
                        Changeset.of("older", "Release test-project-1.0.0"));

        givenLatestRefChange("latest", "master");
        givenOlderRefChange("older", "branch");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        thenDoesJiraVersionExist(Version.of("1.0.1", new ProjectKey("TEST"), true, new Date()));
        thenGetChangesets(times(1), "master");
        thenGetChangesets(times(1), "branch");
    }

	@Test
	public void shouldGenerateJiraVersionAndLinkIssuesWhenHookEventOccursAfterAnotherCommit() throws IOException, CredentialsRequiredException, ResponseException {

		givenRepositoryName("test-project");
		givenSetting("jira-project-key", "TEST");
		givenChangesets(Changeset.of("latest",
		                             "[maven-release-plugin] prepare for next development iteration"),
		                Changeset.of("older", "[maven-release-plugin] prepare release test-project-1.0.0"),
		                Changeset.of("oldest", "TEST-1"));

		givenLatestRefChange("latest", "master");
		givenOlderRefChange("older", "master");

		whenPostReceive(olderRefChange);
		whenPostReceive(latestRefChange);

		thenDoesJiraVersionExist(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));
		thenGetChangesets(times(2), "master");
	}

	@Test
	public void shouldGenerateJiraVersionAndLinkIssuesOnlyForLatestRefChange() throws IOException, CredentialsRequiredException, ResponseException {

		givenRepositoryName("test-project");
		givenSetting("jira-project-key", "TEST");
		givenChangesets(Changeset.of("latest",
		                             "[maven-release-plugin] prepare release test-project-1.0.0"),
		                Changeset.of("older", "TEST-1"));

		givenLatestRefChange("latest", "master");
		givenOlderRefChange("older", "branch");

		whenPostReceive(latestRefChange);
		whenPostReceive(olderRefChange);

		thenDoesJiraVersionExist(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));
		thenGetChangesets(times(1), "master");
		thenGetChangesets(times(1), "branch");
	}

	@Test
	public void shouldGenerateJiraVersionAndLinkIssueWhenThereAreCommitsWithNoIssueKeyInMessage() throws IOException, CredentialsRequiredException, ResponseException {

		givenRepositoryName("test-project");
		givenSetting("jira-project-key", "TEST");
		givenChangesets(Changeset.of("1", "[maven-release-plugin] prepare release test-project-1.0.0"),
		                Changeset.of("2",
		                             "Merge pull request #3 in TEST/test-project from test to master"),
		                Changeset.of("3", "Merge pull request #2 in TEST/test-project from TEST-1"),
		                Changeset.of("4", "Merge pull request #1 in TEST/test-project from TEST-2"));
		givenJiraVersionDoesNotExist(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));

		givenLatestRefChange("1", "master");

		whenPostReceive(latestRefChange);

		thenShouldCreateJiraVersion(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));
		thenShouldAddVersionToIssues(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("1")),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("2")));
	}

	@Test
	public void shouldNotLinkIssuesThatAreNotPartOfVersionProject() throws IOException, CredentialsRequiredException, ResponseException {

		givenRepositoryName("test-project");
		givenSetting("jira-project-key", "TEST");
		givenChangesets(Changeset.of("1", "[maven-release-plugin] prepare release test-project-1.0.0"),
		                Changeset.of("2",
		                             "Merge pull request #298 in TEST/test-project from test to master"),
		                Changeset.of("3", "Merge pull request #2 in TEST/test-project from TEST-1"),
		                Changeset.of("3", "Merge pull request #295 in TEST/test-project from ABCD-1"),
		                Changeset.of("4", "Merge pull request #1 in TEST/test-project from TEST-2"));
		givenJiraVersionDoesNotExist(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));

		givenLatestRefChange("1", "master");

		whenPostReceive(latestRefChange);

		thenShouldCreateJiraVersion(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));
		thenShouldAddVersionToIssues(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("1")),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("2")));
	}

	@Test
	public void shouldLinkAllRelatedIssuesPresentInACommitMessage() throws IOException, CredentialsRequiredException, ResponseException {

		givenRepositoryName("test-project");
		givenSetting("jira-project-key", "TEST");
		givenChangesets(Changeset.of("1", "[maven-release-plugin] prepare release test-project-1.0.0"),
		                Changeset.of("2",
		                             "Merge pull request #298 in TEST/test-project from test to master"),
		                Changeset.of("3",
		                             "Merge pull request #2 in TEST/test-project from TEST-1, TEST-2, TEST-3"));
		givenJiraVersionDoesNotExist(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));

		givenLatestRefChange("1", "master");

		whenPostReceive(latestRefChange);

		thenShouldCreateJiraVersion(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()));
		thenShouldAddVersionToIssues(Version.of("1.0.0", new ProjectKey("TEST"), true, new Date()),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("1")),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("2")),
		                             IssueKey.of(new ProjectKey("TEST"), new IssueId("3")));
	}

	private void givenSetting(String key, String value) {

		given(settings.getString(eq(key), anyString())).willReturn(value);
	}

	private void givenJiraVersionDoesNotExist(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		given(jiraService.doesJiraVersionExist(version)).willReturn(false);
	}

	private void givenOlderRefChange(String hash, String branchName) {

		given(olderRefChange.getToHash()).willReturn(hash);
		given(olderRefChange.getRefId()).willReturn(branchName);
	}

	private void givenLatestRefChange(String hash, String branchName) {

		given(latestRefChange.getToHash()).willReturn(hash);
		given(latestRefChange.getRefId()).willReturn(branchName);
	}

	private void givenChangesets(com.atlassian.stash.content.Changeset... changesets) {

		given(changesetPage.getValues()).willReturn(ImmutableList.copyOf(changesets));
		given(historyService.getChangesets(any(Repository.class),
		                                   anyString(),
		                                   anyString(),
		                                   any(PageRequest.class))).willReturn(changesetPage);
	}

	private void givenRepositoryName(String value) {

		given(repository.getName()).willReturn(value);
	}

	private void whenPostReceive(RefChange refChange) {

		jiraVersionGeneratorHook.postReceive(repositoryHookContext, ImmutableList.of(refChange));
	}

	private void thenShouldAddVersionToIssues(Version version,
	                                          IssueKey... issueKeys) throws CredentialsRequiredException, ResponseException, JsonProcessingException {

		then(jiraService).should().addVersionToIssues(version, ImmutableList.copyOf(issueKeys));
	}

	private void thenShouldCreateJiraVersion(Version version) throws CredentialsRequiredException, ResponseException, JsonProcessingException {

		then(jiraService).should().createJiraVersion(version);
	}

	private void thenGetChangesets(VerificationMode verificationMode, String branchName) {

		then(historyService).should(verificationMode).getChangesets(eq(repository),
		                                                            eq(branchName),
		                                                            anyString(),
		                                                            any(PageRequest.class));
	}

	private void thenDoesJiraVersionExist(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		then(jiraService).should().doesJiraVersionExist(version);
	}
}