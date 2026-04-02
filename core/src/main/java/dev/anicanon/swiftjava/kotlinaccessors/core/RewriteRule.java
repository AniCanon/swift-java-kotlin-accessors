package dev.anicanon.swiftjava.kotlinaccessors.core;

@FunctionalInterface
public interface RewriteRule {
    String apply(String source);

    default RewriteRule untilStable() {
        return source -> {
            String current = source;
            while (true) {
                String next = RewriteRule.this.apply(current);
                if (next.equals(current)) {
                    return current;
                }
                current = next;
            }
        };
    }
}
