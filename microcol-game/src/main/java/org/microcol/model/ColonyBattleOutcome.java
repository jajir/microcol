package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * What a colony battle did, so the player can tell a lost colony from lost
 * equipment. Carried by the attack event next to the colony center.
 */
public final class ColonyBattleOutcome {

    /** Result of one colony battle. */
    public enum Result {
        /** An armed defender lost its equipment; the colony still stands. */
        DEFENDER_EQUIPMENT_LOST,
        /** The attacker lost its equipment; the colony stands. */
        ATTACKER_EQUIPMENT_LOST,
        /** The attacker took the whole colony with all its workers. */
        COLONY_CAPTURED,
        /** The attacker took one unarmed field worker; the colony stands. */
        WORKER_CAPTURED
    }

    private final Colony colony;
    private final Location battlefield;
    private final ColonyDefense.Kind kind;
    private final Result result;
    private final boolean militiaExited;

    ColonyBattleOutcome(final Colony colony, final Location battlefield,
            final ColonyDefense.Kind kind, final Result result,
            final boolean militiaExited) {
        this.colony = Preconditions.checkNotNull(colony);
        this.battlefield = Preconditions.checkNotNull(battlefield);
        this.kind = Preconditions.checkNotNull(kind);
        this.result = Preconditions.checkNotNull(result);
        this.militiaExited = militiaExited;
    }

    public Colony getColony() {
        return colony;
    }

    /** @return the explicit tile where the battle took place */
    public Location getBattlefield() {
        return battlefield;
    }

    public ColonyDefense.Kind getKind() {
        return kind;
    }

    public Result getResult() {
        return result;
    }

    /**
     * @return whether a victorious emergency militiaman left its job to join
     *         the outside garrison
     */
    public boolean isMilitiaExited() {
        return militiaExited;
    }
}
