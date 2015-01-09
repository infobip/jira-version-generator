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

import com.atlassian.applinks.api.*;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * @author lpandzic
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationLinkedJiraServiceTest {

	@InjectMocks
	private ApplicationLinkedJiraService applicationLinkedJiraService;

	@Mock
	private ApplicationLinkService applicationLinkService;

	@Mock
	private ApplicationLink jiraApplicationLink;

	@Mock
	private ApplicationLinkRequestFactory applicationLinkRequestFactory;

	@Mock
	private ApplicationLinkRequest applicationLinkRequest;

	private boolean actual;

	@Before
	public void setUp() throws CredentialsRequiredException {

		givenJiraApplicationLink();
		givenJiraApplicationLinkRequestFactory();
		givenApplicationLinkRequest();
	}

	@Test
	public void shouldBeAbleToDetectExistingJiraVersion() throws IOException, CredentialsRequiredException, ResponseException {

		givenReturnedJson("[\n" +
								  "    {\n" +
								  "        \"self\": \"https://jira.infobip.com/rest/api/2/version/11780\",\n" +
								  "        \"id\": \"11780\",\n" +
								  "        \"description\": \"first release\",\n" +
								  "        \"name\": \"2.0.0\",\n" +
								  "        \"archived\": false,\n" +
								  "        \"released\": true,\n" +
								  "        \"releaseDate\": \"2014-03-03\",\n" +
								  "        \"userReleaseDate\": \"03/Mar/14\",\n" +
								  "        \"projectId\": 10901\n" +
								  "    }\n" +
								  "]");

		whenDoesJiraVersionExist(Version.of("2.0.0",
											new ProjectKey("TEST"),
											false,
											new Date()));

		thenJiraVersionExists();
	}

	@Test
	public void shouldBeAbleToReleaseVersion() throws ResponseException, CredentialsRequiredException, IOException, ParseException {

		givenReturnedJson("[\n" +
								  "    {\n" +
								  "        \"self\": \"https://jira.infobip.com/rest/api/2/version/11780\",\n" +
								  "        \"id\": \"11780\",\n" +
								  "        \"description\": \"first release\",\n" +
								  "        \"name\": \"2.0.0\",\n" +
								  "        \"archived\": false,\n" +
								  "        \"released\": false,\n" +
								  "        \"releaseDate\": \"2014-03-03\",\n" +
								  "        \"userReleaseDate\": \"03/Mar/14\",\n" +
								  "        \"projectId\": 10901\n" +
								  "    }\n" +
								  "]");

		whenReleaseVersion(Version.of("2.0.0",
									  new ProjectKey("TEST"),
									  false,
									  new SimpleDateFormat("yyyy-MM-dd").parse("2014-08-25")));

		thenVersionIsReleased(11780, "2014-08-25");

	}

	private void thenVersionIsReleased(Integer versionId, String date) throws CredentialsRequiredException {

		then(applicationLinkRequestFactory).should().createRequest(Request.MethodType.PUT, "/rest/api/2/version/" + versionId);
		then(applicationLinkRequest).should().setEntity(String.format(
				"{\"released\":  true, \"releaseDate\":  \"%s\"}", date));
	}

	private void whenReleaseVersion(Version version) throws IOException, CredentialsRequiredException, ResponseException {

		applicationLinkedJiraService.releaseVersion(version);
	}

	private void givenReturnedJson(String json) throws ResponseException {

		given(applicationLinkRequest.execute()).willReturn(json);
	}

	private void givenApplicationLinkRequest() throws CredentialsRequiredException {

		given(applicationLinkRequestFactory.createRequest(any(Request.MethodType.class), anyString())).willReturn(
				applicationLinkRequest);
		given(applicationLinkRequest.setHeader(anyString(), anyString())).willReturn(
				applicationLinkRequest);
		given(applicationLinkRequest.setEntity(anyString())).willReturn(
				applicationLinkRequest);
	}

	private void givenJiraApplicationLinkRequestFactory() {

		given(jiraApplicationLink.createAuthenticatedRequestFactory()).willReturn(applicationLinkRequestFactory);
	}

	@SuppressWarnings("unchecked")
	private void givenJiraApplicationLink() {

		given(applicationLinkService.getPrimaryApplicationLink(Matchers.any(Class.class))).willReturn(
				jiraApplicationLink);
	}

	private void whenDoesJiraVersionExist(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		actual = applicationLinkedJiraService.doesJiraVersionExist(version);
	}

	private void thenJiraVersionExists() {

		assertThat(actual).isTrue();
	}
}
