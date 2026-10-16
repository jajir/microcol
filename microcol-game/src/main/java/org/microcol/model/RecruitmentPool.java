package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.microcol.model.store.RecruitmentCandidatePo;
import org.microcol.model.store.RecruitmentPo;

import com.google.common.base.Preconditions;

/**
 * Persistent candidate identities and immigration counters for one colonial
 * player.
 */
public final class RecruitmentPool {

    private final List<RecruitmentCandidatePo> candidates;

    private final Random random = new Random();

    private int nextCandidateId;

    private int paidPassages;

    private int crosses;

    private int earnedImmigrants;

    RecruitmentPool(final RecruitmentPo po) {
        Preconditions.checkArgument(po != null, "Missing recruitment pool");
        Preconditions.checkArgument(
                po.getCandidates() != null && po.getCandidates().size() == 3,
                "Recruitment requires three candidates");
        Preconditions.checkArgument(po.getNextCandidateId() != null
                && po.getNextCandidateId() > 0 && po.getPaidPassages() != null
                && po.getPaidPassages() >= 0 && po.getCrosses() != null
                && po.getCrosses() >= 0 && po.getEarnedImmigrants() != null
                && po.getEarnedImmigrants() >= 0,
                "Invalid recruitment counters");
        final Set<Integer> ids = new HashSet<>();
        for (final RecruitmentCandidatePo candidate : po.getCandidates()) {
            Preconditions
                    .checkArgument(
                            candidate != null && candidate.getId() != null
                                    && candidate.getId() > 0
                                    && candidate.getId() < po
                                            .getNextCandidateId()
                                    && ids.add(candidate.getId())
                                    && isCandidateType(candidate.getType()),
                            "Invalid recruitment candidate");
        }
        candidates = new ArrayList<>(po.getCandidates().size());
        po.getCandidates().forEach(candidate -> candidates
                .add(candidate(candidate.getId(), candidate.getType())));
        nextCandidateId = po.getNextCandidateId();
        paidPassages = po.getPaidPassages();
        crosses = po.getCrosses();
        earnedImmigrants = po.getEarnedImmigrants();
    }

    public static RecruitmentPo initial() {
        final RecruitmentPo po = new RecruitmentPo();
        final Random random = new Random();
        po.setCandidates(List.of(candidate(1, draw(random)),
                candidate(2, draw(random)), candidate(3, draw(random))));
        po.setNextCandidateId(4);
        po.setPaidPassages(0);
        po.setCrosses(0);
        po.setEarnedImmigrants(0);
        return po;
    }

    private static RecruitmentCandidatePo candidate(final int id,
            final UnitType type) {
        final RecruitmentCandidatePo po = new RecruitmentCandidatePo();
        po.setId(id);
        po.setType(type);
        return po;
    }

    static UnitType draw(final Random random) {
        final int roll = random.nextInt(100);
        return draw(roll,
                roll >= 90
                        ? random.nextInt(UnitType.EUROPEAN_SPECIALISTS.size())
                        : 0);
    }

    static UnitType draw(final int roll, final int specialistIndex) {
        Preconditions.checkArgument(roll >= 0 && roll < 100);
        if (roll < 60) {
            return UnitType.COLONIST;
        }
        if (roll < 80) {
            return UnitType.INDENTURED_SERVANT;
        }
        if (roll < 90) {
            return UnitType.PETTY_CRIMINAL;
        }
        return UnitType.EUROPEAN_SPECIALISTS.get(specialistIndex);
    }

    static boolean isCandidateType(final UnitType type) {
        return type == UnitType.COLONIST || type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL
                || UnitType.EUROPEAN_SPECIALISTS.contains(type);
    }

    private static int ceilDivide(final long numerator,
            final long denominator) {
        Preconditions.checkArgument(denominator > 0);
        final long result = Math.addExact(numerator, denominator - 1)
                / denominator;
        return Math.toIntExact(result);
    }

    static int target(final Difficulty difficulty, final String nation,
            final int earnedImmigrants) {
        final int factor = factor(difficulty);
        final int normal = ceilDivide(Math.multiplyExact(
                Math.addExact(100L, Math.multiplyExact(25L, earnedImmigrants)),
                factor), 100);
        return "England".equals(nation)
                ? ceilDivide(Math.multiplyExact(2L, normal), 3)
                : normal;
    }

    private static int factor(final Difficulty difficulty) {
        switch (Preconditions.checkNotNull(difficulty)) {
        case DISCOVERER:
            return 75;
        case EXPLORER:
            return 90;
        case CONQUISTADOR:
            return 100;
        case GOVERNOR:
            return 115;
        case VICEROY:
            return 130;
        default:
            throw new IllegalArgumentException("Unknown difficulty");
        }
    }

    int price(final Difficulty difficulty, final String nation) {
        return priceAt(difficulty, nation, paidPassages);
    }

    private int priceAt(final Difficulty difficulty, final String nation,
            final int passages) {
        final int factor = factor(difficulty);
        final int target = target(difficulty, nation, earnedImmigrants);
        final int base = ceilDivide(Math.multiplyExact(
                Math.addExact(200L, Math.multiplyExact(20L, passages)), factor),
                100);
        final int distance = target - Math.min(crosses, target);
        return Math.max(50,
                ceilDivide(Math.multiplyExact((long) base, distance), target));
    }

    boolean hasReachedTarget(final Difficulty difficulty, final String nation) {
        return crosses >= target(difficulty, nation, earnedImmigrants);
    }

    List<RecruitmentOffer> offers(final Difficulty difficulty,
            final String nation) {
        final int price = price(difficulty, nation);
        final List<RecruitmentOffer> offers = new ArrayList<>(3);
        for (int slot = 0; slot < 3; slot++) {
            final RecruitmentCandidatePo candidate = candidates.get(slot);
            offers.add(new RecruitmentOffer(this, slot, candidate.getId(),
                    candidate.getType(), price, paidPassages, crosses,
                    earnedImmigrants));
        }
        return List.copyOf(offers);
    }

    boolean isCurrent(final RecruitmentOffer offer, final Difficulty difficulty,
            final String nation) {
        if (offer == null || offer.getPool() != this || offer.getSlot() < 0
                || offer.getSlot() >= 3 || hasReachedTarget(difficulty, nation)
                || offer.getPaidPassages() != paidPassages
                || offer.getCrosses() != crosses
                || offer.getEarnedImmigrants() != earnedImmigrants) {
            return false;
        }
        final RecruitmentCandidatePo current = candidates.get(offer.getSlot());
        return current.getId() == offer.getCandidateId()
                && current.getType() == offer.getType()
                && price(difficulty, nation) == offer.getPrice();
    }

    void replace(final int slot) {
        candidates.set(slot, candidate(nextCandidateId++, draw(random)));
        paidPassages++;
    }

    boolean canAdvance(final Difficulty difficulty, final String nation) {
        if (nextCandidateId >= Integer.MAX_VALUE
                || paidPassages >= Integer.MAX_VALUE) {
            return false;
        }
        try {
            priceAt(difficulty, nation, paidPassages + 1);
            return true;
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    RecruitmentPo save() {
        final RecruitmentPo po = new RecruitmentPo();
        final List<RecruitmentCandidatePo> saved = new ArrayList<>(3);
        candidates.forEach(candidate -> saved
                .add(candidate(candidate.getId(), candidate.getType())));
        po.setCandidates(saved);
        po.setNextCandidateId(nextCandidateId);
        po.setPaidPassages(paidPassages);
        po.setCrosses(crosses);
        po.setEarnedImmigrants(earnedImmigrants);
        return po;
    }
}
