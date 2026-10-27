package org.microcol.gui.screen.menu;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public final class ButtonsPanelView
        implements JavaFxComponent, UpdatableLanguage {

    public static final String BUTTON_SETTING_ID = "buttonSetting";

    public static final String BUTTON_CONTINUE_ID = "buttonContinue";

    public static final String BUTTON_RECOVERY_ID = "buttonRecovery";

    public static final String BUTTON_LOAD_ID = "buttonLoad";

    public static final String BUTTON_PLAY_CAMPAIGN_ID = "buttonCampaign";

    public static final String BUTTON_START_FREE_GAME_ID = "buttonStartFreeGame";

    public static final String BUTTON_EXIT_ID = "buttonExit";

    public static final String BUTTON_HALL_OF_FAME_ID = "buttonHallOfFame";

    private final Button buttonContinue = ActionButtons.button(ButtonIcon.PLAY);

    private final Button buttonRecovery = ActionButtons
            .button(ButtonIcon.RECOVER);

    private final Button buttonLoad = ActionButtons.button(ButtonIcon.FOLDER);

    private final Button buttonPlayCampaign = ActionButtons
            .button(ButtonIcon.CAMPAIGN);

    private final Button buttonStartFreeGame = ActionButtons
            .button(ButtonIcon.NEW_GAME);

    private final Button buttonHallOfFame = ActionButtons
            .button(ButtonIcon.TROPHY);

    private final Button buttonSetting = ActionButtons
            .button(ButtonIcon.SETTINGS);

    private final Button buttonExitMicroCol = ActionButtons
            .button(ButtonIcon.EXIT);

    private final VBox buttonsBox = new VBox();

    @Inject
    ButtonsPanelView(final ImageProvider images) {
        buttonContinue.setId(BUTTON_CONTINUE_ID);
        buttonRecovery.setId(BUTTON_RECOVERY_ID);
        buttonLoad.setId(BUTTON_LOAD_ID);
        buttonPlayCampaign.setId(BUTTON_PLAY_CAMPAIGN_ID);
        buttonStartFreeGame.setId(BUTTON_START_FREE_GAME_ID);
        buttonHallOfFame.setId(BUTTON_HALL_OF_FAME_ID);
        buttonSetting.setId(BUTTON_SETTING_ID);
        buttonExitMicroCol.setId(BUTTON_EXIT_ID);
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getChildren().add(buttonContinue);
        buttonsBox.getChildren().add(buttonRecovery);
        buttonsBox.getChildren().add(buttonLoad);
        buttonsBox.getChildren().add(buttonPlayCampaign);
        buttonsBox.getChildren().add(buttonStartFreeGame);
        buttonsBox.getChildren().add(buttonHallOfFame);
        buttonsBox.getChildren().add(buttonSetting);
        buttonsBox.getChildren().add(buttonExitMicroCol);
        final Image icons = images.getImage("menu-icons.png");
        for (int index = 0; index < buttonsBox.getChildren().size(); index++) {
            final Button button = (Button) buttonsBox.getChildren().get(index);
            button.getStyleClass().add("game-toolbar-control");
            button.setMinWidth(0);
            button.setMaxWidth(Region.USE_PREF_SIZE);
            final ImageView icon = new ImageView(icons);
            icon.setViewport(new Rectangle2D(index * 96, 0, 96, 96));
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            icon.setMouseTransparent(true);
            button.setGraphic(icon);
            final Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(button.textProperty());
            button.setTooltip(tooltip);
            button.accessibleTextProperty().bind(button.textProperty());
            buttonsBox.widthProperty()
                    .addListener(observable -> updateLabel(button));
            button.fontProperty()
                    .addListener(observable -> updateLabel(button));
            button.insetsProperty()
                    .addListener(observable -> updateLabel(button));
            button.textProperty()
                    .addListener(observable -> updateLabel(button));
        }
    }

    private void updateLabel(final Button button) {
        final Text label = new Text(button.getText());
        label.setFont(button.getFont());
        final double requiredWidth = label.getLayoutBounds().getWidth() + 32
                + button.getGraphicTextGap() + button.getInsets().getLeft()
                + button.getInsets().getRight();
        button.setPrefWidth(Math.ceil(requiredWidth));
        button.setContentDisplay(
                requiredWidth <= buttonsBox.getWidth() ? ContentDisplay.LEFT
                        : ContentDisplay.GRAPHIC_ONLY);
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
        buttonRecovery.setText(i18n.get(GameMenu.buttonRecovery));
        buttonLoad.setText(i18n.get(GameMenu.buttonLoadSave));
        buttonPlayCampaign.setText(i18n.get(GameMenu.buttonPlayCampaign));
        buttonStartFreeGame.setText(i18n.get(GameMenu.buttonFreeGame));
        buttonHallOfFame.setText(i18n.get(GameMenu.buttonHallOfFame));
        buttonSetting.setText(i18n.get(GameMenu.buttonSetting));
        buttonExitMicroCol.setText(i18n.get(GameMenu.buttonExitMicroCol));
    }

    public Button getButtonStartFreeGame() {
        return buttonStartFreeGame;
    }

    /**
     * @return the button that opens the Hall of Fame (R-0197)
     */
    public Button getButtonHallOfFame() {
        return buttonHallOfFame;
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

    public Button getButtonRecovery() {
        return buttonRecovery;
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
