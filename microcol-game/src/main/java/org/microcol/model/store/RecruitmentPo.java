package org.microcol.model.store;

import java.util.List;

/** Current-schema persistent European recruitment state. */
public final class RecruitmentPo {

    private List<RecruitmentCandidatePo> candidates;

    private Integer nextCandidateId;

    private Integer paidPassages;

    private Integer crosses;

    private Integer earnedImmigrants;

    public List<RecruitmentCandidatePo> getCandidates() {
        return candidates;
    }

    public void setCandidates(final List<RecruitmentCandidatePo> candidates) {
        this.candidates = candidates;
    }

    public Integer getNextCandidateId() {
        return nextCandidateId;
    }

    public void setNextCandidateId(final Integer nextCandidateId) {
        this.nextCandidateId = nextCandidateId;
    }

    public Integer getPaidPassages() {
        return paidPassages;
    }

    public void setPaidPassages(final Integer paidPassages) {
        this.paidPassages = paidPassages;
    }

    public Integer getCrosses() {
        return crosses;
    }

    public void setCrosses(final Integer crosses) {
        this.crosses = crosses;
    }

    public Integer getEarnedImmigrants() {
        return earnedImmigrants;
    }

    public void setEarnedImmigrants(final Integer earnedImmigrants) {
        this.earnedImmigrants = earnedImmigrants;
    }
}
