package org.microcol.gui.screen.setting;

import org.microcol.gui.preferences.GamePreferences;
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
public class SettingShowGridView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final CheckBox checkBox;

    @Inject
    public SettingShowGridView(final GamePreferences gamePreferences, final I18n i18n) {

        checkBox = new CheckBox();
        checkBox.setSelected(gamePreferences.isGridShown());

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
        checkBox.setText(i18n.get(GameMenu.settingShowGrid));
    }

    /**
     * @return the checkBox
     */
    public CheckBox getCheckBox() {
        return checkBox;
    }

}
