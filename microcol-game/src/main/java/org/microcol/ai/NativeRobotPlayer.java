package org.microcol.ai;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

/**
 * Native nation's own turn. It takes the shared callback and ends at once
 * without an action: raid, patrol and trade policy belong to later
 * requirements.
 */
public final class NativeRobotPlayer extends AbstractRobotPlayer {

    public NativeRobotPlayer(final Model model, final Player player,
            final AnimationLock animationLock) {
        super(model, player, animationLock);
    }

    @Override
    void turnStarted() {
        // No native action policy yet; the turn ends with no action.
    }

    @Override
    void moveUnit(final Unit unit) {
        // No native movement policy yet; braves stay where they are.
    }

}
