GitToolBox - plugin for Jetbrains IDEs
======================================
Plugin for family of Jetbrains IDEs that expands build-in `Git Integration`.

## Features
For list of features see [plugin.xml](./GitToolBox/src/main/resources/META-INF/plugin.xml)

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7499)

## Architecture decisions record
Decisions are stored [here](./GitToolBox/doc/arch).

## Building & running
Release build
```
gradle clean check jacocoTestReport buildPlugin
```
Verify build
```
gradle clean check jacocoTestReport
```
Run configuration - to debug from IDE execute configuration with debug action
```
gradle clean runIde
```

## Logging
Plugin can log additional diagnostic information to help with issues investigation.

### Debug logging
Set following in **Help > Debug Log Settings...**
```
#zielu.gittoolbox
```

### Performance logging
Set following in **Help > Debug Log Settings...**
```
#zielu.gittoolbox.perf:trace
```

## Icons attribution:

[Git Logo](https://git-scm.com/downloads/logos) by [Jason Long](https://twitter.com/jasonlong) is licensed under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/)

Some icons by [Yusuke Kamiyamane](http://p.yusukekamiyamane.com). Licensed under a [Creative Commons Attribution 3.0 License](http://creativecommons.org/licenses/by/3.0/)
