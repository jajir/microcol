package org.akropia.gui.dialog;

import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

/**
 * Dialog is shown when user try to move unit to illegal position. For example
 * ground unit move to ocean.
 */
public final class DialogUnitCantMoveHere extends AbstractWarningDialog {

    public DialogUnitCantMoveHere(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.unitCantMoveHere_caption);
        showAndWait();
    }

}
