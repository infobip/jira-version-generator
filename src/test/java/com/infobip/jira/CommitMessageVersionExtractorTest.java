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

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class CommitMessageVersionExtractorTest {

    @Test
    public void shouldSuccessfullyExtractVersionFromMavenReleaseMessage() {

        CommitMessageVersionExtractor extractor = new CommitMessageVersionExtractor("test-project");

        String actual = extractor.extractVersionName("[maven-release-plugin] prepare release test-project-1.0.0").orElse(null);

        then(actual).isEqualTo("1.0.0");
    }

    @Test
    public void shouldSuccessfullyExtractVersionFromReleaseMessage() {

        CommitMessageVersionExtractor extractor = new CommitMessageVersionExtractor("test-project", "Release (?<version>.*)");

        String actual = extractor.extractVersionName("Release 1.0.0").orElse(null);

        then(actual).isEqualTo("1.0.0");
    }

    @Test
    public void shouldFailToExtractVersionFromNonReleaseMessage() {

        CommitMessageVersionExtractor extractor = new CommitMessageVersionExtractor("test-project");

        String actual = extractor.extractVersionName("Non release commit message").orElse(null);

        then(actual).isNull();
    }

    @Test
    public void shouldHandleNull() {
        CommitMessageVersionExtractor extractor = new CommitMessageVersionExtractor("");

        String actual = extractor.extractVersionName(null).orElse(null);

        then(actual).isNull();
    }
}