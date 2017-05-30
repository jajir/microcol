package org.microcol.gui.europe;

import javafx.scene.control.Label;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public class PanelRecruits extends TitledPanel {

	public PanelRecruits(final String title) {
		super(title, new Label(title));
	}

}
