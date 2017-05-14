package org.microcol.gui;

import org.microcol.gui.panelview.GamePanelView;

import com.google.inject.Inject;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Panel hold whole game screen without status bar.
 */
public class MainPanelView {

	private final VBox box;;

	@Inject
	public MainPanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
			final RightPanelView rightPanelView) {
		final ScrollPane scrollPane = new ScrollPane(gamePanel.getCanvas());
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.viewportBoundsProperty().addListener((obj, oldValue, newValue) -> {
			gamePanel.getVisibleArea().setWidth((int) newValue.getWidth());
			gamePanel.getVisibleArea().setHeight((int) newValue.getHeight());
		});
		scrollPane.setHmin(0);
		//FIXME value is fixed just foe one map, value should be readed from loaded map.
		scrollPane.setHmax(1000*35);
		scrollPane.hvalueProperty().addListener((obj, oldValue, newValue) -> {
			gamePanel.getVisibleArea().setX(newValue.intValue());
		});
		scrollPane.setVmin(0);
		//FIXME value is fixed just foe one map, value should be readed from loaded map.
		scrollPane.setVmax(1000*35);
		scrollPane.vvalueProperty().addListener((obj, oldValue, newValue) -> {
			gamePanel.getVisibleArea().setY(newValue.intValue());
		});

		box = new VBox();
		box.setId("mainPanel");
		HBox hBox = new HBox();
		hBox.setId("mainBox");
		hBox.getChildren().addAll(scrollPane, rightPanelView.getBox());
		box.getChildren().addAll(hBox, statusBar.getBox());
	}

	public VBox getBox() {
		return box;
	}

}
