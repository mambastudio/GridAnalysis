package gridanalysis.algorithm;

/**
 * User-selectable parameters for constructing a Hagrid irregular grid.
 *
 * <p>The defaults match the reference implementation and the paper's static
 * scene configuration.</p>
 */
public record HagridConfig(
        float topDensity,
        float secondDensity,
        float mergeAlpha,
        int expansionIterations,
        boolean partialExpansion,
        boolean compression) {

    public static final float DEFAULT_TOP_DENSITY = 0.12f;
    public static final float DEFAULT_SECOND_DENSITY = 2.4f;
    public static final float DEFAULT_MERGE_ALPHA = 0.995f;
    public static final int DEFAULT_EXPANSION_ITERATIONS = 3;
    public static final boolean DEFAULT_PARTIAL_EXPANSION = true;
    public static final boolean DEFAULT_COMPRESSION = false;

    public HagridConfig {
        if (!(topDensity > 0.0f)) {
            throw new IllegalArgumentException("topDensity must be positive");
        }
        if (!(secondDensity > 0.0f)) {
            throw new IllegalArgumentException("secondDensity must be positive");
        }
        if (!(mergeAlpha >= 0.0f && mergeAlpha <= 1.0f)) {
            throw new IllegalArgumentException("mergeAlpha must be between 0 and 1");
        }
        if (expansionIterations < 0) {
            throw new IllegalArgumentException("expansionIterations cannot be negative");
        }
    }

    public static HagridConfig defaults() {
        return new HagridConfig(
                DEFAULT_TOP_DENSITY,
                DEFAULT_SECOND_DENSITY,
                DEFAULT_MERGE_ALPHA,
                DEFAULT_EXPANSION_ITERATIONS,
                DEFAULT_PARTIAL_EXPANSION,
                DEFAULT_COMPRESSION);
    }
}
