package org.microcol.gui;

import org.microcol.gui.colonizopedia.Colonizopedia;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.event.AboutGameEvent;
import org.microcol.gui.event.AboutGameEventController;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.CenterViewController;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.ShowGridEvent;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;

public class MainMenuPresenter {

	public interface Display {

		MenuItem getMenuItemNewGame();

		MenuItem getMenuItemLoadGame();

		MenuItem getMenuItemSameGame();

		MenuItem getMenuItemQuitGame();

		MenuItem getMenuItemAbout();

		RadioMenuItem getRbMenuItemlanguageEn();

		RadioMenuItem getRbMenuItemlanguageCz();

		void updateLanguage();

		MenuItem getMenuItemAnimationSpeed();

		MenuItem getMenuItemVolume();

		CheckMenuItem getMenuItemShowGrid();

		MenuItem getMenuItemMove();

		MenuItem getMenuItemCenterView();

		MenuItem getMenuItemEurope();

		MenuItem getMenuItemColonizopedia();
	}

	private final MainMenuPresenter.Display display;

	private boolean isFocusedMoveableUnit = false;

	public boolean isTileFocused = false;

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display,
			final AboutGameEventController gameEventController, final GamePreferences gamePreferences,
			final ChangeLanguageController changeLanguageController, final Text text, final ViewUtil viewUtil,
			final VolumeChangeController volumeChangeController,
			final AnimationSpeedChangeController animationSpeedChangeController,
			final ShowGridController showGridController, final FocusedTileController focusedTileController,
			final CenterViewController centerViewController, final TurnStartedController turnStartedController,
			final ExitGameController exitGameController, final GameModelController gameController,
			final PersistingDialog persistingDialog, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper, final StartMoveController startMoveController) {
		this.display = Preconditions.checkNotNull(display);
		display.getMenuItemNewGame().setOnAction(actionEvent -> {
			gameController.startNewDefaultGame();
		});
		display.getMenuItemSameGame().setOnAction(event -> persistingDialog.saveModel());
		display.getMenuItemLoadGame().setOnAction(event -> persistingDialog.loadModel());
		display.getMenuItemQuitGame().setOnAction(actionEvent -> {
			exitGameController.fireEvent(new ExitGameEvent());
		});
		display.getMenuItemAbout().setOnAction(actionEvent -> {
			gameEventController.fireEvent(new AboutGameEvent());
		});
		display.getMenuItemColonizopedia()
				.setOnAction(event -> new Colonizopedia(text, viewUtil, imageProvider, localizationHelper));
		display.getRbMenuItemlanguageCz().setOnAction(actionEvent -> {
			changeLanguageController.fireEvent(new ChangeLanguageEvent(Text.Language.cz, gameController.getModel()));
		});
		display.getRbMenuItemlanguageEn().setOnAction(actionEvent -> {
			changeLanguageController.fireEvent(new ChangeLanguageEvent(Text.Language.en, gameController.getModel()));
		});
		display.getMenuItemVolume().setOnAction(actionEvent -> new PreferencesVolume(viewUtil, text,
				volumeChangeController, gamePreferences.getVolume()));
		display.getMenuItemAnimationSpeed().setOnAction(event -> new PreferencesAnimationSpeed(text, viewUtil,
				animationSpeedChangeController, gamePreferences.getAnimationSpeed()));
		display.getMenuItemEurope().setOnAction(
				event -> new EuropeDialog(viewUtil, text, imageProvider, gameController, localizationHelper).show());
		display.getMenuItemShowGrid().setOnAction(ectionEvent -> showGridController
				.fireEvent(new ShowGridEvent(display.getMenuItemShowGrid().isSelected())));
		display.getMenuItemMove().setOnAction(ectionEvent -> {
			startMoveController.fireEvent(new StartMoveEvent());
			display.getMenuItemMove().setDisable(true);
		});
		display.getMenuItemCenterView().setOnAction(event -> centerViewController.fireEvent(new CenterViewEvent()));
		changeLanguageController.addListener(event -> {
			display.updateLanguage();
		});
		focusedTileController.addListener(event -> onFocusedTileEvent(event));
		turnStartedController.addListener(event -> onTurnStartedEvent(event));
	}

	/**
	 * Method process event when user put focus to some tile.
	 * 
	 * @param event
	 *            required event
	 */
	private final void onFocusedTileEvent(final FocusedTileEvent event) {
		display.getMenuItemCenterView().setDisable(false);
		isTileFocused = true;
		if (event.isTileContainsMovebleUnit()) {
			display.getMenuItemMove().setDisable(false);
			isFocusedMoveableUnit = true;
		} else {
			display.getMenuItemMove().setDisable(true);
			isFocusedMoveableUnit = false;
		}
	}

	private final void onTurnStartedEvent(final TurnStartedEvent event) {
		if (event.getPlayer().isHuman()) {
			if (isTileFocused) {
				display.getMenuItemCenterView().setDisable(false);
			}
			if (isFocusedMoveableUnit) {
				display.getMenuItemMove().setDisable(false);
			}
		} else {
			display.getMenuItemCenterView().setDisable(true);
			display.getMenuItemMove().setDisable(true);
		}
	}

}
