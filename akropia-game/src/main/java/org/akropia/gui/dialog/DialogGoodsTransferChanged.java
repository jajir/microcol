package org.akropia.gui.dialog;

import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

/**
 * Warns that a confirmed goods transfer was rejected because the target hold
 * changed after the quantity was chosen. Nothing was moved or charged.
 */
public final class DialogGoodsTransferChanged extends AbstractWarningDialog {

    @Inject
    public DialogGoodsTransferChanged(final ViewUtil viewUtil,
            final I18n i18n) {
        super(viewUtil, i18n, Dialog.goodsTransfer_changed);
    }

}
