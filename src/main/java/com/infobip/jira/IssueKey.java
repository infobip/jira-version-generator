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

import com.atlassian.stash.content.Changeset;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JIRA issue key. For example ABC-123.
 *
 * @author lpandzic
 */
@Immutable
public class IssueKey {

    /**
     * Pattern used to match extract Jira issue key from messages.
     *
     * @see <a href="http://confluence.atlassian.com/display/JIRA/Changing+the+Project+Key+Format">Changing the Project Key Format</a>
     */
    private static final Pattern pattern = Pattern.compile("([A-Z][A-Z0-9_]+)-([0-9]+)");

    private final ProjectKey projectKey;
    private final IssueId issueId;

    /**
     * Generates {@link IssueKey IssueKeys} from {@link Changeset#getMessage()}  changesets message}.
     *
     * @param changeset containing message with Jira issue key
     *
     * @return all {@link IssueKey IssueKeys} that could be extracted from
     */
    public static Iterable<IssueKey> of(Changeset changeset) {

        Matcher matcher = pattern.matcher(changeset.getMessage());

        ImmutableList.Builder<IssueKey> builder = ImmutableList.builder();

        while(matcher.find()) {
            builder.add(new IssueKey(new ProjectKey(matcher.group(1)), new IssueId(matcher.group(2))));
        }

        return builder.build();
    }

    /**
     * Generates {@link IssueKey} from {@link Changeset changesets} message.
     *
     * @param changeset containing message with Jira issue key
     *
     * @return first {@link IssueKey} that could be extracted, else {@link Optional#absent()}
     */
    public static Optional<IssueKey> from(Changeset changeset) {

        Matcher matcher = pattern.matcher(changeset.getMessage());

        if (!matcher.find()) {
            return Optional.absent();
        }

        return Optional.of(new IssueKey(new ProjectKey(matcher.group(1)), new IssueId(matcher.group(2))));
    }

    public static IssueKey of(@Nonnull ProjectKey projectKey, @Nonnull IssueId issueId) {

        return new IssueKey(projectKey, issueId);
    }

    @Nonnull
    public ProjectKey getProjectKey() {

        return projectKey;
    }

    @Nonnull
    public IssueId getIssueId() {

        return issueId;
    }

    private IssueKey(@Nonnull ProjectKey projectKey, @Nonnull IssueId issueId) {

        this.projectKey = Objects.requireNonNull(projectKey);
        this.issueId = Objects.requireNonNull(issueId);
    }

    @Override
    public String toString() {

        return projectKey + "-" + issueId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final IssueKey issueKey = (IssueKey) o;

        if (!issueId.equals(issueKey.issueId)) {
            return false;
        }
        if (!projectKey.equals(issueKey.projectKey)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = projectKey.hashCode();
        result = 31 * result + issueId.hashCode();
        return result;
    }
}
