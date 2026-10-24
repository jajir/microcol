package org.microcol.model.turnevent;

import java.util.Optional;

import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * A persisted notice that a colony's head project was finished during the
 * owner's update. It is routine: it never opens the report by itself when the
 * turn summary is off.
 */
public final class TurnEventProjectCompleted extends AbstractTurnEvent {
    private final int colonyId;
    private final String colonyName;
    private final QueueItemType projectKind;
    private final ConstructionType constructionType;
    private final UnitType unitType;

    TurnEventProjectCompleted(final String playerName, final int colonyId,
            final String colonyName, final QueueItemType projectKind,
            final ConstructionType constructionType, final UnitType unitType) {
        super(playerName);
        Preconditions.checkArgument(
                colonyId >= 0 && colonyName != null && !colonyName.isBlank()
                        && projectKind != null,
                "Invalid project completed event");
        if (projectKind == QueueItemType.CONSTRUCTION) {
            Preconditions
                    .checkArgument(
                            constructionType != null
                                    && ConstructionType.ALL
                                            .contains(constructionType)
                                    && unitType == null,
                            "Invalid completed construction project");
        } else {
            Preconditions.checkArgument(
                    unitType != null && UnitType.UNIT_TYPES.contains(unitType)
                            && constructionType == null,
                    "Invalid completed unit project");
        }
        this.colonyId = colonyId;
        this.colonyName = colonyName;
        this.projectKind = projectKind;
        this.constructionType = constructionType;
        this.unitType = unitType;
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public QueueItemType getProjectKind() {
        return projectKind;
    }

    public Optional<ConstructionType> getConstructionType() {
        return Optional.ofNullable(constructionType);
    }

    public Optional<UnitType> getUnitType() {
        return Optional.ofNullable(unitType);
    }

    /** @return a new value describing the finished project */
    public ColonyBuildingItem getProject() {
        return projectKind == QueueItemType.CONSTRUCTION
                ? new ColonyBuildingItemConstruction(constructionType)
                : new ColonyBuildingItemUnit(unitType);
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setColonyId(colonyId);
        po.setColonyName(colonyName);
        po.setProjectKind(projectKind);
        po.setProjectConstructionType(constructionType);
        po.setProjectUnitType(unitType);
        return po;
    }

    public static TurnEventProjectCompleted tryLoad(final TurnEventPo po) {
        if (!TurnEventProjectCompleted.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getPlayerName() != null && po.getColonyId() != null
                        && po.getColonyName() != null,
                "Incomplete project completed event");
        return new TurnEventProjectCompleted(po.getPlayerName(),
                po.getColonyId(), po.getColonyName(), po.getProjectKind(),
                po.getProjectConstructionType(), po.getProjectUnitType());
    }
}
