package org.akropia.gui.screen.turnreport;

import org.akropia.i18n.MessageKeyResource;

/**
 * When some text localization doesn't belong to specific place that it should
 * be here.
 */
public enum TurnEvents implements MessageKeyResource {
    fishermanLostCargo, fishermanLostLand,

    /*
     * Turn events texts.
     */
    shipComeToEuropePort, shipComeToHighSeas, faminePlagueColony,
    faminePlagueColonyDestroyed, famineWillPlagueColony, colonyWasDestroyed,
    shipsLostWithColony, newUnitIsInEurope, newUnitIsInColony, colonyWasLost,
    goodsWasThrowsAway1, goodsWasThrowsAway2, goodsWasThrowsAway3,
    goodsWasThrowsAway4, constructionWaitingForTools, projectCompleted,
    openColony, openEurope, forestLumberLostFull, forestLumberLostNoColony,
    fieldPlowed, interventionArrived, mercenaryArrived, tradeRouteUnloaded,
    tradeRouteLoaded, tradeRouteSold, tradeRouteBought,

    /*
     * Rest of resources.
     */
    buttonBack;

    ;

}
