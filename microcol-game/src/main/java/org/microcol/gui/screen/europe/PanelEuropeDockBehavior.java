package org.microcol.gui.screen.europe;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.dock.AbstractPanelDockBehavior;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.market.MarketPurchaseText;
import org.microcol.gui.screen.market.ScreenMarketBuyContext;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.EuropePurchaseLimit;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;

/**
 * Define behavior of ship cargo slot.
 */
public final class PanelEuropeDockBehavior extends AbstractPanelDockBehavior {

    final Logger logger = LoggerFactory
            .getLogger(PanelEuropeDockBehavior.class);

    private final EuropeCallback europeDialogCallback;
    private final GameModelController gameModelController;
    private final ChooseGoodsDialog chooseGoodsDialog;
    private final EventBus eventBus;
    private final QuickBuyShortcut quickBuyShortcut;
    private final I18n i18n;

    @Inject
    PanelEuropeDockBehavior(final EuropeCallback europeDialogCallback,
            final GameModelController gameModelController,
            final ImageProvider imageProvider,
            final ChooseGoodsDialog chooseGoodsDialog, final EventBus eventBus,
            final QuickBuyShortcut quickBuyShortcut, final I18n i18n) {
        super(gameModelController, imageProvider);
        this.europeDialogCallback = Preconditions
                .checkNotNull(europeDialogCallback);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.chooseGoodsDialog = Preconditions.checkNotNull(chooseGoodsDialog);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.quickBuyShortcut = Preconditions.checkNotNull(quickBuyShortcut);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    @Override
    public List<UnitWithCargo> getUnitsInPort() {
        return gameModelController.getModel().getEurope().getPort()
                .getShipsInPort(gameModelController.getHumanPlayer());
    }

    @Override
    public boolean isCorrectObject(final CargoSlot cargoSlot,
            final MouseDragEvent event) {
        return cargoSlot != null && event.getGestureSource() instanceof Node
                && ((Node) event.getGestureSource())
                        .getUserData() instanceof PanelGood;
    }

    @Override
    public void onDragDropped(final CargoSlot cargoSlot,
            final MouseDragEvent event) {
        final PanelGood source = (PanelGood) ((Node) event.getGestureSource())
                .getUserData();
        buyGoods(cargoSlot, quickBuyShortcut.isQuickBuy(event),
                source.getGoodsType());
        europeDialogCallback.repaintAfterGoodMoving();
    }

    /**
     * A normal drop opens the purchase choice with the model maximum; the held
     * quick-buy key buys that maximum at once.
     */
    private void buyGoods(final CargoSlot targetCargoSlot,
            final boolean quickBuy, final GoodsType goodsType) {
        final EuropePurchaseLimit limit = targetCargoSlot
                .getEuropePurchaseLimit(goodsType);
        if (limit.isBlocked()) {
            postStatus(MarketPurchaseText.blockedReason(i18n, limit));
        } else if (quickBuy) {
            final int amount = limit.getMaximum().getAmount();
            final Goods bought = targetCargoSlot.buyFromEuropePort(limit,
                    amount);
            postStatus(MarketPurchaseText.bought(i18n, bought,
                    limit.quote(amount).getTotal()));
        } else {
            eventBus.post(new ShowScreenEvent(Screen.MARKET_BUY,
                    new ScreenMarketBuyContext(limit, targetCargoSlot)));
        }
    }

    private void postStatus(final String message) {
        eventBus.post(StatusBarMessageEvent.outcome(message, Source.EUROPE));
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected,
            final ClipboardEval eval) {
        logger.debug("wasShiftPressed "
                + europeDialogCallback.getPropertyShiftWasPressed().get());

        final Goods goods = eval.getGoods().get();
        if (goods.isZero()) {
            return;
        }

        final From transferFrom = eval.getFrom().get();
        if (From.VALUE_FROM_UNIT == transferFrom) {
            final CargoSlot sourceCargoSlot = eval.getCargoSlot().get();
            // transfer between cargo slots
            if (specialOperatonWasSelected) {
                chooseGoodsDialog.init(goods);
                if (chooseGoodsDialog.getActualValue().isZero()) {
                    return;
                }
                targetCargoSlot.storeFromCargoSlot(
                        chooseGoodsDialog.getActualValue(), sourceCargoSlot);
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
