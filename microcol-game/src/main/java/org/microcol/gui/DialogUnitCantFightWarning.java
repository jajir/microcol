package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

public class DialogUnitCantFightWarning extends AbstractWarningDialog {

    public DialogUnitCantFightWarning(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.unitCantFightWarning_caption);
        showAndWait();
    }

}
