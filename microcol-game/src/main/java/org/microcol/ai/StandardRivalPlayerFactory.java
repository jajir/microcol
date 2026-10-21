package org.microcol.ai;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Model;
import org.microcol.model.Player;

/**
 * Production choice: every European computer rival is played by the real
 * {@link SimpleAiPlayer}.
 */
public final class StandardRivalPlayerFactory implements RivalPlayerFactory {

    @Override
    public AbstractRobotPlayer createRival(final Model model,
            final Player player, final AnimationLock animationLock) {
        return new SimpleAiPlayer(model, player, animationLock);
    }
}
