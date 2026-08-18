## 4.0.0

  - upgrade to Bitbucket Data Center 10.2.6 (from 9.4.17) and set minimum supported version to 10.2.6
  - upgrade to Java 21 (required by Bitbucket 10 / Atlassian Platform 8; Java 17 runtime support removed)
  - replace `PageUtils.newRequest(...)` with `new PageRequestImpl(...)` (`PageUtils` was removed from the public API in Bitbucket 10)
  - remove `bitbucket-page-objects` dependency (no longer used; previously provided JUnit transitively)
  - add explicit JUnit 4.13.2 test dependency (no longer available transitively after removing `bitbucket-page-objects`)
  - replace the removed `TestApplicationUser` test helper (dropped from `bitbucket-test-util` in Bitbucket 10) with a Mockito `Person` mock
  - bump AssertJ from 3.5.2 to 3.27.7 to address CVE-2026-24400
  - remove unused `jackson.version` property (Jackson is provided by the Bitbucket platform BOM)

## 3.0.1

  - Migrate from Sonatype OSSRH to Central Publishing Maven Plugin for Maven Central releases
  - Add GitHub Actions workflow to auto-create GitHub releases on tag push
  - Upgrade maven-gpg-plugin from 1.6 to 3.1.0
  - Update maven.yml to make it work
  - Update README.md to correlate with current changes

## 3.0.0

  - upgrade to Bitbucket Server 9.4.17
  - upgrade to Java 17
  - upgrade Lombok from 1.18.20 to 1.18.44 (1.18.22+ required for Java 17 support)
  - upgrade Mockito from 1.x to 5.23.0 (1.x uses cglib which is incompatible with Java 17 module system)
  - upgrade JaCoCo from 0.8.5 to 0.8.14 (0.8.5 does not support Java 17 class file format)
  - upgrade Jackson modules to 2.17.3 (aligned with Bitbucket Server BOM)
  - remove Guava dependency (Bitbucket Server DMZ blocks `com.google.common` packages from plugins; replaced with standard Java utilities: `List.of()`, `String.isEmpty()`)
  - remove unused joda-time imports
  - bundle Jackson modules (jsr310, jdk8, parameter-names) with jackson-core excluded (Bitbucket Server DMZ blocks these packages from plugins, but jackson-core/databind are exported by the platform)

## 2.0.1

  - upgrade to Bitbucket Server 6.5.1

## 2.0.0

  - upgrade to Bitbucket Server 4.8.2
  - upgrade to Java 8

## 1.1.0

  - added option to specify version commit pattern

## 1.0.0

  - first release