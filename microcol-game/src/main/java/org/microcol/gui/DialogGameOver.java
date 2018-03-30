package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.GameOverResult;
import org.microcol.model.campaign.DefaultMissionFindNewWold;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public class DialogGameOver extends AbstractWarningDialog {

    private final Text text;

    @Inject
    public DialogGameOver(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text.get(KEY_DIALOG_OK), text.get("dialogGameOver.caption"));
        this.text = Preconditions.checkNotNull(text);
    }

    public void setGameOverEvent(final GameFinishedEvent event) {
        String str = "";
        getContext().getChildren().clear();
        final GameOverResult gameOverResult = event.getGameOverResult();
        if (GameOverResult.REASON_NO_COLONIES.equals(gameOverResult.getGameOverReason())) {
            str += text.get("dialogGameOver.allColoniesAreLost");
        } else if (GameOverResult.REASON_TIME_IS_UP.equals(gameOverResult.getGameOverReason())) {
            str += text.get("dialogGameOver.timeIsUp");
        } else if (DefaultMissionFindNewWold.GAME_OVER_REASON
                .equals(gameOverResult.getGameOverReason())) {
            str += text.get("campaign.default.gameOver");
        } else {
            throw new IllegalArgumentException("Invalid game over reason: " + gameOverResult);
        }
        getContext().getChildren().add(new Label(str));
    }

}
