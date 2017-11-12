package org.microcol.gui;

import java.util.Optional;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GoldWasChangedController;
import org.microcol.gui.event.model.NextTurnController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.util.Localized;
import org.microcol.model.Calendar;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public class StatusBarPresenter implements Localized {

	public interface Display {

		Label getStatusBarDescription();

		Label getLabelEra();

		Label getLabelGold();
	}

	@Inject
	public StatusBarPresenter(final StatusBarPresenter.Display display,
			final StatusBarMessageController statusBarMessageController, final NextTurnController nextTurnController,
			final ChangeLanguageController changeLanguangeController,
			final GoldWasChangedController goldWasChangedController) {
		statusBarMessageController.addListener(event -> {
			display.getStatusBarDescription().setText(event.getStatusMessage());
		});
		nextTurnController.addListener(event -> {
			setYearText(display.getLabelEra(), event.getCalendar());
		});
		changeLanguangeController.addListener(event -> {
			setYearText(display.getLabelEra(), event.getModel().getCalendar());
			final Optional<Player> human = event.getModel().getPlayers().stream().filter(player -> player.isHuman())
					.findAny();
			if (human.isPresent()) {
				setGoldText(display.getLabelGold(), human.get().getGold());
			}
			display.getStatusBarDescription().setText("");
		});
		goldWasChangedController.addListener(event -> {
			setGoldText(display.getLabelGold(), event.getNewValue());
		});
		display.getLabelEra().setOnMouseEntered(event -> {
			statusBarMessageController.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.era.description")));
		});
		display.getLabelGold().setOnMouseEntered(event -> {
			statusBarMessageController
					.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.gold.description")));
		});
		display.getStatusBarDescription().setOnMouseEntered(event -> {
			statusBarMessageController
					.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.status.description")));
		});
	}

	private final void setYearText(final Label labelEra, final Calendar calendar) {
		Preconditions.checkNotNull(labelEra);
		Preconditions.checkNotNull(calendar);
		labelEra.setText(getText().get("statusBar.era") + " " + calendar.getCurrentYear() + " AD");
	}

	private final void setGoldText(final Label labelGold, final int gold) {
		Preconditions.checkNotNull(labelGold);
		labelGold.setText(getText().get("statusBar.gold") + " " + gold);
	}

}
