package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFromEuropePier;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.TitledPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

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

	private final GameModelController gameModelController;

	private final EuropeDialogCallback europeDialog;

	private final ImageProvider imageProvider;

	private final LocalizationHelper localizationHelper;

	private final VBox panelUnits;

	private Background background;

	@Inject
	public PanelPortPier(final GameModelController gameModelController, final EuropeDialogCallback europeDialogCallback,
			final Text text, final ImageProvider imageProvider, final LocalizationHelper localizationHelper) {
		super(text.get("europe.pier"), new Label(text.get("europe.pier")));
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.europeDialog = Preconditions.checkNotNull(europeDialogCallback);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		panelUnits = new VBox();
		getContentPane().getChildren().add(panelUnits);
		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	void repaint() {
		panelUnits.getChildren().clear();
		gameModelController.getModel().getEurope().getPier().getUnits(gameModelController.getCurrentPlayer()).forEach(
				unit -> panelUnits.getChildren().add(new PanelPortPierUnit(unit, imageProvider, localizationHelper)));
	}

	private final void onDragEntered(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	private final void onDragExited(final DragEvent event) {
		if (isItCorrectObject(event.getDragboard())) {
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

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		ClipboardReader.make(gameModelController.getModel(), event.getDragboard()).readUnit((draggedUnit, transferFrom) -> {
			draggedUnit.placeToEuropePortPier();
			europeDialog.repaintAfterGoodMoving();
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		});
	}

	private boolean isItCorrectObject(final Dragboard db) {
		return ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.filterTransferFrom(oTransferFrom -> oTransferFrom.isPresent()
						&& !(oTransferFrom.get() instanceof TransferFromEuropePier))
				.getUnit().isPresent();
	}

}
