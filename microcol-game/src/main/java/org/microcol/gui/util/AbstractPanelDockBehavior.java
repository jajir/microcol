package org.microcol.gui.util;

import org.microcol.gui.colony.PanelColonyDockBehaviour;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.model.CargoSlot;
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

    public AbstractPanelDockBehavior(final GameModelController gameModelController,
            final ImageProvider imageProvider) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
    }

    @Override
    public void onDragDropped(final CargoSlot targetCargoSlot, final DragEvent event) {
        logger.debug("Object was dropped on ship cargo slot.");
        final Dragboard db = event.getDragboard();
        final ClipboardEval eval = ClipboardEval.make(gameModelController.getModel(), db)
                .filterUnit(unit -> !unit.getType().isShip());
        if (eval.getCargoSlot().isPresent() && eval.getGoodAmount().isPresent()) {
            consumeGoods(targetCargoSlot, event.getTransferMode().equals(TransferMode.LINK), eval);
        } else if (eval.getUnit().isPresent()) {
            consumeUnit(eval.getUnit().get(), eval.getFrom().get());
        }
        event.acceptTransferModes(TransferMode.ANY);
        event.setDropCompleted(true);
        event.consume();
    }

    /**
     * When user drop goods to some slot this method store goods.
     * 
     * @param targetCargoSlot
     *            required target cargo slot where will be goods stored
     * @param clipboardEval
     *            required clipboard evaluator
     * @param specialOperatonWasSelected
     *            it's <code>true</code> when user want special drag &amp; drop
     *            operation like buy goods
     */
    public abstract void consumeGoods(CargoSlot targetCargoSlot, boolean specialOperatonWasSelected,
            ClipboardEval clipboardEval);

    /**
     * Method is called when unit is stored in new cargo store and UI have to
     * repainted.
     * 
     * @param unit
     *            transfered unit
     * @param transferFrom
     *            required place from is goods transfered
     */
    public abstract void consumeUnit(Unit unit, From transferFrom);

    @Override
    public void onDragDetected(final CargoSlot cargoSlot, final MouseEvent event, final Node node) {
        if (cargoSlot != null) {
            if (cargoSlot.getUnit().isPresent()) {
                final Image cargoImage = imageProvider.getUnitImage(cargoSlot.getUnit().get());
                ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK))
                        .addImage(cargoImage)
                        .addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
                        .addUnit(cargoSlot.getUnit().get()).build();
            } else if (cargoSlot.getGoods().isPresent()) {
                final Image cargoImage = imageProvider
                        .getGoodTypeImage(cargoSlot.getGoods().get().getGoodType());
                ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK))
                        .addImage(cargoImage)
                        .addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
                        .addGoodAmount(cargoSlot.getGoods().get()).build();
            }
        }
        event.consume();
    }

    @Override
    public boolean isCorrectObject(final CargoSlot cargoSlot, final Dragboard db) {
        logger.debug("Drag over unit id '" + db.getString() + "'.");
        return !ClipboardEval.make(gameModelController.getModel(), db)
                .filterUnit(unit -> !unit.getType().isShip() && cargoSlot.isEmpty())
                .filterGoods(goods -> canBeGoodsTransfered(cargoSlot, goods)).isEmpty();
    }
}
