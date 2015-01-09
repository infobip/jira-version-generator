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

import java.io.IOException;
import java.util.List;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author lpandzic
 */
public interface JiraService {

	boolean doesJiraVersionExist(Version version) throws CredentialsRequiredException, ResponseException, IOException;

	void createJiraVersion(Version version) throws
	                                        CredentialsRequiredException,
	                                        ResponseException,
	                                        JsonProcessingException;

	void addVersionToIssues(Version version, List<IssueKey> issueKeys) throws
	                                                                   CredentialsRequiredException,
	                                                                   ResponseException,
	                                                                   JsonProcessingException;

	void releaseVersion(Version version) throws CredentialsRequiredException, ResponseException, IOException;

}
