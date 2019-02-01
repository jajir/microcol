package org.microcol.gui;

import org.microcol.gui.buildingqueue.QueueController;
import org.microcol.gui.buildingqueue.QueueDialog;
import org.microcol.gui.buttonpanel.ButtonsGamePanelController;
import org.microcol.gui.buttonpanel.NextTurnListener;
import org.microcol.gui.colonizopedia.ColonizopediaDialog;
import org.microcol.gui.colony.ColonyDialogCallback;
import org.microcol.gui.colony.ColonyMenuPanelPresenter;
import org.microcol.gui.colony.ColonyPanel;
import org.microcol.gui.colony.PanelBuildingQueue;
import org.microcol.gui.colony.PanelColonyDockBehaviour;
import org.microcol.gui.colony.PanelColonyFields;
import org.microcol.gui.colony.PanelColonyGoods;
import org.microcol.gui.colony.PanelColonyStructures;
import org.microcol.gui.colony.PanelOutsideColony;
import org.microcol.gui.europe.BuyUnitsDialog;
import org.microcol.gui.europe.ChooseGoodAmountDialog;
import org.microcol.gui.europe.EuropeButtonsPanelController;
import org.microcol.gui.europe.EuropeCallback;
import org.microcol.gui.europe.EuropeMenuPanelPresenter;
import org.microcol.gui.europe.PanelEuropeDockBehavior;
import org.microcol.gui.europe.PanelEuropeGoods;
import org.microcol.gui.europe.PanelHighSeas;
import org.microcol.gui.europe.PanelPortPier;
import org.microcol.gui.europe.RecruiteUnitsDialog;
import org.microcol.gui.event.AboutGameListenerImpl;
import org.microcol.gui.event.AnimationSpeedChangedListenerPreferences;
import org.microcol.gui.event.BuildColonyListener;
import org.microcol.gui.event.ChangeLanguageListenerFileChooser;
import org.microcol.gui.event.ChangeLanguageListenerPreferences;
import org.microcol.gui.event.DeclareIndependenceListener;
import org.microcol.gui.event.PlowFieldEventListener;
import org.microcol.gui.event.QuitGameListener;
import org.microcol.gui.event.ShowGridListenerPreferences;
import org.microcol.gui.event.VolumeChangedListenerPreferences;
import org.microcol.gui.event.model.ArtifitialPlayersManager;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.gui.gamemenu.ButtonsPanelPresenter;
import org.microcol.gui.gamemenu.ButtonsPanelView;
import org.microcol.gui.gamemenu.CampaignMenuPanelPresenter;
import org.microcol.gui.gamemenu.CampaignMenuPanelView;
import org.microcol.gui.gamemenu.ExitGameListener;
import org.microcol.gui.gamemenu.GameFinishedListener;
import org.microcol.gui.gamemenu.GameMenuPanel;
import org.microcol.gui.gamemenu.SettingAnimationSpeedPresenter;
import org.microcol.gui.gamemenu.SettingButtonsPresenter;
import org.microcol.gui.gamemenu.SettingLanguagePresenter;
import org.microcol.gui.gamemenu.SettingShowGridPresenter;
import org.microcol.gui.gamemenu.SettingVolumePresenter;
import org.microcol.gui.gamemenu.ShowDefaultCampaignMenuListener;
import org.microcol.gui.gamemenu.ShowGameSettingListener;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.gamepanel.ExcludePainting;
import org.microcol.gui.gamepanel.GamePanelController;
import org.microcol.gui.gamepanel.GamePanelPresenter;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.gamepanel.MapManager;
import org.microcol.gui.gamepanel.ModeController;
import org.microcol.gui.gamepanel.MouseOverTileListener;
import org.microcol.gui.gamepanel.MouseOverTileManager;
import org.microcol.gui.gamepanel.MoveModeSupport;
import org.microcol.gui.gamepanel.OneTurnMoveHighlighter;
import org.microcol.gui.gamepanel.PaneCanvas;
import org.microcol.gui.gamepanel.ScrollToSelectedUnit;
import org.microcol.gui.gamepanel.ScrollingManager;
import org.microcol.gui.gamepanel.SelectedTileManager;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.gamepanel.TileWasSelectedListener;
import org.microcol.gui.gamepanel.UnitAttackedEventListener;
import org.microcol.gui.gamepanel.UnitMoveFinishedListener;
import org.microcol.gui.gamepanel.UnitMovedListener;
import org.microcol.gui.gamepanel.VisibleArea;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.IceCoastMapGenerator;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.mainscreen.MainPanelPresenter;
import org.microcol.gui.mainscreen.MainPanelView;
import org.microcol.gui.statistics.ShowStatisticsDialogListener;
import org.microcol.gui.statistics.StatisticsDialog;
import org.microcol.gui.turnreport.ShowTurnEvensOnTurnStartedEvent;
import org.microcol.gui.turnreport.ShowTurnReportListener;
import org.microcol.gui.turnreport.TurnReportDialog;
import org.microcol.gui.util.ApplicationInfo;
import org.microcol.gui.util.FontService;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.PersistentService;
import org.microcol.gui.util.PersistingTool;
import org.microcol.gui.util.TurnStartedListener;
import org.microcol.gui.util.UnitUtil;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
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
        bind(UnitUtil.class).in(Singleton.class);

        bind(GamePreferences.class).in(Singleton.class);
        bind(PathPlanning.class).in(Singleton.class);
        bind(ImageProvider.class).in(Singleton.class);
        bind(ViewUtil.class).in(Singleton.class);
        bind(LocalizationHelper.class).in(Singleton.class);
        bind(PersistentService.class).in(Singleton.class);
        bind(AnimationManager.class).in(Singleton.class);
        bind(ScrollingManager.class).in(Singleton.class);
        bind(MapManager.class).in(Singleton.class);
        bind(GrassCoastMapGenerator.class).in(Singleton.class);
        bind(IceCoastMapGenerator.class).in(Singleton.class);
        bind(FontService.class).asEagerSingleton();
        bind(ApplicationInfo.class).in(Singleton.class);

        /**
         * PanelButtons
         */
        bind(NextTurnListener.class).asEagerSingleton();
        bind(ButtonsGamePanelController.class).asEagerSingleton();

        /**
         * Dialogs
         */
        bind(AboutDialog.class).in(Singleton.class);
        bind(DialogDestroyColony.class).in(Singleton.class);
        bind(ChooseGoodAmountDialog.class).in(Singleton.class);
        bind(DialogMessage.class).in(Singleton.class);
        bind(MissionCallBack.class).in(Singleton.class);
        bind(SettingButtonsPresenter.class).asEagerSingleton();

        /**
         * Event controllers.
         */
        bind(ApplicationController.class).in(Singleton.class);
        bind(GameModelController.class).in(Singleton.class);
        bind(ArtifitialPlayersManager.class).in(Singleton.class);

        bind(TurnStartedListener.class).asEagerSingleton();
        bind(TileWasSelectedListener.class).asEagerSingleton();
        bind(ShowDefaultCampaignMenuListener.class).asEagerSingleton();
        bind(ShowGameSettingListener.class).asEagerSingleton();
        bind(SettingLanguagePresenter.class).asEagerSingleton();
        bind(SettingShowGridPresenter.class).asEagerSingleton();
        bind(SettingAnimationSpeedPresenter.class).asEagerSingleton();
        bind(SettingVolumePresenter.class).asEagerSingleton();

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

        //Game panel
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

        bind(GameMenuPanel.class).in(Singleton.class);

        bind(ButtonsPanelView.class).in(Singleton.class);
        bind(ButtonsPanelPresenter.class).asEagerSingleton();

        bind(CampaignMenuPanelView.class).in(Singleton.class);
        bind(CampaignMenuPanelPresenter.Display.class).to(CampaignMenuPanelView.class)
                .in(Singleton.class);
        bind(CampaignMenuPanelPresenter.class).asEagerSingleton();

        bind(StatusBar.class).annotatedWith(Names.named("GamePanel")).to(StatusBar.class)
                .asEagerSingleton();
        bind(StatusBar.class).annotatedWith(Names.named("Europe")).to(StatusBar.class)
                .asEagerSingleton();
        bind(StatusBar.class).annotatedWith(Names.named("Colony")).to(StatusBar.class)
                .asEagerSingleton();

        bind(RightPanelView.class).in(Singleton.class);
        bind(RightPanelPresenter.class).asEagerSingleton();
        bind(TilePainter.class).in(Singleton.class);

        bind(UnitsPanel.class).in(Singleton.class);
        bind(PersistingDialog.class).in(Singleton.class);

        /**
         * Colony dialog
         */
        bind(ColonyPanel.class).in(Singleton.class);
        bind(ColonyDialogCallback.class).to(ColonyMenuPanelPresenter.class).in(Singleton.class);
        bind(PanelColonyFields.class).in(Singleton.class);
        bind(PanelColonyStructures.class).in(Singleton.class);
        bind(PanelOutsideColony.class).in(Singleton.class);
        bind(PanelColonyGoods.class).in(Singleton.class);
        bind(PanelColonyDockBehaviour.class).in(Singleton.class);
        bind(PanelBuildingQueue.class).in(Singleton.class);
        bind(QueueDialog.class).in(Singleton.class);
        bind(QueueController.class).in(Singleton.class);

        /**
         * Europe dialog
         */
        bind(EuropeCallback.class).to(EuropeMenuPanelPresenter.class).in(Singleton.class);
        bind(EuropeMenuPanelPresenter.class).asEagerSingleton();
        bind(EuropeButtonsPanelController.class).asEagerSingleton();
        bind(PanelHighSeas.class);
        bind(PanelPortPier.class).in(Singleton.class);
        bind(PanelEuropeGoods.class).in(Singleton.class);
        bind(RecruiteUnitsDialog.class).in(Singleton.class);
        bind(BuyUnitsDialog.class).in(Singleton.class);
        bind(PanelEuropeDockBehavior.class).in(Singleton.class);

        /**
         * Rest of UI
         */
        bind(ColonizopediaDialog.class).in(Singleton.class);

        /**
         * Load events manually
         */
        bind(AboutGameListenerImpl.class).asEagerSingleton();
        bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
        bind(ChangeLanguageListenerFileChooser.class).asEagerSingleton();
        bind(VolumeChangedListenerPreferences.class).asEagerSingleton();
        bind(AnimationSpeedChangedListenerPreferences.class).asEagerSingleton();
        bind(ShowGridListenerPreferences.class).asEagerSingleton();
        bind(DeclareIndependenceListener.class).asEagerSingleton();
        bind(PlowFieldEventListener.class).asEagerSingleton();
        bind(BuildColonyListener.class).in(Singleton.class);

        bind(MusicPlayer.class).in(Singleton.class);
        bind(MusicController.class).in(Singleton.class);

        bind(QuitGameListener.class).asEagerSingleton();
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
    I18n makeI18n(final GamePreferences gamePreferences) {
        return I18n.builder().setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(gamePreferences.getLocale()).build();
    }

}
