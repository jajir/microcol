package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingRobotPlayer;
import org.microcol.ai.NativeRobotPlayer;
import org.microcol.ai.RivalPlayerFactory;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class manage artificial players. It creates them wire them to model events
 * and destroy then.
 */
public final class ArtifitialPlayersManager {

    private final AnimationManager animationManager;

    private final RivalPlayerFactory rivalPlayerFactory;

    private List<AbstractRobotPlayer> players = null;

    @Inject
    ArtifitialPlayersManager(final AnimationManager animationManager,
            final RivalPlayerFactory rivalPlayerFactory) {
        this.animationManager = Preconditions.checkNotNull(animationManager);
        this.rivalPlayerFactory = Preconditions
                .checkNotNull(rivalPlayerFactory);
    }

    void initRobotPlayers(final Model model) {
        players = new ArrayList<>();
        model.getPlayers().stream().filter(player -> player.isKing())
                .map(player -> (PlayerKing) player).forEach(player -> {
                    players.add(new KingRobotPlayer(model, player,
                            animationManager));
                });
        model.getPlayers().stream().filter(player -> player.isNative())
                .forEach(player -> {
                    players.add(new NativeRobotPlayer(model, player,
                            animationManager));
                });
        model.getPlayers().stream().filter(player -> isOpponent(player))
                .forEach(player -> {
                    players.add(rivalPlayerFactory.createRival(model, player,
                            animationManager));
                });
    }

    private boolean isOpponent(final Player player) {
        return !player.isKing() && !player.isNative() && player.isComputer();
    }

    void destroyRobotPlayers() {
        players.forEach(player -> player.stop());
        players = null;
    }

}
