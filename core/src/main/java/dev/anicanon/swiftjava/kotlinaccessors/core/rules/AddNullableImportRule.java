package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddNullableImportRule implements RewriteRule {
    private static final Pattern PACKAGE_LINE = Pattern.compile("(?m)^package .+;\\n");

    private final RewriteOptions options;

    public AddNullableImportRule(RewriteOptions options) {
        this.options = options;
    }

    @Override
    public String apply(String source) {
        String annotation = options.getNullableAnnotationFqcn();
        if (annotation == null || annotation.isBlank()) {
            return source;
        }
        String importLine = "import " + annotation + ";";
        if (source.contains(importLine)) {
            return source;
        }
        Matcher packageMatcher = PACKAGE_LINE.matcher(source);
        if (!packageMatcher.find()) {
            return source;
        }
        int insertOffset = packageMatcher.end();
        return source.substring(0, insertOffset) + importLine + "\n" + source.substring(insertOffset);
    }
}
