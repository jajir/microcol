package org.akropia.gui.screen.game;

import java.util.Optional;

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
import org.akropia.gui.screen.game.components.ButtonsGamePanel;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutContext;
import org.akropia.gui.shortcut.ShortcutInput;
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
    private final KeyboardShortcuts shortcuts;
    private final ButtonsGamePanel buttonsGamePanel;

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
            final TradeRoutePlanning tradeRoutePlanning,
            final KeyboardShortcuts shortcuts,
            final ButtonsGamePanel buttonsGamePanel) {
        this.gameMenu = gameMenu;
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.buttonsGamePanel = Preconditions.checkNotNull(buttonsGamePanel);
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
        screenGame.setOnUnitOrder(shortcuts, action -> {
            if (action == ShortcutAction.NEXT_UNIT) {
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
        if (event.getTarget() instanceof javafx.scene.Node && ShortcutInput
                .isOwnedByText(((javafx.scene.Node) event.getTarget())
                        .getScene().getFocusOwner(), event)) {
            // A focused text field keeps its typing keys.
            return;
        }
        if (shortcuts.find(event, ShortcutContext.SESSION).isPresent()
                || shortcuts.find(event, ShortcutContext.GLOBAL).isPresent()) {
            // The main window's shortcut owner handles it; never scroll.
            return;
        }
        final Optional<ShortcutAction> found = shortcuts.find(event,
                ShortcutContext.MAP);
        if (found.isPresent() && onMapAction(found.get(), event)) {
            return;
        }

        wasdController.onKeyPressed(event);

        gamePanelPresenter.quitFromMoveMode();

        logger.debug("Pressed key: '" + event.getCode().getName()
                + "' has code '" + event.getCharacter() + "', modifiers '"
                + event.getCode().isModifierKey() + "'");
    }

    /**
     * @return <code>true</code> when the key is fully handled; otherwise it
     *         also leaves movement mode, as any other key does
     */
    private boolean onMapAction(final ShortcutAction action,
            final KeyEvent event) {
        if (action == ShortcutAction.NEXT_UNIT
                || action == ShortcutAction.SKIP_UNIT) {
            // The screen's unit-order filter owns them.
            return true;
        }
        if (tradeRoutePlanning.isActive() && action.isUnitOrder()) {
            // The route planner gives no unit orders.
            return true;
        }
        switch (action) {
        case QUICK_SAVE:
            persistingService.quickSave();
            event.consume();
            return true;
        case QUICK_LOAD:
            persistingService.quickLoad();
            event.consume();
            return true;
        case CONFIRM_MOVE:
            onKeyPressed_enter();
            return true;
        case STATISTICS:
            eventBus.post(new ShowScreenEvent(Screen.STATISTICS));
            return false;
        case CENTER_VIEW:
            eventBus.post(new CenterViewEvent());
            return false;
        case TURN_REPORT:
            eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT));
            return false;
        case GOALS:
            eventBus.post(new ShowScreenEvent(Screen.GOALS));
            return false;
        case GO_TO:
            eventBus.post(
                    new org.akropia.gui.event.ChooseGoToDestinationEvent());
            event.consume();
            return true;
        case HELP:
            eventBus.post(new ShowHelpEvent());
            return false;
        case CONGRESS:
            eventBus.post(new ShowScreenEvent(Screen.CONGRESS));
            return false;
        case NATIVE_SETTLEMENTS:
            eventBus.post(new ShowNativeSettlementsEvent());
            return false;
        case EUROPE:
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
            return false;
        case COLONIES:
            coloniesMenu.show();
            event.consume();
            return true;
        case MOVE:
            onKeyMWasPressed();
            return true;
        case PLOW:
            onKeyPWasPressed();
            return true;
        case CLEAR_FOREST:
            onKeyFWasPressed();
            return true;
        case BUILD_COLONY:
            eventBus.post(new BuildColonyEvent());
            return false;
        case END_TURN:
        case BUILD_ROAD:
        case FORTIFY:
        case SENTRY:
        case GO_TO_TILE:
        case TRADE_ROUTES:
        case NEW_TRADE_ROUTE:
        case DISBAND:
        case JETTISON:
        case DECLARE_INDEPENDENCE:
        case RETIRE:
            // The command's own control keeps its availability guards.
            buttonsGamePanel.fireShortcut(action);
            event.consume();
            return true;
        case EXIT_TO_MAIN_MENU:
        case RESTART_MISSION:
            // The session menu's own command and confirmation.
            gameMenu.run(action);
            event.consume();
            return true;
        default:
            // Held scroll keys continue with the scroll controller.
            return false;
        }
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
            // A plowing unit offers Cancel instead; otherwise the listener
            // explains why an eligible unit cannot start.
            if (!buttonsGamePanel.fireShortcut(ShortcutAction.PLOW)) {
                eventBus.post(new PlowFieldEvent());
            }
        }
    }

    private void onKeyFWasPressed() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            // A clearing unit offers Cancel instead; otherwise the listener
            // explains why an eligible unit cannot start.
            if (!buttonsGamePanel.fireShortcut(ShortcutAction.CLEAR_FOREST)) {
                eventBus.post(new ClearForestEvent());
            }
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
