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
import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.time.*;
import java.util.Iterator;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class JiraVersionGeneratorTest {

    private static final LocalDate START_OF_2016 = LocalDate.of(2016, 1, 1);

    private JiraVersionGenerator jiraVersionGenerator;

    @Mock
    private JiraService jiraService;

    @Mock
    private Iterator<Commit> commitIterator;

    @Mock
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
    }

    @Test
    public void shouldCheckIfCorrectJiraVersionExists() throws IOException, CredentialsRequiredException, ResponseException {

        givenJiraVersionGeneratorWithReleaseCommit(new SimpleCommit(
                null, "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016));
        given(commitIterator.hasNext()).willReturn(true, true, true, false);
        given(commitIterator.next())
                .willReturn(new SimpleCommit(null, "Merge pull request #276 in TEST/test-project from TEST-1", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));
        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion(null, "1.0.1", "TEST", null, null));

        jiraVersionGenerator.generate("", new ProjectKey("TEST"));

        then(jiraService).should().findVersion(new ProjectKey("TEST"), "1.0.1");
    }

    @Test
    public void shouldCreateJiraVersion() throws IOException, CredentialsRequiredException, ResponseException {

        givenJiraVersionGeneratorWithReleaseCommit(new SimpleCommit(
                null, "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016));
        given(commitIterator.hasNext()).willReturn(true, true, true, false);
        given(commitIterator.next())
                .willReturn(new SimpleCommit(null, "Merge pull request #276 in TEST/test-project from TEST-1", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));
        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion("1", "1.0.1", "TEST", null, null));

        jiraVersionGenerator.generate("", new ProjectKey("TEST"));

        then(jiraService).should().createJiraVersion(new SerializedVersion(null, "1.0.1", "TEST", null, false));
    }

    @Test
    public void shouldLinkIssuesToVersion() throws IOException, CredentialsRequiredException, ResponseException {

        givenJiraVersionGeneratorWithReleaseCommit(new SimpleCommit(
                "", "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016));
        given(commitIterator.hasNext()).willReturn(true, true, true, false);
        given(commitIterator.next())
                .willReturn(new SimpleCommit(null, "Merge pull request #276 in TEST/test-project from TEST-1", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));
        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion(null, "1.0.1", "TEST", null, null));

        jiraVersionGenerator.generate("", new ProjectKey("TEST"));

        then(jiraService).should().addVersionToIssues("1.0.1",
                new ProjectKey("TEST"),
                ImmutableList.of(new IssueKey(new ProjectKey("TEST"), new IssueId("1"))));
    }

    @Test
    public void shouldReleaseVersion() throws IOException, CredentialsRequiredException, ResponseException, ParseException {

        givenJiraVersionGeneratorWithReleaseCommit(new SimpleCommit(null, "[maven-release-plugin] prepare release test-project-1.0.1", START_OF_2016));
        given(commitIterator.hasNext()).willReturn(true, true, true, false);
        given(commitIterator.next())
                .willReturn(new SimpleCommit(null, "Merge pull request #276 in TEST/test-project from TEST-1", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare for next development iteration", START_OF_2016),
                        new SimpleCommit(null, "[maven-release-plugin] prepare release test-project-1.0.0", START_OF_2016));
        given(jiraService.findVersion(any(), any())).willReturn(Optional.empty());
        given(jiraService.createJiraVersion(any())).willReturn(new SerializedVersion(null, "1.0.1", "TEST", null, null));

        jiraVersionGenerator.generate("", new ProjectKey("TEST"));

        then(jiraService).should().releaseVersion(new SerializedVersion(null, "1.0.1", "TEST", null, null), LocalDate.of(2016, 1, 1));
    }

    private void givenJiraVersionGeneratorWithReleaseCommit(Commit commit) {

        jiraVersionGenerator = new JiraVersionGenerator(jiraService,
                commit,
                commitIterator,
                new CommitMessageVersionExtractor("test-project"), clock);
    }

}
