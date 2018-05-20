package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingPlayer;
import org.microcol.ai.SimpleAiPlayer;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class manage artificial players. It creates them wire them to model events
 * and destroy then.
 */
public class ArtifitialPlayersManager {

    private final AnimationManager animationManager;

    private List<AbstractRobotPlayer> players = null;

    @Inject
    ArtifitialPlayersManager(final AnimationManager animationManager) {
        this.animationManager = Preconditions.checkNotNull(animationManager);
    }

    void initRobotPlayers(final Model model) {
        players = new ArrayList<>();
        model.getPlayers().stream().filter(player -> player.isKing()).forEach(player -> {
            players.add(new KingPlayer(model, player, animationManager));
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
