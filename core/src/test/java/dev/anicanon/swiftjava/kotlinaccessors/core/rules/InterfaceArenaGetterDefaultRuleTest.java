package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InterfaceArenaGetterDefaultRuleTest {
    private final InterfaceArenaGetterDefaultRule rule = new InterfaceArenaGetterDefaultRule();

    private static final String INTERFACE = """
        public interface LightboxItem extends JNISwiftInstance, SwiftDowncastable {
          public URL getImageURL(SwiftArena swiftArena);
          public String getLightboxTitle();
          public LightboxInfoMention[] getLightboxMentions(SwiftArena swiftArena);
        }
        """;

    @Test
    void addsArenaFreeDefaults() {
        String result = rule.apply(INTERFACE);
        assertTrue(result.contains("  public default URL getImageURL() {\n"
            + "    return getImageURL(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);\n  }"));
        assertTrue(result.contains("  public default java.util.List<LightboxInfoMention> getLightboxMentions() {\n"
            + "    return java.util.List.of(getLightboxMentions(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));\n  }"));
        assertFalse(result.contains("default String getLightboxTitle"));
    }

    @Test
    void leavesClassesAlone() {
        String source = INTERFACE.replace("public interface", "public final class");
        assertEquals(source, rule.apply(source));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(INTERFACE);
        assertEquals(once, rule.apply(once));
    }
}
