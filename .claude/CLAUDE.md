# Project Configuration

## Language & Runtime
- Java 17 (JDK 17)
- No third-party libraries — use only the Java standard library

## Build System
- Gradle (Kotlin or Groovy DSL)

## Rules
- Do not add any external dependencies to `build.gradle` / `build.gradle.kts`
- Only use packages available in `java.*`, `javax.*`, and `jdk.*`
- Target Java 17 language features and bytecode (`sourceCompatibility = JavaVersion.VERSION_17`)
