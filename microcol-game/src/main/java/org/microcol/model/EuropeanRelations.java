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
    /** Complete rounds after an actual attack or confirmed tribute. */
    static final int ALLIANCE_COOLDOWN_ROUNDS = 10;
    /** Complete rounds after ending an alliance before war may be declared. */
    static final int WAR_WAIT_ROUNDS = 5;

    private final List<Player> registry;
    private final Map<Set<String>, EuropeanRelationState> relations = new LinkedHashMap<>();
    private final Map<Set<String>, PeaceOffer> pendingOffers = new LinkedHashMap<>();
    private final Map<Set<String>, TributeDemand> pendingTribute = new LinkedHashMap<>();
    private final Map<Set<String>, Integer> lastTributeRounds = new LinkedHashMap<>();
    private final Map<Set<String>, AllianceOffer> pendingAlliances = new LinkedHashMap<>();
    private final Map<Set<String>, Integer> lastAggressionRounds = new LinkedHashMap<>();
    private final Map<Set<String>, Integer> allianceEndedRounds = new LinkedHashMap<>();
    private final Map<Set<String>, Set<Location>> retainedOverlaps = new LinkedHashMap<>();

    EuropeanRelations(final List<Player> players,
            final List<EuropeanRelationPo> saved) {
        this(players, saved, 0);
    }

    EuropeanRelations(final List<Player> players,
            final List<EuropeanRelationPo> saved, final int currentRound) {
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
            if (row.getPeaceOffer() != null) {
                Preconditions.checkArgument(
                        row.getState() == EuropeanRelationState.WAR,
                        "Peace offer requires war");
                pendingOffers.put(key,
                        PeaceOffer.load(row.getPeaceOffer(), first, second));
            }
            if (row.getAllianceOffer() != null) {
                Preconditions.checkArgument(
                        row.getState() == EuropeanRelationState.PEACE,
                        "Alliance offer requires peace");
                pendingAlliances.put(key, AllianceOffer
                        .load(row.getAllianceOffer(), first, second));
            }
            Preconditions.checkArgument(row.getLastAggressionRound() == null
                    || row.getLastAggressionRound() >= 0
                            && row.getLastAggressionRound() <= currentRound,
                    "Invalid last aggression round");
            if (row.getLastAggressionRound() != null) {
                lastAggressionRounds.put(key, row.getLastAggressionRound());
            }
            Preconditions.checkArgument(row.getAllianceEndedRound() == null
                    || row.getAllianceEndedRound() >= 0
                            && row.getAllianceEndedRound() <= currentRound
                            && row.getState() == EuropeanRelationState.PEACE,
                    "Invalid alliance termination round");
            if (row.getAllianceEndedRound() != null) {
                allianceEndedRounds.put(key, row.getAllianceEndedRound());
            }
            if (row.getRetainedOverlaps() != null) {
                Preconditions.checkArgument(
                        row.getState() != EuropeanRelationState.ALLIANCE
                                && !row.getRetainedOverlaps().isEmpty()
                                && !row.getRetainedOverlaps().contains(null)
                                && new java.util.HashSet<>(
                                        row.getRetainedOverlaps()).size() == row
                                                .getRetainedOverlaps().size(),
                        "Invalid retained overlap evidence");
                retainedOverlaps.put(key, new java.util.LinkedHashSet<>(
                        row.getRetainedOverlaps()));
            }
            Preconditions.checkArgument(row.getLastTributeRound() == null
                    || row.getLastTributeRound() >= 0
                            && row.getLastTributeRound() <= currentRound,
                    "Invalid last tribute round");
            if (row.getLastTributeRound() != null) {
                lastTributeRounds.put(key, row.getLastTributeRound());
            }
            if (row.getTributeDemand() != null) {
                final TributeDemand demand = TributeDemand.load(
                        row.getTributeDemand(), first, second, currentRound);
                Preconditions.checkArgument(
                        row.getLastTributeRound() == null
                                || (long) demand.getQuotedRound() >= 10L
                                        + row.getLastTributeRound(),
                        "Tribute demand violates cooldown history");
                pendingTribute.put(key, demand);
            }
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
        // Any change invalidates a quote made for the previous relation.
        pendingAlliances.remove(key);
        if (next == EuropeanRelationState.ALLIANCE) {
            // The alliance itself now covers any shared square.
            retainedOverlaps.remove(key);
        }
        if (next == EuropeanRelationState.PEACE) {
            pendingOffers.remove(key);
        }
        if (next != EuropeanRelationState.PEACE) {
            allianceEndedRounds.remove(key);
        }
    }

    java.util.Optional<PeaceOffer> getPending(final Player actor,
            final Player rival) {
        final PeaceOffer offer = pendingOffers.get(key(actor, rival));
        return offer != null && offer.getActor() == actor
                ? java.util.Optional.of(offer)
                : java.util.Optional.empty();
    }

    boolean hasPendingPeace(final Player first, final Player second) {
        return pendingOffers.containsKey(key(first, second));
    }

    void putPending(final PeaceOffer offer) {
        final Set<String> pair = key(offer.getActor(), offer.getRival());
        Preconditions.checkState(
                relations.get(pair) == EuropeanRelationState.WAR
                        && !pendingOffers.containsKey(pair),
                "Peace offer already exists or there is no war");
        pendingOffers.put(pair, offer);
    }

    void clearPending(final PeaceOffer offer) {
        final Set<String> pair = key(offer.getActor(), offer.getRival());
        Preconditions.checkState(pendingOffers.get(pair) == offer,
                "Peace offer is obsolete");
        pendingOffers.remove(pair);
    }

    java.util.Optional<AllianceOffer> getPendingAlliance(final Player actor,
            final Player rival) {
        final AllianceOffer offer = pendingAlliances.get(key(actor, rival));
        return offer != null && offer.getActor() == actor
                ? java.util.Optional.of(offer)
                : java.util.Optional.empty();
    }

    void putPendingAlliance(final AllianceOffer offer) {
        final Set<String> pair = key(offer.getActor(), offer.getRival());
        Preconditions.checkState(
                relations.get(pair) == EuropeanRelationState.PEACE
                        && !pendingAlliances.containsKey(pair),
                "Alliance offer already exists or there is no peace");
        pendingAlliances.put(pair, offer);
    }

    void clearPendingAlliance(final AllianceOffer offer) {
        final Set<String> pair = key(offer.getActor(), offer.getRival());
        Preconditions.checkState(pendingAlliances.get(pair) == offer,
                "Alliance offer is obsolete");
        pendingAlliances.remove(pair);
    }

    /** Explicit termination: alliance becomes peace and the wait starts. */
    void terminateAlliance(final Player first, final Player second,
            final int currentRound) {
        final Set<String> key = key(first, second);
        Preconditions.checkArgument(
                relations.get(key) == EuropeanRelationState.ALLIANCE,
                "There is no alliance to end");
        relations.put(key, EuropeanRelationState.PEACE);
        pendingAlliances.remove(key);
        allianceEndedRounds.put(key, currentRound);
    }

    /** Remaining rounds before war may be declared after a termination. */
    int warWaitRemaining(final Player first, final Player second,
            final int currentRound) {
        final Set<String> key = key(first, second);
        final Integer ended = allianceEndedRounds.get(key);
        return ended == null
                || relations.get(key) != EuropeanRelationState.PEACE ? 0
                        : (int) Math.max(0L,
                                (long) WAR_WAIT_ROUNDS + ended - currentRound);
    }

    void recordAggression(final Player first, final Player second,
            final int currentRound) {
        lastAggressionRounds.put(key(first, second), currentRound);
    }

    int aggressionTurnsRemaining(final Player first, final Player second,
            final int currentRound) {
        final Integer last = lastAggressionRounds.get(key(first, second));
        return last == null ? 0
                : (int) Math.max(0L,
                        (long) ALLIANCE_COOLDOWN_ROUNDS + last - currentRound);
    }

    java.util.Optional<TributeDemand> getPendingTribute(final Player actor,
            final Player rival) {
        final TributeDemand demand = pendingTribute.get(key(actor, rival));
        return demand != null && demand.getActor() == actor
                ? java.util.Optional.of(demand)
                : java.util.Optional.empty();
    }

    void putPendingTribute(final TributeDemand demand) {
        final Set<String> pair = key(demand.getActor(), demand.getRival());
        Preconditions.checkState(!pendingTribute.containsKey(pair),
                "Tribute demand already pending");
        pendingTribute.put(pair, demand);
    }

    void clearPendingTribute(final TributeDemand demand) {
        final Set<String> pair = key(demand.getActor(), demand.getRival());
        Preconditions.checkState(pendingTribute.get(pair) == demand,
                "Tribute demand is obsolete");
        pendingTribute.remove(pair);
    }

    int tributeTurnsRemaining(final Player actor, final Player rival,
            final int currentRound) {
        final Integer last = lastTributeRounds.get(key(actor, rival));
        return last == null ? 0 : (int) Math.max(0L, 10L + last - currentRound);
    }

    void confirmTribute(final TributeDemand demand, final int currentRound) {
        final Set<String> pair = key(demand.getActor(), demand.getRival());
        Preconditions.checkState(pendingTribute.get(pair) == demand,
                "Tribute demand is obsolete");
        pendingTribute.remove(pair);
        lastTributeRounds.put(pair, currentRound);
    }

    /**
     * Remember the tiles where units of a pair legally keep sharing a square
     * after their alliance ended. The evidence grants no entry; it only lets a
     * saved game contain the overlap until one side departs or is lost.
     */
    void retainOverlaps(final Player first, final Player second,
            final java.util.Collection<Location> locations) {
        final Set<String> key = key(first, second);
        if (!locations.isEmpty()) {
            retainedOverlaps
                    .computeIfAbsent(key, k -> new java.util.LinkedHashSet<>())
                    .addAll(locations);
        }
    }

    boolean isRetainedOverlap(final Player first, final Player second,
            final Location at) {
        final Set<Location> retained = retainedOverlaps.get(key(first, second));
        return retained != null && retained.contains(at);
    }

    /** Reject saved evidence that does not match a real overlap. */
    void checkRetainedOverlaps(
            final java.util.function.BiPredicate<Set<String>, Location> live) {
        retainedOverlaps.forEach((pair, locations) -> locations
                .forEach(at -> Preconditions.checkArgument(live.test(pair, at),
                        "Retained overlap without units of both owners")));
    }

    List<EuropeanRelationPo> save(
            final java.util.function.BiPredicate<Set<String>, Location> live) {
        final List<EuropeanRelationPo> saved = new ArrayList<>();
        relations.forEach((pair, state) -> {
            final Iterator<String> names = pair.stream().sorted().iterator();
            final EuropeanRelationPo row = new EuropeanRelationPo();
            row.setFirstPlayer(names.next());
            row.setSecondPlayer(names.next());
            row.setState(state);
            row.setPeaceOffer(
                    java.util.Optional.ofNullable(pendingOffers.get(pair))
                            .map(PeaceOffer::save).orElse(null));
            row.setTributeDemand(
                    java.util.Optional.ofNullable(pendingTribute.get(pair))
                            .map(TributeDemand::save).orElse(null));
            row.setLastTributeRound(lastTributeRounds.get(pair));
            row.setAllianceOffer(
                    java.util.Optional.ofNullable(pendingAlliances.get(pair))
                            .map(AllianceOffer::save).orElse(null));
            row.setLastAggressionRound(lastAggressionRounds.get(pair));
            row.setAllianceEndedRound(allianceEndedRounds.get(pair));
            // Departed, destroyed or captured units expire their evidence.
            final List<Location> kept = retainedOverlaps
                    .getOrDefault(pair, Set.of()).stream()
                    .filter(at -> live.test(pair, at))
                    .collect(java.util.stream.Collectors.toList());
            row.setRetainedOverlaps(kept.isEmpty() ? null : kept);
            saved.add(row);
        });
        return saved;
    }
}
