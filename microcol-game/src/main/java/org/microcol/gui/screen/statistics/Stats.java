package org.microcol.gui.screen.statistics;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum Stats implements MessageKeyResource {

    title,
    titleGold,
    titleMilitary,
    titleWealth,
    titleScore,
    titleIndex,
    buttonCancel,
    statistics_year,
    ;
    
    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
