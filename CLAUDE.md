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

## Conventions

- **Dependency versions** must always be managed in `gradle/libs.versions.toml`. Never hardcode versions directly in `build.gradle`.

## Code Quality

Follow the principles in *Clean Code* by Robert C. Martin.

### Formatting
- All code must be formatted to IntelliJ defaults and imports optimised (unused imports removed, imports ordered per IntelliJ conventions) before saving.
- No blank lines between field declarations:
  ```java
  // correct
  public class EodhdClient {
      private static final String MONTHLY_PERIOD = "m";
      private static final String JSON_FORMAT = "json";
      private final RestClient restClient;
      private final String apiKey;

  // wrong
  public class EodhdClient {

      private static final String MONTHLY_PERIOD = "m";
      private static final String JSON_FORMAT = "json";

      private final RestClient restClient;
      private final String apiKey;
  ```

- Align continuation parameters with the opening parenthesis, not indented on a new line:
  ```java
  // correct
  public EodhdClient(@Value("${eodhd.base-url}") String baseUrl,
                     @Value("${eodhd.api-key}") String apiKey) {

  // wrong
  public EodhdClient(
          @Value("${eodhd.base-url}") String baseUrl,
          @Value("${eodhd.api-key}") String apiKey
  ) {
  ```

### REST API / Swagger
- Annotate all Swagger-exposed endpoints with meaningful OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse`, `@Schema`, etc.) including example schema objects for request and response types.
- To avoid cluttering controller classes, extract all OpenAPI annotations onto a dedicated `*Api` interface per controller; the controller implements the interface and contains only business logic.
  ```java
  // PortfolioApi.java — annotations only
  @Tag(name = "Portfolio", description = "Manage portfolios")
  public interface PortfolioApi {
      @Operation(summary = "List all portfolios")
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PortfolioResponse.class)))
      ResponseEntity<List<PortfolioResponse>> getPortfolios();
  }

  // PortfolioController.java — business logic only
  @RestController
  public class PortfolioController implements PortfolioApi {
      @Override
      public ResponseEntity<List<PortfolioResponse>> getPortfolios() { ... }
  }
  ```

### Testing
- Write unit tests for all code that contains logic. POJOs with only getters/setters do not need tests.
- When a test has multiple assertions, always wrap them in `assertAll()` (JUnit 5).