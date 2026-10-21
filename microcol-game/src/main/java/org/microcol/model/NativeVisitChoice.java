package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * One action a visitor could choose at a native settlement, with the limit that
 * stops it now. A choice is enabled only when its owner's effect exists:
 * teaching (R-0148) is available to an eligible visitor; the other actions
 * still wait for their own requirements.
 */
public final class NativeVisitChoice {

    /** Why an action cannot be chosen now, or that it can. */
    public enum Limit {

        /** The visitor's role does not allow this action. */
        ROLE_REFUSED,

        /** The settlement's attitude toward the visitor's nation refuses it. */
        RELATION_REFUSED,

        /** The visitor qualifies, but this action is not yet available. */
        NOT_YET_AVAILABLE,

        /** The settlement already teaches one pupil in its single course. */
        SETTLEMENT_OCCUPIED,

        /** The visitor qualifies and the action can be chosen now. */
        AVAILABLE

    }

    private final NativeVisitAction action;

    private final Limit limit;

    NativeVisitChoice(final NativeVisitAction action, final Limit limit) {
        this.action = Preconditions.checkNotNull(action);
        this.limit = Preconditions.checkNotNull(limit);
    }

    /**
     * @return the action this choice describes
     */
    public NativeVisitAction getAction() {
        return action;
    }

    /**
     * @return the limit that stops this action now
     */
    public Limit getLimit() {
        return limit;
    }

}
