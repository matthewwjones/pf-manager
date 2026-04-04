# CLAUDE.md

Global coding standards are in `~/.claude/CLAUDE.md`. This file covers only pf-manager specifics.

## Project Overview

**pf-manager** is a Java 21 portfolio manager application. Group: `com.mattjoneslondon`. Build system: Gradle 9. Test framework: JUnit 5 (Jupiter).

## Commands

```bash
# Build
./gradlew build          # Compile, test, and package
./gradlew clean build    # Clean then build

# Test
./gradlew test                              # Run all tests
./gradlew test --tests "ClassName"          # Run a single test class
./gradlew test --tests "ClassName.method"   # Run a single test method
./gradlew test --info                       # Run tests with verbose output

# Compile only
./gradlew compileJava
```

## Architecture

Source layout follows standard Maven/Gradle conventions:

- `src/main/java/` — application source (package root: `com.mattjoneslondon`)
- `src/main/resources/` — runtime resources and configuration
- `src/test/java/` — JUnit 5 tests mirroring the main package structure
- `src/test/resources/` — test fixtures and configuration

## Conventions

- **Dependency versions** must always be managed in `gradle/libs.versions.toml`. Never hardcode versions directly in `build.gradle`.
- **All dependencies** must be declared in `gradle/libs.versions.toml` as library entries and referenced in `build.gradle` via catalog aliases (e.g. `libs.spring.boot.starter.web`). Never use string notation directly in `build.gradle`.
