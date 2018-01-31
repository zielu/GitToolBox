GitToolBox - plugin for Jetbrains IDEs
======================================
Plugin for family of Jetbrains IDEs that expands build-in `Git Integration`.

## Features
For list of features see [plugin.xml](./GitToolBox/src/main/resources/META-INF/plugin.xml)

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7499-gittoolbox)

### EAP builds
Add https://plugins.jetbrains.com/plugins/eap/7499 in **Settings > Plugins > Browse repositories... > Manage 
repositories...** to receive early access builds.

## Architecture decisions record
Decisions are stored [here](./GitToolBox/doc/arch).

## Building & running

## Development builds
If version set in [gradle.properties](./GitToolBox/gradle.properties) ends with `-dev` then build datetime in UTC timezone will be appended.
For example dev build on `2018-01-13 13:06:12 CET` will produce version `173.1.2-dev.20180113.120612.adddfbcd10`

### Useful build commands
Release build
```
gradle clean check jacocoTestReport buildPlugin
```
Full verification of build
```
gradle clean check jacocoTestReport
```
Quick verification of build
```
gradle clean test jacocoTestReport
```
Update gradlew version
```
gradle wrapper --gradle-version 4.5 --distribution-type ALL
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

### Plugin debugging
To debug the plugin execute Gradle run configuration with `runIde` task using IDE **Debug action**.

## Logging
Plugin can log additional diagnostic information to help with issues investigation. All categories can be used in any combination.

### Debug logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.gittoolbox
```

### Performance logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.gittoolbox.perf:trace
```

## Icons attribution:

[Git Logo](https://git-scm.com/downloads/logos) by [Jason Long](https://twitter.com/jasonlong) is licensed under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/)

Some icons by [Yusuke Kamiyamane](http://p.yusukekamiyamane.com). Licensed under a [Creative Commons Attribution 3.0 License](http://creativecommons.org/licenses/by/3.0/)
