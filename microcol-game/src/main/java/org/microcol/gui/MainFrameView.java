package org.microcol.gui;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.VBox;

/**
 * MicroCol's main frame.
 */
public class MainFrameView implements MainFramePresenter.Display {

    private final VBox box;

    private final MainPanelView mainPanelView;

    private final StartPanelView startPanelView;

    private final CampaignPanelView campaignPanelView;

    @Inject
    public MainFrameView(final MainPanelView mainPanelView, final StartPanelView startPanelView,
            final CampaignPanelView campaignPanelView) {
        box = new VBox();
        this.mainPanelView = Preconditions.checkNotNull(mainPanelView);
        this.startPanelView = Preconditions.checkNotNull(startPanelView);
        this.campaignPanelView = Preconditions.checkNotNull(campaignPanelView);
    }

    @Override
    public void showPanel(final String panelName) {
        box.getChildren().clear();
        if (MainFramePresenter.MAIN_GAME_PANEL.equals(panelName)) {
            box.getChildren().add(mainPanelView.getBox());
        } else if (MainFramePresenter.START_PANEL.equals(panelName)) {
            box.getChildren().add(startPanelView.getBox());
        } else if (MainFramePresenter.CAMPAIGN_PANEL.equals(panelName)) {
            box.getChildren().add(campaignPanelView.getBox());
        } else {
            throw new IllegalArgumentException(String.format("Invalid panel name (%s)", panelName));
        }
    }

    @Override
    public VBox getBox() {
        return box;
    }

}
