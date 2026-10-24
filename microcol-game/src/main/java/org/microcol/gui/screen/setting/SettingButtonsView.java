package org.microcol.gui.screen.setting;

import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class SettingButtonsView implements JavaFxComponent, UpdatableLanguage {

    public static final String BUTTON_BACK_ID = "buttonBack";

    private final Logger logger = LoggerFactory
            .getLogger(SettingButtonsView.class);

    private final Button buttonBack;

    private final VBox buttonsBox;

    private final SettingLanguageView settingLanguageView;
    private final SettingShowGridView settingShowGridView;
    private final SettingShowFightAdvisorView settingShowFightAdvisorView;
    private final SettingVolumeView settingVolumeView;
    private final SettingAnimationSpeedView settingAnimationSpeedView;
    private final SettingVersionView settingVersionView;
    private final CheckBox autosaveCheckBox;
    private final CheckBox turnSummaryCheckBox;
    private final CheckBox openEuropeCheckBox;

    @Inject
    SettingButtonsView(final SettingLanguageView settingLanguageView,
            final SettingShowGridView settingShowGridView,
            final SettingShowFightAdvisorView settingShowFightAdvisorView,
            final SettingVolumeView settingVolumeView,
            final SettingAnimationSpeedView settingAnimationSpeedView,
            final SettingVersionView settingVersionView,
            final GamePreferences gamePreferences, final I18n i18n) {
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

        buttonBack = new Button();
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
        buttonsBox.getChildren().add(settingVersionView.getContent());
        buttonsBox.getChildren().add(buttonBack);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return buttonsBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        logger.debug("Current language is: {}", i18n);
        buttonBack.setText(i18n.get(GameMenu.buttonBack));
        this.settingLanguageView.updateLanguage(i18n);
        this.settingShowGridView.updateLanguage(i18n);
        this.settingShowFightAdvisorView.updateLanguage(i18n);
        autosaveCheckBox.setText(i18n.get(GameMenu.settingAutosave));
        turnSummaryCheckBox.setText(i18n.get(GameMenu.settingTurnSummary));
        openEuropeCheckBox
                .setText(i18n.get(GameMenu.settingOpenEuropeOnArrival));
        this.settingVolumeView.updateLanguage(i18n);
        this.settingAnimationSpeedView.updateLanguage(i18n);
        this.settingVersionView.updateLanguage(i18n);
    }

    /**
     * @return the buttonBack
     */
    public Button getButtonBack() {
        return buttonBack;
    }

}
