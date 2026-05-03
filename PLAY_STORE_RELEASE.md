# EvenGo Play Store Release Notes

This file tracks the local steps needed to produce a Google Play uploadable Android App Bundle.

## Current Release Configuration

- Play package ID: `com.oliverloeckler.evengo`
- App label: `EvenGo`
- Version code: `1`
- Version name: `1.0`
- Target SDK: `35`
- Release backend URL: `https://event-planer-backend.onrender.com/`
- Release HTTP body logging: disabled
- Release cleartext traffic: disabled
- Privacy policy URL after backend deploy: `https://event-planer-backend.onrender.com/privacy`
- Account deletion URL after backend deploy: `https://event-planer-backend.onrender.com/account-deletion`

## Build Verification

Debug/test build:

```bash
./gradlew test
```

Unsigned release bundle smoke check:

```bash
./gradlew bundleRelease
```

Output:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Create Upload Key

Run this outside Git-controlled folders, for example in `~/keys`:

```bash
mkdir -p ~/keys
keytool -genkeypair \
  -v \
  -keystore ~/keys/evengo-upload-key.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -alias evengo-upload
```

Keep this file and its passwords safe. Do not commit it.

## Enable Release Signing

Copy the template:

```bash
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties`:

```properties
storeFile=/home/olly/keys/evengo-upload-key.jks
storePassword=your-keystore-password
keyAlias=evengo-upload
keyPassword=your-key-password
```

`keystore.properties` and keystore files are ignored by Git.

Then build the signed bundle:

```bash
./gradlew bundleRelease
```

Verify signing:

```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

The output should not say `jar is unsigned`.

## Upload To Play Console

Use:

```text
app/build/outputs/bundle/release/app-release.aab
```

Start with Internal testing before closed/open testing.

## Play Console Content Draft

Suggested app name:

```text
EvenGo
```

Suggested short description:

```text
Plan events, invite guests, and find venues, vendors, and weather in one flow.
```

Suggested first release notes:

```text
Early access prototype for creating events, inviting participants, and discovering venues, vendors, and weather suggestions.
```

Suggested category:

```text
Productivity or Events, depending on the categories available in Play Console.
```

## Data Safety Draft

The Play Console Data safety form should match the app and backend behavior exactly.
For the current prototype, expect to disclose:

- Account data: username, email address, hashed password on the backend.
- App activity / user-generated content: event titles, descriptions, dates, city names, venues, selected options, vendors, invite links, and participant names.
- App-generated identifiers: JWT/session data and guest-device UUIDs.
- No device location permission: users type a city manually; the app only has Internet permission.
- External processing: city/search data is sent through the backend to OpenStreetMap/Nominatim/Overpass and Open-Meteo for suggestions.
- Data is encrypted in transit for release builds through HTTPS.
- Users can delete their account in the app and through the account deletion web instructions.

Before submitting for review, confirm the developer support email in the Google Play listing is real because the public deletion/privacy pages currently refer users to that contact address.

## Official Google References

- Play App Signing: https://support.google.com/googleplay/android-developer/answer/9842756
- Internal/closed/open testing tracks: https://support.google.com/googleplay/android-developer/answer/9845334
- Release rollout: https://support.google.com/googleplay/android-developer/answer/9859348
- Data safety form: https://support.google.com/googleplay/android-developer/answer/10787469
- Account deletion requirements: https://support.google.com/googleplay/android-developer/answer/13327111
