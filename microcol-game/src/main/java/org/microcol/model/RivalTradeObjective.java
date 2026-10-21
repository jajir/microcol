package org.microcol.model;

import org.microcol.model.store.RivalTradeObjectivePo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * A rival colonial player's single supply cycle: one colony, one carrier and
 * the leg the carrier is on. Both identities are stored as plain IDs. The owner
 * checks them against live ownership before every step, so a destroyed or
 * captured object retires instead of being used.
 */
public final class RivalTradeObjective {

    /** Destination of the carrier's current leg. */
    public enum Stage {
        /** Carrying the colony's exportable lumber to Europe. */
        TO_EUROPE,
        /** Carrying the Europe purchases back to the colony. */
        TO_COLONY
    }

    private final int colonyId;

    private final int carrierId;

    private final Stage stage;

    /**
     * @param colonyId  required identity of the colony that supplies the cycle
     * @param carrierId required identity of the ship that carries the cargo
     * @param stage     required destination of the current leg
     */
    public RivalTradeObjective(final int colonyId, final int carrierId,
            final Stage stage) {
        this.colonyId = colonyId;
        this.carrierId = carrierId;
        this.stage = Preconditions.checkNotNull(stage, "Stage is null");
    }

    /**
     * Restore a saved objective. Missing identities or an unknown stage are
     * rejected instead of guessed; stale identities are kept for retirement.
     *
     * @param po required saved objective
     * @return the restored objective
     */
    static RivalTradeObjective load(final RivalTradeObjectivePo po) {
        Preconditions.checkArgument(
                po.getColonyId() != null && po.getCarrierId() != null
                        && po.getStage() != null,
                "Rival trade objective identity is missing");
        return new RivalTradeObjective(po.getColonyId(), po.getCarrierId(),
                Stage.valueOf(po.getStage()));
    }

    RivalTradeObjectivePo save() {
        final RivalTradeObjectivePo out = new RivalTradeObjectivePo();
        out.setColonyId(colonyId);
        out.setCarrierId(carrierId);
        out.setStage(stage.name());
        return out;
    }

    public int getColonyId() {
        return colonyId;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * @param next required destination of the next leg, same colony and carrier
     * @return the objective moved to that leg
     */
    public RivalTradeObjective withStage(final Stage next) {
        return new RivalTradeObjective(colonyId, carrierId, next);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("colonyId", colonyId)
                .add("carrierId", carrierId).add("stage", stage).toString();
    }
}
