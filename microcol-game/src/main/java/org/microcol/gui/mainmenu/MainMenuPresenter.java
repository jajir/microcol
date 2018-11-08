package org.microcol.gui.mainmenu;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.microcol.gui.PersistingDialog;
import org.microcol.gui.colonizopedia.ColonizopediaDialog;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedEvent;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Unit;
import org.microcol.model.campaign.CampaignNames;
import org.microcol.model.campaign.FreePlayMissionNames;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.control.MenuItem;

@Listener
public final class MainMenuPresenter {

    private final MainMenuView view;

    private final SelectedUnitManager selectedUnitManager;

    private final GameModelController gameModelController;

    private final GameController gameController;

    @Inject
    public MainMenuPresenter(final MainMenuView view, final GameController gameController,
            final GameModelController gameModelController, final PersistingDialog persistingDialog,
            final ColonizopediaDialog colonizopedia, final SelectedUnitManager selectedUnitManager,
            final EventBus eventBus, final I18n i18n) {
        this.view = Preconditions.checkNotNull(view);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gameController = Preconditions.checkNotNull(gameController);
        /**
         * Following section describe what happens when menu item is selected
         */
        view.getMenuItemNewGame().setOnAction(actionEvent -> gameController
                .startCampaignMission(CampaignNames.freePlay, FreePlayMissionNames.freePlay));
        view.getMenuItemExitGame().setOnAction(event -> eventBus.post(new ExitGameEvent()));
        view.getMenuItemSaveGame().setOnAction(event -> persistingDialog.saveModel());
        view.getMenuItemLoadGame().setOnAction(event -> persistingDialog.loadModel());
        view.getMenuItemQuitGame().setOnAction(actionEvent -> eventBus.post(new QuitGameEvent()));
        view.getMenuItemAbout().setOnAction(actionEvent -> eventBus.post(new AboutGameEvent()));
        view.getMenuItemColonizopedia().setOnAction(event -> colonizopedia.showAndWait());
        view.getRbMenuItemlanguageCz().setOnAction(
                actionEvent -> eventBus.post(new ChangeLanguageEvent(Text.Language.cz, i18n)));
        view.getRbMenuItemlanguageEn().setOnAction(
                actionEvent -> eventBus.post(new ChangeLanguageEvent(Text.Language.en, i18n)));
        view.getMenuItemEurope()
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.EUROPE)));
        view.getMenuItemBuildColony().setOnAction(event -> eventBus
                .post(BuildColonyEvent.make(gameModelController, selectedUnitManager)));
        view.getMenuItemMove().setOnAction(ectionEvent -> {
//            startMoveController.fireEvent(new StartMoveEvent());
            view.getMenuItemMove().setDisable(true);
        });
        view.getMenuItemDeclareIndependence().setOnAction(event -> {
            eventBus.post(new DeclareIndependenceEvent(gameModelController.getModel(),
                    gameModelController.getCurrentPlayer()));
        });
        view.getMenuItemCenterView().setOnAction(event -> eventBus.post(new CenterViewEvent()));
        view.getMenuItemNextUnit().setOnAction(event -> eventBus.post(new SelectNextUnitEvent()));
        view.getMenuItemTurnReport().setOnAction(event -> eventBus.post(new ShowTurnReportEvent()));
        view.getMenuItemStatistics().setOnAction(event -> eventBus.post(new ShowStatisticsEvent()));
        view.getMenuItemGoals().setOnAction(event -> eventBus.post(new ShowGoalsEvent()));
//        view.getMenuItemPlowField().setOnAction(event -> plowFieldEventController.fireEvent());
        /**
         * Following section define visibility of menu items.
         */
//        tileWasSelectedController.addRunLaterListener(event -> onFocusedTileEvent(event));
//        endMoveController.addRunLaterListener(this::onEndMoveEvent);
//        selectedUnitWasChangedController.addRunLaterListener(this::onSelectedUnitChanged);
        initialSetting();
    }

    @Subscribe
    private void onExitGame(@SuppressWarnings("unused") final ExitGameEvent event) {
        view.getMenuItemExitGame().setDisable(true);
        view.getMenuItemSaveGame().setDisable(true);
        view.getMenuItemCenterView().setDisable(true);
        view.getMenuItemMove().setDisable(true);
        view.getMenuItemEurope().setDisable(true);
        view.getMenuItemTurnReport().setDisable(true);
        view.getMenuItemStatistics().setDisable(true);
        view.getMenuItemGoals().setDisable(true);
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
        view.getMenuItemPlowField().setDisable(true);
        view.getMenuItemCenterView().setDisable(true);
        view.getMenuItemEurope().setDisable(true);
        view.getMenuItemTurnReport().setDisable(true);
        view.getMenuItemStatistics().setDisable(true);
        view.getMenuItemGoals().setDisable(true);
    }

    @Subscribe
    private void onIndependenceWasDeclared(
            @SuppressWarnings("unused") final IndependenceWasDeclaredEvent event) {
        view.getMenuItemDeclareIndependence().setDisable(true);
    }

    @Subscribe
    private void onUnitMovedStep(
            @SuppressWarnings("unused") final UnitMovedStepStartedEvent event) {
        view.getMenuItemNextUnit().setDisable(true);
    }

    @Subscribe
    private void onUnitMoveFinished(@SuppressWarnings("unused") final UnitMoveFinishedEvent event) {
        view.getMenuItemNextUnit().setDisable(false);
    }

    void onEndMoveEvent(@SuppressWarnings("unused") final EndMoveEvent event) {
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
    @Subscribe
    private void onFocusedTileEvent(final TileWasSelectedEvent event) {
        view.getMenuItemCenterView().setDisable(false);
        view.getMenuItemMove().setDisable(true);
        setBuildColony();
    }

    @Subscribe
    private void onGameFinihedEvent(@SuppressWarnings("unused") final QuitGameEvent exitGameEvent) {
        view.getMenuItemEurope().setDisable(true);
        view.getMenuItemTurnReport().setDisable(true);
        view.getMenuItemStatistics().setDisable(true);
        view.getMenuItemGoals().setDisable(true);
        view.getMenuItemNextUnit().setDisable(true);
        view.getMenuItemBuildColony().setDisable(true);
    }

    @Subscribe
    private void onTurnStartedEvent(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            view.getMenuItemCenterView().setDisable(false);
            if (selectedUnitManager.isSelectedUnitMoveable()) {
                view.getMenuItemMove().setDisable(false);
            } else {
                view.getMenuItemMove().setDisable(true);
            }
            view.getMenuItemExitGame().setDisable(false);
            view.getMenuItemSaveGame().setDisable(false);
            view.getMenuItemEurope().setDisable(false);
            view.getMenuItemTurnReport().setDisable(false);
            view.getMenuItemStatistics().setDisable(false);
            view.getMenuItemGoals().setDisable(false);
            view.getMenuItemNextUnit().setDisable(false);
            view.getMenuItemDeclareIndependence()
                    .setDisable(event.getPlayer().isDeclaredIndependence());
        } else {
            view.getMenuItemExitGame().setDisable(true);
            view.getMenuItemSaveGame().setDisable(true);
            view.getMenuItemCenterView().setDisable(true);
            view.getMenuItemMove().setDisable(true);
            view.getMenuItemEurope().setDisable(true);
            view.getMenuItemTurnReport().setDisable(true);
            view.getMenuItemStatistics().setDisable(true);
            view.getMenuItemGoals().setDisable(true);
            view.getMenuItemNextUnit().setDisable(true);
        }
        setBuildColony();
        setPlowFiled();
    }

    @Subscribe
    private void onSelectedUnitChanged(
            @SuppressWarnings("unused") final SelectedUnitWasChangedEvent event) {
        setBuildColony();
        setMoveUnit();
        setPlowFiled();
    }

    /**
     * <p>
     * It's not necessary to have it as lambda, it could void method. BiConsumer
     * is not used anywhere else.
     * </p>
     */
    private final BiConsumer<MenuItem, SelectedUnitManager> eval = (menuItem,
            selectedUnitManager) -> {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            menuItem.setDisable(unit.getActionPoints() == 0);
        } else {
            menuItem.setDisable(true);
        }
    };

    private void setMoveUnit() {
        eval.accept(view.getMenuItemMove(), selectedUnitManager);
    }

    private void setPlowFiled() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            view.getMenuItemPlowField().setDisable(!unit.canPlowFiled());
        } else {
            view.getMenuItemPlowField().setDisable(true);
        }
    }

    private void setBuildColony() {
        if (selectedUnitManager.getSelectedUnit().isPresent()) {
            final Unit unit = selectedUnitManager.getSelectedUnit().get();
            if (unit.getType().canBuildColony()) {
                final Optional<Colony> oColony = gameModelController.getModel()
                        .getColonyAt(unit.getLocation());
                if (oColony.isPresent()) {
                    view.getMenuItemBuildColony().setDisable(true);
                } else {
                    view.getMenuItemBuildColony().setDisable(unit.getActionPoints() == 0);
                }
            } else {
                view.getMenuItemBuildColony().setDisable(true);
            }
        } else {
            view.getMenuItemBuildColony().setDisable(true);
        }
    }

}
