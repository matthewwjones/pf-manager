# CLAUDE.md

This document captures the best practices and patterns established during the development of this project. It is the authoritative reference for code style, architecture decisions, and the pre-push checklist.

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
- **All dependencies** must be declared in `gradle/libs.versions.toml` as library entries and referenced in `build.gradle` via catalog aliases (e.g. `libs.spring.boot.starter.web`). Never use string notation (e.g. `'org.springframework.boot:spring-boot-starter-web'`) directly in `build.gradle`.

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



## Project Setup Best Practices

### 1. Use Version Catalogs (TOML)

Always centralise dependency versions in `gradle/libs.versions.toml`:

```toml
[versions]
spring-boot = "3.4.13"
wiremock-spring-boot = "3.10.6"

[libraries]
spring-boot-starter-webflux = { module = "org.springframework.boot:spring-boot-starter-webflux" }
wiremock-spring-boot = { module = "org.wiremock.integrations:wiremock-spring-boot", version.ref = "wiremock-spring-boot" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
versions = { id = "com.github.ben-manes.versions", version.ref = "versions" }
```

Benefits:
- Single source of truth for all versions
- No hardcoded versions in build files
- Easy to update dependencies
- Type-safe accessors in IDE

Usage in `build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.spring.boot.starter.webflux)
    testImplementation(libs.wiremock.spring.boot)
}
```

### 2. Condense JUnit Dependencies

Before (5 lines):
```kotlin
testImplementation(platform("org.junit:junit-bom:6.0.3"))
testImplementation(libs.junit.jupiter.api)
testImplementation(libs.junit.jupiter.params)
testRuntimeOnly(libs.junit.jupiter.engine)
testRuntimeOnly(libs.junit.platform.launcher)
```

After (2 lines):
```kotlin
testImplementation(libs.junit.jupiter)        // aggregates api, params, engine
testRuntimeOnly(libs.junit.platform.launcher)
```

### 3. Add Ben-Manes Versions Plugin

Essential for keeping dependencies up to date:

```kotlin
plugins {
    alias(libs.plugins.versions)
}
```

Usage:
```bash
./gradlew dependencyUpdates  # shows available updates
```

---

## Architectural Decisions

### HttpGraphQlClient.url() Gotcha

`HttpGraphQlClient.Builder.url(String)` calls `webClientBuilder.baseUrl(url)`, which **replaces** the WebClient's base URL. Do not pass a path-only string like `/api/v1/graphql`.

Correct pattern: put the full URL including path in `karat.base-url` in `application.yml`, and do not call `.url()` on the builder:

```java
// In KaratGraphQlConfig:
WebClient webClient = WebClient.builder().baseUrl(props.baseUrl()).build(); // full URL
return HttpGraphQlClient.builder(webClient).build();                        // no .url() call
```

### WireMock + @DynamicPropertySource Pattern

Start WireMock in a static initialiser (before the Spring context), then register the full URL:

```java
static final WireMockServer wireMock;
static {
    wireMock = new WireMockServer(options().dynamicPort());
    wireMock.start();
}

@DynamicPropertySource
static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("karat.base-url", () -> wireMock.baseUrl() + "/api/v1/graphql");
}
```

Note: the WireMock Spring Boot integration (`org.wiremock.integrations:wiremock-spring-boot`) lives in package `org.wiremock.spring`. Its `@ConfigureWireMock` uses `baseUrlProperties`, not `property`.

### REST Pagination

Expose a `next` URL field (null when no more pages exist) and accept a `pageToken` query parameter. Never expose internal cursor tokens or GraphQL pagination objects to REST consumers.

```java
// Good — next is an absolute or relative URL the consumer can follow directly
record PetPage(int totalCount, String next, List<Pet> pets) {

    static PetPage from(Page<Pet> page, ServerHttpRequest request) {
        var pageInfo = page.pageInfo();
        var next = (pageInfo != null && pageInfo.hasNextPage())
                ? UriComponentsBuilder.fromUri(request.getURI())
                        .replaceQueryParam("pageToken", pageInfo.endCursor())
                        .build().toUriString()
                : null;
        return new PetPage(page.totalCount(), next, page.nodes());
    }
}

// Bad — exposing internal cursor tokens directly
record BadPage(String endCursor, boolean hasNextPage, List<Pet> nodes) {}
```

### OpenAPI Annotations in `*Api` Interfaces

All OpenAPI/Swagger annotations go on the `*Api` interface, not on the controller class. Controllers contain only implementation logic.

```java
// Good — PetApi.java holds all annotations
@Tag(name = "Pets", description = "...")
@RequestMapping("/pets")
interface PetApi {

    @Operation(summary = "List pets", ...)
    @GetMapping(produces = V1_JSON)
    Mono<PetPage> listPets(...);
}

// Good — PetController.java is clean implementation
@RestController
public class PetController implements PetApi {

    @Override
    public Mono<PetPage> listPets(...) {
        // implementation only
    }
}
```

---

## Code Quality Standards

### Use `final` on Fields

Declare fields `final` wherever they are not reassigned.

### No Comments in Tests

Test method names must be self-documenting. Inline comments are noise.

```java
// Bad
@Test
void fetchesAllPets() {
    // Set up the filter
    PetFilter filter = PetFilter.builder().status("AVAILABLE").build();
    // Call the service
    List<Pet> result = service.listAll(filter, null).collectList().block();
    // Check we got results
    assertThat(result).isNotEmpty();
}

// Good
@Test
void fetchesAllPets() {
    final PetFilter filter = PetFilter.builder().status("AVAILABLE").build();
    final List<Pet> result = service.listAll(filter, null).collectList().block();
    assertThat(result).isNotEmpty();
}
```

### No Spurious Comments in Production Code

A comment is spurious if it restates what the code already clearly says. Comments are appropriate only when explaining *why* something non-obvious is done, or documenting a known gotcha (like the `HttpGraphQlClient.url()` behaviour above).

```java
// Bad — the comment adds nothing
// Fetch the page from the API
return graphQlClient.retrieve("pets").toEntity(...);

// Bad — variable name already communicates this
// cursor for the next page
String cursor = pageInfo.endCursor();

// Good — explains a non-obvious decision
// Do NOT call .url() here; it replaces the WebClient base URL rather than appending.
return HttpGraphQlClient.builder(webClient).build();
```

### Package-Private Methods for Testing

Make helper methods package-private (no access modifier) instead of `private` so they can be tested directly from the same package in the test source tree.

```java
// Bad — cannot be tested in isolation
private PetFilter buildFilter(String status, String type) { ... }

// Good — testable from the same package
PetFilter buildFilter(String status, String type) { ... }
```

Then write dedicated tests:
```java
class PetControllerFilterTest {
    @Test
    void buildsFilterFromTypeParam() {
        final PetController controller = new PetController(...);
        final PetFilter filter = controller.buildFilter("AVAILABLE", "DOG");
        assertThat(filter.type()).isEqualTo("DOG");
    }
}
```

### Always Use Curly Braces

All `if`, `else`, `for`, `while`, and `do-while` statements must use curly braces, even for single-line bodies. This prevents subtle bugs when adding lines later and improves readability.

```java
// Bad
if (exports.contains("users"))
    tasks.add(csvExportService.exportUsers(users).flux());

// Good
if (exports.contains("users")) {
    tasks.add(csvExportService.exportUsers(users).flux());
}
```

### Class Size Limit — 150 Lines Maximum

No class should exceed 150 lines. A class approaching this limit is a signal to split it by responsibility. Name the resulting classes after what they do, not after the original class.

### Small, Focused Classes (~50 Lines)

Prefer classes of around 50 lines. A class that grows past roughly 100 lines is a signal to split it. Name the resulting classes after what they do, not after the original class.

```java
// Bad — one controller doing everything
public class PetController {
    // filter building, pagination, CSV export,
    // error mapping, OpenAPI annotations ... 300 lines
}

// Good — split by responsibility
PetApi         // OpenAPI contract (interface)
PetController  // REST wiring (implements PetApi)
CsvResponse    // shared CSV ResponseEntity builder
PetPage        // REST response record with from() factory
```

### Break Down Complex Logic

Prefer many small, named methods over a few large ones. Every extracted method is a candidate for a direct unit test.

```java
// Bad — one large method
public Mono<Page<Pet>> fetchAll(PetFilter filter, String search) {
    // 80 lines combining pagination, filtering, error handling
}

// Good — composed from small, named methods
public Flux<Pet> listAll(PetFilter filter, String search) {
    return Flux.expand(page -> fetchNextPage(page, filter, search))
               .concatMap(page -> Flux.fromIterable(page.nodes()));
}

Mono<Page<Pet>> fetchNextPage(Page<Pet> previous, PetFilter filter, String search) {
    return hasMore(previous)
            ? client.fetchPage(filter, search, previous.pageInfo().endCursor())
            : Mono.empty();
}

boolean hasMore(Page<Pet> page) {
    return page.pageInfo() != null && page.pageInfo().hasNextPage();
}
```

### Prefer Immutable Objects

Use Java records, `final` fields, and immutable collections. Avoid setters, mutable state, and `Optional.set`.

```java
// Bad — mutable, setter-based object
public class PetFilter {
    private String status;
    public void setStatus(String status) { this.status = status; }
}

// Good — immutable record (or Lombok @Value with builder)
public record PetFilter(String id, String status, String type) {
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        // Lombok @Builder achieves the same result
    }
}
```

### Prefer Non-Static Code

Avoid static utility methods scattered across the codebase. Prefer instance methods on well-named classes that can be injected and tested.

```java
// Bad
public class CsvUtils {
    public static ResponseEntity<byte[]> buildCsvResponse(byte[] bytes, String filename) { ... }
}

// Good — a small class with a clear purpose, usable as a Spring bean or static factory
public final class CsvResponse {
    public static ResponseEntity<byte[]> of(byte[] bytes, String filename) { ... }
    private CsvResponse() {}
}
```

### Static Imports for Readability

Use static imports when they make call sites clearer, particularly for constants, assertions, and factory methods. Do not use them when the originating class provides important context.

```java
// Good — assertion methods are clearly assertion methods
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

assertAll(
    () -> assertThat(page.totalCount()).isEqualTo(10),
    () -> assertThat(page.next()).isNull()
);

// Good — media type constants
import static org.fifties.housewife.server.MediaTypes.CSV;
import static org.fifties.housewife.server.MediaTypes.V1_JSON;

// Bad — static import removes important context
import static org.example.server.PetController.buildFilter; // unclear at call site
```

### No Magic Hard-Coded Strings

Extract repeated string literals into named constants. A string that appears more than once, or whose meaning is not self-evident, must be a constant. Name constants after the value they represent — do not add type prefixes like `VAR_`, `STR_`, `KEY_`.

```java
// Bad — magic strings inline
client.variable("first", 50);
client.variable("after", cursor);

// Bad — type prefix adds nothing
private static final String VAR_FIRST = "first";

// Good — name describes the value, no prefix
public static final String FIRST = "first";
public static final String AFTER = "after";

client.variable(FIRST, props.pageSize());
client.variable(AFTER, cursor);
```

### Meaningful Variable Names — No Abbreviations

Names must communicate intent clearly to a human reader. Abbreviations are only acceptable when they are universally understood in context (e.g. `id`, `url`, `csv`).

```java
// Bad
WebClient wc = WebClient.builder().baseUrl(props.baseUrl()).build();
ByteArrayOutputStream baos = new ByteArrayOutputStream();
PageInfo pi = page.pageInfo();

// Good
WebClient webClient = WebClient.builder().baseUrl(props.baseUrl()).build();
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
PageInfo pageInfo = page.pageInfo();
```

### Meaningful Business Nouns in REST Responses

REST response fields must use meaningful business nouns, not generic container names.

```java
// Bad — generic field names leak implementation details
record Page(int totalCount, String next, List<Pet> items) {}
record Page(int totalCount, String next, List<Pet> nodes) {}
record Page(int totalCount, String next, List<Pet> results) {}

// Good — the field name tells you what it contains
record PetPage(int totalCount, String next, List<Pet> pets) {}
record OrderPage(int totalCount, String next, List<Order> orders) {}
record CustomerPage(int totalCount, String next, List<Customer> customers) {}
```

### Extract Shared Utilities into Named Classes

When the same pattern appears across more than one controller or service, extract it. Name the class after what it produces, not what it does.

```java
// Bad — repeated in every controller that produces CSV
ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
    .contentType(MediaType.parseMediaType("text/csv"))
    .body(bytes);

// Good — extracted into CsvResponse
public final class CsvResponse {
    public static ResponseEntity<byte[]> of(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(MediaTypes.CSV))
                .body(bytes);
    }
    private CsvResponse() {}
}

// Usage
return csvExportService.petsToCsvBytes(flux)
        .map(bytes -> CsvResponse.of(bytes, "pets.csv"));
```

### Class and Method Naming

Names must be clear to a human reader. Avoid jargon terms that describe role rather than purpose:

- No `*Helper`, `*Util`, `*Manager`, `*Processor` — name classes after what they represent or produce
- No method names that start with `handle`, `process`, `do`, `perform` — name methods after what they return or what they change

```java
// Bad
class PaginationHelper { ... }
void handlePetFilter(...) { ... }

// Good
class Pages { ... }             // follows JDK convention: Collections, Files, Paths, Pages
void buildFilter(...) { ... }   // returns a PetFilter
void fetchPage(...) { ... }     // returns a Mono<Page<Pet>>
```

### Logging

Use Lombok `@Slf4j` for all logging. Never use `System.out.println` or `e.printStackTrace()`.

```java
// Bad
System.out.println("Fetching page: " + cursor);
catch (Exception e) { e.printStackTrace(); }

// Good
@Slf4j
public class PetClient {
    log.debug("Fetching page (cursor={}, pageSize={})", cursor, pageSize);
    log.error("GraphQL request failed", exception);
}
```

---

## Test Standards

### No Underscores in Test Names, No Word "test"

Test method names are plain English camelCase sentences describing the behaviour under test.

```java
// Bad
@Test void test_fetchPage_returnsResults() {}
@Test void testFetchPageReturnsResults() {}

// Good
@Test void fetchesPageOfPets() {}
@Test void returnsEmptyPageWhenNoPetsMatch() {}
@Test void propagatesErrorWhenUpstreamFails() {}
```

### Use `assertAll` for Multiple Assertions

When asserting multiple properties of the same object, wrap them in `assertAll` so all failures are reported together.

```java
// Bad — stops at first failure, hides subsequent problems
assertThat(page.totalCount()).isEqualTo(10);
assertThat(page.pets()).hasSize(10);
assertThat(page.next()).isNull();

// Good — all three are reported even if the first fails
assertAll(
    () -> assertThat(page.totalCount()).isEqualTo(10),
    () -> assertThat(page.pets()).hasSize(10),
    () -> assertThat(page.next()).isNull()
);
```

### Tests Are Self-Documenting

A reader must understand what behaviour is being tested without reading the implementation. Structure tests as: arrange, act, assert — no comments needed.

```java
// Bad — requires reading implementation to understand intent
@Test
void returnsPage() {
    wireMock.stubFor(post(urlEqualTo("/api/v1/graphql")).willReturn(okJson(RESPONSE)));
    var result = client.fetchPage(PetFilter.builder().build(), null, null).block();
    assertThat(result).isNotNull();
}

// Good — intent is clear from names and structure alone
@Test
void returnsPageWithCorrectNodeCountAndCursor() {
    wireMock.stubFor(post(urlEqualTo("/api/v1/graphql")).willReturn(okJson(petResponse(10, "cursor_abc"))));
    final Page<Pet> page = client.fetchPage(PetFilter.builder().build(), null, null).block();
    assertAll(
        () -> assertThat(page.nodes()).hasSize(10),
        () -> assertThat(page.pageInfo().endCursor()).isEqualTo("cursor_abc")
    );
}
```

### No Disabled Tests or Commented-Out Code

A disabled test is a lie — it looks like coverage but provides none. Delete it or fix it. Commented-out code belongs in git history, not in source files.

```java
// Bad
@Disabled("TODO: fix this later")
@Test void fetchesPets() { ... }

// Bad
// @Test void fetchesOrders() { ... }

// Good — fix it, or delete it and recover from git history if needed
```

---

## Apply to Any Cloned Repo

When setting up a new or cloned repository, apply these changes in order:

1. Add `gradle/libs.versions.toml` and move all versions into it
2. Replace hardcoded version strings in `build.gradle.kts` with catalog references
3. Add the Ben-Manes versions plugin
4. Condense JUnit dependencies to 2 lines
5. Remove spurious comments
6. Fix all compiler warnings (`-Xlint:all`)

---

## Pre-Push Checklist

Work through this checklist before pushing or opening a pull request.

### Dead Code

- [ ] No unused methods — every non-private method has at least one caller outside its own class (or is a tested package-private helper)
- [ ] No unused imports
- [ ] No unused fields or constants

### Build and Dependencies

- [ ] All dependency versions are declared in `gradle/libs.versions.toml` — no hardcoded versions in `build.gradle.kts`
- [ ] `./gradlew dependencyUpdates` shows no outdated stable dependencies
- [ ] Ben-Manes versions plugin is present in all modules that manage dependencies
- [ ] JUnit dependencies condensed to 2 lines (`libs.junit.jupiter` + `junit.platform.launcher`)
- [ ] No compiler warnings — `./gradlew build` is clean under `-Xlint:all`

### Code Quality

- [ ] No spurious inline comments — every comment explains *why*, not *what*
- [ ] No magic hard-coded strings — repeated or opaque literals are extracted into named constants
- [ ] No abbreviations in variable, field, or parameter names (`webClient` not `wc`, `outputStream` not `baos`)
- [ ] Class and method names are plain English business terms — no `Helper`, `Util`, `Manager`, `Processor`
- [ ] `final` fields that are not reassigned
- [ ] All non-private helper methods are package-private (no modifier), not `private`
- [ ] No static utility methods that should be instance methods
- [ ] Static imports used only where they improve readability
- [ ] All `if`/`else`/`for`/`while` statements use curly braces — no braceless single-line bodies
- [ ] No class exceeds 150 lines — split by responsibility if approaching the limit
- [ ] No deprecated API calls

### Design and Architecture

- [ ] Classes are focused and small (target ~50 lines; investigate anything over 100)
- [ ] REST response fields use meaningful business nouns (`pets`, not `items` or `nodes`)
- [ ] REST pagination uses `next` URL + `pageToken` param — no internal cursors exposed
- [ ] OpenAPI/Swagger annotations are on `*Api` interfaces, not on controller classes
- [ ] Shared response-building logic is extracted into small named classes (e.g. `CsvResponse.of(...)`)
- [ ] Immutable objects are preferred — records, `final` fields, no unnecessary setters
- [ ] `System.out` and `e.printStackTrace()` are absent — Lombok `@Slf4j` is used throughout

### Tests

- [ ] All tests pass: `./gradlew test`
- [ ] Test coverage is at or above 80%: `./gradlew jacocoTestReport` (verify the HTML report)
- [ ] Unit tests exist for all package-private helper methods
- [ ] No test method names contain underscores or the word "test"
- [ ] Multiple assertions use `assertAll` so all failures are visible at once
- [ ] No `@Disabled` tests and no commented-out test code
- [ ] No inline comments inside test methods — names and structure carry all meaning

### Swagger / OpenAPI

- [ ] Every `@ExampleObject` in `*Api` interfaces matches the corresponding Java record schema — no fields in examples that are absent from the record, and all non-nullable record fields are represented
- [ ] Example field ordering matches `@JsonPropertyOrder` on the record (e.g. `organization` before `id` for `Role`, `Group`, `JobRequisition`)
- [ ] Nested objects in examples match their own record schema (e.g. `Group` inside a `Role` example must include `organization`, `id`, `name`, `type`, `active`, `archived`)
- [ ] Page response examples include `totalCount`, `next`, and the correctly named list field (`candidacies`, `roles`, `users`, etc.)

### Documentation

- [ ] README examples are consistent with actual code — endpoints, method signatures, filter fields, CSV columns, and library usage all match the implementation
- [ ] Every REST endpoint is listed in the README endpoints table
- [ ] Every CLI filter documented in the README exists in `ExportFilterSource`
- [ ] Every CSV column table matches the `@JsonPropertyOrder` in the corresponding `*CsvRow` class

### Security

- [ ] No secrets, tokens, or credentials in source files or `application.yml` (use environment variables or secret managers)
- [ ] `application.yml` contains only non-sensitive defaults; sensitive values reference environment variables