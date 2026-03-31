## 3.0.1

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