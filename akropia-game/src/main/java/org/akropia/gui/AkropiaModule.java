package org.akropia.gui;

import org.akropia.gui.buttonpanel.NextTurnListener;
import org.akropia.gui.dialog.ApplicationController;
import org.akropia.gui.dialog.ChooseGoodsDialog;
import org.akropia.gui.dialog.DialogDestroyColony;
import org.akropia.gui.dialog.DialogFight;
import org.akropia.gui.dialog.DialogMessage;
import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.BuildColonyListener;
import org.akropia.gui.event.ChangeLanguageListenerFileChooser;
import org.akropia.gui.event.ChangeLanguageListenerPreferences;
import org.akropia.gui.event.DeclareIndependenceListener;
import org.akropia.gui.event.ClearForestEventListener;
import org.akropia.gui.event.BuildRoadEventListener;
import org.akropia.gui.event.PlowFieldEventListener;
import org.akropia.gui.event.QuitGameListener;
import org.akropia.gui.event.ShowGridListenerPreferences;
import org.akropia.gui.event.model.ArtifitialPlayersManager;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.event.model.MissionEventListener;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.MainPanelPresenter;
import org.akropia.gui.screen.MainPanelView;
import org.akropia.gui.screen.campaign.CampaignPresenter;
import org.akropia.gui.screen.campaign.CampaignView;
import org.akropia.gui.screen.campaign.ScreenCampaignPresenter;
import org.akropia.gui.screen.colonizopedia.ScreenColonizopedia;
import org.akropia.gui.screen.congress.ScreenCongress;
import org.akropia.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.akropia.gui.screen.nativesettlements.ShowNativeSettlementsListener;
import org.akropia.gui.screen.colonizopedia.ShowHelpListener;
import org.akropia.gui.screen.colony.ColonyDialogCallback;
import org.akropia.gui.screen.colony.ColonyPanel;
import org.akropia.gui.screen.colony.PanelColonyDockBehaviour;
import org.akropia.gui.screen.colony.ScreenColonyPresenter;
import org.akropia.gui.screen.colony.buildingqueue.QueueController;
import org.akropia.gui.screen.colony.buildingqueue.QueueDialog;
import org.akropia.gui.screen.colony.buildingqueue.ShowQueueDialogListener;
import org.akropia.gui.screen.editor.EditorMouseListener;
import org.akropia.gui.screen.editor.ScreenEditorPresenter;
import org.akropia.gui.screen.europe.BuyUnitsDialog;
import org.akropia.gui.screen.europe.EuropeButtonsPanelController;
import org.akropia.gui.screen.europe.EuropeCallback;
import org.akropia.gui.screen.europe.MercenaryOfferController;
import org.akropia.gui.screen.statistics.HallOfFameListener;
import org.akropia.gui.screen.statistics.ScoreBoundaryController;
import org.akropia.gui.screen.europe.PanelEuropeDockBehavior;
import org.akropia.gui.screen.europe.PanelEuropeGoods;
import org.akropia.gui.screen.europe.PanelHighSeas;
import org.akropia.gui.screen.europe.PanelPortPier;
import org.akropia.gui.screen.europe.RecruiteUnitsDialog;
import org.akropia.gui.screen.europe.ScreenEuropePresenter;
import org.akropia.gui.screen.game.ScreenGamePresenter;
import org.akropia.gui.screen.game.components.ButtonsGamePanelController;
import org.akropia.gui.screen.game.components.RightPanelPresenter;
import org.akropia.gui.screen.game.components.RightPanelView;
import org.akropia.gui.screen.game.components.StatusBar;
import org.akropia.gui.screen.game.gamepanel.AnimationManager;
import org.akropia.gui.screen.game.gamepanel.ExcludePainting;
import org.akropia.gui.screen.game.gamepanel.GamePanelController;
import org.akropia.gui.screen.game.gamepanel.GamePanelMouseListener;
import org.akropia.gui.screen.game.gamepanel.GamePanelPresenter;
import org.akropia.gui.screen.game.gamepanel.ModeController;
import org.akropia.gui.screen.game.gamepanel.MouseOverTileListener;
import org.akropia.gui.screen.game.gamepanel.MouseOverTileManager;
import org.akropia.gui.screen.game.gamepanel.MoveModeSupport;
import org.akropia.gui.screen.game.gamepanel.ScrollToSelectedUnit;
import org.akropia.gui.screen.game.gamepanel.ScrollingManager;
import org.akropia.gui.screen.game.gamepanel.SelectedTileManager;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.screen.game.gamepanel.TileWasSelectedListener;
import org.akropia.gui.screen.game.gamepanel.UnitAttackedEventListener;
import org.akropia.gui.screen.game.gamepanel.UnitMoveFinishedListener;
import org.akropia.gui.screen.game.gamepanel.UnitMovedListener;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.gui.screen.goals.GoalsButtonsPanelController;
import org.akropia.gui.screen.goals.ScreenGoalsPresenter;
import org.akropia.gui.screen.market.ScreenMarketPresenter;
import org.akropia.gui.screen.menu.ButtonsPanelPresenter;
import org.akropia.gui.screen.menu.ButtonsPanelView;
import org.akropia.gui.screen.menu.ExitGameListener;
import org.akropia.gui.screen.menu.GameFinishedListener;
import org.akropia.gui.screen.menu.ScreenMenu;
import org.akropia.gui.screen.menu.ScreenMenuPresenter;
import org.akropia.gui.screen.setting.ScreenSettingPresenter;
import org.akropia.gui.screen.setting.SettingAnimationSpeedPresenter;
import org.akropia.gui.screen.setting.SettingButtonsPresenter;
import org.akropia.gui.screen.setting.SettingLanguagePresenter;
import org.akropia.gui.screen.setting.SettingShowGridPresenter;
import org.akropia.gui.screen.setting.SettingVolumePresenter;
import org.akropia.gui.screen.statistics.ScreenStatisticsPresenter;
import org.akropia.gui.screen.statistics.StatisticsButtonsPanelController;
import org.akropia.gui.screen.turnreport.ScreenTurnReportPresenter;
import org.akropia.gui.screen.turnreport.ShowRumorReportAfterMove;
import org.akropia.gui.screen.turnreport.ShowTurnEvensOnTurnStartedEvent;
import org.akropia.gui.screen.turnreport.TurnReportButtonsPanelController;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.diagnostics.DiagnosticSubscriberExceptionHandler;
import org.akropia.gui.util.ApplicationInfo;
import org.akropia.gui.util.FontService;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.PaintService;
import org.akropia.gui.util.PathPlanning;
import org.akropia.gui.util.StreamReader;
import org.akropia.gui.util.TurnStartedListener;
import org.akropia.gui.util.UnitUtil;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.PersistingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.ProvisionListener;

public final class AkropiaModule extends AbstractModule {

    private final Logger LOGGER = LoggerFactory.getLogger(AkropiaModule.class);

    private final EventBus eventBus;

    private final DiagnosticLog diagnosticLog;

    public AkropiaModule() {
        diagnosticLog = new DiagnosticLog(DiagnosticLog.defaultDirectory(),
                new ApplicationInfo().getDiagnosticVersion());
        eventBus = new EventBus(
                new DiagnosticSubscriberExceptionHandler(diagnosticLog));
    }

    @Override
    protected void configure() {

        bind(EventBus.class).toInstance(eventBus);
        bind(DiagnosticLog.class).toInstance(diagnosticLog);
        bind(MainStageBuilder.class).in(Singleton.class);
        bind(UnitUtil.class).in(Singleton.class);

        bind(GamePreferences.class).in(Singleton.class);
        bind(PathPlanning.class).in(Singleton.class);
        bind(ViewUtil.class).in(Singleton.class);
        bind(LocalizationHelper.class).in(Singleton.class);
        bind(AnimationManager.class).in(Singleton.class);
        bind(ScrollingManager.class).in(Singleton.class);
        bind(FontService.class).asEagerSingleton();

        /**
         * PanelButtons
         */
        bind(NextTurnListener.class).asEagerSingleton();
        bind(ButtonsGamePanelController.class).asEagerSingleton();
        bind(DialogFight.class).asEagerSingleton();

        /**
         * Dialogs
         */
        bind(DialogDestroyColony.class).in(Singleton.class);
        bind(ChooseGoodsDialog.class).in(Singleton.class);
        bind(DialogMessage.class).in(Singleton.class);
        bind(MissionEventListener.class).asEagerSingleton();
        bind(SettingButtonsPresenter.class).asEagerSingleton();

        /**
         * Event controllers.
         */
        bind(ApplicationController.class).in(Singleton.class);
        bind(GameModelController.class).in(Singleton.class);
        bind(ArtifitialPlayersManager.class).in(Singleton.class);

        bind(TurnStartedListener.class).asEagerSingleton();
        bind(TileWasSelectedListener.class).asEagerSingleton();
        bind(SettingLanguagePresenter.class).asEagerSingleton();
        bind(SettingShowGridPresenter.class).asEagerSingleton();
        bind(SettingAnimationSpeedPresenter.class).asEagerSingleton();
        bind(SettingVolumePresenter.class).asEagerSingleton();

        bind(ExitGameListener.class).asEagerSingleton();
        bind(GameFinishedListener.class).asEagerSingleton();
        bind(HallOfFameListener.class).asEagerSingleton();

        bind(ShowTurnEvensOnTurnStartedEvent.class).asEagerSingleton();
        bind(ShowRumorReportAfterMove.class).asEagerSingleton();

        /**
         * Initialize MVP classes
         */
        bind(MainPanelView.class).in(Singleton.class);
        bind(MainPanelPresenter.class).in(Singleton.class);

        bind(ScreenMenu.class).in(Singleton.class);

        bind(ButtonsPanelView.class).in(Singleton.class);
        bind(ButtonsPanelPresenter.class).asEagerSingleton();

        bind(CampaignView.class).in(Singleton.class);
        bind(CampaignPresenter.Display.class).to(CampaignView.class)
                .in(Singleton.class);
        bind(CampaignPresenter.class).asEagerSingleton();

        bind(StatusBar.class).annotatedWith(Names.named("GamePanel"))
                .to(StatusBar.class).asEagerSingleton();

        bind(RightPanelView.class).in(Singleton.class);
        bind(RightPanelPresenter.class).asEagerSingleton();

        bind(UnitsPanel.class).in(Singleton.class);
        bind(PersistingService.class).in(Singleton.class);

        bind(ScreenSettingPresenter.class).asEagerSingleton();
        bind(ScreenCampaignPresenter.class).asEagerSingleton();
        bind(ScreenMenuPresenter.class).asEagerSingleton();

        /**
         * Colony dialog
         */
        bind(ColonyPanel.class).in(Singleton.class);
        bind(ColonyDialogCallback.class).to(ScreenColonyPresenter.class)
                .in(Singleton.class);
        bind(PanelColonyDockBehaviour.class).in(Singleton.class);
        bind(QueueDialog.class);
        bind(QueueController.class).in(Singleton.class);
        bind(ShowQueueDialogListener.class).asEagerSingleton();
        bind(org.akropia.gui.screen.colony.DialogColonyJobChoices.class)
                .asEagerSingleton();

        /**
         * Europe dialog
         */
        bind(EuropeCallback.class).to(ScreenEuropePresenter.class)
                .in(Singleton.class);
        bind(ScreenEuropePresenter.class).asEagerSingleton();
        bind(EuropeButtonsPanelController.class).asEagerSingleton();
        bind(MercenaryOfferController.class).asEagerSingleton();
        bind(ScoreBoundaryController.class).asEagerSingleton();
        bind(PanelHighSeas.class);
        bind(PanelPortPier.class).in(Singleton.class);
        bind(PanelEuropeGoods.class).in(Singleton.class);
        bind(RecruiteUnitsDialog.class).in(Singleton.class);
        bind(BuyUnitsDialog.class).in(Singleton.class);
        bind(PanelEuropeDockBehavior.class).in(Singleton.class);
        bind(ScreenMarketPresenter.class).asEagerSingleton();

        /**
         * Turn report.
         */
        bind(ScreenStatisticsPresenter.class).asEagerSingleton();
        bind(ScreenTurnReportPresenter.class).asEagerSingleton();
        bind(TurnReportButtonsPanelController.class).asEagerSingleton();

        /**
         * Goals.
         */
        bind(ScreenGoalsPresenter.class).asEagerSingleton();
        bind(GoalsButtonsPanelController.class).asEagerSingleton();

        /**
         * Game screen.
         */
        bind(ScreenGamePresenter.class).asEagerSingleton();
        bind(VisibleAreaService.class).annotatedWith(Names.named("game"))
                .toInstance(new VisibleAreaService(true));
        bind(GamePanelPresenter.class).asEagerSingleton();
        bind(GamePanelMouseListener.class).asEagerSingleton();
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
        bind(UnitMoveFinishedListener.class).asEagerSingleton();
        bind(UnitMovedListener.class).asEagerSingleton();
        bind(ScrollToSelectedUnit.class).asEagerSingleton();

        /**
         * Game editor.
         */
        bind(ScreenEditorPresenter.class).asEagerSingleton();
        bind(VisibleAreaService.class).annotatedWith(Names.named("editor"))
                .toInstance(new VisibleAreaService());
        bind(EditorMouseListener.class).asEagerSingleton();

        /**
         * Statistics.
         */
        bind(StatisticsButtonsPanelController.class).asEagerSingleton();

        /**
         * Rest of UI
         */
        bind(ScreenColonizopedia.class).in(Singleton.class);
        bind(ScreenCongress.class).in(Singleton.class);
        bind(NativeSettlementsDialog.class).in(Singleton.class);

        /**
         * Load events manually
         */
        bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
        bind(ChangeLanguageListenerFileChooser.class).asEagerSingleton();
        bind(ShowGridListenerPreferences.class).asEagerSingleton();
        bind(ShowHelpListener.class).asEagerSingleton();
        bind(ShowNativeSettlementsListener.class).asEagerSingleton();
        bind(DeclareIndependenceListener.class).asEagerSingleton();
        bind(PlowFieldEventListener.class).asEagerSingleton();
        bind(BuildRoadEventListener.class).asEagerSingleton();
        bind(ClearForestEventListener.class).asEagerSingleton();
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
                    LOGGER.debug("Registering listener class '{}'",
                            instance.getClass().getName());
                }
            }
        });
    }

    @Provides
    @Singleton
    I18n makeI18n(final GamePreferences gamePreferences,
            final StreamReader streamReader) {
        return I18n.builder().setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setStreamReader(streamReader)
                .setDefaultLocale(gamePreferences.getLocale()).build();
    }

}
