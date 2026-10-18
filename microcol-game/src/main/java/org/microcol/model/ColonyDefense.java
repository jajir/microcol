package org.microcol.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Frozen defense of one colony against one attacker, as quoted for an
 * {@link AttackOrder}. The battlefield is the colony center even when the
 * selected defender works in a field or building. The quote is read-only: only
 * a confirmed battle changes the model, and it first compares the live colony
 * with this quote.
 */
public final class ColonyDefense {

    /** Which tier of the colony defends. */
    public enum Kind {
        /**
         * Armed units defend first: outside garrison, artillery or armed
         * residents.
         */
        ARMED,
        /** No armed unit exists; the inhabitants defend once as a group. */
        CIVILIANS,
        /**
         * Paul Revere arms one worker from stored muskets because no armed
         * defender exists.
         */
        REVERE,
        /**
         * One worker of a field is attacked on its own field tile; only that
         * worker can be lost.
         */
        FIELD
    }

    private final Colony colony;
    private final Location battlefield;
    private final Kind kind;
    private final Unit defender;
    private final List<Unit> roster;
    private final Map<Unit, Location> escapes;
    private final int revereMuskets;
    private final boolean revereExits;
    private final String inputs;

    ColonyDefense(final Colony colony, final Location battlefield,
            final Kind kind, final Unit defender, final List<Unit> roster,
            final Map<Unit, Location> escapes, final int revereMuskets,
            final boolean revereExits, final String inputs) {
        this.colony = Preconditions.checkNotNull(colony);
        this.battlefield = Preconditions.checkNotNull(battlefield);
        this.kind = Preconditions.checkNotNull(kind);
        this.defender = Preconditions.checkNotNull(defender);
        this.roster = ImmutableList.copyOf(roster);
        this.escapes = new LinkedHashMap<>(escapes);
        this.revereMuskets = revereMuskets;
        this.revereExits = revereExits;
        this.inputs = Preconditions.checkNotNull(inputs);
    }

    public Colony getColony() {
        return colony;
    }

    /**
     * @return the explicit battlefield: the colony center, or the field tile of
     *         an independently attacked worker
     */
    public Location getBattlefield() {
        return battlefield;
    }

    public Kind getKind() {
        return kind;
    }

    /** @return the selected representative of the defending tier */
    public Unit getDefender() {
        return defender;
    }

    /**
     * @return the complete defending tier in selection order: every armed
     *         defender, or the civilian group
     */
    public List<Unit> getRoster() {
        return roster;
    }

    /**
     * @return whether defeating this tier transfers the whole colony; false
     *         while armed defenders remain
     */
    public boolean capturesColony() {
        return kind == Kind.CIVILIANS || kind == Kind.REVERE;
    }

    /**
     * @return the stored muskets a confirmed battle consumes; zero unless Paul
     *         Revere arms a worker
     */
    public int getRevereMuskets() {
        return revereMuskets;
    }

    /**
     * @return whether a victorious emergency militiaman joins the outside
     *         garrison instead of staying in the job
     */
    public boolean isRevereExit() {
        return revereExits;
    }

    Map<Unit, Location> getEscapes() {
        return escapes;
    }

    String getInputs() {
        return inputs;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ColonyDefense)) {
            return false;
        }
        final ColonyDefense that = (ColonyDefense) other;
        return colony == that.colony && battlefield.equals(that.battlefield)
                && kind == that.kind && defender == that.defender
                && roster.equals(that.roster) && escapes.equals(that.escapes)
                && revereMuskets == that.revereMuskets
                && revereExits == that.revereExits
                && inputs.equals(that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colony.getId(), kind, defender.getId(), inputs);
    }
}
