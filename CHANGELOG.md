<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# project-env-intellij-plugin Changelog

## [Unreleased]
### Added
- Set Gradle JDK to project JDK if it was installed through Project-Env.

### Changed
- Update Gradle project settings even if the project was added before running the plugin.

## [3.0.3]
### Fixed
- Fixed resolving Project-Env CLI on Windows.

## [3.0.2]
### Added
- Try to resolve Project-Env CLI from directory `/usr/local/bin` if not resolvable through `PATH`.

## [3.0.1]
### Removed
- Removed support for IntelliJ versions < 2020.3.X.

## [3.0.0]
### Changed
- Refactored the plugin, so it uses the installed Project-Env Core CLI to install and manage the required tools. 

## [2.2.0]
### Changed
- Updated Project-Env core library to version `2.1.0` which introduces the support for Git hooks installation.

## [2.1.0]
### Changed
- Improved plugin icon by transforming text into paths.
- Broaden plugin support to 2020.1.

### Fixed
- Do not reload Project-Env configuration during clean-up.
- Clear environment variables and path elements before registering the new ones.
- Fixed memory leak of Gradle configurer.

## [2.0.0]
### Added
- Action in `Tools` menu to refresh Project-Env.
- Action in `Tools` menu to clean previous installed and now unused Project-Env tools.

### Changed
- Updated Project-Env core library to version `2.0.0`.

## [1.1.1]
### Changed
- Updated Project-Env core library to version `1.1.2`.

## [1.1.0]
### Added
- Added simple progress bar to notify the user about the running Project-Env process.

### Changed
- Updated Project-Env core library to version `1.1.0`.
- Gradle project's are updated now after they were loaded too.
- Improved plugin icon.

## [1.0.1]
### Fixed
- Fixed the namespace of the `toolConfigurer` plugin extension point which is used to handle tool specific configuration tasks.

## [1.0.0]
### Changed
- Changed plugin namespace from `ch.repolevedavaj.projectenv` to `io.projectenv`
- Updated Project-Env core library to version `1.0.0` (does not really change anything - just changed the package name to `io.projectenv`)

## [0.0.1]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Initial implementation of the setup and configuration of tools, declared in the `project-env.yml` 