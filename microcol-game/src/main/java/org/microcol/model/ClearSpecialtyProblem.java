package org.microcol.model;

/** Why a colonist's civilian specialty cannot be cleared right now. */
public enum ClearSpecialtyProblem {
    /** Stale or foreign target, not the owner's turn, or not in this colony. */
    UNAVAILABLE,
    /** The colonist has no civilian specialty to clear. */
    NO_CIVILIAN_SPECIALTY,
    /** The colonist teaches a course that a free colonist could not teach. */
    TEACHER_COURSE,
    /** The colonist occupies a school slot a free colonist could not hold. */
    SCHOOL_SLOT
}
