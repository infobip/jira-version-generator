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

import com.google.common.base.Optional;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Optional.absent;
import static java.util.Objects.requireNonNull;

/**
 * @author lpandzic
 */
public class MavenReleasePluginVersionFinder {

    private final Pattern mavenReleasePluginVersionPattern;

    public MavenReleasePluginVersionFinder(@Nonnull String repositoryName) {

        requireNonNull(repositoryName);
        mavenReleasePluginVersionPattern = Pattern.compile("\\[maven-release-plugin\\] prepare release " +
                                                                   repositoryName +
                                                                   "-(.*)");
    }

    Optional<String> extractVersionName(@Nonnull String commitMessage) {

        Matcher matcher = mavenReleasePluginVersionPattern.matcher(commitMessage);

        if (!matcher.find()) {
            return absent();
        }

        return Optional.of(matcher.group(1));
    }
}
