# Fork the Repo

If you want to fork the repository, you have two options to let [Travis-CI]
check your builds.

## Create Pull Request

If you create a pull request, your build will be checked.
This repository can then integrate the changes into the master branch.
From there, releases will be issued, see [deployment].

## Configure Travis for your Build

You can configure [Travis-CI] to run your own build.
Therefore, some changes need to be made to the `.travis.yml` file
which will make it incompatible with the one running on
`niccokunzmann/mundraub-android`.
To keep other commits easily integratable between you and other repositories,
I recommend commiting all changes to the `.travis.yml` in commits
which do not change other files.

These changes need to be made:

- In the `env` section, there are encrypted variables.
    ```yaml
    secure: "Na ... Hw=" # TRANSIFEX_PASSWORD
    ```
    You will need to change them to your secrets as they only work on
    `niccokunzmann/mundraub-android`.
    - `TRANSIFEX_PASSWORD` is the Transifex API Token which you can receive from
        https://www.transifex.com/user/settings/api/
    - `GITHUB_TOKEN` is the your access token from
        https://github.com/settings/tokens
        with write access to your repository to push tags for releases.
    - `keystore_password` and `keystore_alias_password` are
       the passwords you used when you created a signing key e.g.
       with Android Studio.
       `keystore_alias` is the name of the key.
       The file is located at `.travis/key.jks` or encrypted at
       `.travis/key.jks.enc`.
       https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore
    - `TRAVIS_FILE_KEY` and `TRAVIS_FILE_IV` are the two values used to
       encrypt the two files `.travis/key.jks` and `.travis/api.json`.
- In the `before_install` Section, the two files need to be encrypted using
    ```shell
    travis encrypt-file -p <FILE> --key="$TRAVIS_FILE_KEY" --iv="$TRAVIS_FILE_IV"`
    ```
    and copied into the `.travis` folder.
    - `api.json` is the file you retrieve for your service account as described
       here: https://docs.fastlane.tools/actions/supply/
    - `key.jks` is the file you create as a key store for app signing.
       https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore

[Travis-CI]: https://travis-ci.org/niccokunzmann/mundraub-android/
[deployment]: deployment.md#readme

