package org.microcol.gui.event;

import org.microcol.gui.dialog.DeclareIndependenceDialog;
import org.microcol.gui.dialog.DeclareIndependenceText;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Turns the declare button into a confirmed model declaration. The model
 * validates the live state again, so a state that changed meanwhile is
 * explained instead of being declared.
 */
@Listener
public final class DeclareIndependenceListener {

    private final GameModelController gameModelController;

    /** Created on first use: the window needs the ready primary stage. */
    private final Provider<DeclareIndependenceDialog> dialog;

    private final EventBus eventBus;

    private final I18n i18n;

    @Inject
    public DeclareIndependenceListener(
            final GameModelController gameModelController,
            final Provider<DeclareIndependenceDialog> dialog,
            final EventBus eventBus, final I18n i18n) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.dialog = Preconditions.checkNotNull(dialog);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    @Subscribe
    public void onEvent(
            @SuppressWarnings("unused") final DeclareIndependenceEvent event) {
        final Model model = gameModelController.getModel();
        final Player currentPlayer = gameModelController.getHumanPlayer();
        try {
            if (!dialog.get().showAndWaitForConfirmation()) {
                return;
            }
            try {
                Preconditions.checkState(
                        gameModelController.isGameModelReady()
                                && gameModelController.getModel() == model,
                        "The game was replaced during the confirmation");
                currentPlayer.declareIndependence();
            } catch (final IllegalStateException rejected) {
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(DeclareIndependenceText.rejected),
                        StatusBarMessageEvent.Source.GAME));
            }
        } finally {
            eventBus.post(new DeclareIndependenceResolvedEvent());
        }
    }

}
