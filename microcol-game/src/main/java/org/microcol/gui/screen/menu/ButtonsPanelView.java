package org.microcol.gui.screen.menu;

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
    
    public static final String BUTTON_CONTINUE_ID = "buttonContinue";
    
    public static final String BUTTON_LOAD_ID = "buttonLoad";
    
    public static final String BUTTON_PLAY_CAMPAIGN_ID = "buttonCampaign";
    
    public static final String BUTTON_START_FREE_GAME_ID = "buttonStartFreeGame";
    
    public static final String BUTTON_EXIT_ID = "buttonExit";

    private final Button buttonContinue = new Button();

    private final Button buttonLoad = new Button();

    private final Button buttonPlayCampaign = new Button();

    private final Button buttonStartFreeGame = new Button();

    private final Button buttonSetting = new Button();

    private final Button buttonExitMicroCol = new Button();

    private final VBox buttonsBox = new VBox();

    @Inject
    ButtonsPanelView() {
        buttonContinue.setId(BUTTON_CONTINUE_ID);
        buttonLoad.setId(BUTTON_LOAD_ID);
        buttonPlayCampaign.setId(BUTTON_PLAY_CAMPAIGN_ID);
        buttonStartFreeGame.setId(BUTTON_START_FREE_GAME_ID);
        buttonSetting.setId(BUTTON_SETTING_ID);
        buttonExitMicroCol.setId(BUTTON_EXIT_ID);
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getChildren().add(buttonContinue);
        buttonsBox.getChildren().add(buttonLoad);
        buttonsBox.getChildren().add(buttonPlayCampaign);
        buttonsBox.getChildren().add(buttonStartFreeGame);
        buttonsBox.getChildren().add(buttonSetting);
        buttonsBox.getChildren().add(buttonExitMicroCol);
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
        buttonLoad.setText(i18n.get(GameMenu.buttonLoadSave));
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
    public Button getButtonLoad() {
        return buttonLoad;
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
