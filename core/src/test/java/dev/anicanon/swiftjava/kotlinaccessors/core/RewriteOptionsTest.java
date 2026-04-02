package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RewriteOptionsTest {
    @Test
    void shouldRewriteAllPackagesWhenPrefixesEmpty() {
        RewriteOptions options = new RewriteOptions(List.of(), null);
        assertTrue(options.shouldRewritePackage("com.example.anything"));
    }

    @Test
    void shouldRewriteMatchingPackage() {
        RewriteOptions options = new RewriteOptions(List.of("com.example"), null);
        assertTrue(options.shouldRewritePackage("com.example.model"));
    }

    @Test
    void shouldNotRewriteNonMatchingPackage() {
        RewriteOptions options = new RewriteOptions(List.of("com.example"), null);
        assertFalse(options.shouldRewritePackage("org.other.model"));
    }

    @Test
    void filtersNullPrefixesAtConstruction() {
        RewriteOptions options = new RewriteOptions(Arrays.asList("com.example", null, "org.other"), null);
        assertEquals(2, options.getPackagePrefixes().size());
    }

    @Test
    void handlesNullPrefixList() {
        RewriteOptions options = new RewriteOptions(null, null);
        assertTrue(options.shouldRewritePackage("anything"));
    }
}
