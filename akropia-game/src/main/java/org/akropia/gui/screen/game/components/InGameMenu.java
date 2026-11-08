package org.akropia.gui.screen.game.components;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.dialog.SavedGameText;
import org.akropia.gui.event.ExitGameEvent;
import org.akropia.gui.event.QuitGameEvent;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.i18n.I18n;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Session actions use the same JavaFX popup controls as the colony selector.
 */
@Singleton
public final class InGameMenu {
    private final ContextMenu menu = new ContextMenu();
    private final I18n texts;
    private final PersistingService saves;
    private final GameModelController game;
    private final EventBus events;
    private final KeyboardShortcuts shortcuts;
    private Node anchor;

    @Inject
    InGameMenu(final I18n texts, final PersistingService saves,
            final GameModelController game, final EventBus events,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = shortcuts;
        this.texts = texts;
        this.saves = saves;
        this.game = game;
        this.events = events;
    }

    void setAnchor(final Node anchor) {
        this.anchor = anchor;
    }

    public void show() {
        if (menu.isShowing()) {
            menu.hide();
            return;
        }
        menu.getItems().clear();
        item(SavedGameText.resume, "menuResume", () -> {
        });
        menu.getItems().add(new SeparatorMenuItem());
        item(SavedGameText.save, "menuSave", () -> saves.browse(true, false),
                ShortcutAction.SAVE_GAME).setDisable(!game.isHumanTurnReady());
        item(SavedGameText.load, "menuLoad", () -> saves.browse(false, false),
                ShortcutAction.LOAD_GAME);
        item(SavedGameText.quickSave, "menuQuickSave", saves::quickSave,
                ShortcutAction.QUICK_SAVE).setDisable(!game.isHumanTurnReady());
        item(SavedGameText.quickLoad, "menuQuickLoad", saves::quickLoad,
                ShortcutAction.QUICK_LOAD).setDisable(!game.isHumanTurnReady());
        if (game.isGameModelReady()
                && game.getGameModel().isCampaignMission()) {
            item(SavedGameText.restart, "menuRestart", saves::restartMission,
                    ShortcutAction.RESTART_MISSION);
        }
        menu.getItems().add(new SeparatorMenuItem());
        item(SavedGameText.settings, "menuSettings",
                () -> events.post(new ShowScreenEvent(Screen.SETTING)),
                ShortcutAction.OPEN_SETTINGS);
        item(SavedGameText.mainMenu, "menuMain",
                () -> events.post(new ExitGameEvent()),
                ShortcutAction.EXIT_TO_MAIN_MENU);
        item(SavedGameText.quit, "menuQuit",
                () -> events.post(new QuitGameEvent()),
                ShortcutAction.QUIT_GAME);
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    /**
     * Run a session menu command from its shortcut, with the same action and
     * availability as its menu item.
     *
     * @param action {@link ShortcutAction#EXIT_TO_MAIN_MENU} or
     *               {@link ShortcutAction#RESTART_MISSION}
     */
    public void run(final ShortcutAction action) {
        if (action == ShortcutAction.EXIT_TO_MAIN_MENU) {
            events.post(new ExitGameEvent());
        } else if (action == ShortcutAction.RESTART_MISSION
                && game.isGameModelReady()
                && game.getGameModel().isCampaignMission()) {
            saves.restartMission();
        }
    }

    /**
     * Show the action's current shortcut in the label; its key handler owns the
     * key itself.
     */
    private MenuItem item(final SavedGameText text, final String id,
            final Runnable action, final ShortcutAction shortcut) {
        return item(shortcuts.withHint(texts.get(text), shortcut), id, action);
    }

    private MenuItem item(final SavedGameText text, final String id,
            final Runnable action) {
        return item(texts.get(text), id, action);
    }

    private MenuItem item(final String text, final String id,
            final Runnable action) {
        final MenuItem item = new MenuItem(text);
        item.setId(id);
        item.setOnAction(event -> {
            menu.hide();
            action.run();
        });
        menu.getItems().add(item);
        return item;
    }
}
