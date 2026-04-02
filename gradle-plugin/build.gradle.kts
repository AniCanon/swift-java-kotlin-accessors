plugins {
    `java-gradle-plugin`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(project(":core"))
}

gradlePlugin {
    plugins {
        create("swiftJavaKotlinAccessors") {
            id = "dev.anicanon.swift-java-kotlin-accessors"
            implementationClass = "dev.anicanon.swiftjava.kotlinaccessors.gradle.SwiftJavaKotlinAccessorsPlugin"
            displayName = "swift-java-kotlin-accessors"
            description = "Rewrites swift-java generated Java bindings into Kotlin-friendly generated sources."
        }
    }
}
