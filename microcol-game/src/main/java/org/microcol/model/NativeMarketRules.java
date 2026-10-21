package org.microcol.model;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Pure rules of a native settlement market (R-0159): which goods a settlement
 * offers and wants, how its stock and treasury change at each owner update, and
 * which goods a village may not buy again on the next expedition. Local
 * production uses only the natural output of the surrounding squares, so no
 * European profession, building or land improvement counts.
 */
public final class NativeMarketRules {

    /**
     * Most goods a settlement offers for sale; missing slots stay unavailable.
     */
    public static final int OFFER_SLOTS = 3;

    /** Goods a settlement wants to buy from the player. */
    public static final int WANTED_SLOTS = 3;

    /** Stock of every offered good when the map is created. */
    public static final int INITIAL_STOCK = 100;

    /** Stock added to each offered good per owner update. */
    public static final int RESTOCK = 10;

    /** Highest stock that restocking reaches. */
    public static final int STOCK_CAP = 200;

    /** Treasury gained per owner update by an ordinary settlement. */
    static final int ORDINARY_TREASURY_GAIN = 20;

    /** Treasury gained per owner update by a capital. */
    static final int CAPITAL_TREASURY_GAIN = 40;

    private NativeMarketRules() {
    }

    /**
     * Goods the settlement offers: tradable goods its surroundings produce
     * naturally, highest potential first, then goods name; at most
     * {@link #OFFER_SLOTS}. A short list is never padded.
     *
     * @param surroundings required squares around the settlement, its own
     *                     square included
     * @return offered goods in offer order
     */
    public static List<GoodsType> offeredGoods(
            final List<Terrain> surroundings) {
        Preconditions.checkNotNull(surroundings);
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(goods -> potential(surroundings, goods) > 0)
                .sorted(Comparator.comparingInt(
                        (GoodsType goods) -> potential(surroundings, goods))
                        .reversed().thenComparing(GoodsType::name))
                .limit(OFFER_SLOTS).collect(Collectors.toList());
    }

    /**
     * Goods the settlement wants: the tradable goods its surroundings produce
     * least, lowest potential first, then goods name; exactly
     * {@link #WANTED_SLOTS}, since the tradable list is longer than that.
     *
     * @param surroundings required squares around the settlement, its own
     *                     square included
     * @return wanted goods in demand order
     */
    public static List<GoodsType> wantedGoods(
            final List<Terrain> surroundings) {
        Preconditions.checkNotNull(surroundings);
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .sorted(Comparator.comparingInt(
                        (GoodsType goods) -> potential(surroundings, goods))
                        .thenComparing(GoodsType::name))
                .limit(WANTED_SLOTS).collect(Collectors.toList());
    }

    /**
     * @return natural output summed over the surroundings of one goods type
     */
    static int potential(final List<Terrain> surroundings,
            final GoodsType goods) {
        return surroundings.stream()
                .mapToInt(terrain -> terrain.getNaturalOutput(goods)).sum();
    }

    /**
     * Treasury after one owner update. A treasury already at or above twice its
     * initial amount is kept, never reduced; below it the gain is added and
     * capped there.
     *
     * @param treasury current gold, nonnegative
     * @param capital  whether the settlement is its nation's capital
     * @return treasury after the update
     */
    static int treasuryAfterUpdate(final int treasury, final boolean capital) {
        final int initial = capital ? NativeSettlement.CAPITAL_INITIAL_TREASURY
                : NativeSettlement.ORDINARY_INITIAL_TREASURY;
        final int cap = 2 * initial;
        if (treasury >= cap) {
            return treasury;
        }
        return Math.min(cap, treasury
                + (capital ? CAPITAL_TREASURY_GAIN : ORDINARY_TREASURY_GAIN));
    }

    /**
     * @param stock current stock of one offered good
     * @return stock after one owner update, capped at {@link #STOCK_CAP}
     */
    static int stockAfterUpdate(final int stock) {
        return stock >= STOCK_CAP ? stock
                : Math.min(STOCK_CAP, stock + RESTOCK);
    }

    /**
     * Whether a village refuses to buy a good again on the next expedition: the
     * same good the previous expedition bought. Muskets are the stated
     * exception, and no other class has this restriction.
     *
     * @param settlementClass required class of the settlement
     * @param previous        good bought by the previous expedition, or null
     * @param goods           required good asked for now
     * @return whether the repeated purchase is refused
     */
    static boolean repeatsPreviousGood(
            final NativeSettlementClass settlementClass,
            final GoodsType previous, final GoodsType goods) {
        return settlementClass == NativeSettlementClass.VILLAGE
                && goods.equals(previous) && !GoodsType.MUSKET.equals(goods);
    }

    /**
     * Price per piece a settlement pays before its demand, ship and haggle
     * factors: three quarters of the Europe bid, floored, and never below one
     * gold.
     *
     * @param europeBid current Europe bid of the good, positive
     * @return base price of one piece
     */
    static int buyBasePrice(final int europeBid) {
        return Math.max(1, europeBid * 3 / 4);
    }

    /**
     * Gold a settlement pays for a quantity of one good (R-0160). The base
     * price of a piece is multiplied by ×1.5 for a wanted good, ×0.8 for ship
     * receipts and ×1.1 for a successful haggle; the product of the full
     * quantity is floored once. No European tax applies.
     *
     * @param quantity  nonnegative pieces sold
     * @param europeBid current Europe bid of the good
     * @param wanted    whether the settlement wants this good
     * @param ship      whether the seller is a ship
     * @param haggled   whether the haggle succeeded
     * @return gold paid, exact floor of the full quote
     */
    static int saleTotal(final int quantity, final int europeBid,
            final boolean wanted, final boolean ship, final boolean haggled) {
        final long pieces = Math.multiplyExact((long) quantity,
                buyBasePrice(europeBid));
        // Factors in hundredths of the base: 150 wanted, 80 ship, 110 haggled.
        final long factors = (wanted ? 150L : 100L) * (ship ? 80L : 100L)
                * (haggled ? 110L : 100L);
        return Math
                .toIntExact(Math.multiplyExact(pieces, factors) / 1_000_000L);
    }

    /**
     * Most pieces that the treasury can pay for, found by bisection over the
     * monotone quote; the cargo limits the search from above.
     *
     * @param held      pieces the carrier holds
     * @param treasury  gold the settlement holds
     * @param europeBid current Europe bid of the good
     * @param wanted    whether the settlement wants this good
     * @param ship      whether the seller is a ship
     * @param haggled   whether the haggle succeeded
     * @return largest affordable quantity, from zero to {@code held}
     */
    static int maxSaleQuantity(final int held, final int treasury,
            final int europeBid, final boolean wanted, final boolean ship,
            final boolean haggled) {
        int low = 0;
        int high = held;
        while (low < high) {
            final int middle = low + (high - low + 1) / 2;
            if (saleTotal(middle, europeBid, wanted, ship,
                    haggled) <= treasury) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return low;
    }

    /**
     * Result of the one haggle of an expedition: with the chance of
     * {@code roll} below the content or uneasy probability, 50% and 25%
     * respectively. Any other attitude never accepts.
     *
     * @param hostility attitude of the settlement toward the carrier
     * @param roll      saved random draw in [0, 1)
     * @return whether the haggle succeeds
     */
    static boolean haggleSucceeds(final NativeHostility hostility,
            final double roll) {
        if (hostility == NativeHostility.CONTENT) {
            return roll < 0.5;
        }
        return hostility == NativeHostility.UNEASY && roll < 0.25;
    }

    /**
     * Gold a settlement charges for a complete purchase (R-0161): the Europe
     * ask of each piece times the pieces, with no tax and no other factor. A
     * successful request takes 10% off the whole quote, and the positive total
     * then rounds up to whole gold once.
     *
     * @param quantity   positive pieces bought
     * @param askPrice   Europe ask of one piece, positive
     * @param discounted whether the one request of the purchase succeeded
     * @return gold paid for the complete quote
     */
    static int purchaseTotal(final int quantity, final int askPrice,
            final boolean discounted) {
        final long full = Math.multiplyExact((long) quantity, (long) askPrice);
        if (!discounted) {
            return Math.toIntExact(full);
        }
        // Ninety per cent of the full quote, rounded up: ceil(9 × full / 10).
        return Math.toIntExact((Math.multiplyExact(full, 9L) + 9L) / 10L);
    }

    /**
     * Most pieces of one purchase that the payable gold covers, found by
     * bisection over the monotone quote; the limit is the stock or the room in
     * the carrier, whichever is smaller.
     *
     * @param limit      most pieces the stock and the carrier allow
     * @param payable    gold the buyer may pay and the settlement may receive
     * @param askPrice   Europe ask of one piece, positive
     * @param discounted whether the one request of the purchase succeeded
     * @return largest affordable quantity, from zero to {@code limit}
     */
    static int maxPurchaseQuantity(final int limit, final int payable,
            final int askPrice, final boolean discounted) {
        int low = 0;
        int high = limit;
        while (low < high) {
            final int middle = low + (high - low + 1) / 2;
            if (purchaseTotal(middle, askPrice, discounted) <= payable) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return low;
    }

    /**
     * @param offered saved stock per offered good, in offer order
     * @return whether the stock names only offered goods with allowed amounts
     */
    static boolean isValidStock(final Map<GoodsType, Integer> offered) {
        return offered.values().stream()
                .allMatch(stock -> stock >= 0 && stock <= STOCK_CAP);
    }

}
