# Event Planner - Android App

A native Android application for event planning with Material Design 3. Plan events, manage participants, track vendors, book venues, and check weather forecasts—all in one place.

## Features

- **Event Management** - Create, edit, and delete events with full details
- **Participant Tracking** - Manage event participants and send invitations
- **Vendor Management** - Track vendors and their services for your events
- **Venue Booking** - Search and book venues with filtering options
- **Weather Integration** - Real-time weather forecasts for event dates
- **Guest Mode** - Join events as a guest without creating an account (30-day expiration)
- **Dark Mode Support** - Full dark theme with proper contrast ratios
- **Offline Support** - Event caching for offline access
- **Invite Links** - Share event invitations via deep links and social media

## Tech Stack

- **Language:** Kotlin/Java
- **UI Framework:** Material Design 3 (AndroidX)
- **Architecture:** MVVM with Repository pattern
- **HTTP Client:** Retrofit + OkHttp
- **Local Storage:** Room Database
- **Authentication:** JWT Token-based
- **Build System:** Gradle

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio 2024.1+
- JDK 21 LTS or higher
- Backend API running (see Backend Connection)

## Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Olly1206/event-planer-android.git
   cd event-planer-android
   ```

2. **Open in Android Studio:**
   - File → Open → Select this project
   - Wait for Gradle sync to complete

3. **Configure Backend Connection:**
   - Edit `src/main/res/values/strings.xml`
   - Update `base_url` to your backend API endpoint:
     ```xml
     <string name="base_url">http://your-backend-url:8080</string>
     ```

4. **Build the project:**
   ```bash
   ./gradlew build
   ```

5. **Run on device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

## Running the App

**On Physical Device:**
- Enable Developer Mode (tap Build Number 7 times in Settings → About Phone)
- Enable USB Debugging
- Connect via USB cable
- Run from Android Studio or: `./gradlew installDebug`

**On Emulator:**
- Create or launch an emulator from Android Studio
- Run project from IDE or: `./gradlew installDebug`

**Configuration:**
- Set backend URL in `strings.xml` before building
- For local network: `http://192.168.1.X:8080`
- For remote hosting: `https://your-domain.com`

## Project Structure

```
src/main/
├── java/com/example/eventplanner/
│   ├── ui/
│   │   ├── screens/          # Composable screens
│   │   ├── components/       # Reusable UI components
│   │   └── theme/            # Material Design 3 theme
│   ├── viewmodel/            # MVVM ViewModels
│   ├── repository/           # Data layer
│   ├── network/              # API clients
│   ├── database/             # Room entities & DAOs
│   └── util/                 # Utilities & helpers
└── res/
    ├── values/               # Colors, strings, dimensions
    ├── values-night/         # Dark theme colors
    └── layout/               # XML layouts (if any)
```

## Building & Testing

**Run all tests:**
```bash
./gradlew test
```

**Build release APK:**
```bash
./gradlew assembleRelease
```

**Build app bundle (for Google Play):**
```bash
./gradlew bundleRelease
```

## Backend Configuration

The app communicates with a Spring Boot backend API. Ensure the backend is running and accessible at the configured URL.

**Local Network:**
- Backend runs on: `http://192.168.1.X:8080`
- App connects via: Update `strings.xml`

**Remote Hosting:**
- Backend deployed to cloud service (AWS, Heroku, etc.)
- Update app to use HTTPS endpoint
- Configure CORS if needed on backend

## API Endpoints Expected

The app expects the following backend endpoints:

- `POST /api/auth/login` - User authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/guest` - Guest mode
- `GET /api/events` - Fetch events
- `POST /api/events` - Create event
- `GET /api/events/{id}` - Get event details
- `GET /api/events/{id}/participants` - Get participants
- `POST /api/events/{id}/join` - Join event
- `GET /api/venues` - Search venues
- `GET /api/weather` - Get weather forecast
- `GET /api/vendors` - Fetch vendors

## Dark Mode

The app includes full dark mode support with Material Design 3 colors:
- Automatic switching based on system settings
- Manual toggle in settings (if implemented)
- Proper contrast ratios for accessibility
- Defined in `values-night/colors.xml` and `values-night/themes.xml`

## Authentication

The app uses JWT-based authentication:
- Tokens stored securely in shared preferences
- Auto-refresh on token expiration
- Guest mode available for limited access
- Guest accounts expire after 30 days

## Troubleshooting

**"Cannot connect to server"**
- Verify backend is running
- Check `base_url` in `strings.xml`
- Ensure device/emulator can reach the network
- For emulator: use `10.0.2.2:8080` for local backend

**"Authentication failed"**
- Clear app data: Settings → Apps → Event Planner → Storage → Clear Data
- Re-login with valid credentials
- Check backend JWT configuration

**Dark mode colors incorrect**
- Check if system dark mode is enabled
- Clear app cache and restart
- Verify `values-night/colors.xml` is properly configured

## Building for Google Play

1. **Generate signing key:**
   ```bash
   keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias event-planner
   ```

2. **Configure signing in `build.gradle`:**
   ```gradle
   signingConfigs {
       release {
           storeFile file("release.jks")
           storePassword "your-password"
           keyAlias "event-planner"
           keyPassword "your-password"
       }
   }
   ```

3. **Build signed bundle:**
   ```bash
   ./gradlew bundleRelease
   ```

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m "Add feature"`
3. Push: `git push origin feature/your-feature`
4. Open a Pull Request

## License

This project is private. All rights reserved.

## Support

For issues or questions, refer to the backend repository or contact the development team.

---

**Backend Repository:** [event-planer-backend](https://github.com/Olly1206/event-planer-backend)  
**Platform:** Android 8.0+  
**Last Updated:** April 2026
