package org.microcol.gui.screen;

import org.microcol.gui.WasdController;
import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.model.BeforeGameStartEvent;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Colony;

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

    private final ChainOfCommandStrategy<ShowScreenEvent, String> screenResolver = new ChainOfCommandStrategy<ShowScreenEvent, String>(
            Lists.newArrayList(event -> {
                if (Screen.COLONY == event.getScreen()) {
                    showColony(event.getContext());
                    return "OK";
                }
                return null;
            }, event -> {
                if (Screen.EUROPE == event.getScreen()) {
                    showEurope();
                    return "OK";
                }
                return null;
            }, event -> {
                if (Screen.GAME_MENU == event.getScreen()) {
                    showGameMenu();
                    return "OK";
                }
                return null;
            }, event -> {
                if (Screen.GAME == event.getScreen()) {
                    showGamePanel();
                    return "OK";
                }
                return null;
            }));

    @Inject
    public MainPanelPresenter(final MainPanelView view, final WasdController wasdController,
            final I18n i18n) {
        this.view = Preconditions.checkNotNull(view);
        this.i18n = Preconditions.checkNotNull(i18n);
        view.getContent().setOnKeyPressed(e -> {
            wasdController.onKeyPressed(e);
        });
        view.getContent().setOnKeyReleased(e -> {
            wasdController.onKeyReleased(e);
        });
    }

    @Subscribe
    private void onShowScreen(final ShowScreenEvent event) {
        screenResolver.apply(event);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onChangeLanguage(final ChangeLanguageEvent event) {
        view.updateLanguage(i18n);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onBeforeGameStartEvent(final BeforeGameStartEvent event) {
        view.showScreenGame();
    }

    public void showDefaultCampaignMenu() {
        view.showScreenCampaign();
    }

    private void showGamePanel() {
        view.showScreenGame();
    }

    private void showGameMenu() {
        view.showSceenMenu();
    }

    private void showEurope() {
        view.showScreenEurope();
    }

    public void showGameSetting() {
        view.showScreenSetting();
    }

    private void showColony(final Colony colony) {
        view.showScreenColony(colony);
    }

}
