package org.microcol.gui.buttonpanel;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum Buttons implements MessageKeyResource {
    
    buttonStatistics,
    buttonCenter,
    buttonTurnReport,
    buttonGoals,
    buttonHelp,
    buttonExit,
    buttonNextTurn,
    buttonEurope;
    
    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
