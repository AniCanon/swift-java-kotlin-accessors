# swift-java-kotlin-accessors

A Gradle plugin that post-processes [swift-java](https://github.com/swiftlang/swift-java) generated Java bindings into Kotlin-friendly sources.

## Why

[swift-java](https://github.com/swiftlang/swift-java) bridges Swift APIs into JVM code, but its generated Java bindings follow patterns that don't translate well to Kotlin:

- **Optional return types** — Kotlin has built-in nullability (`String?`), so `Optional<String>` adds friction instead of safety. Kotlin developers expect nullable types, not Optional wrappers.
- **Primitive Optional wrappers** — `OptionalLong`, `OptionalInt`, and `OptionalDouble` have no Kotlin equivalent and can't be used with Kotlin's null-safe operators (`?.`, `?:`, `!!`).
- **Arena-based getters** — Every property accessor requires an explicit `SwiftArena` parameter, which breaks Kotlin property access syntax and clutters call sites.
- **Static factory `init` methods** — Swift initializers become `ClassName.init(...)` in Java, which is awkward in Kotlin since `init` is a reserved word (requiring backtick escaping).

## Transforms

### Optional return types to nullable

```java
// Before (swift-java output)
public Optional<String> getName(SwiftArena arena) {
    return (result != 0L) ? Optional.of(value) : Optional.empty();
}

// After
@Nullable
public String getName(SwiftArena arena) {
    return (result != 0L) ? value : null;
}
```

### Primitive Optionals to boxed nullable

Same treatment applies to primitive Optional wrappers.

```java
// Before
public OptionalLong getTimestamp(SwiftArena arena) {
    return (result != 0L) ? OptionalLong.of(value) : OptionalLong.empty();
}

// After
@Nullable
public Long getTimestamp(SwiftArena arena) {
    return (result != 0L) ? value : null;
}
```

### Optional parameters to nullable

Method parameters using `Optional<T>` or primitive Optional types are rewritten to accept nullable values directly. Call-site idioms like `.isPresent()` and `.orElse(null)` are rewritten to null checks.

```java
// Before
public void update(Optional<String> name, OptionalInt count) {
    if (name.isPresent()) { ... }
    int c = count.orElse(0);
}

// After
public void update(String name, Integer count) {
    if (name != null) { ... }
    int c = ((count != null) ? count : 0);
}
```

### Arena parameter overloads

Methods that take a `SwiftArena` parameter get an overload that uses the default arena. This applies to both instance getters and static methods with a trailing arena parameter.

```java
// Instance getter — original preserved, no-arg overload added
public MyType getValue(SwiftArena swiftArena) { ... }
public MyType getValue() {
    return getValue(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);
}

// Static method — original preserved, arena-free overload added
public static MyType create(String name, SwiftArena swiftArena) { ... }
public static MyType create(String name) {
    return create(name, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);
}
```

### Kotlin factory functions (optional)

When enabled, generates top-level Kotlin factory functions that shadow the class name, giving you constructor-like syntax for swift-java types:

```kotlin
// Without factory functions (default)
val list = ProjectList.`init`("My Projects")

// With factory functions enabled
val list = ProjectList("My Projects")
```

For each `public static ClassName init(...)` method in the rewritten Java, a corresponding Kotlin function is generated in a `*Factories.kt` file:

```kotlin
// Generated: ProjectListFactories.kt
package com.example

fun ProjectList(name: String): ProjectList =
    ProjectList.`init`(name)
```

Parameters that were `Optional<T>`, `OptionalLong`, `OptionalInt`, or `OptionalDouble` in the original swift-java source are emitted as nullable Kotlin types:

```kotlin
// Original swift-java: Optional<String> outfitId
// Rewritten Java: String outfitId
// Generated Kotlin factory:
fun CreationResumeRoute(projectId: String, characterId: String, outfitId: String?, stage: Stage): CreationResumeRoute =
    CreationResumeRoute.`init`(projectId, characterId, outfitId, stage)
```

Trailing `SwiftArena` parameters are automatically stripped from the factory signatures. Arena-only `init` methods are skipped.

## Installation

Publish to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

Add `mavenLocal()` to your `settings.gradle.kts` plugin repositories:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

## Usage

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("dev.anicanon.swift-java-kotlin-accessors") version "0.1.0"
}

swiftJavaKotlinAccessors {
    // Directory containing swift-java generated Java sources
    inputDir.set(layout.projectDirectory.dir("src/generated/swift-java"))

    // Output directory for rewritten sources
    outputDir.set(layout.buildDirectory.dir("generated/swift-java-kotlin"))

    // Only rewrite classes in these packages (empty = rewrite all)
    packagePrefixes.set(listOf("com.example.swift"))

    // Fully qualified @Nullable annotation to add to rewritten methods
    nullableAnnotationFqcn.set("org.jetbrains.annotations.Nullable")

    // Generate Kotlin factory functions for swift-java init methods (default: false)
    generateKotlinFactories.set(true)
}
```

Then run:

```shell
./gradlew generateSwiftJavaKotlinAccessors
```

The rewritten sources are written to `outputDir`. Point your Kotlin source set at this directory instead of the raw swift-java output.

## Configuration

| Property | Type | Default | Description |
|---|---|---|---|
| `inputDir` | `DirectoryProperty` | *required* | swift-java generated sources directory |
| `outputDir` | `DirectoryProperty` | *required* | Output directory for rewritten sources |
| `packagePrefixes` | `ListProperty<String>` | `[]` (all) | Only rewrite classes in matching packages |
| `nullableAnnotationFqcn` | `Property<String>` | `""` (none) | FQCN of `@Nullable` annotation to insert |
| `generateKotlinFactories` | `Property<Boolean>` | `false` | Generate Kotlin factory functions for `init` methods |

## Design

The rewriter is a pipeline of composable `RewriteRule` implementations. Each rule is a single-responsibility transformation that can be tested independently. Optional type handling is data-driven via `OptionalVariant`, so adding support for new Optional types requires no new rule classes.

```
Source -> AddRewriteMarker -> AddNullableImport -> OptionalInterface* -> OptionalMethod* -> OptionalParameter -> ArenaOverloads -> Output
```

## Requirements

- Java 17+
- Gradle 8.0+

## License

[Apache License 2.0](LICENSE)
