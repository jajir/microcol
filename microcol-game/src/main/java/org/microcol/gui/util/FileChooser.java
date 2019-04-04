package org.microcol.gui.util;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum FileChooser implements MessageKeyResource {

    cancelButtonText,
    fileDateHeaderText,
    openButtonText,
    saveButtonText,
    saveDialogTitleText,
    openDialogTitleText,
    newFolderTitleText,
    newFolderButtonText,
    filesOfTypeLabelText,
    saveDialogFileNameLabelText,
    ;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
