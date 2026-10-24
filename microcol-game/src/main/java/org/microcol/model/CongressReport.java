package org.microcol.model;

import java.util.List;
import java.util.Optional;

/**
 * Congress report (R-0203) of one colonial nation. It composes the national
 * rebel support (R-0100), the recruited founders with the selected one and the
 * bells toward it (R-0104), and the Crown roster of the same nation (R-0185).
 * Every figure is read through the owner's getter that the rules use, so the
 * report neither recruits, advances, grows nor consumes anything. It belongs to
 * one viewer; no other nation's Crown is ever part of it.
 *
 * <p>
 * The report's own figures (support, founders, bells, target and the Jones
 * state) are fixed when it is built. The Crown roster is not copied: it is the
 * owner's live roster, read through its public getters, which change nothing.
 * Build a new report to see changes made after this one.
 * </p>
 */
public final class CongressReport {

    private final RebelSupport nationalSupport;
    private final List<Founder> members;
    private final Optional<Founder> selectedFounder;
    private final int bells;
    private final int target;
    private final boolean jonesDeliveryPending;
    private final Optional<RoyalRoster> crownRoster;

    private CongressReport(final RebelSupport nationalSupport,
            final List<Founder> members,
            final Optional<Founder> selectedFounder, final int bells,
            final int target, final boolean jonesDeliveryPending,
            final Optional<RoyalRoster> crownRoster) {
        this.nationalSupport = nationalSupport;
        this.members = members;
        this.selectedFounder = selectedFounder;
        this.bells = bells;
        this.target = target;
        this.jonesDeliveryPending = jonesDeliveryPending;
        this.crownRoster = crownRoster;
    }

    /**
     * Build the report of the current state of one colonial nation.
     *
     * @param viewer required colonial nation whose Congress is reported
     * @return immutable report of the current state
     */
    static CongressReport calculate(final Player viewer) {
        return new CongressReport(viewer.getNationalRebelSupport(),
                List.copyOf(viewer.getCongressMembers()),
                viewer.getSelectedFounder(), viewer.getCongressBells(),
                viewer.getCongressTarget(), viewer.isJonesDeliveryPending(),
                viewer.getCrownRoster());
    }

    /**
     * @return exact national support of the viewer; zero residents give zero
     */
    public RebelSupport getNationalSupport() {
        return nationalSupport;
    }

    /**
     * @return recruited founders in the order of recruitment, never null
     */
    public List<Founder> getMembers() {
        return members;
    }

    /**
     * @return founder chosen for the next objective, empty when none is chosen
     */
    public Optional<Founder> getSelectedFounder() {
        return selectedFounder;
    }

    /**
     * @return Congress bells banked toward the next objective
     */
    public int getBells() {
        return bells;
    }

    /**
     * @return Congress bells needed for the next objective; zero when none is
     *         open
     */
    public int getTarget() {
        return target;
    }

    /**
     * @return whether John Paul Jones is recruited but not yet delivered
     */
    public boolean isJonesDeliveryPending() {
        return jonesDeliveryPending;
    }

    /**
     * The Crown roster is the owner's live roster; its public getters only read
     * it. Empty for a nation without a Crown.
     *
     * @return the Crown roster that rules this nation, if any
     */
    public Optional<RoyalRoster> getCrownRoster() {
        return crownRoster;
    }
}
