package dev.anicanon.swiftjava.kotlinaccessors.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KotlinFactoryGeneratorTest {
    private final KotlinFactoryGenerator generator = new KotlinFactoryGenerator();

    @Test
    void generatesFactoryForSimpleInit() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class ProjectList {",
            "    public static ProjectList init(String name) {",
            "        return new ProjectList();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("package com.example"));
        assertTrue(kotlin.contains("fun ProjectList(name: String): ProjectList ="));
        assertTrue(kotlin.contains("    ProjectList.`init`(name)"));
    }

    @Test
    void generatesFactoryWithMultipleParameters() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Widget {",
            "    public static Widget init(String name, int count, boolean active) {",
            "        return new Widget();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Widget(name: String, count: Int, active: Boolean): Widget ="));
        assertTrue(kotlin.contains("    Widget.`init`(name, count, active)"));
    }

    @Test
    void stripsTrailingArenaParameter() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, SwiftArena swiftArena) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Model(name: String): Model ="));
        assertTrue(kotlin.contains("    Model.`init`(name)"));
        assertFalse(kotlin.contains("SwiftArena"));
    }

    @Test
    void skipsArenaOnlyInit() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(SwiftArena swiftArena) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNull(kotlin);
    }

    @Test
    void returnsNullWhenNoInitMethods() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public String getName() { return name; }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNull(kotlin);
    }

    @Test
    void returnsNullWhenNoClass() {
        String java = "package com.example;\n\ninterface Foo {}\n";
        assertNull(generator.generate(java));
    }

    @Test
    void handlesMultipleInitOverloads() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Item {",
            "    public static Item init(String name) {",
            "        return new Item();",
            "    }",
            "    public static Item init(String name, int priority) {",
            "        return new Item();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Item(name: String): Item ="));
        assertTrue(kotlin.contains("fun Item(name: String, priority: Int): Item ="));
    }

    @Test
    void skipsInitWithWrongReturnType() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static OtherType init(String name) {",
            "        return new OtherType();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNull(kotlin);
    }

    @Test
    void handlesNoArgInit() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Thing {",
            "    public static Thing init() {",
            "        return new Thing();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Thing(): Thing ="));
        assertTrue(kotlin.contains("    Thing.`init`()"));
    }

    @Test
    void includesGeneratedComment() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Foo {",
            "    public static Foo init(String x) {",
            "        return new Foo();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.startsWith("// Generated by swift-java-kotlin-accessors. Do not edit."));
    }

    @Test
    void handlesFinalClass() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public final class FinalModel {",
            "    public static FinalModel init(long id) {",
            "        return new FinalModel();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun FinalModel(id: Long): FinalModel ="));
    }
}
