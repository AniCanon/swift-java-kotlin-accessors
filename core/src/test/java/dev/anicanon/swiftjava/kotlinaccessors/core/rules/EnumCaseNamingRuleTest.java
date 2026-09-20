package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EnumCaseNamingRuleTest {
    private final EnumCaseNamingRule rule = new EnumCaseNamingRule();

    private static final String ENUM = """
        public final class Stage implements JNISwiftInstance {
          public enum Discriminator {
            FACESHEETCAPTURE,
            OUTFITREVIEW,
            IMAGE
          }
          public Discriminator getDiscriminator() {
            var raw = SwiftObjects.getRawDiscriminator(this.$memoryAddress(), this.$typeMetadataAddress());
            return Discriminator.values()[raw];
          }

          public sealed interface Case {
            record FaceSheetCapture() implements Case {}
            record OutfitReview() implements Case {}
            record Image() implements Case {}
          }

          public Case getCase() {
            return switch (this.getDiscriminator()) {
              case FACESHEETCAPTURE -> new Case.FaceSheetCapture();
              case OUTFITREVIEW -> new Case.OutfitReview();
              case IMAGE -> new Case.Image();
            };
          }

          public static Stage faceSheetCapture() {
            return null;
          }
        }
        """;

    @Test
    void renamesConstantsFromCaseRecordNames() {
        String result = rule.apply(ENUM);
        assertTrue(result.contains("FACE_SHEET_CAPTURE,\n    OUTFIT_REVIEW,\n    IMAGE\n"));
        assertTrue(result.contains("case FACE_SHEET_CAPTURE -> new Case.FaceSheetCapture();"));
        assertTrue(result.contains("case OUTFIT_REVIEW -> new Case.OutfitReview();"));
        assertFalse(result.contains("FACESHEETCAPTURE"));
    }

    @Test
    void leavesCodeOutsideTheEnumBlockUntouched() {
        String result = rule.apply(ENUM);
        assertTrue(result.contains("public static Stage faceSheetCapture()"));
        assertTrue(result.contains("return Discriminator.values()[raw];"));
    }

    @Test
    void renamesQualifiedReferencesOutsideTheBlock() {
        String withAccessor = ENUM.replace(
            "  public static Stage faceSheetCapture() {",
            "  public Case.OutfitReview getAsOutfitReview() {\n"
                + "    if (getDiscriminator() != Discriminator.OUTFITREVIEW) {\n"
                + "      return null;\n"
                + "    }\n"
                + "    return new Case.OutfitReview();\n"
                + "  }\n\n"
                + "  public static Stage faceSheetCapture() {"
        );
        String result = rule.apply(withAccessor);
        assertTrue(result.contains("if (getDiscriminator() != Discriminator.OUTFIT_REVIEW) {"));
        assertFalse(result.contains("OUTFITREVIEW"));
    }

    @Test
    void isIdempotent() {
        String once = rule.apply(ENUM);
        assertEquals(once, rule.apply(once));
    }

    @Test
    void skipsWhenCaseCountDoesNotMatch() {
        String mismatched = ENUM.replace("    record Image() implements Case {}\n", "");
        assertEquals(mismatched, rule.apply(mismatched));
    }

    @Test
    void renamesEachEnumInAFileIndependently() {
        String twoEnums = ENUM + ENUM.replace("Stage", "Other").replace("FACESHEETCAPTURE", "HIGHANGLE").replace("FaceSheetCapture", "HighAngle");
        String result = rule.apply(twoEnums);
        assertTrue(result.contains("FACE_SHEET_CAPTURE"));
        assertTrue(result.contains("HIGH_ANGLE"));
    }

    @Test
    void convertsAcronymsAndDigits() {
        assertEquals("URL_PATH", EnumCaseNamingRule.upperSnake("URLPath"));
        assertEquals("SIXTEEN_BY_NINE", EnumCaseNamingRule.upperSnake("SixteenByNine"));
        assertEquals("RATIO16X9", EnumCaseNamingRule.upperSnake("Ratio16x9"));
    }
}
