package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import dev.anicanon.swiftjava.kotlinaccessors.core.OptionalVariant;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import java.util.List;
import org.junit.jupiter.api.Test;

class OptionalMethodRewriteRuleTest {
    private final RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");

    @Test
    void rewritesGenericOptionalMethod() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = String.join("\n",
            "    public Optional<String> getName(SwiftArena arena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("@Nullable"));
        assertTrue(result.contains("public String getName(SwiftArena arena) {"));
        assertTrue(result.contains("return (result != 0L) ? value : null;"));
        assertFalse(result.contains("Optional.of"));
        assertFalse(result.contains("Optional.empty"));
    }

    @Test
    void rewritesStaticOptionalMethod() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = String.join("\n",
            "    public static Optional<String> fromId(long id) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public static String fromId(long id) {"));
    }

    @Test
    void rewritesOptionalLongMethod() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.primitive("OptionalLong", "Long"), options
        );
        String input = String.join("\n",
            "    public OptionalLong getTimestamp(SwiftArena arena) {",
            "        return (result != 0L) ? OptionalLong.of(value) : OptionalLong.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public Long getTimestamp(SwiftArena arena) {"));
        assertTrue(result.contains("return (result != 0L) ? value : null;"));
    }

    @Test
    void rewritesOptionalIntMethod() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.primitive("OptionalInt", "Integer"), options
        );
        String input = String.join("\n",
            "    public OptionalInt getCount() {",
            "        return (result != 0L) ? OptionalInt.of(value) : OptionalInt.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public Integer getCount() {"));
    }

    @Test
    void rewritesOptionalDoubleMethod() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.primitive("OptionalDouble", "Double"), options
        );
        String input = String.join("\n",
            "    public OptionalDouble getScore() {",
            "        return (result != 0L) ? OptionalDouble.of(value) : OptionalDouble.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public Double getScore() {"));
    }

    @Test
    void skipsEnumCaseAccessors() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = String.join("\n",
            "    public Optional<MyEnum> getAsMyCase(SwiftArena arena) {",
            "        return (result != 0L) ? Optional.of(value) : Optional.empty();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("Optional<MyEnum>"));
    }

    @Test
    void preservesMethodWithNoOptionalReturn() {
        OptionalMethodRewriteRule rule = new OptionalMethodRewriteRule(
            OptionalVariant.genericOptional(), options
        );
        String input = String.join("\n",
            "    public Optional<String> getName(SwiftArena arena) {",
            "        return someOtherLogic();",
            "    }",
            ""
        );
        String result = rule.apply(input);
        // Body doesn't match the Optional return pattern, so method is preserved as-is
        assertTrue(result.contains("Optional<String>"));
    }
}
