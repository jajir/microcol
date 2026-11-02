package org.akropia.ai;

import org.akropia.gui.screen.game.gamepanel.AnimationLock;
import org.akropia.model.Model;
import org.akropia.model.Player;

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
