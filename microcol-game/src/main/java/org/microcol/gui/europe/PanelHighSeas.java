package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
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

	private final Logger logger = LoggerFactory.getLogger(PanelHighSeas.class);

	private final boolean isShownShipsTravelingToEurope;
	
	private final EuropeDialog europeDialog;

	private final HBox shipsContainer;

	private final ImageProvider imageProvider;

	private final Model model;

	private Background background;

	public PanelHighSeas(final EuropeDialog europeDialog, final ImageProvider imageProvider, final String title, final Model model,
			final boolean isShownShipsTravelingToEurope) {
		super(title, new Label(title));
		this.europeDialog = Preconditions.checkNotNull(europeDialog);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.model = Preconditions.checkNotNull(model);
		this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
		shipsContainer = new HBox();
		getChildren().add(shipsContainer);
		showShips();
		setOnDragEntered(this::onDragEntered);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
		setOnDragExited(this::onDragExited);
	}
	
	public void repaint(){
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
			logger.debug("drag over unit id '" + db.getString() + "'.");
			final Unit unit = model.getUnitById(Integer.valueOf(db.getString()));
			return UnitType.isShip(unit.getType());
		} else {
			return false;
		}
	}

	private final void onDragDropped(DragEvent event) {
		System.out.println("DragDropped");
		final Dragboard db = event.getDragboard();
		if (db.hasString()) {
			logger.debug("drag over unit id '" + db.getString() + "'.");
			final Unit unit = model.getUnitById(Integer.valueOf(db.getString()));
			Preconditions.checkState(UnitType.isShip(unit.getType()), "Only ships could be send to high seas");
			unit.placeToHighSeas(false);
			europeDialog.repaint();
			event.acceptTransferModes(TransferMode.MOVE);
			/*
			 * let the source know whether the string was successfully transferred
			 * and used
			 */
			event.setDropCompleted(true);
			event.consume();
		} else {
			return;
		}
	}

	private void showShips() {
		model.getHighSea().getUnitsTravelingTo(model.getCurrentPlayer(), isShownShipsTravelingToEurope)
				.forEach(unit -> {
					shipsContainer.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
				});
	}

}
