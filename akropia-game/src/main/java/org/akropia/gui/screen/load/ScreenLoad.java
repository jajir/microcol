package org.akropia.gui.screen.load;

import java.util.List;

import org.akropia.gui.Loc;
import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.dialog.SavedGameText;
import org.akropia.gui.dialog.SavedGamesDialog;
import org.akropia.gui.dialog.SavedGamesView;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.AbstractScreenMenu;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.ReturnToOriginEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.screen.menu.MenuHolderPanel;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.InGamePage;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.SavedGames.Entry;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Load game page of the main window. The opening screen selects the menu sea or
 * the in-game frame; Back and Escape return to that screen. Every action uses
 * the existing {@link PersistingService} operations.
 */
@Singleton
public final class ScreenLoad extends AbstractScreenMenu implements GameScreen {

    public static final String BUTTON_BACK_ID = "savedGamesCancel";

    /**
     * The list and the wider detail need more room than the Settings column.
     */
    private static final double MENU_WIDTH = 960;

    private static final String STYLE_SHEET_SAVED_GAMES = ScreenLoad.class
            .getResource("/gui/SavedGames.css").toExternalForm();

    private final MenuHolderPanel menuHolderPanel;
    private final InGamePage page;
    private final PersistingService saves;
    private final EventBus eventBus;
    private final I18n i18n;
    private final StackPane root = new StackPane();
    private final HBox toolbar = new HBox();
    private final Button back = ActionButtons.button(ButtonIcon.BACK);
    private SavedGamesView view;
    private boolean menuHosted = true;
    private boolean recoveryOnly;
    private boolean shown;

    @Inject
    ScreenLoad(final MenuHolderPanel menuHolderPanel, final InGamePage page,
            final PersistingService saves, final EventBus eventBus,
            final I18n i18n) {
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.page = Preconditions.checkNotNull(page);
        this.saves = Preconditions.checkNotNull(saves);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        menuHolderPanel.getContent().getStylesheets()
                .addAll(STYLE_SHEET_GAME_MENU, STYLE_SHEET_SAVED_GAMES);
        menuHolderPanel.getContent().getStyleClass().add("load-screen");
        page.getContent().getStylesheets().addAll(STYLE_SHEET_GAME_MENU,
                STYLE_SHEET_SAVED_GAMES);
        page.setStatusSource(Source.GAME);
        page.setToolbar(toolbar);
        back.setId(BUTTON_BACK_ID);
        back.setOnAction(event -> eventBus.post(new ReturnToOriginEvent()));
        // Nested popups and confirmations are separate windows that handle
        // their own Escape first.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                eventBus.post(new ReturnToOriginEvent());
            }
        });
    }

    /**
     * @param menuHosted <code>true</code> when opened from a menu screen
     */
    public void setMenuHosted(final boolean menuHosted) {
        this.menuHosted = menuHosted;
    }

    /**
     * @param recoveryOnly start with the recovery-point filter
     */
    public void setRecoveryOnly(final boolean recoveryOnly) {
        this.recoveryOnly = recoveryOnly;
    }

    @Override
    public Region getContent() {
        return root;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(SavedGameText.load));
        page.setTitle(i18n.get(SavedGameText.load));
        back.setText(menuHosted ? i18n.get(GameMenu.buttonBack)
                : i18n.get(Loc.backToGame));
        page.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        shown = true;
        updateLanguage(i18n);
        view = new SavedGamesView(i18n, false, recoveryOnly, this::perform,
                null, saves::preview);
        refresh();
        if (menuHosted) {
            toolbar.getChildren().clear();
            menuHolderPanel.showInSea(view.getContent(), back, MENU_WIDTH);
            menuHolderPanel.beforeShow();
            root.getChildren().setAll(menuHolderPanel.getContent());
        } else {
            toolbar.getChildren().setAll(back);
            page.setContent(view.getContent());
            page.beforeShow();
            root.getChildren().setAll(page.getContent());
        }
        // The main panel attaches the page later on the FX thread.
        final SavedGamesView shownView = view;
        Platform.runLater(shownView::requestFocus);
    }

    @Override
    public void beforeHide() {
        shown = false;
        menuHolderPanel.beforeHide();
    }

    private void perform(final SavedGamesDialog.Choice choice) {
        saves.perform(choice);
        // A completed load has already shown the new game.
        if (shown) {
            refresh();
        }
    }

    private void refresh() {
        final List<Entry> entries = saves.listSavedGames();
        if (entries != null) {
            view.setEntries(entries);
        }
    }

}
