Mundraub-Android
================

[![Build Status](https://travis-ci.org/niccokunzmann/mundraub-android.svg?branch=master)][travis]

This is the Android app for [Mundraub.org] which allows you to store the found
plants on the phone and upload them when an Internet connection is available.

The API level is 9 so it runs on old mobile phones.

Download
--------

You can download the latest version from GitHub
- [Download latest release](https://niccokunzmann.github.io/download_latest/mundraub-release-unsigned.apk)
- [Download latest debug version](https://niccokunzmann.github.io/download_latest/mundraub-release-unsigned.apk)

Contribute
----------

Contributions are welcome.
We use Android Studio to develop the app.

Software Material List
----------------------

App components:

- [Mundraub][Mundraub.org] for publising the found plants
- [OpenLayers] for refining te position
- [openstreetmap] for getting the OSM layer tiles
- [staticmap] for fetching the map preview
- [okhttp] for making requests
- [commons-lang3] for useful functions everywhere

Services:

- [Travis][travis] to build the app and test the code
- [GitHub] for publising and contributing to the code

[Mundraub.org]: https://mundraub.org
[OpenLayers]: https://dev.openlayers.org
[staticmap]: http://staticmap.openstreetmap.de
[okhttp]: https://github.com/square/okhttp/
[commons-lang3]: https://commons.apache.org/proper/commons-lang/
[travis]: https://travis-ci.org/niccokunzmann/mundraub-android
[openstreetmap]: https://openstreetmap.org
[GitHub]: https://github.com/niccokunzmann/mundraub-android

