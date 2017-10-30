package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 */
public class PanelHighSeas extends TitledPanel {

	private final boolean isShownShipsTravelingToEurope;

	private final EuropeDialog europeDialog;

	private final HBox shipsContainer;

	private final ImageProvider imageProvider;

	private final GameModelController gameController;

	private Background background;

	public PanelHighSeas(final EuropeDialog europeDialog, final ImageProvider imageProvider, final String title,
			final GameModelController gameController, final boolean isShownShipsTravelingToEurope) {
		super(title, null);
		this.europeDialog = Preconditions.checkNotNull(europeDialog);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
		minHeightProperty().set(80);
		shipsContainer = new HBox();
		getChildren().add(shipsContainer);
		setOnDragEntered(this::onDragEntered);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
		setOnDragExited(this::onDragExited);
	}

	public void repaint() {
		shipsContainer.getChildren().clear();
		showShips();
	}

	private final void onDragEntered(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		if (background != null) {
			setBackground(background);
			background = null;
		}
	}

	private final void onDragOver(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private boolean isItCorrectObject(final Dragboard db) {
		if (!isShownShipsTravelingToEurope && db.hasString()) {
			return ClipboardReader.make(gameController.getModel(), db)
					.filterUnit(unit -> unit.getType().isShip()).getUnit().isPresent();
		} else {
			return false;
		}
	}

	private final void onDragDropped(DragEvent event) {
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameController.getModel(), db).readUnit((unit, transferFrom) -> {
			Preconditions.checkState(unit.getType().isShip(), "Only ships could be send to high seas");
			unit.placeToHighSeas(false);
			europeDialog.repaint();
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		});
	}

	private void showShips() {
		gameController.getModel().getHighSea()
				.getUnitsTravelingTo(gameController.getCurrentPlayer(), isShownShipsTravelingToEurope)
				.forEach(unit -> {
					shipsContainer.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
				});
	}

}
