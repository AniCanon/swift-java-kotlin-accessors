package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StaticTrailingArenaOverloadRuleTest {
    private final StaticTrailingArenaOverloadRule rule = new StaticTrailingArenaOverloadRule();

    @Test
    void addsOverloadWithoutArena() {
        String input = String.join("\n",
            "    public static MyType create(String name, SwiftArena swiftArena) {",
            "        return doStuff(name, swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public static MyType create(String name) {"));
        assertTrue(result.contains("return create(name, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"));
    }

    @Test
    void addsOverloadForArenaOnlyParam() {
        String input = String.join("\n",
            "    public static MyType init(SwiftArena swiftArena) {",
            "        return doStuff(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public static MyType init() {"));
        assertTrue(result.contains("return init(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"));
    }

    @Test
    void skipsGetterMethods() {
        String input = String.join("\n",
            "    public static MyType getInstance(SwiftArena swiftArena) {",
            "        return doStuff(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertFalse(result.contains("DEFAULT_SWIFT_JAVA_AUTO_ARENA"));
    }

    @Test
    void skipsWrapMemoryAddressUnsafe() {
        String input = String.join("\n",
            "    public static MyType wrapMemoryAddressUnsafe(long addr, SwiftArena swiftArena) {",
            "        return doStuff(addr, swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertEquals(1, result.split("public static").length - 1);
    }

    @Test
    void skipsWhenArenaNotTrailing() {
        String input = String.join("\n",
            "    public static MyType create(SwiftArena swiftArena, String name) {",
            "        return doStuff(swiftArena, name);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertFalse(result.contains("DEFAULT_SWIFT_JAVA_AUTO_ARENA"));
    }

    @Test
    void preservesOriginalMethod() {
        String input = String.join("\n",
            "    public static MyType create(String name, SwiftArena swiftArena) {",
            "        return doStuff(name, swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public static MyType create(String name, SwiftArena swiftArena) {"));
    }
}
