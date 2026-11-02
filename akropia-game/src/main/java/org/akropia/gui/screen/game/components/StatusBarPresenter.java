package org.akropia.gui.screen.game.components;

import java.util.Locale;

import org.akropia.gui.Loc;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import org.akropia.model.Calendar;
import org.akropia.model.event.GoldWasChangedEvent;
import org.akropia.model.event.RoundStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

@Listener
final class StatusBarPresenter implements UpdatableLanguage {

    private final Logger logger = LoggerFactory
            .getLogger(StatusBarPresenter.class);

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private StatusBarView view;

    private Source showEventsFromSource;

    /** Pointer travel in pixels that releases a shown result. */
    private static final double OUTCOME_MOVE = 4;

    private String contextMessage = "";
    private String hint;
    private boolean outcomeShown;
    private Point2D outcomeAnchor;
    private final EventHandler<MouseEvent> mouseFilter = this::onMouse;

    @Inject
    public StatusBarPresenter(final EventBus eventBus, final I18n text,
            final GameModelController gameModelController) {
        this.i18n = Preconditions.checkNotNull(text);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
    }

    @Subscribe
    private void onStatusBarMessageChange(final StatusBarMessageEvent event) {
        Preconditions.checkNotNull(showEventsFromSource,
                "It's not defined which events should be shown in status bar.");
        if (showEventsFromSource == event.getSource()) {
            if (outcomeShown && !event.isOutcome()) {
                // Hover help waits until the player has seen the result.
                return;
            }
            logger.debug("Setting text at status bar '{}'",
                    event.getStatusMessage());
            contextMessage = event.getStatusMessage();
            outcomeShown = event.isOutcome();
            outcomeAnchor = null;
            refreshDescription();
        }
    }

    /**
     * A shown result gives way to hover help once the pointer really moves or a
     * mouse button is pressed; the enter and exit events that follow the drop
     * or the screen change keep it.
     */
    private void onMouse(final MouseEvent event) {
        if (!outcomeShown) {
            return;
        }
        if (MouseEvent.MOUSE_PRESSED == event.getEventType()) {
            outcomeShown = false;
        } else if (MouseEvent.MOUSE_MOVED == event.getEventType()) {
            final Point2D position = new Point2D(event.getScreenX(),
                    event.getScreenY());
            if (outcomeAnchor == null) {
                outcomeAnchor = position;
            } else if (outcomeAnchor.distance(position) > OUTCOME_MOVE) {
                outcomeShown = false;
            }
        }
    }

    void setHint(final String text) {
        hint = text;
        refreshDescription();
    }

    private void refreshDescription() {
        view.getStatusBarDescription()
                .setText(hint == null ? contextMessage : hint);
    }

    void setStatusBarView(final StatusBarView statusBarView) {
        this.view = Preconditions.checkNotNull(statusBarView);
        statusBarView.getContent().sceneProperty()
                .addListener((property, oldScene, newScene) -> {
                    if (oldScene != null) {
                        oldScene.removeEventFilter(MouseEvent.ANY, mouseFilter);
                    }
                    if (newScene != null) {
                        newScene.addEventFilter(MouseEvent.ANY, mouseFilter);
                    }
                });
        statusBarView.getLabelEra().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Loc.statusBar_era_description),
                    showEventsFromSource));
        });
        statusBarView.getLabelGold().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Loc.statusBar_gold_description),
                    showEventsFromSource));
        });
        statusBarView.getStatusBarDescription().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Loc.statusBar_status_description),
                    showEventsFromSource));
        });
    }

    @Subscribe
    private void onRoundStarted(final RoundStartedEvent event) {
        Platform.runLater(() -> {
            setYearText(view.getLabelEra(), event.getCalendar());
        });
    }

    @Subscribe
    private void onGoldChange(final GoldWasChangedEvent event) {
        Platform.runLater(() -> {
            setGoldText(event.getNewValue());
        });
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        if (gameModelController.isGameModelReady()) {
            setYearText(view.getLabelEra(),
                    gameModelController.getModel().getCalendar());
            setGoldText(gameModelController.getHumanPlayer().getGold());
        } else {
            setGoldText(0);
        }
        contextMessage = "";
        outcomeShown = false;
        refreshDescription();
    }

    private void setYearText(final Label labelEra, final Calendar calendar) {
        Preconditions.checkNotNull(labelEra);
        Preconditions.checkNotNull(calendar);
        String date = "AD " + calendar.getCurrentYear();
        if (calendar.getCurrentSeason().isPresent()) {
            date += " " + i18n.get(Loc
                    .getTerrainDescription(calendar.getCurrentSeason().get()));
        }
        labelEra.setText(date);
    }

    private void setGoldText(final int gold) {
        final String amount = String.format(Locale.ROOT, "%,d", gold)
                .replace(',', '\u00a0');
        getLabelGold().setText(amount);
        getLabelGold()
                .setAccessibleText(i18n.get(Loc.statusBar_gold) + " " + amount);
    }

    /**
     * @param showEventsFromSource the showEventsFromSource to set
     */
    void setShowEventsFromSource(final Source showEventsFromSource) {
        this.showEventsFromSource = Preconditions
                .checkNotNull(showEventsFromSource);
    }

    private Label getLabelGold() {
        return Preconditions.checkNotNull(view.getLabelGold(),
                "Label gold in null");
    }

}
