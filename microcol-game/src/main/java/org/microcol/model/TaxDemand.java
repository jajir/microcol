package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Immutable snapshot of one Crown tax demand: the current and proposed rate and
 * the exact protest consequence. The model confirms or rejects a snapshot
 * against the live state, so a stale view never changes anything.
 */
public final class TaxDemand {

    private final Player player;

    private final int serial;

    private final int currentRate;

    private final int proposedRate;

    private final TaxProtest protest;

    TaxDemand(final Player player, final int serial, final int currentRate,
            final int proposedRate, final TaxProtest protest) {
        this.player = Preconditions.checkNotNull(player);
        this.serial = serial;
        this.currentRate = currentRate;
        this.proposedRate = proposedRate;
        this.protest = protest;
    }

    /** @return the colonial nation that must answer */
    public Player getPlayer() {
        return player;
    }

    /** @return position of this demand among those issued to the nation */
    public int getSerial() {
        return serial;
    }

    public int getCurrentRate() {
        return currentRate;
    }

    public int getProposedRate() {
        return proposedRate;
    }

    /** @return protest consequence, or empty when no stock is eligible */
    public Optional<TaxProtest> getProtest() {
        return Optional.ofNullable(protest);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TaxDemand)) {
            return false;
        }
        final TaxDemand other = (TaxDemand) object;
        return player == other.player && serial == other.serial
                && currentRate == other.currentRate
                && proposedRate == other.proposedRate
                && Objects.equals(protest, other.protest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getName(), serial, currentRate, proposedRate,
                protest);
    }

    @Override
    public String toString() {
        return "TaxDemand[" + player.getName() + " #" + serial + " "
                + currentRate + "% -> " + proposedRate + "%, " + protest + "]";
    }
}
