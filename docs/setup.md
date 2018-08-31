# Setup

This should prepare you for developing the app.

## Translations

You can always translate the app from English to your preferred language online on [Transifex].
If you like to build the app with translations included, you can use the Transifex client to
download all of them at once.

Before you build the app, you can download the current translations to include them in the build and test
different layouts e.g. in German which uses the most space.
To download the translations, you can follow these steps:

1. Install [Transifex client][tx-client].
2. Go to the repository root
   ```
   cd mundraub-android
   ```
3. Pull the latest translations. You may need to log in or get an API key.
   ```
   tx pull --all
   ```

[tx-client]: https://docs.transifex.com/client/installing-the-client
[Transifex]: https://www.transifex.com/mundraub-android/mundraub-android-app
