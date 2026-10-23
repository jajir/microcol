package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved one-time notice of the foreign-intervention target, set at the first
 * rebel update, or of its earning (R-0189).
 */
public final class TurnEventIntervention extends AbstractTurnEvent {
    private final int target;
    private final boolean earned;

    TurnEventIntervention(final String playerName, final int target,
            final boolean earned) {
        super(playerName);
        Preconditions.checkArgument(target > 0, "Invalid intervention notice");
        this.target = target;
        this.earned = earned;
    }

    public int getTarget() {
        return target;
    }

    /**
     * @return whether this notice reports the earning rather than the target
     */
    public boolean isEarned() {
        return earned;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setInterventionTarget(target);
        po.setInterventionEarned(earned);
        return po;
    }

    public static TurnEventIntervention tryLoad(final TurnEventPo po) {
        if (!TurnEventIntervention.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getInterventionTarget() != null
                        && po.getInterventionTarget() > 0
                        && po.getInterventionEarned() != null,
                "Incomplete intervention notice");
        return new TurnEventIntervention(po.getPlayerName(),
                po.getInterventionTarget(), po.getInterventionEarned());
    }
}
