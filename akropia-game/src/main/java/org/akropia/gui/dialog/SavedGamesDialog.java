package org.akropia.gui.dialog;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames.Entry;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * Modal host of the shared saved-games view; used for saving. The detail shows
 * the current game, kept apart from a selected overwrite target.
 */
public final class SavedGamesDialog extends AbstractMessageWindow {
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

    private Choice result;
    private final I18n texts;

    public SavedGamesDialog(final ViewUtil view, final I18n texts) {
        super(view, texts);
        this.texts = texts;
    }

    /**
     * Show the Save browser over the main window.
     *
     * @param entries       listed saves; a selected manual save is the
     *                      overwrite target
     * @param previews      reads the preview of one selected save
     * @param current       summary of the game being saved
     * @param currentImage  optional preview of the game being saved
     * @param suggestedName prefilled editable name
     * @return chosen action, or <code>null</code> when cancelled
     */
    public Choice chooseSave(final List<Entry> entries,
            final Function<Entry, Optional<byte[]>> previews,
            final SaveSummaryPo current, final byte[] currentImage,
            final String suggestedName) {
        result = null;
        final SavedGamesView view = new SavedGamesView(texts, true, false,
                choice -> {
                    result = choice;
                    close();
                }, this::close, previews);
        view.setEntries(entries);
        view.setCurrentGame(current, currentImage, suggestedName);
        final Region content = view.getContent();
        content.setPadding(new Insets(16));
        content.setPrefSize(
                Math.min(860, getViewUtil().getPrimaryStage().getWidth() - 40),
                Math.min(600,
                        getViewUtil().getPrimaryStage().getHeight() - 40));
        // Handle Escape before the focused list or name field consumes it; the
        // filter menu and confirmations are separate windows that handle
        // their own Escape first.
        content.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                close();
            }
        });
        init(content);
        getScene().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        setTitle(texts.get(SavedGameText.title));
        showAndWait();
        return result;
    }
}
