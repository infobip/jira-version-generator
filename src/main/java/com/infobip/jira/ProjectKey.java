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

import lombok.Value;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 *  JIRA project key. For example in JIRA issue key "ABC-123" "ABC" represents the project key.
*/
@Value
public class ProjectKey {

	/**
	 * Pattern used to match extract Jira issue key from messages.
	 *
	 * @see <a href="http://confluence.atlassian.com/display/JIRA/Changing+the+Project+Key+Format">Changing the Project Key Format</a>
	 */
	private static final Pattern projectKeyPattern = Pattern.compile("[A-Z][A-Z0-9_]+");

    private final String value;

    public ProjectKey(String value) {
		if(!projectKeyPattern.matcher(value).matches()) {
			throw new IllegalArgumentException("Project key must match the JIRA project key format: [A-Z][A-Z0-9_]+");
		}

        this.value = Objects.requireNonNull(value);
    }
}
