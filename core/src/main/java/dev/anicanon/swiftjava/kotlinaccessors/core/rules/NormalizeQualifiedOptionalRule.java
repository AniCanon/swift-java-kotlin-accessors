package dev.anicanon.swiftjava.kotlinaccessors.core.rules;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteRule;
import java.util.regex.Pattern;

/**
 * Normalizes fully-qualified {@code java.util.Optional} type references to the bare
 * {@code Optional} form so the downstream Optional rewrite rules match consistently.
 *
 * <p>Newer swift-java (jextract) emits fully-qualified {@code java.util.Optional<...>}
 * in method signatures and parameters, whereas the {@code OptionalInterfaceRewriteRule},
 * {@code OptionalMethodRewriteRule}, and {@code OptionalParameterRewriteRule} are all
 * written against the bare {@code Optional} form. Left unqualified, a qualified parameter
 * such as {@code java.util.Optional<Foo> x} is only partially rewritten to
 * {@code java.util.Foo x} (the {@code java.util.} prefix is stripped from {@code Optional}
 * but wrongly retained on the inner type), and a qualified return type is not rewritten at
 * all. The generated files already {@code import java.util.*}, so dropping the qualifier is
 * safe.
 *
 * <p>Covers {@code Optional}, {@code OptionalLong}, {@code OptionalInt}, and
 * {@code OptionalDouble}, which all share the {@code java.util.Optional} prefix.
 * References inside {@code import} statements are left untouched.
 */
public final class NormalizeQualifiedOptionalRule implements RewriteRule {
    private static final Pattern QUALIFIED_OPTIONAL = Pattern.compile("(?<!import )\\bjava\\.util\\.(Optional)");

    @Override
    public String apply(String source) {
        return QUALIFIED_OPTIONAL.matcher(source).replaceAll("$1");
    }
}
