package org.microcol.gui;

import org.microcol.gui.event.AboutGameEventController;
import org.microcol.gui.event.AboutGameListenerImpl;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.AnimationSpeedChangedListenerPreferences;
import org.microcol.gui.event.CenterViewController;
import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.ChangeLanguageListenerPreferences;
import org.microcol.gui.event.ChangeLanguageListenerText;
import org.microcol.gui.event.EventInitializer;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.ShowGridListenerPreferences;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.VolumeChangedListenerPreferences;
import org.microcol.gui.event.model.DebugRequestController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameFinishedController;
import org.microcol.gui.event.model.ModelEventManager;
import org.microcol.gui.event.model.MoveUnitController;
import org.microcol.gui.event.model.NewGameController;
import org.microcol.gui.event.model.NextTurnController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.event.model.UnitAttackedEventController;
import org.microcol.gui.panelview.GamePanelPresenter;
import org.microcol.gui.panelview.GamePanelView;
import org.microcol.gui.panelview.MouseOverTileChangedController;
import org.microcol.gui.panelview.MouseOverTileListener;
import org.microcol.gui.panelview.MoveModeSupport;
import org.microcol.gui.panelview.PaintService;
import org.microcol.gui.panelview.UnitAttackedEventListener;
import org.microcol.gui.panelview.UnitService;
import org.microcol.gui.panelview.ViewState;
import org.microcol.gui.town.PanelTownLayout;
import org.microcol.gui.town.TownDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class MicroColModule extends AbstractModule {

	@Override
	protected void configure() {

		bind(MainStageBuilder.class).in(Singleton.class);
		bind(StatusBarMessageController.class).in(Singleton.class);

		bind(GamePreferences.class).in(Singleton.class);
		bind(PathPlanning.class).in(Singleton.class);
		bind(ImageProvider.class).in(Singleton.class);
		bind(ViewUtil.class).in(Singleton.class);
		bind(LocalizationHelper.class).in(Singleton.class);
		bind(UnitService.class).in(Singleton.class);

		/**
		 * Event controllers.
		 */
		bind(ApplicationController.class).in(Singleton.class);
		bind(MoveUnitController.class).in(Singleton.class);
		bind(KeyController.class).in(Singleton.class);
		bind(NextTurnController.class).in(Singleton.class);
		bind(FocusedTileController.class).in(Singleton.class);
		bind(AboutGameEventController.class).in(Singleton.class);
		bind(ChangeLanguageController.class).in(Singleton.class);
		bind(StartMoveController.class).in(Singleton.class);
		bind(NewGameController.class).in(Singleton.class);
		bind(GameController.class).in(Singleton.class);
		bind(TurnStartedController.class).in(Singleton.class);
		bind(VolumeChangeController.class).in(Singleton.class);
		bind(AnimationSpeedChangeController.class).in(Singleton.class);
		bind(ShowGridController.class).in(Singleton.class);
		bind(CenterViewController.class).in(Singleton.class);
		bind(ExitGameController.class).in(Singleton.class);
		bind(DebugRequestController.class).in(Singleton.class);
		bind(GameFinishedController.class).in(Singleton.class);
		bind(MouseOverTileChangedController.class).in(Singleton.class);
		bind(UnitAttackedEventController.class).in(Singleton.class);

		bind(ModelEventManager.class).in(Singleton.class);

		/**
		 * Initialize MVP classes
		 */
		bind(MainFrameView.class).in(Singleton.class);
		bind(MainFramePresenter.Display.class).to(MainFrameView.class).in(Singleton.class);
		bind(MainFramePresenter.class).in(Singleton.class);

		bind(GamePanelView.class).in(Singleton.class);
		bind(GamePanelPresenter.Display.class).to(GamePanelView.class).in(Singleton.class);
		bind(GamePanelPresenter.class).asEagerSingleton();
		bind(ViewState.class).in(Singleton.class);
		bind(MoveModeSupport.class).in(Singleton.class);
		bind(MouseOverTileListener.class).asEagerSingleton();
		bind(PaintService.class).in(Singleton.class);
		bind(UnitAttackedEventListener.class).asEagerSingleton();

		bind(StartPanelView.class).in(Singleton.class);
		bind(StartPanelPresenter.Display.class).to(StartPanelView.class).in(Singleton.class);
		bind(StartPanelPresenter.class).asEagerSingleton();

		bind(StatusBarView.class).in(Singleton.class);
		bind(StatusBarPresenter.Display.class).to(StatusBarView.class).in(Singleton.class);
		bind(StatusBarPresenter.class).asEagerSingleton();

		bind(MainMenuView.class).in(Singleton.class);
		bind(MainMenuPresenter.Display.class).to(MainMenuView.class).in(Singleton.class);
		bind(MainMenuPresenter.class).asEagerSingleton();
		bind(MainMenuDevelopment.class).in(Singleton.class);

		bind(RightPanelView.class).in(Singleton.class);
		bind(RightPanelPresenter.Display.class).to(RightPanelView.class).in(Singleton.class);
		bind(RightPanelPresenter.class).asEagerSingleton();

		bind(UnitsPanel.class).asEagerSingleton();
		bind(TownDialog.class).asEagerSingleton();
		bind(PanelTownLayout.class).asEagerSingleton();
		bind(PersistingDialog.class).in(Singleton.class);

		/**
		 * Load events manually
		 */
		bind(AboutGameListenerImpl.class).asEagerSingleton();
		bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
		bind(ChangeLanguageListenerText.class).asEagerSingleton();
		bind(VolumeChangedListenerPreferences.class).asEagerSingleton();
		bind(AnimationSpeedChangedListenerPreferences.class).asEagerSingleton();
		bind(ShowGridListenerPreferences.class).asEagerSingleton();

		bind(MusicPlayer.class).in(Singleton.class);
		bind(MusicController.class).in(Singleton.class);

		bind(EventInitializer.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	Text makeText(final GamePreferences gamePreferences) {
		return new Text(gamePreferences.getLocale());
	}

}
