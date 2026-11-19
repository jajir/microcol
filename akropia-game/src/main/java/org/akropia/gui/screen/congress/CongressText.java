package org.akropia.gui.screen.congress;

import org.akropia.i18n.MessageKeyResource;
import org.akropia.i18n.I18n;
import org.akropia.model.Founder;

/** Localized Congress labels and founder names. */
public enum CongressText implements MessageKeyResource {
    title, choose, back, support, allMembers, jonesEffect, progress, objective,
    chooseObjective, members, none, jonesPending, jonesDelivered, firstColony,
    chosen, changed, founderJoined, royalInfantry, royalCavalry, royalArtillery,
    royalWarGalleys, royalFamily, royalOwls, royalStopped,
    interventionAnnounced, interventionEarned, interventionProgress,
    interventionAwaiting, interventionDelivered, interventionPending, MILITARY,
    POLITICAL, TRADE, RELIGIOUS, EXPLORATION, HERNAN_CORTES, FRANCIS_DRAKE,
    JOHN_PAUL_JONES, PAUL_REVERE, GEORGE_WASHINGTON, SIMON_BOLIVAR,
    BENJAMIN_FRANKLIN, THOMAS_JEFFERSON, THOMAS_PAINE, POCAHONTAS, ADAM_SMITH,
    JAKOB_FUGGER, PETER_MINUIT, PETER_STUYVESANT, JAN_DE_WITT, JEAN_DE_BREBEUF,
    WILLIAM_BREWSTER, BARTOLOME_DE_LAS_CASAS, WILLIAM_PENN, JUAN_DE_SEPULVEDA,
    FRANCISCO_DE_CORONADO, HENRY_HUDSON, SIEUR_DE_LA_SALLE, FERDINAND_MAGELLAN,
    HERNANDO_DE_SOTO, selected, deployment, royalSummary, effectHERNAN_CORTES,
    effectFRANCIS_DRAKE, effectJOHN_PAUL_JONES, effectPAUL_REVERE,
    effectGEORGE_WASHINGTON, effectSIMON_BOLIVAR, effectBENJAMIN_FRANKLIN,
    effectTHOMAS_JEFFERSON, effectTHOMAS_PAINE, effectPOCAHONTAS,
    effectADAM_SMITH, effectJAKOB_FUGGER, effectPETER_MINUIT,
    effectPETER_STUYVESANT, effectJAN_DE_WITT, effectJEAN_DE_BREBEUF,
    effectWILLIAM_BREWSTER, effectBARTOLOME_DE_LAS_CASAS, effectWILLIAM_PENN,
    effectJUAN_DE_SEPULVEDA, effectFRANCISCO_DE_CORONADO, effectHENRY_HUDSON,
    effectSIEUR_DE_LA_SALLE, effectFERDINAND_MAGELLAN, effectHERNANDO_DE_SOTO,
    help, cost, history, wiki, source, externalFailure, requirements,
    shipDelivery, buildingPrerequisites, artifactHERNAN_CORTES,
    artifactFRANCIS_DRAKE, artifactJOHN_PAUL_JONES, artifactPAUL_REVERE,
    artifactGEORGE_WASHINGTON, artifactSIMON_BOLIVAR, artifactBENJAMIN_FRANKLIN,
    artifactTHOMAS_JEFFERSON, artifactTHOMAS_PAINE, artifactPOCAHONTAS,
    artifactADAM_SMITH, artifactJAKOB_FUGGER, artifactPETER_MINUIT,
    artifactPETER_STUYVESANT, artifactJAN_DE_WITT, artifactJEAN_DE_BREBEUF,
    artifactWILLIAM_BREWSTER, artifactBARTOLOME_DE_LAS_CASAS,
    artifactWILLIAM_PENN, artifactJUAN_DE_SEPULVEDA,
    artifactFRANCISCO_DE_CORONADO, artifactHENRY_HUDSON,
    artifactSIEUR_DE_LA_SALLE, artifactFERDINAND_MAGELLAN,
    artifactHERNANDO_DE_SOTO, historyHERNAN_CORTES, historyFRANCIS_DRAKE,
    historyJOHN_PAUL_JONES, historyPAUL_REVERE, historyGEORGE_WASHINGTON,
    historySIMON_BOLIVAR, historyBENJAMIN_FRANKLIN, historyTHOMAS_JEFFERSON,
    historyTHOMAS_PAINE, historyPOCAHONTAS, historyADAM_SMITH,
    historyJAKOB_FUGGER, historyPETER_MINUIT, historyPETER_STUYVESANT,
    historyJAN_DE_WITT, historyJEAN_DE_BREBEUF, historyWILLIAM_BREWSTER,
    historyBARTOLOME_DE_LAS_CASAS, historyWILLIAM_PENN,
    historyJUAN_DE_SEPULVEDA, historyFRANCISCO_DE_CORONADO, historyHENRY_HUDSON,
    historySIEUR_DE_LA_SALLE, historyFERDINAND_MAGELLAN,
    historyHERNANDO_DE_SOTO;

    public static String effect(I18n i18n, Founder founder) {
        return i18n.get(valueOf("effect" + founder.name()));
    }

    public static String rules(final I18n i18n, final Founder founder) {
        switch (founder) {
        case JOHN_PAUL_JONES:
            return i18n.get(shipDelivery);
        case ADAM_SMITH:
        case PETER_STUYVESANT:
            return i18n.get(buildingPrerequisites);
        default:
            return "";
        }
    }

    public static String name(I18n i18n, Founder founder) {
        return i18n.get(valueOf(founder.name()));
    }
}
