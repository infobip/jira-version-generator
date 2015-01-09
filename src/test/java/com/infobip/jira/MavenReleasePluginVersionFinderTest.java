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

import static org.assertj.core.api.Assertions.assertThat;

public class MavenReleasePluginVersionFinderTest {

    private MavenReleasePluginVersionFinder mavenReleasePluginVersionFinder;

    private Optional<String> version;

    @Test
    public void shouldBeAbleToExtractVersionForAppcoreReleaseMessages() {

        givenRepositoryName("test-project");

        whenExtractVersion("[maven-release-plugin] prepare release test-project-1.0.0");

        assertThat(version).isEqualTo(Optional.of("1.0.0"));
    }

    private void givenRepositoryName(String repositoryName) {

        mavenReleasePluginVersionFinder = new MavenReleasePluginVersionFinder(repositoryName);

    }

    private void whenExtractVersion(String commitMessage) {

        version = mavenReleasePluginVersionFinder.extractVersionName(commitMessage);
    }
}