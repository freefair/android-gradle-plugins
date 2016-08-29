# gradle-android-plugins
Gradle Plugins for Android builds

## How to include: [![](https://jitpack.io/v/io.freefair/android-gradle-plugins.svg)](https://jitpack.io/#io.freefair/android-gradle-plugins)

```gradle
buildscript {
    repositories {
        // jcenter() etc.
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        // classpath 'com.android.tools.build:gradle:<version>' etc.
        classpath 'io.freefair:android-gradle-plugins:<version>'
    }
}
```
```gradle
apply plugin: 'io.freefair.<plugin-name>'
```
