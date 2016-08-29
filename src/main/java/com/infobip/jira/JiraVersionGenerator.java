/**
 * # Copyright 2016 Infobip
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * # http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */
package com.infobip.jira;

import com.atlassian.bitbucket.commit.Commit;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class JiraVersionGenerator {

    private final JiraService jiraService;
    private final Commit releaseCommit;
    private final Iterator<Commit> commitIterator;
    private final CommitMessageVersionExtractor commitMessageVersionExtractor;
    private final Clock clock;

    public JiraVersionGenerator(JiraService jiraService,
                                Commit releaseCommit,
                                Iterator<Commit> commitIterator,
                                CommitMessageVersionExtractor commitMessageVersionExtractor,
                                Clock clock) {

        this.releaseCommit = releaseCommit;

        this.jiraService = requireNonNull(jiraService);
        this.commitIterator = requireNonNull(commitIterator);
        this.commitMessageVersionExtractor = requireNonNull(commitMessageVersionExtractor);
        this.clock = clock;
    }

    public void generate(String jiraVersionPrefix,
                         ProjectKey projectKey) {

        if (!commitIterator.hasNext()) {
            return;
        }

        commitMessageVersionExtractor.extractVersionName(releaseCommit.getMessage())
                .ifPresent(versionName -> generate(jiraVersionPrefix, projectKey, versionName));

    }

    private void generate(String jiraVersionPrefix,
                          ProjectKey projectKey,
                          String versionName) {

        String prefixedVersionName = jiraVersionPrefix + versionName;
        List<Commit> versionCommits = getAllCommitsNewerThanPreviousRelease();

        SerializedVersion version = jiraService.findVersion(projectKey, prefixedVersionName)
                .orElseGet(() -> createNewVersion(projectKey, prefixedVersionName));

        List<IssueKey> issuesSolvedInVersion = getIssueKeys(versionCommits, projectKey);
        jiraService.addVersionToIssues(version.getName(), projectKey, issuesSolvedInVersion);
        LocalDate releaseDate = releaseCommit.getAuthorTimestamp().toInstant().atZone(clock.getZone()).toLocalDate();
        jiraService.releaseVersion(version, releaseDate);
    }

    private SerializedVersion createNewVersion(ProjectKey projectKey, String prefixedVersionName) {
        SerializedVersion version = new SerializedVersion(null, prefixedVersionName, projectKey.getValue(), null, false);
        return jiraService.createJiraVersion(version);
    }

    private List<IssueKey> getIssueKeys(List<Commit> versionCommits, ProjectKey projectKey) {

        return versionCommits.stream()
                .flatMap(commit -> getIssueKeys(commit, projectKey).stream())
                .collect(Collectors.toList());
    }

    private List<IssueKey> getIssueKeys(Commit commit, ProjectKey projectKey) {

        return Optional.ofNullable(commit.getMessage())
                .map(message -> {
                    Pattern pattern = Pattern.compile(projectKey.getValue() + "-([0-9]+)");
                    Matcher matcher = pattern.matcher(message);
                    List<IssueKey> issueKeys = new ArrayList<>();
                    while (matcher.find()) {
                        issueKeys.add(new IssueKey(projectKey, new IssueId(matcher.group(1))));
                    }
                    return issueKeys;
                }).orElse(Collections.emptyList());
    }

    private List<Commit> getAllCommitsNewerThanPreviousRelease() {

        List<Commit> versionCommit = new ArrayList<>();

        while (commitIterator.hasNext()) {
            Commit commit = commitIterator.next();

            if (commitMessageVersionExtractor.extractVersionName(commit.getMessage()).isPresent()) {
                break;
            }

            versionCommit.add(commit);
        }
        return versionCommit;
    }
}
