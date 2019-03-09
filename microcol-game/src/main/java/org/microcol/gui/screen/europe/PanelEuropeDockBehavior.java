package org.microcol.gui.screen.europe;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
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

    private final EuropeCallback europeDialogCallback;
    private final GameModelController gameModelController;
    private final DialogNotEnoughGold dialogNotEnoughGold;
    private final ChooseGoodsDialog chooseGoods;

    @Inject
    PanelEuropeDockBehavior(final EuropeCallback europeDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final DialogNotEnoughGold dialogNotEnoughGold, final ChooseGoodsDialog chooseGoods) {
        super(gameModelController, imageProvider);
        this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
    }

    @Override
    public List<Unit> getUnitsInPort() {
        return gameModelController.getModel().getEurope().getPort()
                .getShipsInPort(gameModelController.getCurrentPlayer());
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected, final ClipboardEval eval) {
        logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());
        Goods goods = eval.getGoods().get();
        goods = chooseGoods(goods, specialOperatonWasSelected, targetCargoSlot);
        if (goods.isZero()) {
            return;
        }
        final From transferFrom = eval.getFrom().get();
        if (From.VALUE_FROM_EUROPE_SHOP == transferFrom) {
            try {
                targetCargoSlot.storeFromEuropePort(goods);
            } catch (NotEnoughtGoldException e) {
                dialogNotEnoughGold.showAndWait();
            }
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            targetCargoSlot.storeFromCargoSlot(goods, eval.getCargoSlot().get());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }
        europeDialogCallback.repaintAfterGoodMoving();
    }

    private Goods chooseGoods(final Goods goods, final boolean specialOperatonWasSelected,
            final CargoSlot targetCargoSlot) {
        return chooseGoods(chooseGoods, goods, specialOperatonWasSelected,
                targetCargoSlot.maxPossibleGoodsToMoveHere(goods));
    }

    @Override
    public void consumeUnit(final CargoSlot targetCargoSlot, final Unit unit,
            final From transferFrom) {
        targetCargoSlot.store(unit);
        europeDialogCallback.repaintAfterGoodMoving();
    }

}