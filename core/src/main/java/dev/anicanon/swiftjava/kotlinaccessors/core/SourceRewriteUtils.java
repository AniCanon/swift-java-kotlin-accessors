package dev.anicanon.swiftjava.kotlinaccessors.core;

import java.util.regex.Pattern;

public final class SourceRewriteUtils {
    private static final Pattern ENUM_CASE_ACCESSOR = Pattern.compile("getAs[A-Z].*");

    private SourceRewriteUtils() {}

    public static boolean isOptionalEnumCaseAccessor(String methodName) {
        return ENUM_CASE_ACCESSOR.matcher(methodName).matches();
    }

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
