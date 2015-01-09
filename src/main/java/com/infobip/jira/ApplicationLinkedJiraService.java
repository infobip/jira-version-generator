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
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.List;

import static com.atlassian.sal.api.net.Request.MethodType;

/**
 * {@link JiraService} implementation based on JIRA 6.1.3 REST API.
 *
 * @author lpandzic
 * @see <a href="https://docs.atlassian.com/jira/REST/6.1.3/">JIRA 6.1.3 REST API documentation</a>
 */
public class ApplicationLinkedJiraService implements JiraService {

	private final ApplicationLinkService applicationLinkService;
	private final ObjectMapper objectMapper;

	public ApplicationLinkedJiraService(ApplicationLinkService applicationLinkService) {

		this.applicationLinkService = applicationLinkService;
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	/**
	 * Checks if the version exists by iterating over all versions.
	 *
	 * @param version to search for
	 *
	 * @return true if the version exists, false otherwise
	 *
	 * @see <a href="http://answers.atlassian.com/questions/203842/how-to-get-the-id-of-a-project-version-through-the-rest-api">How to get the {id} of a project version through the REST api?</a>
	 * @see <a href="https://docs.atlassian.com/jira/REST/6.1.3/#d2e231">Rest API</a>
	 */
	public boolean doesJiraVersionExist(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		Version[] existingVersions = getVersions(version);

		for (Version existingVersion : existingVersions) {
			if (version.name.equals(existingVersion.name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see <a href="https://docs.atlassian.com/jira/REST/6.1.3/#d2e3548">Rest API</a>
	 */
	@Override
	public void createJiraVersion(Version version) throws CredentialsRequiredException, ResponseException, JsonProcessingException {

		sendJiraApplicationLinkRequest(MethodType.POST, "/rest/api/2/version")
				.setHeader("Content-Type", "application/json")
				.setRequestBody(objectMapper.writeValueAsString(version)).execute();
	}

	/**
	 * @see <a href="https://docs.atlassian.com/jira/REST/6.1.3/#d2e1013">Rest API</a>
	 */
	@Override
	public void addVersionToIssues(Version version,
								   List<IssueKey> issueKeys) throws CredentialsRequiredException, ResponseException, JsonProcessingException {

		for (IssueKey issueKey : issueKeys) {
			String updateJson = String.format(
					"{\"update\":  {\"fixVersions\": [{\"add\": {\"name\":\"%s\", \"project\":\"%s\"}}] } }",
					version.name,
					version.getProject());
			sendJiraApplicationLinkRequest(MethodType.PUT, "/rest/api/2/issue/" + issueKey)
					.setHeader("Content-Type", "application/json")
					.setEntity(updateJson).execute();
		}
	}

	@Override
	public void releaseVersion(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		Version[] existingVersions = getVersions(version);

		Integer versionId = null;

		for (Version existingVersion : existingVersions) {
			if (version.name.equals(existingVersion.name)) {
				versionId = existingVersion.getId();
				break;
			}
		}

		if (versionId == null) {
			return;
		}

		String updateJson = String.format(
				"{\"released\":  true, \"releaseDate\":  \"%s\"}",
				version.releaseDate);

		sendJiraApplicationLinkRequest(MethodType.PUT, "/rest/api/2/version/" + versionId)
				.setHeader("Content-Type", "application/json")
				.setEntity(updateJson)
				.execute();
	}

	private Version[] getVersions(Version version) throws CredentialsRequiredException, ResponseException, IOException {

		String url = getUrl("/rest/api/2/project/", version.projectKey.get().value, "/versions");
		ApplicationLinkRequest jiraApplicationLinkRequest = sendJiraApplicationLinkRequest(MethodType.GET, url);
		String versionsJson = jiraApplicationLinkRequest.execute();

		return objectMapper.readValue(versionsJson, Version[].class);
	}

	private ApplicationLinkRequestFactory getJiraApplicationLinkRequestFactory() {

		ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);

		if (applicationLink == null) {
			throw new IllegalStateException("Primary JIRA application link does not exist!");
		}

		return applicationLink.createAuthenticatedRequestFactory();
	}

	private ApplicationLinkRequest sendJiraApplicationLinkRequest(MethodType methodType,
																  String url) throws CredentialsRequiredException {

		return getJiraApplicationLinkRequestFactory().createRequest(methodType, url);
	}

	private String getUrl(String... parts) {

		return Joiner.on("").join(parts);
	}
}
