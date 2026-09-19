package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds an arena-free {@code default} getter next to each protocol getter that takes a
 * {@code SwiftArena}, so Kotlin reads {@code item.imageURL} on the interface type. Arrays of
 * class types are returned as {@code java.util.List}.
 */
public final class InterfaceArenaGetterDefaultRule implements RewriteRule {
    private static final Pattern INTERFACE = Pattern.compile("(?m)^public interface \\w+[^{]*\\{");
    private static final Pattern GETTER = Pattern.compile(
        "(?m)^(\\s*)public ([\\w.$\\[\\]<>]+) (get\\w+)\\(SwiftArena swiftArena\\);$"
    );

    @Override
    public String apply(String source) {
        Matcher interfaceMatcher = INTERFACE.matcher(source);
        if (!interfaceMatcher.find()) {
            return source;
        }
        Matcher matcher = GETTER.matcher(source);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            String indent = matcher.group(1).replace("\n", "");
            String returnType = matcher.group(2);
            String name = matcher.group(3);
            String call = name + "(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA)";
            String declaration;
            if (SourceRewriteUtils.isReferenceArray(returnType)) {
                String element = returnType.substring(0, returnType.length() - 2);
                declaration = "public default java.util.List<" + element + "> " + name + "() {\n"
                    + indent + "  return java.util.List.of(" + call + ");\n";
            } else {
                declaration = "public default " + returnType + " " + name + "() {\n"
                    + indent + "  return " + call + ";\n";
            }
            String overload = indent + declaration + indent + "}";
            result.append(source, last, matcher.end());
            if (!source.contains(indent + "public default " + (SourceRewriteUtils.isReferenceArray(returnType)
                ? "java.util.List<" + returnType.substring(0, returnType.length() - 2) + ">" : returnType) + " " + name + "() {")) {
                result.append("\n\n").append(overload);
            }
            last = matcher.end();
        }
        result.append(source.substring(last));
        return result.toString();
    }
}
