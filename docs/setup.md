# Setup

This should prepare you for developing the app.

## Setup App Development

1. **Install Android Studio**  
   In order to develop the app, I use [Android Studio].
   You can download and install it.
2. **Install Git**  
   While Android Studio installs, you can get the source code.
   It is on [GitHub]. We use [Git] to develop the app in a distributed way.
   Please install [Git] or the [GitHub Desktop].
3. **[Fork]** the repository to your GitHub Account.
4. **Get the source code**  
   It should appear automatically in GitHub Desktop.
   If you use the command line client, you can clone the app like this:
   ```
   git clone https://github.com/YOUR_USER_NAME/mundraub-android.git
   ```
   Please replace `YOUR_USER_NAME` with your GitHub username.
5. **Open the source code**  
   Once Android Studio is installed, you can click on File → Open and navigate to the project.
6. Now you are **ready to go**. You can change the source code and build the app!
   Please view the [Contribution Guidelines] if you like to share your improvements or just
   submit them and we will talk.

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

## Map

The [map] is an HTML/CSS/JavaScript website which is displayed 
in the app as an [Android WebView].
This map can be worked on locally and in the app.
To display the map, you need a web browser like Firefox.

### Proxy

Mundraub.org does not allow JavaScript access to the map
directly because it does not set the `Access-Control-Allow-Origin`
header.
Thus, we need a proxy for this map.
The proxy can be run with the [command `scripts/runHTTPProxy.sh`][script-proxy]
You need `java` and `javac` for this.

Please the the [API] to know what the proxy provides.

[tx-client]: https://docs.transifex.com/client/installing-the-client
[Transifex]: https://www.transifex.com/mundraub-android/mundraub-android-app
[Android Studio]: https://developer.android.com/studio/index.html
[GitHub]: https://github.com/niccokunzmann/mundraub-android/
[Git]: https://git-scm.com/
[GitHub Desktop]: https://desktop.github.com/
[Fork]: https://github.com/niccokunzmann/mundraub-android/fork
[Contribution Guidelines]: ../Contributing.md
[script-proxy]: ../scripts
[map]: ../app/src/main/assets/map/
[Android WebView]: https://developer.android.com/reference/android/webkit/WebView
[API]: api.md

