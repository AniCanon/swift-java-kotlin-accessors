package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.SourceRewriteUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exposes Swift arrays of class types as {@code java.util.List} on the methods Kotlin calls.
 *
 * <p>Applies to public methods with a body, no {@code SwiftArena} parameter and a return type
 * other than {@code CompletableFuture}. A {@code T[]} return becomes {@code java.util.List<T>};
 * a {@code T[]} parameter becomes {@code java.util.List<T>} and is converted back to an array
 * wherever the body reads it. The arena overloads keep their arrays, since the generated
 * bridges call them. Primitive and nested arrays are left alone, and so is a method whose
 * array return is not a single top-level {@code return} statement.
 *
 * <p>A return is nullable when the method carries a {@code Nullable} annotation, its return
 * expression contains {@code null}, or the Swift declaration returns {@code [T]?}; a parameter
 * is nullable when it is annotated {@code Nullable} or the body compares it with {@code null}.
 * Nullable values are converted null-safely.
 */
public final class ArrayListRule implements RewriteRule {
    private static final Pattern METHOD_SIGNATURE = Pattern.compile(
        "(?m)^([ \\t]*)public ((?:static )?)(<[^(){}]*>\\s+)?([\\w.$<>\\[\\]]+) (\\w+)\\(([^)]*)\\)"
            + SourceRewriteUtils.THROWS_CLAUSE + "\\s*\\{[ \\t]*$"
    );
    private static final Pattern PARAMETER = Pattern.compile(
        "^((?:@[\\w.]+\\s+)*)([\\w.$<>\\[\\]]+)\\s+(\\w+)$"
    );
    private static final Pattern NULLABLE_ANNOTATION = Pattern.compile("@(?:[\\w.]+\\.)?\\w*Nullable\\b");
    private static final Pattern NULL_LITERAL = Pattern.compile("(?<![\\w$])null(?![\\w$])");
    private static final Pattern SWIFT_SNIPPET = Pattern.compile("\\{@snippet lang=swift :\\n\\s*\\*\\s*([^\\n]*)");
    private static final Pattern SWIFT_PROPERTY_TYPE = Pattern.compile("\\b(?:var|let)\\s+\\w+\\s*:\\s*(.+?)\\s*(?:\\{.*)?$");
    private static final String RESULT = "array$";

    @Override
    public String apply(String source) {
        Matcher matcher = METHOD_SIGNATURE.matcher(source);
        StringBuilder output = new StringBuilder();
        int last = 0;
        while (matcher.find(last)) {
            int openBrace = source.indexOf('{', matcher.end(6));
            int closeBrace = SourceRewriteUtils.findMatchingBrace(source, openBrace);
            String replacement = rewrite(source, matcher, openBrace, closeBrace);
            output.append(source, last, matcher.start());
            output.append(replacement != null ? replacement : source.substring(matcher.start(), closeBrace));
            last = closeBrace;
        }
        output.append(source.substring(last));
        return output.toString();
    }

    private String rewrite(String source, Matcher matcher, int openBrace, int closeBrace) {
        String returnType = matcher.group(4);
        String parameterList = matcher.group(6);
        if (parameterList.contains("SwiftArena") || returnType.contains("CompletableFuture")) {
            return null;
        }
        List<Parameter> parameters = parseParameters(parameterList);
        if (parameters == null) {
            return null;
        }
        boolean arrayReturn = SourceRewriteUtils.isReferenceArray(returnType);
        if (!arrayReturn && parameters.stream().noneMatch(Parameter::isArray)) {
            return null;
        }

        String body = source.substring(openBrace + 1, closeBrace);
        List<String> rewrittenParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (!parameter.isArray()) {
                rewrittenParameters.add(parameter.declaration());
                continue;
            }
            boolean nullable = NULLABLE_ANNOTATION.matcher(parameter.annotations()).find()
                || comparesWithNull(body, parameter.name());
            String toArray = parameter.name() + ".toArray(new " + parameter.element() + "[0])";
            body = replaceReads(body, parameter.name(),
                nullable ? "(" + parameter.name() + " == null ? null : " + toArray + ")" : toArray);
            rewrittenParameters.add(parameter.annotations() + "java.util.List<" + parameter.element() + "> " + parameter.name());
        }

        String rewrittenReturnType = returnType;
        if (arrayReturn) {
            String element = returnType.substring(0, returnType.length() - 2);
            boolean nullable = hasNullableAnnotation(source, matcher.start())
                || returnsOptionalSwiftArray(source, matcher.start());
            body = wrapReturn(body, returnType, element, nullable);
            if (body == null) {
                return null;
            }
            rewrittenReturnType = "java.util.List<" + element + ">";
        }

        return matcher.group(1) + "public " + matcher.group(2)
            + (matcher.group(3) == null ? "" : matcher.group(3))
            + rewrittenReturnType + " " + matcher.group(5) + "(" + String.join(", ", rewrittenParameters) + ")"
            + (matcher.group(7) == null ? "" : matcher.group(7))
            + source.substring(matcher.end(7) < 0 ? matcher.end(6) + 1 : matcher.end(7), openBrace + 1)
            + body;
    }

    private static List<Parameter> parseParameters(String parameterList) {
        List<Parameter> parameters = new ArrayList<>();
        if (parameterList.isBlank()) {
            return parameters;
        }
        for (String part : parameterList.split(",")) {
            Matcher matcher = PARAMETER.matcher(part.trim());
            if (!matcher.matches()) {
                return null;
            }
            parameters.add(new Parameter(matcher.group(1), matcher.group(2), matcher.group(3)));
        }
        return parameters;
    }

    private static boolean comparesWithNull(String body, String name) {
        String quoted = Pattern.quote(name);
        return Pattern.compile("(?<![\\w$.])" + quoted + "\\s*[!=]=\\s*null(?![\\w$])"
            + "|(?<![\\w$])null\\s*[!=]=\\s*" + quoted + "(?![\\w$])").matcher(body).find();
    }

    private static boolean hasNullableAnnotation(String source, int methodStart) {
        int lineStart = source.lastIndexOf('\n', methodStart - 1);
        while (lineStart > 0) {
            int previousStart = source.lastIndexOf('\n', lineStart - 1) + 1;
            String line = source.substring(previousStart, lineStart).trim();
            if (!line.startsWith("@")) {
                return false;
            }
            if (NULLABLE_ANNOTATION.matcher(line).find()) {
                return true;
            }
            lineStart = previousStart - 1;
        }
        return false;
    }

    private static boolean returnsOptionalSwiftArray(String source, int methodStart) {
        int sectionStart = source.lastIndexOf("// ====", methodStart);
        Matcher snippet = SWIFT_SNIPPET.matcher(source.substring(Math.max(sectionStart, 0), methodStart));
        if (!snippet.find()) {
            return false;
        }
        String declaration = snippet.group(1).trim();
        String swiftType;
        int arrow = declaration.lastIndexOf("->");
        if (arrow >= 0) {
            swiftType = declaration.substring(arrow + 2).replaceAll("\\{.*$", "").trim();
        } else {
            Matcher property = SWIFT_PROPERTY_TYPE.matcher(declaration);
            if (!property.find()) {
                return false;
            }
            swiftType = property.group(1);
        }
        return swiftType.startsWith("[") && swiftType.endsWith("]?");
    }

    /** Wraps the body's single top-level {@code return EXPR;}, or returns null when there is not exactly one. */
    private static String wrapReturn(String body, String arrayType, String element, boolean nullable) {
        List<int[]> returns = topLevelReturns(body);
        if (returns.size() != 1) {
            return null;
        }
        int start = returns.get(0)[0];
        int end = returns.get(0)[1];
        String expression = body.substring(start + "return".length(), end).trim();
        String statement;
        if (nullable || NULL_LITERAL.matcher(stripLiterals(expression)).find()) {
            int lineStart = body.lastIndexOf('\n', start) + 1;
            String indent = body.substring(lineStart, start);
            statement = arrayType + " " + RESULT + " = " + expression + ";\n"
                + indent + "return " + RESULT + " == null ? null : java.util.List.of(" + RESULT + ");";
        } else {
            statement = "return java.util.List.of(" + expression + ");";
        }
        return body.substring(0, start) + statement + body.substring(end + 1);
    }

    /** Start of each {@code return} keyword outside nested braces, and the index of its terminating semicolon. */
    private static List<int[]> topLevelReturns(String body) {
        List<int[]> returns = new ArrayList<>();
        int depth = 0;
        int index = 0;
        while (index < body.length()) {
            int skipped = skipLiteralOrComment(body, index);
            if (skipped != index) {
                index = skipped;
                continue;
            }
            char current = body.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
            } else if (depth == 0 && body.startsWith("return", index) && isWordBoundary(body, index, "return".length())) {
                int end = statementEnd(body, index);
                if (end < 0) {
                    return List.of();
                }
                returns.add(new int[] {index, end});
                index = end + 1;
                continue;
            }
            index++;
        }
        return returns;
    }

    private static int statementEnd(String body, int from) {
        int depth = 0;
        int index = from;
        while (index < body.length()) {
            int skipped = skipLiteralOrComment(body, index);
            if (skipped != index) {
                index = skipped;
                continue;
            }
            char current = body.charAt(index);
            if (current == '(' || current == '{' || current == '[') {
                depth++;
            } else if (current == ')' || current == '}' || current == ']') {
                depth--;
            } else if (current == ';' && depth == 0) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /** Replaces reads of {@code name} outside literals and comments, leaving null comparisons and calls to a same-named method. */
    private static String replaceReads(String body, String name, String replacement) {
        Pattern read = Pattern.compile("(?<![\\w$.])" + Pattern.quote(name) + "(?![\\w$])(?!\\s*\\()(?!\\s*[!=]=\\s*null)");
        Pattern nullOnLeft = Pattern.compile("null\\s*[!=]=\\s*$");
        StringBuilder result = new StringBuilder();
        int index = 0;
        int codeStart = 0;
        while (index <= body.length()) {
            int skipped = index < body.length() ? skipLiteralOrComment(body, index) : index;
            if (skipped != index || index == body.length()) {
                String code = body.substring(codeStart, index);
                Matcher matcher = read.matcher(code);
                int last = 0;
                while (matcher.find()) {
                    result.append(code, last, matcher.start());
                    boolean comparedOnRight = nullOnLeft.matcher(code.substring(0, matcher.start())).find();
                    result.append(comparedOnRight ? matcher.group() : replacement);
                    last = matcher.end();
                }
                result.append(code.substring(last));
                if (index == body.length()) {
                    break;
                }
                result.append(body, index, skipped);
                index = skipped;
                codeStart = skipped;
                continue;
            }
            index++;
        }
        return result.toString();
    }

    private static String stripLiterals(String code) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < code.length()) {
            int skipped = skipLiteralOrComment(code, index);
            if (skipped != index) {
                result.append(' ');
                index = skipped;
                continue;
            }
            result.append(code.charAt(index));
            index++;
        }
        return result.toString();
    }

    /** Index just past the string literal, char literal or line comment starting at {@code index}, or {@code index} itself. */
    private static int skipLiteralOrComment(String code, int index) {
        char current = code.charAt(index);
        if (current == '"' || current == '\'') {
            int cursor = index + 1;
            while (cursor < code.length() && code.charAt(cursor) != current) {
                cursor += code.charAt(cursor) == '\\' ? 2 : 1;
            }
            return Math.min(cursor + 1, code.length());
        }
        if (current == '/' && code.startsWith("//", index)) {
            int newline = code.indexOf('\n', index);
            return newline < 0 ? code.length() : newline;
        }
        return index;
    }

    private static boolean isWordBoundary(String code, int start, int length) {
        boolean before = start == 0 || !isIdentifierPart(code.charAt(start - 1));
        int end = start + length;
        boolean after = end >= code.length() || !isIdentifierPart(code.charAt(end));
        return before && after;
    }

    private static boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '$';
    }

    private record Parameter(String annotations, String type, String name) {
        boolean isArray() {
            return SourceRewriteUtils.isReferenceArray(type);
        }

        String element() {
            return type.substring(0, type.length() - 2);
        }

        String declaration() {
            return annotations + type + " " + name;
        }
    }
}
