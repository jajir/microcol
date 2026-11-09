package org.akropia.gui.screen.menu;

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutButtons;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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

    public static final String BUTTON_LOAD_ID = "buttonLoad";

    public static final String BUTTON_PLAY_CAMPAIGN_ID = "buttonCampaign";

    public static final String BUTTON_START_FREE_GAME_ID = "buttonStartFreeGame";

    public static final String BUTTON_EXIT_ID = "buttonExit";

    public static final String BUTTON_HALL_OF_FAME_ID = "buttonHallOfFame";

    public static final String BUTTON_MAP_EDITOR_ID = "buttonMapEditor";

    /** Horizontal gap between Settings and the Map editor entry. */
    private static final double MAP_EDITOR_GAP = 8;

    /**
     * Columns of menu-icons.png in button order; column 1 holds the recovery
     * artwork, which no longer has a main-menu entry (R-0376).
     */
    private static final int[] ICON_COLUMNS = { 0, 2, 3, 4, 5, 6, 7 };

    private final Button buttonContinue = ActionButtons.button(ButtonIcon.PLAY);

    private final Button buttonLoad = ActionButtons.button(ButtonIcon.FOLDER);

    private final Button buttonPlayCampaign = ActionButtons
            .button(ButtonIcon.CAMPAIGN);

    private final Button buttonStartFreeGame = ActionButtons
            .button(ButtonIcon.NEW_GAME);

    private final Button buttonHallOfFame = ActionButtons
            .button(ButtonIcon.TROPHY);

    private final Button buttonSetting = ActionButtons
            .button(ButtonIcon.SETTINGS);

    private final Button buttonExitAkropia = ActionButtons
            .button(ButtonIcon.EXIT);

    /** Developer-only entry beside Settings (R-0435). */
    private final Button buttonMapEditor = ActionButtons
            .button(ButtonIcon.LAND);

    private final VBox buttonsBox = new VBox();

    private final KeyboardShortcuts shortcuts;

    private I18n currentI18n;

    @Inject
    ButtonsPanelView(final ImageProvider images,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = shortcuts;
        // Saved shortcut changes rename the Load button at once.
        shortcuts.addListener(() -> {
            if (currentI18n != null) {
                updateLanguage(currentI18n);
            }
        });
        final java.util.Map<ShortcutAction, java.util.function.Supplier<? extends Button>> commands = new java.util.EnumMap<>(
                ShortcutAction.class);
        commands.put(ShortcutAction.NEW_FREE_GAME, () -> buttonStartFreeGame);
        commands.put(ShortcutAction.PLAY_CAMPAIGN, () -> buttonPlayCampaign);
        commands.put(ShortcutAction.HALL_OF_FAME, () -> buttonHallOfFame);
        ShortcutButtons.install(buttonsBox, shortcuts, commands);
        buttonContinue.setId(BUTTON_CONTINUE_ID);
        buttonLoad.setId(BUTTON_LOAD_ID);
        buttonPlayCampaign.setId(BUTTON_PLAY_CAMPAIGN_ID);
        buttonStartFreeGame.setId(BUTTON_START_FREE_GAME_ID);
        buttonHallOfFame.setId(BUTTON_HALL_OF_FAME_ID);
        buttonSetting.setId(BUTTON_SETTING_ID);
        buttonExitAkropia.setId(BUTTON_EXIT_ID);
        buttonMapEditor.setId(BUTTON_MAP_EDITOR_ID);
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonMapEditor.getStyleClass().add("game-toolbar-control");
        buttonMapEditor.setMinWidth(0);
        buttonMapEditor.setMaxWidth(Region.USE_PREF_SIZE);
        buttonMapEditor.accessibleTextProperty()
                .bind(buttonMapEditor.textProperty());
        // The column lays out only its seven entries; the developer-only
        // entry follows Settings in focus order and stands right of it.
        buttonMapEditor.setManaged(false);
        buttonSetting.boundsInParentProperty()
                .addListener(observable -> placeMapEditor());
        buttonMapEditor.textProperty()
                .addListener(observable -> placeMapEditor());
        buttonMapEditor.fontProperty()
                .addListener(observable -> placeMapEditor());
        buttonsBox.getChildren().add(buttonContinue);
        buttonsBox.getChildren().add(buttonLoad);
        buttonsBox.getChildren().add(buttonPlayCampaign);
        buttonsBox.getChildren().add(buttonStartFreeGame);
        buttonsBox.getChildren().add(buttonHallOfFame);
        buttonsBox.getChildren().add(buttonSetting);
        buttonsBox.getChildren().add(buttonMapEditor);
        buttonsBox.getChildren().add(buttonExitAkropia);
        final Image icons = images.getImage("menu-icons.png");
        final Button[] buttons = { buttonContinue, buttonLoad,
                buttonPlayCampaign, buttonStartFreeGame, buttonHallOfFame,
                buttonSetting, buttonExitAkropia };
        for (int index = 0; index < buttons.length; index++) {
            final Button button = buttons[index];
            button.getStyleClass().add("game-toolbar-control");
            button.setMinWidth(0);
            button.setMaxWidth(Region.USE_PREF_SIZE);
            final ImageView icon = new ImageView(icons);
            icon.setViewport(
                    new Rectangle2D(ICON_COLUMNS[index] * 96, 0, 96, 96));
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            icon.setMouseTransparent(true);
            button.setGraphic(icon);
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

    private void placeMapEditor() {
        final javafx.geometry.Bounds setting = buttonSetting
                .getBoundsInParent();
        buttonMapEditor.autosize();
        buttonMapEditor.relocate(setting.getMaxX() + MAP_EDITOR_GAP,
                buttonSetting.getLayoutY());
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
        currentI18n = i18n;
        buttonContinue.setText(i18n.get(GameMenu.buttonContinue));
        buttonLoad.setText(shortcuts.withHint(i18n.get(GameMenu.buttonLoadSave),
                ShortcutAction.LOAD_GAME));
        buttonPlayCampaign.setText(i18n.get(GameMenu.buttonPlayCampaign));
        buttonStartFreeGame.setText(i18n.get(GameMenu.buttonFreeGame));
        buttonHallOfFame.setText(i18n.get(GameMenu.buttonHallOfFame));
        buttonSetting.setText(i18n.get(GameMenu.buttonSetting));
        buttonMapEditor.setText(i18n.get(GameMenu.buttonMapEditor));
        buttonExitAkropia.setText(i18n.get(GameMenu.buttonExitAkropia));
        // The menu has no status line and no floating tooltips; the visible
        // labels stay compact and assistive technology names the key.
        buttonStartFreeGame.setAccessibleHelp(shortcuts.withHint(
                buttonStartFreeGame.getText(), ShortcutAction.NEW_FREE_GAME));
        buttonPlayCampaign.setAccessibleHelp(shortcuts.withHint(
                buttonPlayCampaign.getText(), ShortcutAction.PLAY_CAMPAIGN));
        buttonHallOfFame.setAccessibleHelp(shortcuts.withHint(
                buttonHallOfFame.getText(), ShortcutAction.HALL_OF_FAME));
        buttonSetting.setAccessibleHelp(shortcuts.withHint(
                buttonSetting.getText(), ShortcutAction.OPEN_SETTINGS));
        buttonExitAkropia.setAccessibleHelp(shortcuts.withHint(
                buttonExitAkropia.getText(), ShortcutAction.QUIT_GAME));
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
     * @return the buttonExitAkropia
     */
    public Button getButtonExitAkropia() {
        return buttonExitAkropia;
    }

    /**
     * @return the developer-only Map editor button
     */
    public Button getButtonMapEditor() {
        return buttonMapEditor;
    }

    /**
     * @return the buttonSetting
     */
    public Button getButtonSetting() {
        return buttonSetting;
    }

}
