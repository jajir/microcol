package org.microcol.ai;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.inject.ImplementedBy;

/**
 * Creates the robot that plays one European computer rival. Royal and native
 * robots are not created here. The default is the real {@link SimpleAiPlayer};
 * a functional test may bind another implementation in its Guice override
 * module to keep unrelated rivals out of its route.
 */
@ImplementedBy(StandardRivalPlayerFactory.class)
public interface RivalPlayerFactory {

    /**
     * @param model         required running model
     * @param player        required European computer rival
     * @param animationLock required animation slot shared by all robots
     * @return the started robot controlling the rival
     */
    AbstractRobotPlayer createRival(Model model, Player player,
            AnimationLock animationLock);
}
