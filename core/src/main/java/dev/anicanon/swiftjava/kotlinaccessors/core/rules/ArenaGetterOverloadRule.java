package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArenaGetterOverloadRule implements RewriteRule {
    private static final Pattern ARENA_GETTER_SIGNATURE = Pattern.compile(
        "(?m)^(\\s*)public ([\\w.$\\[\\]<>]+) (get\\w+)\\(SwiftArena swiftArena\\) \\{$"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = ARENA_GETTER_SIGNATURE.matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            int openBrace = source.indexOf('{', matcher.end() - 1);
            int closeBrace = SourceRewriteUtils.findMatchingBrace(source, openBrace);
            int methodEnd = SourceRewriteUtils.endOfLine(source, closeBrace);

            String indent = matcher.group(1);
            String returnType = matcher.group(2);
            String methodName = matcher.group(3);

            output.append(source, last, methodEnd);

            String noArgSignature = methodName + "()";
            if (!source.contains(noArgSignature)) {
                String overload = "\n"
                    + indent + "public " + returnType + " " + methodName + "() {\n"
                    + indent + "  return " + methodName + "(SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA);\n"
                    + indent + "}\n";
                output.append(overload);
            }
            last = methodEnd;
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
