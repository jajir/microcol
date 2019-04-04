package org.microcol.model.campaign;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

enum Missions implements MessageKeyResource {

    default_m0_g1,
    default_m0_g2,
    default_m0_g3,
    default_m0_g4,
    default_m0_start,
    default_m0_cantMoveToHighSeas,
    default_m0_pressNextTurn,
    default_m0_moveUnitBeforeEndTurn,
    default_m0_continentInSight,
    default_m0_firstColonyWasFounded,
    default_m0_produce100cigars,
    default_m0_sellCigarsInEuropePort,
    default_m0_cigarsWasSold,
    default_m0_gameOver,
    default_m0_cantDeclareIndependence,
    default_m1_g1,
    default_m1_g2,
    default_m1_g3,
    default_m1_start,
    default_m1_foundColonies,
    default_m1_foundColonies_done,
    default_m1_get5000,
    default_m1_get5000_done,
    default_m1_makeArmy,
    default_m1_makeArmy_done,
    default_m1_done,
    default_m2_g1,
    default_m2_g2,
    default_m2_start,
    default_m2_declareIndependence,
    default_m2_declareIndependence_done,
    default_m2_portIsClosed,
    default_m2_done1,
    default_m2_done2,;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
