package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

public class DialogUnitCantFightWarning extends AbstractWarningDialog {

    public DialogUnitCantFightWarning(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, "dialogUnitCantFightWarning.caption");
        showAndWait();
    }

}
