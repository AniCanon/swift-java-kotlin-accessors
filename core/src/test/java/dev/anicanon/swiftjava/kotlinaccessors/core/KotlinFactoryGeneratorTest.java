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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
        assertNull(kotlin);
    }

    @Test
    void returnsNullWhenNoClass() {
        String java = "package com.example;\n\ninterface Foo {}\n";
        assertNull(generator.generate(java, java));
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
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

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.startsWith("// Generated by swift-java-kotlin-accessors. Do not edit."));
    }

    @Test
    void deduplicatesArenaAndNonArenaOverloads() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, SwiftArena swiftArena) {",
            "        return new Model();",
            "    }",
            "    public static Model init(String name) {",
            "        return init(name, DEFAULT_ARENA);",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        // Should produce exactly one factory, not two
        int count = kotlin.split("fun Model\\(").length - 1;
        assertEquals(1, count, "Should deduplicate arena/non-arena overloads");
    }

    @Test
    void mapsFullyQualifiedJavaLangTypes() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Item {",
            "    public static Item init(java.lang.String name, java.lang.String desc) {",
            "        return new Item();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Item(name: String, desc: String): Item ="));
        assertFalse(kotlin.contains("java.lang.String"));
    }

    @Test
    void mapsArrayTypes() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Container {",
            "    public static Container init(Item[] items, java.lang.String[] names) {",
            "        return new Container();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("items: Array<Item>"));
        assertTrue(kotlin.contains("names: Array<String>"));
        assertFalse(kotlin.contains("[]"));
    }

    @Test
    void handlesOptionalParameters() {
        // Original source has Optional<String>
        String original = String.join("\n",
            "package com.example;",
            "",
            "public class Route {",
            "    public static Route init(String projectId, String characterId, Optional<String> outfitId, Stage stage, SwiftArena swiftArena) {",
            "        return new Route();",
            "    }",
            "}",
            ""
        );
        // Rewritten source has Optional unwrapped to String
        String rewritten = String.join("\n",
            "package com.example;",
            "",
            "public class Route {",
            "    public static Route init(String projectId, String characterId, String outfitId, Stage stage, SwiftArena swiftArena) {",
            "        return new Route();",
            "    }",
            "    public static Route init(String projectId, String characterId, String outfitId, Stage stage) {",
            "        return init(projectId, characterId, outfitId, stage, DEFAULT_ARENA);",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(rewritten, original);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Route(projectId: String, characterId: String, outfitId: String?, stage: Stage): Route ="));
        assertTrue(kotlin.contains("    Route.`init`(projectId, characterId, outfitId, stage)"));
    }

    @Test
    void handlesMultipleOptionalParameters() {
        String original = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, Optional<String> description, Optional<Integer> priority) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );
        String rewritten = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, String description, Integer priority) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(rewritten, original);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Model(name: String, description: String?, priority: Int?): Model ="));
        assertTrue(kotlin.contains("    Model.`init`(name, description, priority)"));
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

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun FinalModel(id: Long): FinalModel ="));
    }

    @Test
    void domainCharacterTypeIsNotMappedToChar() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Scene {",
            "    public static Scene init(Character protagonist) {",
            "        return new Scene();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("protagonist: Character"), "Domain 'Character' should not become 'Char'");
    }

    @Test
    void fqnJavaLangCharacterMapsToChar() {
        String java = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(java.lang.Character letter) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(java, java);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("letter: Char"), "java.lang.Character should become Char");
    }

    @Test
    void stripsQualifiedAnnotationsFromParams() {
        String original = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );
        String rewritten = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(@org.jetbrains.annotations.Nullable String name) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(rewritten, original);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Model(name: String): Model ="));
        assertFalse(kotlin.contains("@org.jetbrains"));
    }

    @Test
    void optionalScopedPerMethodNotGlobal() {
        // Method A has Optional<String> description, method B has plain String description
        String original = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, Optional<String> description) {",
            "        return new Model();",
            "    }",
            "    public static Model init(String id, String description, int count) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );
        String rewritten = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, String description) {",
            "        return new Model();",
            "    }",
            "    public static Model init(String id, String description, int count) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(rewritten, original);
        assertNotNull(kotlin);
        // Method A: description should be nullable
        assertTrue(kotlin.contains("fun Model(name: String, description: String?): Model ="));
        // Method B: description should NOT be nullable
        assertTrue(kotlin.contains("fun Model(id: String, description: String, count: Int): Model ="));
    }

    @Test
    void handlesPrimitiveOptionalParameters() {
        String original = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, OptionalLong timestamp, OptionalInt count) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );
        String rewritten = String.join("\n",
            "package com.example;",
            "",
            "public class Model {",
            "    public static Model init(String name, Long timestamp, Integer count) {",
            "        return new Model();",
            "    }",
            "}",
            ""
        );

        String kotlin = generator.generate(rewritten, original);
        assertNotNull(kotlin);
        assertTrue(kotlin.contains("fun Model(name: String, timestamp: Long?, count: Int?): Model ="));
    }
}
