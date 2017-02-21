package org.microcol.gui;

import javax.swing.JLabel;

import org.microcol.gui.model.World;

import com.google.inject.Inject;

public class StatusBarPresenter implements Localized {

	public interface Display {
		JLabel getStatusBarDescription();

		JLabel getLabelEra();
	}

	@Inject
	public StatusBarPresenter(final StatusBarPresenter.Display display,
			final StatusBarMessageController statusBarMessageController, final NextTurnController nextTurnController,
			final LanguangeController languangeController) {
		statusBarMessageController.addStatusMessageListener(message -> {
			display.getStatusBarDescription().setText(message);
		});
		nextTurnController.addNextTurnListener(world -> {
			setYearText(display.getLabelEra(), world);
		});
		languangeController.addLanguageListener(event -> {
			setYearText(display.getLabelEra(), event.getWorld());
		});
	}

	private final void setYearText(JLabel labelEra, final World world) {
		labelEra.setText(getText().get("statusBar.year") + " " + world.getCurrentYear() + " AD");
	}

}
