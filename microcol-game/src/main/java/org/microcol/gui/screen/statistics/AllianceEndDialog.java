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
 * One-shot confirmation of an explicit alliance termination. The dialog never
 * mutates the model; the caller revalidates the live state before acting on a
 * {@code true} decision.
 */
final class AllianceEndDialog extends AbstractMessageWindow {
    private final I18n i18n;
    private final String actorLabel;
    private final String partnerLabel;
    private final int waitRounds;
    private boolean confirmed;
    private boolean answered;

    AllianceEndDialog(final ViewUtil viewUtil, final I18n i18n,
            final String actorLabel, final String partnerLabel,
            final int waitRounds) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.actorLabel = actorLabel;
        this.partnerLabel = partnerLabel;
        this.waitRounds = waitRounds;
    }

    /** @return whether this invocation received explicit confirmation */
    boolean confirm() {
        if (answered) {
            return false;
        }
        final VBox root = new VBox(12);
        root.setPrefWidth(460);
        root.setId("allianceEndDialog");
        root.getChildren().add(new Label(i18n.get(Stats.allianceEndTitle)));
        final Label message = new Label(i18n.get(Stats.allianceEndMessage,
                actorLabel, partnerLabel, waitRounds));
        message.setId("allianceEndMessage");
        message.setWrapText(true);
        message.setMaxWidth(460);
        root.getChildren().add(message);
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Stats.allianceEndCancel));
        cancel.setId("allianceEndCancel");
        cancel.setCancelButton(true);
        cancel.setDefaultButton(true);
        cancel.setOnAction(e -> onCancelDialog());
        final Button end = ActionButtons.button(ButtonIcon.STOP,
                i18n.get(Stats.allianceEndConfirm));
        end.setId("allianceEndConfirm");
        end.setOnAction(e -> {
            if (!answered) {
                answered = true;
                confirmed = true;
                close();
            }
        });
        root.getChildren().add(new HBox(12, cancel, end));
        init(root);
        setTitle(i18n.get(Stats.allianceEndTitle));
        cancel.requestFocus();
        showAndWait();
        answered = true;
        return confirmed;
    }

    @Override
    protected void onCancelDialog() {
        answered = true;
        confirmed = false;
        close();
    }
}
