package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * Actual result of one chief visit (R-0158), reported once to the player. A
 * repeat visit reports {@link NativeChiefOutcome#INFORMATION} again, with no
 * gold and no death. The information holds only this settlement's own goods and
 * its teachable skill; the land within two squares is revealed on the map.
 */
public final class NativeChiefResult {

    private final NativeChiefOutcome outcome;

    private final boolean repeat;

    private final int gold;

    private final Optional<Information> information;

    private NativeChiefResult(final NativeChiefOutcome outcome,
            final boolean repeat, final int gold,
            final Optional<Information> information) {
        this.outcome = outcome;
        this.repeat = repeat;
        this.gold = gold;
        this.information = information;
    }

    /**
     * @param outcome     drawn outcome of the first visit of this nation
     * @param gold        gold moved from the settlement to the visitor's nation
     * @param information disclosed information, present for information only
     * @return result of a first visit
     */
    static NativeChiefResult first(final NativeChiefOutcome outcome,
            final int gold, final Optional<Information> information) {
        return new NativeChiefResult(outcome, false, gold, information);
    }

    /**
     * @param information disclosed information, repeated without a draw
     * @return result of a repeat visit
     */
    static NativeChiefResult repeated(final Information information) {
        return new NativeChiefResult(NativeChiefOutcome.INFORMATION, true, 0,
                Optional.of(information));
    }

    /**
     * @return outcome that applies to this visit; information on a repeat
     */
    public NativeChiefOutcome getOutcome() {
        return outcome;
    }

    /**
     * @return whether the nation already met the chief here, so nothing was
     *         drawn
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * @return gold moved to the visitor's nation, zero unless it was gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * @return the information disclosed by this visit, empty unless informed
     */
    public Optional<Information> getInformation() {
        return information;
    }

    /**
     * What the chief tells about one native settlement.
     */
    public static final class Information {

        private final List<GoodsType> offeredGoods;

        private final List<GoodsType> wantedGoods;

        private final UnitType teachableSkill;

        Information(final List<GoodsType> offeredGoods,
                final List<GoodsType> wantedGoods,
                final UnitType teachableSkill) {
            this.offeredGoods = ImmutableList.copyOf(offeredGoods);
            this.wantedGoods = ImmutableList.copyOf(wantedGoods);
            this.teachableSkill = teachableSkill;
        }

        /**
         * @return goods the settlement offers for sale, in offer order
         */
        public List<GoodsType> getOfferedGoods() {
            return offeredGoods;
        }

        /**
         * @return goods the settlement wants to buy, in demand order
         */
        public List<GoodsType> getWantedGoods() {
            return wantedGoods;
        }

        /**
         * @return skill the settlement offers to teach
         */
        public UnitType getTeachableSkill() {
            return teachableSkill;
        }

    }

}
