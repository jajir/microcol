package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public class PanelPortPier extends TitledPanel {

	private final ImageProvider imageProvider;

	private final LocalizationHelper localizationHelper;

	private final VBox panelUnits;

	public PanelPortPier(final String title, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper) {
		super(title, new Label(title));
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		panelUnits = new VBox();
		getContentPane().getChildren().add(panelUnits);
	}

	void setEurope(final Model model) {
		panelUnits.getChildren().clear();
		model.getEurope().getPier().getUnits(model.getCurrentPlayer())
				.forEach(unit -> panelUnits.getChildren().add(new PanelUnit(unit, imageProvider, localizationHelper)));
	}

}
