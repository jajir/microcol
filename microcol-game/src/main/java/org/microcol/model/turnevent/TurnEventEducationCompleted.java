package org.microcol.model.turnevent;

import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/** Frozen report of one completed social, school, or work-learning step. */
public final class TurnEventEducationCompleted extends AbstractTurnEvent {
    private final int colonyId;
    private final String colonyName;
    private final int studentId;
    private final UnitType beforeType;
    private final UnitType afterType;
    private final boolean wasVeteran;
    private final boolean veteran;
    private final boolean workExperience;

    TurnEventEducationCompleted(final String playerName, final int colonyId,
            final String colonyName, final int studentId,
            final UnitType beforeType, final UnitType afterType,
            final boolean wasVeteran, final boolean veteran,
            final boolean workExperience) {
        super(playerName);
        Preconditions
                .checkArgument(
                        colonyId >= 0 && studentId >= 0 && beforeType != null
                                && afterType != null && colonyName != null
                                && !colonyName.isEmpty()
                                && (beforeType != afterType
                                        || wasVeteran != veteran),
                        "Invalid education completion report");
        this.colonyId = colonyId;
        this.colonyName = colonyName;
        this.studentId = studentId;
        this.beforeType = beforeType;
        this.afterType = afterType;
        this.wasVeteran = wasVeteran;
        this.veteran = veteran;
        this.workExperience = workExperience;
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public int getStudentId() {
        return studentId;
    }

    public UnitType getBeforeType() {
        return beforeType;
    }

    public UnitType getAfterType() {
        return afterType;
    }

    public boolean wasVeteran() {
        return wasVeteran;
    }

    public boolean isVeteran() {
        return veteran;
    }

    public boolean isWorkExperience() {
        return workExperience;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setColonyId(colonyId);
        po.setColonyName(colonyName);
        po.setEducationStudentId(studentId);
        po.setEducationBeforeType(beforeType);
        po.setEducationAfterType(afterType);
        po.setEducationWasVeteran(wasVeteran);
        po.setEducationVeteran(veteran);
        po.setEducationWorkExperience(workExperience);
        return po;
    }

    public static TurnEventEducationCompleted tryLoad(final TurnEventPo po) {
        if (!TurnEventEducationCompleted.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getColonyId() != null && po.getEducationStudentId() != null
                        && po.getEducationBeforeType() != null
                        && po.getEducationAfterType() != null
                        && po.getEducationWasVeteran() != null
                        && po.getEducationVeteran() != null
                        && po.getEducationWorkExperience() != null,
                "Incomplete education completion report");
        return new TurnEventEducationCompleted(po.getPlayerName(),
                po.getColonyId(), po.getColonyName(),
                po.getEducationStudentId(), po.getEducationBeforeType(),
                po.getEducationAfterType(), po.getEducationWasVeteran(),
                po.getEducationVeteran(), po.getEducationWorkExperience());
    }
}
