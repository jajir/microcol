package org.akropia.gui.screen.statistics;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.HallOfFameDao;
import org.akropia.model.Model;
import org.akropia.model.event.ScoreBoundaryRequiredEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;

/**
 * Opens the frozen result when spring 1800 has come, on the screen the player
 * is already using. The game stays at the boundary until the player continues,
 * so a loaded game shows the same choice again.
 */
@Singleton
@Listener
public class ScoreBoundaryController {

    private final ViewUtil viewUtil;
    private final I18n i18n;
    private final GameModelController controller;
    private final HallOfFameDao hallOfFameDao;

    /** Only touched on the JavaFX thread. */
    private boolean showing;

    @Inject
    public ScoreBoundaryController(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller,
            final HallOfFameDao hallOfFameDao) {
        this.viewUtil = viewUtil;
        this.i18n = i18n;
        this.controller = controller;
        this.hallOfFameDao = hallOfFameDao;
    }

    @Subscribe
    private void onScoreBoundaryRequired(
            final ScoreBoundaryRequiredEvent event) {
        if (!event.getPlayer().isHuman()) {
            return;
        }
        Platform.runLater(() -> {
            final Model model = event.getModel();
            // A stale event of a replaced model, an answered choice or a
            // dialog that is already open shows nothing.
            if (showing || controller.getModel() != model
                    || !model.isAwaitingScoreChoice()) {
                return;
            }
            showing = true;
            try {
                model.getFrozenResult()
                        .ifPresent(result -> new ScoreBoundaryDialog(viewUtil,
                                i18n, controller, model, event.getPlayer(),
                                result, hallOfFameDao).showAndWait());
            } finally {
                showing = false;
            }
        });
    }
}
