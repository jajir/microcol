package org.akropia.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;

import org.akropia.model.store.NativeFriendshipPo;

import com.google.common.base.Preconditions;

/**
 * The sole native relationship: national favour towards each colonial nation.
 */
public final class NativeFriendships {
    public static final int INITIAL_FAVOUR = 5;
    public static final int FRIEND_THRESHOLD = 8;
    public static final int HOSTILE_MAXIMUM = 3;
    public static final int CONVERT_PERIOD = 40;

    private final Map<NativeNation, NativeFriendshipPo> nations = new EnumMap<>(
            NativeNation.class);
    private final Set<EuropeanNation> participants;

    private NativeFriendships(final Set<EuropeanNation> participants) {
        this.participants = Set.copyOf(participants);
    }

    public static List<NativeFriendshipPo> fresh() {
        final List<NativeFriendshipPo> result = new ArrayList<>();
        for (final NativeNation nation : NativeNation.values()) {
            final NativeFriendshipPo row = new NativeFriendshipPo();
            row.setNation(nation);
            final Map<EuropeanNation, Integer> favour = new EnumMap<>(
                    EuropeanNation.class);
            for (final EuropeanNation european : EuropeanNation.values()) {
                favour.put(european, INITIAL_FAVOUR);
            }
            row.setFavour(favour);
            result.add(row);
        }
        return result;
    }

    static NativeFriendships make(final List<NativeFriendshipPo> saved,
            final Set<EuropeanNation> participants) {
        Preconditions.checkArgument(saved != null,
                "Missing native friendships");
        final NativeFriendships result = new NativeFriendships(participants);
        for (final NativeFriendshipPo row : saved) {
            Preconditions.checkArgument(
                    row != null && row.getNation() != null
                            && row.getFavour() != null,
                    "Incomplete native friendship");
            Preconditions.checkArgument(
                    row.getFavour().size() == EuropeanNation.values().length,
                    "Incomplete native favour table");
            for (final EuropeanNation european : EuropeanNation.values()) {
                final Integer value = row.getFavour().get(european);
                Preconditions.checkArgument(
                        value != null && value >= 1 && value <= 10,
                        "Native favour must be between 1 and 10");
            }
            Preconditions.checkArgument(
                    row.getElapsed() >= 0 && row.getElapsed() < CONVERT_PERIOD
                            && row.getArrival() >= 0
                            && row.getArrival() <= CONVERT_PERIOD,
                    "Invalid native conversion period");
            Preconditions.checkArgument(
                    result.nations.put(row.getNation(), copy(row)) == null,
                    "Duplicate native friendship");
        }
        Preconditions.checkArgument(
                result.nations.size() == NativeNation.values().length,
                "Incomplete native friendship table");
        for (final NativeFriendshipPo row : result.nations.values()) {
            Preconditions.checkArgument(
                    row.getFriend() == result.friend(row.getNation())
                            .orElse(null),
                    "Native friend disagrees with favour");
            Preconditions.checkArgument(
                    row.getFriend() != null
                            || row.getElapsed() == 0 && row.getArrival() == 0,
                    "Conversion period without native friend");
            Preconditions.checkArgument(
                    row.getElapsed() == 0 || row.getArrival() > 0,
                    "Conversion period has no arrival turn");
        }
        return result;
    }

    public int favour(final NativeNation nation,
            final EuropeanNation european) {
        return nations.get(nation).getFavour().get(european);
    }

    public Optional<EuropeanNation> friend(final NativeNation nation) {
        EuropeanNation best = null;
        int highest = 0;
        boolean tied = false;
        for (final EuropeanNation european : participants) {
            final int value = favour(nation, european);
            if (value > highest) {
                highest = value;
                best = european;
                tied = false;
            } else if (value == highest) {
                tied = true;
            }
        }
        return highest >= FRIEND_THRESHOLD && !tied ? Optional.of(best)
                : Optional.empty();
    }

    void sendMissionary(final NativeNation nation, final EuropeanNation sender,
            final int strength) {
        Preconditions.checkArgument(
                participants.contains(sender)
                        && (strength == 1 || strength == 2),
                "Invalid native missionary influence");
        final Map<EuropeanNation, Integer> values = nations.get(nation)
                .getFavour();
        for (final EuropeanNation european : participants) {
            values.put(european, clamp(values.get(european)
                    + (european == sender ? strength : -strength)));
        }
        refreshFriend(nation);
    }

    void attacked(final NativeNation nation, final EuropeanNation attacker) {
        Preconditions.checkArgument(participants.contains(attacker),
                "Invalid native attacker");
        nations.get(nation).getFavour().put(attacker,
                clamp(favour(nation, attacker) - 3));
        refreshFriend(nation);
    }

    /**
     * One update per native nation's turn; the random arrival is drawn once per
     * period.
     */
    Optional<EuropeanNation> advance(final NativeNation nation,
            final IntSupplier arrivalTurn) {
        refreshFriend(nation);
        final NativeFriendshipPo row = nations.get(nation);
        if (row.getFriend() == null) {
            return Optional.empty();
        }
        if (row.getArrival() == 0) {
            final int arrival = arrivalTurn.getAsInt();
            Preconditions.checkArgument(
                    arrival >= 1 && arrival <= CONVERT_PERIOD,
                    "Invalid native arrival draw");
            row.setArrival(arrival);
        }
        row.setElapsed(row.getElapsed() + 1);
        final boolean arrives = row.getElapsed() == row.getArrival();
        if (row.getElapsed() == CONVERT_PERIOD) {
            row.setElapsed(0);
            row.setArrival(0);
        }
        return arrives ? Optional.of(row.getFriend()) : Optional.empty();
    }

    private void refreshFriend(final NativeNation nation) {
        final NativeFriendshipPo row = nations.get(nation);
        final EuropeanNation friend = friend(nation).orElse(null);
        if (row.getFriend() != friend) {
            row.setFriend(friend);
            row.setElapsed(0);
            row.setArrival(0);
        }
    }

    List<NativeFriendshipPo> save() {
        final List<NativeFriendshipPo> result = new ArrayList<>();
        nations.values().forEach(row -> result.add(copy(row)));
        return result;
    }

    private static NativeFriendshipPo copy(final NativeFriendshipPo row) {
        final NativeFriendshipPo out = new NativeFriendshipPo();
        out.setNation(row.getNation());
        out.setFavour(new EnumMap<>(row.getFavour()));
        out.setFriend(row.getFriend());
        out.setElapsed(row.getElapsed());
        out.setArrival(row.getArrival());
        return out;
    }

    private static int clamp(final int value) {
        return Math.max(1, Math.min(10, value));
    }
}
