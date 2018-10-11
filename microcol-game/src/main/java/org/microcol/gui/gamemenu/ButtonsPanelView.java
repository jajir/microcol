package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public final class ButtonsPanelView implements JavaFxComponent, UpdatableLanguage {

    private final Button buttonContinue;

    private final Button buttonLoadSave;

    private final Button buttonPlayCampaign;

    private final Button buttonStartFreeGame;

    private final Button buttonExitMicroCol;

    private final VBox mainBox;

    @Inject
    ButtonsPanelView(final I18n i18n) {
        buttonContinue = new Button();
        buttonLoadSave = new Button();
        buttonPlayCampaign = new Button();
        buttonStartFreeGame = new Button();
        buttonExitMicroCol = new Button();
        final VBox buttonsBox = new VBox();
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getChildren().add(buttonContinue);
        buttonsBox.getChildren().add(buttonLoadSave);
        buttonsBox.getChildren().add(buttonPlayCampaign);
        buttonsBox.getChildren().add(buttonStartFreeGame);
        buttonsBox.getChildren().add(buttonExitMicroCol);

        mainBox = new VBox();
        mainBox.setAlignment(Pos.CENTER);
        
        final HBox box = new HBox();
        box.getStyleClass().add("game-menu");
        box.setAlignment(Pos.CENTER);

        box.getChildren().add(buttonsBox);
        mainBox.getChildren().add(box);
        updateLanguage(i18n);
    }

    void setContinueEnabled(final boolean isEnabled) {
        buttonContinue.setDisable(!isEnabled);
    }

    void setFreeGameEnabled(final boolean isEnabled) {
        buttonStartFreeGame.setDisable(!isEnabled);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        buttonContinue.setText(i18n.get(GameMenu.buttonContinue));
        buttonLoadSave.setText(i18n.get(GameMenu.buttonLoadSave));
        buttonPlayCampaign.setText(i18n.get(GameMenu.buttonPlayCampaign));
        buttonStartFreeGame.setText(i18n.get(GameMenu.buttonFreeGame));
        buttonExitMicroCol.setText(i18n.get(GameMenu.buttonExitMicroCol));
    }

    public Button getButtonStartFreeGame() {
        return buttonStartFreeGame;
    }

    @Override
    public Region getContent() {
        return mainBox;
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
