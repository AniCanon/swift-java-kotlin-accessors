package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds an arena-free overload next to each instance method whose last parameter is a
 * {@code SwiftArena}, registering the result with the default auto arena. Getters are left
 * to {@link ArenaGetterOverloadRule}, and methods returning {@code CompletableFuture} are
 * left to the generated bridges.
 */
public final class InstanceTrailingArenaOverloadRule implements RewriteRule {
    private static final Pattern INSTANCE_TRAILING_ARENA_SIGNATURE = Pattern.compile(
        "(?m)^(\\s*)public (?!static )(<[^(){}]*>\\s+)?([\\w.$\\[\\]<>]+) (\\w+)\\(([^)]*\\bSwiftArena swiftArena)\\)"
            + SourceRewriteUtils.THROWS_CLAUSE + " \\{$"
    );

    @Override
    public String apply(String source) {
        Matcher matcher = INSTANCE_TRAILING_ARENA_SIGNATURE.matcher(source);
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
            String throwsClause = matcher.group(6) == null ? "" : matcher.group(6);
            String parametersWithoutArena = SourceRewriteUtils.stripTrailingArenaParameter(parameters);

            output.append(source, last, methodEnd);
            last = methodEnd;
            if (methodName.startsWith("get") || returnType.contains("CompletableFuture") || parametersWithoutArena == null) {
                continue;
            }

            String arguments = SourceRewriteUtils.invocationArguments(parametersWithoutArena);
            arguments = (arguments.isBlank() ? "" : arguments + ", ") + "SwiftMemoryManagement.DEFAULT_SWIFT_JAVA_AUTO_ARENA";
            String declaration = returnType + " " + methodName + "(" + parametersWithoutArena + ")";
            if (!SourceRewriteUtils.containsDeclaration(source, declaration)) {
                output.append("\n")
                    .append(indent).append("public ").append(typeParameters).append(declaration).append(throwsClause).append(" {\n")
                    .append(indent).append("  return ").append(methodName).append("(").append(arguments).append(");\n")
                    .append(indent).append("}\n");
            }
        }
        output.append(source.substring(last));
        return output.toString();
    }
}
