# swift-java-kotlin-accessors

A Gradle plugin that post-processes [swift-java](https://github.com/swiftlang/swift-java) generated Java bindings into Kotlin-friendly sources.

`swift-java` generates Java classes with `Optional<T>` return types, primitive Optional wrappers, and arena-based getters. These work fine from Java but are awkward from Kotlin. This plugin rewrites them into idiomatic patterns — nullable returns, boxed primitives, and no-arg overloads — so Kotlin consumers get natural property access without hand-written glue code.

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

`OptionalLong`, `OptionalInt`, and `OptionalDouble` are rewritten to their boxed nullable equivalents.

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

### No-arg arena getter overloads

Getters that take a `SwiftArena` parameter get a no-arg overload using the default arena.

```java
// Original (preserved)
public MyType getValue(SwiftArena swiftArena) { ... }

// Added overload
public MyType getValue() {
    return getValue(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);
}
```

### Static trailing arena overloads

Static methods where `SwiftArena` is the last parameter get an overload without it.

```java
// Original (preserved)
public static MyType create(String name, SwiftArena swiftArena) { ... }

// Added overload
public static MyType create(String name) {
    return create(name, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);
}
```

## Usage

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("dev.anicanon.swift-java-kotlin-accessors")
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

## Design

The rewriter is a pipeline of composable `RewriteRule` implementations. Each rule is a single-responsibility transformation that can be tested independently. Optional type handling is data-driven via `OptionalVariant`, so adding support for new Optional types requires no new rule classes.

```
Source -> AddRewriteMarker -> AddNullableImport -> OptionalInterface* -> OptionalMethod* -> OptionalParameter -> ArenaGetter -> StaticArena -> Output
```

## Requirements

- Java 17+
- Gradle 8.0+

## License

[Apache License 2.0](LICENSE)
