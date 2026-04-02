package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import java.util.List;
import org.junit.jupiter.api.Test;

class AddNullableImportRuleTest {
    @Test
    void insertsImportAfterPackage() {
        RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");
        AddNullableImportRule rule = new AddNullableImportRule(options);

        String input = "package com.example;\nimport java.util.List;\n";
        String result = rule.apply(input);
        assertTrue(result.contains("import org.jetbrains.annotations.Nullable;\n"));
    }

    @Test
    void doesNotDuplicateImport() {
        RewriteOptions options = new RewriteOptions(List.of(), "org.jetbrains.annotations.Nullable");
        AddNullableImportRule rule = new AddNullableImportRule(options);

        String input = "package com.example;\nimport org.jetbrains.annotations.Nullable;\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void noOpWhenAnnotationIsBlank() {
        RewriteOptions options = new RewriteOptions(List.of(), "");
        AddNullableImportRule rule = new AddNullableImportRule(options);

        String input = "package com.example;\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void noOpWhenAnnotationIsNull() {
        RewriteOptions options = new RewriteOptions(List.of(), null);
        AddNullableImportRule rule = new AddNullableImportRule(options);

        String input = "package com.example;\n";
        assertEquals(input, rule.apply(input));
    }
}
