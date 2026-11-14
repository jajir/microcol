package org.akropia.gui.dialog;

import org.akropia.gui.util.AbstractYesNoDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * Dialog ask player if he want to destroy colony.
 */
public final class DialogDestroyColony extends AbstractYesNoDialog {

    @Inject
    DialogDestroyColony(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, i18n.get(Dialog.destroyColony_caption),
                i18n.get(Dialog.destroyColony_confirm));

        getContext().getChildren()
                .add(new Label(i18n.get(Dialog.destroyColony_question)));
    }

}
