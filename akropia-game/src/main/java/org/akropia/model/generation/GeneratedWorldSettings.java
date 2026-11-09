package org.akropia.model.generation;

import com.google.common.base.Preconditions;

/**
 * Player choices that define one generated New World. Equal settings always
 * produce the same starting world.
 */
public final class GeneratedWorldSettings {

    /** Largest accepted seed; a seed is typed as a plain decimal number. */
    public static final int MAX_SEED = 999_999_999;

    private static final int MAX_SEED_DIGITS = 9;

    private final int seed;
    private String textSeed;
    private GeneratedMapSize size;
    private GeneratedLandLayout layout;

    public GeneratedWorldSettings(final String seed,
            final GeneratedMapSize size, final GeneratedLandLayout layout) {
        this(seedNumber(seed), GeneratedClimate.TEMPERATE,
                GeneratedLandShare.MEDIUM);
        this.textSeed = org.akropia.model.Player.normalizeDisplayName(seed);
        this.size = Preconditions.checkNotNull(size, "Map size is null");
        this.layout = Preconditions.checkNotNull(layout, "Land layout is null");
    }

    /** Stable UTF-8 digest; independent of platform and JVM random state. */
    public static int seedNumber(final String text) {
        final String normalized = org.akropia.model.Player
                .normalizeDisplayName(text);
        return Math.floorMod(com.google.common.hash.Hashing.sha256()
                .hashString(normalized, java.nio.charset.StandardCharsets.UTF_8)
                .asInt(), MAX_SEED + 1);
    }

    public long getGenerationSeed() {
        return textSeed == null ? seed
                : com.google.common.hash.Hashing.sha256()
                        .hashString(textSeed,
                                java.nio.charset.StandardCharsets.UTF_8)
                        .asLong();
    }

    public String getTextSeed() {
        return textSeed;
    }

    public GeneratedMapSize getSize() {
        return size;
    }

    public GeneratedLandLayout getLayout() {
        return layout;
    }

    public int getWidth() {
        return size == null ? GeneratedWorld.WIDTH : size.getWidth();
    }

    public int getHeight() {
        return size == null ? GeneratedWorld.HEIGHT : size.getHeight();
    }

    private final GeneratedClimate climate;

    private final GeneratedLandShare landShare;

    public GeneratedWorldSettings(final int seed,
            final GeneratedClimate climate,
            final GeneratedLandShare landShare) {
        Preconditions.checkArgument(seed >= 0 && seed <= MAX_SEED,
                "Seed must be from 0 to %s", MAX_SEED);
        this.seed = seed;
        this.climate = Preconditions.checkNotNull(climate, "Climate is null");
        this.landShare = Preconditions.checkNotNull(landShare,
                "Land share is null");
    }

    /**
     * Read a seed typed by the player. Surrounding Unicode whitespace is
     * ignored; the rest must be between one and nine ASCII digits.
     *
     * @param text required text of the seed field
     * @return the seed
     * @throws IllegalArgumentException when the text is not a valid seed
     */
    public static int parseSeed(final String text) {
        Preconditions.checkNotNull(text, "Seed is null");
        final String trimmed = text.strip();
        Preconditions.checkArgument(
                trimmed.length() >= 1 && trimmed.length() <= MAX_SEED_DIGITS
                        && trimmed.chars().allMatch(c -> c >= '0' && c <= '9'),
                "Seed must be a whole number from 0 to %s", MAX_SEED);
        return Integer.parseInt(trimmed);
    }

    public int getSeed() {
        return seed;
    }

    public GeneratedClimate getClimate() {
        return climate;
    }

    public GeneratedLandShare getLandShare() {
        return landShare;
    }

}
