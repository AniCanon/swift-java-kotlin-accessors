package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class NormalizeQualifiedOptionalRuleTest {
    private final NormalizeQualifiedOptionalRule rule = new NormalizeQualifiedOptionalRule();

    @Test
    void stripsQualifierFromGenericOptionalReturn() {
        String input = "  public static java.util.Optional<VideoGenerationQuality> init(String rawValue) {\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public static Optional<VideoGenerationQuality> init"));
        assertFalse(result.contains("java.util.Optional"));
    }

    @Test
    void stripsQualifierFromOptionalParameterWithoutCorruptingInnerType() {
        // Regression: a qualified Optional param must normalize to bare Optional so the
        // parameter rule unwraps it cleanly, never leaving java.util.<InnerType>.
        String input = "  public static X init(java.util.Optional<VideoSpokenLanguage> spokenLanguage) {\n";
        String result = rule.apply(input);
        assertTrue(result.contains("Optional<VideoSpokenLanguage> spokenLanguage"));
        assertFalse(result.contains("java.util."));
    }

    @Test
    void stripsQualifierFromPrimitiveOptionals() {
        String input = "java.util.OptionalLong a; java.util.OptionalInt b; java.util.OptionalDouble c;";
        String result = rule.apply(input);
        assertEquals("OptionalLong a; OptionalInt b; OptionalDouble c;", result);
    }

    @Test
    void leavesWildcardImportUntouched() {
        String input = "import java.util.*;\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void leavesExplicitOptionalImportUntouched() {
        String input = "import java.util.Optional;\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void leavesUnqualifiedOptionalUntouched() {
        String input = "  public Optional<String> name() {\n";
        assertEquals(input, rule.apply(input));
    }

    @Test
    void doesNotAffectOtherJavaUtilTypes() {
        String input = "java.util.List<String> items; java.util.concurrent.CompletableFuture<Long> f;";
        assertEquals(input, rule.apply(input));
    }
}
