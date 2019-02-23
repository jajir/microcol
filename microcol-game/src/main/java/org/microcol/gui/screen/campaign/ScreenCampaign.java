package org.microcol.gui.screen.campaign;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.screen.menu.MenuHolderPanel;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

public class ScreenCampaign extends AbstractScreenMenu implements JavaFxComponent {

    private final MenuHolderPanel menuPanel;

    private final ScreenCampaignView campaignMenuPanelView;

    @Inject
    ScreenCampaign(final ScreenCampaignView campaignMenuPanelView,
            final MenuHolderPanel menuHolderPanel, final I18n i18n) {
        this.menuPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.campaignMenuPanelView = Preconditions.checkNotNull(campaignMenuPanelView);
        menuHolderPanel.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
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
