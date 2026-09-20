package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renames the constants of a Swift enum's {@code Discriminator} from jextract's
 * collapsed form ({@code FACESHEETCAPTURE}) to {@code FACE_SHEET_CAPTURE}.
 *
 * <p>jextract emits the {@code Case} records in the same order as the
 * {@code Discriminator} constants and keeps the Swift case name's word boundaries in
 * the record names, so the n-th record names the n-th constant. The
 * {@code Discriminator} is resolved by ordinal, so renaming its constants does not
 * change the native contract. Bare references are rewritten only inside the enum's own
 * generated block (the {@code Discriminator}, the {@code Case} interface and
 * {@code getCase()}); qualified {@code Discriminator.X} references anywhere in the file.
 */
public final class EnumCaseNamingRule implements RewriteRule {
    private static final Pattern DISCRIMINATOR = Pattern.compile("public enum Discriminator \\{([^}]*)\\}");
    private static final Pattern CASE_INTERFACE = Pattern.compile("public sealed interface Case \\{");
    private static final Pattern RECORD = Pattern.compile("\\brecord (\\w+)\\(");
    private static final Pattern GET_CASE = Pattern.compile("public Case getCase\\(\\) \\{");

    @Override
    public String apply(String source) {
        StringBuilder result = new StringBuilder();
        Map<String, String> allRenames = new LinkedHashMap<>();
        int cursor = 0;
        Matcher discriminator = DISCRIMINATOR.matcher(source);
        while (discriminator.find(cursor)) {
            int blockStart = discriminator.start();
            int blockEnd = blockEnd(source, discriminator.end());
            if (blockEnd < 0) {
                break;
            }
            Map<String, String> renames = renames(
                constants(discriminator.group(1)),
                caseNames(source, discriminator.end(), blockEnd)
            );
            allRenames.putAll(renames);
            result.append(source, cursor, blockStart);
            result.append(renamed(source.substring(blockStart, blockEnd), renames));
            cursor = blockEnd;
        }
        result.append(source.substring(cursor));
        return qualifiedRenamed(result.toString(), allRenames);
    }

    /** Case accessors outside the enum block compare against {@code Discriminator.X}. */
    private static String qualifiedRenamed(String source, Map<String, String> renames) {
        String result = source;
        for (Map.Entry<String, String> rename : renames.entrySet()) {
            result = result.replaceAll(
                "\\bDiscriminator\\." + Pattern.quote(rename.getKey()) + "\\b",
                Matcher.quoteReplacement("Discriminator." + rename.getValue())
            );
        }
        return result;
    }

    /** End of {@code getCase()}, or of the {@code Case} interface when there is no {@code getCase()}. */
    private static int blockEnd(String source, int from) {
        Matcher caseInterface = CASE_INTERFACE.matcher(source);
        if (!caseInterface.find(from)) {
            return -1;
        }
        int interfaceEnd = closingBrace(source, caseInterface.end() - 1);
        if (interfaceEnd < 0) {
            return -1;
        }
        Matcher getCase = GET_CASE.matcher(source);
        Matcher nextDiscriminator = DISCRIMINATOR.matcher(source);
        int nextBoundary = nextDiscriminator.find(interfaceEnd) ? nextDiscriminator.start() : source.length();
        if (getCase.find(interfaceEnd) && getCase.start() < nextBoundary) {
            int getCaseEnd = closingBrace(source, getCase.end() - 1);
            return getCaseEnd < 0 ? interfaceEnd + 1 : getCaseEnd + 1;
        }
        return interfaceEnd + 1;
    }

    private static List<String> constants(String body) {
        List<String> names = new ArrayList<>();
        for (String part : body.split(",")) {
            String name = part.trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    private static List<String> caseNames(String source, int from, int to) {
        Matcher caseInterface = CASE_INTERFACE.matcher(source);
        List<String> names = new ArrayList<>();
        if (!caseInterface.find(from) || caseInterface.start() >= to) {
            return names;
        }
        int interfaceEnd = closingBrace(source, caseInterface.end() - 1);
        Matcher record = RECORD.matcher(source.substring(caseInterface.end(), interfaceEnd));
        while (record.find()) {
            names.add(record.group(1));
        }
        return names;
    }

    private static Map<String, String> renames(List<String> constants, List<String> caseNames) {
        Map<String, String> renames = new LinkedHashMap<>();
        if (constants.size() != caseNames.size()) {
            return renames;
        }
        for (int i = 0; i < constants.size(); i++) {
            String constant = constants.get(i);
            String target = upperSnake(caseNames.get(i));
            if (!constant.equals(target) && constant.equals(target.replace("_", ""))) {
                renames.put(constant, target);
            }
        }
        return renames;
    }

    private static String renamed(String block, Map<String, String> renames) {
        String result = block;
        for (Map.Entry<String, String> rename : renames.entrySet()) {
            result = result.replaceAll("\\b" + Pattern.quote(rename.getKey()) + "\\b", Matcher.quoteReplacement(rename.getValue()));
        }
        return result;
    }

    static String upperSnake(String camel) {
        return camel
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
            .toUpperCase();
    }

    private static int closingBrace(String source, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
