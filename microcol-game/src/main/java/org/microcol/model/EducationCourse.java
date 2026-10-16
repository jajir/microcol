package org.microcol.model;

import org.microcol.model.store.EducationCoursePo;

import com.google.common.base.Preconditions;

/** Immutable school progress shown to the player and saved with its colony. */
public final class EducationCourse {
    private final Integer teacherId;
    private final int studentId;
    private final UnitType skill;
    private final int completedUpdates;
    private final int requiredUpdates;
    private final boolean paused;

    EducationCourse(final Integer teacherId, final int studentId,
            final UnitType skill, final int completedUpdates,
            final int requiredUpdates, final boolean paused) {
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.skill = Preconditions.checkNotNull(skill);
        this.completedUpdates = completedUpdates;
        this.requiredUpdates = requiredUpdates;
        this.paused = paused;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public int getStudentId() {
        return studentId;
    }

    public UnitType getSkill() {
        return skill;
    }

    public int getCompletedUpdates() {
        return completedUpdates;
    }

    public int getRequiredUpdates() {
        return requiredUpdates;
    }

    public boolean isPaused() {
        return paused;
    }

    EducationCoursePo save() {
        final EducationCoursePo po = new EducationCoursePo();
        po.setTeacherId(teacherId);
        po.setStudentId(studentId);
        po.setSkill(skill);
        po.setCompletedUpdates(completedUpdates);
        return po;
    }
}
