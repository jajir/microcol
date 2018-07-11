package org.microcol.gui.gamemenu;

import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public final class GameMenuPanelView {

    private final Text text;

    private final Button buttonContinue;

    private final Button buttonLoadSave;

    private final Button buttonPlayCampaign;

    private final Button buttonStartFreeGame;

    private final Button buttonExitMicroCol;

    private final VBox box;

    @Inject
    GameMenuPanelView(final Text text) {
        this.text = Preconditions.checkNotNull(text);
        box = new VBox();
        box.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
        box.setAlignment(Pos.CENTER);
        buttonContinue = new Button();
        buttonLoadSave = new Button();
        buttonPlayCampaign = new Button();
        buttonStartFreeGame = new Button();
        buttonExitMicroCol = new Button();
        box.getChildren().add(buttonContinue);
        box.getChildren().add(buttonLoadSave);
        box.getChildren().add(buttonPlayCampaign);
        box.getChildren().add(buttonStartFreeGame);
        box.getChildren().add(buttonExitMicroCol);
        setLocalizedText();
    }

    void setContinueEnabled(final boolean isEnabled) {
        buttonContinue.setDisable(!isEnabled);
    }

    void setFreeGameEnabled(final boolean isEnabled) {
        buttonStartFreeGame.setDisable(!isEnabled);
    }

    public void updateLanguage() {
        setLocalizedText();
    }

    private void setLocalizedText() {
        buttonContinue.setText(text.get("startPanel.buttonContinue"));
        buttonLoadSave.setText(text.get("startPanel.buttonLoadSave"));
        buttonPlayCampaign.setText(text.get("startPanel.buttonPlayCampaign"));
        buttonStartFreeGame.setText(text.get("startPanel.buttonFreeGame"));
        buttonExitMicroCol.setText(text.get("startPanel.buttonExitMicroCol"));
    }

    public Button getButtonStartFreeGame() {
        return buttonStartFreeGame;
    }

    //TODO hide it
    public VBox getBox() {
        return box;
    }

    /**
     * @return the buttonContinue
     */
    public Button getButtonContinue() {
        return buttonContinue;
    }

    /**
     * @return the buttonLoadSave
     */
    public Button getButtonLoadSave() {
        return buttonLoadSave;
    }

    /**
     * @return the buttonPlayCampaign
     */
    public Button getButtonPlayCampaign() {
        return buttonPlayCampaign;
    }

    /**
     * @return the buttonExitMicroCol
     */
    public Button getButtonExitMicroCol() {
        return buttonExitMicroCol;
    }

}
