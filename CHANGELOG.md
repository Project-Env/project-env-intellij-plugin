<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# project-env-intellij-plugin Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

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