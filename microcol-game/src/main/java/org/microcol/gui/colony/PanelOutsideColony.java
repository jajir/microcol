package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Show units outside colony.
 */
public class PanelOutsideColony extends TitledPanel {

	private final HBox units;
	
	private final ImageProvider imageProvider;
	
	public PanelOutsideColony(final ImageProvider imageProvider) {
		super("Outside Colony", null);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		units = new HBox();
		getContentPane().getChildren().add(units);
	}

	public void setColony(final Colony colony) {
		units.getChildren().clear();
		colony.getUnitsOutSideColony().forEach(unit -> {
			ImageView imageView = new ImageView(imageProvider.getUnitImage(unit.getType()));
			units.getChildren().add(imageView);
		});
	}

}
