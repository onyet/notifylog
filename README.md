# NotifyLog 📲

NotifyLog is an Android app that records and stores notification history locally on the device (offline-first).

---

## 🔧 Key Features
- Capture incoming notifications via a NotificationListenerService
- Persist logs in SQLite using Room
- History list with search and filters (by app and date range)
- Detailed view for each notification (title, content, posted/received timestamps, package, channel)
- Permission onboarding to request Notification access
- Settings: enable/disable logging, ignore system apps, auto-delete retention

## ⚙️ App Information
- Application ID / package: `id.onyet.app.notifylog`
- Minimum SDK: 26
- Target SDK: 34
- Data architecture: Room (Entity, DAO, Repository)
- UI: Jetpack Compose + Material3

## 🧭 Project Structure (high level)

- `app/src/main/java/id/onyet/app/notifylog/` - main application code
  - `data/` - Room database, repository, preferences
  - `service/` - `NotificationLogService.kt` (NotificationListenerService)
  - `ui/` - screens, navigation, theme
  - `util/` - helper utilities (e.g., permission helpers)
- `blueprint/` - design assets and UI references (splash, onboarding, history, detail, filter, settings)

## 🚀 Build & Run
1. Open the project in Android Studio and sync Gradle (recommended).
2. Or use the command line:
   - Build debug APK: `./gradlew assembleDebug`
   - Install debug APK on a connected device: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Ensure your Android SDK path is configured in `local.properties` (see `sdk.dir`).

## 🔐 Permissions & Testing
- Users must enable Notification Access manually: `Settings → Notification access → NotifyLog`.
- The onboarding screen includes a button to open the system setting for convenience.
- To test: enable notification access and send notifications from another app; logs will appear in NotifyLog.

## 🎨 Assets & Design
- App icon and drawable assets are copied from `blueprint/icons/android/res` to `app/src/main/res/`.
- Design references and screens live under `blueprint/`.

## ⚠️ Play Store & Privacy Notes
- All notification data is stored locally and not sent to any external servers.
- Add a Privacy Policy page before publishing to comply with Play Store requirements.

## 🧪 Development Tips
- Use the repository and DAO in `NotifyLogApp` for manual inserts during testing.
- Consider adding unit and instrumentation tests to validate database and UI flows.

## 👤 Author
- **Dian Mukti Wibowo**
- Email: <onyetcorp@gmail.com>

## 📄 License
This project is licensed under the MIT License - see the `LICENSE` file for details.
