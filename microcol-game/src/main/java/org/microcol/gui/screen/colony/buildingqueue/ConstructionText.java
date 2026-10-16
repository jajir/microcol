package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.UnitTypes;
import org.microcol.gui.screen.colony.ConstructionTypeName;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.ColonyBuildingQueue.UnavailableReason;
import org.microcol.model.ConstructionType;

public enum ConstructionText implements MessageKeyResource {
    open, title, projects, queue, choose, cost, available, population,
    predecessor, alreadyBuilt, alreadyQueued, unavailable, add, up, down,
    remove, cancel, changed, added, empty, hammers, hammerPool, tools,
    waitingTools, waitingHammers, ready, coastalRequired, missingShipyard, rush,
    rushConfirm, rushCancel, rushDetail, rushUnavailable, rushNoGold,
    rushChanged, rushCompleted, missingFounder;

    public static String name(final I18n i18n, final ColonyBuildingItem item) {
        if (item instanceof ColonyBuildingItemConstruction) {
            return i18n.get(ConstructionTypeName
                    .getNameForType(((ColonyBuildingItemConstruction) item)
                            .getConstructionType()));
        }
        return i18n.get(UnitTypes
                .getUnitName(((ColonyBuildingItemUnit) item).getUnitType()));
    }

    public static String reason(final I18n i18n, final UnavailableReason reason,
            final ConstructionType type, final Colony colony) {
        switch (reason) {
        case MISSING_FOUNDER:
            return type == null ? i18n.get(unavailable)
                    : i18n.get(missingFounder,
                            org.microcol.gui.screen.congress.CongressText.name(
                                    i18n,
                                    type.getRequiredFounder().orElseThrow()));
        case COASTAL_REQUIRED:
            return i18n.get(coastalRequired);
        case MISSING_SHIPYARD:
            return i18n.get(missingShipyard);
        case POPULATION:
            return i18n.get(population, type.getRequiredColonyPopulation(),
                    colony.getUnitsInsideColony().size());
        case MISSING_PREDECESSOR:
            if (type == null) {
                return i18n.get(unavailable);
            }
            return i18n.get(predecessor, i18n.get(ConstructionTypeName
                    .getNameForType(type.getUpgradeFrom().get())));
        case ALREADY_BUILT:
            return i18n.get(alreadyBuilt);
        case ALREADY_QUEUED:
            return i18n.get(alreadyQueued);
        default:
            return i18n.get(unavailable);
        }
    }

    public static String status(final I18n i18n, final Colony colony) {
        return colony.getColonyBuildingQueue().getActuallyBuildingStat()
                .map(stats -> {
                    final java.util.Optional<UnavailableReason> reason = colony
                            .getColonyBuildingQueue()
                            .getCompletionUnavailableReason();
                    if (reason.isPresent()) {
                        final ColonyBuildingItem item = stats.getItem();
                        return item instanceof ColonyBuildingItemConstruction
                                ? reason(i18n, reason.get(),
                                        ((ColonyBuildingItemConstruction) item)
                                                .getConstructionType(),
                                        colony)
                                : reason(i18n, reason.get(), null, colony);
                    }
                    if (stats.getTools().getAlreadyHave() < stats.getTools()
                            .getRequired()) {
                        return i18n.get(waitingTools,
                                stats.getTools().getRequired()
                                        - stats.getTools().getAlreadyHave());
                    }
                    return i18n.get(stats.getHammers().getAlreadyHave() < stats
                            .getHammers().getRequired() ? waitingHammers
                                    : ready);
                }).orElse(i18n.get(empty));
    }
}
