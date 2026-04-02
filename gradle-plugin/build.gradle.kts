plugins {
    `java-gradle-plugin`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(project(":core"))
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
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

// java-gradle-plugin auto-creates "pluginMaven" and marker publications.
// Configure the auto-created publication with the correct coordinates.
afterEvaluate {
    publishing {
        publications {
            named<MavenPublication>("pluginMaven") {
                groupId = rootProject.group.toString()
                artifactId = "kotlin-accessors-gradle-plugin"
                version = rootProject.version.toString()
            }
        }
    }
}
