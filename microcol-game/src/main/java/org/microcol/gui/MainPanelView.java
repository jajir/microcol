package org.microcol.gui;

import org.microcol.gui.panelview.GamePanelView;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Panel hold whole game screen without status bar.
 */
public class MainPanelView {

	private final VBox box;;

	@Inject
	public MainPanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
			final RightPanelView rightPanelView) {
		box = new VBox();
		box.setId("mainPanel");
		HBox hBox = new HBox();
		hBox.setId("mainBox");

		Pane canvasPane = new Pane();
		canvasPane.setId("canvas");
		canvasPane.getChildren().add(gamePanel.getCanvas());
		gamePanel.getCanvas().widthProperty().bind(canvasPane.widthProperty());
		gamePanel.getCanvas().heightProperty().bind(canvasPane.heightProperty());

		canvasPane.widthProperty().addListener((obj, oldValue, newValue) -> {
			if (newValue.intValue() < 1000)
				gamePanel.getVisibleArea().setCanvasWidth(newValue.intValue());
		});
		canvasPane.heightProperty().addListener((obj, oldValue, newValue) -> {
			if (newValue.intValue() < 1000)
				gamePanel.getVisibleArea().setCanvasHeight(newValue.intValue());
		});

		Pane rightPane = new Pane();
		rightPane.setId("rightPanel");
		Label l2 = new Label("blue right panel");
		rightPane.getChildren().add(l2);

		hBox.getChildren().addAll(canvasPane, rightPanelView.getBox());

		box.getChildren().addAll(hBox, statusBar.getBox());
	}

	public VBox getBox() {
		return box;
	}

}
