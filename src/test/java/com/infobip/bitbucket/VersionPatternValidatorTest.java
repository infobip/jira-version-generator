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
public class VersionPatternValidatorTest {

    private VersionPatternValidator versionPatternValidator = new VersionPatternValidator();

    @Mock
    private Settings settings;

    @Mock
    private SettingsValidationErrors settingsValidationErrors;

    @Mock
    private Scope scope;

    @Test
    public void shouldSuccessfullyValidatePatternWithNamedCapturingGroup() {

        givenSetting("Release (?<version>.*) version.");

        whenValidate();

        then(settingsValidationErrors).should(never()).addFieldError(anyString(), anyString());
    }

    @Test
    public void shouldSuccessfullyValidateEmptyString() {

        givenSetting("");

        whenValidate();

        then(settingsValidationErrors).should(never()).addFieldError(anyString(), anyString());
    }

    @Test
    public void shouldFailToValidatePatternWithoutNamedCapturingGroup() {

        givenSetting("(.*)");

        whenValidate();

        then(settingsValidationErrors).should().addFieldError(eq(VersionPatternValidator.SETTINGS_KEY), anyString());
    }

    private void givenSetting(String setting) {

        given(settings.getString(anyString(), anyString())).willReturn(setting);
    }

    private void whenValidate() {

        versionPatternValidator.validate(settings, settingsValidationErrors, scope);
    }
}