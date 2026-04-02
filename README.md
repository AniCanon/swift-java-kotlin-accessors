# swift-java-kotlin-accessors

Post-processes `swift-java` generated Java bindings into Kotlin-friendly generated sources.

Goals:

- preserve `swift-java` as the raw generator
- avoid app-local DTO mirrors and per-field accessor glue
- make Kotlin consume generated shared models with natural property access

Planned transforms:

- rewrite `Optional<T>` getters to nullable-style getters
- rewrite `OptionalLong` / `OptionalInt` / `OptionalDouble` getters to boxed nullable getters
- add no-arg getter overloads for arena-based getters using `DEFAULT_SWIFT_JAVA_AUTO_ARENA`

This repository is intentionally generic and should not contain AniCanon-specific model logic.
