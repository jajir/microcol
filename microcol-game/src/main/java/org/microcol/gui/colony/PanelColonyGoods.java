package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.GoodType;

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
public class PanelColonyGoods extends TitledPanel {

	private final HBox hBox;

	private final GameModelController gameModelController;
	
	private final ColonyDialogCallback colonyDialog;

	private Background background;
	
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

		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void setColony(final Colony colony) {
		colonyWarehouse = Preconditions.checkNotNull(colony).getColonyWarehouse();
		hBox.getChildren().forEach(node -> {
			final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
			panelColonyGood.setColony(colony.getColonyWarehouse());
		});
	}

	public void repaint() {
		hBox.getChildren().forEach(node -> {
			final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
			panelColonyGood.repaint();
		});
	};
	
	private final void onDragEntered(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		setBackground(background);
		background = null;
	}

	private final void onDragOver(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(final DragEvent event) {
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
