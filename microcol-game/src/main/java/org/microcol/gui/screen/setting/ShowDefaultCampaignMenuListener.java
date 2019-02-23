package org.microcol.gui.screen.setting;

import org.microcol.gui.screen.MainPanelPresenter;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class ShowDefaultCampaignMenuListener {

    private final MainPanelPresenter mainPanelPresenter;

    @Inject
    public ShowDefaultCampaignMenuListener(final MainPanelPresenter mainPanelPresenter) {
        this.mainPanelPresenter = Preconditions.checkNotNull(mainPanelPresenter);
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onShowDefaultCampaignMenu(final ShowDefaultCampaignMenuEvent event) {
        mainPanelPresenter.showDefaultCampaignMenu();
    }

}
