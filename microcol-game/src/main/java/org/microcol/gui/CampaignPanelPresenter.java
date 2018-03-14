package org.microcol.gui;

import java.util.function.Consumer;

import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
public class CampaignPanelPresenter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public interface Display {

        void updateLanguage();

        Button getButtonBack();

        void setOnSelectedMission(final Consumer<String> onSelectedMission);

    }

    @Inject
    public CampaignPanelPresenter(final CampaignPanelPresenter.Display display,
            final ChangeLanguageController changeLanguageController,
            final MainFramePresenter mainFramePresenter) {
        changeLanguageController.addListener(listener -> display.updateLanguage());
        display.getButtonBack()
                .setOnAction(event -> mainFramePresenter.showPanel(MainFramePresenter.START_PANEL));
        display.setOnSelectedMission(this::onSelectedMission);
    }

    private void onSelectedMission(final String misssionName) {
        logger.debug("Mission '%s' was selected to play.", misssionName);
    }

}
