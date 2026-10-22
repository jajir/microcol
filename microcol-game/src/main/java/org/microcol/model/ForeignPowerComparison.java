package org.microcol.model;

import java.util.Optional;

/**
 * One foreign colonial power as the viewer may read it in the foreign report.
 * The relation is always shown; actual aggregates exist only when the viewer
 * recruited Jan de Witt and a colonist of the viewer currently contacts the
 * power. Locations and unit lists are never part of this snapshot.
 */
public final class ForeignPowerComparison {

    /** Why the comparison figures are shown or withheld. */
    public enum Disclosure {
        /** Jan de Witt is not in the viewer's Congress. */
        LOCKED,
        /**
         * Jan de Witt is recruited, but no current contact reveals the power.
         */
        UNCONTACTED,
        /** Jan de Witt is recruited and the power is contacted. */
        REVEALED
    }

    /**
     * Actual aggregates of a revealed power at the time of the snapshot.
     */
    public static final class Figures {

        private final int population;

        private final int colonyCount;

        /** Population divided by colony count; zero when it has no colony. */
        private final double averageColonySize;

        /** Sum of base strengths of living armed land units. */
        private final double militaryStrength;

        /** Sum of base strengths of armed ships. */
        private final double navalStrength;

        /** Net European-sale revenue of the last ten owner turns. */
        private final int tradeRevenue;

        Figures(final int population, final int colonyCount,
                final double averageColonySize, final double militaryStrength,
                final double navalStrength, final int tradeRevenue) {
            this.population = population;
            this.colonyCount = colonyCount;
            this.averageColonySize = averageColonySize;
            this.militaryStrength = militaryStrength;
            this.navalStrength = navalStrength;
            this.tradeRevenue = tradeRevenue;
        }

        /** @return summed population of the power's colonies */
        public int getPopulation() {
            return population;
        }

        /** @return number of colonies the power owns */
        public int getColonyCount() {
            return colonyCount;
        }

        /** @return average population per colony, zero without colonies */
        public double getAverageColonySize() {
            return averageColonySize;
        }

        /** @return military strength of living armed land units */
        public double getMilitaryStrength() {
            return militaryStrength;
        }

        /** @return naval strength of armed ships */
        public double getNavalStrength() {
            return navalStrength;
        }

        /** @return net European-sale revenue over the last ten owner turns */
        public int getTradeRevenue() {
            return tradeRevenue;
        }

    }

    private final String name;

    private final String displayName;

    private final EuropeanRelationState relation;

    private final Disclosure disclosure;

    private final Figures figures;

    private ForeignPowerComparison(final String name, final String displayName,
            final EuropeanRelationState relation, final Disclosure disclosure,
            final Figures figures) {
        this.name = name;
        this.displayName = displayName;
        this.relation = relation;
        this.disclosure = disclosure;
        this.figures = figures;
    }

    static ForeignPowerComparison locked(final Player power,
            final EuropeanRelationState relation) {
        return new ForeignPowerComparison(power.getName(),
                power.getDisplayName(), relation, Disclosure.LOCKED, null);
    }

    static ForeignPowerComparison uncontacted(final Player power,
            final EuropeanRelationState relation) {
        return new ForeignPowerComparison(power.getName(),
                power.getDisplayName(), relation, Disclosure.UNCONTACTED, null);
    }

    static ForeignPowerComparison revealed(final Player power,
            final EuropeanRelationState relation, final Figures figures) {
        return new ForeignPowerComparison(power.getName(),
                power.getDisplayName(), relation, Disclosure.REVEALED, figures);
    }

    /** @return internal participant key, not an editable display name */
    public String getName() {
        return name;
    }

    /** @return the current visible name of the power */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the current war, peace or alliance state with the viewer */
    public EuropeanRelationState getRelation() {
        return relation;
    }

    /** @return why the figures are shown or withheld */
    public Disclosure getDisclosure() {
        return disclosure;
    }

    /** @return the aggregates, present only when revealed */
    public Optional<Figures> getFigures() {
        return Optional.ofNullable(figures);
    }

}
