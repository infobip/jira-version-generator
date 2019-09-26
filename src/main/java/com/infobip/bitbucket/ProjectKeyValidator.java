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
package com.infobip.bitbucket;

import com.atlassian.bitbucket.scope.Scope;
import com.atlassian.bitbucket.setting.*;
import com.infobip.jira.ProjectKey;

import javax.annotation.Nonnull;

class ProjectKeyValidator implements SettingsValidator {

	static final String SETTINGS_KEY = "jira-project-key";

	@Override
	public void validate(@Nonnull Settings settings,
	                     @Nonnull SettingsValidationErrors settingsValidationErrors,
	                     @Nonnull Scope scope) {
		try {
			new ProjectKey(settings.getString(SETTINGS_KEY, ""));
		} catch (IllegalArgumentException e) {
			settingsValidationErrors.addFieldError(SETTINGS_KEY, e.getMessage());
		}
	}
}
