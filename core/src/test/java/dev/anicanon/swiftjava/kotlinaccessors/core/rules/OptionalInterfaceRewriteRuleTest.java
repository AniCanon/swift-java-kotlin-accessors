package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import dev.anicanon.swiftjava.kotlinaccessors.core.OptionalVariant;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import java.util.List;
import org.junit.jupiter.api.Test;

class OptionalInterfaceRewriteRuleTest {
    private final RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");

    @Test
    void rewritesGenericOptionalInterface() {
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = "    public Optional<String> getName(SwiftArena arena);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("@Nullable"));
        assertTrue(result.contains("public String getName(SwiftArena arena);"));
        assertFalse(result.contains("Optional<String>"));
    }

    @Test
    void rewritesOptionalLongInterface() {
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.primitive("OptionalLong", "Long"), options
        );
        String input = "    public OptionalLong getTimestamp(SwiftArena arena);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("@Nullable"));
        assertTrue(result.contains("public Long getTimestamp(SwiftArena arena);"));
    }

    @Test
    void rewritesOptionalIntInterface() {
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.primitive("OptionalInt", "Integer"), options
        );
        String input = "    public OptionalInt getCount();\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public Integer getCount();"));
    }

    @Test
    void rewritesOptionalDoubleInterface() {
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.primitive("OptionalDouble", "Double"), options
        );
        String input = "    public OptionalDouble getScore();\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public Double getScore();"));
    }

    @Test
    void skipsEnumCaseAccessors() {
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = "    public Optional<MyEnum> getAsMyCase();\n";
        String result = rule.apply(input);
        assertEquals(input, result);
    }

    @Test
    void noAnnotationWhenFqcnBlank() {
        RewriteOptions noAnnotation = new RewriteOptions(List.of(), "");
        OptionalInterfaceRewriteRule rule = new OptionalInterfaceRewriteRule(
            OptionalVariant.genericOptional(), noAnnotation
        );
        String input = "    public Optional<String> getName();\n";
        String result = rule.apply(input);
        assertFalse(result.contains("@"));
        assertTrue(result.contains("public String getName();"));
    }
}
