package org.microcol.gui.screen.europe;

import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.VoyageReversalOffer;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Confirmation for an exact, model-provided reversal offer. */
public final class VoyageReversalDialog extends AbstractMessageWindow {
    private final I18n i18n;
    private boolean confirmed;

    @Inject
    VoyageReversalDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    public boolean confirm(final VoyageReversalOffer offer) {
        confirmed = false;
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        final Label title = new Label(i18n.get(Europe.voyageReverseTitle));
        title.setId("voyageReverseTitle");
        final String destination = i18n.get(
                offer.isNewDestinationEurope() ? Europe.voyageDestinationEurope
                        : Europe.voyageDestinationNewWorld);
        final Label question = new Label(i18n.get(Europe.voyageReverseQuestion,
                destination, offer.getRemainingAfterReverse()));
        question.setId("voyageReverseQuestion");
        question.setWrapText(true);
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Europe.voyageReverse), i18n.get(Dialog.cancel));
        buttons.getButtonYes().setId("voyageReverseConfirm");
        buttons.getButtonNo().setId("voyageReverseCancel");
        buttons.getButtonYes().setOnAction(event -> {
            confirmed = true;
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(title, question, buttons.getContent());
        init(root);
        setTitle(i18n.get(Europe.voyageReverseTitle));
        showAndWait();
        return confirmed;
    }

    @Override
    protected void onCancelDialog() {
        confirmed = false;
        close();
    }
}
