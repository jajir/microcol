package org.microcol.model;

import java.util.Objects;

/**
 * Immutable terms of a rival-mission contest (R-0165): the visiting missionary,
 * the resident that holds the rival mission, the settlement, each side's
 * expertise and numeric hostility, the challenger's exact chance and the
 * revision of the mission. The confirmation shows these terms; the confirmed
 * contest runs only when the current terms equal them, so a changed mission is
 * refused even when its visible values match again.
 */
public final class NativeMissionContestView {

    private final int visitorId;

    private final int settlementId;

    private final int residentId;

    private final EuropeanNation residentOwner;

    private final String residentOwnerName;

    private final boolean visitorExpert;

    private final boolean residentExpert;

    private final double visitorHostility;

    private final double residentHostility;

    private final double challengerChance;

    private final int missionRevision;

    NativeMissionContestView(final int visitorId, final int settlementId,
            final int residentId, final EuropeanNation residentOwner,
            final String residentOwnerName, final boolean visitorExpert,
            final boolean residentExpert, final double visitorHostility,
            final double residentHostility, final double challengerChance,
            final int missionRevision) {
        this.visitorId = visitorId;
        this.settlementId = settlementId;
        this.residentId = residentId;
        this.residentOwner = Objects.requireNonNull(residentOwner);
        this.residentOwnerName = Objects.requireNonNull(residentOwnerName);
        this.visitorExpert = visitorExpert;
        this.residentExpert = residentExpert;
        this.visitorHostility = visitorHostility;
        this.residentHostility = residentHostility;
        this.challengerChance = challengerChance;
        this.missionRevision = missionRevision;
    }

    /**
     * @return identity of the visiting missionary that would challenge
     */
    public int getVisitorId() {
        return visitorId;
    }

    /**
     * @return identity of the settlement whose mission is contested
     */
    public int getSettlementId() {
        return settlementId;
    }

    /**
     * @return identity of the resident missionary that holds the mission
     */
    public int getResidentId() {
        return residentId;
    }

    /**
     * @return the colonial nation that holds the rival mission
     */
    public EuropeanNation getResidentOwner() {
        return residentOwner;
    }

    /**
     * @return the display name of the rival, as the game shows it
     */
    public String getResidentOwnerName() {
        return residentOwnerName;
    }

    /**
     * @return whether the visiting missionary acts as an expert now
     */
    public boolean isVisitorExpert() {
        return visitorExpert;
    }

    /**
     * @return whether the resident missionary acts as an expert now
     */
    public boolean isResidentExpert() {
        return residentExpert;
    }

    /**
     * @return the visitor's nation's numeric hostility, the larger of tribal
     *         anger and local alarm, from 0 to 100
     */
    public double getVisitorHostility() {
        return visitorHostility;
    }

    /**
     * @return the rival nation's numeric hostility, the larger of tribal anger
     *         and local alarm, from 0 to 100
     */
    public double getResidentHostility() {
        return residentHostility;
    }

    /**
     * @return chance that the visiting missionary wins, a fraction from 0.1 to
     *         0.9 inclusive
     */
    public double getChallengerChance() {
        return challengerChance;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NativeMissionContestView)) {
            return false;
        }
        final NativeMissionContestView that = (NativeMissionContestView) other;
        return visitorId == that.visitorId && settlementId == that.settlementId
                && residentId == that.residentId
                && residentOwner == that.residentOwner
                && residentOwnerName.equals(that.residentOwnerName)
                && visitorExpert == that.visitorExpert
                && residentExpert == that.residentExpert
                && visitorHostility == that.visitorHostility
                && residentHostility == that.residentHostility
                && challengerChance == that.challengerChance
                && missionRevision == that.missionRevision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitorId, settlementId, residentId, residentOwner,
                residentOwnerName, visitorExpert, residentExpert,
                visitorHostility, residentHostility, challengerChance,
                missionRevision);
    }

}
