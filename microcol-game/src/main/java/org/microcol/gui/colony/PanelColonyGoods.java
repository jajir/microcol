package org.microcol.gui.colony;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public class PanelColonyGoods extends TitledPanel {

	private final HBox hBox;

	private final GameModelController gameModelController;
	
	private final ColonyDialogCallback colonyDialog;
	
	private ColonyWarehouse colonyWarehouse;

	@Inject
	public PanelColonyGoods(final GameModelController gameModelController, final ImageProvider imageProvider,
			final ColonyDialogCallback colonyDialog) {
		super("zbozi");
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		hBox = new HBox();
		getContentPane().getChildren().add(hBox);
		GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
			hBox.getChildren().add(new PanelColonyGood(imageProvider.getGoodTypeImage(goodType), goodType));
		});

		final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this, this::isItGoodAmount);
		setOnDragEntered(backgroundHighlighter::onDragEntered);
		setOnDragExited(backgroundHighlighter::onDragExited);
		
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void setColony(final Colony colony) {
		colonyWarehouse = Preconditions.checkNotNull(colony).getColonyWarehouse();
		hBox.getChildren().forEach(node -> {
			final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
			panelColonyGood.setColony(colony);
		});
	}

	public void repaint() {
		hBox.getChildren().forEach(node -> {
			final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
			panelColonyGood.repaint();
		});
	};
	
	private void onDragOver(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.ANY);
			event.consume();
		}
	}

	private void onDragDropped(final DragEvent event) {
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).tryReadGood((goodAmount, transferFrom) -> {
			if (transferFrom.isPresent() && transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
				final ClipboardReader.TransferFromCargoSlot fromCargoSlot = (ClipboardReader.TransferFromCargoSlot) transferFrom
						.get();
				colonyWarehouse.moveToWarehouse(goodAmount.getGoodType(), goodAmount.getAmount(), fromCargoSlot.getCargoSlot());
				event.setDropCompleted(true);
				colonyDialog.repaint();
			}
			event.consume();
		});
	}

	private boolean isItGoodAmount(final Dragboard db) {
		return ClipboardReader.make(gameModelController.getModel(), db).getGoods().isPresent();
	}

}
