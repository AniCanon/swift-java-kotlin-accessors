package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SwiftJavaKotlinAccessorsGeneratorTest {
    @TempDir
    Path tempDir;

    @Test
    void rewritesJavaFilesAndCopiesOthers() throws IOException {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Path packageDir = inputDir.resolve("com/example");
        Files.createDirectories(packageDir);

        String javaSource = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public Optional<String> getName(SwiftArena swiftArena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            "}",
            ""
        );
        Files.writeString(packageDir.resolve("MyModel.java"), javaSource, StandardCharsets.UTF_8);
        Files.writeString(packageDir.resolve("data.txt"), "keep this", StandardCharsets.UTF_8);

        RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");
        new SwiftJavaKotlinAccessorsGenerator().generate(inputDir, outputDir, options);

        Path outputJava = outputDir.resolve("com/example/MyModel.java");
        assertTrue(Files.exists(outputJava));
        String rewritten = Files.readString(outputJava, StandardCharsets.UTF_8);
        assertTrue(rewritten.contains("@Nullable"));
        assertTrue(rewritten.contains("public String getName(SwiftArena swiftArena) {"));

        Path outputTxt = outputDir.resolve("com/example/data.txt");
        assertTrue(Files.exists(outputTxt));
        assertEquals("keep this", Files.readString(outputTxt, StandardCharsets.UTF_8));
    }

    @Test
    void respectsPackagePrefixFilter() throws IOException {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Path includedDir = inputDir.resolve("com/included");
        Path excludedDir = inputDir.resolve("com/excluded");
        Files.createDirectories(includedDir);
        Files.createDirectories(excludedDir);

        String source = String.join("\n",
            "package %s;",
            "",
            "public class Model {",
            "    public Optional<String> getName(SwiftArena swiftArena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            "}",
            ""
        );
        Files.writeString(includedDir.resolve("Model.java"),
            String.format(source, "com.included"), StandardCharsets.UTF_8);
        Files.writeString(excludedDir.resolve("Model.java"),
            String.format(source, "com.excluded"), StandardCharsets.UTF_8);

        RewriteOptions options = new RewriteOptions(List.of("com.included"), "org.jetbrains.annotations.Nullable");
        new SwiftJavaKotlinAccessorsGenerator().generate(inputDir, outputDir, options);

        String included = Files.readString(outputDir.resolve("com/included/Model.java"), StandardCharsets.UTF_8);
        assertTrue(included.contains("public String getName"));

        String excluded = Files.readString(outputDir.resolve("com/excluded/Model.java"), StandardCharsets.UTF_8);
        assertTrue(excluded.contains("Optional<String>"));
    }
}
