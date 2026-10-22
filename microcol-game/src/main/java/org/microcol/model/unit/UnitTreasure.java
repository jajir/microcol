package org.microcol.model.unit;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Treasure train left by a conquered native settlement (R-0175). The unit
 * belongs to the conqueror, stands on the map and keeps its face value itself;
 * the owner's gold is not changed by its creation. It has no combat strength
 * and cannot start a fight. An enemy that defeats it takes nothing, because the
 * value is lost with the train. A ship stores it only in six free holds, which
 * the train reserves as one unit (R-0134).
 */
public final class UnitTreasure extends AbstractUnit {

    /** Face value in gold, always positive. */
    private final int value;
    private Integer pickupColonyId;
    private Integer pickupNet;

    /**
     * Confirmed royal collection survives Save/Continue until the next owner
     * update.
     */
    public void setPickup(final Integer colonyId, final Integer net) {
        Preconditions.checkArgument((colonyId == null) == (net == null),
                "Incomplete treasure pickup");
        Preconditions.checkArgument(colonyId == null
                || colonyId >= 0 && net >= 0 && net <= value - value / 2,
                "Invalid treasure pickup");
        pickupColonyId = colonyId;
        pickupNet = net;
    }

    public Integer getPickupColonyId() {
        return pickupColonyId;
    }

    public Integer getPickupNet() {
        return pickupNet;
    }

    public UnitTreasure(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action,
            final Integer value) {
        super(model, id, placeBuilder, owner, availableMoves, action);
        Preconditions.checkArgument(value != null && value > 0,
                "Treasure train needs a positive face value");
        this.value = value;
    }

    @Override
    public UnitType getType() {
        return UnitType.TREASURE_TRAIN;
    }

    @Override
    public boolean isShip() {
        return false;
    }

    /**
     * Only waiting is possible: a train may stand idle or on sentry, but it
     * cannot work, equip, plow or clear for another order.
     */
    @Override
    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.noAction, UnitActionType.sentry);
    }

    /**
     * @return the face value in gold that the train carries
     */
    public int getValue() {
        return value;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setTreasureValue(value);
        out.setTreasurePickupColonyId(pickupColonyId);
        out.setTreasurePickupNet(pickupNet);
        return out;
    }

    /** A treasure train has no combat strength of its own. */
    @Override
    public double getMilitaryStrenght() {
        return 0;
    }

    /**
     * A defeated train is destroyed with its value, and nothing is paid out.
     */
    @Override
    protected boolean isDestroyedWhenDefeated() {
        return true;
    }

}
