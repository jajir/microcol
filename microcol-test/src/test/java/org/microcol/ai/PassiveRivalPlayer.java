package org.microcol.ai;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

/**
 * Test replacement of a European computer rival that takes its turn without any
 * action. It uses the ordinary robot lifecycle, so the model still prepares and
 * ends the rival's turn; the rival only never moves, attacks, founds or trades,
 * so it cannot stand on a square a functional test sails through. It lives in
 * the robot package because the robot base class is package-private.
 */
public final class PassiveRivalPlayer extends AbstractRobotPlayer {

    public PassiveRivalPlayer(final Model model, final Player player,
            final AnimationLock animationLock) {
        super(model, player, animationLock);
    }

    @Override
    void turnStarted() {
        // No action: the base class ends the turn.
    }

    @Override
    void moveUnit(final Unit unit) {
        // Units of a passive rival stay where they are.
    }
}
