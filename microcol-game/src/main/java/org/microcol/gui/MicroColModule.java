package org.microcol.gui;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.ChangeLanguageListenerPreferences;
import org.microcol.gui.event.ChangeLanguageListenerText;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.GameEventController;
import org.microcol.gui.event.GameEventListenerImpl;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageControllerImpl;
import org.microcol.gui.model.GameController;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class MicroColModule extends AbstractModule {

	@Override
	protected void configure() {

		bind(StatusBarMessageController.class).to(StatusBarMessageControllerImpl.class).in(Singleton.class);

		bind(MoveAutomatization.class).in(Singleton.class);
		bind(PathPlanning.class).in(Singleton.class);
		bind(ImageProvider.class).in(Singleton.class);
		bind(ViewUtil.class).in(Singleton.class);

		/**
		 * Event controllers.
		 */
		bind(MoveUnitController.class).in(Singleton.class);
		bind(KeyController.class).in(Singleton.class);
		bind(NextTurnController.class).in(Singleton.class);
		bind(FocusedTileController.class).in(Singleton.class);
		bind(GameEventController.class).in(Singleton.class);
		bind(ChangeLanguageController.class).in(Singleton.class);
		bind(GameController.class).in(Singleton.class);

		/**
		 * Initialize MVP classes
		 */
		bind(MainFrameView.class).in(Singleton.class);
		bind(MainFramePresenter.Display.class).to(MainFrameView.class).in(Singleton.class);
		bind(MainFramePresenter.class).in(Singleton.class);

		bind(GamePanelView.class).in(Singleton.class);
		bind(GamePanelPresenter.Display.class).to(GamePanelView.class).in(Singleton.class);
		bind(GamePanelPresenter.class).asEagerSingleton();

		bind(StatusBarView.class).in(Singleton.class);
		bind(StatusBarPresenter.Display.class).to(StatusBarView.class).in(Singleton.class);
		bind(StatusBarPresenter.class).asEagerSingleton();

		bind(MainMenuView.class).in(Singleton.class);
		bind(MainMenuPresenter.Display.class).to(MainMenuView.class).in(Singleton.class);
		bind(MainMenuPresenter.class).asEagerSingleton();

		bind(RightPanelView.class).in(Singleton.class);
		bind(RightPanelPresenter.Display.class).to(RightPanelView.class).in(Singleton.class);
		bind(RightPanelPresenter.class).asEagerSingleton();

		bind(UnitsPanel.class).asEagerSingleton();

		/**
		 * Load events manually
		 */
		bind(GameEventListenerImpl.class).asEagerSingleton();
		bind(ChangeLanguageListenerPreferences.class).asEagerSingleton();
		bind(ChangeLanguageListenerText.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	Text makeText(final GamePreferences gamePreferences) {
		return new Text(gamePreferences.getLanguage());
	}

}
