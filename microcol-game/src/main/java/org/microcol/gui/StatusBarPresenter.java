package org.microcol.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.model.Calendar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class StatusBarPresenter implements Localized {

	public interface Display {
		JLabel getStatusBarDescription();

		JLabel getLabelEra();
	}

	@Inject
	public StatusBarPresenter(final StatusBarPresenter.Display display,
			final StatusBarMessageController statusBarMessageController, final NextTurnController nextTurnController,
			final ChangeLanguageController languangeController) {
		statusBarMessageController.addStatusMessageListener(message -> {
			display.getStatusBarDescription().setText(message);
		});
		nextTurnController.addNextTurnListener(event -> {
			setYearText(display.getLabelEra(), event.getCalendar());
		});
		languangeController.addLanguageListener(event -> {
			setYearText(display.getLabelEra(), event.getGame().getCalendar());
			display.getStatusBarDescription().setText("");
		});
		display.getLabelEra().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				statusBarMessageController.fireStatusMessageWasChangedEvent(getText().get("statusBar.era.description"));
			}
		});
		display.getStatusBarDescription().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				statusBarMessageController
						.fireStatusMessageWasChangedEvent(getText().get("statusBar.status.description"));
			}
		});
	}

	private final void setYearText(JLabel labelEra, final Calendar calendar) {
		Preconditions.checkNotNull(labelEra);
		Preconditions.checkNotNull(calendar);
		labelEra.setText(getText().get("statusBar.year") + " " + calendar.getCurrentYear() + " AD");
	}

}
