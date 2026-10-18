package org.microcol.gui.event.model;

import org.microcol.gui.screen.game.gamepanel.AnimationStartedEvent;
import java.util.List;

import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Path;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.page.TestContext;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Announce a foreign step through the same model listener the game uses and
 * count what the player interface receives: forwarded step events and scheduled
 * animations.
 */
public final class ForeignStepProbe {
    private ModelListenerImpl listener;
    private int stepEvents;
    private int animations;
    private int fights;

    /** Start counting from zero; calling it again restarts the counts. */
    public void listen(final TestContext context) {
        final EventBus eventBus = context.getClassFromGuice(EventBus.class);
        if (listener != null) {
            eventBus.unregister(this);
        }
        listener = new ModelListenerImpl(
                context.getClassFromGuice(GameModelController.class), eventBus);
        stepEvents = 0;
        animations = 0;
        fights = 0;
        eventBus.register(this);
    }

    public void announce(final TestContext context, final Unit unit,
            final Location from, final Location to) {
        // Send the balanced lifecycle of a normal move so the map input lock
        // set by a forwarded step is released again.
        listener.onUnitMovedStepStarted(new UnitMovedStepStartedEvent(
                context.getModel(), unit, from, to, Direction.west));
        listener.onUnitMovedStepFinished(new UnitMovedStepFinishedEvent(
                context.getModel(), unit, from, to));
        listener.onUnitMoveFinished(new UnitMoveFinishedEvent(
                context.getModel(), unit, Path.of(List.of(from, to))));
    }

    /**
     * Announce a foreign fight through the game's model listener.
     */
    public void announceFight(final UnitAttackedEvent event) {
        listener.onUnitAttacked(event);
    }

    @Subscribe
    public void onStepStarted(final UnitMovedStepStartedEvent event) {
        stepEvents++;
    }

    @Subscribe
    public void onAnimationStarted(final AnimationStartedEvent event) {
        animations++;
    }

    @Subscribe
    public void onAttacked(final UnitAttackedEvent event) {
        fights++;
    }

    public int getFights() {
        return fights;
    }

    public int getStepEvents() {
        return stepEvents;
    }

    public int getAnimations() {
        return animations;
    }
}
