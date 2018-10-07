package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ApplicationInfo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * About MicroCol game dialog.
 */
public class AboutDialog extends AbstractWarningDialog {

    @Inject
    public AboutDialog(final ViewUtil viewUtil, final I18n i18n,
            final ApplicationInfo applicationInfo) {
        super(viewUtil, i18n, Dialog.about_caption);
        getContext().getChildren()
                .add(new Label(i18n.get(Dialog.about_version) + applicationInfo.getVersion()));
    }

}
