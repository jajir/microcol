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

	@Inject
	public StatusBarView() {
		statusBarDescription = new Label();
		Pane pane = new Pane(statusBarDescription);
		pane.setId("description");
		labelEra = new Label();
		labelEra.setId("labelEra");
		HBox.setHgrow(pane, Priority.ALWAYS);
		statusBar = new HBox();
		statusBar.getChildren().addAll(pane, labelEra);
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
}
