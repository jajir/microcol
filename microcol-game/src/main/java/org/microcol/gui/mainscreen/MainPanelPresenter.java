package org.microcol.gui.mainscreen;

import org.microcol.gui.WasdController;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.model.BeforeGameStartEvent;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * MicroCol's main panel. Based on commands and events just change between main
 * game panel and game menu.
 */
@Listener
public final class MainPanelPresenter {

    private final MainPanelView view;

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
    public MainPanelPresenter(final MainPanelView view, final KeyController keyController,
            final WasdController wasdController,
            final ChangeLanguageController changeLanguageController) {
        this.view = Preconditions.checkNotNull(view);
        view.getBox().setOnKeyPressed(e -> {
            wasdController.onKeyPressed(e);
            keyController.fireEvent(e);
        });
        view.getBox().setOnKeyReleased(e -> {
            wasdController.onKeyReleased(e);
        });
        changeLanguageController.addListener(this::onChangeLanguage);
    }

    @Subscribe
    private void onShowScreen(final ShowScreenEvent event) {
        screenResolver.apply(event);
    }

    private void onChangeLanguage(final ChangeLanguageEvent event) {
        view.updateLanguage(event.getI18n());
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onBeforeGameStartEvent(final BeforeGameStartEvent event) {
        view.showGamePanel();
    }

    public void showDefaultCampaignMenu() {
        view.showDefaultCampaignMenu();
    }

    private void showGamePanel() {
        view.showGamePanel();
    }

    private void showGameMenu() {
        view.showGameMenu();
    }

    private void showEurope() {
        view.showEurope();
    }

    public void showGameSetting() {
        view.showGameSetting();
    }

    private void showColony(final Colony colony) {
        view.showColony(colony);
    }

}
