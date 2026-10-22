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

        /**
         * The settlement already holds a mission of the visitor's own nation; a
         * second one is refused, so none replaces it.
         */
        MISSION_OCCUPIED,

        /**
         * The settlement holds a rival nation's mission; the visitor may
         * challenge it in the explicit contest of R-0165, which is not an
         * establishment.
         */
        CONTEST_AVAILABLE,

        /**
         * The visitor qualifies, but no other European power may be selected as
         * the target of an incitement now (R-0166).
         */
        NO_INCITEMENT_TARGET,

        /**
         * The visitor qualifies, but the settlement has no actual defender or
         * attack terms the visitor can meet now (R-0174).
         */
        NO_ATTACK_TARGET,

        /**
         * The visitor qualifies, but this European already demanded tribute
         * here within the last ten turns (R-0162).
         */
        TRIBUTE_COOLDOWN,

        /**
         * The visitor qualifies, but a unit other than the settlement's own
         * braves holds its square, so no tribute demand can be made (R-0162).
         */
        NO_TRIBUTE_TARGET,

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
