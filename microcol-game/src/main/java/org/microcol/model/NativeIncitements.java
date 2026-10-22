package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.model.store.NativeIncitementPo;

import com.google.common.base.Preconditions;

/**
 * Persistent paid-incitement orders (R-0166), at most one per native tribe. The
 * model alone changes them. Each order is saved with its remaining native
 * turns, so Save and Continue keep the exact clock and never tick again.
 *
 * The revision counts every change of any order in this session, so a quote
 * taken before a change is stale even when its visible values match again. It
 * is not saved: a loaded game starts with new quotes.
 */
final class NativeIncitements {

    private final Map<NativeNation, NativeIncitementOrder> orders;

    private int revision;

    private NativeIncitements(
            final Map<NativeNation, NativeIncitementOrder> orders) {
        this.orders = orders;
    }

    /**
     * Restore saved orders. An absent list means no tribe has an order; each
     * entry must name a tribe once, with a known European and 1 to 20 turns.
     *
     * @param saved saved entries, nullable meaning none
     * @return validated orders
     */
    static NativeIncitements make(final List<NativeIncitementPo> saved) {
        final Map<NativeNation, NativeIncitementOrder> orders = new EnumMap<>(
                NativeNation.class);
        for (final NativeIncitementPo entry : saved == null
                ? List.<NativeIncitementPo>of()
                : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getNation() != null
                            && entry.getTarget() != null
                            && entry.getRemainingTurns() != null,
                    "Unsupported save: invalid native incitement");
            Preconditions.checkArgument(
                    orders.put(entry.getNation(),
                            new NativeIncitementOrder(entry.getTarget(),
                                    entry.getRemainingTurns())) == null,
                    "Unsupported save: duplicate native incitement");
        }
        return new NativeIncitements(orders);
    }

    /**
     * @return saved entries, in nation order; a tribe without an order has none
     */
    List<NativeIncitementPo> save() {
        final List<NativeIncitementPo> out = new ArrayList<>();
        for (final NativeNation nation : NativeNation.values()) {
            final NativeIncitementOrder order = orders.get(nation);
            if (order != null) {
                final NativeIncitementPo po = new NativeIncitementPo();
                po.setNation(nation);
                po.setTarget(order.getTarget());
                po.setRemainingTurns(order.getRemainingTurns());
                out.add(po);
            }
        }
        return out;
    }

    /**
     * @param nation required native tribe
     * @return the tribe's order, empty when it has none
     */
    Optional<NativeIncitementOrder> find(final NativeNation nation) {
        return Optional.ofNullable(orders.get(nation));
    }

    /**
     * @return the session revision of every order change
     */
    int revision() {
        return revision;
    }

    /**
     * Replace the tribe's order with a fresh one of the full lifetime. A
     * successful incitement always resets the clock to twenty turns.
     *
     * @param nation required native tribe
     * @param target required European power
     */
    void install(final NativeNation nation, final EuropeanNation target) {
        Preconditions.checkNotNull(nation);
        orders.put(nation, new NativeIncitementOrder(target,
                NativeIncitementRules.ORDER_TURNS));
        revision++;
    }

    /**
     * Remove the tribe's order, if any.
     *
     * @param nation required native tribe
     */
    void clear(final NativeNation nation) {
        if (orders.remove(nation) != null) {
            revision++;
        }
    }

    /**
     * One native action turn is over: the order loses one turn and is removed
     * after its last one, so exactly twenty native turns act on it.
     *
     * @param nation required native tribe
     */
    void endTurn(final NativeNation nation) {
        final NativeIncitementOrder order = orders.get(nation);
        if (order == null) {
            return;
        }
        if (order.getRemainingTurns() == 1) {
            orders.remove(nation);
        } else {
            orders.put(nation, order.afterTurn());
        }
        revision++;
    }

}
