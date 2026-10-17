package org.microcol.model.unit;

import java.util.Optional;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.store.UnitActionClearForestPo;
import org.microcol.model.store.UnitActionPo;
import org.microcol.model.turnevent.TurnEventProvider;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Describe clear forest action. Its completion is the single transaction that
 * removes the forest, spends the tools and delivers or discards the lumber.
 */
public final class UnitActionClearForest implements UnitAction {

    /** Owner updates needed by an ordinary pioneer. */
    public static final int REQUIRED_TURNS_TO_CLEAR_FOREST = 4;

    /** Owner updates needed by a hardy pioneer. */
    public static final int HARDY_TURNS_TO_CLEAR_FOREST = 2;

    /** Tools consumed once when the forest is removed. */
    public static final int REQUIRED_TOOLS = 20;

    /** Lumber yielded by one cleared forest before warehouse limits. */
    public static final int LUMBER_YIELD = 20;

    /** Largest eight-neighbor distance from the forest to a recipient. */
    public static final int RECIPIENT_RADIUS = 2;

    private int remainingTurns;

    /** Set once the work completed or became stale; it never revives. */
    private boolean finished;

    /**
     * Create instance and initialize number of remaining turns.
     */
    public UnitActionClearForest() {
        remainingTurns = REQUIRED_TURNS_TO_CLEAR_FOREST;
    }

    @Override
    public UnitActionType getType() {
        return UnitActionType.clearForest;
    }

    /**
     * Get remaining number of turns.
     *
     * @return number of turns
     */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    /**
     * Allows to set how many turns more will be action executed.
     *
     * @param remainingTurns remaining number of turn
     */
    public void setRemainingTurns(final int remainingTurns) {
        Preconditions.checkArgument(
                remainingTurns >= 1
                        && remainingTurns <= REQUIRED_TURNS_TO_CLEAR_FOREST,
                "Invalid clear forest countdown %s", remainingTurns);
        this.remainingTurns = remainingTurns;
    }

    @Override
    public UnitActionPo save() {
        final UnitActionClearForestPo out = new UnitActionClearForestPo();
        out.setRemainingTurns(remainingTurns);
        return out;
    }

    @Override
    public boolean startTurn(final Model model, final AbstractUnit unit) {
        // A callback from the wrong model or unit, or for a canceled or
        // replaced order, changes nothing, not even this order's lifecycle.
        if (unit.getAction() != this
                || model.tryGetUnitById(unit.getId()).orElse(null) != unit) {
            return true;
        }
        if (finished) {
            return true;
        }
        unit.setActionPoints(0);
        if (!isTargetStillValid(model, unit)) {
            // Stale work ends without terrain change, tool charge or lumber.
            finished = true;
            return true;
        }
        remainingTurns--;
        if (remainingTurns > 0) {
            return false;
        }
        finished = true;
        complete(model, (UnitFreeColonist) unit);
        return true;
    }

    private void complete(final Model model, final UnitFreeColonist pioneer) {
        final Location at = pioneer.getLocation();
        pioneer.consumeWorkTools(REQUIRED_TOOLS);
        model.getMap().clearForest(at);
        // The recipient is resolved from the world as it is now; nothing the
        // player was shown before is a commitment.
        final Optional<Colony> recipient = model
                .getForestClearingRecipient(pioneer.getOwner(), at);
        final Goods lumber = Goods.of(GoodsType.LUMBER, LUMBER_YIELD);
        final Goods lost;
        if (recipient.isPresent()) {
            final Colony colony = recipient.get();
            final int stored = colony.getWarehouse().getGoods(GoodsType.LUMBER)
                    .getAmount();
            final int capacity = colony.getWarehouse()
                    .getStorageCapacity(GoodsType.LUMBER).getAmount();
            // A warehouse that already holds its limit takes nothing.
            lost = stored >= capacity ? lumber
                    : colony.getWarehouse().addGoodsWithThrowingAway(lumber,
                            null);
        } else {
            lost = lumber;
        }
        if (lost.isNotZero()) {
            model.getTurnEventStore().add(TurnEventProvider.getForestLumberLost(
                    pioneer.getOwner(), lost, at, recipient.orElse(null)));
        }
    }

    private boolean isTargetStillValid(final Model model,
            final AbstractUnit unit) {
        if (!(unit instanceof UnitFreeColonist) || !unit.isAtPlaceLocation()
                || ((UnitFreeColonist) unit).getTools() < REQUIRED_TOOLS
                || model.getColonyAt(unit.getLocation()).isPresent()) {
            return false;
        }
        final Terrain terrain = model.getMap().getTerrainAt(unit.getLocation());
        return terrain.isHasTrees();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("remainingTurns", remainingTurns).toString();
    }

}
