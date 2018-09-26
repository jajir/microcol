package org.microcol.gui.europe;

import java.util.List;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodsAmount;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Define behavior of ship cargo slot.
 */
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
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected, final ClipboardEval eval) {
        final GoodsAmount goodsAmount = eval.getGoodAmount().get();
        final From transferFrom = eval.getFrom().get();

        GoodsAmount tmp = goodsAmount;
        logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());
        if (specialOperatonWasSelected) {
            chooseGoodAmount.init(targetCargoSlot.maxPossibleGoodsToMoveHere(
                    CargoSlot.MAX_CARGO_SLOT_CAPACITY, goodsAmount.getAmount()));
            tmp = new GoodsAmount(goodsAmount.getGoodType(), chooseGoodAmount.getActualValue());
            if (tmp.isZero()) {
                return;
            }
        }
        if (From.VALUE_FROM_EUROPE_SHOP == transferFrom) {
            try {
                targetCargoSlot.storeFromEuropePort(tmp);
            } catch (NotEnoughtGoldException e) {
                dialogNotEnoughGold.showAndWait();
            }
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            targetCargoSlot.storeFromCargoSlot(tmp, eval.getCargoSlot().get());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }
        europeDialogCallback.repaintAfterGoodMoving();
    }

    @Override
    public void consumeUnit(final CargoSlot targetCargoSlot, final Unit unit,
            final From transferFrom) {
        targetCargoSlot.store(unit);
        europeDialogCallback.repaintAfterGoodMoving();
    }

}