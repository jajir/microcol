package org.microcol.gui.screen.europe;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.dock.AbstractPanelDockBehavior;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.market.ScreenMarketBuyContext;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Define behavior of ship cargo slot.
 */
public final class PanelEuropeDockBehavior extends AbstractPanelDockBehavior {

    final Logger logger = LoggerFactory.getLogger(PanelEuropeDockBehavior.class);

    private final EuropeCallback europeDialogCallback;
    private final GameModelController gameModelController;
    private final DialogNotEnoughGold dialogNotEnoughGold;
    private final ChooseGoodsDialog chooseGoodsDialog;
    private final EventBus eventBus;

    @Inject
    PanelEuropeDockBehavior(final EuropeCallback europeDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final DialogNotEnoughGold dialogNotEnoughGold,
            final ChooseGoodsDialog chooseGoodsDialog, final EventBus eventBus) {
        super(gameModelController, imageProvider);
        this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        this.chooseGoodsDialog = Preconditions.checkNotNull(chooseGoodsDialog);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Override
    public List<UnitWithCargo> getUnitsInPort() {
        return gameModelController.getModel().getEurope().getPort()
                .getShipsInPort(gameModelController.getCurrentPlayer());
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected, final ClipboardEval eval) {
        logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());

        final Goods goods = eval.getGoods().get();
        if (goods.isZero()) {
            return;
        }

        final Goods maxPossibleGoods = targetCargoSlot.maxPossibleGoodsToMoveHere(goods);
        final From transferFrom = eval.getFrom().get();

        if (From.VALUE_FROM_EUROPE_SHOP == transferFrom) {
            if (specialOperatonWasSelected) {
                eventBus.post(new ShowScreenEvent(Screen.MARKET_BUY,
                        new ScreenMarketBuyContext(maxPossibleGoods, targetCargoSlot)));
                return;
            } else {
                try {
                    targetCargoSlot.buyAndStoreFromEuropePort(maxPossibleGoods);
                } catch (NotEnoughtGoldException e) {
                    dialogNotEnoughGold.showAndWait();
                }
            }
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            final CargoSlot sourceCargoSlot = eval.getCargoSlot().get();
            // transfer between cargo slots
            if (specialOperatonWasSelected) {
                chooseGoodsDialog.init(goods);
                targetCargoSlot.storeFromCargoSlot(chooseGoodsDialog.getActualValue(),
                        sourceCargoSlot);
            } else {
                targetCargoSlot.storeFromCargoSlot(goods, sourceCargoSlot);
            }
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