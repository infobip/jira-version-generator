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
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lpandzic
 */
public class IssueKeyTest {

    private Changeset changeset;
    private Optional<IssueKey> jiraIssueKey;

    @Test
    public void shouldBeAbleToExtractJiraIssueKeyFromIssueCommit() {

        givenChangeset("Merge pull request #270 in TEST/test-project from TEST-1-first-task");

        whenGenerateJiraIssueKey();

        thenProjectKeyAndIssueNumberShouldBe("TEST", "1");
    }

    @Test
    public void shouldProduceValidIssueKeyString() {

        Optional<IssueKey> issueKey = IssueKey.from(Changeset.of("TEST-123"));

        assertThat(issueKey.get().toString()).isEqualTo("TEST-123");

    }

    private void whenGenerateJiraIssueKey() {

        jiraIssueKey = IssueKey.from(changeset);
    }

    private void givenChangeset(String message) {

        changeset = Changeset.of(message);
    }

    private void thenProjectKeyAndIssueNumberShouldBe(String projectKey, String issueNumber) {

        assertThat(jiraIssueKey.isPresent()).isTrue();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jiraIssueKey.get().getProjectKey().value).isEqualTo(projectKey);
        softAssertions.assertThat(jiraIssueKey.get().getIssueId().value).isEqualTo(issueNumber);
        softAssertions.assertAll();
    }
}
