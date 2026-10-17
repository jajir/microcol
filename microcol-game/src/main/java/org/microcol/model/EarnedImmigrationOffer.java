package org.microcol.model;

/** One frozen free choice at a pending national immigration boundary. */
public final class EarnedImmigrationOffer {
    private final RecruitmentPool pool;
    private final Player player;
    private final int slot;
    private final int candidateId;
    private final UnitType type;
    private final int threshold;
    private final int crosses;
    private final int earnedImmigrants;

    EarnedImmigrationOffer(final RecruitmentPool pool, final Player player,
            final int slot, final int candidateId, final UnitType type,
            final int threshold, final int crosses,
            final int earnedImmigrants) {
        this.pool = pool;
        this.player = player;
        this.slot = slot;
        this.candidateId = candidateId;
        this.type = type;
        this.threshold = threshold;
        this.crosses = crosses;
        this.earnedImmigrants = earnedImmigrants;
    }

    RecruitmentPool getPool() {
        return pool;
    }

    Player getPlayer() {
        return player;
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

    public int getThreshold() {
        return threshold;
    }
}
