package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Europe;
import org.microcol.model.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Show list of all available goods.
 */
public class PanelEuropeGoods extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelEuropeGoods.class);

	private final HBox hBox;

	private final ImageProvider imageProvider;

	private final GameModelController gameModelController;

	private final EuropeDialogCallback europeDialogCallback;
	
	private Background background;

	@Inject
	public PanelEuropeGoods(final EuropeDialogCallback europeDialogCallback, final GameModelController gameModelController,
			final ImageProvider imageProvider) {
		super("zbozi");
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
		hBox = new HBox();
		getContentPane().getChildren().add(hBox);

		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void repaint() {
		final Europe europe = gameModelController.getModel().getEurope();
		hBox.getChildren().clear();
		GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
			hBox.getChildren()
					.add(new PanelGood(imageProvider.getGoodTypeImage(goodType), europe.getGoodTradeForType(goodType)));
		});
	}

	private final void onDragEntered(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	private final void onDragExited(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			setBackground(background);
			background = null;
		}
	}

	private final void onDragOver(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Object was dropped on panel goods.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).tryReadGood((goodAmount, transferFrom) -> {
			if (transferFrom.isPresent() && transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
				final ClipboardReader.TransferFromCargoSlot fromCargo = (ClipboardReader.TransferFromCargoSlot) transferFrom
						.get();
				fromCargo.getCargoSlot().sellAndEmpty(goodAmount);
				europeDialogCallback.repaint();
			}
			event.setDropCompleted(true);
			event.consume();
		});
	}

	private boolean isItGoodAmount(final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		return ClipboardReader.make(gameModelController.getModel(), db).filterTransferFrom(transferFrom -> {
			if (transferFrom.isPresent() && transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
				return true;
			}
			return false;
		}).getGoods().isPresent();
	}

}
