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
 * A persisted notice that a colony's head project has enough hammers but waits
 * for tools. It freezes the project and shortage seen at the construction
 * phase.
 */
public final class TurnEventConstructionWaitingForTools
        extends AbstractTurnEvent {
    private final int colonyId;
    private final String colonyName;
    private final QueueItemType projectKind;
    private final ConstructionType constructionType;
    private final UnitType unitType;
    private final int missingTools;

    TurnEventConstructionWaitingForTools(final String playerName,
            final int colonyId, final String colonyName,
            final QueueItemType projectKind,
            final ConstructionType constructionType, final UnitType unitType,
            final int missingTools) {
        super(playerName);
        Preconditions.checkArgument(
                colonyId >= 0 && colonyName != null && !colonyName.isBlank()
                        && projectKind != null,
                "Invalid construction waiting event");
        final int requiredTools;
        if (projectKind == QueueItemType.CONSTRUCTION) {
            Preconditions
                    .checkArgument(
                            constructionType != null
                                    && ConstructionType.ALL
                                            .contains(constructionType)
                                    && constructionType.canBeConstructed()
                                    && unitType == null,
                            "Invalid construction waiting project");
            requiredTools = constructionType.getRequiredTools();
        } else {
            Preconditions.checkArgument(
                    unitType != null && UnitType.UNIT_TYPES.contains(unitType)
                            && unitType.canBeBuildInColony()
                            && constructionType == null,
                    "Invalid construction waiting project");
            requiredTools = unitType.getRequiredTools();
        }
        Preconditions.checkArgument(
                missingTools > 0 && missingTools <= requiredTools,
                "Invalid construction tools shortage");
        this.colonyId = colonyId;
        this.colonyName = colonyName;
        this.projectKind = projectKind;
        this.constructionType = constructionType;
        this.unitType = unitType;
        this.missingTools = missingTools;
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

    /** @return a new value describing the reported project */
    public ColonyBuildingItem getProject() {
        return projectKind == QueueItemType.CONSTRUCTION
                ? new ColonyBuildingItemConstruction(constructionType)
                : new ColonyBuildingItemUnit(unitType);
    }

    public int getMissingTools() {
        return missingTools;
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
        po.setMissingTools(missingTools);
        return po;
    }

    public static TurnEventConstructionWaitingForTools tryLoad(
            final TurnEventPo po) {
        if (!TurnEventConstructionWaitingForTools.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getPlayerName() != null && po.getColonyId() != null
                        && po.getMissingTools() != null,
                "Incomplete construction waiting event");
        return new TurnEventConstructionWaitingForTools(po.getPlayerName(),
                po.getColonyId(), po.getColonyName(), po.getProjectKind(),
                po.getProjectConstructionType(), po.getProjectUnitType(),
                po.getMissingTools());
    }
}
