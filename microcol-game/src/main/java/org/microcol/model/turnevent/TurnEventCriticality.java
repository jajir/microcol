package org.microcol.model.turnevent;

import com.google.common.base.Preconditions;

/**
 * Shared rule which saved turn notices must stay visible for the human player
 * even when the routine turn summary is switched off. Classification only reads
 * the notice; it neither changes the notice nor the game.
 */
public final class TurnEventCriticality {

    private TurnEventCriticality() {
    }

    /**
     * @param turnEvent required saved turn notice
     * @return <code>true</code> when the notice reports famine or a lost or
     *         destroyed colony
     */
    public static boolean isCritical(final TurnEvent turnEvent) {
        Preconditions.checkNotNull(turnEvent);
        return turnEvent instanceof TurnEventFaminePlagueColony
                || turnEvent instanceof TurnEventFamineWillPlagueColony
                || turnEvent instanceof TurnEventColonyWasLost
                || turnEvent instanceof TurnEventColonyWasDestroyed;
    }

}
