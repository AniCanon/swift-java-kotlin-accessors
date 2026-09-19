package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ArrayListRuleTest {
    private final ArrayListRule rule = new ArrayListRule();

    private static final String ARENA_GETTER = String.join("\n",
        "  public Project[] getProjects(SwiftArena swiftArena) {",
        "    return Arrays.stream(ProjectListOverview.$getProjects(this.$memoryAddress())).mapToObj((pointer) -> {",
        "      return Project.wrapMemoryAddressUnsafe(pointer, swiftArena);",
        "    } // render(_:_:) @ JExtractSwiftLib/JNISwift2JavaGenerator+JavaTranslation.swift:2072",
        "    ).toArray(Project[]::new);",
        "  } // printJavaBindingWrapperMethod(_:_:importedFunc:skipMethodBody:) @ JExtractSwiftLib/JNISwift2JavaGenerator+JavaBindingsPrinting.swift:950",
        ""
    );

    @Test
    void rewritesDelegatingGetterReturningObjectArray() {
        String input = ARENA_GETTER + String.join("\n",
            "  public Project[] getProjects() {",
            "    return getProjects(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            "  private static native long[] $getProjects(long selfPointer);",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(String.join("\n",
            "  public java.util.List<Project> getProjects() {",
            "    return java.util.List.of(getProjects(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));",
            "  }"
        )));
        assertTrue(result.contains("  public Project[] getProjects(SwiftArena swiftArena) {"));
        assertTrue(result.contains("private static native long[] $getProjects(long selfPointer);"));
    }

    @Test
    void rewritesStringArrayGetterAndSetterPair() {
        String input = String.join("\n",
            "  public java.lang.String[] getDistinguishingFeatures() {",
            "    return AppearanceAnalysis.$getDistinguishingFeatures(this.$memoryAddress());",
            "  }",
            "  public void setDistinguishingFeatures(java.lang.String[] newValue) {",
            "    AppearanceAnalysis.$setDistinguishingFeatures(Objects.requireNonNull(newValue, \"newValue must not be null\"), this.$memoryAddress());",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(String.join("\n",
            "  public java.util.List<java.lang.String> getDistinguishingFeatures() {",
            "    return java.util.List.of(AppearanceAnalysis.$getDistinguishingFeatures(this.$memoryAddress()));",
            "  }"
        )));
        assertTrue(result.contains(String.join("\n",
            "  public void setDistinguishingFeatures(java.util.List<java.lang.String> newValue) {",
            "    AppearanceAnalysis.$setDistinguishingFeatures(Objects.requireNonNull(newValue.toArray(new java.lang.String[0]), "
                + "\"newValue must not be null\"), this.$memoryAddress());",
            "  }"
        )));
    }

    @Test
    void rewritesStaticFunctionWithArrayParameterAndReturn() {
        String input = String.join("\n",
            "  public static ProjectListItem[] topPublicProjects(ProjectListItem[] projects, SwiftArena swiftArena) {",
            "    return Arrays.stream(ProjectDiscovery.$topPublicProjects(Arrays.stream(Objects.requireNonNull(projects, "
                + "\"projects must not be null\")).mapToLong(ProjectListItem::$memoryAddress).toArray())).mapToObj((pointer) -> {",
            "      return ProjectListItem.wrapMemoryAddressUnsafe(pointer, swiftArena);",
            "    }",
            "    ).toArray(ProjectListItem[]::new);",
            "  }",
            "",
            "  public static ProjectListItem[] topPublicProjects(ProjectListItem[] projects) {",
            "    return topPublicProjects(projects, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(String.join("\n",
            "  public static java.util.List<ProjectListItem> topPublicProjects(java.util.List<ProjectListItem> projects) {",
            "    return java.util.List.of(topPublicProjects(projects.toArray(new ProjectListItem[0]), "
                + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));",
            "  }"
        )));
        assertTrue(result.contains("Objects.requireNonNull(projects, \"projects must not be null\")"));
        assertTrue(result.contains("public static ProjectListItem[] topPublicProjects(ProjectListItem[] projects, SwiftArena swiftArena) {"));
    }

    @Test
    void leavesCallsToSameNamedMethodAlone() {
        String input = String.join("\n",
            "  public static ProjectListItem[] followedProjects(ProjectListItem[] followedProjects, ProjectListItem project) {",
            "    return followedProjects(followedProjects, project, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(
            "    return java.util.List.of(followedProjects(followedProjects.toArray(new ProjectListItem[0]), project, "
                + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));"
        ));
    }

    @Test
    void rewritesInitWithArrayParameter() {
        String input = String.join("\n",
            "  public static CharacterAnalysisResult init(String suggestedName, AppearanceAnalysis appearance, "
                + "java.lang.String[] suggestedTraits, String suggestedPersonality) {",
            "    return init(suggestedName, appearance, suggestedTraits, suggestedPersonality, "
                + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(
            "  public static CharacterAnalysisResult init(String suggestedName, AppearanceAnalysis appearance, "
                + "java.util.List<java.lang.String> suggestedTraits, String suggestedPersonality) {"
        ));
        assertTrue(result.contains(
            "    return init(suggestedName, appearance, suggestedTraits.toArray(new java.lang.String[0]), suggestedPersonality, "
                + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"
        ));
    }

    @Test
    void keepsNestedElementTypesQualified() {
        String input = String.join("\n",
            "  public GenerateSceneRequest.CharacterRef[] getCharacters() {",
            "    return getCharacters(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            "  public void setCharacters(GenerateSceneRequest.CharacterRef[] newValue) {",
            "    GenerateSceneRequest.$setCharacters(Arrays.stream(Objects.requireNonNull(newValue, \"newValue must not be null\"))"
                + ".mapToLong(GenerateSceneRequest.CharacterRef::$memoryAddress).toArray(), this.$memoryAddress());",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("  public java.util.List<GenerateSceneRequest.CharacterRef> getCharacters() {"));
        assertTrue(result.contains("  public void setCharacters(java.util.List<GenerateSceneRequest.CharacterRef> newValue) {"));
        assertTrue(result.contains(
            "Objects.requireNonNull(newValue.toArray(new GenerateSceneRequest.CharacterRef[0]), \"newValue must not be null\")"
        ));
    }

    @Test
    void wrapsNullableReturnNullSafely() {
        String input = String.join("\n",
            "  public java.lang.String[] getHighlights() {",
            "    byte[] result$_discriminator$ = new byte[1];",
            "    java.lang.String[] result$ = FragmentPlanMeta.$getHighlights(this.$memoryAddress(), result$_discriminator$);",
            "    return (result$_discriminator$[0] == 1) ? result$ : null;",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(String.join("\n",
            "  public java.util.List<java.lang.String> getHighlights() {",
            "    byte[] result$_discriminator$ = new byte[1];",
            "    java.lang.String[] result$ = FragmentPlanMeta.$getHighlights(this.$memoryAddress(), result$_discriminator$);",
            "    java.lang.String[] array$ = (result$_discriminator$[0] == 1) ? result$ : null;",
            "    return array$ == null ? null : java.util.List.of(array$);",
            "  }"
        )));
    }

    @Test
    void treatsAnnotatedReturnAsNullable() {
        String input = String.join("\n",
            "  @Nullable",
            "  public Tag[] getTags() {",
            "    return getTags(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("    Tag[] array$ = getTags(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);"));
        assertTrue(result.contains("    return array$ == null ? null : java.util.List.of(array$);"));
    }

    @Test
    void treatsOptionalSwiftArrayReturnAsNullable() {
        String input = String.join("\n",
            "  // ==== --------------------------------------------------",
            "  // getter:Plan.tags",
            "  /**",
            "   * Downcall to Swift:",
            "   * {@snippet lang=swift :",
            "   * public var tags: [Tag]?",
            "   * }",
            "   */",
            "  public Tag[] getTags() {",
            "    return getTags(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains("    return array$ == null ? null : java.util.List.of(array$);"));
    }

    @Test
    void convertsNullableParameterNullSafely() {
        String input = String.join("\n",
            "  public void setTags(java.lang.String[] newValue) {",
            "    Plan.$setTags((byte) (newValue != null ? 1 : 0), newValue, this.$memoryAddress());",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(String.join("\n",
            "  public void setTags(java.util.List<java.lang.String> newValue) {",
            "    Plan.$setTags((byte) (newValue != null ? 1 : 0), "
                + "(newValue == null ? null : newValue.toArray(new java.lang.String[0])), this.$memoryAddress());",
            "  }"
        )));
    }

    @Test
    void leavesPrimitiveArraysUntouched() {
        String input = String.join("\n",
            "  public static Data init(@Unsigned byte[] bytes) {",
            "    return init(bytes, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            "  public byte[] toByteArray() {",
            "    return Data.$toByteArray(this.$memoryAddress());",
            "  }",
            "  public long[] getIds() {",
            "    return Model.$getIds(this.$memoryAddress());",
            "  }",
            ""
        );
        assertEquals(input, rule.apply(input));
    }

    @Test
    void leavesArenaOverloadsUntouched() {
        assertEquals(ARENA_GETTER, rule.apply(ARENA_GETTER));
    }

    @Test
    void leavesAsyncMethodsUntouched() {
        String input = String.join("\n",
            "  public java.util.concurrent.CompletableFuture<java.lang.String[]> listReferenceImages(java.lang.String projectId) {",
            "    return listReferenceImagesAsync(projectId);",
            "  }",
            "  public java.util.concurrent.CompletableFuture<java.lang.Void> addReferenceImages(java.lang.String[] assetIds) {",
            "    return addReferenceImagesAsync(Objects.requireNonNull(assetIds, \"assetIds must not be null\"));",
            "  }",
            ""
        );
        assertEquals(input, rule.apply(input));
    }

    @Test
    void leavesInterfaceDeclarationsUntouched() {
        String input = String.join("\n",
            "  public LightboxInfoMention[] getLightboxMentions(SwiftArena swiftArena);",
            "  public Tag[] tags();",
            ""
        );
        assertEquals(input, rule.apply(input));
    }

    @Test
    void keepsThrowsClause() {
        String input = String.join("\n",
            "  public static MangaReaderPageSurface init(java.lang.String pageId, MangaReaderPageSurface.VideoPanel[] videoPanels) "
                + "throws SwiftIntegerOverflowException {",
            "    return init(pageId, videoPanels, SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            ""
        );
        String result = rule.apply(input);
        assertTrue(result.contains(
            "  public static MangaReaderPageSurface init(java.lang.String pageId, "
                + "java.util.List<MangaReaderPageSurface.VideoPanel> videoPanels) throws SwiftIntegerOverflowException {"
        ));
        assertTrue(result.contains("init(pageId, videoPanels.toArray(new MangaReaderPageSurface.VideoPanel[0]), "));
    }

    @Test
    void leavesMethodWithSeveralTopLevelReturnsUntouched() {
        String input = String.join("\n",
            "  public Tag[] getTags() {",
            "    if (flag) return first();",
            "    return second();",
            "  }",
            ""
        );
        assertEquals(input, rule.apply(input));
    }

    @Test
    void isIdempotent() {
        String input = ARENA_GETTER + String.join("\n",
            "  public Project[] getProjects() {",
            "    return getProjects(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);",
            "  }",
            "  public java.lang.String[] getHighlights() {",
            "    return (flag == 1) ? result$ : null;",
            "  }",
            "  public void setCharacters(GenerateSceneRequest.CharacterRef[] newValue) {",
            "    GenerateSceneRequest.$setCharacters(Objects.requireNonNull(newValue, \"newValue must not be null\"));",
            "  }",
            ""
        );
        String once = rule.apply(input);
        assertNotEquals(input, once);
        assertEquals(once, rule.apply(once));
    }
}
