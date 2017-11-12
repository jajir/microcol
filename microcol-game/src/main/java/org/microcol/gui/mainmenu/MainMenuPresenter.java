package org.microcol.gui.mainmenu;

import org.microcol.gui.DialogIndependenceWasDeclared;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.PersistingDialog;
import org.microcol.gui.PreferencesAnimationSpeed;
import org.microcol.gui.PreferencesVolume;
import org.microcol.gui.colonizopedia.Colonizopedia;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
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

		MenuItem getMenuItemDeclareIndependence();

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

		MenuItem getMenuItemBuildColony();

		MenuItem getMenuItemCenterView();

		MenuItem getMenuItemEurope();

		MenuItem getMenuItemColonizopedia();
	}

	private final MainMenuPresenter.Display display;

	private boolean isFocusedMoveableUnit = false;

	public boolean isTileFocused = false;

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display,
			/**
			 * Following parameters are controllers reacting on menu events 
			 */
			final AboutGameEventController gameEventController,
			final AnimationSpeedChangeController animationSpeedChangeController,
			final BuildColonyEventController buildColonyEventController,
			final CenterViewController centerViewController,
			final ChangeLanguageController changeLanguageController,
			final DeclareIndependenceController declareIndependenceController,
			final ExitGameController exitGameController,
			final ShowGridController showGridController,
			final VolumeChangeController volumeChangeController,
			
			/**
			 * Menu items react on following events
			 */
			final FocusedTileController focusedTileController,
			final GameModelController gameModelController,
			final StartMoveController startMoveController,
			final TurnStartedController turnStartedController,
			
			/**
			 * Other events consumers and helpers
			 */
			final GamePreferences gamePreferences,
			final PersistingDialog persistingDialog,
			final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper,
			final Text text, final ViewUtil viewUtil
			) {
		this.display = Preconditions.checkNotNull(display);
		display.getMenuItemNewGame().setOnAction(actionEvent -> gameModelController.startNewDefaultGame());
		display.getMenuItemSameGame().setOnAction(event -> persistingDialog.saveModel());
		display.getMenuItemLoadGame().setOnAction(event -> persistingDialog.loadModel());
		display.getMenuItemQuitGame().setOnAction(actionEvent -> exitGameController.fireEvent(new ExitGameEvent()));
		display.getMenuItemAbout().setOnAction(actionEvent -> gameEventController.fireEvent(new AboutGameEvent()));
		display.getMenuItemColonizopedia().setOnAction(event -> new Colonizopedia(text, viewUtil));
		display.getRbMenuItemlanguageCz().setOnAction(actionEvent -> changeLanguageController
				.fireEvent(new ChangeLanguageEvent(Text.Language.cz, gameModelController.getModel())));
		display.getRbMenuItemlanguageEn().setOnAction(actionEvent -> changeLanguageController
				.fireEvent(new ChangeLanguageEvent(Text.Language.en, gameModelController.getModel())));
		display.getMenuItemVolume().setOnAction(actionEvent -> new PreferencesVolume(viewUtil, text,
				volumeChangeController, gamePreferences.getVolume()));
		display.getMenuItemAnimationSpeed().setOnAction(event -> new PreferencesAnimationSpeed(text, viewUtil,
				animationSpeedChangeController, gamePreferences.getAnimationSpeed()));
		display.getMenuItemEurope().setOnAction(
				event -> new EuropeDialog(viewUtil, text, imageProvider, gameModelController, localizationHelper).show());
		display.getMenuItemShowGrid().setOnAction(ectionEvent -> showGridController
				.fireEvent(new ShowGridEvent(display.getMenuItemShowGrid().isSelected())));
		display.getMenuItemBuildColony().setOnAction(event -> buildColonyEventController.fireEvent());
		display.getMenuItemMove().setOnAction(ectionEvent -> {
			startMoveController.fireEvent(new StartMoveEvent());
			display.getMenuItemMove().setDisable(true);
		});
		display.getMenuItemDeclareIndependence().setOnAction(event -> {
			new DialogIndependenceWasDeclared(viewUtil, text);
			declareIndependenceController.fireEvent(
					new DeclareIndependenceEvent(gameModelController.getModel(), gameModelController.getCurrentPlayer()));
		});
		display.getMenuItemCenterView().setOnAction(event -> centerViewController.fireEvent(new CenterViewEvent()));
		
		changeLanguageController.addListener(event -> {
			display.updateLanguage();
		});
		focusedTileController.addListener(event -> onFocusedTileEvent(event));
		turnStartedController.addListener(event -> onTurnStartedEvent(event));
		declareIndependenceController.addListener(event -> display.getMenuItemDeclareIndependence().setDisable(true));

		exitGameController.addListener(evnt -> {
			display.getMenuItemEurope().setDisable(true);
		});
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
			display.getMenuItemBuildColony().setDisable(!event.isPossibleToBuildColony());
		} else {
			display.getMenuItemBuildColony().setDisable(true);
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
			display.getMenuItemEurope().setDisable(false);
			display.getMenuItemDeclareIndependence().setDisable(event.getPlayer().isDeclaredIndependence());
		} else {
			display.getMenuItemCenterView().setDisable(true);
			display.getMenuItemMove().setDisable(true);
			display.getMenuItemEurope().setDisable(true);
		}
	}

}
