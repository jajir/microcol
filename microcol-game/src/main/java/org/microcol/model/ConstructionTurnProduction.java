package org.microcol.model;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds information about goods production in one building in colony in one
 * turn.
 * <p>
 * Note that all properties could be empty. Because not all building produce
 * something and not all constructions consume goods during producing.
 * </p>
 */
public final class ConstructionTurnProduction {

    public static final ConstructionTurnProduction EMPTY = new ConstructionTurnProduction(null,
            null, null);

    private final Goods consumedGoods;

    private final Goods producedGoods;

    private final Goods blockedGoods;

    ConstructionTurnProduction(final Goods consumedGoods, final Goods producedGoods,
            final Goods blockedGoods) {
        this.consumedGoods = consumedGoods;
        this.producedGoods = producedGoods;
        this.blockedGoods = blockedGoods;
        verify();
    }

    private void verify() {
        if (getBlockedGoods().isPresent()) {
            Preconditions.checkArgument(getProducedGoods().isPresent(),
                    "When blocked goods is present then produced goods have to be present");
            Preconditions.checkArgument(
                    getBlockedGoods().get().getType().equals(getProducedGoods().get().getType()),
                    "Blocked and produced goods type are not same.");
        }
        if (getConsumedGoods().isPresent()) {
            Preconditions.checkArgument(getProducedGoods().isPresent(),
                    "When consumed goods is presented then produces goods have to presented also.");
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("consumedGoods", consumedGoods)
                .add("producedGoods", producedGoods).add("blockedGoods", blockedGoods).toString();
    }

    public ConstructionTurnProduction add(final ConstructionTurnProduction prod) {
        Preconditions.checkNotNull(prod);
        return new ConstructionTurnProduction(add(consumedGoods, prod.consumedGoods),
                add(producedGoods, prod.producedGoods), add(blockedGoods, prod.blockedGoods));
    }

    private Goods add(final Goods a, final Goods b) {
        if (a == null) {
            if (b == null) {
                return null;
            } else {
                return b;
            }
        } else {
            if (b == null) {
                return a;
            } else {
                return a.add(b);
            }

        }
    }

    /**
     * @return the consumedGoods
     */
    public Optional<Goods> getConsumedGoods() {
        return Optional.ofNullable(consumedGoods);
    }

    /**
     * @return the producedGoods
     */
    public Optional<Goods> getProducedGoods() {
        return Optional.ofNullable(producedGoods);
    }

    /**
     * How much of produced goods wasn't possible to produce because of missing
     * source goods.
     *
     * @return the blockedGoods goods blocked by missing source goods
     */
    public Optional<Goods> getBlockedGoods() {
        return Optional.ofNullable(blockedGoods);
    }

}
