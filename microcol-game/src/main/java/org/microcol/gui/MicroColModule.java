package org.microcol.gui;

import org.microcol.gui.colonizopedia.Colonizopedia;
import org.microcol.gui.colony.ColonyDialog;
import org.microcol.gui.colony.ColonyDialogCallback;
import org.microcol.gui.colony.PanelColonyDockBehaviour;
import org.microcol.gui.colony.PanelColonyFields;
import org.microcol.gui.colony.PanelColonyGoods;
import org.microcol.gui.colony.PanelColonyStructures;
import org.microcol.gui.colony.PanelOutsideColony;
import org.microcol.gui.colony.UnitMovedOutsideColonyController;
import org.microcol.gui.europe.BuyUnitsDialog;
import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.europe.EuropeDialogCallback;
import org.microcol.gui.europe.PanelEuropeDockBehavior;
import org.microcol.gui.europe.PanelEuropeGoods;
import org.microcol.gui.europe.PanelHighSeas;
import org.microcol.gui.europe.PanelPortPier;
import org.microcol.gui.europe.RecruiteUnitsDialog;
import org.microcol.gui.event.AboutGameListenerImpl;
import org.microcol.gui.event.AnimationSpeedChangedListenerPreferences;
import org.microcol.gui.event.BuildColonyListener;
import org.microcol.gui.event.ChangeLanguageListenerPreferences;
import org.microcol.gui.event.ChangeLanguageListenerText;
import org.microcol.gui.event.DeclareIndependenceListener;
import org.microcol.gui.event.EventInitializer;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.ShowGridListenerPreferences;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.VolumeChangedListenerPreferences;
import org.microcol.gui.event.model.ArtifitialPlayersManager;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.gui.gamemenu.CampaignPanelPresenter;
import org.microcol.gui.gamemenu.CampaignPanelView;
import org.microcol.gui.gamemenu.ExitGameListener;
import org.microcol.gui.gamemenu.GameFinishedListener;
import org.microcol.gui.gamemenu.GameMenuPanelPresenter;
import org.microcol.gui.gamemenu.GameMenuPanelView;
import org.microcol.gui.gamemenu.ShowDefaultCampaignMenuControler;
import org.microcol.gui.gamemenu.ShowDefaultCampaignMenuListener;
import org.microcol.gui.gamepanel.AnimationIsDoneController;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.gamepanel.AnimationStartedController;
import org.microcol.gui.gamepanel.ExcludePainting;
import org.microcol.gui.gamepanel.GamePanelController;
import org.microcol.gui.gamepanel.GamePanelPresenter;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.gamepanel.MapManager;
import org.microcol.gui.gamepanel.ModeController;
import org.microcol.gui.gamepanel.MouseOverTileChangedController;
import org.microcol.gui.gamepanel.MouseOverTileListener;
import org.microcol.gui.gamepanel.MouseOverTileManager;
import org.microcol.gui.gamepanel.MoveModeSupport;
import org.microcol.gui.gamepanel.OneTurnMoveHighlighter;
import org.microcol.gui.gamepanel.PaintService;
import org.microcol.gui.gamepanel.PaneCanvas;
import org.microcol.gui.gamepanel.ScrollToSelectedUnit;
import org.microcol.gui.gamepanel.ScrollingManager;
import org.microcol.gui.gamepanel.SelectedTileManager;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.gamepanel.SelectedUnitWasChangedController;
import org.microcol.gui.gamepanel.TileWasSelectedController;
import org.microcol.gui.gamepanel.TileWasSelectedListener;
import org.microcol.gui.gamepanel.UnitAttackedEventListener;
import org.microcol.gui.gamepanel.UnitMoveFinishedListener;
import org.microcol.gui.gamepanel.UnitMovedListener;
import org.microcol.gui.gamepanel.VisibleArea;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.IceCoastMapGenerator;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.mainmenu.AboutGameEventController;
import org.microcol.gui.mainmenu.AnimationSpeedChangeController;
import org.microcol.gui.mainmenu.BuildColonyEventController;
import org.microcol.gui.mainmenu.CenterViewController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.DeclareIndependenceController;
import org.microcol.gui.mainmenu.ExitGameController;
import org.microcol.gui.mainmenu.MainMenuDevelopment;
import org.microcol.gui.mainmenu.MainMenuPresenter;
import org.microcol.gui.mainmenu.MainMenuView;
import org.microcol.gui.mainmenu.PlowFieldEventController;
import org.microcol.gui.mainmenu.PlowFieldEventListener;
import org.microcol.gui.mainmenu.QuitGameController;
import org.microcol.gui.mainmenu.SelectNextUnitController;
import org.microcol.gui.mainmenu.ShowGoalsController;
import org.microcol.gui.mainmenu.ShowGridController;
import org.microcol.gui.mainmenu.ShowStatisticsController;
import org.microcol.gui.mainmenu.ShowTurnReportController;
import org.microcol.gui.mainmenu.VolumeChangeController;
import org.microcol.gui.statistics.ShowStatisticsDialogListener;
import org.microcol.gui.statistics.StatisticsDialog;
import org.microcol.gui.turnreport.ShowTurnEvensOnTurnStartedEvent;
import org.microcol.gui.turnreport.ShowTurnReportListener;
import org.microcol.gui.turnreport.TurnReportDialog;
import org.microcol.gui.util.ApplicationInfo;
import org.microcol.gui.util.FontService;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PersistentService;
import org.microcol.gui.util.PersistingTool;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.TurnStartedListener;
import org.microcol.gui.util.ViewUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

public final class MicroColModule extends AbstractModule {

    private final Logger LOGGER = LoggerFactory.getLogger(MicroColModule.class);

    private final EventBus eventBus;

    public MicroColModule() {
        eventBus = new EventBus("microCol");
    }

    @Override
    protected void configure() {

        bind(EventBus.class).toInstance(eventBus);
        bind(MainStageBuilder.class).in(Singleton.class);
        bind(StatusBarMessageController.class).in(Singleton.class);

        bind(GamePreferences.class).in(Singleton.class);
        bind(PathPlanning.class).in(Singleton.class);
        bind(ImageProvider.class).in(Singleton.class);
        bind(ViewUtil.class).in(Singleton.class);
        bind(LocalizationHelper.class).in(Singleton.class);
        bind(PersistentService.class).in(Singleton.class);
        bind(AnimationManager.class).in(Singleton.class);
        bind(ScrollingManager.class).in(Singleton.class);
        bind(WasdController.class).in(Singleton.class);
        bind(MapManager.class).in(Singleton.class);
        bind(GrassCoastMapGenerator.class).in(Singleton.class);
        bind(IceCoastMapGenerator.class).in(Singleton.class);
        bind(FontService.class).asEagerSingleton();
        bind(ApplicationInfo.class).in(Singleton.class);

        /**
         * Dialogs
         */
        bind(AboutDialog.class).in(Singleton.class);
        bind(DialogDestroyColony.class).in(Singleton.class);
        bind(ChooseGoodAmount.class).in(Singleton.class);
        bind(DialogMessage.class).in(Singleton.class);
        bind(MissionCallBack.class).in(Singleton.class);

        /**
         * Event controllers.
         */
        bind(ApplicationController.class).in(Singleton.class);
        bind(KeyController.class).in(Singleton.class);
        bind(TileWasSelectedController.class).in(Singleton.class);
        bind(AboutGameEventController.class).in(Singleton.class);
        bind(ChangeLanguageController.class).in(Singleton.class);
        bind(StartMoveController.class).in(Singleton.class);
        bind(VolumeChangeController.class).in(Singleton.class);
        bind(AnimationSpeedChangeController.class).in(Singleton.class);
        bind(ShowGridController.class).in(Singleton.class);
        bind(CenterViewController.class).in(Singleton.class);
        bind(QuitGameController.class).in(Singleton.class);
        bind(MouseOverTileChangedController.class).in(Singleton.class);
        bind(DeclareIndependenceController.class).in(Singleton.class);
        bind(BuildColonyEventController.class).in(Singleton.class);
        bind(SelectNextUnitController.class).in(Singleton.class);
        bind(AnimationIsDoneController.class).in(Singleton.class);
        bind(AnimationStartedController.class).in(Singleton.class);
        bind(UnitMovedOutsideColonyController.class).in(Singleton.class);
        bind(ExitGameController.class).in(Singleton.class);
        bind(SelectedUnitWasChangedController.class).in(Singleton.class);
        bind(ShowDefaultCampaignMenuControler.class).in(Singleton.class);
        bind(GameModelController.class).in(Singleton.class);
        bind(ArtifitialPlayersManager.class).in(Singleton.class);
        bind(ShowTurnReportController.class).in(Singleton.class);
        bind(ShowStatisticsController.class).in(Singleton.class);
        bind(ShowGoalsController.class).in(Singleton.class);
        bind(PlowFieldEventController.class).in(Singleton.class);

        bind(TurnStartedListener.class).asEagerSingleton();
        bind(TileWasSelectedListener.class).asEagerSingleton();
        bind(ShowDefaultCampaignMenuListener.class).asEagerSingleton();
        bind(PlowFieldEventListener.class).asEagerSingleton();

        bind(ExitGameListener.class).asEagerSingleton();
        bind(GameFinishedListener.class).asEagerSingleton();
        bind(ShowTurnReportListener.class).asEagerSingleton();

        bind(TurnReportDialog.class).in(Singleton.class);
        bind(ShowTurnEvensOnTurnStartedEvent.class).asEagerSingleton();

        bind(StatisticsDialog.class).in(Singleton.class);
        bind(ShowStatisticsDialogListener.class).asEagerSingleton();
        bind(MissionGoalsShowListener.class).asEagerSingleton();

        /**
         * Initialize MVP classes
         */
        bind(MainPanelView.class).in(Singleton.class);
        bind(MainPanelPresenter.class).in(Singleton.class);

        // gui.gamepanel package binding
        bind(GamePanelView.class).in(Singleton.class);
        bind(GamePanelPresenter.class).asEagerSingleton();
        bind(OneTurnMoveHighlighter.class).in(Singleton.class);
        bind(SelectedTileManager.class).in(Singleton.class);
        bind(MoveModeSupport.class).in(Singleton.class);
        bind(MouseOverTileListener.class).asEagerSingleton();
        bind(PaintService.class).in(Singleton.class);
        bind(UnitAttackedEventListener.class).asEagerSingleton();
        bind(MouseOverTileManager.class).in(Singleton.class);
        bind(ModeController.class).in(Singleton.class);
        bind(SelectedUnitManager.class).in(Singleton.class);
        bind(ExcludePainting.class).in(Singleton.class);
        bind(GamePanelController.class).in(Singleton.class);
        bind(VisibleArea.class).in(Singleton.class);
        bind(PaneCanvas.class).in(Singleton.class);
        bind(UnitMoveFinishedListener.class).asEagerSingleton();
        bind(UnitMovedListener.class).asEagerSingleton();
        bind(ScrollToSelectedUnit.class).asEagerSingleton();

        bind(GameMenuPanelView.class).in(Singleton.class);
        bind(GameMenuPanelPresenter.class).asEagerSingleton();

        bind(CampaignPanelView.class).in(Singleton.class);
        bind(CampaignPanelPresenter.Display.class).to(CampaignPanelView.class).in(Singleton.class);
        bind(CampaignPanelPresenter.class).asEagerSingleton();

        bind(StatusBarView.class).in(Singleton.class);
        bind(StatusBarPresenter.Display.class).to(StatusBarView.class).in(Singleton.class);
        bind(StatusBarPresenter.class).asEagerSingleton();

        bind(MainMenuView.class).in(Singleton.class);
        bind(MainMenuPresenter.class).asEagerSingleton();
        bind(MainMenuDevelopment.class).in(Singleton.class);

        bind(RightPanelView.class).in(Singleton.class);
        bind(RightPanelPresenter.class).asEagerSingleton();
        bind(RightPanelNextButtonControll.class).asEagerSingleton();
        bind(TilePainter.class).in(Singleton.class);

        bind(UnitsPanel.class).in(Singleton.class);
        bind(PersistingDialog.class).in(Singleton.class);

        /**
         * Colony dialog
         */
        bind(ColonyDialog.class).in(Singleton.class);
        bind(ColonyDialogCallback.class).to(ColonyDialog.class).in(Singleton.class);
        bind(PanelColonyFields.class).in(Singleton.class);
        bind(PanelColonyStructures.class).in(Singleton.class);
        bind(PanelOutsideColony.class).in(Singleton.class);
        bind(PanelColonyGoods.class).in(Singleton.class);
        bind(PanelColonyDockBehaviour.class).in(Singleton.class);

        /**
         * Europe dialog
         */
        bind(EuropeDialog.class).in(Singleton.class);
        bind(EuropeDialogCallback.class).to(EuropeDialog.class).in(Singleton.class);
        bind(PanelHighSeas.class);
        bind(PanelPortPier.class).in(Singleton.class);
        bind(PanelEuropeGoods.class).in(Singleton.class);
        bind(RecruiteUnitsDialog.class).in(Singleton.class);
        bind(BuyUnitsDialog.class).in(Singleton.class);
        bind(PanelEuropeDockBehavior.class).in(Singleton.class);

        /**
         * Rest of UI
         */
        bind(Colonizopedia.class).in(Singleton.class);
        bind(PreferencesVolume.class).in(Singleton.class);
        bind(PreferencesAnimationSpeed.class).in(Singleton.class);

        /**
         * Load events manually
         */
        bind(AboutGameListenerImpl.class).asEagerSingleton();
        bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
        bind(ChangeLanguageListenerText.class).asEagerSingleton();
        bind(VolumeChangedListenerPreferences.class).asEagerSingleton();
        bind(AnimationSpeedChangedListenerPreferences.class).asEagerSingleton();
        bind(ShowGridListenerPreferences.class).asEagerSingleton();
        bind(DeclareIndependenceListener.class).asEagerSingleton();
        bind(BuildColonyListener.class).in(Singleton.class);

        bind(MusicPlayer.class).in(Singleton.class);
        bind(MusicController.class).in(Singleton.class);

        bind(EventInitializer.class).in(Singleton.class);
        bind(PersistingTool.class).in(Singleton.class);

        bindListener(Matchers.any(), new ProvisionListener() {

            @Override
            public <T> void onProvision(ProvisionInvocation<T> provision) {
                final T instance = provision.provision();
                if (instance.getClass().isAnnotationPresent(Listener.class)) {
                    eventBus.register(instance);
                    LOGGER.debug("Registering listener class '{}'", instance.getClass().getName());
                }
            }
        });
    }

    @Provides
    @Singleton
    Text makeText(final GamePreferences gamePreferences) {
        return new Text(gamePreferences.getLocale());
    }

}
