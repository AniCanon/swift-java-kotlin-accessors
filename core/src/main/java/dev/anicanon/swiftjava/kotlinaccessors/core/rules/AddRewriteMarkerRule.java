package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;

public final class AddRewriteMarkerRule implements RewriteRule {
    private static final String REWRITTEN_MARKER = "// Rewritten by swift-java-kotlin-accessors. Do not edit.";

    @Override
    public String apply(String source) {
        if (source.contains(REWRITTEN_MARKER)) {
            return source;
        }
        int packageEnd = source.indexOf('\n');
        if (packageEnd < 0) {
            return REWRITTEN_MARKER + "\n" + source;
        }
        return source.substring(0, packageEnd + 1) + REWRITTEN_MARKER + "\n" + source.substring(packageEnd + 1);
    }
}
