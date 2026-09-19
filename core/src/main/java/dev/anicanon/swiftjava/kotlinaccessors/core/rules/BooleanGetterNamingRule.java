package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Restores the Swift name of {@code Bool} properties.
 *
 * <p>jextract prefixes every {@code Bool} getter with {@code is}, so
 * {@code var canUseAuthoringTools: Bool} becomes {@code isCanUseAuthoringTools()}
 * and Kotlin reads it as {@code isCanUseAuthoringTools}. When the Swift name does
 * not itself start with {@code is}, the public wrapper is renamed to
 * {@code getCanUseAuthoringTools()}, which Kotlin reads as {@code canUseAuthoringTools}.
 * The private native method keeps its generated name. The Swift name comes from the
 * {@code // getter:Owner.name} marker jextract prints above each getter.
 */
public final class BooleanGetterNamingRule implements RewriteRule {
    private static final Pattern GETTER = Pattern.compile(
        "(// getter:[\\w.]+\\.(\\w+)\\n(?:(?!// getter:)[\\s\\S])*?public boolean )is(\\w+)\\(\\)"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = GETTER.matcher(source);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String swiftName = matcher.group(2);
            String javaSuffix = matcher.group(3);
            String replacement = matcher.group(0);
            if (!startsWithIs(swiftName) && javaSuffix.equals(capitalized(swiftName))) {
                replacement = matcher.group(1) + "get" + javaSuffix + "()";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean startsWithIs(String name) {
        return name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2));
    }

    private static String capitalized(String name) {
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
