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
public class MainPanelView  {

	private final VBox box;;

	@Inject
	public MainPanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
			final RightPanelView rightPanelView) {
		ScrollPane scrollPane = new ScrollPane(gamePanel.getCanvas());
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

		box = new VBox();
		HBox hBox = new HBox();
		hBox.getChildren().addAll(scrollPane, rightPanelView.getBox());
		box.getChildren().addAll(hBox, statusBar.getBox());
	}

	public VBox getBox() {
		return box;
	}

}
