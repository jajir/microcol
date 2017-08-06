package org.microcol.gui.europe;

import org.easymock.EasyMock;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public class PanelRecruits extends TitledPanel {
	//TODO J rename it to port
	public PanelRecruits(final String title, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper) {
		super(title, new Label(title));
		VBox hBox = new VBox();

		// FIXME JJ get ships from API
		Unit ship21 = EasyMock.createMock(Unit.class);
		EasyMock.expect(ship21.getType()).andReturn(UnitType.COLONIST).anyTimes();
		EasyMock.replay(ship21);

		hBox.getChildren().addAll(

				new PanelUnit(ship21, imageProvider, localizationHelper)

		);

		getContentPane().getChildren().add(hBox);
	}

}
