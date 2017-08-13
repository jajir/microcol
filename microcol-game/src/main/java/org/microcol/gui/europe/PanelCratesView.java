package org.microcol.gui.europe;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;

import javafx.scene.layout.HBox;

public class PanelCratesView extends HBox {

	final static int MAX_NUMBER_OF_CRATES = 6;

	// XXX could be used getChildrem?
	private final List<PanelCrate> crates = new ArrayList<>();

	PanelCratesView(final GameController gameController, final ImageProvider imageProvider,
			final EuropeDialog europeDialog) {
		for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
			PanelCrate paneCrate = new PanelCrate(gameController, imageProvider, europeDialog);
			crates.add(paneCrate);
			getChildren().add(paneCrate);
		}
	}

	List<PanelCrate> getCrates() {
		return crates;
	}
}
