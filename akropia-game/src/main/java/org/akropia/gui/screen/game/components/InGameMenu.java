package org.akropia.gui.screen.game.components;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.dialog.SavedGameText;
import org.akropia.gui.event.ExitGameEvent;
import org.akropia.gui.event.QuitGameEvent;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.i18n.I18n;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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
    private Node anchor;

    @Inject
    InGameMenu(final I18n texts, final PersistingService saves,
            final GameModelController game, final EventBus events) {
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
        item(SavedGameText.saveMenu, "menuSave",
                () -> saves.browse(true, false), KeyCode.S)
                .setDisable(!game.isHumanTurnReady());
        item(SavedGameText.loadMenu, "menuLoad",
                () -> saves.browse(false, false), KeyCode.O);
        item(SavedGameText.quickSave, "menuQuickSave", saves::quickSave)
                .setDisable(!game.isHumanTurnReady());
        item(SavedGameText.quickLoad, "menuQuickLoad", saves::quickLoad)
                .setDisable(!game.isHumanTurnReady());
        if (game.isGameModelReady()
                && game.getGameModel().isCampaignMission()) {
            item(SavedGameText.restart, "menuRestart", saves::restartMission);
        }
        menu.getItems().add(new SeparatorMenuItem());
        item(SavedGameText.settings, "menuSettings",
                () -> events.post(new ShowScreenEvent(Screen.SETTING)));
        item(SavedGameText.mainMenu, "menuMain",
                () -> events.post(new ExitGameEvent()));
        item(SavedGameText.quit, "menuQuit",
                () -> events.post(new QuitGameEvent()));
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    /**
     * Show the platform shortcut (Command on macOS, Ctrl elsewhere) in the
     * label; the main window's shortcut owner handles the key itself.
     */
    private MenuItem item(final SavedGameText text, final String id,
            final Runnable action, final KeyCode shortcut) {
        return item(
                texts.get(text,
                        new KeyCodeCombination(shortcut,
                                KeyCombination.SHORTCUT_DOWN).getDisplayText()),
                id, action);
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
