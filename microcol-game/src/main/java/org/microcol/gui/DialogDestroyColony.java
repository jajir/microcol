package org.microcol.gui;

import org.microcol.gui.util.AbstractYesNoDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * Dialog ask player if he want to destroy colony.
 */
public final class DialogDestroyColony extends AbstractYesNoDialog {

    @Inject
    DialogDestroyColony(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, text.get("dialogDestroyColony.caption"));

        getContext().getChildren().add(new Label(text.get("dialogDestroyColony.question")));
    }

    boolean isSelectedContinue() {
        return showWaitAndReturnIfYesWasSelected();
    }

}
