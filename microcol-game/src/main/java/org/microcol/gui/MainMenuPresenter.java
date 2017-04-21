package org.microcol.gui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

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
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.MoveUnitController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class MainMenuPresenter {

	public interface Display {

		JMenuItem getMenuItemNewGame();

		JMenuItem getMenuItemLoadGame();

		JMenuItem getMenuItemSameGame();

		JMenuItem getMenuItemQuitGame();

		JMenuItem getMenuItemAbout();

		JRadioButtonMenuItem getRbMenuItemlanguageEn();

		JRadioButtonMenuItem getRbMenuItemlanguageCz();

		void updateLanguage();

		JMenuItem getMenuItemAnimationSpeed();

		JMenuItem getMenuItemVolume();

		JCheckBoxMenuItem getMenuItemShowGrid();

		JMenuItem getMenuItemMove();

		JMenuItem getMenuItemCenterView();

		JMenuItem getMenuItemEurope();
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
			final MoveUnitController moveUnitController, final CenterViewController centerViewController,
			final TurnStartedController turnStartedController, final ExitGameController exitGameController,
			final GameController gameController, final PersistingDialog persistingDialog) {
		this.display = Preconditions.checkNotNull(display);
		display.getMenuItemNewGame().addActionListener(actionEvent -> {
			gameController.startNewGame();
		});
		display.getMenuItemSameGame().addActionListener(event -> persistingDialog.saveModel());
		display.getMenuItemLoadGame().addActionListener(event -> persistingDialog.loadModel());
		if (!gamePreferences.isOSX()) {
			display.getMenuItemQuitGame().addActionListener(actionEvent -> {
				exitGameController.fireEvent(new ExitGameEvent());
			});
			display.getMenuItemAbout().addActionListener(actionEvent -> {
				gameEventController.fireEvent(new AboutGameEvent());
			});
		}
		display.getRbMenuItemlanguageCz().addActionListener(actionEvent -> {
			changeLanguageController.fireEvent(new ChangeLanguageEvent(Text.Language.cz, gameController.getModel()));
		});
		display.getRbMenuItemlanguageEn().addActionListener(actionEvent -> {
			changeLanguageController.fireEvent(new ChangeLanguageEvent(Text.Language.en, gameController.getModel()));
		});
		display.getMenuItemVolume().addActionListener(actionEvent -> new PreferencesVolume(viewUtil, text,
				volumeChangeController, gamePreferences.getVolume()));
		display.getMenuItemAnimationSpeed().addActionListener(event -> new PreferencesAnimationSpeed(text, viewUtil,
				animationSpeedChangeController, gamePreferences.getAnimationSpeed()));
		display.getMenuItemEurope().addActionListener(event -> new EuropeDialog(viewUtil, text, gameController));
		display.getMenuItemShowGrid().addActionListener(ectionEvent -> showGridController
				.fireEvent(new ShowGridEvent(display.getMenuItemShowGrid().isSelected())));
		display.getMenuItemMove().addActionListener(ectionEvent -> {
			moveUnitController.fireStartMoveEvent();
			display.getMenuItemMove().setEnabled(false);
		});
		display.getMenuItemCenterView()
				.addActionListener(event -> centerViewController.fireEvent(new CenterViewEvent()));
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
		display.getMenuItemCenterView().setEnabled(true);
		isTileFocused = true;
		if (event.isTileContainsMovebleUnit()) {
			display.getMenuItemMove().setEnabled(true);
			isFocusedMoveableUnit = true;
		} else {
			display.getMenuItemMove().setEnabled(false);
			isFocusedMoveableUnit = false;
		}
	}

	private final void onTurnStartedEvent(final TurnStartedEvent event) {
		if (event.getPlayer().isHuman()) {
			if (isTileFocused) {
				display.getMenuItemCenterView().setEnabled(true);
			}
			if (isFocusedMoveableUnit) {
				display.getMenuItemMove().setEnabled(true);
			}
		} else {
			display.getMenuItemCenterView().setEnabled(false);
			display.getMenuItemMove().setEnabled(false);
		}
	}

}
