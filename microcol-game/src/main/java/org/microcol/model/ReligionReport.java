package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Religion report of one colonial nation (R-0199): the cross output each colony
 * gives to the national pool at the next update, the accumulated immigration
 * crosses, the next threshold and the state of the earned immigrant. Output and
 * progress are separate figures. The report is a snapshot of the live model;
 * nothing is kept between calls and nothing is settled or spent.
 */
public final class ReligionReport {

    /** Where the earned immigration of the nation stands. */
    public enum Immigration {
        /** No threshold has been reached. */
        NONE,
        /**
         * The threshold is reached; the arrival is settled at the next turn.
         */
        ARRIVES_NEXT_TURN,
        /** An earned choice is open and waits for the player in Europe. */
        CHOICE_WAITING,
        /**
         * The threshold is reached, but a declared nation receives no arrivals
         * from Europe; the crosses stay saved.
         */
        BLOCKED_EUROPE
    }

    /** Cross output of one colony at the next update. */
    public static final class ColonyOutput {

        private final int colonyId;
        private final String name;
        private final int crosses;

        private ColonyOutput(final int colonyId, final String name,
                final int crosses) {
            this.colonyId = colonyId;
            this.name = name;
            this.crosses = crosses;
        }

        /** @return stable colony ID */
        public int getColonyId() {
            return colonyId;
        }

        /** @return current name of the colony */
        public String getName() {
            return name;
        }

        /**
         * @return crosses this colony gives to the national pool at the next
         *         update, staffed and unattended output together
         */
        public int getCrosses() {
            return crosses;
        }
    }

    private final List<ColonyOutput> colonies;
    private final int nationalOutput;
    private final int accumulated;
    private final int target;
    private final int remaining;
    private final Immigration immigration;
    private final int choiceOfferCount;

    private ReligionReport(final List<ColonyOutput> colonies,
            final int nationalOutput, final int accumulated, final int target,
            final int remaining, final Immigration immigration,
            final int choiceOfferCount) {
        this.colonies = colonies;
        this.nationalOutput = nationalOutput;
        this.accumulated = accumulated;
        this.target = target;
        this.remaining = remaining;
        this.immigration = immigration;
        this.choiceOfferCount = choiceOfferCount;
    }

    /**
     * Build the report of the current state of one colonial nation. Each colony
     * reports the same cross row that its next update credits, so Penn's bonus
     * is already inside each colony and is never applied to the total again.
     *
     * @param owner required colonial nation; the caller has checked that it
     *              belongs to the model
     * @return immutable report of the current state
     */
    static ReligionReport calculate(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkArgument(owner.isColonialNation(),
                "Religion report belongs to a colonial nation");
        final List<ColonyOutput> outputs = new ArrayList<>();
        int nationalOutput = 0;
        for (final Colony colony : owner.getColonies()) {
            if (!colony.isValid()) {
                continue;
            }
            final int crosses = Math.max(0, colony.getRawGoodsStats()
                    .getStatsByType(GoodsType.CROSS).getRowProduction());
            outputs.add(new ColonyOutput(colony.getId(), colony.getName(),
                    crosses));
            nationalOutput = Math.addExact(nationalOutput, crosses);
        }
        outputs.sort(Comparator.comparingInt(ColonyOutput::getColonyId));
        final int accumulated = owner.getImmigrationCrosses();
        final int target = owner.getImmigrationTarget();
        return new ReligionReport(List.copyOf(outputs), nationalOutput,
                accumulated, target, Math.max(0, target - accumulated),
                immigrationOf(owner),
                owner.getEarnedImmigrationOffers().size());
    }

    /**
     * The earned state in the order the model settles it: a frozen choice comes
     * first, then the threshold, and a declared nation never receives the
     * arrival in Europe.
     */
    private static Immigration immigrationOf(final Player owner) {
        if (owner.hasFrozenEarnedChoice()) {
            return Immigration.CHOICE_WAITING;
        }
        if (!owner.hasPendingImmigrant()) {
            return Immigration.NONE;
        }
        return owner.canTradeInEurope() ? Immigration.ARRIVES_NEXT_TURN
                : Immigration.BLOCKED_EUROPE;
    }

    /** @return colonies of the nation with their output, ordered by ID */
    public List<ColonyOutput> getColonies() {
        return colonies;
    }

    /**
     * @return crosses the nation gives to the national pool at the next update
     */
    public int getNationalOutput() {
        return nationalOutput;
    }

    /** @return crosses already accumulated toward the next immigrant */
    public int getAccumulated() {
        return accumulated;
    }

    /** @return crosses needed for the next immigrant, from the shared model */
    public int getTarget() {
        return target;
    }

    /**
     * @return crosses still missing to the next threshold; zero once the
     *         threshold is reached
     */
    public int getRemaining() {
        return remaining;
    }

    /** @return state of the earned immigration */
    public Immigration getImmigration() {
        return immigration;
    }

    /**
     * @return number of candidates waiting in an open earned choice, zero when
     *         no choice is open
     */
    public int getChoiceOfferCount() {
        return choiceOfferCount;
    }
}
