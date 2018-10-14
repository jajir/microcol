package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

public class CampaignMenuPanel implements JavaFxComponent {

    private final MenuHolderPanel menuPanel;

    private final CampaignMenuPanelView campaignMenuPanelView;

    @Inject
    CampaignMenuPanel(final CampaignMenuPanelView campaignMenuPanelView,
            final MenuHolderPanel menuHolderPanel,
            final I18n i18n) {
        this.menuPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.campaignMenuPanelView = Preconditions.checkNotNull(campaignMenuPanelView);
        menuHolderPanel.setMenuPanel(campaignMenuPanelView.getContent());
        menuHolderPanel.setTitle(i18n.get(GameMenu.campaignTitle));
    }

    @Override
    public Region getContent() {
        return menuPanel.getContent();
    }

    public void refresh() {
        campaignMenuPanelView.refresh();
    }

}
