package org.microcol.gui.mainmenu;

import java.util.Optional;

import org.microcol.gui.DialogIndependenceWasDeclared;
import org.microcol.gui.MainFramePresenter;
import org.microcol.gui.PersistingDialog;
import org.microcol.gui.PreferencesAnimationSpeed;
import org.microcol.gui.PreferencesVolume;
import org.microcol.gui.colonizopedia.Colonizopedia;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.event.EndMoveController;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.event.model.UnitMoveFinishedController;
import org.microcol.gui.event.model.UnitMovedController;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.gamepanel.TileWasSelectedController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.util.Text;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;

public class MainMenuPresenter {

    private final MainMenuPresenter.Display display;

    private final SelectedUnitManager selectedUnitManager;

    private boolean isFocusedMoveableUnit = false;

    public boolean isTileFocused = false;
    
    private final GameModelController gameModelController;

    public interface Display {

        MenuItem getMenuItemNewGame();

        MenuItem getMenuItemDeclareIndependence();

        MenuItem getMenuItemLoadGame();

        MenuItem getMenuItemSaveGame();

        MenuItem getMenuItemQuitGame();

        MenuItem getMenuItemAbout();

        RadioMenuItem getRbMenuItemlanguageEn();

        RadioMenuItem getRbMenuItemlanguageCz();

        void updateLanguage();

        MenuItem getMenuItemAnimationSpeed();

        MenuItem getMenuItemVolume();

        CheckMenuItem getMenuItemShowGrid();

        MenuItem getMenuItemMove();

        MenuItem getMenuItemBuildColony();

        MenuItem getMenuItemNextUnit();

        MenuItem getMenuItemCenterView();

        MenuItem getMenuItemEurope();

        MenuItem getMenuItemColonizopedia();
        
        MenuItem getMenuItemExitGame();
    }

    @Inject
    public MainMenuPresenter(final MainMenuPresenter.Display display,
            /**
             * Following parameters are controllers reacting on menu events
             */
            final AboutGameEventController gameEventController,
            final BuildColonyEventController buildColonyEventController,
            final CenterViewController centerViewController,
            final ChangeLanguageController changeLanguageController,
            final DeclareIndependenceController declareIndependenceController,
            final ExitGameController exitGameController,
            final ShowGridController showGridController,
            final SelectNextUnitController selectNextUnitController,

            /**
             * Menu items react on following events
             */
            final TileWasSelectedController tileWasSelectedController,
            final GameController gameController,
            final GameModelController gameModelController,
            final StartMoveController startMoveController,
            final EndMoveController endMoveController,
            final TurnStartedController turnStartedController,

            /**
             * Other events consumers and helpers
             */
            final PersistingDialog persistingDialog,
            final EuropeDialog europeDialog,
            final DialogIndependenceWasDeclared dialogIndependenceWasDeclared,
            final Colonizopedia colonizopedia,
            final PreferencesAnimationSpeed preferencesAnimationSpeed,
            final PreferencesVolume preferencesVolume,
            final SelectedUnitManager selectedUnitManager,
            final UnitMovedController unitMovedStepController,
            final UnitMoveFinishedController unitMoveFinishedController,
            final MainFramePresenter mainFramePresenter) {
        this.display = Preconditions.checkNotNull(display);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        /**
         * Following section describe what happens when menu item is selected
         */
        display.getMenuItemNewGame()
                .setOnAction(actionEvent -> gameController.startNewDefaultGame());
        display.getMenuItemSaveGame().setOnAction(event -> persistingDialog.saveModel());
        display.getMenuItemLoadGame().setOnAction(event -> persistingDialog.loadModel());
        display.getMenuItemQuitGame()
                .setOnAction(actionEvent -> exitGameController.fireEvent(new ExitGameEvent()));
        display.getMenuItemAbout()
                .setOnAction(actionEvent -> gameEventController.fireEvent(new AboutGameEvent()));
        display.getMenuItemColonizopedia().setOnAction(event -> colonizopedia.showAndWait());
        display.getRbMenuItemlanguageCz()
                .setOnAction(actionEvent -> changeLanguageController.fireEvent(
                        new ChangeLanguageEvent(Text.Language.cz, gameModelController.getModel())));
        display.getRbMenuItemlanguageEn()
                .setOnAction(actionEvent -> changeLanguageController.fireEvent(
                        new ChangeLanguageEvent(Text.Language.en, gameModelController.getModel())));
        display.getMenuItemVolume()
                .setOnAction(actionEvent -> preferencesVolume.resetAndShowAndWait());
        display.getMenuItemAnimationSpeed()
                .setOnAction(event -> preferencesAnimationSpeed.resetAndShowAndWait());
        display.getMenuItemEurope().setOnAction(event -> europeDialog.show());
        display.getMenuItemShowGrid().setOnAction(ectionEvent -> showGridController
                .fireEvent(new ShowGridEvent(display.getMenuItemShowGrid().isSelected())));
        display.getMenuItemBuildColony()
                .setOnAction(event -> buildColonyEventController.fireEvent());
        display.getMenuItemMove().setOnAction(ectionEvent -> {
            startMoveController.fireEvent(new StartMoveEvent());
            display.getMenuItemMove().setDisable(true);
        });
        display.getMenuItemDeclareIndependence().setOnAction(event -> {
            dialogIndependenceWasDeclared.showAndWait();
            declareIndependenceController.fireEvent(new DeclareIndependenceEvent(
                    gameModelController.getModel(), gameModelController.getCurrentPlayer()));
        });
        display.getMenuItemCenterView()
                .setOnAction(event -> centerViewController.fireEvent(new CenterViewEvent()));
        display.getMenuItemNextUnit().setOnAction(
                event -> selectNextUnitController.fireEvent(new SelectNextUnitEvent()));
        display.getMenuItemExitGame().setOnAction(event->{
            gameController.stopGame();
            mainFramePresenter.showPanel(MainFramePresenter.START_PANEL);
        });

        /**
         * Following section define visibility of menu items.
         */
        changeLanguageController.addListener(event -> {
            display.updateLanguage();
        });
        tileWasSelectedController.addListener(event -> onFocusedTileEvent(event));
        turnStartedController.addListener(this::onTurnStartedEvent);
        declareIndependenceController
                .addListener(event -> display.getMenuItemDeclareIndependence().setDisable(true));

        exitGameController.addListener(this::onGameFinihedEvent);
        endMoveController.addRunLaterListener(this::onEndMoveEvent);
        unitMovedStepController.addRunLaterListener(this::onUnitMovedStep);
        unitMoveFinishedController.addRunLaterListener(this::unitMoveFinishedController);
    }

    @SuppressWarnings("unused")
    private void onUnitMovedStep(final UnitMovedStepEvent event) {
        // TODO event queue is blocked by animation of move. This method is
        // called as last method.
        display.getMenuItemNextUnit().setDisable(true);
    }

    @SuppressWarnings("unused")
    private void unitMoveFinishedController(final UnitMoveFinishedEvent event) {
        display.getMenuItemNextUnit().setDisable(false);
    }

    @SuppressWarnings("unused")
    void onEndMoveEvent(final EndMoveEvent event) {
        if (selectedUnitManager.isSelectedUnitMoveable()) {
            display.getMenuItemMove().setDisable(false);
        } else {
            display.getMenuItemMove().setDisable(true);
        }
    }

    /**
     * Method process event when user put focus to some tile.
     * 
     * @param event
     *            required event
     */
    private void onFocusedTileEvent(final TileWasSelectedEvent event) {
        display.getMenuItemCenterView().setDisable(false);
        isTileFocused = true;
        if (isTileContainsMovebleUnit(event)) {
            display.getMenuItemMove().setDisable(false);
            isFocusedMoveableUnit = true;
            display.getMenuItemBuildColony().setDisable(!isPossibleToBuildColony(event));
        } else {
            display.getMenuItemBuildColony().setDisable(true);
            display.getMenuItemMove().setDisable(true);
            isFocusedMoveableUnit = false;
        }
    }
    
    // TODO use selectedUnitController
    private boolean isTileContainsMovebleUnit(final TileWasSelectedEvent event) {
        final Model model = gameModelController.getModel();
        final Optional<Unit> unit = model.getUnitsAt(event.getLocation()).stream().findFirst();
        return unit.isPresent() && unit.get().getOwner().equals(model.getCurrentPlayer());
    }

    private boolean isPossibleToBuildColony(final TileWasSelectedEvent event) {
        final Model model = gameModelController.getModel();
        if (isTileContainsMovebleUnit(event)) {
            final Unit unit = model.getUnitsAt(event.getLocation()).stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("It should not be here"));
            return unit.getType().canBuildColony() && unit.getAvailableMoves() > 0;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void onGameFinihedEvent(final ExitGameEvent exitGameEvent) {
        display.getMenuItemEurope().setDisable(true);
        display.getMenuItemNextUnit().setDisable(true);
        display.getMenuItemBuildColony().setDisable(true);
    }

    private void onTurnStartedEvent(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            if (isTileFocused) {
                display.getMenuItemCenterView().setDisable(false);
            }
            if (isFocusedMoveableUnit) {
                display.getMenuItemMove().setDisable(false);
            }
            display.getMenuItemEurope().setDisable(false);
            display.getMenuItemNextUnit().setDisable(false);
            display.getMenuItemDeclareIndependence()
                    .setDisable(event.getPlayer().isDeclaredIndependence());
        } else {
            display.getMenuItemCenterView().setDisable(true);
            display.getMenuItemMove().setDisable(true);
            display.getMenuItemEurope().setDisable(true);
            display.getMenuItemNextUnit().setDisable(true);
        }
    }

}
