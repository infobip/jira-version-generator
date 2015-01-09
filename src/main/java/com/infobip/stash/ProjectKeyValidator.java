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
package com.infobip.stash;

import javax.annotation.Nonnull;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.infobip.jira.ProjectKey;

class ProjectKeyValidator implements RepositorySettingsValidator {

	static final String JIRA_PROJECT_KEY_SETTINGS_KEY = "jira-project-key";

	@Override
	public void validate(@Nonnull Settings settings,
	                     @Nonnull SettingsValidationErrors settingsValidationErrors,
	                     @Nonnull Repository repository) {

		try {
			ProjectKey.of(settings.getString(JIRA_PROJECT_KEY_SETTINGS_KEY, ""));
		} catch (IllegalArgumentException e) {
			settingsValidationErrors.addFieldError(JIRA_PROJECT_KEY_SETTINGS_KEY, e.getMessage());
		}
	}
}
