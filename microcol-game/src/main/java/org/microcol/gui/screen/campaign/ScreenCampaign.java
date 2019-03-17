package org.microcol.gui.screen.campaign;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.screen.menu.MenuHolderPanel;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

@Singleton
public class ScreenCampaign extends AbstractScreenMenu implements GameScreen {

    private final MenuHolderPanel menuHolderPanel;

    private final ScreenCampaignView campaignMenuPanelView;

    @Inject
    ScreenCampaign(final ScreenCampaignView campaignMenuPanelView,
            final MenuHolderPanel menuPanel) {
        this.menuHolderPanel = Preconditions.checkNotNull(menuPanel);
        this.campaignMenuPanelView = Preconditions.checkNotNull(campaignMenuPanelView);
        menuPanel.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        menuPanel.setMenuPanel(campaignMenuPanelView);
    }

    @Override
    public Region getContent() {
        return menuHolderPanel.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        campaignMenuPanelView.updateLanguage(i18n);
        System.out.println("prasopers " + i18n.get(GameMenu.campaignTitle));
        menuHolderPanel.setTitle(i18n.get(GameMenu.campaignTitle));
    }

    @Override
    public void beforeShow() {
        menuHolderPanel.beforeShow();
        campaignMenuPanelView.refresh();
    }

    @Override
    public void beforeHide() {
        menuHolderPanel.beforeHide();
    }

}
