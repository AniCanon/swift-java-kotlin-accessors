package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OptionalParameterRewriteRuleTest {
    private final OptionalParameterRewriteRule rule = new OptionalParameterRewriteRule();

    @Test
    void rewritesOptionalParameterType() {
        String input = "    public void update(Optional<String> name) {\n    }\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public void update(String name) {"));
    }

    @Test
    void rewritesOptionalLongParameter() {
        String input = "    public void setCount(OptionalLong count) {\n    }\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public void setCount(Long count) {"));
    }

    @Test
    void rewritesOptionalIntParameter() {
        String input = "    public void setIndex(OptionalInt index) {\n    }\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public void setIndex(Integer index) {"));
    }

    @Test
    void rewritesOptionalDoubleParameter() {
        String input = "    public void setScore(OptionalDouble score) {\n    }\n";
        String result = rule.apply(input);
        assertTrue(result.contains("public void setScore(Double score) {"));
    }

    @Test
    void rewritesIsPresentToNullCheck() {
        String input = String.join("\n",
            "    public void update(Optional<String> name) {",
            "        if (name.isPresent()) {",
            "            use(name);",
            "        }",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("name != null"));
        assertFalse(result.contains("isPresent"));
    }

    @Test
    void rewritesOrElseNull() {
        String input = String.join("\n",
            "    public void update(Optional<String> name) {",
            "        String val = name.orElse(null);",
            "    }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("String val = name;"));
    }

    @Test
    void rewritesOrElsePrimitiveLong() {
        String input = "        long val = count.orElse(0L);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("((count != null) ? count : 0L)"));
    }

    @Test
    void rewritesOrElsePrimitiveInt() {
        String input = "        int val = count.orElse(0);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("((count != null) ? count : 0)"));
    }

    @Test
    void rewritesOrElsePrimitiveDouble() {
        String input = "        double val = score.orElse(0.0);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("((score != null) ? score : 0.0)"));
    }

    @Test
    void rewritesOrElseBoolean() {
        String input = "        boolean val = flag.orElse(false);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("((flag != null) ? flag : false)"));
    }

    @Test
    void rewritesMapMemoryAddress() {
        String input = "        long addr = obj.map(MyType::$memoryAddress).orElse(0L);\n";
        String result = rule.apply(input);
        assertTrue(result.contains("(obj != null) ? obj.$memoryAddress() : 0L"));
    }

    @Test
    void rewritesMultipleParameters() {
        String input = "    public void update(Optional<String> name, OptionalInt count) {\n    }\n";
        String result = rule.apply(input);
        assertTrue(result.contains("String name, Integer count"));
    }
}
