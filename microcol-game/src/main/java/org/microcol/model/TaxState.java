package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.microcol.model.store.TaxDemandPo;
import org.microcol.model.store.TaxPo;
import org.microcol.model.store.TaxProtestPo;

import com.google.common.base.Preconditions;

/**
 * The one authoritative tax state of a colonial player: rate, demand cadence,
 * the unresolved demand and commodity arrears. A Crown has none of its own.
 */
public final class TaxState {

    /** Highest rate a demand can reach. */
    public static final int MAX_RATE = 75;

    /** Percentage points added by one accepted demand. */
    public static final int INCREMENT = 2;

    /** Persisted unresolved demand; the protest part is null without stock. */
    static final class Frozen {
        private final int proposedRate;
        private final GoodsType goods;
        private final int colonyId;
        private final int quantity;
        private final int bid;

        Frozen(final int proposedRate, final GoodsType goods,
                final int colonyId, final int quantity, final int bid) {
            this.proposedRate = proposedRate;
            this.goods = goods;
            this.colonyId = colonyId;
            this.quantity = quantity;
            this.bid = bid;
        }

        int getProposedRate() {
            return proposedRate;
        }

        boolean hasProtest() {
            return goods != null;
        }

        GoodsType getGoods() {
            return goods;
        }

        int getColonyId() {
            return colonyId;
        }

        int getQuantity() {
            return quantity;
        }

        int getBid() {
            return bid;
        }
    }

    private int rate;

    private boolean activated;

    private int updates;

    private int issued;

    private Frozen pending;

    private final Map<GoodsType, Integer> arrears = new TreeMap<>(
            Comparator.comparingInt(GoodsType.GOOD_TYPES::indexOf));

    TaxState(final TaxPo po, final Difficulty difficulty) {
        Preconditions.checkArgument(po != null, "Missing colonial tax state");
        Preconditions.checkArgument(
                po.getUpdates() == null || po.getUpdates() == 0
                        || difficulty != null,
                "Tax cadence needs a difficulty");
        Preconditions
                .checkArgument(
                        po.getRate() != null && po.getRate() >= 0
                                && po.getRate() <= MAX_RATE
                                && po.getActivated() != null
                                && po.getUpdates() != null
                                && po.getUpdates() >= 0
                                && (po.getUpdates() == 0 || po
                                        .getUpdates() < interval(difficulty))
                                && po.getIssued() != null && po.getIssued() >= 0
                                && po.getArrears() != null,
                        "Invalid tax counters");
        rate = po.getRate();
        activated = po.getActivated();
        updates = po.getUpdates();
        issued = po.getIssued();
        Preconditions.checkArgument(
                activated || updates == 0 && po.getPending() == null,
                "Tax cadence needs a first colony");
        for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            final Integer owed = po.getArrears().get(type.name());
            if (owed != null) {
                Preconditions.checkArgument(owed >= TaxProtest.MIN_ARREARS,
                        "Invalid tax arrears");
                arrears.put(type, owed);
            }
        }
        Preconditions.checkArgument(arrears.size() == po.getArrears().size(),
                "Unknown or duplicate arrears commodity");
        pending = po.getPending() == null ? null : freeze(po.getPending());
    }

    private Frozen freeze(final TaxDemandPo demand) {
        // The counter restarts when a demand falls due, and a demand is only
        // issued to a nation whose cadence has been activated.
        Preconditions.checkArgument(
                activated && updates == 0 && demand.getProposedRate() != null
                        && issued >= 1 && rate < MAX_RATE
                        && demand.getProposedRate() == nextRate(),
                "Invalid pending tax demand");
        final TaxProtestPo protest = demand.getProtest();
        if (protest == null) {
            return new Frozen(demand.getProposedRate(), null, -1, 0, 0);
        }
        Preconditions.checkArgument(
                protest.getGoods() != null
                        && GoodsType.BUYABLE_GOOD_TYPES
                                .contains(protest.getGoods())
                        && protest.getColonyId() != null
                        && protest.getColonyId() >= 0
                        && protest.getQuantity() != null
                        && protest.getQuantity() >= 1
                        && protest.getQuantity() <= TaxProtest.MAX_DESTROYED
                        && protest.getBid() != null && protest.getBid() >= 0,
                "Invalid pending tax protest");
        // An already boycotted commodity is never offered for protest.
        Preconditions.checkArgument(!arrears.containsKey(protest.getGoods()),
                "Pending tax protest names a boycotted commodity");
        try {
            TaxProtest.arrears(protest.getQuantity(), protest.getBid());
        } catch (final ArithmeticException overflow) {
            throw new IllegalArgumentException(
                    "Pending tax arrears exceed supported range", overflow);
        }
        return new Frozen(demand.getProposedRate(), protest.getGoods(),
                protest.getColonyId(), protest.getQuantity(), protest.getBid());
    }

    /**
     * Create the strict saved state for a new colonial nation.
     *
     * @return zero rate, inactive cadence, no demand and no arrears
     */
    public static TaxPo initial() {
        final TaxPo po = new TaxPo();
        po.setRate(0);
        po.setActivated(false);
        po.setUpdates(0);
        po.setIssued(0);
        po.setArrears(new LinkedHashMap<>());
        return po;
    }

    /**
     * @param difficulty selected game profile
     * @return own updates between two demands, {@code max(5, 20 - 3d)}
     */
    public static int interval(final Difficulty difficulty) {
        return Math.max(5, 20 - 3 * difficulty.ordinal());
    }

    TaxPo save() {
        final TaxPo po = new TaxPo();
        po.setRate(rate);
        po.setActivated(activated);
        po.setUpdates(updates);
        po.setIssued(issued);
        final Map<String, Integer> saved = new LinkedHashMap<>();
        arrears.forEach((type, owed) -> saved.put(type.name(), owed));
        po.setArrears(saved);
        if (pending != null) {
            final TaxDemandPo demand = new TaxDemandPo();
            demand.setProposedRate(pending.proposedRate);
            if (pending.hasProtest()) {
                final TaxProtestPo protest = new TaxProtestPo();
                protest.setGoods(pending.goods);
                protest.setColonyId(pending.colonyId);
                protest.setQuantity(pending.quantity);
                protest.setBid(pending.bid);
                demand.setProtest(protest);
            }
            po.setPending(demand);
        }
        return po;
    }

    int getRate() {
        return rate;
    }

    void setRate(final int newRate) {
        Preconditions.checkArgument(newRate >= 0 && newRate <= MAX_RATE,
                "Tax rate must be between 0 and %s", MAX_RATE);
        rate = newRate;
    }

    int getIssued() {
        return issued;
    }

    /** @return the rate acceptance would set, never above the cap */
    int nextRate() {
        return Math.min(MAX_RATE, rate + INCREMENT);
    }

    /**
     * Count one own update.
     *
     * @param hasColony  whether the nation owns a colony now
     * @param difficulty selected game profile
     * @return true when this update completes a demand interval
     */
    boolean advance(final boolean hasColony, final Difficulty difficulty) {
        if (!activated) {
            if (!hasColony) {
                return false;
            }
            activated = true;
        }
        updates++;
        if (updates < interval(difficulty)) {
            return false;
        }
        updates = 0;
        return true;
    }

    boolean isPending() {
        return pending != null;
    }

    Frozen getPending() {
        return pending;
    }

    /** @return the serial of the demand being issued */
    int issue() {
        Preconditions.checkState(
                pending == null && rate < MAX_RATE
                        && issued < Integer.MAX_VALUE,
                "No tax demand can be issued");
        return ++issued;
    }

    void setPending(final Frozen frozen) {
        Preconditions.checkState(pending == null);
        pending = Preconditions.checkNotNull(frozen);
    }

    void clearPending() {
        pending = null;
    }

    boolean isBoycotted(final GoodsType type) {
        return arrears.containsKey(type);
    }

    /** @return commodities owed, in stable goods order, with their arrears */
    Map<GoodsType, Integer> getArrears() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(arrears));
    }

    /** @return whether the arrears can grow by the amount without overflow */
    boolean canAddArrears(final GoodsType type, final int amount) {
        return (long) arrears.getOrDefault(type, 0)
                + amount <= Integer.MAX_VALUE;
    }

    void addArrears(final GoodsType type, final int amount) {
        Preconditions.checkState(canAddArrears(type, amount));
        arrears.merge(type, amount, Integer::sum);
    }

    /** Remove exactly one commodity's arrears; the other boycotts stay. */
    void removeArrears(final GoodsType type) {
        Preconditions.checkState(arrears.containsKey(type),
                "No arrears to pay for %s", type);
        arrears.remove(type);
    }

    /** @return the commodities whose boycott was removed */
    List<GoodsType> clearArrears() {
        final List<GoodsType> cleared = new ArrayList<>(arrears.keySet());
        arrears.clear();
        return cleared;
    }
}
