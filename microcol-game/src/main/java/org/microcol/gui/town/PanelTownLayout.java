package org.microcol.gui.town;

import org.microcol.gui.europe.TitledPanel;

import javafx.scene.control.Label;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelTownLayout extends TitledPanel {

	public PanelTownLayout() {
		super("Colony layout", new Label("Colony layout"));
	}

}
