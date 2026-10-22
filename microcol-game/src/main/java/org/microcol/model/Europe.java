package org.microcol.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
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

    /** Settled cumulative accounts; a missing pair is an empty account. */
    private final Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> accounts;

    /**
     * Net sale revenue of the last ten owner turns; a missing nation is empty.
     */
    private final Map<EuropeanNation, EuropeSaleWindow> saleWindows;

    /** New game: every national market starts at the catalogue state. */
    Europe(final Model model) {
        this(model, initialMarkets(),
                new EnumMap<EuropeanNation, Map<GoodsType, EuropeTradeAccount>>(
                        EuropeanNation.class),
                new EnumMap<EuropeanNation, EuropeSaleWindow>(
                        EuropeanNation.class));
    }

    /** Saved game: the complete market state is validated before use. */
    Europe(final Model model, final EuropePo po) {
        this(model, restoreMarkets(po), restoreAccounts(po),
                restoreSaleWindows(po));
    }

    private Europe(final Model model,
            final Map<EuropeanNation, List<GoodsTrade>> markets,
            final Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> accounts,
            final Map<EuropeanNation, EuropeSaleWindow> saleWindows) {
        this.model = Preconditions.checkNotNull(model);
        this.markets = markets;
        this.accounts = accounts;
        this.saleWindows = saleWindows;
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
     * @param nation    nation whose account is read
     * @param goodsType commodity with a European market
     * @return the settled cumulative account; a pair never traded is empty
     */
    public EuropeTradeAccount getTradeAccount(final EuropeanNation nation,
            final GoodsType goodsType) {
        Preconditions.checkNotNull(nation);
        Preconditions.checkArgument(goodsType.isCanBeBought(),
                "%s has no European market", goodsType);
        return accounts.getOrDefault(nation, Map.of()).getOrDefault(goodsType,
                EuropeTradeAccount.EMPTY);
    }

    /**
     * @return an unmodifiable snapshot of every nonempty account, by nation and
     *         in goods order of each nation
     */
    public Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> getTradeAccounts() {
        final Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> out = new EnumMap<>(
                EuropeanNation.class);
        accounts.forEach((nation, goods) -> out.put(nation,
                Collections.unmodifiableMap(new LinkedHashMap<>(goods))));
        return Collections.unmodifiableMap(out);
    }

    /**
     * @param nation nation whose European sales are read
     * @return net gold of its committed sales in the last ten owner turns;
     *         purchases, previews and refused trades never count
     */
    public int getRecentSaleRevenue(final EuropeanNation nation) {
        return getSaleWindow(nation).getRevenue();
    }

    EuropeSaleWindow getSaleWindow(final EuropeanNation nation) {
        Preconditions.checkNotNull(nation);
        return saleWindows.getOrDefault(nation, EuropeSaleWindow.EMPTY);
    }

    /**
     * Stores a window after its sale has been computed. An empty window is
     * removed, so it is never saved.
     *
     * @param nation nation whose sales the window counts
     * @param window the complete window after the settlement
     */
    void saveSaleWindow(final EuropeanNation nation,
            final EuropeSaleWindow window) {
        if (window.getRevenue() == 0) {
            saleWindows.remove(nation);
        } else {
            saleWindows.put(nation, window);
        }
    }

    /**
     * Starts the nation's own next owner turn. Called only from that owner's
     * turn start, never from loads, Continue or reports.
     *
     * @param nation nation whose owner turn begins
     */
    void advanceSaleWindow(final EuropeanNation nation) {
        saveSaleWindow(nation, getSaleWindow(nation).advanceOwnerTurn());
    }

    /**
     * Stores an account after its settlement has been applied. The caller has
     * already computed the account before any mutation, so this cannot fail on
     * arithmetic.
     *
     * @param nation    nation whose market settled the trade
     * @param goodsType traded commodity
     * @param account   the complete account after the settlement
     */
    void saveTradeAccount(final EuropeanNation nation,
            final GoodsType goodsType, final EuropeTradeAccount account) {
        final Map<GoodsType, EuropeTradeAccount> nationAccounts = accounts
                .computeIfAbsent(nation, key -> new HashMap<>());
        if (account.isEmpty()) {
            nationAccounts.remove(goodsType);
        } else {
            nationAccounts.put(goodsType, account);
        }
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
        final EuropePo po = save(markets);
        po.setAccounts(savedAccounts());
        po.setSaleWindows(savedSaleWindows());
        return po;
    }

    /** Only nonempty windows are written; each holds a slot and ten buckets. */
    private Map<String, int[]> savedSaleWindows() {
        final Map<String, int[]> out = new LinkedHashMap<>();
        saleWindows.forEach(
                (nation, window) -> out.put(nation.name(), window.save()));
        return out;
    }

    /** Only nonempty accounts are written; each pair is six counters. */
    private Map<String, Map<String, int[]>> savedAccounts() {
        final Map<String, Map<String, int[]>> out = new LinkedHashMap<>();
        accounts.forEach((nation, goods) -> {
            if (goods.isEmpty()) {
                return;
            }
            final Map<String, int[]> saved = new LinkedHashMap<>();
            goods.forEach((goodsType, account) -> saved.put(goodsType.name(),
                    savedValues(account)));
            out.put(nation.name(), saved);
        });
        return out;
    }

    private static int[] savedValues(final EuropeTradeAccount account) {
        return new int[] { account.getBoughtQuantity(), account.getBoughtGold(),
                account.getSoldQuantity(), account.getSoldGold(),
                account.getRevolutionaryQuantity(),
                account.getRevolutionaryGold() };
    }

    /**
     * Restores only canonical data: names must identify a nation and a buyable
     * commodity, every pair has six counters, and an empty pair is absent.
     */
    private static Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> restoreAccounts(
            final EuropePo po) {
        Preconditions.checkArgument(po.getAccounts() != null,
                "Missing European trade accounts");
        final Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> out = new EnumMap<>(
                EuropeanNation.class);
        for (final Map.Entry<String, Map<String, int[]>> nationEntry : po
                .getAccounts().entrySet()) {
            final Map<String, int[]> savedGoods = nationEntry.getValue();
            Preconditions.checkArgument(
                    savedGoods != null && !savedGoods.isEmpty(),
                    "Empty trade accounts for %s", nationEntry.getKey());
            final EuropeanNation nation = EuropeanNation
                    .valueOf(nationEntry.getKey());
            final Map<GoodsType, EuropeTradeAccount> goods = new HashMap<>();
            for (final Map.Entry<String, int[]> goodsEntry : savedGoods
                    .entrySet()) {
                final GoodsType goodsType = GoodsType
                        .valueOf(goodsEntry.getKey());
                Preconditions.checkArgument(goodsType.isCanBeBought(),
                        "%s has no European market", goodsType);
                final int[] saved = goodsEntry.getValue();
                Preconditions.checkArgument(saved != null && saved.length == 6,
                        "Trade account for %s in %s must hold six counters",
                        goodsType, nation);
                final EuropeTradeAccount account = new EuropeTradeAccount(
                        saved[0], saved[1], saved[2], saved[3], saved[4],
                        saved[5]);
                Preconditions.checkArgument(!account.isEmpty(),
                        "Empty trade account for %s in %s", goodsType, nation);
                goods.put(goodsType, account);
            }
            out.put(nation, goods);
        }
        return out;
    }

    /**
     * Restores only canonical windows: names must identify a nation, and each
     * window is validated before any sale is recorded against it.
     */
    private static Map<EuropeanNation, EuropeSaleWindow> restoreSaleWindows(
            final EuropePo po) {
        Preconditions.checkArgument(po.getSaleWindows() != null,
                "Missing European sale windows");
        final Map<EuropeanNation, EuropeSaleWindow> out = new EnumMap<>(
                EuropeanNation.class);
        for (final Map.Entry<String, int[]> entry : po.getSaleWindows()
                .entrySet()) {
            out.put(EuropeanNation.valueOf(entry.getKey()),
                    EuropeSaleWindow.restore(entry.getValue()));
        }
        return out;
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
