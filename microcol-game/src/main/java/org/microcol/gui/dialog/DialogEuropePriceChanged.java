package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

/**
 * Warns that a confirmed European trade was rejected because the price moved
 * after the quote was shown. Nothing was moved or charged.
 */
public final class DialogEuropePriceChanged extends AbstractWarningDialog {

    @Inject
    public DialogEuropePriceChanged(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.europePrice_changed);
    }

}
