package org.microcol.gui.europe;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 */
public class PanelHighSeas extends TitledPanel {

	private boolean isShownShipsTravelingToEurope;

	private final EuropeDialogCallback europeDialog;

	private final HBox shipsContainer;

	private final ImageProvider imageProvider;

	private final GameModelController gameModelController;

	private final BackgroundHighlighter backgroundHighlighter;

	@Inject
	public PanelHighSeas(final EuropeDialogCallback europeDialog, final ImageProvider imageProvider,
			final GameModelController gameModelController) {
		super();
		this.europeDialog = Preconditions.checkNotNull(europeDialog);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		minHeightProperty().set(80);
		shipsContainer = new HBox();
		getChildren().add(shipsContainer);
		backgroundHighlighter = new BackgroundHighlighter(this, this::isItCorrectObject);
		setOnDragEntered(backgroundHighlighter::onDragEntered);
		setOnDragExited(backgroundHighlighter::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void repaint() {
		shipsContainer.getChildren().clear();
		showShips();
	}

	private void onDragOver(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private boolean isItCorrectObject(final Dragboard db) {
		if (!isShownShipsTravelingToEurope && db.hasString()) {
			return ClipboardReader.make(gameModelController.getModel(), db)
					.filterUnit(unit -> unit.getType().isShip()).getUnit().isPresent();
		} else {
			return false;
		}
	}

	private void onDragDropped(DragEvent event) {
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).readUnit((unit, transferFrom) -> {
			Preconditions.checkState(unit.getType().isShip(), "Only ships could be send to high seas");
			unit.placeToHighSeas(false);
			europeDialog.repaint();
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		});
	}

	private void showShips() {
		gameModelController.getModel().getHighSea()
				.getUnitsTravelingTo(gameModelController.getCurrentPlayer(), isShownShipsTravelingToEurope)
				.forEach(unit -> {
					shipsContainer.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
				});
	}

	/**
	 * @param isShownShipsTravelingToEurope the isShownShipsTravelingToEurope to set
	 */
	public void setShownShipsTravelingToEurope(boolean isShownShipsTravelingToEurope) {
		this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
	}

}
