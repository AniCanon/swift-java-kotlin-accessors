package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StaticConstantRuleTest {
    private final StaticConstantRule rule = new StaticConstantRule();

    private static String getter(String swiftDecl, String javaType, String javaName) {
        return """
              // ==== --------------------------------------------------
              // getter:Trait.NAME

              /**
               * Downcall to Swift:
               * {@snippet lang=swift :
               * DECL
               * }
               */
              public static TYPE JAVA() {
                return Trait.$JAVA();
              } // printJavaBindingWrapperMethod
              private static native TYPE $JAVA();
            """
            .replace("DECL", swiftDecl)
            .replace("TYPE", javaType)
            .replace("JAVA", javaName)
            .replace("NAME", "x");
    }

    @Test
    void turnsPrimitiveLetConstantIntoField() {
        String result = rule.apply(getter("public static let maxSelectionCount: Int", "long", "getMaxSelectionCount"));
        assertTrue(result.contains("public static final long maxSelectionCount = Trait.$getMaxSelectionCount();"));
        assertFalse(result.contains("public static long getMaxSelectionCount()"));
        assertTrue(result.contains("private static native long $getMaxSelectionCount();"));
        assertTrue(result.contains("* public static let maxSelectionCount: Int\n"));
    }

    @Test
    void turnsBooleanAndStringConstantsIntoFields() {
        assertTrue(rule.apply(getter("public static let isEnabled: Bool", "boolean", "isIsEnabled"))
            .contains("public static final boolean isEnabled = Trait.$isIsEnabled();"));
        assertTrue(rule.apply(getter("public static let label: String", "java.lang.String", "getLabel"))
            .contains("public static final java.lang.String label = Trait.$getLabel();"));
    }

    @Test
    void leavesComputedStaticVarsAsCalls() {
        String source = getter("public static var maximumCount: Int", "long", "getMaximumCount");
        assertEquals(source, rule.apply(source));
    }

    @Test
    void leavesObjectConstantsAsCalls() {
        String source = getter("public static let preview: Profile", "Profile", "getPreview");
        assertEquals(source, rule.apply(source));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(getter("public static let maxSelectionCount: Int", "long", "getMaxSelectionCount"));
        assertEquals(once, rule.apply(once));
    }
}
