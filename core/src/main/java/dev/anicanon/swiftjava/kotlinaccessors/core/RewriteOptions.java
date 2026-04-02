package dev.anicanon.swiftjava.kotlinaccessors.core;

import java.util.List;
import java.util.Objects;

public final class RewriteOptions {
    private final List<String> packagePrefixes;
    private final String nullableAnnotationFqcn;

    public RewriteOptions(List<String> packagePrefixes, String nullableAnnotationFqcn) {
        this.packagePrefixes = (packagePrefixes == null ? List.<String>of() : packagePrefixes)
            .stream().filter(Objects::nonNull).toList();
        this.nullableAnnotationFqcn = nullableAnnotationFqcn;
    }

    public List<String> getPackagePrefixes() {
        return packagePrefixes;
    }

    public String getNullableAnnotationFqcn() {
        return nullableAnnotationFqcn;
    }

    public boolean shouldRewritePackage(String packageName) {
        if (packagePrefixes.isEmpty()) {
            return true;
        }
        return packagePrefixes.stream().anyMatch(packageName::startsWith);
    }
}
