package org.microcol.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;

/**
 * Base score of one colonial nation before the war and difficulty terms
 * (R-0195). Every component is named and immutable; the subtotal is their exact
 * sum. Reading the score never changes the nation, its units or its treasury.
 */
public final class BaseScore {

    /** Points for a criminal or an indentured servant. */
    public static final int SERVANT_POINTS = 1;

    /** Points for a free colonist and for a native convert. */
    public static final int COLONIST_POINTS = 2;

    /** Points for a person with a civilian specialty. */
    public static final int EXPERT_POINTS = 4;

    /** Points for each distinct recruited founding father. */
    public static final int FATHER_POINTS = 5;

    /** Gold for each point of the treasury component. */
    public static final int GOLD_PER_POINT = 1000;

    private final int servants;
    private final int colonists;
    private final int experts;
    private final int fathers;
    private final long gold;
    private final int supportPercent;
    private final long populationPoints;
    private final long fatherPoints;
    private final long goldPoints;
    private final long supportPoints;
    private final long subtotal;

    private BaseScore(final int servants, final int colonists,
            final int experts, final int fathers, final long gold,
            final int supportPercent) {
        this.servants = servants;
        this.colonists = colonists;
        this.experts = experts;
        this.fathers = fathers;
        this.gold = gold;
        this.supportPercent = supportPercent;
        populationPoints = Math.addExact(
                Math.addExact(
                        Math.multiplyExact(servants, (long) SERVANT_POINTS),
                        Math.multiplyExact(colonists, (long) COLONIST_POINTS)),
                Math.multiplyExact(experts, (long) EXPERT_POINTS));
        fatherPoints = Math.multiplyExact(fathers, (long) FATHER_POINTS);
        goldPoints = gold / GOLD_PER_POINT;
        supportPoints = supportPercent;
        subtotal = Math.addExact(
                Math.addExact(Math.addExact(populationPoints, fatherPoints),
                        goldPoints),
                supportPoints);
    }

    /**
     * Build the score from already counted components. Used by the nation
     * calculation and by tests of the weights.
     *
     * @param servants       criminals and servants, 1 point each
     * @param colonists      free colonists and native converts, 2 points each
     * @param experts        persons with a civilian specialty, 4 points each
     * @param fathers        distinct recruited founding fathers
     * @param gold           treasury gold, nonnegative
     * @param supportPercent displayed whole national support from 0 to 100
     * @return immutable base score
     */
    static BaseScore of(final int servants, final int colonists,
            final int experts, final int fathers, final long gold,
            final int supportPercent) {
        Preconditions.checkArgument(
                servants >= 0 && colonists >= 0 && experts >= 0 && fathers >= 0,
                "Negative person or father count");
        Preconditions.checkArgument(gold >= 0, "Negative gold");
        Preconditions.checkArgument(
                supportPercent >= 0 && supportPercent <= 100,
                "Support percentage outside 0..100");
        return new BaseScore(servants, colonists, experts, fathers, gold,
                supportPercent);
    }

    /**
     * Calculate the base score of a colonial nation from its live state. Each
     * owned living person is counted once by its unit ID, wherever it stands
     * (colony, map, cargo or Europe). Crown-seized units are not the nation's
     * people. Military status is not a separate person.
     *
     * @param owner required colonial nation
     * @return immutable base score of the current state
     */
    static BaseScore calculate(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkArgument(owner.isColonialNation(),
                "Base score belongs to a colonial nation");
        final Map<Integer, UnitFreeColonist> people = new LinkedHashMap<>();
        for (final Unit unit : owner.getAllUnits()) {
            if (unit instanceof UnitFreeColonist && !unit.isCrownSeized()) {
                people.putIfAbsent(unit.getId(), (UnitFreeColonist) unit);
            }
        }
        int servants = 0;
        int colonists = 0;
        int experts = 0;
        for (final UnitFreeColonist person : people.values()) {
            final int points = pointsOf(person);
            if (points == SERVANT_POINTS) {
                servants++;
            } else if (points == COLONIST_POINTS) {
                colonists++;
            } else {
                experts++;
            }
        }
        final int fathers = new HashSet<>(owner.getCongressMembers()).size();
        return new BaseScore(servants, colonists, experts, fathers,
                owner.getGold(),
                owner.getNationalRebelSupport().getDisplayPercent());
    }

    /**
     * The civilian scoring identity of one person. A native convert counts as a
     * colonist. Military veteran status and the Continental or royal role keep
     * the civilian identity: they neither add a person nor remove expertise.
     *
     * @param person required living person
     * @return points of the person, one of the weight constants
     */
    static int pointsOf(final UnitFreeColonist person) {
        if (person.isNativeConvert()) {
            return COLONIST_POINTS;
        }
        final UnitType type = person.getType();
        if (type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL) {
            return SERVANT_POINTS;
        }
        return person.hasCivilianSpecialty() ? EXPERT_POINTS : COLONIST_POINTS;
    }

    /** @return criminals and servants counted in the population */
    public int getServants() {
        return servants;
    }

    /** @return free colonists and native converts counted in the population */
    public int getColonists() {
        return colonists;
    }

    /** @return persons with a civilian specialty counted in the population */
    public int getExperts() {
        return experts;
    }

    /** @return distinct recruited founding fathers */
    public int getFathers() {
        return fathers;
    }

    /** @return treasury gold of the nation */
    public long getGold() {
        return gold;
    }

    /** @return displayed whole national support percentage */
    public int getSupportPercent() {
        return supportPercent;
    }

    /** @return points of all living people, by civilian identity */
    public long getPopulationPoints() {
        return populationPoints;
    }

    /** @return points of the distinct recruited fathers */
    public long getFatherPoints() {
        return fatherPoints;
    }

    /** @return points of the treasury, floor of gold per 1000 */
    public long getGoldPoints() {
        return goldPoints;
    }

    /** @return points of the displayed national support */
    public long getSupportPoints() {
        return supportPoints;
    }

    /** @return exact sum of the four components, the base score */
    public long getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return "BaseScore[population=" + populationPoints + ", fathers="
                + fatherPoints + ", gold=" + goldPoints + ", support="
                + supportPoints + ", subtotal=" + subtotal + "]";
    }
}
