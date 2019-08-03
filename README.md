Mundraub-Android
================

[![Build Status](https://travis-ci.org/niccokunzmann/mundraub-android.svg?branch=master)][travis]
[![Percentage of issues still open](http://isitmaintained.com/badge/open/niccokunzmann/mundraub-android.svg)](http://isitmaintained.com/project/niccokunzmann/mundraub-android "Percentage of issues still open")
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/niccokunzmann/mundraub-android.svg)](http://isitmaintained.com/project/niccokunzmann/mundraub-android "Average time to resolve an issue")
[![][first-issues-image]][first-issues]
[![Latest Release](https://img.shields.io/github/release/niccokunzmann/mundraub-android.svg?logo=github)][releases]
[![F-Droid](https://img.shields.io/f-droid/v/eu.quelltext.mundraub.svg)][fdroid]

This is the Android app for [Mundraub.org] which allows you to store the found
plants on the phone and upload them when an Internet connection is available.

The API level is 9 so the application can run on old mobile phones.

[<img src="docs/images/link-to-screenshots.png" height="120"/>][screenshots]
[View the screenshots][screenshots]

Download
--------

[<img src="https://f-droid.org/badge/get-it-on.png" height="75">][fdroid]

The latest version can be downloaded from GitHub
- [Download latest release](https://niccokunzmann.github.io/download_latest/mundraub-release-unsigned.apk)
- [Download latest debug version](https://niccokunzmann.github.io/download_latest/mundraub-release-unsigned.apk)

Contribute
----------

Contributions are welcome.
 
- To add another supported language, translate the app on [Transifex][tx].
  Suggestions on new languanges to support can be submitted through an [issue][issues].
- The app is developed on [Android Studio] . We suggest starting development with [small issues. ![][first-issues-image]][first-issues]  
    Permission is not necessary to begin working on the app -
    The app is free software so anyone is able to modify it. 
    Helpful tips for the developing process:
    - leaving a comment allows others to support you and ensures no duplicate work is done.
    - Start a pull request as soon as possible so the community can support you and Travis can check your code.
        Earlier feedback means faster development.
- Feedback is welcome, please open an [issue][issues].
  - Crash: 
    - Report a debug log file if you gave EXTERNAL_STORAGE permissions or use logcat to retrieve the log.
      If you gave permissions, you should be able to find it under `eu.quelltext.mundraub.log.txt`
      or `eu.quelltext.mundraub.error.txt`.
      The app (if starting) should point you to the file.
    - Use [MatLog Libre] to retrieve the app log before and after a crash.
    

Pease read the following pages: [Setup Guide][setup], [Contribution Guidelines] and the [documentation].

Software Material List
----------------------

App components:

- For publishing the found plants and markers on the map:
    - [Mundraub.org] (German)
    - [Na-Ovoce.cz] (Czech)
    - [FruitMap.org] (Slovakian, English and Czech) (download only)
    - www.quelltext.eu in case the insecure connections for old Androids are enabled.
- [OpenLayers] for refining te position
- [openstreetmap] for rendering OpenStreetMap
- [World Imagery] for rendering a satellite map
- [staticmap] for fetching the map preview
- [okhttp] for making requests
- [commons-lang3] for useful functions everywhere
- [nanohttpd] for providing an API to the map

Services:

- [Travis][travis] to build the app and test the code
- [GitHub] for publising and contributing to the code
- [Transifex][tx] for managing translations
    - [Microsoft Translate] with a free account for suggesting translations. 
- [F-Droid][fdroid] for publishing the app
- OpenStreetMap's [Nominatim] to search for places on the map

Languages:

- Java for the app, Android 2.3 [GINGERBREAD]
- HTML/CSS/JavaScript for the [map]

[Mundraub.org]: https://mundraub.org
[OpenLayers]: https://dev.openlayers.org
[staticmap]: http://staticmap.openstreetmap.de
[okhttp]: https://github.com/square/okhttp/
[commons-lang3]: https://commons.apache.org/proper/commons-lang/
[travis]: https://travis-ci.org/niccokunzmann/mundraub-android
[openstreetmap]: https://openstreetmap.org
[GitHub]: https://github.com/niccokunzmann/mundraub-android
[World Imagery]: https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/
[GINGERBREAD]: https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels
[map]: https://niccokunzmann.github.io/mundraub-android/app/src/main/assets/map/examples/fullScreen.html
[issues]: https://github.com/niccokunzmann/mundraub-android/issues
[tx]: https://www.transifex.com/mundraub-android/mundraub-android-app/
[setup]: docs/setup.md
[screenshots]: docs/usage.md#readme
[fdroid]: https://f-droid.org/en/packages/eu.quelltext.mundraub/
[Android Studio]: https://developer.android.com/studio/
[releases]: https://github.com/niccokunzmann/mundraub-android/releases
[Contribution Guidelines]: CONTRIBUTING.md
[nanohttpd]: http://nanohttpd.org/
[first-issues]: https://github.com/niccokunzmann/mundraub-android/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22
[first-issues-image]: https://img.shields.io/github/issues/niccokunzmann/mundraub-android/good%20first%20issue.svg?label=good%20first%20issues
[Microsoft Translate]: https://portal.azure.com/#@niccokunzmannlive.onmicrosoft.com/resource/subscriptions/7ad79494-1d19-43e4-8f0c-59d1a34c5711/resourcegroups/Mundraub-Android-Resource-Group/providers/Microsoft.CognitiveServices/accounts/Mundraub-Android-Translations/quickstart
[Na-Ovoce.cz]: https://na-ovoce.cz/
[FruitMap.org]: https://www.fruitmap.org/
[MatLog Libre]: https://f-droid.org/en/packages/com.pluscubed.matloglibre/
[documentation]: docs#readme
[Nominatim]: https://nominatim.openstreetmap.org/
