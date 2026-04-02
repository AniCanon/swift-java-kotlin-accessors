package dev.anicanon.swiftjava.kotlinaccessors.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SwiftJavaKotlinAccessorsPluginFunctionalTest {

    @TempDir
    File projectDir;

    @Test
    void generateSwiftJavaKotlinAccessors_rewritesJavaAndGeneratesKotlinFactory() throws IOException {
        // --- arrange: write build.gradle ---
        String buildScript = """
                plugins {
                    id("dev.anicanon.swift-java-kotlin-accessors")
                }
                swiftJavaKotlinAccessors {
                    inputDir.set(layout.projectDirectory.dir("src/input"))
                    outputDir.set(layout.buildDirectory.dir("generated"))
                    packagePrefixes.set(listOf("com.example"))
                    generateKotlinFactories.set(true)
                }
                """;
        writeFile("build.gradle.kts", buildScript);
        writeFile("settings.gradle.kts", "rootProject.name = \"test-project\"\n");

        // --- arrange: write a dummy Java source with a static init method ---
        String javaSource = """
                package com.example;

                public final class Hero {
                    public static Hero init(String name, SwiftArena swiftArena) {
                        return null;
                    }
                }
                """;
        writeFile("src/input/com/example/Hero.java", javaSource);

        // --- act ---
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("generateSwiftJavaKotlinAccessors", "--stacktrace")
                .withPluginClasspath()
                .build();

        // --- assert: task succeeded ---
        assertTrue(
                result.task(":generateSwiftJavaKotlinAccessors").getOutcome() == TaskOutcome.SUCCESS,
                "Task should succeed");

        // --- assert: rewritten Java file exists and contains marker ---
        Path outputJava = projectDir.toPath()
                .resolve("build/generated/com/example/Hero.java");
        assertTrue(Files.exists(outputJava), "Rewritten Java file should exist at " + outputJava);

        String rewrittenContent = Files.readString(outputJava, StandardCharsets.UTF_8);
        assertTrue(
                rewrittenContent.contains("Rewritten by swift-java-kotlin-accessors"),
                "Rewritten file should contain the rewrite marker");

        // --- assert: Kotlin factory file exists ---
        Path outputKotlin = projectDir.toPath()
                .resolve("build/generated/com/example/HeroFactories.kt");
        assertTrue(Files.exists(outputKotlin), "Kotlin factory file should exist at " + outputKotlin);

        String kotlinContent = Files.readString(outputKotlin, StandardCharsets.UTF_8);
        assertTrue(
                kotlinContent.contains("fun Hero("),
                "Kotlin factory should contain a factory function for Hero");
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path path = projectDir.toPath().resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
