package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

/**
 * Warns that a confirmed European purchase was rejected because the treasury
 * changed after the choice was shown. Nothing was moved or charged.
 */
public final class DialogEuropeGoldChanged extends AbstractWarningDialog {

    @Inject
    public DialogEuropeGoldChanged(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.europeGold_changed);
    }

}
