package org.microcol.gui.screen.turnreport;

import static org.microcol.model.GoodsType.BELL;
import static org.microcol.model.GoodsType.CIGARS;
import static org.microcol.model.GoodsType.COAT;
import static org.microcol.model.GoodsType.COTTON;
import static org.microcol.model.GoodsType.CROSS;
import static org.microcol.model.GoodsType.GOODS;
import static org.microcol.model.GoodsType.HORSE;
import static org.microcol.model.GoodsType.MUSKET;
import static org.microcol.model.GoodsType.ORE;
import static org.microcol.model.GoodsType.RUM;
import static org.microcol.model.GoodsType.SUGAR;
import static org.microcol.model.GoodsType.TOBACCO;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.GoodsTypeName;
import org.microcol.i18n.I18n;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventGoodsWasThrownAway;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorGoodsWasThrownAway extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    final static List<GoodsType> masculine = ImmutableList.of(TOBACCO, HORSE, RUM, CIGARS, COAT,
            CROSS, BELL);

    final static List<GoodsType> feminine = ImmutableList.of(SUGAR, COTTON, ORE, GOODS, MUSKET);

//    final static List<GoodsType> neuter = ImmutableList.of(CORN, FUR, LUMBER, SILVER, COTTON, TOOLS,
//            HAMMERS);

    @Inject
    TeProcessorGoodsWasThrownAway(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventGoodsWasThrownAway) {
            final TurnEventGoodsWasThrownAway te = (TurnEventGoodsWasThrownAway) turnEvent;
            return new TeItemSimple(getMessage(te.getGoods(), te.getColonyName()));
        }
        return null;
    }

    private String getMessage(final Goods goods, final String colonyName) {
        final String goodsName = getI18n().get(GoodsTypeName.getKey(goods.getType(), 2, false));
        if (goods.getAmount() == 1) {
            if (masculine.contains(goods.getType())) {
                return getI18n().get(TurnEvents.goodsWasThrowsAway2, colonyName, goodsName);
            } else if (feminine.contains(goods.getType())) {
                return getI18n().get(TurnEvents.goodsWasThrowsAway3, colonyName, goodsName);
            } else {
                return getI18n().get(TurnEvents.goodsWasThrowsAway4, colonyName, goodsName);
            }
        } else {
            return getI18n().get(TurnEvents.goodsWasThrowsAway1, colonyName, goods.getAmount(),
                    goodsName);
        }
    }

}
