package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.OptionalVariant;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;

public final class OptionalInterfaceRewriteRule implements RewriteRule {
    private final OptionalVariant variant;
    private final RewriteOptions options;

    public OptionalInterfaceRewriteRule(OptionalVariant variant, RewriteOptions options) {
        this.variant = variant;
        this.options = options;
    }

    @Override
    public String apply(String source) {
        Matcher matcher = variant.getInterfaceSignature().matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            String indent = matcher.group(1);
            String returnType = variant.extractInterfaceReturnType(matcher);
            String methodName = variant.extractInterfaceMethodName(matcher);
            String parameters = variant.extractInterfaceParameters(matcher);

            output.append(source, last, matcher.start());
            if (SourceRewriteUtils.isOptionalEnumCaseAccessor(methodName)) {
                output.append(source, matcher.start(), matcher.end());
            } else {
                output.append(SourceRewriteUtils.nullableAnnotation(indent, options));
                output.append(indent)
                    .append("public ")
                    .append(returnType)
                    .append(' ')
                    .append(methodName)
                    .append('(')
                    .append(parameters)
                    .append(");");
            }
            last = matcher.end();
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
