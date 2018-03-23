package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

/**
 * Show simple warning dialog with warning message.
 */
public class DialogNotEnoughGold extends AbstractWarningDialog {

    @Inject
    public DialogNotEnoughGold(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, "dialogNotEnoughGold.caption");
    }

}
