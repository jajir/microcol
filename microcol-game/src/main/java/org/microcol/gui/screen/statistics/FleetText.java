package org.microcol.gui.screen.statistics;

import org.microcol.i18n.MessageKeyResource;

/**
 * Labels of the fleet report (R-0202). The ships, places, voyages, repairs and
 * holds come from the model.
 */
public enum FleetText implements MessageKeyResource {
    note, unavailable, total, empty, shipTitle, placeMap, placeEurope, placeSea,
    placeRepair, placeOther, voyageEurope, voyageColonies, voyageWaiting,
    reversed, repair, repairToEurope, repairToColony, repairToLost, cargo,
    goodsNone, goodsItem, passengerItem, holds, show, shipUnavailable,
    noDestination, foreignTitle, foreignEmpty, foreignHolds, foreignUnavailable;
}
