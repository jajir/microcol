package org.microcol.gui.dialog;

import java.util.List;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.po.SavedGames.Entry;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/** Modal host of the shared saved-games view; used for saving. */
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

    public Choice choose(final boolean saving, final List<Entry> entries,
            final boolean recoveryOnly) {
        result = null;
        final SavedGamesView view = new SavedGamesView(texts, saving,
                recoveryOnly, choice -> {
                    result = choice;
                    close();
                }, this::close);
        view.setEntries(entries);
        final Region content = view.getContent();
        content.setPadding(new Insets(16));
        content.setPrefSize(
                Math.min(760, getViewUtil().getPrimaryStage().getWidth() - 40),
                Math.min(520,
                        getViewUtil().getPrimaryStage().getHeight() - 40));
        init(content);
        getScene().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        setTitle(texts.get(SavedGameText.title));
        showAndWait();
        return result;
    }
}
