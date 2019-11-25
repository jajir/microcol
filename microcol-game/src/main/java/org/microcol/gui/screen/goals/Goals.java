package org.microcol.gui.screen.goals;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

/**
 * When some text localization doesn't belong to specific place that it should
 * be here.
 */
public enum Goals implements MessageKeyResource {

    /*
     * goals texts.
     */
    title,
    notYetDone,
    
    /*
     * Rest of resources.
     */
    buttonBack;
    
    ;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
