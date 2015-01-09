# Jira Version Generator

Stash (post receive hook) plugin that generates Jira version and links issues to the version.

Note that this plugin depends on Java version 7.

## Contents

1. [Preconditions](#Preconditions)
2. [Contributing](#Contributing)
3. [Useful Atlassian Plugin SDK commands](#UsefulAtlassianPluginSDKCommands)
4. [License](#License)

## <a name="Preconditions"></a> Preconditions:

1. Stash has an application link with a Jira instance.
2. Project artifactId matches the name of the repository.
3. User that commits the release commit exists on Jira and has rights to create the version and modify issues on the project.

Each commit on the repository is checked against Maven Release Plugin release commit message pattern (`[maven-release-plugin] prepare release ${artifactId}-${version}`).

If the commit message matches the pattern:

1. Version is extracted from the commit message
2. Jira issue keys are extracted from older commits in that branch until commit that matches the pattern is encountered.
3. Jira version is created
4. Jira issues are linked to the Jira version

If the version already exists on the Jira the version will not be created and issues will not be linked to the version.

## <a name="Contributing"></a> Contributing

If you have an idea for a new feature or want to report a bug please use the issue tracker.

Pull requests are welcome!

## <a name="UsefulAtlassianPluginSDKCommands"></a>Useful Atlassian Plugin SDK commands (for development)

atlas-clean - similar to mvn clean, removes local instances of products (Stash, Jira)
atlas-debug - runs the project product (Stash) in debug mode, [guide for remote debugging](https://developer.atlassian.com/display/DOCS/Creating+a+Remote+Debug+Target)
atlas-run --product jira -v 6.1.3 - runs local Jira instance of version 6.1.3
atlas-cli - interactive command line interface - passing pi reinstalls the plugin without restarting the stash server

Note: JDK 1.7 is required for local development (JDK 8 is not supported yet).

## <a name="License"></a> License

This plugin is licensed under the Apache License, Version 2.0.