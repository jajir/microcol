package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Persisted notice that a hostile patrol slowed or stopped one of the owner's
 * ships on a legal step. It keeps the selected patrol for the record, but the
 * player sees only the result. An undetected passage records nothing, so the
 * notice never discloses a hidden patrol.
 */
public final class TurnEventPatrolInterception extends AbstractTurnEvent {

    private final int patrolId;
    private final boolean stopped;

    public TurnEventPatrolInterception(final String playerName,
            final int patrolId, final boolean stopped) {
        super(playerName);
        Preconditions.checkArgument(patrolId >= 0,
                "Invalid patrol interception event");
        this.patrolId = patrolId;
        this.stopped = stopped;
    }

    /** @return ID of the patrol selected for the check */
    public int getPatrolId() {
        return patrolId;
    }

    /**
     * @return whether the ship lost all remaining movement, not just one point
     */
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setPatrolId(patrolId);
        po.setPatrolStopped(stopped);
        return po;
    }

    public static TurnEventPatrolInterception tryLoad(final TurnEventPo po) {
        if (!TurnEventPatrolInterception.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getPatrolId() != null && po.isPatrolStopped() != null,
                "Incomplete patrol interception event");
        return new TurnEventPatrolInterception(po.getPlayerName(),
                po.getPatrolId(), po.isPatrolStopped());
    }
}
