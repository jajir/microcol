package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.event.ColonyWasCapturedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public final class DialogColonyWasCaptured extends AbstractWarningDialog {

    private final I18n i18n;

    @Inject
    public DialogColonyWasCaptured(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.colonyWasCaptured_caption);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    @Override
    public void showAndWait() {
        throw new IllegalStateException(
                "Don't call this method use method 'showAndWait' with parameters");
    }

    public void showAndWait(final ColonyWasCapturedEvent event) {
        getContext().getChildren().clear();
        getContext().getChildren().add(new Label(i18n.get(Dialog.colonyWasCaptured_text1)
                + event.getCapturedColony().getName() + i18n.get(Dialog.colonyWasCaptured_text2)));
        super.showAndWait();
    }

}
