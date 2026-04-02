package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SourceRewriteUtilsTest {

    @Test
    void findMatchingBraceNestedBraces() {
        String source = "{ { } }";
        assertEquals(6, SourceRewriteUtils.findMatchingBrace(source, 0));
    }

    @Test
    void findMatchingBraceImmediateClose() {
        String source = "{}";
        assertEquals(1, SourceRewriteUtils.findMatchingBrace(source, 0));
    }

    @Test
    void findMatchingBraceUnmatchedThrows() {
        String source = "{ {";
        assertThrows(IllegalStateException.class, () ->
            SourceRewriteUtils.findMatchingBrace(source, 0)
        );
    }

    @Test
    void findMatchingBraceBraceAtEndOfString() {
        String source = "x{y}";
        assertEquals(3, SourceRewriteUtils.findMatchingBrace(source, 1));
    }
}
