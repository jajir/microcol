package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.GameOverResult;
import org.microcol.model.event.GameFinishedEvent;

import javafx.scene.control.Label;

public class DialogGameOver extends AbstractWarningDialog {

    public DialogGameOver(final ViewUtil viewUtil, final Text text, final GameFinishedEvent event) {
        super(viewUtil, text.get(KEY_DIALOG_OK), text.get("dialogGameOver.caption"));
        String str = "";
        final GameOverResult gameOverResult = event.getGameOverResult();
        if (GameOverResult.REASON_NO_COLONIES.equals(gameOverResult.getGameOverReason())) {
            str += text.get("dialogGameOver.allColoniesAreLost");
        } else if (GameOverResult.REASON_TIME_IS_UP.equals(gameOverResult.getGameOverReason())) {
            str += text.get("dialogGameOver.timeIsUp");
        } else {
            throw new IllegalArgumentException("Invalid game over reason: " + gameOverResult);
        }
        getContext().getChildren().add(new Label(str));
        showAndWait();
    }

}
