package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractYesNoDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * Dialog ask player if he want to destroy colony.
 */
public final class DialogDestroyColony extends AbstractYesNoDialog {

    @Inject
    DialogDestroyColony(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, i18n.get(Dialog.destroyColony_caption));

        getContext().getChildren().add(new Label(i18n.get(Dialog.destroyColony_question)));
    }

}
