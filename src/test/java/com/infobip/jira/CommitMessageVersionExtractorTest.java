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
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class CommitMessageVersionExtractorTest {

    private CommitMessageVersionExtractor commitMessageVersionExtractor;

    private Optional<String> version;

    private String repositoryName;
    private String versionPattern;

    @Test
    public void shouldSuccessfullyExtractVersionFromMavenReleaseMessage() {

        givenRepositoryName("test-project");
        givenNoVersionPattern();

        whenExtractVersion("[maven-release-plugin] prepare release test-project-1.0.0");

        then(version).isEqualTo(Optional.of("1.0.0"));
    }

    @Test
    public void shouldSuccessfullyExtractVersionFromReleaseMessage() {

        givenRepositoryName("test-project");
        givenVersionPattern("Release (?<version>.*)");

        whenExtractVersion("Release 1.0.0");

        then(version).isEqualTo(Optional.of("1.0.0"));
    }

    @Test
    public void shouldFailToExtractVersionFromNonReleaseMessage() {

        givenRepositoryName("test-project");
        givenNoVersionPattern();

        whenExtractVersion("Non release commit message");

        then(version).isEqualTo(Optional.absent());
    }

    private void givenRepositoryName(String repositoryName) {

        this.repositoryName = repositoryName;
    }

    private void givenNoVersionPattern() {

        this.versionPattern = null;
    }

    private void givenVersionPattern(String regex) {

        this.versionPattern = regex;
    }

    private void whenExtractVersion(String commitMessage) {

        commitMessageVersionExtractor = new CommitMessageVersionExtractor(repositoryName, versionPattern);
        version = commitMessageVersionExtractor.extractVersionName(commitMessage);
    }
}