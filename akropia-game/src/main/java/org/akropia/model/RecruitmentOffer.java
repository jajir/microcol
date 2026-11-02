package org.akropia.model;

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

    private final int torches;

    private final int earnedImmigrants;

    RecruitmentOffer(final RecruitmentPool pool, final int slot,
            final int candidateId, final UnitType type, final int price,
            final int paidPassages, final int torches,
            final int earnedImmigrants) {
        this.pool = pool;
        this.slot = slot;
        this.candidateId = candidateId;
        this.type = type;
        this.price = price;
        this.paidPassages = paidPassages;
        this.torches = torches;
        this.earnedImmigrants = earnedImmigrants;
    }

    RecruitmentPool getPool() {
        return pool;
    }

    int getPaidPassages() {
        return paidPassages;
    }

    int getTorches() {
        return torches;
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
