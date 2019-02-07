package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public final class ButtonsPanelView implements JavaFxComponent, UpdatableLanguage {
    
    public static final String BUTTON_SETTING_ID = "buttonSetting";

    private final Button buttonContinue;

    private final Button buttonLoadSave;

    private final Button buttonPlayCampaign;

    private final Button buttonStartFreeGame;

    private final Button buttonSetting;

    private final Button buttonExitMicroCol;

    private final VBox buttonsBox;

    @Inject
    ButtonsPanelView(final I18n i18n) {
        buttonContinue = new Button();
        buttonLoadSave = new Button();
        buttonPlayCampaign = new Button();
        buttonStartFreeGame = new Button();
        buttonSetting = new Button();
        buttonSetting.setId(BUTTON_SETTING_ID);
        buttonExitMicroCol = new Button();
        buttonsBox = new VBox();
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getChildren().add(buttonContinue);
        buttonsBox.getChildren().add(buttonLoadSave);
        buttonsBox.getChildren().add(buttonPlayCampaign);
        buttonsBox.getChildren().add(buttonStartFreeGame);
        buttonsBox.getChildren().add(buttonSetting);
        buttonsBox.getChildren().add(buttonExitMicroCol);

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
        buttonSetting.setText(i18n.get(GameMenu.buttonSetting));
        buttonExitMicroCol.setText(i18n.get(GameMenu.buttonExitMicroCol));
    }

    public Button getButtonStartFreeGame() {
        return buttonStartFreeGame;
    }

    @Override
    public Region getContent() {
        return buttonsBox;
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

    /**
     * @return the buttonSetting
     */
    public Button getButtonSetting() {
        return buttonSetting;
    }

}
