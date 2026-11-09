package org.akropia.gui.screen.setting;

import org.akropia.gui.preferences.PreferencesProperties;
import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class SettingShowFightAdvisorView
        implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final CheckBox checkBox;

    @Inject
    public SettingShowFightAdvisorView(
            final PreferencesProperties preferencesProperties,
            final I18n i18n) {

        checkBox = new CheckBox();
        checkBox.setId("settingFightAdvisor");
        checkBox.setSelected(
                preferencesProperties.getShowFightAdvisorProperty().get());
        preferencesProperties.getShowFightAdvisorProperty()
                .bindBidirectional(checkBox.selectedProperty());

        mainPanel = SettingRow.check(checkBox);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        checkBox.setText(i18n.get(GameMenu.settingShowFightAdvisor));
    }

}
