package org.microcol.gui.dialog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.util.ActionButtons;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.Player;
import org.microcol.model.campaign.po.SavedGames.Entry;
import org.microcol.model.campaign.po.SavedGames.Kind;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Saved-game list, filter and actions shared by the Save dialog and the Load
 * page. Every chosen action is passed to its host; persistence stays with
 * {@link PersistingService}.
 */
public final class SavedGamesView {

    private final I18n texts;
    private final ListView<Entry> list = new ListView<>();
    private final MenuButton filter;
    private final TextField name = new TextField();
    private final Label error = new Label();
    private final VBox content;
    private final Consumer<SavedGamesDialog.Choice> chosen;
    private List<Entry> entries = new ArrayList<>();
    private Kind shownKind;

    /**
     * @param texts        localized labels
     * @param saving       show the new-save name and save actions
     * @param recoveryOnly start with the recovery-point filter
     * @param chosen       receives every chosen action
     * @param cancel       optional cancel action shown with the other actions
     */
    public SavedGamesView(final I18n texts, final boolean saving,
            final boolean recoveryOnly,
            final Consumer<SavedGamesDialog.Choice> chosen,
            final Runnable cancel) {
        this.texts = texts;
        this.chosen = chosen;
        shownKind = recoveryOnly ? Kind.AUTOMATIC : null;
        list.setId("savedGamesList");
        list.setPlaceholder(new Label(texts.get(SavedGameText.empty)));
        list.setCellFactory(ignored -> new ListCell<Entry>() {
            {
                setWrapText(true);
                prefWidthProperty().bind(list.widthProperty().subtract(28));
            }

            @Override
            protected void updateItem(final Entry entry, final boolean empty) {
                super.updateItem(entry, empty);
                setText(empty || entry == null ? null : describe(entry));
            }
        });
        filter = ActionButtons.menu(ButtonIcon.FILTER, texts.get(
                recoveryOnly ? SavedGameText.automatic : SavedGameText.all));
        filter.setId("savedGamesFilter");
        filter.getStyleClass().add("game-toolbar-control");
        final MenuItem all = new MenuItem(texts.get(SavedGameText.all));
        all.setOnAction(event -> {
            shownKind = null;
            showEntries();
            filter.setText(all.getText());
        });
        filter.getItems().add(all);
        for (final Kind kind : Kind.values()) {
            final MenuItem item = new MenuItem(texts.get(kindText(kind)));
            item.setId("saveFilter" + kind.name());
            item.setOnAction(event -> {
                shownKind = kind;
                showEntries();
                filter.setText(item.getText());
            });
            filter.getItems().add(item);
        }
        name.setId("savedGameName");
        name.setPromptText(texts.get(SavedGameText.name));
        name.setAccessibleText(texts.get(SavedGameText.name));
        error.setId("savedGameError");
        error.getStyleClass().add("name-error");
        error.setWrapText(true);
        final FlowPane actions = new FlowPane(8, 8);
        final Button primary = add(actions,
                saving ? SavedGameText.save : SavedGameText.load,
                saving ? "saveNew" : "loadSelected", () -> {
                    if (saving) {
                        try {
                            choose(new SavedGamesDialog.Choice(
                                    SavedGamesDialog.Action.SAVE, null,
                                    Player.normalizeDisplayName(
                                            name.getText())));
                        } catch (IllegalArgumentException invalid) {
                            error.setText(texts.get(SavedGameText.invalidName));
                        }
                    } else if (list.getSelectionModel()
                            .getSelectedItem() != null) {
                        choose(new SavedGamesDialog.Choice(
                                SavedGamesDialog.Action.LOAD,
                                list.getSelectionModel().getSelectedItem(),
                                null));
                    }
                });
        if (!saving) {
            primary.disableProperty().bind(
                    list.getSelectionModel().selectedItemProperty().isNull());
        }
        if (saving) {
            addSelection(actions, SavedGameText.overwrite, "overwriteSave",
                    SavedGamesDialog.Action.OVERWRITE);
        }
        addSelection(actions, SavedGameText.delete, "deleteSave",
                SavedGamesDialog.Action.DELETE);
        add(actions,
                saving ? SavedGameText.exportFile : SavedGameText.importFile,
                saving ? "exportSave" : "importSave",
                () -> choose(
                        new SavedGamesDialog.Choice(
                                saving ? SavedGamesDialog.Action.EXPORT
                                        : SavedGamesDialog.Action.IMPORT,
                                null, null)));
        add(actions, SavedGameText.folder, "openSaveFolder",
                () -> choose(new SavedGamesDialog.Choice(
                        SavedGamesDialog.Action.FOLDER, null, null)));
        if (cancel != null) {
            add(actions, SavedGameText.cancel, "savedGamesCancel", cancel);
        }
        content = new VBox(10, new Label(texts.get(SavedGameText.title)),
                filter, list);
        if (saving) {
            content.getChildren().add(name);
        }
        content.getChildren().addAll(error, actions);
        content.getStyleClass().add("saved-games");
        VBox.setVgrow(list, Priority.ALWAYS);
    }

    /**
     * Replace the listed saves, keeping the filter and the selected file.
     *
     * @param entries current saved games
     */
    public void setEntries(final List<Entry> entries) {
        this.entries = new ArrayList<>(entries);
        showEntries();
    }

    /**
     * @param message optional error shown next to the list
     */
    public void setError(final String message) {
        error.setText(message == null ? "" : message);
    }

    public Region getContent() {
        return content;
    }

    /** Start keyboard navigation in the list. */
    public void requestFocus() {
        list.requestFocus();
    }

    private void showEntries() {
        final Entry selected = list.getSelectionModel().getSelectedItem();
        list.getItems()
                .setAll(entries.stream().filter(
                        entry -> shownKind == null || entry.kind == shownKind)
                        .collect(Collectors.toList()));
        if (selected != null) {
            list.getItems().stream()
                    .filter(entry -> entry.file.equals(selected.file))
                    .findFirst()
                    .ifPresent(entry -> list.getSelectionModel().select(entry));
        }
    }

    private void choose(final SavedGamesDialog.Choice choice) {
        error.setText("");
        chosen.accept(choice);
    }

    private void addSelection(final FlowPane actions, final SavedGameText text,
            final String id, final SavedGamesDialog.Action action) {
        final Button button = add(actions, text, id,
                () -> choose(new SavedGamesDialog.Choice(action,
                        list.getSelectionModel().getSelectedItem(), null)));
        button.disableProperty()
                .bind(javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                    final Entry entry = list.getSelectionModel()
                            .getSelectedItem();
                    return entry == null || entry.kind != Kind.MANUAL;
                }, list.getSelectionModel().selectedItemProperty()));
    }

    private Button add(final FlowPane pane, final SavedGameText text,
            final String id, final Runnable action) {
        final Button button = ActionButtons.button(icon(text), texts.get(text));
        button.setId(id);
        button.setOnAction(event -> action.run());
        pane.getChildren().add(button);
        return button;
    }

    private static ButtonIcon icon(final SavedGameText action) {
        switch (action) {
        case save:
        case overwrite:
            return ButtonIcon.SAVE;
        case load:
            return ButtonIcon.FOLDER;
        case delete:
            return ButtonIcon.DELETE;
        case exportFile:
            return ButtonIcon.EXPORT;
        case importFile:
            return ButtonIcon.IMPORT;
        case folder:
            return ButtonIcon.FOLDER;
        case cancel:
            return ButtonIcon.CANCEL;
        default:
            throw new IllegalArgumentException(
                    "No saved-game action icon: " + action);
        }
    }

    static SavedGameText kindText(final Kind kind) {
        return kind == Kind.MANUAL ? SavedGameText.manual
                : kind == Kind.QUICK ? SavedGameText.quick
                        : SavedGameText.automatic;
    }

    private String describe(final Entry entry) {
        final String time = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(texts.getCurrentLocale())
                .format(Instant.ofEpochMilli(entry.modified)
                        .atZone(ZoneId.systemDefault()));
        String details = texts.get(SavedGameText.corrupt);
        if (entry.snapshot != null && entry.snapshot.getModel() != null) {
            try {
                final var model = entry.snapshot.getModel();
                final var player = model.getGameManager().getPlayers().stream()
                        .filter(value -> !value.isComputer()).findFirst()
                        .orElseThrow();
                final Object displayName = player.getExtraData()
                        .getOrDefault("displayName", player.getName());
                final String playerName = Player
                        .normalizeDisplayName((String) displayName);
                final String mission = org.microcol.model.campaign.CampaignNames.defaultCampaign
                        .toString().equals(entry.snapshot.getCampaignName())
                                ? texts.get(
                                        org.microcol.gui.screen.menu.GameMenu
                                                .get(org.microcol.model.campaign.CampaignNames.defaultCampaign,
                                                        entry.snapshot
                                                                .getMissionName()))
                                : texts.get(SavedGameText.freePlay);
                details = playerName
                        + (playerName.equals(player.getName()) ? ""
                                : " · " + player.getName())
                        + " · " + mission + " · "
                        + Calendar.make(model.getCalendar()).getCurrentYear()
                        + " · " + new org.microcol.gui.LocalizationHelper(texts)
                                .getDifficultyName(model.getDifficulty());
            } catch (RuntimeException invalid) {
                details = texts.get(SavedGameText.corrupt);
            }
        }
        return displayName(entry) + " · " + texts.get(kindText(entry.kind))
                + " · " + time + "\n" + details;
    }

    private String displayName(final Entry entry) {
        if (entry.kind == Kind.AUTOMATIC) {
            switch (entry.file.getName()) {
            case "autosave.microcol":
                return texts.get(
                        org.microcol.gui.screen.menu.GameMenu.recoveryLatestPosition);
            case "latest-turn.microcol":
                return texts.get(
                        org.microcol.gui.screen.menu.GameMenu.recoveryLatestTurn);
            case "previous-decade.microcol":
                return texts.get(
                        org.microcol.gui.screen.menu.GameMenu.recoveryPreviousDecade);
            default:
                break;
            }
        }
        return entry.getName();
    }
}
