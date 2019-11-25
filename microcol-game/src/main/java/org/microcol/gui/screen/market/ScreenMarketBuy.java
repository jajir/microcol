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
public class ScreenMarketBuy extends AbstractScreenMarket {

    private final MarketBuyPanel marketBuyPanel;

    @Inject
    ScreenMarketBuy(final I18n i18n, final MarketBuyPanel marketBuyPanel,
            final MarketView marketView) {
        super(i18n, marketView);
        this.marketBuyPanel = Preconditions.checkNotNull(marketBuyPanel);
        marketView.setMenuPanel(marketBuyPanel);
    }

    public void init(final ScreenMarketBuyContext screenMarketContext) {
        marketBuyPanel.init(Preconditions.checkNotNull(screenMarketContext));
        updateTitle(screenMarketContext.getGoodsType());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        marketBuyPanel.updateLanguage(i18n);
        updateTitle(GoodsType.CORN);
    }

    private void updateTitle(final GoodsType goodsType) {
        getMarketView().setTitle(getI18n().get(Market.titleBuy)
                + getI18n().get(GoodsTypeName.getKey(goodsType, 7, false)));
    }

}
