package org.microcol.model.turnevent;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

/**
 * When some text localization doesn't belong to specific place that it should
 * be here.
 */
public enum TurnEvents implements MessageKeyResource {

    shipComeToEuropePort,
    shipComeToHighSeas,
    faminePlagueColony,
    famineWillPlagueColony,
    colonyWasDestroyed,
    newUnitIsInEurope,
    newUnitIsInColony,
    colonyWasLost,
    goodsWasThrowsAway,
    

    ;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
