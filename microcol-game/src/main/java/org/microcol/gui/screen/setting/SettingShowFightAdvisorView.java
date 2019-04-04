package org.microcol.gui.screen.setting;

import org.microcol.gui.preferences.PreferencesProperties;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class SettingShowFightAdvisorView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final CheckBox checkBox;

    @Inject
    public SettingShowFightAdvisorView(final PreferencesProperties preferencesProperties,
            final I18n i18n) {

        checkBox = new CheckBox();
        checkBox.setSelected(preferencesProperties.getShowFightAdvisorProperty().get());
        preferencesProperties.getShowFightAdvisorProperty()
                .bindBidirectional(checkBox.selectedProperty());

        mainPanel = new HBox();
        mainPanel.getChildren().add(checkBox);

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
