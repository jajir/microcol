package org.microcol.model;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.microcol.model.store.EuropePo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Europe {

    /** Catalogue quotes; the spread between bid and ask stays constant. */
    private static final List<GoodsTrade> CATALOGUE = ImmutableList.of(
            new GoodsTrade(GoodsType.CORN, 2, 4),
            new GoodsTrade(GoodsType.SUGAR, 3, 5),
            new GoodsTrade(GoodsType.TOBACCO, 3, 5),
            new GoodsTrade(GoodsType.COTTON, 3, 5),
            new GoodsTrade(GoodsType.FUR, 4, 8),
            new GoodsTrade(GoodsType.LUMBER, 3, 8),
            new GoodsTrade(GoodsType.ORE, 4, 8),
            new GoodsTrade(GoodsType.SILVER, 3, 20),
            new GoodsTrade(GoodsType.HORSE, 2, 5),
            new GoodsTrade(GoodsType.RUM, 11, 12),
            new GoodsTrade(GoodsType.CIGARS, 11, 12),
            new GoodsTrade(GoodsType.SILK, 11, 12),
            new GoodsTrade(GoodsType.COAT, 9, 10),
            new GoodsTrade(GoodsType.GOODS, 2, 3),
            new GoodsTrade(GoodsType.TOOLS, 1, 2),
            new GoodsTrade(GoodsType.MUSKET, 2, 3));

    /** Eighths of pressure per unit of goods in the actor's own market. */
    private static final int OWN_EIGHTHS_PER_UNIT = 8;

    /** A quarter of a unit in every other European market. */
    private static final int CROSS_EIGHTHS_PER_UNIT = 2;

    /** Dutch market responds half as much: half a unit in its own market. */
    private static final int DUTCH_OWN_EIGHTHS_PER_UNIT = 4;

    /** Dutch market responds half as much: an eighth of a unit from others. */
    private static final int DUTCH_CROSS_EIGHTHS_PER_UNIT = 1;

    private final EuropePort port;

    private final EuropePier pier;

    private final Model model;

    /** One complete market per nation, in buyable goods order. */
    private final Map<EuropeanNation, List<GoodsTrade>> markets;

    /** New game: every national market starts at the catalogue state. */
    Europe(final Model model) {
        this(model, initialMarkets());
    }

    /** Saved game: the complete market state is validated before use. */
    Europe(final Model model, final EuropePo po) {
        this(model, restoreMarkets(po));
    }

    private Europe(final Model model,
            final Map<EuropeanNation, List<GoodsTrade>> markets) {
        this.model = Preconditions.checkNotNull(model);
        this.markets = markets;
        port = new EuropePort(this.model);
        pier = new EuropePier(this.model);
    }

    /** @return the catalogue state of every national market, for new games */
    public static EuropePo initialPo() {
        return save(initialMarkets());
    }

    public GoodsTrade getGoodsTradeForType(final GoodsType goodsType,
            final EuropeanNation nation) {
        Preconditions.checkArgument(goodsType.isCanBeBought(),
                "%s has no European market", goodsType);
        return markets.get(nation).stream().filter(
                goodsTrade -> goodsTrade.getGoodsType().equals(goodsType))
                .findAny().orElseThrow();
    }

    /**
     * Applies one accepted and already settled trade to every national market:
     * the full quantity to the actor's market and a quarter to each other; the
     * Dutch market takes half of each of those shares.
     *
     * @param actor        nation whose player traded
     * @param goodsType    traded commodity
     * @param signedAmount sold units are positive, bought units negative
     */
    void applyTrade(final EuropeanNation actor, final GoodsType goodsType,
            final int signedAmount) {
        if (signedAmount == 0) {
            return;
        }
        Preconditions.checkNotNull(actor);
        // Long pressure cannot overflow and addPressure cannot throw, so the
        // four markets never end up partly updated.
        for (final EuropeanNation nation : EuropeanNation.values()) {
            getGoodsTradeForType(goodsType, nation)
                    .addPressure((long) signedAmount
                            * eighthsPerUnit(nation, nation == actor));
        }
    }

    /**
     * Quiet recovery for one nation at its own update boundary; every good of
     * that national market is judged independently.
     */
    void recoverMarkets(final EuropeanNation nation) {
        markets.get(nation).forEach(GoodsTrade::recoverAtUpdate);
    }

    /**
     * @param receiver  nation whose market moves
     * @param ownMarket whether the market belongs to the trading actor
     * @return eighths of pressure per traded unit for that market
     */
    private static int eighthsPerUnit(final EuropeanNation receiver,
            final boolean ownMarket) {
        if (receiver == EuropeanNation.DUTCH) {
            return ownMarket ? DUTCH_OWN_EIGHTHS_PER_UNIT
                    : DUTCH_CROSS_EIGHTHS_PER_UNIT;
        }
        return ownMarket ? OWN_EIGHTHS_PER_UNIT : CROSS_EIGHTHS_PER_UNIT;
    }

    public EuropePort getPort() {
        return port;
    }

    public EuropePier getPier() {
        return pier;
    }

    EuropePo save() {
        return save(markets);
    }

    private static EuropePo save(
            final Map<EuropeanNation, List<GoodsTrade>> nationalMarkets) {
        final Map<String, Map<String, int[]>> out = new LinkedHashMap<>();
        for (final EuropeanNation nation : EuropeanNation.values()) {
            final Map<String, int[]> goods = new LinkedHashMap<>();
            for (final GoodsTrade trade : nationalMarkets.get(nation)) {
                goods.put(trade.getGoodsType().name(),
                        new int[] { trade.getSellPrice(), trade.getPressure(),
                                trade.isTradedSinceUpdate() ? 1 : 0 });
            }
            out.put(nation.name(), goods);
        }
        final EuropePo po = new EuropePo();
        po.setMarkets(out);
        return po;
    }

    private static Map<EuropeanNation, List<GoodsTrade>> initialMarkets() {
        final EnumMap<EuropeanNation, List<GoodsTrade>> out = new EnumMap<>(
                EuropeanNation.class);
        for (final EuropeanNation nation : EuropeanNation.values()) {
            out.put(nation, initialGoodsTrades());
        }
        return out;
    }

    private static List<GoodsTrade> initialGoodsTrades() {
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .map(Europe::initialGoodsTrade).collect(Collectors.toList());
    }

    /**
     * A tradable good missing from the catalogue starts at bid 2 and ask 4.
     */
    private static GoodsTrade initialGoodsTrade(final GoodsType goodsType) {
        return CATALOGUE.stream().filter(
                goodsTrade -> goodsTrade.getGoodsType().equals(goodsType))
                .findAny()
                .map(goodsTrade -> new GoodsTrade(goodsType,
                        goodsTrade.getSellPrice(), goodsTrade.getBuyPrice()))
                .orElseGet(() -> new GoodsTrade(goodsType, 2, 4));
    }

    private static Map<EuropeanNation, List<GoodsTrade>> restoreMarkets(
            final EuropePo po) {
        Preconditions.checkArgument(po.getMarkets() != null,
                "Missing European markets");
        Preconditions.checkArgument(
                po.getMarkets().size() == EuropeanNation.values().length,
                "European markets must name exactly the four nations");
        final EnumMap<EuropeanNation, List<GoodsTrade>> out = new EnumMap<>(
                EuropeanNation.class);
        for (final EuropeanNation nation : EuropeanNation.values()) {
            final Map<String, int[]> saved = po.getMarkets().get(nation.name());
            Preconditions.checkArgument(saved != null,
                    "Missing European market for %s", nation);
            Preconditions.checkArgument(
                    saved.size() == GoodsType.BUYABLE_GOOD_TYPES.size(),
                    "European market %s must hold exactly the tradable goods",
                    nation);
            final List<GoodsTrade> trades = initialGoodsTrades();
            for (final GoodsTrade trade : trades) {
                final int[] state = saved.get(trade.getGoodsType().name());
                Preconditions.checkArgument(state != null && state.length == 3,
                        "Missing market state for %s in %s",
                        trade.getGoodsType(), nation);
                Preconditions.checkArgument(state[2] == 0 || state[2] == 1,
                        "Traded flag must be 0 or 1");
                trade.restoreState(state[0], state[1], state[2] == 1);
            }
            out.put(nation, trades);
        }
        return out;
    }

}
