package org.microcol.gui.mainmenu;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.microcol.gui.DialogIndependenceWasDeclared;
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
import org.microcol.gui.gamepanel.SelectedUnitWasChangedController;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
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

import javafx.scene.control.MenuItem;

public class MainMenuPresenter {

    private final MainMenuView view;

    private final SelectedUnitManager selectedUnitManager;

    private boolean isFocusedMoveableUnit = false;

    public boolean isTileFocused = false;

    private final GameModelController gameModelController;

    private final GameController gameController;

    @Inject
    public MainMenuPresenter(final MainMenuView view,
            final AboutGameEventController gameEventController,
            final BuildColonyEventController buildColonyEventController,
            final CenterViewController centerViewController,
            final ChangeLanguageController changeLanguageController,
            final DeclareIndependenceController declareIndependenceController,
            final QuitGameController quitGameController,
            final ShowGridController showGridController,
            final SelectNextUnitController selectNextUnitController,
            final ExitGameController exitGameController,
            final TileWasSelectedController tileWasSelectedController,
            final GameController gameController, final GameModelController gameModelController,
            final StartMoveController startMoveController,
            final EndMoveController endMoveController,
            final TurnStartedController turnStartedController,
            final PersistingDialog persistingDialog, final EuropeDialog europeDialog,
            final DialogIndependenceWasDeclared dialogIndependenceWasDeclared,
            final Colonizopedia colonizopedia,
            final PreferencesAnimationSpeed preferencesAnimationSpeed,
            final PreferencesVolume preferencesVolume,
            final SelectedUnitManager selectedUnitManager,
            final UnitMovedController unitMovedStepController,
            final UnitMoveFinishedController unitMoveFinishedController,
            final SelectedUnitWasChangedController selectedUnitWasChangedController) {
        this.view = Preconditions.checkNotNull(view);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gameController = Preconditions.checkNotNull(gameController);
        /**
         * Following section describe what happens when menu item is selected
         */
        view.getMenuItemNewGame().setOnAction(actionEvent -> gameController.startNewDefaultGame());
        view.getMenuItemExitGame()
                .setOnAction(event -> exitGameController.fireEvent(new ExitGameEvent()));
        view.getMenuItemSaveGame().setOnAction(event -> persistingDialog.saveModel());
        view.getMenuItemLoadGame().setOnAction(event -> persistingDialog.loadModel());
        view.getMenuItemQuitGame()
                .setOnAction(actionEvent -> quitGameController.fireEvent(new QuitGameEvent()));
        view.getMenuItemAbout()
                .setOnAction(actionEvent -> gameEventController.fireEvent(new AboutGameEvent()));
        view.getMenuItemColonizopedia().setOnAction(event -> colonizopedia.showAndWait());
        view.getRbMenuItemlanguageCz().setOnAction(actionEvent -> changeLanguageController
                .fireEvent(new ChangeLanguageEvent(Text.Language.cz)));
        view.getRbMenuItemlanguageEn().setOnAction(actionEvent -> changeLanguageController
                .fireEvent(new ChangeLanguageEvent(Text.Language.en)));
        view.getMenuItemVolume()
                .setOnAction(actionEvent -> preferencesVolume.resetAndShowAndWait());
        view.getMenuItemAnimationSpeed()
                .setOnAction(event -> preferencesAnimationSpeed.resetAndShowAndWait());
        view.getMenuItemEurope().setOnAction(event -> europeDialog.show());
        view.getMenuItemShowGrid().setOnAction(ectionEvent -> showGridController
                .fireEvent(new ShowGridEvent(view.getMenuItemShowGrid().isSelected())));
        view.getMenuItemBuildColony().setOnAction(event -> buildColonyEventController.fireEvent());
        view.getMenuItemMove().setOnAction(ectionEvent -> {
            startMoveController.fireEvent(new StartMoveEvent());
            view.getMenuItemMove().setDisable(true);
        });
        view.getMenuItemDeclareIndependence().setOnAction(event -> {
            dialogIndependenceWasDeclared.showAndWait();
            declareIndependenceController.fireEvent(new DeclareIndependenceEvent(
                    gameModelController.getModel(), gameModelController.getCurrentPlayer()));
        });
        view.getMenuItemCenterView()
                .setOnAction(event -> centerViewController.fireEvent(new CenterViewEvent()));
        view.getMenuItemNextUnit().setOnAction(
                event -> selectNextUnitController.fireEvent(new SelectNextUnitEvent()));

        /**
         * Following section define visibility of menu items.
         */
        changeLanguageController.addListener(event -> {
            view.updateLanguage();
        });
        tileWasSelectedController.addRunLaterListener(event -> onFocusedTileEvent(event));
        turnStartedController.addRunLaterListener(this::onTurnStartedEvent);
        declareIndependenceController
                .addListener(event -> view.getMenuItemDeclareIndependence().setDisable(true));

        quitGameController.addListener(this::onGameFinihedEvent);
        endMoveController.addRunLaterListener(this::onEndMoveEvent);
        unitMovedStepController.addRunLaterListener(this::onUnitMovedStep);
        unitMoveFinishedController.addRunLaterListener(this::unitMoveFinishedController);
        exitGameController.addListener(this::onExitGame);
        selectedUnitWasChangedController.addListener(this::onSelectedUnitChanged);
        initialSetting();
    }

    @SuppressWarnings("unused")
    private void onExitGame(final ExitGameEvent event) {
        view.getMenuItemExitGame().setDisable(true);
        view.getMenuItemSaveGame().setDisable(true);
        view.getMenuItemCenterView().setDisable(true);
        view.getMenuItemMove().setDisable(true);
        view.getMenuItemEurope().setDisable(true);
        view.getMenuItemNextUnit().setDisable(true);
        view.getMenuItemDeclareIndependence().setDisable(true);
    }

    /**
     * Configure menus how should be after application start.
     */
    private void initialSetting() {
        view.getMenuItemDeclareIndependence().setDisable(true);
        view.getMenuItemNewGame().setDisable(!gameController.isDefaultCampaignFinished());
        view.getMenuItemExitGame().setDisable(true);
        view.getMenuItemSaveGame().setDisable(true);
        view.getMenuItemNextUnit().setDisable(true);
        view.getMenuItemCenterView().setDisable(true);
        view.getMenuItemEurope().setDisable(true);
    }

    @SuppressWarnings("unused")
    private void onUnitMovedStep(final UnitMovedStepEvent event) {
        // TODO event queue is blocked by animation of move. This method is
        // called as last method.
        view.getMenuItemNextUnit().setDisable(true);
    }

    @SuppressWarnings("unused")
    private void unitMoveFinishedController(final UnitMoveFinishedEvent event) {
        view.getMenuItemNextUnit().setDisable(false);
    }

    @SuppressWarnings("unused")
    void onEndMoveEvent(final EndMoveEvent event) {
        if (selectedUnitManager.isSelectedUnitMoveable()) {
            view.getMenuItemMove().setDisable(false);
        } else {
            view.getMenuItemMove().setDisable(true);
        }
    }

    /**
     * Method process event when user put focus to some tile.
     * 
     * @param event
     *            required event
     */
    private void onFocusedTileEvent(final TileWasSelectedEvent event) {
        view.getMenuItemCenterView().setDisable(false);
        isTileFocused = true;
        if (isTileContainsMovebleUnit(event)) {
            view.getMenuItemMove().setDisable(false);
            isFocusedMoveableUnit = true;
            view.getMenuItemBuildColony().setDisable(!isPossibleToBuildColony(event));
        } else {
            view.getMenuItemBuildColony().setDisable(true);
            view.getMenuItemMove().setDisable(true);
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
    private void onGameFinihedEvent(final QuitGameEvent exitGameEvent) {
        view.getMenuItemEurope().setDisable(true);
        view.getMenuItemNextUnit().setDisable(true);
        view.getMenuItemBuildColony().setDisable(true);
    }

    private void onTurnStartedEvent(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            if (isTileFocused) {
                view.getMenuItemCenterView().setDisable(false);
            }
            if (isFocusedMoveableUnit) {
                view.getMenuItemMove().setDisable(false);
            }
            // TODO when focused unit is settler try to enable build colony menu
            // item
            view.getMenuItemExitGame().setDisable(false);
            view.getMenuItemSaveGame().setDisable(false);
            view.getMenuItemEurope().setDisable(false);
            view.getMenuItemNextUnit().setDisable(false);
            view.getMenuItemDeclareIndependence()
                    .setDisable(event.getPlayer().isDeclaredIndependence());
        } else {
            view.getMenuItemExitGame().setDisable(true);
            view.getMenuItemSaveGame().setDisable(true);
            view.getMenuItemCenterView().setDisable(true);
            view.getMenuItemMove().setDisable(true);
            view.getMenuItemEurope().setDisable(true);
            view.getMenuItemNextUnit().setDisable(true);
        }
        setBuildColony();
    }

    @SuppressWarnings("unused")
    private void onSelectedUnitChanged(final SelectedUnitWasChangedEvent event) {
        setBuildColony();
        setMoveUnit();
    }

    /**
     * <p>
     * It's not necessary to have it as lambda, it could void method. BiConsumer
     * is not used anywhere else.
     * </p>
     */
    private final BiConsumer<MenuItem, SelectedUnitManager> eval = (menuItem, sum) -> {
        if (sum.getSelectedUnit().isPresent()) {
            final Unit unit = sum.getSelectedUnit().get();
            menuItem.setDisable(!unit.getType().canBuildColony() || unit.getAvailableMoves() == 0);
        } else {
            menuItem.setDisable(true);
        }
    };

    private void setMoveUnit() {
        eval.accept(view.getMenuItemMove(), selectedUnitManager);
    }

    private void setBuildColony() {
        eval.accept(view.getMenuItemBuildColony(), selectedUnitManager);
    }

}
