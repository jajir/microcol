package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Answer to a quick cargo request: either the transfer the player may confirm,
 * or the reason it is refused.
 */
public final class QuickCargoChoice {

    private final QuickCargoOffer offer;

    private final QuickCargoProblem problem;

    private QuickCargoChoice(final QuickCargoOffer offer,
            final QuickCargoProblem problem) {
        this.offer = offer;
        this.problem = problem;
    }

    static QuickCargoChoice of(final QuickCargoOffer offer) {
        return new QuickCargoChoice(Preconditions.checkNotNull(offer), null);
    }

    static QuickCargoChoice refused(final QuickCargoProblem problem) {
        return new QuickCargoChoice(null, Preconditions.checkNotNull(problem));
    }

    /** @return the transfer to confirm, empty when the action is refused */
    public Optional<QuickCargoOffer> getOffer() {
        return Optional.ofNullable(offer);
    }

    /** @return the reason of the refusal, empty when an offer exists */
    public Optional<QuickCargoProblem> getProblem() {
        return Optional.ofNullable(problem);
    }
}
