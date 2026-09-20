package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InstanceTrailingArenaOverloadRuleTest {
    private final InstanceTrailingArenaOverloadRule rule = new InstanceTrailingArenaOverloadRule();

    private static final String METHOD = """
          public ProjectListItem updatingFollowState(ProjectFollowState followState, SwiftArena swiftArena) {
            return ProjectListItem.wrapMemoryAddressUnsafe(ProjectListItem.$updatingFollowState(followState.$memoryAddress(), this.$memoryAddress()), swiftArena);
          } // printJavaBindingWrapperMethod
        """;

    @Test
    void addsArenaFreeOverload() {
        String result = rule.apply(METHOD);
        assertTrue(result.contains("  public ProjectListItem updatingFollowState(ProjectFollowState followState) {\n"
            + "    return updatingFollowState(followState, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);\n"
            + "  }\n"));
    }

    @Test
    void carriesThrowsClause() {
        String source = METHOD.replace("SwiftArena swiftArena) {", "SwiftArena swiftArena) throws SwiftIntegerOverflowException {");
        assertTrue(rule.apply(source).contains("updatingFollowState(ProjectFollowState followState) throws SwiftIntegerOverflowException {"));
    }

    @Test
    void leavesStaticsGettersAndFuturesAlone() {
        String statics = METHOD.replace("public ProjectListItem", "public static ProjectListItem");
        String getter = METHOD.replace("updatingFollowState(", "getThing(");
        String future = METHOD.replace("public ProjectListItem updatingFollowState", "public java.util.concurrent.CompletableFuture<ProjectListItem> fetch");
        assertEquals(statics, rule.apply(statics));
        assertEquals(getter, rule.apply(getter));
        assertEquals(future, rule.apply(future));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(METHOD);
        assertEquals(once, rule.apply(once));
    }
}
