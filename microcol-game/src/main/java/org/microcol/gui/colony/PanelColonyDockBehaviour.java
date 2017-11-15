package org.microcol.gui.colony;

import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class PanelColonyDockBehaviour implements PanelDockBehavior {
	/**
	 * 
	 */
	private final ColonyDialog PanelColonyDockBehaviour;
	private final GameModelController gameController;
	private final ViewUtil viewUtil;
	private final Text text;
	private final ImageProvider imageProvider;

	@Inject
	PanelColonyDockBehaviour(final ColonyDialog colonyDialog, final GameModelController gameController,
			final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider) {
		PanelColonyDockBehaviour = Preconditions.checkNotNull(colonyDialog);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	@Override
	public List<Unit> getUnitsInPort() {
		return PanelColonyDockBehaviour.colony.getUnitsInPort();
	}

	@Override
	public void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
		PanelColonyDockBehaviour.logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.tryReadGood((goodAmount, transferFrom) -> {
					Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");
					GoodAmount tmp = goodAmount;
					PanelColonyDockBehaviour.logger
							.debug("wasShiftPressed " + PanelColonyDockBehaviour.getPropertyShiftWasPressed().get());
					if (PanelColonyDockBehaviour.getPropertyShiftWasPressed().get()) {
						ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text,
								goodAmount.getAmount());
						tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
					}
					if (transferFrom.get() instanceof ClipboardReader.TransferFromColonyWarehouse) {
						cargoSlot.storeFromColonyWarehouse(tmp, PanelColonyDockBehaviour.colony);
					} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
						cargoSlot.storeFromCargoSlot(tmp,
								((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
					} else {
						throw new IllegalArgumentException("Unsupported source transfer '" + transferFrom + "'");
					}
					PanelColonyDockBehaviour.repaint();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				}).tryReadUnit((unit, transferFrom) -> {
					cargoSlot.store(unit);
					PanelColonyDockBehaviour.repaint();
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
		PanelColonyDockBehaviour.logger.debug("Drag over unit id '" + db.getString() + "'.");
		if (cargoSlot != null && cargoSlot.isEmpty()) {
			return !ClipboardReader.make(gameController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
					.isEmpty();
		}
		return false;
	}
}