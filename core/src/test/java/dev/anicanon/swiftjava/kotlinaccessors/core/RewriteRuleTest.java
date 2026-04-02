package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RewriteRuleTest {
    @Test
    void untilStableAppliesRepeatedly() {
        RewriteRule rule = (source -> source.length() < 3 ? source + "x" : source);
        RewriteRule stable = rule.untilStable();
        assertEquals("xxx", stable.apply("x"));
    }

    @Test
    void untilStableReturnsImmediatelyWhenNoChange() {
        RewriteRule identity = source -> source;
        RewriteRule stable = identity.untilStable();
        assertEquals("hello", stable.apply("hello"));
    }

    @Test
    void untilStableThrowsOnNonConvergingRule() {
        RewriteRule oscillating = source -> source.equals("a") ? "b" : "a";
        RewriteRule stable = oscillating.untilStable();
        assertThrows(IllegalStateException.class, () -> stable.apply("a"));
    }
}
