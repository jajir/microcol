package org.microcol.gui.colony;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodsAmount;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public final class PanelColonyDockBehaviour extends AbstractPanelDockBehavior {

    final Logger logger = LoggerFactory.getLogger(PanelColonyDockBehaviour.class);

    private final ColonyDialogCallback colonyDialogCallback;
    private final ChooseGoodAmount chooseGoodAmount;

    @Inject
    PanelColonyDockBehaviour(final ColonyDialogCallback colonyDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final ChooseGoodAmount chooseGoodAmount) {
        super(gameModelController, imageProvider);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        this.chooseGoodAmount = Preconditions.checkNotNull(chooseGoodAmount);
    }

    @Override
    public List<Unit> getUnitsInPort() {
        return colonyDialogCallback.getColony().getUnitsInPort();
    }

    @Override
    public void consumeGoods(final CargoSlot cargoSlot, final GoodsAmount goodsAmount,
            final Optional<TransferFrom> transferFrom, final boolean specialOperatonWasSelected) {

        GoodsAmount tmp = goodsAmount;
        logger.debug("wasShiftPressed " + colonyDialogCallback.getPropertyShiftWasPressed().get());
        if (specialOperatonWasSelected) {
            // synchronously get information about transfered amount
            chooseGoodAmount.init(cargoSlot.maxPossibleGoodsToMoveHere(
                    CargoSlot.MAX_CARGO_SLOT_CAPACITY, goodsAmount.getAmount()));
            tmp = new GoodsAmount(goodsAmount.getGoodType(), chooseGoodAmount.getActualValue());
            if (tmp.isZero()) {
                return;
            }
        }
        // TODO following code doesn't look readable
        if (transferFrom.get() instanceof ClipboardReader.TransferFromColonyWarehouse) {
            cargoSlot.storeFromColonyWarehouse(tmp, colonyDialogCallback.getColony());
        } else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
            cargoSlot.storeFromCargoSlot(tmp,
                    ((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }

        colonyDialogCallback.repaint();
    }

    @Override
    public void consumeUnit(final Unit unit, final Optional<TransferFrom> transferFrom) {
        colonyDialogCallback.repaint();
    }

}