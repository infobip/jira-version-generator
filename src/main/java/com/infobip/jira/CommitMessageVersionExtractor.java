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

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class CommitMessageVersionExtractor {

    private static final String VERSION_REGEX_CAPTURING_GROUP_NAME = "version";

    private final Pattern versionPattern;

    public CommitMessageVersionExtractor(String repositoryName,
                                         String versionPattern) {

        requireNonNull(repositoryName);

        this.versionPattern = getPattern(repositoryName, versionPattern);
    }

    private Pattern getPattern(String repositoryName,
                               String versionPattern) {

        if (Strings.isNullOrEmpty(versionPattern)) {
            return defaultPattern(repositoryName);
        }

        return Pattern.compile(versionPattern);
    }

    private Pattern defaultPattern(String repositoryName) {

        return Pattern.compile("\\[maven-release-plugin\\] prepare release " + repositoryName +
                                       "-(?<" + VERSION_REGEX_CAPTURING_GROUP_NAME + ">.*)");
    }

    Optional<String> extractVersionName(@Nullable String commitMessage) {

        return Optional.ofNullable(commitMessage)
                .map(versionPattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(VERSION_REGEX_CAPTURING_GROUP_NAME));
    }
}
