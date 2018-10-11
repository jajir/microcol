package org.microcol.gui.gamemenu;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum GameMenu implements MessageKeyResource {

    buttonContinue, buttonLoadSave, buttonPlayCampaign, buttonFreeGame, buttonExitMicroCol;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
