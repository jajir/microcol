package org.microcol.gui.gamemenu;

import org.microcol.gui.MainPanelPresenter;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ShowDefaultCampaignMenuListener {

    private final MainPanelPresenter mainPanelPresenter;

    @Inject
    public ShowDefaultCampaignMenuListener(
            final ShowDefaultCampaignMenuControler showDefaultCampaignMenuControler,
            final MainPanelPresenter mainPanelPresenter) {
        this.mainPanelPresenter = Preconditions.checkNotNull(mainPanelPresenter);
        showDefaultCampaignMenuControler.addListener(this::onShowDefaultCampaignMenu);
    }

    @SuppressWarnings("unused")
    private void onShowDefaultCampaignMenu(final ShowDefaultCampaignMenuEvent event) {
        mainPanelPresenter.showDefaultCampaignMenu();
    }

}
