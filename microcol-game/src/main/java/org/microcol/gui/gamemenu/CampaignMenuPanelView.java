package org.microcol.gui.gamemenu;

import java.util.function.Consumer;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Text;
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
public final class CampaignMenuPanelView
        implements CampaignMenuPanelPresenter.Display, JavaFxComponent {

    private final Text text;

    private final CampaignManager campaignManager;

    private final Button buttonBack;

    private final VBox box;

    private Consumer<String> onSelectedMission;

    @Inject
    CampaignMenuPanelView(final Text text, final CampaignManager campaignManager) {
        this.text = Preconditions.checkNotNull(text);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        box = new VBox();
        box.getStyleClass().add("game-menu-inner");
        buttonBack = new Button();
        setLocalizedText();
        refresh();
    }

    public void refresh() {
        box.getChildren().clear();
        final Campaign campaign = campaignManager.getDefaultCampain();
        campaign.getMissions().forEach(mission -> {
            final Button missionButton = new Button(text.get(
                    "campaignPanel." + campaign.getName().toString() + "." + mission.getName()));
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
    public void updateLanguage() {
        setLocalizedText();
    }

    private void setLocalizedText() {
        buttonBack.setText(text.get("campaignPanel.buttonBack"));
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

}
