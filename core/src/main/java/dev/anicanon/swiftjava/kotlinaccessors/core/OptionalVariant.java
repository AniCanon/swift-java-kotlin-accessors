package dev.anicanon.swiftjava.kotlinaccessors.core;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptionalVariant {
    private final String boxedType;
    private final boolean generic;
    private final Pattern methodSignature;
    private final Pattern interfaceSignature;
    private final Pattern returnLine;
    private final Pattern parameter;

    private OptionalVariant(String boxedType, boolean generic,
                            Pattern methodSignature, Pattern interfaceSignature,
                            Pattern returnLine, Pattern parameter) {
        this.boxedType = boxedType;
        this.generic = generic;
        this.methodSignature = methodSignature;
        this.interfaceSignature = interfaceSignature;
        this.returnLine = returnLine;
        this.parameter = parameter;
    }

    public String getBoxedType() {
        return boxedType;
    }

    public Pattern getMethodSignature() {
        return methodSignature;
    }

    public Pattern getInterfaceSignature() {
        return interfaceSignature;
    }

    public Pattern getReturnLine() {
        return returnLine;
    }

    public Pattern getParameter() {
        return parameter;
    }

    public String extractReturnType(Matcher methodMatcher) {
        if (generic) {
            return methodMatcher.group(3).trim();
        }
        return boxedType;
    }

    public boolean extractIsStatic(Matcher methodMatcher) {
        if (generic) {
            return methodMatcher.group(2) != null;
        }
        return methodMatcher.group(2) != null;
    }

    public String extractMethodName(Matcher methodMatcher) {
        return generic ? methodMatcher.group(4) : methodMatcher.group(3);
    }

    public String extractMethodParameters(Matcher methodMatcher) {
        return (generic ? methodMatcher.group(5) : methodMatcher.group(4)).trim();
    }

    public String extractInterfaceReturnType(Matcher interfaceMatcher) {
        if (generic) {
            return interfaceMatcher.group(2).trim();
        }
        return boxedType;
    }

    public String extractInterfaceMethodName(Matcher interfaceMatcher) {
        return generic ? interfaceMatcher.group(3) : interfaceMatcher.group(2);
    }

    public String extractInterfaceParameters(Matcher interfaceMatcher) {
        return (generic ? interfaceMatcher.group(4) : interfaceMatcher.group(3)).trim();
    }

    public static OptionalVariant genericOptional() {
        return new OptionalVariant(
            null, true,
            Pattern.compile("(?m)^(\\s*)public( static)? Optional<(.+?)> (\\w+)\\(([^)]*)\\) \\{$"),
            Pattern.compile("(?m)^(\\s*)public Optional<(.+?)> (\\w+)\\(([^)]*)\\);$"),
            Pattern.compile("return \\(([^\\n]+)\\) \\? Optional\\.of\\(([^\\n]+)\\) : Optional\\.empty\\(\\);"),
            Pattern.compile("\\bOptional<([\\w.$\\[\\]<>]+)>\\s+(\\w+)\\b")
        );
    }

    public static OptionalVariant primitive(String optionalTypeName, String boxedType) {
        String escaped = Pattern.quote(optionalTypeName);
        return new OptionalVariant(
            boxedType, false,
            Pattern.compile("(?m)^(\\s*)public( static)? " + escaped + " (\\w+)\\(([^)]*)\\) \\{$"),
            Pattern.compile("(?m)^(\\s*)public " + escaped + " (\\w+)\\(([^)]*)\\);$"),
            Pattern.compile("return \\(([^\\n]+)\\) \\? " + escaped + "\\.of\\(([^\\n]+)\\) : " + escaped + "\\.empty\\(\\);"),
            Pattern.compile("\\b" + escaped + "\\s+(\\w+)\\b")
        );
    }

    public static List<OptionalVariant> standardVariants() {
        return List.of(
            genericOptional(),
            primitive("OptionalLong", "Long"),
            primitive("OptionalInt", "Integer"),
            primitive("OptionalDouble", "Double")
        );
    }
}
