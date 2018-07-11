package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.event.ColonyWasCapturedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public final class DialogColonyWasCaptured extends AbstractWarningDialog {

    private final Text text;

    @Inject
    public DialogColonyWasCaptured(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, text.get(KEY_DIALOG_OK), text.get("dialogColonyWasCaptured.caption"));
        this.text = Preconditions.checkNotNull(text);
    }

    @Override
    public void showAndWait() {
        throw new IllegalStateException(
                "Don't call this method use method 'showAndWait' with parameters");
    }

    public void showAndWait(final ColonyWasCapturedEvent event) {
        getContext().getChildren().clear();
        getContext().getChildren().add(new Label(text.get("dialogColonyWasCaptured.text1")
                + event.getCapturedColony().getName() + text.get("dialogColonyWasCaptured.text2")));
        super.showAndWait();
    }

}
