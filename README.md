GitToolBox - plugin for Jetbrains IDEs
======================================
[![Build Status: develop](https://travis-ci.org/zielu/GitToolBox.svg?branch=develop)](https://travis-ci.org/zielu/GitToolBox)

Plugin for family of Jetbrains IDEs that expands build-in `Git Integration`.

## Features
For list of features see [description](./GitToolBox/description.html)

## Change notes
For list of change notes see [changenotes](./GitToolBox/change-notes.html)

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7499-gittoolbox)

### EAP builds
Add https://plugins.jetbrains.com/plugins/eap/7499 in **Settings > Plugins > Browse repositories... > Manage 
repositories...** to receive early access builds.

## Architecture decisions record
Decisions are stored [here](./GitToolBox/doc/arch).

## Building & running

### Build parameters
```-Pfast=true``` speed-up is achieved by:
* skipping integration tests 
* skipping spotbugs

### Releases

#### Release current `-SNAPSHOT`
```
gradle clean check jacocoTestReport buildPlugin release
```
#### Upgrade to next minor version
```
gradle clean check jacocoTestReport buildPlugin releaseMinorVersion
```
#### Upgrade to next major version
```
gradle clean check jacocoTestReport buildPlugin releaseMajorVersion
```

#### Release and publish
Append ```-Ppublish=true``` and include ```publishPlugin``` task.
For example
```
gradle clean check jacocoTestReport buildPlugin release publishPlugin -Ppublish=true
```

### Useful build commands
Full verification of build
```
gradle clean check jacocoTestReport
```
Quick verification of build
```
gradle clean test jacocoTestReport -Pfast=true
```
Update gradlew version
```
gradle wrapper --gradle-version 5.6.2 --distribution-type ALL
```

### Useful run commands
Run with previous sandbox contents
```
gradle runIde
```
Run with fresh sandbox
```
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
