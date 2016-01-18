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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.commit.Commit;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author lpandzic
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraVersionGeneratorTest {

	private JiraVersionGenerator jiraVersionGenerator;

	@Mock
	private JiraService jiraService;

	@Mock
	private Iterator<Commit> changesetIterator;

	@Test
	public void shouldCheckIfCorrectJiraVersionExists() throws IOException, CredentialsRequiredException, ResponseException {

		givenJiraVersionGeneratorWithReleaseCommit(Changeset.of(
				"[maven-release-plugin] prepare release test-project-1.0.1"));
		given(changesetIterator.hasNext()).willReturn(true, true, true, false);
		given(changesetIterator.next())
				.willReturn(Changeset.of("Merge pull request #276 in TEST/test-project from TEST-1"),
				            Changeset.of("[maven-release-plugin] prepare for next development iteration"),
				            Changeset.of("[maven-release-plugin] prepare release test-project-1.0.0"));

		jiraVersionGenerator.generateJiraVersionAndLinkIssues("", new ProjectKey("TEST"));

		then(jiraService).should().doesJiraVersionExist(Version.of("1.0.1",
		                                                           new ProjectKey("TEST"),
		                                                           true,
		                                                           null));
	}

	@Test
	public void shouldCreateJiraVersion() throws IOException, CredentialsRequiredException, ResponseException {

		givenJiraVersionGeneratorWithReleaseCommit(Changeset.of(
				"[maven-release-plugin] prepare release test-project-1.0.1"));
		given(changesetIterator.hasNext()).willReturn(true, true, true, false);
		given(changesetIterator.next())
				.willReturn(Changeset.of("Merge pull request #276 in TEST/test-project from TEST-1"),
				            Changeset.of("[maven-release-plugin] prepare for next development iteration"),
				            Changeset.of("[maven-release-plugin] prepare release test-project-1.0.0"));
		given(jiraService.doesJiraVersionExist(any(Version.class))).willReturn(false);

		jiraVersionGenerator.generateJiraVersionAndLinkIssues("", new ProjectKey("TEST"));

		then(jiraService).should().createJiraVersion(Version.of("1.0.1",
		                                                        new ProjectKey("TEST"),
		                                                        true,
		                                                        null));
	}

	@Test
	public void shouldLinkIssuesToVersion() throws IOException, CredentialsRequiredException, ResponseException {

		givenJiraVersionGeneratorWithReleaseCommit(Changeset.of(
				"[maven-release-plugin] prepare release test-project-1.0.1"));
		given(changesetIterator.hasNext()).willReturn(true, true, true, false);
		given(changesetIterator.next())
				.willReturn(Changeset.of("Merge pull request #276 in TEST/test-project from TEST-1"),
				            Changeset.of("[maven-release-plugin] prepare for next development iteration"),
				            Changeset.of("[maven-release-plugin] prepare release test-project-1.0.0"));
		given(jiraService.doesJiraVersionExist(any(Version.class))).willReturn(false);

		jiraVersionGenerator.generateJiraVersionAndLinkIssues("", new ProjectKey("TEST"));

		then(jiraService).should().addVersionToIssues(Version.of("1.0.1",
		                                                         new ProjectKey("TEST"),
		                                                         true,
		                                                         null),
		                                              ImmutableList.of(IssueKey.of(new ProjectKey("TEST"),
		                                                                           new IssueId("1"))));
	}

	@Test
	public void shouldReleaseVersion() throws IOException, CredentialsRequiredException, ResponseException, ParseException {

		givenJiraVersionGeneratorWithReleaseCommit(Changeset.of(null,
		                                                        "[maven-release-plugin] prepare release test-project-1.0.1",
		                                                        "2015-01-01"));
		given(changesetIterator.hasNext()).willReturn(true, true, true, false);
		given(changesetIterator.next())
				.willReturn(Changeset.of("Merge pull request #276 in TEST/test-project from TEST-1"),
				            Changeset.of("[maven-release-plugin] prepare for next development iteration"),
				            Changeset.of("[maven-release-plugin] prepare release test-project-1.0.0"));
		given(jiraService.doesJiraVersionExist(any(Version.class))).willReturn(false);

		jiraVersionGenerator.generateJiraVersionAndLinkIssues("", new ProjectKey("TEST"));

		then(jiraService).should().releaseVersion(Version.of("1.0.1",
		                                                     new ProjectKey("TEST"),
		                                                     true,
		                                                     new SimpleDateFormat("yyyy-MM-dd").parse("2015-01-01")));
	}

	private void givenJiraVersionGeneratorWithReleaseCommit(Changeset changeset) {

		jiraVersionGenerator = new JiraVersionGenerator(jiraService,
		                                                changeset,
		                                                changesetIterator,
		                                                new CommitMessageVersionExtractor(
				                                                "test-project", null));
	}

}
