package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.OptionalVariant;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;

public final class OptionalMethodRewriteRule implements RewriteRule {
    private final OptionalVariant variant;
    private final RewriteOptions options;

    public OptionalMethodRewriteRule(OptionalVariant variant, RewriteOptions options) {
        this.variant = variant;
        this.options = options;
    }

    @Override
    public String apply(String source) {
        Matcher matcher = variant.getMethodSignature().matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            int methodStart = matcher.start();
            int openBrace = source.indexOf('{', matcher.end() - 1);
            int closeBrace = SourceRewriteUtils.findMatchingBrace(source, openBrace);
            int methodEnd = SourceRewriteUtils.endOfLine(source, closeBrace);

            String indent = matcher.group(1);
            boolean isStatic = variant.extractIsStatic(matcher);
            String returnType = variant.extractReturnType(matcher);
            String methodName = variant.extractMethodName(matcher);
            String parameters = variant.extractMethodParameters(matcher);
            String body = source.substring(openBrace + 1, closeBrace);
            String closingSuffix = source.substring(closeBrace, methodEnd);
            String original = source.substring(methodStart, methodEnd);

            String replacement = original;
            if (!SourceRewriteUtils.isOptionalEnumCaseAccessor(methodName)) {
                String rewrittenBody = variant.getReturnLine().matcher(body)
                    .replaceFirst("return ($1) ? $2 : null;");
                if (!rewrittenBody.equals(body)) {
                    replacement = SourceRewriteUtils.nullableAnnotation(indent, options)
                        + indent + "public" + (isStatic ? " static" : "") + " " + returnType + " " + methodName + "(" + parameters + ") {"
                        + rewrittenBody
                        + closingSuffix;
                }
            }

            output.append(source, last, methodStart);
            output.append(replacement);
            last = methodEnd;
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
