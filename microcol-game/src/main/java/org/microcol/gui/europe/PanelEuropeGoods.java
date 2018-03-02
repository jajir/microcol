package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public class PanelEuropeGoods extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelEuropeGoods.class);

	private final HBox hBox;

	private final GameModelController gameModelController;

	private final EuropeDialogCallback europeDialogCallback;

	@Inject
	public PanelEuropeGoods(final EuropeDialogCallback europeDialogCallback,
			final GameModelController gameModelController, final ImageProvider imageProvider,
			final DialogNotEnoughGold dialogNotEnoughGold) {
		super("zbozi");
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
		hBox = new HBox();
		GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
			hBox.getChildren().add(new PanelGood(goodType, imageProvider, gameModelController, dialogNotEnoughGold));
		});
		getContentPane().getChildren().add(hBox);
		final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this, this::isItGoodAmount);
		setOnDragEntered(backgroundHighlighter::onDragEntered);
		setOnDragExited(backgroundHighlighter::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void repaint() {
		hBox.getChildren().forEach(node -> {
			((PanelGood) node).replain();
		});
	}

	private void onDragOver(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.ANY);
			event.consume();
		}
	}

	private void onDragDropped(final DragEvent event) {
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
		return ClipboardReader.make(gameModelController.getModel(), db)
				.filterTransferFrom(transferFrom -> transferFrom.isPresent()
						&& transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot)
				.getGoods().isPresent();
	}

}
