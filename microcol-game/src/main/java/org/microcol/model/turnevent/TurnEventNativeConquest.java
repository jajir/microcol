package org.microcol.model.turnevent;

import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Durable record of one conquered native settlement (R-0174). It is created
 * once, in the same committed attack that removes the settlement, and it is
 * saved with the game. The treasure step (R-0175) reads it exactly once; it
 * carries the identity, owner and location of the settlement and the number of
 * defenders overcome.
 */
public final class TurnEventNativeConquest extends AbstractTurnEvent {

    private final int settlementId;
    private final NativeNation nation;
    private final Location location;
    private final boolean capital;
    private final int defendersDefeated;

    TurnEventNativeConquest(final String playerName, final int settlementId,
            final NativeNation nation, final Location location,
            final boolean capital, final int defendersDefeated) {
        super(playerName);
        Preconditions.checkArgument(
                nation != null && location != null && defendersDefeated > 0,
                "Invalid native conquest record");
        this.settlementId = settlementId;
        this.nation = nation;
        this.location = location;
        this.capital = capital;
        this.defendersDefeated = defendersDefeated;
    }

    /** @return stable ID of the conquered settlement */
    public int getSettlementId() {
        return settlementId;
    }

    /** @return nation the settlement belonged to */
    public NativeNation getNation() {
        return nation;
    }

    /** @return square the settlement stood on */
    public Location getLocation() {
        return location;
    }

    /** @return whether the conquered settlement was its nation's capital */
    public boolean isCapital() {
        return capital;
    }

    /** @return number of actual defenders overcome in the attack */
    public int getDefendersDefeated() {
        return defendersDefeated;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setNativeConquestSettlementId(settlementId);
        po.setNativeConquestNation(nation.name());
        po.setNativeConquestX(location.getX());
        po.setNativeConquestY(location.getY());
        po.setNativeConquestCapital(capital);
        po.setNativeConquestDefeated(defendersDefeated);
        return po;
    }

    public static TurnEventNativeConquest tryLoad(final TurnEventPo po) {
        if (!TurnEventNativeConquest.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getNativeConquestSettlementId() != null
                        && po.getNativeConquestNation() != null
                        && po.getNativeConquestX() != null
                        && po.getNativeConquestY() != null
                        && po.getNativeConquestCapital() != null
                        && po.getNativeConquestDefeated() != null,
                "Incomplete native conquest record");
        return new TurnEventNativeConquest(po.getPlayerName(),
                po.getNativeConquestSettlementId(),
                NativeNation.valueOf(po.getNativeConquestNation()),
                Location.of(po.getNativeConquestX(), po.getNativeConquestY()),
                po.getNativeConquestCapital(), po.getNativeConquestDefeated());
    }
}
