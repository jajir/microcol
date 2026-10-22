package org.microcol.model;

import java.util.Objects;

/**
 * Immutable terms of a native mission (R-0164): the owner who holds it, whether
 * its missionary acts as an expert, the owner's current attitude of the
 * settlement and the nonhostile updates counted so far. The chooser shows the
 * terms of a mission to establish; the settlement view shows an established
 * one. Equal terms mean the same mission, so a stale confirmation can be
 * recognized.
 */
public final class NativeMissionView {

    /** Nonhostile owner updates of one conversion cycle. */
    public static final int REQUIRED_UPDATES = 5;

    private final EuropeanNation owner;

    private final String ownerName;

    private final boolean effectiveExpert;

    private final NativeHostility hostility;

    private final int completedUpdates;

    NativeMissionView(final EuropeanNation owner, final String ownerName,
            final boolean effectiveExpert, final NativeHostility hostility,
            final int completedUpdates) {
        this.owner = Objects.requireNonNull(owner);
        this.ownerName = Objects.requireNonNull(ownerName);
        this.effectiveExpert = effectiveExpert;
        this.hostility = Objects.requireNonNull(hostility);
        this.completedUpdates = completedUpdates;
    }

    /**
     * @return the colonial nation that holds the mission
     */
    public EuropeanNation getOwner() {
        return owner;
    }

    /**
     * @return the display name of the owner, as the game shows it
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @return whether the missionary acts as an expert now, from a Jesuit or
     *         from Jean de Brebeuf's live membership
     */
    public boolean isEffectiveExpert() {
        return effectiveExpert;
    }

    /**
     * @return the owner's current attitude of the settlement
     */
    public NativeHostility getHostility() {
        return hostility;
    }

    /**
     * @return nonhostile updates counted in the current conversion cycle
     */
    public int getCompletedUpdates() {
        return completedUpdates;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NativeMissionView)) {
            return false;
        }
        final NativeMissionView that = (NativeMissionView) other;
        return owner == that.owner && ownerName.equals(that.ownerName)
                && effectiveExpert == that.effectiveExpert
                && hostility == that.hostility
                && completedUpdates == that.completedUpdates;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, ownerName, effectiveExpert, hostility,
                completedUpdates);
    }

}
