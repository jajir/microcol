package org.microcol.gui;

import java.util.function.Consumer;

import org.microcol.gui.util.Text;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignDefault;
import org.microcol.model.campaign.CampaignManager;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public class CampaignPanelView implements CampaignPanelPresenter.Display {

    private final Text text;

    private final CampaignManager campaignManager;

    private final Button buttonBack;

    private final VBox box;
    
    private Consumer<String> onSelectedMission;

    @Inject
    CampaignPanelView(final Text text, final CampaignManager campaignManager) {
        this.text = Preconditions.checkNotNull(text);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        box = new VBox();
        box.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
        box.setAlignment(Pos.CENTER);
        buttonBack = new Button();
        setLocalizedText();
        refresh();
    }

    private void refresh() {
        box.getChildren().clear();
        final Campaign campaign = campaignManager.getCmapaignByName(CampaignDefault.NAME);
        campaign.getMissions().forEach(mission -> {
            final Button missionButton = new Button(
                    text.get("campaignPanel." + campaign.getName() + "." + mission.getName()));
            box.getChildren().add(missionButton);
            missionButton.setOnAction(event -> onSelectedMission(mission.getName()));
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

    public VBox getBox() {
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
     * @param onSelectedMission the onSelectedMission to set
     */
    @Override
    public void setOnSelectedMission(final Consumer<String> onSelectedMission) {
        this.onSelectedMission = onSelectedMission;
    }

}
