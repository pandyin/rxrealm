DOWNLOAD
========

project build.gradle:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

module build.gradle:
```
dependencies {
  compile 'com.github.pandyin:rxrealm:1.1.2'
}
```
