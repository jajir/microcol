package org.microcol.gui.screen.statistics;

import org.microcol.i18n.MessageKeyResource;

/**
 * Labels of the colony advisor (R-0200). The residents, the goods and the
 * project figures come from the model.
 */
public enum ColonyAdvisorText implements MessageKeyResource {
    note, empty, unavailable, colonyRow, growthNone, growthBirths, growthFamine,
    tableGood, tableStock, tableOutput, tableUsed, tableOther, tableChange,
    tableNext, outputBlocked, nextLost, projectNone, projectWaitingHammers,
    projectWaitingTools, projectCompletes, projectBlocked, projectStock, open,
    colonyUnavailable, unknownValue, uncertainLost, uncertainColonyLost,
    uncertainStorage, projectUncertain, projectColonyLost;
}
