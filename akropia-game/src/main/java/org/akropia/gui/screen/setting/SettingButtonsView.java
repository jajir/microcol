package org.akropia.gui.screen.setting;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutText;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class SettingButtonsView implements JavaFxComponent, UpdatableLanguage {

    public static final String BUTTON_BACK_ID = "buttonBack";

    public static final String BUTTON_SHORTCUTS_ID = "settingKeyboardShortcuts";

    private final Logger logger = LoggerFactory
            .getLogger(SettingButtonsView.class);

    private final Button buttonBack;

    private final VBox buttonsBox;

    private final SettingLanguageView settingLanguageView;
    private final SettingShowGridView settingShowGridView;
    private final SettingShowFightAdvisorView settingShowFightAdvisorView;
    private final SettingVolumeView settingVolumeView;
    private final SettingAnimationSpeedView settingAnimationSpeedView;
    private final SettingMapScrollSpeedView settingMapScrollSpeedView;
    private final SettingVersionView settingVersionView;
    private final CheckBox autosaveCheckBox;
    private final CheckBox turnSummaryCheckBox;
    private final CheckBox openEuropeCheckBox;
    private final CheckBox developerCheckBox;
    private final Button shortcutsButton;
    private final KeyboardShortcuts shortcuts;
    private I18n currentI18n;
    private final javafx.scene.control.Label developerHeading = new javafx.scene.control.Label();
    private final javafx.scene.control.Label developerDescription = new javafx.scene.control.Label();

    @Inject
    SettingButtonsView(final SettingLanguageView settingLanguageView,
            final SettingShowGridView settingShowGridView,
            final SettingShowFightAdvisorView settingShowFightAdvisorView,
            final SettingVolumeView settingVolumeView,
            final SettingAnimationSpeedView settingAnimationSpeedView,
            final SettingMapScrollSpeedView settingMapScrollSpeedView,
            final SettingVersionView settingVersionView,
            final GamePreferences gamePreferences, final I18n i18n,
            final org.akropia.gui.preferences.DeveloperMode developerMode,
            final KeyboardShortcuts shortcuts,
            final Provider<KeyboardShortcutsDialog> shortcutsDialog) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        shortcutsButton = ActionButtons.button(ButtonIcon.SETTINGS);
        shortcutsButton.setId(BUTTON_SHORTCUTS_ID);
        shortcutsButton.setOnAction(event -> shortcutsDialog.get().open());
        // Saved shortcut changes rename the developer shortcut at once.
        shortcuts.addListener(() -> {
            if (currentI18n != null) {
                updateLanguage(currentI18n);
            }
        });
        this.settingMapScrollSpeedView = settingMapScrollSpeedView;
        this.settingLanguageView = Preconditions
                .checkNotNull(settingLanguageView);
        this.settingShowGridView = Preconditions
                .checkNotNull(settingShowGridView);
        this.settingShowFightAdvisorView = Preconditions
                .checkNotNull(settingShowFightAdvisorView);
        this.settingVolumeView = Preconditions.checkNotNull(settingVolumeView);
        this.settingAnimationSpeedView = Preconditions
                .checkNotNull(settingAnimationSpeedView);
        this.settingVersionView = Preconditions
                .checkNotNull(settingVersionView);
        autosaveCheckBox = new CheckBox();
        autosaveCheckBox.setId("autosaveEnabled");
        autosaveCheckBox.setSelected(gamePreferences.isAutosaveEnabled());
        autosaveCheckBox.setOnAction(event -> gamePreferences
                .setAutosaveEnabled(autosaveCheckBox.isSelected()));
        turnSummaryCheckBox = new CheckBox();
        turnSummaryCheckBox.setId("settingTurnSummary");
        turnSummaryCheckBox.setSelected(gamePreferences.isShowTurnSummary());
        turnSummaryCheckBox.setOnAction(event -> gamePreferences
                .setShowTurnSummary(turnSummaryCheckBox.isSelected()));
        openEuropeCheckBox = new CheckBox();
        openEuropeCheckBox.setId("settingOpenEuropeOnArrival");
        openEuropeCheckBox.setSelected(gamePreferences.isOpenEuropeOnArrival());
        openEuropeCheckBox.setOnAction(event -> gamePreferences
                .setOpenEuropeOnArrival(openEuropeCheckBox.isSelected()));

        developerCheckBox = new CheckBox();
        developerCheckBox.setId("settingDeveloperMode");
        developerCheckBox.setSelected(developerMode.isEnabled());
        developerCheckBox.setOnAction(event -> {
            try {
                developerMode.setEnabled(developerCheckBox.isSelected());
            } finally {
                developerCheckBox.setSelected(developerMode.isEnabled());
            }
        });
        developerMode.enabledProperty().addListener((property, before,
                after) -> developerCheckBox.setSelected(after));
        developerDescription.setWrapText(true);
        developerDescription.setMaxWidth(440);
        final VBox developerSection = new VBox(4, developerHeading,
                developerCheckBox, developerDescription);
        developerSection.setId("developerSettings");
        buttonBack = ActionButtons.button(ButtonIcon.BACK);
        buttonBack.setId(BUTTON_BACK_ID);

        buttonsBox = new VBox();
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getStyleClass().add("settings-controls");
        buttonsBox.getChildren().add(settingLanguageView.getContent());
        buttonsBox.getChildren().add(settingShowGridView.getContent());
        buttonsBox.getChildren().add(settingShowFightAdvisorView.getContent());
        buttonsBox.getChildren().add(autosaveCheckBox);
        buttonsBox.getChildren().add(turnSummaryCheckBox);
        buttonsBox.getChildren().add(openEuropeCheckBox);
        buttonsBox.getChildren().add(settingVolumeView.getContent());
        buttonsBox.getChildren().add(settingAnimationSpeedView.getContent());
        buttonsBox.getChildren().add(settingMapScrollSpeedView.getContent());
        buttonsBox.getChildren().add(shortcutsButton);
        buttonsBox.getChildren().add(developerSection);
        buttonsBox.getChildren().add(settingVersionView.getContent());

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return buttonsBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        logger.debug("Current language is: {}", i18n);
        currentI18n = i18n;
        shortcutsButton.setText(i18n.get(ShortcutText.open));
        buttonBack.setText(i18n.get(GameMenu.buttonBack));
        developerHeading.setText(i18n.get(GameMenu.settingDeveloperHeading));
        developerCheckBox.setText(
                shortcuts.withHint(i18n.get(GameMenu.settingDeveloperMode),
                        ShortcutAction.DEVELOPER_MODE));
        developerDescription
                .setText(i18n.get(GameMenu.settingDeveloperDescription));
        this.settingLanguageView.updateLanguage(i18n);
        this.settingShowGridView.updateLanguage(i18n);
        this.settingShowFightAdvisorView.updateLanguage(i18n);
        autosaveCheckBox.setText(i18n.get(GameMenu.settingAutosave));
        turnSummaryCheckBox.setText(i18n.get(GameMenu.settingTurnSummary));
        openEuropeCheckBox
                .setText(i18n.get(GameMenu.settingOpenEuropeOnArrival));
        this.settingVolumeView.updateLanguage(i18n);
        this.settingAnimationSpeedView.updateLanguage(i18n);
        this.settingMapScrollSpeedView.updateLanguage(i18n);
        this.settingVersionView.updateLanguage(i18n);
    }

    /**
     * @return the buttonBack
     */
    public Button getButtonBack() {
        return buttonBack;
    }

}
