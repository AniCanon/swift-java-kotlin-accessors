package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StaticTrailingArenaOverloadRule implements RewriteRule {
    private static final Pattern STATIC_TRAILING_ARENA_SIGNATURE = Pattern.compile(
        "(?m)^(\\s*)public static (<[^(){}]*>\\s+)?([\\w.$\\[\\]<>]+) (\\w+)\\(([^)]*\\bSwiftArena swiftArena)\\) \\{$"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = STATIC_TRAILING_ARENA_SIGNATURE.matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            int openBrace = source.indexOf('{', matcher.end() - 1);
            int closeBrace = SourceRewriteUtils.findMatchingBrace(source, openBrace);
            int methodEnd = SourceRewriteUtils.endOfLine(source, closeBrace);

            String indent = matcher.group(1);
            String typeParameters = matcher.group(2) == null ? "" : matcher.group(2).trim() + " ";
            String returnType = matcher.group(3);
            String methodName = matcher.group(4);
            String parameters = matcher.group(5).trim();

            String parametersWithoutArena = SourceRewriteUtils.stripTrailingArenaParameter(parameters);
            if (methodName.startsWith("get") || methodName.equals("wrapMemoryAddressUnsafe") || parametersWithoutArena == null) {
                output.append(source, last, methodEnd);
                last = methodEnd;
                continue;
            }

            String invocationArguments = SourceRewriteUtils.invocationArguments(parametersWithoutArena);
            if (!invocationArguments.isBlank()) {
                invocationArguments = invocationArguments + ", ";
            }
            invocationArguments = invocationArguments + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA";

            output.append(source, last, methodEnd);

            String declarationSignature = returnType + " " + methodName + "(" + parametersWithoutArena + ") {";
            if (!source.contains(declarationSignature)) {
                String overload = "\n"
                    + indent + "public static " + typeParameters + returnType + " " + methodName + "(" + parametersWithoutArena + ") {\n"
                    + indent + "  return " + methodName + "(" + invocationArguments + ");\n"
                    + indent + "}\n";
                output.append(overload);
            }
            last = methodEnd;
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
