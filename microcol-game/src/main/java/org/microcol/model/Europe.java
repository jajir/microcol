package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Europe {

    private final EuropePort port;

    private final EuropePier pier;

    private final Model model;

    private final List<GoodsTrade> goodsTrades = ImmutableList.of(new GoodsTrade(GoodsType.CORN, 2, 4),
            new GoodsTrade(GoodsType.SUGAR, 3, 5), new GoodsTrade(GoodsType.TOBACCO, 3, 5),
            new GoodsTrade(GoodsType.COTTON, 3, 5), new GoodsTrade(GoodsType.FUR, 4, 8),
            new GoodsTrade(GoodsType.LUMBER, 3, 8), new GoodsTrade(GoodsType.ORE, 4, 8),
            new GoodsTrade(GoodsType.SILVER, 3, 20), new GoodsTrade(GoodsType.HORSE, 2, 5),
            new GoodsTrade(GoodsType.RUM, 11, 12), new GoodsTrade(GoodsType.CIGARS, 11, 12),
            new GoodsTrade(GoodsType.SILK, 11, 12), new GoodsTrade(GoodsType.COAT, 9, 10),
            new GoodsTrade(GoodsType.GOODS, 2, 3), new GoodsTrade(GoodsType.TOOLS, 1, 2),
            new GoodsTrade(GoodsType.MUSKET, 2, 3));

    Europe(final Model model) {
        this.model = Preconditions.checkNotNull(model);
        port = new EuropePort(this.model);
        pier = new EuropePier(this.model);
    }

    public GoodsTrade getGoodsTradeForType(final GoodsType goodsType) {
        return goodsTrades.stream().filter(goodsTrade -> goodsTrade.getGoodsType().equals(goodsType))
                .findAny().get();
    }

    public EuropePort getPort() {
        return port;
    }

    public EuropePier getPier() {
        return pier;
    }

}
