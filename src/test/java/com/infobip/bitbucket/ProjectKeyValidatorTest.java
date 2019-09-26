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
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class ProjectKeyValidatorTest {

    private ProjectKeyValidator projectKeyValidator = new ProjectKeyValidator();

    @Mock
    private Settings settings;

    @Mock
    private SettingsValidationErrors settingsValidationErrors;

    @Mock
    private Scope scope;

    @Test
    public void shouldAddErrorsForEmptyProjectKeyString() {

        givenSetting("jira-project-key", "");

        whenValidate();

        then(settingsValidationErrors).should()
                                      .addFieldError("jira-project-key",
                                                     "Project key must match the JIRA project key format: [A-Z][A-Z0-9_]+");
    }

    @Test
    public void shouldAddErrorsForIncorrectProjectKey() {

        givenSetting("jira-project-key", "1A");

        whenValidate();

        then(settingsValidationErrors).should()
                                      .addFieldError("jira-project-key",
                                                     "Project key must match the JIRA project key format: [A-Z][A-Z0-9_]+");
    }

    @Test
    public void shouldNotAddErrorForCorrectProjectKey() {

        givenSetting("jira-project-key", "TEST");

        whenValidate();

        then(settingsValidationErrors).should(never()).addFieldError(anyString(), anyString());
    }

    private void givenSetting(String key, String value) {

        given(settings.getString(eq(key), anyString())).willReturn(value);
    }

    private void whenValidate() {

        projectKeyValidator.validate(settings, settingsValidationErrors, scope);
    }
}
