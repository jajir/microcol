package org.microcol.gui.gamemenu;

import org.microcol.gui.mainmenu.ShowGridController;
import org.microcol.gui.mainmenu.ShowGridEvent;

import com.google.inject.Inject;

public class SettingShowGridPresenter {

    @Inject
    SettingShowGridPresenter(final SettingShowGridView settingShowGridView,
            final ShowGridController showGridController) {
        settingShowGridView.getCheckBox().setOnAction(ectionEvent -> showGridController
                .fireEvent(new ShowGridEvent(settingShowGridView.getCheckBox().isSelected())));
    }

}
