package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

/**
 * A one‑shot dialog that asks the user to confirm a declaration of war. The
 * dialog never mutates the game model – the caller is responsible for
 * validating the current state and invoking the model when the decision is
 * {@code true}.
 */
final class DeclarationDialog extends AbstractMessageWindow {

    private final I18n i18n;
    private final String actorLabel;
    private final String targetLabel;

    /**
     * Creates a new declaration dialog.
     *
     * @param viewUtil    shared window utilities
     * @param i18n        localized text provider
     * @param actorLabel  displayed declaring player's name
     * @param targetLabel displayed rival's name
     */
    DeclarationDialog(final ViewUtil viewUtil, final I18n i18n,
            final String actorLabel, final String targetLabel) {
        super(viewUtil, i18n);

        this.i18n = i18n;
        this.actorLabel = actorLabel;
        this.targetLabel = targetLabel;
    }

    /**
     * Shows the dialog if it has not been answered yet and returns the
     * decision. Subsequent calls return {@code false} immediately without
     * showing the UI again.
     *
     * @return whether this invocation received explicit confirmation
     */
    boolean confirm() {
        if (answered) {
            return false;
        }

        final VBox root = new VBox(12);
        root.setPrefWidth(440);
        root.setId("declarationReview");
        root.getChildren().add(new Label(i18n.get(Stats.declarationTitle)));

        final Label message = new Label(
                i18n.get(Stats.declarationMessage, actorLabel, targetLabel));
        message.setId("declarationMessage");
        message.setWrapText(true);
        message.setMaxWidth(440);
        root.getChildren().add(message);

        final HBox buttons = new HBox(12);
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Stats.declarationCancel));
        cancel.setId("declarationCancel");
        cancel.setCancelButton(true);
        cancel.setDefaultButton(true);
        cancel.setOnAction(e -> onCancelDialog());

        final Button confirm = ActionButtons.button(ButtonIcon.MILITARY,
                i18n.get(Stats.declarationConfirm));
        confirm.setId("declarationConfirm");
        confirm.setDefaultButton(false);
        confirm.setOnAction(e -> {
            if (!answered) {
                answered = true;
                confirmed = true;
                close();
            }
        });

        buttons.getChildren().addAll(cancel, confirm);
        root.getChildren().add(buttons);

        init(root);
        setTitle(i18n.get(Stats.declarationTitle));
        cancel.requestFocus();
        showAndWait();
        answered = true;
        return confirmed;
    }

    private boolean confirmed = false;
    private boolean answered = false;

    @Override
    protected void onCancelDialog() {
        answered = true;
        confirmed = false;
        close();
    }
}
