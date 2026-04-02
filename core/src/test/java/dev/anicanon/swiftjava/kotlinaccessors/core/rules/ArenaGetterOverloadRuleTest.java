package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ArenaGetterOverloadRuleTest {
    private final ArenaGetterOverloadRule rule = new ArenaGetterOverloadRule();

    @Test
    void addsNoArgOverload() {
        String input = String.join("\n",
            "    public MyType getValue(SwiftArena swiftArena) {",
            "        return doStuff(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public MyType getValue()"));
        assertTrue(result.contains("return getValue(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"));
    }

    @Test
    void preservesOriginalMethod() {
        String input = String.join("\n",
            "    public MyType getValue(SwiftArena swiftArena) {",
            "        return doStuff(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public MyType getValue(SwiftArena swiftArena) {"));
    }

    @Test
    void doesNotMatchNonGetterMethods() {
        String input = String.join("\n",
            "    public MyType createSomething(SwiftArena swiftArena) {",
            "        return doStuff(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertFalse(result.contains("DEFAULT_SWIFT_JAVA_AUTO_ARENA"));
    }

    @Test
    void handlesArrayReturnType() {
        String input = String.join("\n",
            "    public String[] getNames(SwiftArena swiftArena) {",
            "        return fetchNames(swiftArena);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("public String[] getNames()"));
    }
}
