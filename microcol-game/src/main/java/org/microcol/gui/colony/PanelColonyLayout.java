package org.microcol.gui.colony;

import org.microcol.gui.europe.TitledPanel;

import javafx.scene.control.Label;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelColonyLayout extends TitledPanel {

	public PanelColonyLayout() {
		super("Colony layout", new Label("Colony layout"));
	}

}
