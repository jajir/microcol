package org.microcol.gui.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Show panel with simple border and title. Title is places in top border line.
 */
public class TitledPanel extends StackPane {

	private final StackPane contentPane;

	public TitledPanel(final String titleString) {
		this(titleString, null);
	}

	public TitledPanel(final String titleString, final Node content) {
		final Label title = new Label(" " + titleString + " ");
		title.getStyleClass().add("bordered-titled-title");
		StackPane.setAlignment(title, Pos.TOP_CENTER);

		contentPane = new StackPane();
		if (content != null) {
			content.getStyleClass().add("bordered-titled-content");
			contentPane.getChildren().add(content);
		}
		getStyleClass().add("bordered-titled-border");
		getChildren().addAll(title, contentPane);
	}

	public StackPane getContentPane() {
		return contentPane;
	}

}
