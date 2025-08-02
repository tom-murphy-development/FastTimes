# FastTimes

A simple Open Source Android fasting timer and tracking app.

## Features
- Fasting Timer with start, pause, and stop
- Fast Length/Profiles (e.g., 16:8, 20:4, custom)
- Fast History tracking
- Stats section to summarize fasting history
- Responsive UI for phones and tablets
- Light/Dark theme with dynamic color (Material 3)

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (Jetpack)
- **Dependency Injection:** Hilt
- **Async:** Kotlin Coroutines & Flows
- **Navigation:** Jetpack Navigation Compose

## Project Structure
```
app/
  src/main/java/com/fasttimes/
    data/        # Room database, DAOs, entities
    di/          # Hilt modules
    ui/          # Compose screens, navigation, theme
    util/        # Utility classes
```

## Build Instructions
1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/fasttimes.git
   ```
2. Open in Android Studio (latest stable).
3. Build & run on an emulator or device (minSdk 24+).

## Contribution Guidelines
- Fork the repo and create a feature branch.
- Follow Kotlin and Jetpack Compose best practices.
- Use MVVM and Hilt for architecture and DI.
- Submit a pull request with a clear description.

## License
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
