package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

/**
 * Warns that a confirmed European purchase was rejected because market access
 * ended or the goods became boycotted after the choice was shown. Nothing was
 * moved or charged.
 */
public final class DialogEuropeTradeChanged extends AbstractWarningDialog {

    @Inject
    public DialogEuropeTradeChanged(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.europeTrade_changed);
    }

}
