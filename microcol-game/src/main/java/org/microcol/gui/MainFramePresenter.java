package org.microcol.gui;

import org.microcol.gui.event.KeyController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main frame.
 */
public class MainFramePresenter {

	static final String START_PANEL = "Start panel";

	static final String MAIN_GAME_PANEL = "Main game panel";

	private final MainFramePresenter.Display display;
	
	public interface Display {

		VBox getBox();

		void showPanel(String panelName);
	}


	@Inject
	public MainFramePresenter(final MainFramePresenter.Display display, final KeyController keyController) {
		this.display = Preconditions.checkNotNull(display);
		display.getBox().setOnKeyPressed(e -> {
			keyController.fireEvent(e);
		});
	}

	void showPanel(final String panelName) {
		display.showPanel(panelName);
	}

}
