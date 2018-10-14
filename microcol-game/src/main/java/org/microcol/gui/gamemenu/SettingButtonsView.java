package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class SettingButtonsView implements JavaFxComponent, UpdatableLanguage {

    private final Button buttonBack;

    private final VBox buttonsBox;

    private final SettingLanguageView settingLanguageView;
    private final SettingShowGridView settingShowGridView;
    private final SettingShowFightAdvisorView settingShowFightAdvisorView;
    private final SettingVolumeView settingVolumeView;
    private final SettingAnimationSpeedView settingAnimationSpeedView;

    @Inject
    SettingButtonsView(final SettingLanguageView settingLanguageView,
            final SettingShowGridView settingShowGridView,
            final SettingShowFightAdvisorView settingShowFightAdvisorView,
            final SettingVolumeView settingVolumeView,
            final SettingAnimationSpeedView settingAnimationSpeedView, final I18n i18n) {
        this.settingLanguageView = Preconditions.checkNotNull(settingLanguageView);
        this.settingShowGridView = Preconditions.checkNotNull(settingShowGridView);
        this.settingShowFightAdvisorView = Preconditions.checkNotNull(settingShowFightAdvisorView);
        this.settingVolumeView = Preconditions.checkNotNull(settingVolumeView);
        this.settingAnimationSpeedView = Preconditions.checkNotNull(settingAnimationSpeedView);
        
        buttonBack = new Button();

        buttonsBox = new VBox();
        buttonsBox.getStyleClass().add("game-menu-inner");
        buttonsBox.getChildren().add(settingLanguageView.getContent());
        buttonsBox.getChildren().add(settingShowGridView.getContent());
        buttonsBox.getChildren().add(settingShowFightAdvisorView.getContent());
        buttonsBox.getChildren().add(settingVolumeView.getContent());
        buttonsBox.getChildren().add(settingAnimationSpeedView.getContent());
        buttonsBox.getChildren().add(buttonBack);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return buttonsBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        buttonBack.setText(i18n.get(GameMenu.buttonBack));
        this.settingLanguageView.updateLanguage(i18n);
        this.settingShowGridView.updateLanguage(i18n);
        this.settingShowFightAdvisorView.updateLanguage(i18n);
        this.settingVolumeView.updateLanguage(i18n);
        this.settingAnimationSpeedView.updateLanguage(i18n);
    }

    /**
     * @return the buttonBack
     */
    public Button getButtonBack() {
        return buttonBack;
    }

}
