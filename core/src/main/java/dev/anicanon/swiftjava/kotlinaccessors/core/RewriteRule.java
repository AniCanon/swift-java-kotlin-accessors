package dev.anicanon.swiftjava.kotlinaccessors.core;

@FunctionalInterface
public interface RewriteRule {
    String apply(String source);

    int MAX_ITERATIONS = 100;

    default RewriteRule untilStable() {
        return source -> {
            String current = source;
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                String next = RewriteRule.this.apply(current);
                if (next.equals(current)) {
                    return current;
                }
                current = next;
            }
            throw new IllegalStateException(
                "RewriteRule did not stabilize after " + MAX_ITERATIONS + " iterations"
            );
        };
    }
}
