package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Immutable view of the one native course a settlement holds (R-0148): who
 * learns, the offered skill it grants, how many owner updates are done, and the
 * stage the course is in. A finished or canceled course keeps its colonist at
 * the settlement until a legal square is free to leave.
 */
public final class NativeTeachingCourse {

    /** European owner updates a course needs before the skill is granted. */
    public static final int REQUIRED_UPDATES = 4;

    /** Stage of a course, saved with the settlement. */
    public enum Stage {

        /** The colonist is still learning and counts owner updates. */
        LEARNING,

        /** The skill was granted once; the colonist only waits to leave. */
        COMPLETED,

        /**
         * Hostility ended the learning; progress is discarded, nothing granted.
         */
        CANCELED

    }

    private final int pupilId;

    private final UnitType skill;

    private final int completedUpdates;

    private final Stage stage;

    NativeTeachingCourse(final int pupilId, final UnitType skill,
            final int completedUpdates, final Stage stage) {
        this.pupilId = pupilId;
        this.skill = Preconditions.checkNotNull(skill);
        this.completedUpdates = completedUpdates;
        this.stage = Preconditions.checkNotNull(stage);
    }

    /**
     * @return identity of the colonist at the settlement
     */
    public int getPupilId() {
        return pupilId;
    }

    /**
     * @return the settlement's offered skill, which the pupil receives once
     */
    public UnitType getSkill() {
        return skill;
    }

    /**
     * @return owner updates counted in the learning, from zero to the required
     *         count
     */
    public int getCompletedUpdates() {
        return completedUpdates;
    }

    /**
     * @return owner updates still needed before the skill is granted; zero once
     *         the learning is no longer going on
     */
    public int getRequiredUpdates() {
        return REQUIRED_UPDATES;
    }

    /**
     * @return the stage of this course
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * @return whether the colonist still needs a legal square to leave the
     *         settlement, having finished or been canceled
     */
    public boolean isAwaitingExit() {
        return stage != Stage.LEARNING;
    }

}
