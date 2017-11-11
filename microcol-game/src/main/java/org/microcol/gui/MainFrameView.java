package org.microcol.gui;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main frame.
 */
public class MainFrameView implements MainFramePresenter.Display {

	private final VBox box;

	private final MainPanelView mainPanelView;

	private final StartPanelView startPanelView;

	@Inject
	public MainFrameView(final MainPanelView mainPanelView, final StartPanelView startPanelView) {
		box = new VBox();
		this.mainPanelView = Preconditions.checkNotNull(mainPanelView);
		this.startPanelView = Preconditions.checkNotNull(startPanelView);
	}

	@Override
	public void showPanel(final String panelName) {
		box.getChildren().clear();
		if (MainFramePresenter.MAIN_GAME_PANEL.equals(panelName)) {
			box.getChildren().add(mainPanelView.getBox());
		} else if (MainFramePresenter.START_PANEL.equals(panelName)) {
			box.getChildren().add(startPanelView.getBox());
		} else {
			throw new IllegalArgumentException(String.format("Invalid panel name (%s)", panelName));
		}
	}

	@Override
	public VBox getBox() {
		return box;
	}

}
