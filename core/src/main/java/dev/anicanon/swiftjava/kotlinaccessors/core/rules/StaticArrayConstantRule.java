package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds a {@code public static final java.util.List} field for a Swift {@code static let}
 * array constant, next to the arena getter jextract emits for it, so Kotlin reads
 * {@code TraitGroup.all}. The elements are registered with the default auto arena when the
 * class initializes, after it has loaded its native libraries.
 */
public final class StaticArrayConstantRule implements RewriteRule {
    private static final Pattern CONSTANT = Pattern.compile(
        "\\* public static let (\\w+): \\[[^\\n]*\\]\\n\\s*\\*\\s*\\}\\n\\s*\\*/\\n"
            + "(\\s*)public static ([\\w.$]+)\\[\\] get(\\w+)\\(SwiftArena swiftArena\\) \\{"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = CONSTANT.matcher(source);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            String swiftName = matcher.group(1);
            String indent = matcher.group(2).replace("\n", "");
            String field = "public static final java.util.List<" + matcher.group(3) + "> " + swiftName
                + " = java.util.List.of(get" + matcher.group(4) + "(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA));";
            int openBrace = source.indexOf('{', matcher.end() - 1);
            int methodEnd = SourceRewriteUtils.endOfLine(source, SourceRewriteUtils.findMatchingBrace(source, openBrace));
            result.append(source, last, methodEnd);
            if (matcher.group(4).equals(capitalized(swiftName)) && !source.contains(field)) {
                result.append("\n").append(indent).append(field).append("\n");
            }
            last = methodEnd;
        }
        result.append(source.substring(last));
        return result.toString();
    }

    private static String capitalized(String name) {
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
