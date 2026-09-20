package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the getter jextract emits for a Swift {@code static let} constant of a
 * primitive or {@code String} type into a {@code public static final} field named
 * after the Swift constant, so Kotlin reads {@code PersonalityTrait.maxSelectionCount}
 * instead of calling {@code getMaxSelectionCount()}.
 *
 * <p>The field is read once, when the class initializes, after the class has loaded its
 * native libraries. Only {@code let} constants are rewritten: a {@code static var} can
 * change between reads, so its getter is left as a call.
 */
public final class StaticConstantRule implements RewriteRule {
    private static final Set<String> VALUE_TYPES = Set.of(
        "boolean", "byte", "char", "short", "int", "long", "float", "double",
        "String", "java.lang.String"
    );

    private static final Pattern CONSTANT = Pattern.compile(
        "(\\* public static let (\\w+): [^\\n]*\\n\\s*\\*\\s*\\}\\n\\s*\\*/\\n)"
            + "(\\s*)public static ([\\w.]+) (?:get|is)(\\w+)\\(\\) \\{\\n"
            + "\\s*return (\\w+)\\.(\\$(?:get|is)\\w+)\\(\\);\\n"
            + "\\s*\\}"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = CONSTANT.matcher(source);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String swiftName = matcher.group(2);
            String javaType = matcher.group(4);
            String replacement = matcher.group(0);
            if (VALUE_TYPES.contains(javaType) && matcher.group(5).equals(capitalized(swiftName))) {
                replacement = matcher.group(1) + matcher.group(3)
                    + "public static final " + javaType + " " + swiftName + " = "
                    + matcher.group(6) + "." + matcher.group(7) + "();";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String capitalized(String name) {
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
