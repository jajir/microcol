package org.microcol.model.store;

import org.microcol.model.UnitType;

/** One persisted school assignment and its completed owner updates. */
public final class EducationCoursePo {
    private Integer teacherId;
    private Integer studentId;
    private UnitType skill;
    private Integer completedUpdates;

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(final Integer value) {
        teacherId = value;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(final Integer value) {
        studentId = value;
    }

    public UnitType getSkill() {
        return skill;
    }

    public void setSkill(final UnitType value) {
        skill = value;
    }

    public Integer getCompletedUpdates() {
        return completedUpdates;
    }

    public void setCompletedUpdates(final Integer value) {
        completedUpdates = value;
    }
}
