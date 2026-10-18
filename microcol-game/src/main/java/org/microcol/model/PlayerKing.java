package org.microcol.model;

import java.util.Map;
import java.util.Set;

import org.microcol.model.store.PlayerPo;

import com.google.common.base.Preconditions;

/**
 * King player. The Crown has no tax state of its own; it reports and adjusts
 * the single rate of the colonial nation it rules.
 */
public class PlayerKing extends Player {

    /**
     * If it's not null than it's king player.
     */
    private final Player whosKingThisPlayerIs;

    PlayerKing(final String name, final boolean computer, final int initialGold,
            final Model model, final boolean declaredIndependence,
            final Player whosKingThisPlayerIs,
            final Map<String, Object> extraData, Set<Location> visible) {
        super(name, computer, initialGold, model, declaredIndependence,
                extraData, visible, null, null, null);
        this.whosKingThisPlayerIs = Preconditions
                .checkNotNull(whosKingThisPlayerIs);
    }

    /** @return the colonial nation's tax percentage */
    public int getKingsTaxPercentage() {
        return getTaxRate();
    }

    /**
     * Controlled-test access to the ruled nation's rate; gameplay changes it
     * only through an answered demand.
     *
     * @param kingsTaxPercentage percentage from 0 to 75
     */
    public void setKingsTaxPercentage(int kingsTaxPercentage) {
        whosKingThisPlayerIs.setTaxRate(kingsTaxPercentage);
    }

    @Override
    public int getTaxRate() {
        return whosKingThisPlayerIs.getTaxRate();
    }

    /**
     * @return the whosKingThisPlayerIs
     */
    public Player getWhosKingThisPlayerIs() {
        return whosKingThisPlayerIs;
    }

    public PlayerPo save() {
        final PlayerPo out = super.save();
        out.setWhosKingThisPlayerIs(whosKingThisPlayerIs.getName());
        return out;
    }

}
