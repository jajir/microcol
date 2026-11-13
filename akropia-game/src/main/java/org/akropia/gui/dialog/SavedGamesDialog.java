package org.akropia.gui.dialog;

import java.util.List;
import com.google.common.eventbus.EventBus;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.akropia.gui.Loc;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.MainPanelPresenter;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.InGamePage;
import org.akropia.gui.util.ViewUtil;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames.Entry;
import com.google.inject.Provider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Full-window Save host, preserving synchronous save-before-leaving decisions.
 */
@Singleton
public final class SavedGamesDialog implements GameScreen {
    public enum Action {
        SAVE, LOAD, OVERWRITE, DELETE, IMPORT, EXPORT, FOLDER
    }

    public static final class Choice {
        final Action action;
        final Entry entry;
        final String name;

        Choice(final Action action, final Entry entry, final String name) {
            this.action = action;
            this.entry = entry;
            this.name = name;
        }
    }

    private final EventBus eventBus;
    private Choice result;
    private final I18n texts;
    private final InGamePage page;
    private final Provider<MainPanelPresenter> screens;
    private final Button back = ActionButtons.button(ButtonIcon.BACK);
    private SavedGamesView view;
    private boolean waiting;
    private final ViewUtil window;
    private final EventHandler<WindowEvent> closeRequest = event -> {
        event.consume();
        close();
    };

    @Inject
    public SavedGamesDialog(final InGamePage page, final I18n texts,
            final Provider<MainPanelPresenter> screens, final ViewUtil window,
            final EventBus eventBus) {
        this.eventBus = eventBus;
        this.page = page;
        this.texts = texts;
        this.screens = screens;
        this.window = window;
        page.setStatusSource(Source.GAME);
        page.getContent().getStyleClass().add("saved-games-screen");
        page.getContent().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        back.setId("savedGamesCancel");
        back.setOnAction(event -> close());
        page.setToolbar(new HBox(back));
        page.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                close();
            }
        });
    }

    public Choice chooseSave(final List<Entry> entries,
            final Function<Entry, Optional<byte[]>> previews,
            final SaveSummaryPo current, final byte[] currentImage,
            final String suggestedName, final Predicate<Choice> perform,
            final Supplier<List<Entry>> refresh) {
        result = null;
        view = new SavedGamesView(texts, true, choice -> {
            if (perform.test(choice)) {
                result = choice;
                // Finish the confirmation's event before closing its owner.
                view.getContent().setDisable(true);
                Platform.runLater(this::close);
            } else if (choice.action == Action.DELETE) {
                final List<Entry> updated = refresh.get();
                view.setEntries(updated == null ? List.of() : updated);
            }
        }, null, previews);
        view.setSaveHelp(message -> eventBus
                .post(new StatusBarMessageEvent(message, Source.GAME)));
        view.setEntries(entries);
        view.setCurrentGame(current, currentImage, suggestedName);
        page.setContent(view.getContent());
        waiting = true;
        // Navigate directly: a save-before-leaving EventBus subscriber may
        // still be on the stack while this synchronous choice is open.
        screens.get().showSaveScreen();
        window.getPrimaryStage()
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, closeRequest);
        try {
            Platform.enterNestedEventLoop(this);
        } finally {
            window.getPrimaryStage().removeEventFilter(
                    WindowEvent.WINDOW_CLOSE_REQUEST, closeRequest);
        }
        return result;
    }

    private void close() {
        screens.get().closeSaveScreen();
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        page.setTitle(i18n.get(SavedGameText.save));
        back.setText(i18n.get(Loc.backToGame));
        page.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        updateLanguage(texts);
        page.beforeShow();
        Platform.runLater(view::requestFocus);
    }

    @Override
    public void beforeHide() {
        if (waiting) {
            waiting = false;
            Platform.exitNestedEventLoop(this, null);
        }
    }
}
