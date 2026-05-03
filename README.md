# Event Planner Android Prototype

Native Android client for the Event Planner prototype. The app lets users register or continue as a guest, create and edit events, browse created/joined events, share invite links, and request venue, vendor, and weather suggestions from the Spring Boot backend.

## Prototype Scope

- Dashboard for current events
- Private/company event flows
- Event creation, editing, deletion, joining, and invite-code sharing
- Login, registration, and guest mode
- Venue suggestions by city, radius, location type, and event type
- Vendor suggestions by city, radius, and selected event options
- Weather forecast lookup through the backend
- XML layouts with Java activities and Retrofit networking

## Tech Stack

- Java 17 source compatibility
- Android Gradle Plugin with Gradle wrapper
- AndroidX AppCompat, Activity, ConstraintLayout
- Material Components
- Retrofit, Gson converter, OkHttp logging interceptor

## Project Structure

```text
app/src/main/
|-- AndroidManifest.xml
|-- java/com/example/myapplication/
|   |-- auth/          Login, registration, and token/session handling
|   |-- adapter/       RecyclerView adapters
|   |-- model/         App-side models
|   |-- network/       Retrofit client, API service, and DTOs
|   `-- *.java         Activity screens for the prototype flow
`-- res/
    |-- drawable/      UI backgrounds and launcher assets
    |-- layout/        XML screens and list rows
    |-- values/        App strings, colors, and themes
    `-- values-night/  Dark theme resources
```

## Requirements

- Android Studio 2024.1 or newer
- JDK 17 or newer
- Android SDK with compile SDK 35
- A device/emulator running Android 11 or newer
- Backend API reachable from the device/emulator

## Backend Connection

The current prototype points at the deployed backend in:

```text
app/src/main/java/com/example/myapplication/network/RetrofitClient.java
```

Current base URL:

```text
https://event-planer-backend.onrender.com/
```

For a local backend, update `BASE_URL` before building:

```java
// Android emulator talking to backend on this computer
private static final String BASE_URL = "http://10.0.2.2:8080/";

// Physical phone on same Wi-Fi as this computer
private static final String BASE_URL = "http://<computer-lan-ip>:8080/";
```

## Build And Run

```bash
./gradlew test
./gradlew assembleDebug
./gradlew installDebug
```

You can also open the folder in Android Studio, wait for Gradle sync, select a device, and press Run.

## Useful Demo Flow

1. Start the backend or confirm the deployed backend is awake.
2. Open the app and register, log in, or continue as guest.
3. Create a new private or company event.
4. Choose event type, options, timeframe, and location details.
5. Open the event from the dashboard.
6. Show invite code/link, venue suggestions, vendor suggestions, and weather forecast.

## Packaging Notes

Generated folders such as `.gradle/`, `build/`, `.idea/`, and `local.properties` should stay out of stakeholder zip files. The handoff package created for the prototype includes only source, Gradle wrapper files, resources, and documentation.
