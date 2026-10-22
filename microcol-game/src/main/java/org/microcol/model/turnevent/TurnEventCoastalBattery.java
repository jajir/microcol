package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Persisted notice that a coastal battery fired at one of the owner's ships
 * after a legal step (R-0177). A hit only reports the damage; any repair or
 * sinking that follows is reported by the ordinary ship repair notices. The
 * notice names only the battery and the ship by their IDs and the result.
 */
public final class TurnEventCoastalBattery extends AbstractTurnEvent {

    private final int colonyId;
    private final int shipId;
    private final boolean hit;

    public TurnEventCoastalBattery(final String playerName, final int colonyId,
            final int shipId, final boolean hit) {
        super(playerName);
        Preconditions.checkArgument(colonyId >= 0 && shipId >= 0,
                "Invalid coastal battery event");
        this.colonyId = colonyId;
        this.shipId = shipId;
        this.hit = hit;
    }

    /** @return ID of the colony whose battery fired */
    public int getColonyId() {
        return colonyId;
    }

    /** @return ID of the ship that was fired at */
    public int getShipId() {
        return shipId;
    }

    /** @return whether the shot hit and damaged the ship */
    public boolean isHit() {
        return hit;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setCoastalColonyId(colonyId);
        po.setCoastalShipId(shipId);
        po.setCoastalHit(hit);
        return po;
    }

    public static TurnEventCoastalBattery tryLoad(final TurnEventPo po) {
        if (!TurnEventCoastalBattery.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getCoastalColonyId() != null && po.getCoastalShipId() != null
                        && po.isCoastalHit() != null,
                "Incomplete coastal battery event");
        return new TurnEventCoastalBattery(po.getPlayerName(),
                po.getCoastalColonyId(), po.getCoastalShipId(),
                po.isCoastalHit());
    }
}
