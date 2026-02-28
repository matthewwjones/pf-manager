# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**pf-manager** is a Java 21 portfolio manager application. Group: `com.mattjoneslondon`. Build system: Gradle 9.0.0. Test framework: JUnit 5 (Jupiter).

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

Early-stage Java project. Source layout follows standard Maven/Gradle conventions:

- `src/main/java/` — application source (package root: `com.mattjoneslondon`)
- `src/main/resources/` — runtime resources and configuration
- `src/test/java/` — JUnit 5 tests mirroring the main package structure
- `src/test/resources/` — test fixtures and configuration

No linting or code formatting tools are currently configured.