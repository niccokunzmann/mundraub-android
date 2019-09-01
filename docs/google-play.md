# Google Play

This app is uploaded to google play.
A separate APK file is created for this as Google Play drops
support for Android version 13 and lower.

## Setup

- [Initialize Fastlane](https://docs.fastlane.tools/getting-started/android/setup/)
  Make sure [not to have the `CI` or `TRAVIS` env variables set](https://github.com/fastlane/fastlane/issues/12011#issuecomment-378547581).
- [Getting the supply configuration](https://docs.fastlane.tools/actions/supply/)
  with your Google Play credentials.

## Deployment

Each tag is deployed.
If there is a changelog file for the version code,
then there will be a deployment to production,
otherwise the release is deployed to the beta branch.

