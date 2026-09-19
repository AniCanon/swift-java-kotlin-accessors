package dev.anicanon.swiftjava.kotlinaccessors.core;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourceRewriteUtils {
    public static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^package\\s+([\\w.]+);\\s*$");
    /** Optional {@code throws} clause between a method's parameter list and its opening brace. */
    public static final String THROWS_CLAUSE = "(\\s+throws\\s+[\\w.$]+(?:\\s*,\\s*[\\w.$]+)*)?";
    private static final Pattern ENUM_CASE_ACCESSOR = Pattern.compile("getAs[A-Z].*");
    private static final Pattern REFERENCE_ARRAY = Pattern.compile("([\\w.$]+)\\[\\](?!\\[)");
    private static final Set<String> PRIMITIVES = Set.of(
        "boolean", "byte", "char", "short", "int", "long", "float", "double"
    );

    private SourceRewriteUtils() {}

    /** True for {@code T[]} where {@code T} is a class type, not a primitive or a nested array. */
    public static boolean isReferenceArray(String type) {
        if (!type.endsWith("[]") || type.endsWith("[][]")) {
            return false;
        }
        return !PRIMITIVES.contains(type.substring(0, type.length() - 2));
    }

    /** {@code text} with every class-typed array {@code T[]} written as {@code java.util.List<T>}. */
    public static String withArraysAsLists(String text) {
        Matcher arrays = REFERENCE_ARRAY.matcher(text);
        StringBuilder result = new StringBuilder();
        while (arrays.find()) {
            String replacement = PRIMITIVES.contains(arrays.group(1))
                ? arrays.group()
                : "java.util.List<" + arrays.group(1) + ">";
            arrays.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        arrays.appendTail(result);
        return result.toString();
    }

    /**
     * True when {@code source} declares the public method {@code declaration} (return type, name
     * and parameter list, without modifiers) in any of its forms: with or without a
     * {@code throws} clause, and with each class-typed array written either as {@code T[]} or
     * as the {@code java.util.List<T>} that {@code ArrayListRule} turns it into.
     */
    public static boolean containsDeclaration(String source, String declaration) {
        StringBuilder regex = new StringBuilder("(?m)^\\s*public (?:static )?(?:<[^(){}]*>\\s+)?");
        Matcher arrays = REFERENCE_ARRAY.matcher(declaration);
        int last = 0;
        while (arrays.find()) {
            regex.append(Pattern.quote(declaration.substring(last, arrays.start())));
            if (PRIMITIVES.contains(arrays.group(1))) {
                regex.append(Pattern.quote(arrays.group()));
            } else {
                regex.append("(?:").append(Pattern.quote(arrays.group()))
                    .append("|").append(Pattern.quote("java.util.List<" + arrays.group(1) + ">")).append(")");
            }
            last = arrays.end();
        }
        regex.append(Pattern.quote(declaration.substring(last))).append(THROWS_CLAUSE).append("\\s*\\{");
        return Pattern.compile(regex.toString()).matcher(source).find();
    }

    public static boolean isOptionalEnumCaseAccessor(String methodName) {
        return ENUM_CASE_ACCESSOR.matcher(methodName).matches();
    }

    /**
     * Finds the index of the closing brace that matches the opening brace at {@code openBrace}.
     * <p>
     * Note: this method does not account for braces inside string literals or comments.
     * This is safe because the input is machine-generated jextract code which does not
     * contain brace characters in string literals.
     */
    public static int findMatchingBrace(String source, int openBrace) {
        int depth = 0;
        for (int index = openBrace; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        throw new IllegalStateException("Unmatched method brace in generated source.");
    }

    public static int endOfLine(String source, int position) {
        int newline = source.indexOf('\n', position);
        return newline >= 0 ? newline + 1 : source.length();
    }

    public static String nullableAnnotation(String indent, RewriteOptions options) {
        String annotation = options.getNullableAnnotationFqcn();
        if (annotation == null || annotation.isBlank()) {
            return "";
        }
        String simpleName = annotation.substring(annotation.lastIndexOf('.') + 1);
        return indent + "@" + simpleName + "\n";
    }

    public static String stripTrailingArenaParameter(String parameters) {
        if (parameters.equals("SwiftArena swiftArena")) {
            return "";
        }
        String suffix = ", SwiftArena swiftArena";
        if (!parameters.endsWith(suffix)) {
            return null;
        }
        return parameters.substring(0, parameters.length() - suffix.length());
    }

    public static String invocationArguments(String parameters) {
        if (parameters.isBlank()) {
            return "";
        }
        String[] parts = parameters.split(",\\s*");
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            String parameter = parts[index].trim();
            int lastSpace = parameter.lastIndexOf(' ');
            String argument = lastSpace >= 0 ? parameter.substring(lastSpace + 1) : parameter;
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(argument);
        }
        return builder.toString();
    }
}
