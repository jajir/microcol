package org.microcol.gui.screen.campaign;

import java.util.function.Consumer;

import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public final class CampaignView
        implements CampaignPresenter.Display, JavaFxComponent, UpdatableLanguage {

    private final CampaignManager campaignManager;

    private final Button buttonBack;

    private final VBox box;

    private final I18n i18n;

    private Consumer<String> onSelectedMission;

    @Inject
    CampaignView(final CampaignManager campaignManager, final I18n i18n) {
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.i18n = Preconditions.checkNotNull(i18n);
        box = new VBox();
        box.getStyleClass().add("game-menu-inner");
        buttonBack = new Button();
        refresh();
    }

    public void refresh() {
        box.getChildren().clear();
        final Campaign campaign = campaignManager.getDefaultCampain();
        campaign.getMissions().forEach(mission -> {
            final Button missionButton = new Button(
                    i18n.get(GameMenu.get(campaign.getName(), mission.getName())));
            missionButton.setDisable(!campaign.isMissionEnabled(mission));
            missionButton.setOnAction(event -> onSelectedMission(mission.getName()));
            box.getChildren().add(missionButton);
        });
        box.getChildren().add(buttonBack);
    }

    private void onSelectedMission(final String missionName) {
        onSelectedMission.accept(missionName);
    }

    @Override
    public Region getContent() {
        return box;
    }

    /**
     * @return the buttonBack
     */
    @Override
    public Button getButtonBack() {
        return buttonBack;
    }

    /**
     * @param onSelectedMission
     *            the onSelectedMission to set
     */
    @Override
    public void setOnSelectedMission(final Consumer<String> onSelectedMission) {
        this.onSelectedMission = onSelectedMission;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        buttonBack.setText(i18n.get(GameMenu.campaignPanelButtonBack));
    }

}
