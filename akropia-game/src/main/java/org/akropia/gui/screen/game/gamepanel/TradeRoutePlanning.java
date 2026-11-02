package org.akropia.gui.screen.game.gamepanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.Listener;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.TradeRoutePreview;
import org.akropia.model.event.ColonyWasCapturedEvent;
import org.akropia.model.event.ColonyWasFoundEvent;
import org.akropia.model.event.GameStartedEvent;
import org.akropia.model.event.GameStoppedEvent;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.event.UnitMoveFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;

/**
 * Presentation state of the map trade route planner: whether the mode is on,
 * which route is selected, and the model's previews of the human player's
 * routes. The previews are read once and kept until the routes, the explored
 * map or the units may have changed, so painting never searches paths. The mode
 * only changes what is shown; it issues no order and is not saved.
 */
@Singleton
@Listener
public final class TradeRoutePlanning {

    private final GameModelController game;
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean active;
    private Integer selectedRouteId;
    /** Previews of the current routes, or {@code null} when stale. */
    private List<TradeRoutePreview> previews;

    @Inject
    TradeRoutePlanning(final GameModelController game) {
        this.game = Preconditions.checkNotNull(game);
    }

    /**
     * @param listener required callback on the JavaFX thread after the mode,
     *                 the selection or the previews changed
     */
    public void addListener(final Runnable listener) {
        listeners.add(Preconditions.checkNotNull(listener));
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Switches the planner on or off. Leaving it forgets the selection, so the
     * normal map and its right panel return unchanged.
     *
     * @param value whether the planner is shown
     */
    public void setActive(final boolean value) {
        if (active == value) {
            return;
        }
        active = value;
        selectedRouteId = null;
        previews = null;
        fireChanged();
    }

    public Optional<Integer> getSelectedRouteId() {
        return Optional.ofNullable(selectedRouteId);
    }

    /**
     * @param routeId route to describe in the panel, or {@code null} for none
     */
    public void select(final Integer routeId) {
        if (Objects.equals(selectedRouteId, routeId)) {
            return;
        }
        selectedRouteId = routeId;
        fireChanged();
    }

    /**
     * @return previews of the human player's routes in creation order; empty
     *         when the planner is off or no game runs
     */
    public List<TradeRoutePreview> getPreviews() {
        if (!active || !game.isGameModelReady()) {
            return List.of();
        }
        if (previews == null) {
            final Model model = game.getModel();
            final Player player = game.getHumanPlayer();
            previews = model.getTradeRoutes(player).stream()
                    .map(route -> model.getTradeRoutePreview(player, route))
                    .collect(Collectors.toList());
            if (selectedRouteId != null && previews.stream().noneMatch(
                    preview -> preview.getRoute().getId() == selectedRouteId)) {
                // The selected route was deleted meanwhile.
                selectedRouteId = null;
            }
        }
        return previews;
    }

    /** @return preview of the selected route, if it still exists */
    public Optional<TradeRoutePreview> getSelected() {
        final List<TradeRoutePreview> all = getPreviews();
        return all.stream()
                .filter(preview -> selectedRouteId != null
                        && preview.getRoute().getId() == selectedRouteId)
                .findFirst();
    }

    /**
     * @param location required map square
     * @return previews whose shown paths pass the square, in creation order
     */
    public List<TradeRoutePreview> getRoutesAt(final Location location) {
        return getPreviews().stream()
                .filter(preview -> preview.passes(location))
                .collect(Collectors.toList());
    }

    /**
     * Selects a route under a clicked square. Repeated clicks on a square
     * shared by several routes step through them; the panel list also selects
     * each one by name.
     *
     * @param location required clicked map square
     * @return whether a route passes the square
     */
    public boolean selectAt(final Location location) {
        final List<TradeRoutePreview> here = getRoutesAt(location);
        if (here.isEmpty()) {
            return false;
        }
        int next = 0;
        for (int index = 0; index < here.size(); index++) {
            if (here.get(index).getRoute().getId() == getSelectedRouteId()
                    .orElse(-1)) {
                next = (index + 1) % here.size();
            }
        }
        select(here.get(next).getRoute().getId());
        return true;
    }

    /**
     * Reads the previews again from the model after a route, unit or map
     * change, so no stale path is shown.
     */
    public void invalidate() {
        if (!active) {
            return;
        }
        previews = null;
        fireChanged();
    }

    private void fireChanged() {
        new ArrayList<>(listeners).forEach(Runnable::run);
    }

    private void invalidateLater() {
        Platform.runLater(this::invalidate);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        invalidateLater();
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        invalidateLater();
    }

    @Subscribe
    private void onColonyWasFounded(final ColonyWasFoundEvent event) {
        invalidateLater();
    }

    @Subscribe
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        invalidateLater();
    }

    /** A loaded or restarted game always opens on the normal map. */
    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        Platform.runLater(() -> setActive(false));
    }

    @Subscribe
    private void onGameStopped(final GameStoppedEvent event) {
        Platform.runLater(() -> setActive(false));
    }
}
