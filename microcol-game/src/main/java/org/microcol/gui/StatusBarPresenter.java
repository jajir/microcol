package org.microcol.gui;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.NextTurnController;
import org.microcol.gui.util.Localized;
import org.microcol.model.Calendar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public class StatusBarPresenter implements Localized {

	public interface Display {

		Label getStatusBarDescription();

		Label getLabelEra();
	}

	@Inject
	public StatusBarPresenter(final StatusBarPresenter.Display display,
			final StatusBarMessageController statusBarMessageController, final NextTurnController nextTurnController,
			final ChangeLanguageController changeLanguangeController) {
		statusBarMessageController.addListener(event -> {
			display.getStatusBarDescription().setText(event.getStatusMessage());
		});
		nextTurnController.addListener(event -> {
			setYearText(display.getLabelEra(), event.getCalendar());
		});
		changeLanguangeController.addListener(event -> {
			setYearText(display.getLabelEra(), event.getModel().getCalendar());
			display.getStatusBarDescription().setText("");
		});
		display.getLabelEra().setOnMouseDragEntered(event -> {
			statusBarMessageController.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.era.description")));
		});
		display.getStatusBarDescription().setOnMouseDragEntered(event -> {
			statusBarMessageController
					.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.status.description")));
		});
	}

	private final void setYearText(final Label labelEra, final Calendar calendar) {
		Preconditions.checkNotNull(labelEra);
		Preconditions.checkNotNull(calendar);
		labelEra.setText(getText().get("statusBar.year") + " " + calendar.getCurrentYear() + " AD");
	}

}
