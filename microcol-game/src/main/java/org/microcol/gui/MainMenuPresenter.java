package org.microcol.gui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.GameEventController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.TurnStartedController;
import org.microcol.gui.event.ViewController;
import org.microcol.gui.event.VolumeChangeController;
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

		JMenuItem getMenuItemVolume();

		JCheckBoxMenuItem getMenuItemShowGrid();

		JMenuItem getMenuItemMove();

		JMenuItem getMenuItemCenterView();
	}

	private final MainMenuPresenter.Display display;

	private boolean isFocusedMoveableUnit = false;

	public boolean isTileFocused = false;

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display, final GameEventController gameEventController,
			final GamePreferences gamePreferences, final ChangeLanguageController languangeController, final Text text,
			final ViewUtil viewUtil, final VolumeChangeController volumeChangeController,
			final ShowGridController showGridController, final FocusedTileController focusedTileController,
			final MoveUnitController moveUnitController, final ViewController viewController,
			final TurnStartedController turnStartedController, final ExitGameController exitGameController) {
		this.display = Preconditions.checkNotNull(display);
		display.getMenuItemNewGame().addActionListener(actionEvent -> {

		});
		if (!gamePreferences.isOSX()) {
			display.getMenuItemQuitGame().addActionListener(actionEvent -> {
				exitGameController.fireEvent(new ExitGameEvent());
			});
			display.getMenuItemAbout().addActionListener(actionEvent -> {
				gameEventController.fireAboutGameEvent();
			});
		}
		display.getRbMenuItemlanguageCz().addActionListener(actionEvent -> {
			languangeController.fireLanguageWasChangedEvent(Text.Language.cz);
		});
		display.getRbMenuItemlanguageEn().addActionListener(actionEvent -> {
			languangeController.fireLanguageWasChangedEvent(Text.Language.en);
		});
		display.getMenuItemVolume().addActionListener(actionEvent -> {
			PreferencesVolume preferencesVolume = new PreferencesVolume(viewUtil, text, volumeChangeController,
					gamePreferences.getVolume());
			preferencesVolume.setVisible(true);
		});
		display.getMenuItemShowGrid().addActionListener(
				ectionEvent -> showGridController.fireShowGridEvent(display.getMenuItemShowGrid().isSelected()));
		display.getMenuItemMove().addActionListener(ectionEvent -> {
			moveUnitController.fireStartMoveEvent();
			display.getMenuItemMove().setEnabled(false);
		});
		display.getMenuItemCenterView().addActionListener(event -> viewController.fireCenterView());
		languangeController.addLanguageListener(event -> {
			display.updateLanguage();
		});
		focusedTileController.addFocusedTileListener(event -> onFocusedTileEvent(event));
		turnStartedController.addTurnStartedListener(event -> onTurnStartedEvent(event));
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
