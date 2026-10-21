package org.microcol.model.generation;

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
