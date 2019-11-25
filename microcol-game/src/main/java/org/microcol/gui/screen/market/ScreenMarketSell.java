package org.microcol.gui.screen.market;

import org.microcol.gui.GoodsTypeName;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Screen with market in Europe. It allows to buy various goods.
 */
@Singleton
public class ScreenMarketSell extends AbstractScreenMarket {

    private final MarketSellPanel marketSellPanel;

    @Inject
    ScreenMarketSell(final I18n i18n, final MarketSellPanel marketSellPanel,
            final MarketView marketView) {
        super(i18n, marketView);
        this.marketSellPanel = Preconditions.checkNotNull(marketSellPanel);
        marketView.setMenuPanel(marketSellPanel);
    }

    public void init(final ScreenMarketSellContext screenMarketContext) {
        marketSellPanel.init(Preconditions.checkNotNull(screenMarketContext));
        updateTitle(screenMarketContext.getGoodsType());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        marketSellPanel.updateLanguage(i18n);
        updateTitle(GoodsType.CORN);
    }

    private void updateTitle(final GoodsType goodsType) {
        getMarketView().setTitle(getI18n().get(Market.titleSell)
                + getI18n().get(GoodsTypeName.getKey(goodsType, 7, false)));
    }

}
