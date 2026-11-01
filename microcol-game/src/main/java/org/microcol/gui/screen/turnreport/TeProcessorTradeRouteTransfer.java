package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventTradeRouteTransfer;

import com.google.inject.Inject;

/**
 * Display one recorded automatic route transfer from its snapshot, without
 * reading the current route, carrier, colony or prices.
 */
public final class TeProcessorTradeRouteTransfer extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorTradeRouteTransfer(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (!(turnEvent instanceof TurnEventTradeRouteTransfer)) {
            return null;
        }
        final TurnEventTradeRouteTransfer transfer = (TurnEventTradeRouteTransfer) turnEvent;
        final String carrier = getI18n()
                .get(UnitTypes.getUnitName(transfer.getCarrierType()));
        final String goods = getI18n().get(GoodsTypeName
                .getNameForGoodsType(transfer.getGoods().getType()));
        final int stop = transfer.getStopIndex() + 1;
        final int amount = transfer.getGoods().getAmount();
        if (transfer.isEurope()) {
            return new TeItemSimple(getI18n().get(
                    transfer.isUnload() ? TurnEvents.tradeRouteSold
                            : TurnEvents.tradeRouteBought,
                    transfer.getRouteName(), carrier, transfer.getCarrierId(),
                    stop, amount, goods,
                    String.format("%+d", transfer.getGoldChange())));
        }
        return new TeItemSimple(getI18n().get(
                transfer.isUnload() ? TurnEvents.tradeRouteUnloaded
                        : TurnEvents.tradeRouteLoaded,
                transfer.getRouteName(), carrier, transfer.getCarrierId(), stop,
                transfer.getColonyName().orElseThrow(), amount, goods));
    }

}
