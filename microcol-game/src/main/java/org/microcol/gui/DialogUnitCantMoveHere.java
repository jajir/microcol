package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

/**
 * Dialog is shown when user try to move unit to illegal position. For example
 * ground unit move to ocean.
 */
public final class DialogUnitCantMoveHere extends AbstractWarningDialog {

    public DialogUnitCantMoveHere(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, "dialogUnitCantMoveHere.caption");
        showAndWait();
    }

}
