package org.microcol.gui.screen.congress;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.I18n;
import org.microcol.model.Founder;

/** Localized Congress labels and founder names. */
public enum CongressText implements MessageKeyResource {
    title, choose, back, support, allMembers, jonesEffect, progress, objective,
    chooseObjective, members, none, jonesPending, jonesDelivered, firstColony,
    chosen, changed, founderJoined, royalInfantry, royalCavalry, royalArtillery,
    royalMenOfWar, royalFamily, royalBells, royalStopped, interventionAnnounced,
    interventionEarned, interventionProgress, interventionAwaiting,
    interventionDelivered, interventionPending, MILITARY, POLITICAL, TRADE,
    RELIGIOUS, EXPLORATION, HERNAN_CORTES, FRANCIS_DRAKE, JOHN_PAUL_JONES,
    PAUL_REVERE, GEORGE_WASHINGTON, SIMON_BOLIVAR, BENJAMIN_FRANKLIN,
    THOMAS_JEFFERSON, THOMAS_PAINE, POCAHONTAS, ADAM_SMITH, JAKOB_FUGGER,
    PETER_MINUIT, PETER_STUYVESANT, JAN_DE_WITT, JEAN_DE_BREBEUF,
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
    effectSIEUR_DE_LA_SALLE, effectFERDINAND_MAGELLAN, effectHERNANDO_DE_SOTO;

    public static String effect(I18n i18n, Founder founder) {
        return i18n.get(valueOf("effect" + founder.name()));
    }

    public static String name(I18n i18n, Founder founder) {
        return i18n.get(valueOf(founder.name()));
    }
}
