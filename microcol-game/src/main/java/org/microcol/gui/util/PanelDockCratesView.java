package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.ImageProvider;

import javafx.scene.layout.HBox;

public class PanelDockCratesView extends HBox {

	final static int MAX_NUMBER_OF_CRATES = 6;

	// XXX could be used getChildrem?
	private final List<PanelDockCrate> crates = new ArrayList<>();

	PanelDockCratesView(final ImageProvider imageProvider, final PanelDockBehavior panelDockBehavior) {
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			PanelDockCrate paneCrate = new PanelDockCrate(imageProvider, panelDockBehavior);
			crates.add(paneCrate);
			getChildren().add(paneCrate);
		}
	}

	List<PanelDockCrate> getCrates() {
		return crates;
	}
}
