package org.microcol.gui;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class StatusBarView implements StatusBarPresenter.Display {

	private final HBox statusBar;

	private final Label statusBarDescription;

	private final Label labelEra;

	private final Label labelGold;

	@Inject
	public StatusBarView() {
		statusBarDescription = new Label();
		Pane pane = new Pane(statusBarDescription);
		pane.setId("description");
		HBox.setHgrow(pane, Priority.ALWAYS);

		labelEra = new Label();
		labelEra.setId("labelEra");

		labelGold = new Label();
		labelGold.setId("labelGold");

		statusBar = new HBox();
		statusBar.getChildren().addAll(pane, labelGold, labelEra);
		statusBar.setId("statusBar");
	}

	@Override
	public Label getStatusBarDescription() {
		return statusBarDescription;
	}

	@Override
	public Label getLabelEra() {
		return labelEra;
	}

	public HBox getStatusBar() {
		return statusBar;
	}

	@Override
	public Label getLabelGold() {
		return labelGold;
	}
}
