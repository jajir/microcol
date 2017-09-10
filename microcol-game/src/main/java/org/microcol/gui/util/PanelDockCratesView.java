package org.microcol.gui.util;

import java.util.List;

import org.microcol.gui.ImageProvider;

import javafx.scene.layout.HBox;

public class PanelDockCratesView extends HBox {

	final static int MAX_NUMBER_OF_CRATES = 6;

	PanelDockCratesView(final ImageProvider imageProvider, final PanelDockBehavior panelDockBehavior) {
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			final PanelDockCrate paneCrate = new PanelDockCrate(imageProvider, panelDockBehavior);
			getChildren().add(paneCrate);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	List<PanelDockCrate> getCrates() {
		return (List)getChildren();
	}
}
