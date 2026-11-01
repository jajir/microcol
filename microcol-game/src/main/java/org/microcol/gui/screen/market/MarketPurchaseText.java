package org.microcol.gui.screen.market;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.screen.europe.Europe;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropePurchaseLimit;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

/**
 * Localized explanations of a European purchase shared by the purchase choice
 * and the quick buy. All amounts come from the model snapshot.
 */
public final class MarketPurchaseText {

    private MarketPurchaseText() {
    }

    /**
     * @param i18n  required localization
     * @param limit required purchase snapshot that is not blocked
     * @return why no more than the maximum can be bought
     */
    public static String limitReason(final I18n i18n,
            final EuropePurchaseLimit limit) {
        final int maximum = limit.getMaximum().getAmount();
        if (limit.isLimitedByGold() && limit.isLimitedByRoom()) {
            return i18n.get(Market.limitBoth, maximum);
        } else if (limit.isLimitedByGold()) {
            return i18n.get(Market.limitGold, maximum);
        }
        return i18n.get(Market.limitHold, maximum);
    }

    /**
     * @param i18n  required localization
     * @param limit required blocked purchase snapshot
     * @return why nothing was bought
     */
    public static String blockedReason(final I18n i18n,
            final EuropePurchaseLimit limit) {
        final String goodsName = name(i18n, limit.getGoodsType());
        switch (limit.getBlocker()) {
        case TRADE_UNAVAILABLE:
            return i18n.get(Europe.equipmentTRADE_RESTRICTED);
        case BOYCOTTED:
            return i18n.get(Europe.goodsBoycotted, goodsName);
        case HOLD_UNAVAILABLE:
            return i18n.get(Market.blockedHold, goodsName);
        case HOLD_FULL:
            return i18n.get(Market.blockedFull, goodsName);
        case NOT_ENOUGH_GOLD:
            return i18n.get(Market.blockedGold, goodsName);
        default:
            throw new IllegalArgumentException(
                    "Purchase is not blocked: " + limit);
        }
    }

    /**
     * @param i18n   required localization
     * @param bought required bought goods
     * @param total  paid gold including tax
     * @return status message about the completed purchase
     */
    public static String bought(final I18n i18n, final Goods bought,
            final int total) {
        return i18n.get(Market.bought, name(i18n, bought.getType()),
                bought.getAmount(), total);
    }

    /**
     * @param i18n required localization
     * @param mac  whether the quick-buy key is Command
     * @return visible name of the quick-buy key
     */
    public static String quickBuyKey(final I18n i18n, final boolean mac) {
        return i18n.get(mac ? Market.quickBuyKeyMac : Market.quickBuyKeyOther);
    }

    /**
     * @param i18n required localization
     * @param mac  whether the quick-buy key is Command
     * @return name of the quick-buy key for assistive technologies
     */
    public static String quickBuyKeySpoken(final I18n i18n, final boolean mac) {
        return i18n.get(
                mac ? Market.quickBuyKeyMacSpoken : Market.quickBuyKeyOther);
    }

    private static String name(final I18n i18n, final GoodsType goodsType) {
        return i18n.get(GoodsTypeName.getNameForGoodsType(goodsType));
    }

}
