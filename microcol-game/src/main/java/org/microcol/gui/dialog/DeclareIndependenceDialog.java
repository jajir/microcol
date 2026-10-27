package org.microcol.gui.dialog;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explains the consequences of independence before the model is asked to
 * declare it. Cancel, Escape and closing the window change nothing; the model
 * validates the live state again when the declaration is executed.
 */
@Singleton
public final class DeclareIndependenceDialog extends AbstractMessageWindow {

    public static final String DIALOG_ID = "declareIndependenceDialog";
    public static final String CONFIRM_ID = "declareIndependenceConfirm";
    public static final String CANCEL_ID = "declareIndependenceCancel";

    private final Label support = new Label();
    private final Label muster = new Label();
    private final Label europe = new Label();
    private final Label irreversible = new Label();
    private final Label feedback = new Label();
    private final Button confirm = ActionButtons
            .button(ButtonIcon.INDEPENDENCE);
    private final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
    private final GameModelController controller;
    private final I18n i18n;
    private Model openedModel;
    private Player player;
    private boolean confirmed;

    @Inject
    public DeclareIndependenceDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.controller = controller;
        final VBox root = new VBox(12);
        root.setId(DIALOG_ID);
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);
        support.setId("declareIndependenceSupport");
        muster.setId("declareIndependenceMuster");
        europe.setId("declareIndependenceEurope");
        irreversible.setId("declareIndependenceIrreversible");
        feedback.setId("declareIndependenceFeedback");
        for (final Label label : new Label[] { support, muster, europe,
                irreversible, feedback }) {
            label.setWrapText(true);
        }
        confirm.setId(CONFIRM_ID);
        cancel.setId(CANCEL_ID);
        confirm.setOnAction(event -> onConfirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        root.getChildren().addAll(support, muster, europe, irreversible,
                feedback, new HBox(12, confirm, cancel));
        init(root);
    }

    /**
     * Ask the human player to confirm.
     *
     * @return true only when the player confirmed while the declaration was
     *         still allowed; false for cancel, Escape or a refused state
     */
    public boolean showAndWaitForConfirmation() {
        confirmed = false;
        openedModel = controller.getModel();
        player = controller.getHumanPlayer();
        feedback.setText("");
        confirm.setDisable(false);
        if (!player.isPossibleToDecalareIndependence()) {
            return false;
        }
        try {
            refresh();
            showAndWait();
            return confirmed;
        } finally {
            openedModel = null;
            player = null;
        }
    }

    private void refresh() {
        setTitle(i18n.get(DeclareIndependenceText.title));
        confirm.setText(i18n.get(DeclareIndependenceText.confirm));
        cancel.setText(i18n.get(DeclareIndependenceText.cancel));
        support.setText(i18n.get(DeclareIndependenceText.support,
                player.getNationalRebelSupport().getDisplayPercent()));
        final int mustered = player.getIndependenceMusterPreview().size();
        muster.setText(mustered > 0
                ? i18n.get(DeclareIndependenceText.muster, mustered)
                : i18n.get(DeclareIndependenceText.musterNone));
        europe.setText(i18n.get(DeclareIndependenceText.europe));
        irreversible.setText(i18n.get(DeclareIndependenceText.irreversible));
    }

    private void onConfirm() {
        if (controller.isGameModelReady()
                && controller.getModel() == openedModel
                && player.isPossibleToDecalareIndependence()) {
            confirmed = true;
            close();
            return;
        }
        // The state changed while the dialog was open: explain, change nothing.
        confirm.setDisable(true);
        feedback.setText(i18n.get(DeclareIndependenceText.changed));
    }
}
