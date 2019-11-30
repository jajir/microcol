package org.microcol.gui;

import org.microcol.gui.buttonpanel.NextTurnListener;
import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.dialog.DialogDestroyColony;
import org.microcol.gui.dialog.DialogFight;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.event.BuildColonyListener;
import org.microcol.gui.event.ChangeLanguageListenerFileChooser;
import org.microcol.gui.event.ChangeLanguageListenerPreferences;
import org.microcol.gui.event.DeclareIndependenceListener;
import org.microcol.gui.event.PlowFieldEventListener;
import org.microcol.gui.event.QuitGameListener;
import org.microcol.gui.event.ShowGridListenerPreferences;
import org.microcol.gui.event.model.ArtifitialPlayersManager;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.MissionEventListener;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.MainPanelPresenter;
import org.microcol.gui.screen.MainPanelView;
import org.microcol.gui.screen.campaign.CampaignPresenter;
import org.microcol.gui.screen.campaign.CampaignView;
import org.microcol.gui.screen.campaign.ScreenCampaignPresenter;
import org.microcol.gui.screen.colonizopedia.ColonizopediaDialog;
import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.gui.screen.colony.ColonyPanel;
import org.microcol.gui.screen.colony.PanelColonyDockBehaviour;
import org.microcol.gui.screen.colony.ScreenColonyPresenter;
import org.microcol.gui.screen.colony.buildingqueue.QueueController;
import org.microcol.gui.screen.colony.buildingqueue.QueueDialog;
import org.microcol.gui.screen.colony.buildingqueue.ShowQueueDialogListener;
import org.microcol.gui.screen.editor.EditorMouseListener;
import org.microcol.gui.screen.editor.ScreenEditorPresenter;
import org.microcol.gui.screen.europe.BuyUnitsDialog;
import org.microcol.gui.screen.europe.EuropeButtonsPanelController;
import org.microcol.gui.screen.europe.EuropeCallback;
import org.microcol.gui.screen.europe.PanelEuropeDockBehavior;
import org.microcol.gui.screen.europe.PanelEuropeGoods;
import org.microcol.gui.screen.europe.PanelHighSeas;
import org.microcol.gui.screen.europe.PanelPortPier;
import org.microcol.gui.screen.europe.RecruiteUnitsDialog;
import org.microcol.gui.screen.europe.ScreenEuropePresenter;
import org.microcol.gui.screen.game.ScreenGamePresenter;
import org.microcol.gui.screen.game.components.ButtonsGamePanelController;
import org.microcol.gui.screen.game.components.RightPanelPresenter;
import org.microcol.gui.screen.game.components.RightPanelView;
import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.gui.screen.game.gamepanel.ExcludePainting;
import org.microcol.gui.screen.game.gamepanel.GamePanelController;
import org.microcol.gui.screen.game.gamepanel.GamePanelMouseListener;
import org.microcol.gui.screen.game.gamepanel.GamePanelPresenter;
import org.microcol.gui.screen.game.gamepanel.ModeController;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileListener;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileManager;
import org.microcol.gui.screen.game.gamepanel.MoveModeSupport;
import org.microcol.gui.screen.game.gamepanel.ScrollToSelectedUnit;
import org.microcol.gui.screen.game.gamepanel.ScrollingManager;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.screen.game.gamepanel.TileWasSelectedListener;
import org.microcol.gui.screen.game.gamepanel.UnitAttackedEventListener;
import org.microcol.gui.screen.game.gamepanel.UnitMoveFinishedListener;
import org.microcol.gui.screen.game.gamepanel.UnitMovedListener;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.gui.screen.goals.GoalsButtonsPanelController;
import org.microcol.gui.screen.goals.ScreenGoalsPresenter;
import org.microcol.gui.screen.market.ScreenMarketPresenter;
import org.microcol.gui.screen.menu.ButtonsPanelPresenter;
import org.microcol.gui.screen.menu.ButtonsPanelView;
import org.microcol.gui.screen.menu.ExitGameListener;
import org.microcol.gui.screen.menu.GameFinishedListener;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.gui.screen.menu.ScreenMenuPresenter;
import org.microcol.gui.screen.setting.ScreenSettingPresenter;
import org.microcol.gui.screen.setting.SettingAnimationSpeedPresenter;
import org.microcol.gui.screen.setting.SettingButtonsPresenter;
import org.microcol.gui.screen.setting.SettingLanguagePresenter;
import org.microcol.gui.screen.setting.SettingShowGridPresenter;
import org.microcol.gui.screen.setting.SettingVolumePresenter;
import org.microcol.gui.screen.statistics.ScreenStatisticsPresenter;
import org.microcol.gui.screen.statistics.StatisticsButtonsPanelController;
import org.microcol.gui.screen.turnreport.ScreenTurnReportPresenter;
import org.microcol.gui.screen.turnreport.ShowTurnEvensOnTurnStartedEvent;
import org.microcol.gui.screen.turnreport.TurnReportButtonsPanelController;
import org.microcol.gui.util.FontService;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.PathPlanning;
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

        bind(ShowTurnEvensOnTurnStartedEvent.class).asEagerSingleton();

        /**
         * Initialize MVP classes
         */
        bind(MainPanelView.class).in(Singleton.class);
        bind(MainPanelPresenter.class).in(Singleton.class);

        bind(ScreenMenu.class).in(Singleton.class);

        bind(ButtonsPanelView.class).in(Singleton.class);
        bind(ButtonsPanelPresenter.class).asEagerSingleton();

        bind(CampaignView.class).in(Singleton.class);
        bind(CampaignPresenter.Display.class).to(CampaignView.class).in(Singleton.class);
        bind(CampaignPresenter.class).asEagerSingleton();

        bind(StatusBar.class).annotatedWith(Names.named("GamePanel")).to(StatusBar.class)
                .asEagerSingleton();
        bind(StatusBar.class).annotatedWith(Names.named("Europe")).to(StatusBar.class)
                .asEagerSingleton();
        bind(StatusBar.class).annotatedWith(Names.named("Colony")).to(StatusBar.class)
                .asEagerSingleton();

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
        bind(ColonyDialogCallback.class).to(ScreenColonyPresenter.class).in(Singleton.class);
        bind(PanelColonyDockBehaviour.class).in(Singleton.class);
        bind(QueueDialog.class).in(Singleton.class);
        bind(QueueController.class).in(Singleton.class);
        bind(ShowQueueDialogListener.class).asEagerSingleton();

        /**
         * Europe dialog
         */
        bind(EuropeCallback.class).to(ScreenEuropePresenter.class).in(Singleton.class);
        bind(ScreenEuropePresenter.class).asEagerSingleton();
        bind(EuropeButtonsPanelController.class).asEagerSingleton();
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
                .toInstance(new VisibleAreaService());
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
        bind(ColonizopediaDialog.class).in(Singleton.class);

        /**
         * Load events manually
         */
        bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
        bind(ChangeLanguageListenerFileChooser.class).asEagerSingleton();
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
    @SuppressWarnings("ucd")
    I18n makeI18n(final GamePreferences gamePreferences) {
        return I18n.builder().setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(gamePreferences.getLocale()).build();
    }

}
