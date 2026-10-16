package org.microcol.model;

/** Immutable preview of one currently available teacher/student choice. */
public final class EducationOption {
    private final UnitType skill;
    private final int requiredSchoolTier;
    private final int requiredUpdates;
    private final UnitType nextType;
    private final boolean becomeVeteran;

    EducationOption(final UnitType skill, final int requiredSchoolTier,
            final int requiredUpdates, final UnitType nextType,
            final boolean becomeVeteran) {
        this.skill = skill;
        this.requiredSchoolTier = requiredSchoolTier;
        this.requiredUpdates = requiredUpdates;
        this.nextType = nextType;
        this.becomeVeteran = becomeVeteran;
    }

    public UnitType getSkill() {
        return skill;
    }

    public int getRequiredSchoolTier() {
        return requiredSchoolTier;
    }

    public int getRequiredUpdates() {
        return requiredUpdates;
    }

    public UnitType getNextType() {
        return nextType;
    }

    public boolean willBecomeVeteran() {
        return becomeVeteran;
    }
}
