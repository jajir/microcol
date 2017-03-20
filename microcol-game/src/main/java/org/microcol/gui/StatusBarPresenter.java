package org.microcol.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
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
		display.getLabelEra().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				statusBarMessageController
						.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.era.description")));
			}
		});
		display.getStatusBarDescription().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				statusBarMessageController
						.fireEvent(new StatusBarMessageEvent(getText().get("statusBar.status.description")));
			}
		});
	}

	private final void setYearText(JLabel labelEra, final Calendar calendar) {
		Preconditions.checkNotNull(labelEra);
		Preconditions.checkNotNull(calendar);
		labelEra.setText(getText().get("statusBar.year") + " " + calendar.getCurrentYear() + " AD");
	}

}
