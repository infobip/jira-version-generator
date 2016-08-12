/**
 *# Copyright 2016 Infobip
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
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.infobip.infrastructure.ObjectMapperFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static com.atlassian.sal.api.net.Request.MethodType;

/**
 * @see <a href="https://docs.atlassian.com/jira/REST/6.4/">JIRA 6.4 REST API documentation</a>
 */
public class JiraService {

	private final ApplicationLinkService applicationLinkService;
	private final ObjectMapper objectMapper;

	public JiraService(ApplicationLinkService applicationLinkService) {

		this.applicationLinkService = applicationLinkService;
		this.objectMapper = ObjectMapperFactory.getInstance();
		this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	Optional<SerializedVersion> findVersion(ProjectKey projectKey, String name) {
		return getVersions(projectKey).stream()
				.filter(version -> Objects.equals(name, version.getName()))
				.findFirst();
	}

	SerializedVersion createJiraVersion(SerializedVersion version) {

		String responseJson = executeJsonHttpRequest(MethodType.POST, "/rest/api/2/version", version);

		return toObject(responseJson, SerializedVersion.class);
	}

	void addVersionToIssues(String versionName,
	                        ProjectKey projectKey,
	                        List<IssueKey> issueKeys) {

		for (IssueKey issueKey : issueKeys) {
			String body = String.format(
					"{\"update\":{\"fixVersions\":[{\"add\":{\"name\":\"%s\",\"project\":\"%s\"}}]}}",
					versionName,
					projectKey.getValue());
			executeJsonHttpRequest(MethodType.PUT, "/rest/api/2/issue/" + issueKey, body);
		}
	}

	void releaseVersion(SerializedVersion version, LocalDate releaseDate) {

		String body = String.format(
				"{\"released\":true,\"releaseDate\":\"%s\"}",
				releaseDate);

		executeJsonHttpRequest(MethodType.PUT, "/rest/api/2/version/" + version.getId(), body);
	}

	private List<SerializedVersion> getVersions(ProjectKey projectKey) {

		String url = getUrl("/rest/api/2/project/", projectKey.getValue(), "/versions");
		String versionsJson = executeJsonHttpRequest(MethodType.GET, url);

		try {
			return objectMapper.readValue(versionsJson, new TypeReference<List<SerializedVersion>>() {});
		} catch (IOException e) {
			throw new JiraServiceException("Failed ", e);
		}
	}

	private ApplicationLinkRequestFactory getJiraApplicationLinkRequestFactory() {

		ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);

		if (applicationLink == null) {
			throw new IllegalStateException("Primary JIRA application link does not exist!");
		}

		return applicationLink.createAuthenticatedRequestFactory();
	}

	private String executeJsonHttpRequest(MethodType methodType, String url, Object body) {

		return executeJsonHttpRequest(methodType, url, toJson(body));
	}

	private String executeJsonHttpRequest(MethodType methodType, String url, String body) {

		try {
			return getJiraApplicationLinkRequestFactory().createRequest(methodType, url)
					.setHeader("Content-Type", "application/json")
					.setRequestBody(body).execute();
		} catch (CredentialsRequiredException | ResponseException e) {
			throw new JiraServiceException("Failed to create request " + methodType + " " + url + " " + body, e);
		}
	}

	private String executeJsonHttpRequest(MethodType methodType, String url) {

		try {
			return getJiraApplicationLinkRequestFactory().createRequest(methodType, url)
					.setHeader("Content-Type", "application/json").execute();
		} catch (CredentialsRequiredException | ResponseException e) {
			throw new JiraServiceException("Failed to create request " + methodType + " " + url, e);
		}
	}

	private String getUrl(String... parts) {

		return Joiner.on("").join(parts);
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new JiraServiceException("Failed to write to json " + value, e);
		}
	}

	private <T> T toObject(String json, Class<T> objectClass) {
		try {
			return objectMapper.readValue(json, objectClass);
		} catch (IOException e) {
			throw new JiraServiceException("Failed to deserialize " + json + " to " + objectClass, e);
		}
	}
}
