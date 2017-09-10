package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Show units outside colony.
 */
public class PanelOutsideColony extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelOutsideColony.class);

	private final HBox panelUnits;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	private final ColonyDialogCallback colonyDialog;

	public PanelOutsideColony(final ImageProvider imageProvider, final GameController gameController,
			final ColonyDialog colonyDialog) {
		super("Outside Colony", null);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		panelUnits = new HBox();
		getContentPane().getChildren().add(panelUnits);
		setOnDragDropped(this::onDragDropped);
		setOnDragOver(this::onDragOver);
	}

	private Colony colony;

	public void setColony(final Colony colony) {
		this.colony = colony;
		panelUnits.getChildren().clear();
		colony.getUnitsOutSideColony().forEach(unit -> {
			final Image image = imageProvider.getUnitImage(unit.getType());
			final ImageView imageView = new ImageView(image);
			Pane paneImage = new Pane(imageView);
			paneImage.setOnDragDetected(mouseEvent -> {
				final Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE);
				ClipboardWritter.make(db).addImage(image).addTransferFromOutsideColony().addUnit(unit).build();
				mouseEvent.consume();
			});
			panelUnits.getChildren().add(paneImage);
		});
	}

	private final void onDragOver(final DragEvent event) {
		if (isItUnit(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private boolean isItUnit(final Dragboard db) {
		return ClipboardReader.make(gameController.getModel(), db).getUnit().isPresent();
	}

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Object was dropped on panel outside colony.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameController.getModel(), db).tryReadUnit((unit, transferFrom) -> {
			unit.placeToMap(colony.getLocation());
			colonyDialog.repaint();
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		});
	}

}
