package org.microcol.gui;

import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main frame.
 */
public class MainFrameView implements MainFramePresenter.Display {

	private final VBox box;

	@Inject
	public MainFrameView(final MainPanelView mainPanelView, final StartPanelView startPanelView) {
		box = new VBox();
		box.getChildren().add(mainPanelView.getBox());
	}

	@Override
	public void showPanel(final String panelName) {
		// FIXME JJ startPanelView switching should be there
		// cardLayout.show(getContentPane(),
		// Preconditions.checkNotNull(panelName));
	}

	@Override
	public VBox getBox() {
		return box;
	}

}
