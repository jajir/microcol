package org.akropia.gui.screen.setting;

import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.util.ApplicationInfo;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class SettingVersionView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final ApplicationInfo applicationInfo;

    private final Label label;

    @Inject
    public SettingVersionView(final I18n i18n,
            final ApplicationInfo applicationInfo) {
        this.applicationInfo = Preconditions.checkNotNull(applicationInfo);

        label = new Label();
        mainPanel = new HBox();
        mainPanel.getChildren().add(label);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        label.setText(i18n.get(GameMenu.settingVersion)
                + applicationInfo.getBuildVersion());
    }

}
