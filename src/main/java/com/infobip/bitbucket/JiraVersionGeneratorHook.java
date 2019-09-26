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
package com.infobip.bitbucket;

import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.scope.Scope;
import com.atlassian.bitbucket.setting.*;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.infobip.infrastructure.ClockFactory;
import com.infobip.jira.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

public class JiraVersionGeneratorHook implements PostRepositoryHook, SettingsValidator {

    private static final Logger logger = LoggerFactory.getLogger(JiraVersionGeneratorHook.class);

    private final CommitService commitService;
    private final JiraService jiraService;
    private final ImmutableList<SettingsValidator> settingsValidators;

    public JiraVersionGeneratorHook(CommitService commitService, JiraService jiraService) {

        this.commitService = commitService;
        this.jiraService = jiraService;

        settingsValidators = ImmutableList.of(new ProjectKeyValidator(), new VersionPatternValidator());
    }

    @Override
    public void postUpdate(@Nonnull PostRepositoryHookContext context, @Nonnull RepositoryHookRequest request) {

        Collection<RefChange> refChanges = request.getRefChanges();

        if (refChanges.size() != 1) {
            return;
        }

        RefChange refChange = refChanges.iterator().next();

        try {
            postReceive(context, request, refChange);
        } catch (NoSuchCommitException ignored) {
            // branch was deleted
        } catch (RuntimeException e) {
            logger.error("Failed to generate jira JIRA version and link issues", e);
        }
    }

    private void postReceive(RepositoryHookContext context, RepositoryHookRequest request, RefChange change) {

        JiraVersionGenerator jiraVersionGenerator = createJiraVersionGenerator(context, request, change);

        ProjectKey projectKey = new ProjectKey(requireNonEmptySetting(context, ProjectKeyValidator.SETTINGS_KEY));
        String jiraVersionPrefix = getNonEmptySetting(context, "jira-version-prefix").orElse("");
        jiraVersionGenerator.generate(jiraVersionPrefix, projectKey);
    }

    @Override
    public void validate(@Nonnull Settings settings,
                         @Nonnull SettingsValidationErrors validationErrors,
                         @Nonnull Scope scope) {
        settingsValidators.forEach(validator -> validator.validate(settings, validationErrors, scope));
    }

    private Optional<String> getNonEmptySetting(RepositoryHookContext context, String key) {
        String setting = Strings.emptyToNull(context.getSettings().getString(key));
        return Optional.ofNullable(setting);
    }

    private String requireNonEmptySetting(RepositoryHookContext context, String key) {
        String setting = context.getSettings().getString(key);

        if (Strings.isNullOrEmpty(setting)) {
            String message = String.format("%s hook setting is not set to a non empty value", key);
            throw new IllegalStateException(message);
        }

        return setting;
    }

    private JiraVersionGenerator createJiraVersionGenerator(RepositoryHookContext context,
                                                            RepositoryHookRequest request,
                                                            RefChange refChange) {

        Iterator<Commit> commitIterator = CommitPageCrawler.of(commitService, request.getRepository(),
                                                               refChange);

        Commit releaseCommit = commitIterator.next();

        String repositoryName = request.getRepository().getName();

        CommitMessageVersionExtractor commitMessageVersionExtractor = getNonEmptySetting(context,
                                                                                         VersionPatternValidator.SETTINGS_KEY)
                .map(versionPattern -> new CommitMessageVersionExtractor(repositoryName, versionPattern))
                .orElseGet(() -> new CommitMessageVersionExtractor(repositoryName));

        return new JiraVersionGenerator(jiraService,
                                        releaseCommit,
                                        commitIterator,
                                        commitMessageVersionExtractor,
                                        ClockFactory.getInstance());
    }
}
