package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class SwiftJavaJavaRewriterIntegrationTest {
    private final RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");
    private final SwiftJavaJavaRewriter rewriter = new SwiftJavaJavaRewriter(options);

    @Test
    void fullPipelineRewritesOptionalGetterWithArena() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public Optional<String> getName(SwiftArena swiftArena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        assertTrue(result.contains("// Rewritten by swift-java-kotlin-accessors. Do not edit."));
        assertTrue(result.contains("import org.jetbrains.annotations.Nullable;"));
        assertTrue(result.contains("@Nullable"));
        assertTrue(result.contains("public String getName(SwiftArena swiftArena) {"));
        assertTrue(result.contains("return (result != 0L) ? value : null;"));
        assertTrue(result.contains("public String getName()"));
        assertTrue(result.contains("DEFAULT_SWIFT_JAVA_AUTO_ARENA"));
    }

    @Test
    void fullPipelineRewritesPrimitiveOptionals() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public OptionalLong getTimestamp(SwiftArena swiftArena) {",
            "        return (result != 0L) ? OptionalLong.of(value) : OptionalLong.empty();",
            "    }",
            "    public OptionalInt getCount(SwiftArena swiftArena) {",
            "        return (result != 0L) ? OptionalInt.of(value) : OptionalInt.empty();",
            "    }",
            "    public OptionalDouble getScore(SwiftArena swiftArena) {",
            "        return (result != 0L) ? OptionalDouble.of(value) : OptionalDouble.empty();",
            "    }",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        assertTrue(result.contains("public Long getTimestamp(SwiftArena swiftArena) {"));
        assertTrue(result.contains("public Integer getCount(SwiftArena swiftArena) {"));
        assertTrue(result.contains("public Double getScore(SwiftArena swiftArena) {"));
    }

    @Test
    void fullPipelineRewritesStaticMethodWithTrailingArena() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public static MyModel create(String name, SwiftArena swiftArena) {",
            "        return new MyModel(name, swiftArena);",
            "    }",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        assertTrue(result.contains("public static MyModel create(String name) {"));
        assertTrue(result.contains("return create(name, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"));
    }

    @Test
    void fullPipelineRewritesInterfaceMethods() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public interface MyInterface {",
            "    public Optional<String> getName(SwiftArena arena);",
            "    public OptionalLong getId();",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        assertTrue(result.contains("public String getName(SwiftArena arena);"));
        assertTrue(result.contains("public Long getId();"));
        assertEquals(2, result.split("@Nullable").length - 1);
    }

    @Test
    void fullPipelineRewritesOptionalParameters() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public void update(Optional<String> name, OptionalInt count) {",
            "        if (name.isPresent()) {",
            "            String val = name.orElse(null);",
            "        }",
            "        int c = count.orElse(0);",
            "    }",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        assertTrue(result.contains("String name, Integer count"));
        assertTrue(result.contains("name != null"));
        assertTrue(result.contains("((count != null) ? count : 0)"));
    }

    @Test
    void fullPipelineCombinesOptionalReturnOptionalParameterAndTrailingArena() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public static Optional<String> findTag(Optional<String> category, SwiftArena swiftArena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            "}",
            ""
        );
        String result = rewriter.rewrite(input);

        // Optional return is unwrapped to String with @Nullable
        assertTrue(result.contains("@Nullable"), "Should add @Nullable annotation for Optional return");
        assertTrue(result.contains("public static String findTag(String category, SwiftArena swiftArena)"),
            "Should unwrap Optional return and Optional parameter");
        // Optional.of/empty replaced with null-returning ternary
        assertTrue(result.contains("return (result != 0L) ? value : null;"),
            "Should rewrite Optional return expression");
        // Optional parameter is unwrapped (Optional<String> -> String)
        assertFalse(result.contains("Optional<String>"), "Should not contain Optional<String> anywhere");
        // Static trailing arena overload generated
        assertTrue(result.contains("public static String findTag(String category)"),
            "Should generate arena-free overload");
        assertTrue(result.contains("DEFAULT_SWIFT_JAVA_AUTO_ARENA"),
            "Should reference default arena in overload");
        // Nullable import added
        assertTrue(result.contains("import org.jetbrains.annotations.Nullable;"));
        // Rewrite marker present
        assertTrue(result.contains("// Rewritten by swift-java-kotlin-accessors. Do not edit."));
    }

    @Test
    void idempotent() {
        String input = String.join("\n",
            "package com.example;",
            "",
            "public class MyModel {",
            "    public Optional<String> getName(SwiftArena swiftArena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            "}",
            ""
        );
        String first = rewriter.rewrite(input);
        String second = rewriter.rewrite(first);
        assertEquals(first, second);
    }
}
