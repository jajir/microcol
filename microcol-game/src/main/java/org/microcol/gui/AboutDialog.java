package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ApplicationInfo;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * About MicroCol game dialog.
 */
public class AboutDialog extends AbstractWarningDialog {

    @Inject
    public AboutDialog(final ViewUtil viewUtil, final Text text,
            final ApplicationInfo applicationInfo) {
        super(viewUtil, text, "dialogAbout.caption");
        getContext().getChildren()
                .add(new Label(text.get("dialogAbout.version") + applicationInfo.getVersion()));
    }

}
