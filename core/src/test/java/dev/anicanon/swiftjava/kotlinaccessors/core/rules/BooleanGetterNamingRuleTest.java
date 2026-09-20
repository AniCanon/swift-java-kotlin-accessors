package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BooleanGetterNamingRuleTest {
    private final BooleanGetterNamingRule rule = new BooleanGetterNamingRule();

    private static String getter(String swiftName, String javaName) {
        return """
              // ==== --------------------------------------------------
              // getter:Stage.%s

              /**
               * <p>Downcall to Swift:
               * {@snippet lang=swift :
               * public var %s: Bool
               * }
               */
              public boolean %s() {
                return Stage.$%s(this.$memoryAddress());
              }
              private static native boolean $%s(long selfPointer);
            """.formatted(swiftName, swiftName, javaName, javaName, javaName);
    }

    @Test
    void restoresSwiftNameWhenJextractAddedTheIsPrefix() {
        String result = rule.apply(getter("resumesOutfitSession", "isResumesOutfitSession"));
        assertTrue(result.contains("public boolean getResumesOutfitSession() {"));
        assertTrue(result.contains("return Stage.$isResumesOutfitSession(this.$memoryAddress());"));
        assertTrue(result.contains("private static native boolean $isResumesOutfitSession(long selfPointer);"));
    }

    @Test
    void keepsSwiftNamesThatAlreadyStartWithIs() {
        String input = getter("isFaceSheet", "isFaceSheet");
        assertEquals(input, rule.apply(input));
    }

    @Test
    void keepsWordsThatOnlyLookLikeIs() {
        String result = rule.apply(getter("issued", "isIssued"));
        assertTrue(result.contains("public boolean getIssued() {"));
    }

    @Test
    void handlesSeveralGettersInOneFile() {
        String input = getter("canRetry", "isCanRetry") + getter("isReady", "isReady") + getter("hasAudio", "isHasAudio");
        String result = rule.apply(input);
        assertTrue(result.contains("public boolean getCanRetry() {"));
        assertTrue(result.contains("public boolean isReady() {"));
        assertTrue(result.contains("public boolean getHasAudio() {"));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(getter("canRetry", "isCanRetry"));
        assertEquals(once, rule.apply(once));
    }
}
