package org.microcol.gui.util;

import java.util.Optional;

import org.microcol.gui.colony.PanelColonyDockBehaviour;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodsAmount;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Provide basic support for drag &amp; drop over ships in dock.
 */
public abstract class AbstractPanelDockBehavior implements PanelDockBehavior {

	final Logger logger = LoggerFactory.getLogger(PanelColonyDockBehaviour.class);

	private final GameModelController gameModelController;
	private final ImageProvider imageProvider;

	public AbstractPanelDockBehavior(final GameModelController gameModelController, final ImageProvider imageProvider) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
	}

	@Override
	public void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
		logger.debug("Object was dropped on ship cargo slot.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameModelController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
				.tryReadGood((goodAmount, transferFrom) -> {
					Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");

					consumeGoods(cargoSlot, goodAmount, transferFrom,
							event.getTransferMode().equals(TransferMode.LINK));

					event.acceptTransferModes(TransferMode.ANY);
					event.setDropCompleted(true);
					event.consume();
				}).tryReadUnit((unit, transferFrom) -> {
					cargoSlot.store(unit);

					consumeUnit(unit, transferFrom);

					event.acceptTransferModes(TransferMode.ANY);
					event.setDropCompleted(true);
					event.consume();
				});
	}

	/**
	 * When user drop goods to some slot this method store goods.
	 * 
	 * @param cargoSlot
	 *            required cargo slot where will be goods stored
	 * @param goodsAmount
	 *            required goods type and it's quantity
	 * @param transferFrom
	 *            required place from is goods transfered
	 * @param specialOperatonWasSelected
	 *            it's <code>true</code> when user want special drag &amp; drop
	 *            operation like buy goods
	 */
	public abstract void consumeGoods(final CargoSlot cargoSlot, final GoodsAmount goodsAmount,
			final Optional<TransferFrom> transferFrom, final boolean specialOperatonWasSelected);

	/**
	 * Method is called when unit is stored in new cargo store and UI have to
	 * repainted.
	 * 
	 * @param unit
	 *            transfered unit
	 * @param transferFrom
	 *            required place from is goods transfered
	 */
	public abstract void consumeUnit(final Unit unit, final Optional<TransferFrom> transferFrom);

	@Override
	public void onDragDetected(final CargoSlot cargoSlot, final MouseEvent event, final Node node) {
		if (cargoSlot != null) {
			if (cargoSlot.getUnit().isPresent()) {
				final Image cargoImage = imageProvider.getUnitImage(cargoSlot.getUnit().get().getType());
				ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK)).addImage(cargoImage)
						.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot).addUnit(cargoSlot.getUnit().get())
						.build();
			} else if (cargoSlot.getGoods().isPresent()) {
				final Image cargoImage = imageProvider.getGoodTypeImage(cargoSlot.getGoods().get().getGoodType());
				ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK)).addImage(cargoImage)
						.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
						.addGoodAmount(cargoSlot.getGoods().get()).build();
			}
		}
		event.consume();
	}

	@Override
	public boolean isCorrectObject(final CargoSlot cargoSlot, final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		return !ClipboardReader.make(gameModelController.getModel(), db)
				.filterUnit(unit -> !unit.getType().isShip() && cargoSlot.isEmpty())
				.filterGoods(goods -> canBeGoodsTransfered(cargoSlot, goods)).isEmpty();
	}
}
