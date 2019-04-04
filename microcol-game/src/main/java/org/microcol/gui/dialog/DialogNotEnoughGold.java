package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

/**
 * Show simple warning dialog with warning message.
 */
public final class DialogNotEnoughGold extends AbstractWarningDialog {

    @Inject
    public DialogNotEnoughGold(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.notEnoughGold_caption);
    }

}
