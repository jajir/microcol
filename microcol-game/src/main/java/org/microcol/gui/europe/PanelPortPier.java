package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public class PanelPortPier extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelPortPier.class);

	private final GameController gameController;

	private final EuropeDialog europeDialog;
	
	private final ImageProvider imageProvider;

	private final LocalizationHelper localizationHelper;

	private final VBox panelUnits;

	private Background background;

	public PanelPortPier(final GameController gameController,final EuropeDialog europeDialog, final String title, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper) {
		super(title, new Label(title));
		this.gameController = Preconditions.checkNotNull(gameController);
		this.europeDialog = Preconditions.checkNotNull(europeDialog);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		panelUnits = new VBox();
		getContentPane().getChildren().add(panelUnits);
		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	void setEurope(final Model model) {
		panelUnits.getChildren().clear();
		model.getEurope().getPier().getUnits(model.getCurrentPlayer())
				.forEach(unit -> panelUnits.getChildren().add(new PanelUnit(unit, imageProvider, localizationHelper)));
	}

	private final void onDragEntered(final DragEvent event) {
		System.out.println("Entered");
		if (isItCorrectObject(event.getDragboard())) {
			System.out.println("It's my object");
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		System.out.println("Exited");
		setBackground(background);
		background = null;
	}

	private final void onDragOver(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		if (db.hasString()) {
			logger.debug("Drop over unit id '" + db.getString() + "'.");
			final Unit draggedUnit = gameController.getModel().getUnitById(Integer.valueOf(db.getString()));
			draggedUnit.placeToEuropePortPier();
			europeDialog.repaintAfterGoodMoving();
			/*
			 * let the source know whether the string was successfully
			 * transferred and used
			 */
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		} else {
			return;
		}
	}

	private boolean isItCorrectObject(final Dragboard db) {
		if (db.hasString()) {
			final Unit unit = gameController.getModel().getUnitById(Integer.valueOf(db.getString()));
			return !UnitType.isShip(unit.getType());
		} else {
			return false;
		}
	}

}
