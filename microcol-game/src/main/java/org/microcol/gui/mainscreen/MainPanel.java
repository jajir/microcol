package org.microcol.gui.mainscreen;

import org.microcol.model.Colony;

public interface MainPanel {

    void showColony(final Colony colony);

    void showDefaultCampaignMenu();

    void showGamePanel();

    void showGameMenu();

    void showEurope();

    void showGameSetting();
}
