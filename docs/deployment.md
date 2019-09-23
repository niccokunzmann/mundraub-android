# Deployment

The app is deployed from the main repository.
The process runs as follows:

1. A commit is made to the master branch.
2. The tests run on [Travis].
    - this includes a test which merges the [`google-play` branch][google-play-branch].
3. If the tests run, a tag is created and pushed to the repository. It includes
    - updated translations from [Transifex][translations].
    - a changelog generated from the [scripts]
4. The tag is tested on [Travis].
5. The APKs are built
    - APKs for Android 2.3 and higher are deployed to
        - [F-Droid] - F-Droid will pull the tags from time to time and create a
           build on their side and serve the own APK file.
        - Github [Releases]
    - APKs for Android 4 and higher are deployed to
        - [Google Play]
            - If there is a new change in this tag which has not been there before,
                then the change is deployed to the production branch of [Google Play].
            - Else (if there is no change in the changelog which this tag owns), 
                the file is pushed to the [Beta branch of Google Play][Google-Play-Beta].

## Changelog

The changelog file is generated from the commits.
This allows us to track issues linked in the commits.
The [changelog] is automatically generated.

## Google Play

This app is uploaded to [Google Play].
A separate APK file is created for this as Google Play drops
support for Android version 13 and lower.

### Setup

- [Initialize Fastlane](https://docs.fastlane.tools/getting-started/android/setup/)
  Make sure [not to have the `CI` or `TRAVIS` env variables set](https://github.com/fastlane/fastlane/issues/12011#issuecomment-378547581).
- [Getting the supply configuration](https://docs.fastlane.tools/actions/supply/)
  with your Google Play credentials.

[Travis]: https://travis-ci.org/niccokunzmann/mundraub-android
[translations]: translations.md#readme
[scripts]: ../scripts
[google-play-branch]: https://github.com/niccokunzmann/mundraub-android/tree/google-play
[F-Droid]: https://f-droid.org/en/packages/eu.quelltext.mundraub/
[Releases]: https://github.com/niccokunzmann/mundraub-android/releases
[Google Play]: https://play.google.com/store/apps/details?id=eu.quelltext.mundraub
[Google-Play-Beta]: https://play.google.com/apps/testing/eu.quelltext.mundraub
[changelog]: https://niccokunzmann.github.io/mundraub-android/docs/changelog/

