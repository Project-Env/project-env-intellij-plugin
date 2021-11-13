# Project-Env IntelliJ-Plugin

![Build](https://github.com/Project-Env/project-env-intellij-plugin/workflows/Build/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Project-Env_project-env-intellij-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=Project-Env_project-env-intellij-plugin)
[![Version](https://img.shields.io/jetbrains/plugin/v/15746-project-env.svg)](https://plugins.jetbrains.com/plugin/15746-project-env)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/15746-project-env.svg)](https://plugins.jetbrains.com/plugin/15746-project-env)

<!-- Plugin description -->
This plugin configures tools setup by the Project-Env CLI (see [Project-Env](https://project-env.github.io/) for more details), as far as possible, in the IntelliJ project settings.

Currently, the following project settings are configured automatically if the tool is declared:
* JDK: Adds the JDK to the JDK table and sets it as project JDK
* Maven: 
  * Sets the project specific path to Maven to the corresponding location.
  * Makes all environment variables of all declared tools available to Maven executions
  * Configures the user specific settings file - the global settings file is (if existing) linked into the Maven distribution.
* Gradle:
  * Sets the project specific path to Gradle to the corresponding location.
  * Makes all environment variables of all declared tools available to Gradle executions
* Node: Adds a new interpreter and set it as project default interpreter

Please note that changes in this file are only loaded once after opening the project. Afterwards, the Project-Env must be manually refreshed by using the action under <kbd>Tools</kbd> > <kbd>Project-Env</kbd> > <kbd>Refresh️</kbd>.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Project-Env"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/Project-Env/project-env-intellij-plugin/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
