package org.microcol.gui.screen.europe;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.event.MercenaryOfferRequiredEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;

/**
 * Opens the mercenary decision on the screen the player is already using, once
 * the human owner's update is complete. The screen is not switched: a rebel's
 * home Europe may be closed, and the paid group goes to a colony port anyway.
 * The offer stays in the model until it is answered or its turn ends, so a
 * loaded game asks again.
 */
@Singleton
@Listener
public class MercenaryOfferController {

    private final ViewUtil viewUtil;
    private final I18n i18n;
    private final GameModelController controller;

    @Inject
    public MercenaryOfferController(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller) {
        this.viewUtil = viewUtil;
        this.i18n = i18n;
        this.controller = controller;
    }

    @Subscribe
    private void onMercenaryOfferRequired(
            final MercenaryOfferRequiredEvent event) {
        if (!event.getPlayer().isHuman()) {
            return;
        }
        Platform.runLater(() -> {
            final Model model = event.getModel();
            if (controller.getModel() != model) {
                return;
            }
            model.getPendingMercenaryOffer(event.getPlayer())
                    .ifPresent(offer -> new MercenaryOfferDialog(viewUtil, i18n,
                            controller, model, offer).showAndWait());
        });
    }
}
