# Jira Version Generator

![](https://github.com/infobip/jira-version-generator/workflows/maven/badge.svg)
[![Coverage Status](https://coveralls.io/repos/infobip/jira-version-generator/badge.png?branch=master)](https://coveralls.io/r/infobip/jira-version-generator?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.infobip/jira-version-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.infobip/jira-version-generator)

[Bitbucket Server](https://www.atlassian.com/software/bitbucket/server) [(post receive hook) plugin](https://confluence.atlassian.com/display/STASH/Using+repository+hooks#Usingrepositoryhooks-Post-receivehooks) that generates [Jira](https://www.atlassian.com/software/jira) version and links issues to the version.

## Contents

1. [News](#News)
2. [Requirements](#Requirements)
3. [Installation](#Installation)
4. [Usage](#Usage)
5. [Features](#Features)
    * [Core](#Core)
    * [Release commit version pattern](#ReleaseCommitVersionPattern)
    * [Version prefix](#VersionPrefix)
6. [Contributing](#Contributing)
7. [Useful Atlassian Plugin SDK commands](#UsefulAtlassianPluginSDKCommands)
8. [Credits](#Credits)
9. [License](#License)

## <a name="News"></a> News

### 2.0.1

Upgrade to Bitbucket Server 6.5.1.

For previous changes see the [changelog](https://github.com/infobip/jira-version-generator/blob/master/CHANGELOG.md).

## <a name="Requirements"></a> Requirements:

1. Bitbucket Server has an application link with a Jira instance.
2. User that commits the release commit exists on Jira and has rights to create the version and modify issues on the project.

By default, each commit on the repository is checked against the [Maven Release Plugin](http://maven.apache.org/maven-release/maven-release-plugin/) release commit message pattern: `\[maven-release-plugin\] prepare release repositoryName-(?<version>.*)`.
This behavior can be changed by changing the [release commit version pattern](#ReleaseCommitVersionPattern).

If the commit message matches the pattern, following actions are taken:

1. Version is extracted from the commit message
2. Jira issue keys are extracted from older commits in that branch until commit that matches the pattern is encountered
3. Jira version is created
4. Jira issues are linked to the Jira version

If the version already exists on Jira, the version will not be created and issues will not be linked to the version.

## <a name="Installation"></a> Installation:

Simply download latest jar from the Maven Central [here](https://maven-badges.herokuapp.com/maven-central/com.infobip/jira-version-generator) and install it on your Bitbucket Server instance.

## <a name="Usage"></a> Usage:

Jira version generator plugin needs to be activated for each repository:

![project-settings](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/project-settings.png)

JIRA project key parameter is required. Only issues with that project key will be updated on JIRA, others will be ignored.

JIRA version prefix is optional. See [Version prefix](#VersionPrefix) for more information about this parameter.

## <a name="Features"></a> Features:

### <a name="Core"></a> Core:

The core feature of this plugin is to extract information about releases from commit messages. For example, given the current state of the repo is

![commits-1.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/commits-1.png)

when a new commit arrives with a message `[maven-release-plugin] prepare release my-test-project-1.0.0`

![commits-2.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/commits-2.png)

the plugin generates the version 1.0.0 for the project and tags MTP-1, MTP-2 and MTP-3 issues with fix version 1.0.0.

![version.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/version.png)

### <a name="ReleaseCommitVersionPattern"></a> Release commit version pattern:

Optional configuration setting, default value is `\[maven-release-plugin\] prepare release repositoryName-(?<version>.*)`.

Used to extract release message from a commit. Must contain named capturing group with name "version".
Default pattern matches the maven release plugin release commit message with the repository name as artifactId.

![release-commit-version-pattern.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/release-commit-version-pattern.png)

### <a name="VersionPrefix"></a> Version prefix:

Optional configuration setting, default value is `""`.

Version prefix defines a static prefix that will be applied to every version created on JIRA.
For example, for version prefix defined as

![version-prefix.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/version-prefix.png)

release version will look like

![prefixed-version.png](https://raw.githubusercontent.com/infobip/jira-version-generator/master/docs/prefixed-version.png)

## <a name="Contributing"></a> Contributing

If you have an idea for a new feature or want to report a bug please use the issue tracker.

Pull requests are welcome!

## <a name="UsefulAtlassianPluginSDKCommands"></a>Useful Atlassian Plugin SDK commands (for development)

- `atlas-clean` - similar to mvn clean, removes local instances of products (Bitbucket Server, Jira)
- `atlas-debug` - runs the project product (Bitbucket Server) in debug mode, [guide for remote debugging](https://developer.atlassian.com/display/DOCS/Creating+a+Remote+Debug+Target)
- `atlas-run-standalone --product jira -v 6.4` - runs local Jira instance of version 6.4

## <a name="Credits"></a> Credits

The original project (before open sourcing) was conceived by [Marko Bjelac](https://github.com/mbjelac) and the implementation
and bug fixes were done by [Anja Hula](https://github.com/anhula) and [Lovro Pandzic](https://github.com/lpandzic).

## <a name="License"></a> License

This plugin is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
