package org.microcol.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.model.store.EuropeanRelationPo;

import com.google.common.base.Preconditions;

/** One saved state per unordered pair of registered colonizing players. */
final class EuropeanRelations {
    private final List<Player> registry;
    private final Map<Set<String>, EuropeanRelationState> relations = new LinkedHashMap<>();

    EuropeanRelations(final List<Player> players,
            final List<EuropeanRelationPo> saved) {
        Preconditions.checkArgument(players != null && saved != null,
                "Missing European relation data");
        registry = List.copyOf(players);
        final long colonizers = registry.stream()
                .filter(player -> !player.isKing()).count();
        for (final EuropeanRelationPo row : saved) {
            Preconditions.checkArgument(
                    row != null && row.getFirstPlayer() != null
                            && row.getSecondPlayer() != null
                            && row.getState() != null,
                    "Invalid European relation row");
            final Player first = registry.stream().filter(
                    player -> player.getName().equals(row.getFirstPlayer()))
                    .findFirst().orElse(null);
            final Player second = registry.stream().filter(
                    player -> player.getName().equals(row.getSecondPlayer()))
                    .findFirst().orElse(null);
            final Set<String> key = key(first, second);
            Preconditions.checkArgument(!relations.containsKey(key),
                    "Duplicate European relation pair");
            relations.put(key, row.getState());
        }
        Preconditions.checkArgument(
                relations.size() == colonizers * (colonizers - 1) / 2,
                "Incomplete European relation pairs");
    }

    private Set<String> key(final Player first, final Player second) {
        Preconditions
                .checkArgument(
                        first != null && second != null && first != second
                                && !first.isKing() && !second.isKing()
                                && registry.stream()
                                        .anyMatch(player -> player == first)
                                && registry.stream()
                                        .anyMatch(player -> player == second),
                        "Invalid European relation participants");
        return Set.of(first.getName(), second.getName());
    }

    EuropeanRelationState get(final Player first, final Player second) {
        return relations.get(key(first, second));
    }

    void transition(final Player first, final Player second,
            final EuropeanRelationState expected,
            final EuropeanRelationState next) {
        final Set<String> key = key(first, second);
        Preconditions.checkArgument(
                expected != null && next != null && expected != next
                        && relations.get(key) == expected,
                "Invalid or stale European relation transition");
        relations.put(key, next);
    }

    List<EuropeanRelationPo> save() {
        final List<EuropeanRelationPo> saved = new ArrayList<>();
        relations.forEach((pair, state) -> {
            final Iterator<String> names = pair.stream().sorted().iterator();
            final EuropeanRelationPo row = new EuropeanRelationPo();
            row.setFirstPlayer(names.next());
            row.setSecondPlayer(names.next());
            row.setState(state);
            saved.add(row);
        });
        return saved;
    }
}
