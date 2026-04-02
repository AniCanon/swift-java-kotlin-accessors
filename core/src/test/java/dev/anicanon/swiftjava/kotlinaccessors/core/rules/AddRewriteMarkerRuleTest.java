package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AddRewriteMarkerRuleTest {
    private final AddRewriteMarkerRule rule = new AddRewriteMarkerRule();

    @Test
    void insertsMarkerAfterPackageLine() {
        String input = "package com.example;\nimport java.util.List;\n";
        String result = rule.apply(input);
        assertEquals(
            "package com.example;\n// Rewritten by swift-java-kotlin-accessors. Do not edit.\nimport java.util.List;\n",
            result
        );
    }

    @Test
    void doesNotDuplicateMarker() {
        String input = "package com.example;\n// Rewritten by swift-java-kotlin-accessors. Do not edit.\nimport java.util.List;\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void handlesSourceWithoutNewline() {
        String input = "package com.example;";
        String result = rule.apply(input);
        assertEquals("// Rewritten by swift-java-kotlin-accessors. Do not edit.\npackage com.example;", result);
    }
}
