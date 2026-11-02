package org.akropia.gui.dialog;

import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

public class DialogUnitCantFightWarning extends AbstractWarningDialog {

    public DialogUnitCantFightWarning(final ViewUtil viewUtil,
            final I18n i18n) {
        super(viewUtil, i18n, Dialog.unitCantFightWarning_caption);
        showAndWait();
    }

}
