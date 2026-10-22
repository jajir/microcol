package org.microcol.model;

import java.util.Objects;

/**
 * Terms of a chief visit shown before the player confirms it (R-0158). Asking
 * for it draws nothing. A repeat visit repeats the information without a draw,
 * so its chances are information only. Equal previews name the same terms, so
 * the model accepts a confirmation only for the terms the player was shown.
 */
public final class NativeChiefPreview {

    private final NativeHostility hostility;

    private final boolean seasoned;

    private final boolean repeat;

    private final int informationPerMille;

    private final int goldPerMille;

    private final int deathPerMille;

    private NativeChiefPreview(final NativeHostility hostility,
            final boolean seasoned, final boolean repeat) {
        this.hostility = hostility;
        this.seasoned = seasoned;
        this.repeat = repeat;
        if (repeat) {
            informationPerMille = NativeChiefRules.TOTAL;
            goldPerMille = 0;
            deathPerMille = 0;
        } else {
            informationPerMille = NativeChiefRules
                    .informationPerMille(hostility, seasoned);
            goldPerMille = NativeChiefRules.goldPerMille(hostility);
            deathPerMille = NativeChiefRules.deathPerMille(hostility, seasoned);
        }
    }

    /**
     * @param hostility required mood of the settlement toward the visitor
     * @param seasoned  whether the visitor is a seasoned scout
     * @param repeat    whether this nation already had its encounter here
     * @return the terms of the visit
     */
    static NativeChiefPreview of(final NativeHostility hostility,
            final boolean seasoned, final boolean repeat) {
        return new NativeChiefPreview(Objects.requireNonNull(hostility),
                seasoned, repeat);
    }

    /**
     * @return mood of the settlement toward the visitor's nation
     */
    public NativeHostility getHostility() {
        return hostility;
    }

    /**
     * @return whether the visitor is a seasoned scout
     */
    public boolean isSeasoned() {
        return seasoned;
    }

    /**
     * @return whether the nation already met the chief here, so no draw is made
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * @return per-mille chance of information, all of it on a repeat visit
     */
    public int getInformationPerMille() {
        return informationPerMille;
    }

    /**
     * @return per-mille chance of gold, zero on a repeat visit
     */
    public int getGoldPerMille() {
        return goldPerMille;
    }

    /**
     * @return per-mille chance of death, zero on a repeat visit
     */
    public int getDeathPerMille() {
        return deathPerMille;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NativeChiefPreview)) {
            return false;
        }
        final NativeChiefPreview that = (NativeChiefPreview) other;
        return hostility == that.hostility && seasoned == that.seasoned
                && repeat == that.repeat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostility, seasoned, repeat);
    }

}
