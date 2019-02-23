package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

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
