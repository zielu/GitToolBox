GitToolBox - plugin for Jetbrains IDEs
======================================
![Build Status](https://github.com/zielu/GitToolBox/workflows/Build/badge.svg?branch=master)

Plugin for family of Jetbrains IDEs that expands build-in `Git Integration`.

## Features
For list of features see [the manual](https://github.com/zielu/GitToolBox/wiki/Manual)

## Change notes
For list of change notes see [changenotes](./GitToolBox/change-notes.html)

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7499-gittoolbox)

### EAP builds
Add https://plugins.jetbrains.com/plugins/eap/7499 in **Settings > Plugins > Browse repositories... > Manage 
repositories...** to receive early access builds.

## Architecture decisions record
Record of decisions is [here](https://github.com/zielu/GitToolBox/wiki/ADR).

## Building & running

### Releases

#### Release current `-SNAPSHOT`
```shell script
gradle clean release
```
#### Upgrade to next minor version
```shell script
gradle clean releaseMinorVersion
```
#### Upgrade to next major version
```shell script
gradle clean releaseMajorVersion
```

#### Release and publish
Append ```-Ppublish=true``` and include ```publishPlugin``` task.
For example
```shell script
gradle clean release publishPlugin -Ppublish=true
```

### Useful build commands
Full verification of build
```shell script
gradle clean check integrationTest
```
Quick verification of build
```shell script
gradle clean check
```
Generate code coverage report
```shell script
gradle clean codeCoverage
```
Update gradlew version
```shell script
gradle wrapper --gradle-version 5.6.4 --distribution-type ALL
```
Build to install with Install from disk
```shell script
gradle buildPlugin
```

### Useful run commands
Run with previous sandbox contents
```shell script
gradle runIde
```
Run with fresh sandbox
```shell script
gradle clean runIde
```

### Debugging
To debug the plugin execute Gradle run configuration with `runIde` task using IDE **Debug action**.

## Logging
Plugin can log additional diagnostic information to help with issues investigation. All categories can be used in any combination.

### Debug logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.gittoolbox
```

## Performance metrics
Metrics are exposed via JMX beans under `zielu.gittoolbox` domain.

## Icons attribution:

[Git Logo](https://git-scm.com/downloads/logos) by [Jason Long](https://twitter.com/jasonlong) is licensed under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/)

Some icons by [Yusuke Kamiyamane](http://p.yusukekamiyamane.com). Licensed under a [Creative Commons Attribution 3.0 License](http://creativecommons.org/licenses/by/3.0/)
