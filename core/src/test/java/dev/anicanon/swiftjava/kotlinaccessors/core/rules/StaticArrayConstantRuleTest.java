package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StaticArrayConstantRuleTest {
    private final StaticArrayConstantRule rule = new StaticArrayConstantRule();

    private static final String GETTER = """
          /**
           * Downcall to Swift:
           * {@snippet lang=swift :
           * public static let all: [TraitGroup]
           * }
           */
          public static TraitGroup[] getAll(SwiftArena swiftArena) {
            return Arrays.stream(TraitGroup.$getAll()).mapToObj((pointer) -> {
              return TraitGroup.wrapMemoryAddressUnsafe(pointer, swiftArena);
            }).toArray(TraitGroup[]::new);
          } // printJavaBindingWrapperMethod
          private static native long[] $getAll();
        """;

    @Test
    void addsListConstantAfterTheGetter() {
        String result = rule.apply(GETTER);
        assertTrue(result.contains("} // printJavaBindingWrapperMethod\n\n"
            + "  public static final java.util.List<TraitGroup> all = java.util.List.of(getAll(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));\n"));
        assertTrue(result.contains("public static TraitGroup[] getAll(SwiftArena swiftArena) {"));
    }

    @Test
    void leavesComputedVarsAlone() {
        String source = GETTER.replace("public static let all", "public static var all");
        assertEquals(source, rule.apply(source));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(GETTER);
        assertEquals(once, rule.apply(once));
    }
}
