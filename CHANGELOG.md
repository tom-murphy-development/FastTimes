# Changelog

## [Unreleased]
### Added
-
### Changed
-
### Fixed
-

## [1.0.4] - 2026-05-31
### Added
- Added monochrome themed icon support for Android13+.
- Introduced `datesCovered` logic to accurately track multi-day and ongoing fasts.
- Added "Current", "Last", and "Longest" streak labels in Statistics for better progress insights.

### Changed
- Refactored streak and activity logic to include intermediate days of multi-day fasts.
- Updated Dashboard and Statistics ViewModels to include ongoing fasts in metrics.
- Refactored all time displays to dynamically respect system 24-hour/12-hour settings.
- Optimized UI performance by leveraging `remember` blocks for date/time formatters.
- Improved `StreakCard` visibility logic to only show for active streaks of 2+ days.

### Fixed
- Fixed issue where multi-day fasts were not correctly counting toward daily progress (#15).
- Fixed streak calculation inaccuracies for ongoing fasting sessions (#16).
- Fixed time display formatting to adhere to user locale and device settings (#14).
- Added monochrome layer to adaptive icons (#11).

## [1.0.3] - 2026-05-07
### Added
- Implement fasting phases visualisation, tracking & education
### Changed
- Refined UI styling
### Fixed
- Resolved minor bug to improve duration parsing logic for custom fasts.

## [1.0.2] - 2026-04-30
### Added
- Added goal end time
### Changed
- Updated Dependencies
### Fixed
- Notification permission dialog to allow no notifications

## [1.0.1]
### Added
- Implemented responsive layout
### Changed
- Updated Dependencies
### Fixed
- Calendar Display issue in Edit dialog

## [1.0.0] - 2026-02-02
### Added
-
### Changed
-
### Fixed
-

## [1.0.0] - 2026-01-27
### Added
-
### Changed
-
### Fixed
-

## [1.0.0] - 2026-01-27
### Added
-
### Changed
-
### Fixed
-

## [1.0.0] - 2026-01-27
### Added
-
### Changed
-
### Fixed
-

## [1.0.0] - 2026-01-26
### Added
-
### Changed
-
### Fixed
-

## [1.0.0] - 2026-01-26
### Added
-
### Changed
-
### Fixed
-

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-01-26
### Added
- Initial Beta release of FastTimes.
- Support for multiple fasting profiles (12:12, 14:10, 16:8, 18:6, 20:4).
- Indefinite fasting timer.
- Detailed history
- Performance metrics
- Streak tracking and weekly progress visualization.
- Dark mode & custom theming support.

### Changed
-
### Fixed
-
