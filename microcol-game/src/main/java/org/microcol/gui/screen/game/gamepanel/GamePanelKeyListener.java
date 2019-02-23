package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.dialog.PersistingDialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Listener
@Singleton
public class GamePanelKeyListener {

    private final Logger logger = LoggerFactory.getLogger(GamePanelKeyListener.class);

    private final PersistingDialog persistingDialog;

    private final GameModelController gameModelController;

    @Inject
    GamePanelKeyListener(final PersistingDialog persistingDialog,
            final GameModelController gameModelController) {
        this.persistingDialog = Preconditions.checkNotNull(persistingDialog);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onKeyEvent(final KeyEvent event) {
        if (KeyCode.S == event.getCode() & event.isControlDown()) {
            persistingDialog.saveModel(gameModelController.getModel());
        }

        logger.debug("Pressed key: '" + event.getCode().getName() + "' has code '"
                + event.getCharacter() + "', modifiers '" + event.getCode().isModifierKey() + "'");
    }

}
