package org.microcol.gui.europe;

import java.util.List;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.NotEnoughtGoldException;
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

public class PanelEuropeDockBehavior implements PanelDockBehavior {

	final Logger logger = LoggerFactory.getLogger(PanelEuropeDockBehavior.class);

	/**
	 * 
	 */
	private final EuropeDialogCallback europeDialogCallback;
	private final GameModelController gameModelController;
	private final Text text;
	private final ViewUtil viewUtil;
	private final ImageProvider imageProvider;

	@Inject
	PanelEuropeDockBehavior(EuropeDialogCallback europeDialogCallback, GameModelController gameModelController, Text text,
			ViewUtil viewUtil, ImageProvider imageProvider) {
		this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.text = Preconditions.checkNotNull(text);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	@Override
	public List<Unit> getUnitsInPort() {
		return gameModelController.getModel().getEurope().getPort().getShipsInPort(gameModelController.getCurrentPlayer());
	}

	@Override
	public final void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.tryReadGood((goodAmount, transferFrom) -> {
					Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");
					GoodAmount tmp = goodAmount;
					logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());
					if (europeDialogCallback.getPropertyShiftWasPressed().get()) {
						ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text,
								goodAmount.getAmount());
						tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
					}
					if (transferFrom.get() instanceof ClipboardReader.TransferFromEuropeShop) {
						try {
							cargoSlot.buyAndStore(tmp);
						} catch (NotEnoughtGoldException e) {
							new DialogNotEnoughGold(viewUtil, text);
						}
					} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
						cargoSlot.storeFromCargoSlot(tmp,
								((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
					} else {
						throw new IllegalArgumentException("Unsupported source transfer '" + transferFrom + "'");
					}
					europeDialogCallback.repaintAfterGoodMoving();
					event.acceptTransferModes(TransferMode.MOVE);
					event.setDropCompleted(true);
					event.consume();
				}).tryReadUnit((unit, transferFrom) -> {
					cargoSlot.store(unit);
					europeDialogCallback.repaintAfterGoodMoving();
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
		if (cargoSlot != null && cargoSlot.isEmpty()) {
			return !ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
					.isEmpty();
		}
		return false;
	}
}