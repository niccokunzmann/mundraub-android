# Transifex Configuration

This is the configuration for the Transifex translation platform.

- [Transifex Client](https://docs.transifex.com/client/introduction)
- [configure new translations](https://docs.transifex.com/client/config)
    Example:
    ```
    tx config mapping -r mundraub-android-app.fdroid-short-description-txt \
                      --source-lang en-US \
                      --type TXT \
                      --source-file fastlane/metadata/android/en-US/short_description.txt \
                      --expression 'fastlane/metadata/android/<lang>/short_description.txt' \
                      --execute \
                      --minimum-perc=100
    ```
    - `--type` see [formats](http://docs.transifex.com/formats/)
    And push it:
    ```
    tx push --source
    ```
- [Transifex Github Integration](https://docs.transifex.com/integrations/github/)

