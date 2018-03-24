package org.microcol.ai;

import java.util.function.Consumer;

import org.microcol.gui.gamepanel.AnimationLock;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Base class for robot players AI or king.
 */
public abstract class AbstractRobotPlayer {

    private final Logger logger = LoggerFactory.getLogger(AbstractRobotPlayer.class);

    private final Model model;

    private final ModelListenerAdapter modelAdapter;

    private final AnimationLock animationLock;

    /**
     * Controlled player.
     */
    private final Player player;

    private boolean running;

    public AbstractRobotPlayer(final Model model, final Player player,
            final AnimationLock animationLock) {
        this.model = Preconditions.checkNotNull(model);
        this.player = Preconditions.checkNotNull(player);
        this.animationLock = Preconditions.checkNotNull(animationLock);
        this.modelAdapter = new RobotModelListener(this::onTurnStarted);
        model.addListener(modelAdapter);
        running = true;
        logger.info("Robot player started.");
    }

    private void onTurnStarted(final TurnStartedEvent event) {
        if (event.getPlayer().equals(player)) {
            turn(event.getPlayer());
        }
    }

    public void stop() {
        model.removeListener(modelAdapter);
    }

    public void suspend() {
        running = false;
    }

    public void resume() {
        running = true;
        if (model.isGameRunning() && model.getCurrentPlayer().isComputer()) {
            turn(model.getCurrentPlayer());
        }
    }

    private void turn(final Player player) {
        if (!running) {
            return;
        }
        turnStarted();
        player.getUnits().stream().filter(unit -> unit.isAtPlaceLocation())
                .forEach(unit -> move(unit));
        animationLock.waitWhileRunning();
        logger.info("Robot finish move for player {}", player);
        player.endTurn();
    }

    private void move(final Unit unit) {
        if (!running) {
            return;
        }
        moveUnit(unit);
        animationLock.waitWhileRunning();
    }

    /**
     * When AI need to know that turn started than should override this method.
     */
    abstract void turnStarted();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("model", model)
                .add("animationLock", animationLock).add("running", running).toString();
    }

    abstract void moveUnit(final Unit unit);

    /**
     * @return the model
     */
    protected Model getModel() {
        return model;
    }

    /**
     * @return the player
     */
    protected Player getPlayer() {
        return player;
    }

    private class RobotModelListener extends ModelListenerAdapter {

        private final Consumer<TurnStartedEvent> onTurnStarted;

        RobotModelListener(final Consumer<TurnStartedEvent> onTurnStarted) {
            this.onTurnStarted = Preconditions.checkNotNull(onTurnStarted);
        }

        @Override
        public void turnStarted(final TurnStartedEvent event) {
            onTurnStarted.accept(event);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper("AbstractRobotPlayer.TurnStartedEventListener")
                    .toString();
        }

    };

}
