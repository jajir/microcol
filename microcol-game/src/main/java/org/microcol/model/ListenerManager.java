package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.event.UnitMovedStepEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

class ListenerManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<ModelListener> listeners;

    ListenerManager() {
        listeners = new ArrayList<>();
    }

    void addListener(final ModelListener listener) {
        Preconditions.checkNotNull(listener);

        if (!listeners.contains(listener)) {
            logger.info("Added model listener {}.", listener);
            listeners.add(listener);
        } else {
            logger.warn("Model listener {} already added.", listener);
        }
    }

    void removeListener(final ModelListener listener) {
        Preconditions.checkNotNull(listener);

        logger.info("Removed model listener {}.", listener);

        listeners.remove(listener);
    }
    
    void removeAllListeners() {
        logger.info("Removed all listeners.");

        List<ModelListener> copy = Lists.newArrayList(listeners);
        copy.forEach(listener -> {
            removeListener(listener);
        });
    }

    void fireGameStarted(final Model model) {
        final GameStartedEvent event = new GameStartedEvent(model);

        logger.info("Game started: {}.", event);

        listeners.forEach(listener -> listener.gameStarted(event));
    }

    void fireRoundStarted(final Model model, final Calendar calendar) {
        final RoundStartedEvent event = new RoundStartedEvent(model, calendar);

        logger.info("Round started: {}.", event);

        executeInSeparateThread(listener -> listener.roundStarted(event));
    }

    void fireTurnStarted(final Model model, final Player player) {
        final TurnStartedEvent event = new TurnStartedEvent(model, player);

        logger.info("Turn started: {}.", event);

        executeInSeparateThread(listener -> listener.turnStarted(event));
    }

    void fireUnitMovedStep(final Model model, final Unit unit, final Location start,
            final Location end) {
        final UnitMovedStepEvent event = new UnitMovedStepEvent(model, unit, start, end);

        logger.info("Unit moved: {}.", event);

        executeInSameThread(listener -> listener.unitMovedStep(event));
    }

    void fireUnitMovedFinished(final Model model, final Unit unit, final Path path) {
        final UnitMoveFinishedEvent event = new UnitMoveFinishedEvent(model, unit, path);

        logger.info("Unit moved: {}.", event);

        executeInSameThread(listener -> listener.unitMoveFinished(event));
    }

    void fireUnitMovedToHighSeas(final Model model, final Unit unit) {
        final UnitMovedToHighSeasEvent event = new UnitMovedToHighSeasEvent(model, unit);

        logger.info("Unit moved to high seas: {}.", event);

        executeInSameThread(listener -> listener.unitMovedToHighSeas(event));
    }

    boolean fireBeforeEndTurn(final Model model) {
        final BeforeEndTurnEvent event = new BeforeEndTurnEvent(model);

        logger.info("Before End Turn: {}.", event);

        return evaluateInSameThread(listener -> {
            listener.beforeEndTurn(event);
            return event.isStopped();
        });
    }

    /**
     *
     * @param model
     *            required model
     * @param unit
     *            required moved unit
     * @param path
     *            required path which unit will move
     * @return When return <code>true</code> than event processing should
     *         continue otherwise return <code>false</code> and event processing
     *         should be stopped.
     */
    boolean fireUnitMoveStarted(final Model model, final Unit unit, final Path path) {
        final UnitMoveStartedEvent event = new UnitMoveStartedEvent(model, unit, path);

        logger.info("Unit move started: {}.", event);

        return evaluateInSameThread(listener -> {
            listener.unitMoveStarted(event);
            return event.isStopped();
        });
    }

    void fireUnitAttacked(final Model model, final Unit attacker, final Unit defender,
            final Unit destroyed) {
        final UnitAttackedEvent event = new UnitAttackedEvent(model, attacker, defender, destroyed);

        logger.info("Unit attacked: {}.", event);

        executeInSeparateThread(listener -> listener.unitAttacked(event));
    }

    void fireUnitEmbarked(final Model model, final Unit unit, final CargoSlot slot) {
        final UnitEmbarkedEvent event = new UnitEmbarkedEvent(model, unit, slot);

        logger.info("Unit embarked: {}.", event);

        listeners.forEach(listener -> listener.unitEmbarked(event));
    }

    void fireIndependenceWasDeclared(final Model model, final Player whoDecalareIt) {
        final IndependenceWasDeclaredEvent event = new IndependenceWasDeclaredEvent(model,
                whoDecalareIt);

        logger.info("Independence was declared: {}.", event);

        listeners.forEach(listener -> listener.independenceWasDeclared(event));
    }

    boolean fireBeforeDeclaringIndependence(final Model model, final Player whoDecalareIt) {
        final BeforeDeclaringIndependenceEvent event = new BeforeDeclaringIndependenceEvent(model,
                whoDecalareIt);

        logger.info("Before declaring independence: {}.", event);

        return evaluateInSameThread(listener -> {
            listener.beforeDeclaringIndependence(event);
            return event.isStopped();
        });
    }

    void fireGoldWasChanged(final Model model, final Player player, final int oldValue,
            final int newValue) {
        final GoldWasChangedEvent event = new GoldWasChangedEvent(model, player, oldValue,
                newValue);

        logger.info("Gold amount changed: {}.", event);

        listeners.forEach(listener -> listener.goldWasChanged(event));
    }

    void fireGoodsWasSoldInEurope(final Model model, final GoodsAmount goodsAmount) {
        final GoodsWasSoldInEuropeEvent event = new GoodsWasSoldInEuropeEvent(model, goodsAmount);

        logger.info("Goods was sold in Europe: {}.", event);

        listeners.forEach(listener -> listener.goodsWasSoldInEurope(event));
    }

    void fireColonyWasFounded(final Model model, final Colony foundedColony) {
        final ColonyWasFoundEvent event = new ColonyWasFoundEvent(model, foundedColony);

        logger.info("Colony was founded: {}.", event);

        listeners.forEach(listener -> listener.colonyWasFounded(event));
    }

    void fireColonyWasCaptured(final Model model, final Unit capturingUnit,
            final Colony capturedColony) {
        final ColonyWasCapturedEvent event = new ColonyWasCapturedEvent(model, capturingUnit,
                capturedColony);

        logger.info("Colony was captured: {}.", event);

        listeners.forEach(listener -> listener.colonyWasCaptured(event));
    }

    void fireGameFinished(final Model model, final GameOverResult gameOverResult) {
        final GameFinishedEvent event = new GameFinishedEvent(model, gameOverResult);

        logger.info("Game finished: {}.", event);

        listeners.forEach(listener -> listener.gameFinished(event));
    }

    void fireDebugRequested(final Model model, final List<Location> locations) {
        final DebugRequestedEvent event = new DebugRequestedEvent(model, locations);

        listeners.forEach(listener -> listener.debugRequested(event));
    }

    private void executeInSeparateThread(Consumer<ModelListener> action) {
        listeners.forEach(listener -> Executors.newSingleThreadExecutor()
                .execute(() -> action.accept(listener)));
    }

    private void executeInSameThread(Consumer<ModelListener> action) {
        listeners.forEach(listener -> action.accept(listener));
    }

    /**
     * Allows to trigger stoppable event.
     * 
     * @param action
     *            required function that return <code>true</code> or
     *            <code>false</code>. When any function return <code>true</code>
     *            than action processing will be stopped. When no function
     *            return <code>true</code> than action is normally executed.
     * @return Return <code>true</code> when action should be executed. When
     *         method return <code>false</code> than action should not be
     *         executed.
     */
    private boolean evaluateInSameThread(Function<ModelListener, Boolean> action) {
        return !listeners.stream().filter(listener -> action.apply(listener)).findAny().isPresent();
    }

}
