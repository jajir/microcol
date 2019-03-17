package org.microcol.gui.screen;

import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.screen.campaign.ScreenCampaign;
import org.microcol.gui.screen.colony.ScreenColony;
import org.microcol.gui.screen.europe.ScreenEurope;
import org.microcol.gui.screen.game.ScreenGame;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.gui.screen.setting.ScreenSetting;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.ChainOfCommandStrategy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * MicroCol's main panel. Based on commands and events just change between main
 * game panel and game menu.
 * <p>
 * WASD control could be in game panel, but sometimes event are not correctly
 * catch by game panel.
 * </p>
 */
@Listener
@Singleton
public final class MainPanelPresenter {

    private final MainPanelView view;

    private final I18n i18n;

    private final ChainOfCommandStrategy<ShowScreenEvent, GameScreen> screenResolver;

    private GameScreen shownScreen;

    @Inject
    public MainPanelPresenter(final MainPanelView view, final I18n i18n,
            final ScreenGame screenGame, final ScreenMenu screenMenu,
            final ScreenCampaign screenCampaign, final ScreenEurope screenEurope,
            final ScreenSetting screenSetting, final ScreenColony screenColony) {
        this.view = Preconditions.checkNotNull(view);
        this.i18n = Preconditions.checkNotNull(i18n);

        /*
         * Wasd controlled is used just in game screen. In game screen wasd
         * controlled doesn't obtain key events.
         */
        screenResolver = new ChainOfCommandStrategy<ShowScreenEvent, GameScreen>(
                Lists.newArrayList(event -> {
                    if (Screen.COLONY == event.getScreen()) {
                        screenColony.setColony(event.getContext());
                        return screenColony;
                    }
                    return null;
                }, event -> {
                    if (Screen.EUROPE == event.getScreen()) {
                        return screenEurope;
                    }
                    return null;
                }, event -> {
                    if (Screen.CAMPAIGN == event.getScreen()) {
                        return screenCampaign;
                    }
                    return null;
                }, event -> {
                    if (Screen.SETTING == event.getScreen()) {
                        return screenSetting;
                    }
                    return null;
                }, event -> {
                    if (Screen.MENU == event.getScreen()) {
                        return screenMenu;
                    }
                    return null;
                }, event -> {
                    if (Screen.GAME == event.getScreen()) {
                        return screenGame;
                    }
                    return null;
                }));
        view.showScreen(screenMenu);

    }

    @Subscribe
    private void onShowScreen(final ShowScreenEvent event) {
        final GameScreen screen = screenResolver.apply(event);
        beforeHide(shownScreen);
        beforeShow(screen);
        view.showScreen(screen);
        shownScreen = screen;
    }

    private void beforeShow(final GameScreen screen) {
        if (screen == null) {
            return;
        }
        screen.beforeShow();
    }

    private void beforeHide(final GameScreen screen) {
        if (screen == null) {
            return;
        }
        screen.beforeHide();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onChangeLanguage(final ChangeLanguageEvent event) {
        view.updateLanguage(i18n);
    }

}
