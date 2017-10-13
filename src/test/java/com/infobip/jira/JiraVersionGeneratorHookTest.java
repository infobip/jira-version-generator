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
import com.atlassian.bitbucket.user.TestApplicationUser;
import com.atlassian.bitbucket.util.*;
import com.atlassian.sal.api.net.ResponseException;
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
import java.time.ZoneOffset;
import java.util.*;

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
    private CommitService commitService;

    @Mock
    private JiraService jiraService;

    @Mock
    private RepositoryHookContext repositoryHookContext;

    @Mock
    private Repository repository;

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
        given(latestRefChange.getToHash()).willReturn("latestRefChange");
        given(olderRefChange.getToHash()).willReturn("olderRefChange");
    }

    @Test
    public void shouldGenerateVersionWithNoIssues() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        Commit secondReleaseCommit = givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016);
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016),
                secondReleaseCommit);
        givenCommits(olderRefChange, secondReleaseCommit);

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
        Commit firstReleaseCommit = givenCommit("[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016);
        Commit secondReleaseCommit = givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016);
        givenCommits(latestRefChange, firstReleaseCommit, secondReleaseCommit);
        givenCommits(olderRefChange, secondReleaseCommit);

        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "infobip-test-1.0.1", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "infobip-test-1.0.1");
        thenGetCommits(times(1), latestRefChange);
        thenGetCommits(times(1), olderRefChange);
    }

    @Test
    public void shouldGenerateJiraVersionWithACustomVersionPattern() throws IOException, CredentialsRequiredException, ResponseException {

        givenSetting("jira-project-key", "TEST");
        givenSetting("release-commit-version-pattern", "Release (?<version>.*)");
        givenRepositoryName("test-project");
        Commit secondCommit = givenCommit("Release test-project-1.0.0", START_OF_2016);
        givenCommits(latestRefChange, givenCommit("Release 1.0.1", START_OF_2016), secondCommit);
        givenCommits(olderRefChange, secondCommit);

        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.1", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.1");
        thenGetCommits(times(1), latestRefChange);
        thenGetCommits(times(1), olderRefChange);
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssuesWhenHookEventOccursAfterAnotherCommit() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        Commit secondCommit = givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016);
        Commit thirdCommit = givenCommit("TEST-1", START_OF_2016);
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                secondCommit,
                thirdCommit);
        givenCommits(olderRefChange, secondCommit, thirdCommit);

        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(olderRefChange);
        whenPostReceive(latestRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.0");
        thenGetCommits(times(1), olderRefChange);
        thenGetCommits(times(1), latestRefChange);
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssuesOnlyForLatestRefChange() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        Commit secondCommit = givenCommit("TEST-1", START_OF_2016);
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                secondCommit);
        givenCommits(olderRefChange, secondCommit);

        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);
        whenPostReceive(olderRefChange);

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.0");
        thenGetCommits(times(1), latestRefChange);
        thenGetCommits(times(1), olderRefChange);
    }

    @Test
    public void shouldGenerateJiraVersionAndLinkIssueWhenThereAreCommitsWithNoIssueKeyInMessage() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                givenCommit("Merge pull request #3 in TEST/test-project from test to master", START_OF_2016),
                givenCommit("Merge pull request #2 in TEST/test-project from TEST-1", START_OF_2016),
                givenCommit("Merge pull request #1 in TEST/test-project from TEST-2", START_OF_2016));
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);

        then(jiraService).should().createJiraVersion(unreleasedSerializedVersion("1.0.0", "TEST"));

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2"))));
    }

    @Test
    public void shouldNotLinkIssuesThatAreNotPartOfVersionProject() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                givenCommit("Merge pull request #298 in TEST/test-project from test to master", START_OF_2016),
                givenCommit("Merge pull request #2 in TEST/test-project from TEST-1", START_OF_2016),
                givenCommit("Merge pull request #295 in TEST/test-project from ABCD-1", START_OF_2016),
                givenCommit("Merge pull request #1 in TEST/test-project from TEST-2", START_OF_2016));
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);

        then(jiraService).should().createJiraVersion(unreleasedSerializedVersion("1.0.0", "TEST"));

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2"))));
    }

    @Test
    public void shouldLinkAllRelatedIssuesPresentInACommitMessage() throws IOException, CredentialsRequiredException, ResponseException {

        givenRepositoryName("test-project");
        givenSetting("jira-project-key", "TEST");
        givenCommits(latestRefChange,
                givenCommit("[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016),
                givenCommit("Merge pull request #298 in TEST/test-project from test to master", START_OF_2016),
                givenCommit("Merge pull request #2 in TEST/test-project from TEST-1, TEST-2, TEST-3", START_OF_2016));
        givenJiraVersionDoesNotExist();
        givenCreatedVersion("1", "1.0.0", "TEST");

        whenPostReceive(latestRefChange);

        then(jiraService).should().createJiraVersion(unreleasedSerializedVersion("1.0.0", "TEST"));

        then(jiraService).should().addVersionToIssues("1.0.0", new ProjectKey("TEST"), Arrays.asList(new IssueKey(new ProjectKey("TEST"), new IssueId("1")), new IssueKey(new ProjectKey("TEST"), new IssueId("2")), new IssueKey(new ProjectKey("TEST"), new IssueId("3"))));
    }

    private void givenCreatedVersion(String id, String name, String project) {
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion(id, name, project, null, false));
    }

    private void givenSetting(String key, String value) {

        given(settings.getString(eq(key))).willReturn(value);
    }

    private void givenJiraVersionDoesNotExist() throws CredentialsRequiredException, ResponseException, IOException {

        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
    }

    private void givenCommits(RefChange refChange, Commit... commits) {

        CommitsBetweenRequest request = new CommitsBetweenRequest.Builder(repository).include(refChange.getToHash()).build();
        given(commitService.getCommitsBetween(refEq(request), any())).willReturn(new PageImpl<>(null, Arrays.asList(commits), true));
    }

    private void givenRepositoryName(String value) {

        given(repository.getName()).willReturn(value);
    }

    private void whenPostReceive(RefChange refChange) {

        jiraVersionGeneratorHook.postReceive(repositoryHookContext, ImmutableList.of(refChange));
    }

    private SerializedVersion unreleasedSerializedVersion(String name, String Project) {

        return new SerializedVersion(null, name, Project, null, false);
    }

    private void thenGetCommits(VerificationMode verificationMode, RefChange refChange) {

        CommitsBetweenRequest request = new CommitsBetweenRequest.Builder(repository)
                .include(refChange.getToHash())
                .build();

        then(commitService).should(verificationMode).getCommitsBetween(refEq(request),
                any(PageRequest.class));
    }

    Commit givenCommit(String message, LocalDate authorTimestamp) {
        return new SimpleCommit.Builder("id")
                .author(new TestApplicationUser(""))
                .message(message)
                .authorTimestamp(Date.from(authorTimestamp.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build();
    }

    Commit givenCommit(String message) {
        return new SimpleCommit.Builder("id")
                .author(new TestApplicationUser(""))
                .message(message)
                .build();
    }
}