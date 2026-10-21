package org.microcol.gui.screen.colony;

import org.microcol.i18n.MessageKeyResource;

/** Trade route editor labels and the model's rejection reasons. */
public enum TradeRouteText implements MessageKeyResource {
    open, title, rule, nameLabel, kindLabel, land, sea, startLabel, stopLabel,
    europe, existing, noRoutes, create, cancel, changed, nameEMPTY,
    nameTOO_LONG, nameINVALID, nameDUPLICATE, notOwner, idExhausted,
    startUnavailable, startNotPort, stopUnavailable, europeByLand, sameStop,
    stopNotReachable,
    // Cargo at route stops: configuration, priority and processing.
    cargoOpen, cargoTitle, cargoRule, cargoRouteLabel, cargoStopLabel,
    cargoStopItem, unloadHeading, loadHeading, addGoods, removeGoods, moveUp,
    moveDown, saveCargo, closeCargo, cargoNoRoutes, cargoSaved, cargoUnsaved,
    carrierLabel, noCarrier, carrierReady, carrierItem, processNow,
    resultHeading, verbUnload, verbLoad, entryLine, entryGold, noEntries,
    problemCARRIER_UNAVAILABLE, problemROUTE_UNAVAILABLE,
    problemSTOP_UNAVAILABLE, problemVISIT_ALREADY_PROCESSED, problemTURN_BUSY,
    reasonMOVED, reasonPARTIAL_DESTINATION_FULL, reasonPARTIAL_HOLD_FULL,
    reasonPARTIAL_FUNDS, reasonNOTHING_ON_BOARD, reasonDESTINATION_FULL,
    reasonHOLD_FULL, reasonSOURCE_EMPTY, reasonINSUFFICIENT_FUNDS,
    reasonEUROPE_CLOSED, reasonBOYCOTTED, reasonREJECTED, reasonNOT_ATTEMPTED,
    // Assigning a carrier to repeat the route and showing where it is heading.
    assignRule, assignFirstStopLabel, assignCarrier, assignDone,
    assignedHeading, assignedNone, assignedMoving, assignedPaused,
    assignProblemNOT_OWNER, assignProblemNOT_CARRIER, assignProblemCARRIER_BUSY,
    assignProblemROUTE_UNAVAILABLE, assignProblemSTOP_UNAVAILABLE,
    assignProblemSTOP_NOT_REACHABLE, assignProblemTURN_BUSY, pauseNO_ROUTE,
    pauseSTOP_UNREACHABLE, pauseSTOP_LOST, pauseEUROPE_CLOSED,
    pauseCARRIER_DAMAGED,
    // Canceling one carrier, deleting a route, and replacing a stop's colony.
    cancelAssignment, cancelAssignmentDone, deleteRoute, routeDeleted, stopLost,
    replaceStopLabel, replaceStop, replaceStopDone, editProblemNOT_OWNER,
    editProblemROUTE_UNAVAILABLE, editProblemSTOP_UNAVAILABLE,
    editProblemEUROPE_BY_LAND, editProblemSAME_STOP,
    editProblemSTOP_NOT_REACHABLE, editProblemTURN_BUSY,
    // Choosing a carrier to cancel, confirming a delete, and pending changes.
    assignedPickLabel, confirmDeleteRoute, deleteConfirm, deleteReleases,
    replaceAffects, assignedPending, assignedRepaired, affectedNone;
}
