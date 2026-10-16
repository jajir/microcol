package org.microcol.model;

/**
 * A price and candidate snapshot from one player's current recruitment pool.
 */
public final class RecruitmentOffer {

    private final RecruitmentPool pool;

    private final int slot;

    private final int candidateId;

    private final UnitType type;

    private final int price;

    private final int paidPassages;

    private final int crosses;

    private final int earnedImmigrants;

    RecruitmentOffer(final RecruitmentPool pool, final int slot,
            final int candidateId, final UnitType type, final int price,
            final int paidPassages, final int crosses,
            final int earnedImmigrants) {
        this.pool = pool;
        this.slot = slot;
        this.candidateId = candidateId;
        this.type = type;
        this.price = price;
        this.paidPassages = paidPassages;
        this.crosses = crosses;
        this.earnedImmigrants = earnedImmigrants;
    }

    RecruitmentPool getPool() {
        return pool;
    }

    int getPaidPassages() {
        return paidPassages;
    }

    int getCrosses() {
        return crosses;
    }

    int getEarnedImmigrants() {
        return earnedImmigrants;
    }

    public int getSlot() {
        return slot;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public UnitType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }
}
