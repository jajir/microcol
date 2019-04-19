package org.microcol.model;

import java.util.Map;
import java.util.Set;

import org.microcol.model.store.PlayerPo;

import com.google.common.base.Preconditions;

/**
 * King player.
 */
public class PlayerKing extends Player {

    private int kingsTaxPercentage;

    /**
     * If it's not null than it's king player.
     */
    private final Player whosKingThisPlayerIs;

    PlayerKing(final String name, final boolean computer, final int initialGold, final Model model,
            final boolean declaredIndependence, final Player whosKingThisPlayerIs,
            final Map<String, Object> extraData, Set<Location> visible,
            final int kingsTaxPercentage) {
        super(name, computer, initialGold, model, declaredIndependence, extraData, visible);
        this.whosKingThisPlayerIs = Preconditions.checkNotNull(whosKingThisPlayerIs);
        this.kingsTaxPercentage = kingsTaxPercentage;
    }

    public int getKingsTaxPercentage() {
        return kingsTaxPercentage;
    }

    public void setKingsTaxPercentage(int kingsTaxPercentage) {
        this.kingsTaxPercentage = kingsTaxPercentage;
    }

    /**
     * @return the whosKingThisPlayerIs
     */
    public Player getWhosKingThisPlayerIs() {
        return whosKingThisPlayerIs;
    }

    public PlayerPo save() {
        final PlayerPo out = super.save();
        out.setKingsTaxPercentage(kingsTaxPercentage);
        out.setWhosKingThisPlayerIs(whosKingThisPlayerIs.getName());
        return out;
    }

}
