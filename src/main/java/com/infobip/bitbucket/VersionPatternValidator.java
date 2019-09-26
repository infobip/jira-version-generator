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

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

class VersionPatternValidator implements SettingsValidator {

    static final String SETTINGS_KEY = "release-commit-version-pattern";

    private static final Pattern versionValidationPattern = Pattern.compile(".*\\(\\?<version>.*\\).*");

    @Override
    public void validate(@Nonnull Settings settings,
                         @Nonnull SettingsValidationErrors settingsValidationErrors,
                         @Nonnull Scope scope) {

        String versionPattern = settings.getString(SETTINGS_KEY, "");

        if(versionPattern.isEmpty()) {
            return;
        }

        if (!versionValidationPattern.matcher(versionPattern).matches()) {
            settingsValidationErrors.addFieldError(SETTINGS_KEY,
                                                   "Version pattern doesn't contain named capturing group with a name \"version\"");
        }
    }
}
