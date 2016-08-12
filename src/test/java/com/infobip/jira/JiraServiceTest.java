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

import com.atlassian.applinks.api.*;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.ResponseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class JiraServiceTest {

    @InjectMocks
    private JiraService jiraService;

    @Mock
    private ApplicationLinkService applicationLinkService;

    @Mock
    private ApplicationLink jiraApplicationLink;

    @Mock
    private ApplicationLinkRequestFactory applicationLinkRequestFactory;

    @Mock
    private ApplicationLinkRequest applicationLinkRequest;

    @Before
    public void setUp() throws CredentialsRequiredException {

        given(applicationLinkService.getPrimaryApplicationLink(any())).willReturn(jiraApplicationLink);
        given(jiraApplicationLink.createAuthenticatedRequestFactory()).willReturn(applicationLinkRequestFactory);
        given(applicationLinkRequestFactory.createRequest(any(), anyString())).willReturn(applicationLinkRequest);
        given(applicationLinkRequest.setHeader(any(), any())).willReturn(applicationLinkRequest);
        given(applicationLinkRequest.setRequestBody(any())).willReturn(applicationLinkRequest);
    }

    @Test
    public void shouldFindJiraVersion() throws CredentialsRequiredException, ResponseException {

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

        Optional<SerializedVersion> actual = jiraService.findVersion(new ProjectKey("TEST"), "2.0.0");

        then(actual).isPresent()
                .contains(new SerializedVersion("11780", "2.0.0", null, LocalDate.of(2014, 3, 3), true));
    }

    @Test
    public void shouldReleaseVersion() throws ResponseException, CredentialsRequiredException {

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

        jiraService.releaseVersion(new SerializedVersion("11780", "2.0.0", "TEST", null, false), LocalDate.of(2016, 1, 1));

        thenShouldSendRequest(MethodType.PUT, "/rest/api/2/version/11780", "{\"released\":true,\"releaseDate\":\"2016-01-01\"}");
    }

    @Test
    public void shouldAddVersionToIssues() throws CredentialsRequiredException {

        ProjectKey projectKey = new ProjectKey("TEST");
        jiraService.addVersionToIssues("1.0.0", projectKey, Collections.singletonList(new IssueKey(projectKey, new IssueId("1"))));

        String actualUrl = "/rest/api/2/issue/TEST-1";
        String actualBody = "{\"update\":{\"fixVersions\":[{\"add\":{\"name\":\"1.0.0\",\"project\":\"TEST\"}}]}}";
        thenShouldSendRequest(MethodType.PUT, actualUrl, actualBody);
    }

    private void thenShouldSendRequest(MethodType methodType, String url, String body) throws CredentialsRequiredException {

        BDDMockito.then(applicationLinkRequestFactory).should().createRequest(methodType, url);
        BDDMockito.then(applicationLinkRequest).should().setRequestBody(body);
    }

    private void givenReturnedJson(String json) throws ResponseException {

        given(applicationLinkRequest.execute()).willReturn(json);
    }
}
