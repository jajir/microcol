package org.akropia.gui.screen.game;

import org.akropia.gui.WasdController;
import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.ShowHelpEvent;
import org.akropia.gui.event.ShowNativeSettlementsEvent;
import org.akropia.gui.event.BuildColonyEvent;
import org.akropia.gui.event.CenterViewEvent;
import org.akropia.gui.event.ClearForestEvent;
import org.akropia.gui.event.PlowFieldEvent;
import org.akropia.gui.event.SelectNextUnitEvent;
import org.akropia.gui.event.SkipUnitTurnEvent;
import org.akropia.gui.event.StartMoveEvent;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.game.gamepanel.GamePanelPresenter;
import org.akropia.gui.screen.game.components.ColoniesMenu;
import org.akropia.gui.screen.game.gamepanel.ModeController;
import org.akropia.gui.screen.game.gamepanel.MouseOverTileManager;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning;
import org.akropia.model.Unit;
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

    private final Logger logger = LoggerFactory
            .getLogger(ScreenGamePresenter.class);

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final PersistingService persistingService;

    private final GamePanelPresenter gamePanelPresenter;

    private final GameModelController gameModelController;

    private final WasdController wasdController;

    private final ColoniesMenu coloniesMenu;
    private final org.akropia.gui.screen.game.components.InGameMenu gameMenu;
    private final TradeRoutePlanning tradeRoutePlanning;

    @Inject
    ScreenGamePresenter(final ScreenGame screenGame, final EventBus eventBus,
            final MouseOverTileManager mouseOverTileManager,
            final ModeController modeController,
            final SelectedUnitManager selectedUnitManager,
            final PersistingService persistingService,
            final GamePanelPresenter gamePanelPresenter,
            final GameModelController gameModelController,
            final WasdController wasdController,
            final ColoniesMenu coloniesMenu,
            final org.akropia.gui.screen.game.components.InGameMenu gameMenu,
            final TradeRoutePlanning tradeRoutePlanning) {
        this.gameMenu = gameMenu;
        this.tradeRoutePlanning = Preconditions
                .checkNotNull(tradeRoutePlanning);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.mouseOverTileManager = Preconditions
                .checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.gamePanelPresenter = Preconditions
                .checkNotNull(gamePanelPresenter);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.wasdController = Preconditions.checkNotNull(wasdController);
        this.coloniesMenu = Preconditions.checkNotNull(coloniesMenu);
        screenGame.setOnKeyPressed(this::onKeyPressed);
        screenGame.setOnKeyReleased(this::onKeyReleased);
        screenGame.setOnUnitOrder(event -> {
            if (event.getCode() == KeyCode.TAB) {
                eventBus.post(new SelectNextUnitEvent());
            } else {
                eventBus.post(new SkipUnitTurnEvent());
            }
        });
        // Entering the planner drops an unconfirmed movement preview, so no
        // later click can complete it.
        tradeRoutePlanning.addListener(() -> {
            if (tradeRoutePlanning.isActive() && modeController.isMoveMode()) {
                selectedUnitManager.getSelectedUnit()
                        .ifPresent(gamePanelPresenter::disableMoveMode);
            }
        });
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            onKeyPressed_escape(event);
            return;
        }
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        if (tradeRoutePlanning.isActive() && isUnitKey(event.getCode())) {
            // The route planner gives no unit orders.
            return;
        }
        if (event.getCode() == KeyCode.F5) {
            persistingService.quickSave();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.F9) {
            persistingService.quickLoad();
            event.consume();
            return;
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
        if (KeyCode.K == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.CONGRESS));
        }
        if (KeyCode.N == event.getCode()) {
            eventBus.post(new ShowNativeSettlementsEvent());
        }
        if (KeyCode.E == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
        if (KeyCode.O == event.getCode() && !event.isShortcutDown()) {
            coloniesMenu.show();
            event.consume();
            return;
        }
        if (KeyCode.M == event.getCode()) {
            onKeyMWasPressed();
            return;
        }
        if (KeyCode.P == event.getCode()) {
            onKeyPWasPressed();
            return;
        }
        if (KeyCode.F == event.getCode()) {
            onKeyFWasPressed();
            return;
        }
        if (KeyCode.B == event.getCode()) {
            eventBus.post(new BuildColonyEvent());
        }
        if (KeyCode.S == event.getCode() && event.isShortcutDown()) {
            // The main window's shortcut owner opens saving; never scroll.
            return;
        }

        wasdController.onKeyPressed(event);

        gamePanelPresenter.quitFromMoveMode();

        logger.debug("Pressed key: '" + event.getCode().getName()
                + "' has code '" + event.getCharacter() + "', modifiers '"
                + event.getCode().isModifierKey() + "'");
    }

    private static boolean isUnitKey(final KeyCode code) {
        return code == KeyCode.ENTER || code == KeyCode.M || code == KeyCode.P
                || code == KeyCode.F || code == KeyCode.B;
    }

    private void onKeyMWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            if (unit.getMovementThirds() > 0) {
                eventBus.post(new StartMoveEvent());
            }
        }
    }

    private void onKeyPWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            // The listener explains why an eligible unit cannot start.
            eventBus.post(new PlowFieldEvent());
        }
    }

    private void onKeyFWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            // The listener explains why an eligible unit cannot start.
            eventBus.post(new ClearForestEvent());
        }
    }

    private void onKeyReleased(final KeyEvent event) {
        wasdController.onKeyReleased(event);
    }

    private void onKeyPressed_escape(final KeyEvent event) {
        if (tradeRoutePlanning.isActive()) {
            // Escape leaves the planner first and restores the normal map.
            tradeRoutePlanning.setActive(false);
            event.consume();
        } else if (modeController.isMoveMode()) {
            Preconditions.checkArgument(modeController.isMoveMode(),
                    "switch to move mode was called from move mode");
            final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
            gamePanelPresenter.disableMoveMode(movingUnit);
            event.consume();
        } else if (gameModelController.isHumanTurnReady()) {
            gameMenu.show();
            event.consume();
        }
    }

    private void onKeyPressed_enter() {
        if (modeController.isMoveMode()) {
            gamePanelPresenter.switchToNormalMode(
                    mouseOverTileManager.getMouseOverTile().get());
        }
    }

}
