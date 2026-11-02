package org.akropia.gui.screen.turnreport;

import static org.akropia.model.GoodsType.OWLS;
import static org.akropia.model.GoodsType.WINE;
import static org.akropia.model.GoodsType.AMPHORAE;
import static org.akropia.model.GoodsType.WOOL;
import static org.akropia.model.GoodsType.TORCHES;
import static org.akropia.model.GoodsType.GOODS;
import static org.akropia.model.GoodsType.HORSE;
import static org.akropia.model.GoodsType.WEAPONS;
import static org.akropia.model.GoodsType.ORE;
import static org.akropia.model.GoodsType.OLIVE_OIL;
import static org.akropia.model.GoodsType.OLIVES;
import static org.akropia.model.GoodsType.GRAPES;

import java.util.List;
import java.util.function.Function;

import org.akropia.gui.GoodsTypeName;
import org.akropia.i18n.I18n;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventGoodsWasThrownAway;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorGoodsWasThrownAway extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    final static List<GoodsType> masculine = ImmutableList.of(GRAPES, HORSE,
            OLIVE_OIL, WINE, AMPHORAE, TORCHES, OWLS);

    final static List<GoodsType> feminine = ImmutableList.of(OLIVES, WOOL, ORE,
            GOODS, WEAPONS);

    // final static List<GoodsType> neuter = ImmutableList.of(GRAIN, CLAY,
    // LUMBER,
    // SILVER, WOOL, TOOLS,
    // HAMMERS);

    @Inject
    TeProcessorGoodsWasThrownAway(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventGoodsWasThrownAway) {
            final TurnEventGoodsWasThrownAway te = (TurnEventGoodsWasThrownAway) turnEvent;
            return new TeItemColony(
                    getMessage(te.getGoods(), te.getColonyName()),
                    te.getColonyId());
        }
        return null;
    }

    private String getMessage(final Goods goods, final String colonyName) {
        final String goodsName = getI18n()
                .get(GoodsTypeName.getKey(goods.getType(), 2, false));
        if (goods.getAmount() == 1) {
            if (masculine.contains(goods.getType())) {
                return getI18n().get(TurnEvents.goodsWasThrowsAway2, colonyName,
                        goodsName);
            } else if (feminine.contains(goods.getType())) {
                return getI18n().get(TurnEvents.goodsWasThrowsAway3, colonyName,
                        goodsName);
            } else {
                return getI18n().get(TurnEvents.goodsWasThrowsAway4, colonyName,
                        goodsName);
            }
        } else {
            return getI18n().get(TurnEvents.goodsWasThrowsAway1, colonyName,
                    goods.getAmount(), goodsName);
        }
    }

}
