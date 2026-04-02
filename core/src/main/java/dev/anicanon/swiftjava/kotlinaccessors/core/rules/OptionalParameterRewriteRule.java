package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptionalParameterRewriteRule implements RewriteRule {
    private static final Pattern METHOD_OR_INTERFACE_SIGNATURE = Pattern.compile(
        "(?m)^(\\s*(?:public|private|protected).+?\\()([^)]*)(\\)\\s*(?:throws\\s+[^\\{;]+\\s*)?(?:\\{|;))$"
    );
    private static final Pattern OPTIONAL_PARAMETER = Pattern.compile(
        "\\bOptional<([\\w.$\\[\\]<>]+)>\\s+(\\w+)\\b"
    );
    private static final Pattern OPTIONAL_LONG_PARAMETER = Pattern.compile(
        "\\bOptionalLong\\s+(\\w+)\\b"
    );
    private static final Pattern OPTIONAL_INT_PARAMETER = Pattern.compile(
        "\\bOptionalInt\\s+(\\w+)\\b"
    );
    private static final Pattern OPTIONAL_DOUBLE_PARAMETER = Pattern.compile(
        "\\bOptionalDouble\\s+(\\w+)\\b"
    );
    private static final Pattern OPTIONAL_IS_PRESENT = Pattern.compile(
        "\\b(\\w+)\\.isPresent\\(\\)"
    );
    private static final Pattern OPTIONAL_OR_ELSE_NULL = Pattern.compile(
        "\\b(\\w+)\\.orElse\\(null\\)"
    );
    private static final Pattern OPTIONAL_OR_ELSE_LONG = Pattern.compile(
        "\\b(\\w+)\\.orElse\\(0L\\)"
    );
    private static final Pattern OPTIONAL_OR_ELSE_INT = Pattern.compile(
        "\\b(\\w+)\\.orElse\\(0\\)"
    );
    private static final Pattern OPTIONAL_OR_ELSE_DOUBLE = Pattern.compile(
        "\\b(\\w+)\\.orElse\\(0\\.0\\)"
    );
    private static final Pattern OPTIONAL_OR_ELSE_BOOLEAN = Pattern.compile(
        "\\b(\\w+)\\.orElse\\((true|false)\\)"
    );
    private static final Pattern OPTIONAL_MAP_MEMORY_ADDRESS = Pattern.compile(
        "\\b(\\w+)\\.map\\(([\\w.$]+)::\\$memoryAddress\\)\\.orElse\\(0L\\)"
    );

    @Override
    public String apply(String source) {
        String rewritten = rewriteSignatures(source);
        rewritten = OPTIONAL_IS_PRESENT.matcher(rewritten).replaceAll("$1 != null");
        rewritten = OPTIONAL_OR_ELSE_NULL.matcher(rewritten).replaceAll("$1");
        rewritten = OPTIONAL_OR_ELSE_LONG.matcher(rewritten).replaceAll("(($1 != null) ? $1 : 0L)");
        rewritten = OPTIONAL_OR_ELSE_INT.matcher(rewritten).replaceAll("(($1 != null) ? $1 : 0)");
        rewritten = OPTIONAL_OR_ELSE_DOUBLE.matcher(rewritten).replaceAll("(($1 != null) ? $1 : 0.0)");
        rewritten = OPTIONAL_OR_ELSE_BOOLEAN.matcher(rewritten).replaceAll("(($1 != null) ? $1 : $2)");
        rewritten = OPTIONAL_MAP_MEMORY_ADDRESS.matcher(rewritten).replaceAll("($1 != null) ? $1.\\$memoryAddress() : 0L");
        return rewritten;
    }

    private String rewriteSignatures(String source) {
        Matcher matcher = METHOD_OR_INTERFACE_SIGNATURE.matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            String prefix = matcher.group(1);
            String parameters = matcher.group(2);
            String suffix = matcher.group(3);

            String rewrittenParameters = OPTIONAL_PARAMETER.matcher(parameters).replaceAll("$1 $2");
            rewrittenParameters = OPTIONAL_LONG_PARAMETER.matcher(rewrittenParameters).replaceAll("Long $1");
            rewrittenParameters = OPTIONAL_INT_PARAMETER.matcher(rewrittenParameters).replaceAll("Integer $1");
            rewrittenParameters = OPTIONAL_DOUBLE_PARAMETER.matcher(rewrittenParameters).replaceAll("Double $1");

            output.append(source, last, matcher.start());
            output.append(prefix).append(rewrittenParameters).append(suffix);
            last = matcher.end();
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
