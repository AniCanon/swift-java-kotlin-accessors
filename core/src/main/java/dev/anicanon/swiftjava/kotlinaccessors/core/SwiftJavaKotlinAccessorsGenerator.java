package dev.anicanon.swiftjava.kotlinaccessors.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SwiftJavaKotlinAccessorsGenerator {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^package\\s+([\\w.]+);\\s*$");

    public void generate(Path inputDir, Path outputDir, RewriteOptions options) throws IOException {
        Files.createDirectories(outputDir);
        SwiftJavaJavaRewriter rewriter = new SwiftJavaJavaRewriter(options);
        try (Stream<Path> paths = Files.walk(inputDir)) {
            paths.filter(Files::isRegularFile)
                .sorted(Comparator.naturalOrder())
                .forEach(path -> rewriteFile(path, inputDir, outputDir, options, rewriter));
        }
    }

    private void rewriteFile(Path file, Path inputDir, Path outputDir, RewriteOptions options, SwiftJavaJavaRewriter rewriter) {
        try {
            Path relativePath = inputDir.relativize(file);
            Path outputFile = outputDir.resolve(relativePath);
            Files.createDirectories(outputFile.getParent());

            if (!file.getFileName().toString().endsWith(".java")) {
                Files.copy(file, outputFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            String source = Files.readString(file, StandardCharsets.UTF_8);
            String packageName = extractPackageName(source);
            String rewritten = options.shouldRewritePackage(packageName)
                ? rewriter.rewrite(source)
                : source;
            Files.writeString(outputFile, rewritten, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rewrite " + file, e);
        }
    }

    private String extractPackageName(String source) {
        Matcher matcher = PACKAGE_PATTERN.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
