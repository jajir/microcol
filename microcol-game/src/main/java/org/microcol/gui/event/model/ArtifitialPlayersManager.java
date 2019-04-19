package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingRobotPlayer;
import org.microcol.ai.SimpleAiPlayer;
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

    private List<AbstractRobotPlayer> players = null;

    @Inject
    ArtifitialPlayersManager(final AnimationManager animationManager) {
        this.animationManager = Preconditions.checkNotNull(animationManager);
    }

    void initRobotPlayers(final Model model) {
        players = new ArrayList<>();
        model.getPlayers().stream().filter(player -> player.isKing())
                .map(player -> (PlayerKing) player).forEach(player -> {
                    players.add(new KingRobotPlayer(model, player, animationManager));
                });
        model.getPlayers().stream().filter(player -> isOpponent(player)).forEach(player -> {
            players.add(new SimpleAiPlayer(model, player, animationManager));
        });
    }

    private boolean isOpponent(final Player player) {
        return !player.isKing() && player.isComputer();
    }

    void destroyRobotPlayers() {
        players.forEach(player -> player.stop());
        players = null;
    }

    public void suspendAi() {
        players.forEach(player -> player.suspend());
    }

    public void resumeAi() {
        players.forEach(player -> player.resume());
    }

}
