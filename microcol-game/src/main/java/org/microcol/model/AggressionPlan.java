package org.microcol.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Diplomatic consequences of one actual attack or capture, computed from the
 * live relations before any combat, movement or action-point mutation. It is
 * applied once by {@link Model#applyAggression(AggressionPlan)}.
 */
final class AggressionPlan {
    /** One planned transition of a pair to war. */
    static final class Change {
        final Player first;
        final Player second;
        final EuropeanRelationState expected;

        private Change(final Player first, final Player second,
                final EuropeanRelationState expected) {
            this.first = first;
            this.second = second;
            this.expected = expected;
        }
    }

    /** A direct ally obliged to defend the victim against the attacker. */
    static final class Defense {
        final Player ally;
        final Player aggressor;
        final Player victim;

        private Defense(final Player ally, final Player aggressor,
                final Player victim) {
            this.ally = ally;
            this.aggressor = aggressor;
            this.victim = victim;
        }
    }

    private final Map<Set<String>, Change> changes = new LinkedHashMap<>();
    private final List<Player[]> aggressions = new ArrayList<>();
    private final List<Defense> defenses = new ArrayList<>();
    private boolean applied;

    void addChange(final Player first, final Player second,
            final EuropeanRelationState expected) {
        changes.putIfAbsent(Set.of(first.getName(), second.getName()),
                new Change(first, second, expected));
    }

    boolean hasChange(final Player first, final Player second) {
        return changes.containsKey(Set.of(first.getName(), second.getName()));
    }

    void addAggression(final Player attacker, final Player victim) {
        aggressions.add(new Player[] { attacker, victim });
    }

    void addDefense(final Player ally, final Player aggressor,
            final Player victim) {
        defenses.add(new Defense(ally, aggressor, victim));
    }

    List<Change> getChanges() {
        return List.copyOf(changes.values());
    }

    List<Player[]> getAggressions() {
        return List.copyOf(aggressions);
    }

    List<Defense> getDefenses() {
        return List.copyOf(defenses);
    }

    boolean isEmpty() {
        return changes.isEmpty() && aggressions.isEmpty();
    }

    void markApplied() {
        applied = true;
    }

    boolean isApplied() {
        return applied;
    }
}
