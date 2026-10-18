package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Immutable snapshot of one commodity's back taxes: the exact frozen amount and
 * the demand generation it was read in. The model confirms or rejects a
 * snapshot against the live state, so a stale view never charges anything.
 */
public final class TaxArrearsQuote {

    private final Player player;

    private final GoodsType goods;

    private final int amount;

    private final int generation;

    TaxArrearsQuote(final Player player, final GoodsType goods,
            final int amount, final int generation) {
        this.player = Preconditions.checkNotNull(player);
        this.goods = Preconditions.checkNotNull(goods);
        this.amount = amount;
        this.generation = generation;
    }

    /** @return the colonial nation that owes the arrears */
    public Player getPlayer() {
        return player;
    }

    /** @return the boycotted commodity whose arrears would be paid */
    public GoodsType getGoods() {
        return goods;
    }

    /** @return the gold to pay, frozen when the protest was answered */
    public int getAmount() {
        return amount;
    }

    /**
     * @return the current demand generation when the quote was read; a later
     *         generation makes this snapshot stale, even for an identical debt
     */
    int getGeneration() {
        return generation;
    }

    @Override
    public String toString() {
        return "TaxArrearsQuote[" + player.getName() + " " + goods + " "
                + amount + " gold #" + generation + "]";
    }
}
