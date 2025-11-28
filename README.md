# FastTimes

A simple Open Source Android fasting timer and tracking app.

## Features
- **Fasting Timer**: Start and stop fasts with persistent notification support.
- **Customizable Goals**: Set your fasting duration (e.g., 16:8, 20:4, custom).
- **History Tracking**: View past fasts and track your progress.
- **Statistics**: Visualize your fasting habits.
- **Dynamic Theming**: Light/Dark theme with Material 3 Dynamic Colors.
- **Custom Theming**: Customize accent colors, wavy indicators, and more.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Database:** Room (Jetpack)
- **Local Storage:** DataStore (Preferences)
- **Dependency Injection:** Hilt
- **Async:** Kotlin Coroutines & Flows
- **Navigation:** Jetpack Navigation Compose
- **Serialization:** Kotlin Serialization
- **Services:** Foreground Service for timer persistence

## Project Structure
```
app/src/main/java/com/fasttimes/
  ├── alarms/      # Alarm scheduling logic
  ├── data/        # Room database, DataStore, Repositories
  ├── di/          # Hilt Dependency Injection modules
  ├── receiver/    # BroadcastReceivers (e.g., BootCompleted)
  ├── service/     # Foreground services
  ├── ui/          # Jetpack Compose screens and components
  └── FastTimesApp.kt
```

## Build Instructions
1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/fasttimes.git
   ```
2. Open in Android Studio (Ladybug or newer recommended).
3. Ensure JDK 17 is selected as the Gradle JVM.
4. Build & run on an emulator or device (**minSdk 30+**).

## Contribution Guidelines
- Fork the repo and create a feature branch.
- Follow Kotlin and Jetpack Compose best practices.
- This project uses **Spotless** for code formatting. Run `./gradlew spotlessApply` before committing.
- Use MVVM and Hilt for architecture and DI.
- Submit a pull request with a clear description.

## License
This project is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for details.
