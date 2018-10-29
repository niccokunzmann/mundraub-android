Translations
============

We manage translations through [Transifex].
if works like this (roughly, please correct it when you do it)

1. You log in to [Transifex].
2. You choose the language you want to translate.
3. After you translated, it will be automatically included in new versions.
    If you cannot wait for it to be included, please open an issue.

Requesting Languages and Sharing Translations
---------------------------------------------

If you request a language e.g. German, you can request German `de`, German
for Germany `de-DE` and, German auf Austria `de-AU`.

Please translate the common part (`de`, `en`, `fr`, ...)
before the contry specific language (`de-DE`, `en-US`, `fr-FR`, ...).
As an example, if you translate to German `de`,
you automatically share the translations with
German for Germany `de-DE` and German for Austria `de-AU`.

Priority List
-------------

Here is a priority list for translations:

- descriptions for F-Droid
- plant names and menu items and titles of activities
- descriptions and text
- dialogs
- error messages
- privacy policy

You can choose to translate the title to your language.

Transifex Configuration
-----------------------

The configuration for the Transifex translation platform is in the [.tx] folder.

- [Transifex Client](https://docs.transifex.com/client/introduction)
- [configure new translations](https://docs.transifex.com/client/config)
    Example:
    ```
    tx config mapping -r mundraub-android-app.fdroid-short-description-txt \
                      --source-lang en-US \
                      --type TXT \
                      --source-file metadata/en/short_description.txt \
                      --expression 'metadata/<lang>/short_description.txt' \
                      --execute \
                      --minimum-perc=100
    ```
    - `--type` see [formats](http://docs.transifex.com/formats/)
    And push it:
    ```
    tx push --source
    ```
- [Transifex Github Integration](https://docs.transifex.com/integrations/github/)

Resetting Changes by tx --pull
------------------------------

If you pulled translations and you would like to revert back to what was before that
on git, make sure you did not edit something yourself. Then, execute

    .tx/reset-changes

Which either checks out files or removes them.

[Transifex]: https://www.transifex.com/mundraub-android/mundraub-android-app/
[.tx]: ../.tx
