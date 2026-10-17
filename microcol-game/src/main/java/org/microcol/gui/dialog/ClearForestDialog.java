package org.microcol.gui.dialog;

import java.util.Optional;

import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.ClearForestOffer;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Confirmation for the exact, model-provided terms of one forest-clearing
 * order. The previewed colony is shown as an expectation only.
 */
public final class ClearForestDialog extends AbstractMessageWindow {

    private final I18n i18n;

    private boolean confirmed;

    @Inject
    ClearForestDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param offer   terms to confirm
     * @param preview name of the colony that would receive the lumber now
     * @return <code>true</code> only when the player explicitly confirmed
     */
    public boolean confirm(final ClearForestOffer offer,
            final Optional<String> preview) {
        confirmed = false;
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setPrefWidth(520);
        final Label title = new Label(i18n.get(Buttons.clearForestTitle));
        title.setId("clearForestTitle");
        final Label question = new Label(preview.isPresent()
                ? i18n.get(Buttons.clearForestQuestion, offer.getUpdates(),
                        offer.getTools(), offer.getLumber(), preview.get())
                : i18n.get(Buttons.clearForestQuestionNoColony,
                        offer.getUpdates(), offer.getTools(),
                        offer.getLumber()));
        question.setId("clearForestQuestion");
        question.setWrapText(true);
        question.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Buttons.clearForestConfirm),
                i18n.get(Buttons.clearForestCancel));
        buttons.getButtonYes().setId("clearForestConfirm");
        buttons.getButtonNo().setId("clearForestCancel");
        buttons.getButtonYes().setOnAction(event -> {
            confirmed = true;
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(title, question, buttons.getContent());
        init(root);
        setTitle(i18n.get(Buttons.clearForestTitle));
        showAndWait();
        return confirmed;
    }

    @Override
    protected void onCancelDialog() {
        confirmed = false;
        close();
    }

}
