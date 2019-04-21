package org.microcol.gui.screen.game;

import org.microcol.gui.WasdController;
import org.microcol.gui.dialog.PersistingDialog;
import org.microcol.gui.event.AboutGameEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.SelectNextUnitEvent;
import org.microcol.gui.event.ShowGoalsEvent;
import org.microcol.gui.event.ShowTurnReportEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.gamepanel.GamePanelPresenter;
import org.microcol.gui.screen.game.gamepanel.ModeController;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileManager;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenGamePresenter {

    private final Logger logger = LoggerFactory.getLogger(ScreenGamePresenter.class);

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final PersistingDialog persistingDialog;

    private final GamePanelPresenter gamePanelPresenter;

    private final GameModelController gameModelController;
    
    private final WasdController wasdController;
    
    @Inject
    ScreenGamePresenter(final ScreenGame screenGame, final EventBus eventBus, final MouseOverTileManager mouseOverTileManager, final ModeController modeController,
            final SelectedUnitManager selectedUnitManager, final PersistingDialog persistingDialog,
            final GamePanelPresenter gamePanelPresenter,
            final GameModelController gameModelController, final WasdController wasdController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gamePanelPresenter = Preconditions.checkNotNull(gamePanelPresenter);
        this.persistingDialog = Preconditions.checkNotNull(persistingDialog);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.wasdController = Preconditions.checkNotNull(wasdController);
        screenGame.setOnKeyPressed(this::onKeyPressed);
        screenGame.setOnKeyReleased(this::onKeyReleased);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            onKeyPressed_escape();
        }
        /**
         * Enter
         */
        if (KeyCode.ENTER == event.getCode()) {
            onKeyPressed_enter();
        }

        if (KeyCode.R == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.STATISTICS));
        }
        if (KeyCode.C == event.getCode()) {
            eventBus.post(new CenterViewEvent());
        }
        if (KeyCode.T == event.getCode()) {
            eventBus.post(new ShowTurnReportEvent());
        }
        if (KeyCode.G == event.getCode()) {
            eventBus.post(new ShowGoalsEvent());
        }
        if (KeyCode.H == event.getCode()) {
            eventBus.post(new AboutGameEvent());
        }
        if (KeyCode.E == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
        if (KeyCode.M == event.getCode()) {
            eventBus.post(new StartMoveEvent());
        }
        if (KeyCode.P == event.getCode()) {
            eventBus.post(new PlowFieldEvent());
        }
        if (KeyCode.B == event.getCode()) {
            eventBus.post(new BuildColonyEvent());
        }
        if (KeyCode.S == event.getCode() & event.isControlDown()) {
            persistingDialog.saveModel(gameModelController.getModel());
        }

        if (KeyCode.TAB == event.getCode()) {
            eventBus.post(new SelectNextUnitEvent());
        }
        
        wasdController.onKeyPressed(event);

        logger.debug("Pressed key: '" + event.getCode().getName() + "' has code '"
                + event.getCharacter() + "', modifiers '" + event.getCode().isModifierKey() + "'");
    }

    private void onKeyReleased(final KeyEvent event) {
        wasdController.onKeyReleased(event);
    }

    private void onKeyPressed_escape() {
        if (modeController.isMoveMode()) {
            Preconditions.checkArgument(modeController.isMoveMode(),
                    "switch to move mode was called from move mode");
            final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
            gamePanelPresenter.disableMoveMode(movingUnit);
        }
    }

    private void onKeyPressed_enter() {
        if (modeController.isMoveMode()) {
            gamePanelPresenter.switchToNormalMode(mouseOverTileManager.getMouseOverTile().get());
        }
    }
    
}
