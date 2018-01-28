package org.microcol.gui.colony;

import java.util.List;

import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class PanelColonyDockBehaviour implements PanelDockBehavior {

	//FIXME look at other implementation of PanelDockBehavior, reuse some functionality in abstract impl  

	final Logger logger = LoggerFactory.getLogger(PanelColonyDockBehaviour.class);

	/**
	 * 
	 */
	private final ColonyDialogCallback colonyDialogCallback;
	private final GameModelController gameModelController;
	private final ViewUtil viewUtil;
	private final Text text;
	private final ImageProvider imageProvider;

	@Inject
	PanelColonyDockBehaviour(final ColonyDialogCallback colonyDialogCallback,
			final GameModelController gameModelController, final ViewUtil viewUtil, final Text text,
			final ImageProvider imageProvider) {
		this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	@Override
	public List<Unit> getUnitsInPort() {
		return colonyDialogCallback.getColony().getUnitsInPort();
	}
	
	@Override
	public void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.tryReadGood((goodAmount, transferFrom) -> {
					Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");
					
					GoodAmount tmp = goodAmount;
					logger.debug("wasShiftPressed " + colonyDialogCallback.getPropertyShiftWasPressed().get());
					if (colonyDialogCallback.getPropertyShiftWasPressed().get()) {
						//synchronously get information about transfered amount
						ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text,
								goodAmount.getAmount());
						tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
					}
					//TODO following code doesn't look readable
					if (transferFrom.get() instanceof ClipboardReader.TransferFromColonyWarehouse) {
						cargoSlot.storeFromColonyWarehouse(tmp, colonyDialogCallback.getColony());
					} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
						cargoSlot.storeFromCargoSlot(tmp,
								((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
					} else {
						throw new IllegalArgumentException("Unsupported source transfer '" + transferFrom + "'");
					}
					
					colonyDialogCallback.repaint();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				}).tryReadUnit((unit, transferFrom) -> {
					cargoSlot.store(unit);
					colonyDialogCallback.repaint();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				});
	}

	@Override
	public void onDragDetected(final CargoSlot cargoSlot, final MouseEvent event, final Node node) {
		if (cargoSlot != null) {
			if (cargoSlot.getUnit().isPresent()) {
				final Image cargoImage = imageProvider.getUnitImage(cargoSlot.getUnit().get().getType());
				ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage)
						.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot).addUnit(cargoSlot.getUnit().get())
						.build();
			} else if (cargoSlot.getGoods().isPresent()) {
				final Image cargoImage = imageProvider.getGoodTypeImage(cargoSlot.getGoods().get().getGoodType());
				ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage)
						.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
						.addGoodAmount(cargoSlot.getGoods().get()).build();
			}
		}
		event.consume();
	}

	@Override
	public boolean isCorrectObject(final CargoSlot cargoSlot, final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		return !ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.filterGoods(goods -> canBetransfered(cargoSlot, goods)).isEmpty();
	}

	// TODO can by shared among places, this standard behavior
	private boolean canBetransfered(final CargoSlot cargoSlot, final GoodAmount goods) {
		if (cargoSlot.getGoods().isPresent()) {
			return cargoSlot.getGoods().get().getGoodType().equals(goods.getGoodType());
		} else {
			return true;
		}
	}
}