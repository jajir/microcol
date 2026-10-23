package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * What one player knows about the native nations (R-0201): each nation reached
 * by a land contact or by a learned settlement, and each learned settlement
 * with its relation view and the chief information the player has learned
 * there. Only the model builds it; undiscovered settlements, their stock and
 * skills, and the discoveries of other players never appear in it.
 */
public final class NativeAdvisorReport {

    private final List<NativeNation> knownNations;

    private final List<Settlement> settlements;

    private NativeAdvisorReport(final List<NativeNation> knownNations,
            final List<Settlement> settlements) {
        this.knownNations = knownNations;
        this.settlements = settlements;
    }

    /**
     * @param knownNations required nations, in enum order
     * @param settlements  required learned settlements, in saved order
     * @return the report
     */
    static NativeAdvisorReport of(final List<NativeNation> knownNations,
            final List<Settlement> settlements) {
        Preconditions.checkNotNull(knownNations);
        Preconditions.checkNotNull(settlements);
        return new NativeAdvisorReport(ImmutableList.copyOf(knownNations),
                ImmutableList.copyOf(settlements));
    }

    /**
     * @return nations the player knows, by a land contact or a learned
     *         settlement, in enum order; empty for a king or a scripted actor
     */
    public List<NativeNation> getKnownNations() {
        return knownNations;
    }

    /**
     * @return learned settlements of the player, in saved order
     */
    public List<Settlement> getSettlements() {
        return settlements;
    }

    /**
     * One learned settlement as the player may read it.
     */
    public static final class Settlement {

        private final NativeSettlement settlement;

        private final NativeRelationView relation;

        private final Optional<NativeChiefResult.Information> chiefFacts;

        private Settlement(final NativeSettlement settlement,
                final NativeRelationView relation,
                final Optional<NativeChiefResult.Information> chiefFacts) {
            this.settlement = settlement;
            this.relation = relation;
            this.chiefFacts = chiefFacts;
        }

        /**
         * @param settlement required learned settlement
         * @param relation   required relation view of that settlement
         * @param chiefFacts required chief information, empty before the chief
         *                   has told it
         * @return the settlement row
         */
        static Settlement of(final NativeSettlement settlement,
                final NativeRelationView relation,
                final Optional<NativeChiefResult.Information> chiefFacts) {
            Preconditions.checkNotNull(settlement);
            Preconditions.checkNotNull(relation);
            Preconditions.checkNotNull(chiefFacts);
            return new Settlement(settlement, relation, chiefFacts);
        }

        /**
         * @return the learned settlement, its nation, class and capital status
         */
        public NativeSettlement getSettlement() {
            return settlement;
        }

        /**
         * @return attitude and alarm change toward the player's own European
         */
        public NativeRelationView getRelation() {
            return relation;
        }

        /**
         * @return goods for sale, goods wanted and teachable skill that the
         *         chief has told the player; empty when the chief has not
         */
        public Optional<NativeChiefResult.Information> getChiefFacts() {
            return chiefFacts;
        }

    }

}
