package dev.anicanon.swiftjava.kotlinaccessors.core;

import dev.anicanon.swiftjava.kotlinaccessors.core.rules.AddNullableImportRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.AddRewriteMarkerRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.ArenaGetterOverloadRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.OptionalInterfaceRewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.OptionalMethodRewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.OptionalParameterRewriteRule;
import dev.anicanon.swiftjava.kotlinaccessors.core.rules.StaticTrailingArenaOverloadRule;
import java.util.ArrayList;
import java.util.List;

public final class SwiftJavaJavaRewriter {
    private final List<RewriteRule> pipeline;

    public SwiftJavaJavaRewriter(RewriteOptions options) {
        List<RewriteRule> rules = new ArrayList<>();
        rules.add(new AddRewriteMarkerRule());
        rules.add(new AddNullableImportRule(options));

        List<OptionalVariant> variants = OptionalVariant.standardVariants();
        for (OptionalVariant variant : variants) {
            rules.add(new OptionalInterfaceRewriteRule(variant, options).untilStable());
        }
        for (OptionalVariant variant : variants) {
            rules.add(new OptionalMethodRewriteRule(variant, options).untilStable());
        }

        rules.add(new OptionalParameterRewriteRule());
        rules.add(new ArenaGetterOverloadRule());
        rules.add(new StaticTrailingArenaOverloadRule());

        this.pipeline = List.copyOf(rules);
    }

    public String rewrite(String source) {
        String result = source;
        for (RewriteRule rule : pipeline) {
            result = rule.apply(result);
        }
        return result;
    }
}
