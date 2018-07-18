package org.microcol.gui.europe;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodsAmount;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public final class PanelEuropeDockBehavior extends AbstractPanelDockBehavior {

    final Logger logger = LoggerFactory.getLogger(PanelEuropeDockBehavior.class);

    private final EuropeDialogCallback europeDialogCallback;
    private final GameModelController gameModelController;
    private final DialogNotEnoughGold dialogNotEnoughGold;
    private final ChooseGoodAmount chooseGoodAmount;

    @Inject
    PanelEuropeDockBehavior(final EuropeDialogCallback europeDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final DialogNotEnoughGold dialogNotEnoughGold,
            final ChooseGoodAmount chooseGoodAmount) {
        super(gameModelController, imageProvider);
        this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        this.chooseGoodAmount = Preconditions.checkNotNull(chooseGoodAmount);
    }

    @Override
    public List<Unit> getUnitsInPort() {
        return gameModelController.getModel().getEurope().getPort()
                .getShipsInPort(gameModelController.getCurrentPlayer());
    }

    @Override
    public void consumeGoods(final CargoSlot cargoSlot, final GoodsAmount goodsAmount,
            final Optional<TransferFrom> transferFrom, final boolean specialOperatonWasSelected) {
        GoodsAmount tmp = goodsAmount;
        logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());
        if (specialOperatonWasSelected) {
            chooseGoodAmount.init(cargoSlot.maxPossibleGoodsToMoveHere(
                    CargoSlot.MAX_CARGO_SLOT_CAPACITY, goodsAmount.getAmount()));
            tmp = new GoodsAmount(goodsAmount.getGoodType(), chooseGoodAmount.getActualValue());
            if (tmp.isZero()) {
                return;
            }
        }
        if (transferFrom.get() instanceof ClipboardReader.TransferFromEuropeShop) {
            try {
                cargoSlot.storeFromEuropePort(tmp);
            } catch (NotEnoughtGoldException e) {
                dialogNotEnoughGold.showAndWait();
            }
        } else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
            cargoSlot.storeFromCargoSlot(tmp,
                    ((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }
        europeDialogCallback.repaintAfterGoodMoving();
    }

    @Override
    public void consumeUnit(final Unit unit, final Optional<TransferFrom> transferFrom) {
        europeDialogCallback.repaintAfterGoodMoving();
    }

}