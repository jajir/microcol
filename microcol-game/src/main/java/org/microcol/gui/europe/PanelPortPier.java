package org.microcol.gui.europe;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.microcol.gui.ImageProvider;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 */
public class PanelPortPier extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelPortPier.class);

	public PanelPortPier(final ImageProvider imageProvider) {
		super("pristav");

		List<Unit> ships = new ArrayList<>();

		Unit ship1 = EasyMock.createMock(Unit.class);
		Unit ship2 = EasyMock.createMock(Unit.class);
		EasyMock.expect(ship1.getType()).andReturn(UnitType.FRIGATE);
		EasyMock.expect(ship2.getType()).andReturn(UnitType.GALLEON);
		EasyMock.replay(ship1, ship2);
		ships.add(ship1);
		ships.add(ship2);

		HBox panelShips = new HBox();
		for (Unit unit : ships) {
			final ImageView imageIcon = new ImageView(imageProvider.getUnitImage(unit.getType()));
			panelShips.getChildren().add(imageIcon);
		}

		HBox panelCrates = new HBox();
		for (int i = 0; i < 6; i++) {
			final ImageView imageIcon = new ImageView(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
			imageIcon.setFitWidth(70);
			imageIcon.setFitHeight(70);
			imageIcon.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				logger.debug(db.getImage().toString());
				event.setDropCompleted(true);
				event.consume();
			});
			imageIcon.setOnDragOver(event -> {
				event.acceptTransferModes(TransferMode.MOVE);
				event.consume();
			});
			panelCrates.getChildren().add(imageIcon);
		}

		VBox mainPanel = new VBox(panelShips, panelCrates);
		getContentPane().getChildren().add(mainPanel);

	}

}
