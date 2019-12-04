package org.microcol.gui.screen.game;

import org.microcol.gui.WasdController;
import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.event.ShowHelpEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.SelectNextUnitEvent;
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

    private final PersistingService persistingService;

    private final GamePanelPresenter gamePanelPresenter;

    private final GameModelController gameModelController;

    private final WasdController wasdController;

    @Inject
    ScreenGamePresenter(final ScreenGame screenGame, final EventBus eventBus,
            final MouseOverTileManager mouseOverTileManager, final ModeController modeController,
            final SelectedUnitManager selectedUnitManager,
            final PersistingService persistingService, final GamePanelPresenter gamePanelPresenter,
            final GameModelController gameModelController, final WasdController wasdController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gamePanelPresenter = Preconditions.checkNotNull(gamePanelPresenter);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.wasdController = Preconditions.checkNotNull(wasdController);
        screenGame.setOnKeyPressed(this::onKeyPressed);
        screenGame.setOnKeyReleased(this::onKeyReleased);
        screenGame.setOnTabPressed(event -> eventBus.post(new SelectNextUnitEvent()));
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
            return;
        }

        if (KeyCode.R == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.STATISTICS));
        }
        if (KeyCode.C == event.getCode()) {
            eventBus.post(new CenterViewEvent());
        }
        if (KeyCode.T == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT));
        }
        if (KeyCode.G == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.GOALS));
        }
        if (KeyCode.H == event.getCode()) {
            eventBus.post(new ShowHelpEvent());
        }
        if (KeyCode.E == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
        if (KeyCode.M == event.getCode()) {
            onKeyMWasPressed();
            return;
        }
        if (KeyCode.P == event.getCode()) {
            onKeyPWasPressed();
            return;
        }
        if (KeyCode.B == event.getCode()) {
            eventBus.post(new BuildColonyEvent());
        }
        if (KeyCode.S == event.getCode() & event.isControlDown()) {
            persistingService.saveModel(gameModelController.getModel());
        }

        wasdController.onKeyPressed(event);

        gamePanelPresenter.quitFromMoveMode();

        logger.debug("Pressed key: '" + event.getCode().getName() + "' has code '"
                + event.getCharacter() + "', modifiers '" + event.getCode().isModifierKey() + "'");
    }

    private void onKeyMWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            if (unit.getActionPoints() > 0) {
                eventBus.post(new StartMoveEvent());
            }
        }
    }

    private void onKeyPWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            if (unit.canPlowFiled()) {
                eventBus.post(new PlowFieldEvent());
            }
        }
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
