package org.microcol.gui.dialog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.Player;
import org.microcol.model.campaign.po.SavedGames.Entry;
import org.microcol.model.campaign.po.SavedGames.Kind;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Shared save/load browser; all persistence remains with its caller. */
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
        final ListView<Entry> list = new ListView<>();
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
        final MenuButton filter = new MenuButton(texts.get(
                recoveryOnly ? SavedGameText.automatic : SavedGameText.all));
        filter.setId("savedGamesFilter");
        filter.getStyleClass().add("game-toolbar-control");
        final MenuItem all = new MenuItem(texts.get(SavedGameText.all));
        all.setOnAction(event -> {
            list.getItems().setAll(entries);
            filter.setText(all.getText());
        });
        filter.getItems().add(all);
        for (final Kind kind : Kind.values()) {
            final MenuItem item = new MenuItem(texts.get(kindText(kind)));
            item.setId("saveFilter" + kind.name());
            item.setOnAction(event -> {
                list.getItems()
                        .setAll(entries.stream()
                                .filter(entry -> entry.kind == kind)
                                .collect(java.util.stream.Collectors.toList()));
                filter.setText(item.getText());
            });
            filter.getItems().add(item);
        }
        list.getItems()
                .setAll(entries.stream().filter(
                        entry -> !recoveryOnly || entry.kind == Kind.AUTOMATIC)
                        .collect(java.util.stream.Collectors.toList()));
        final TextField name = new TextField();
        name.setId("savedGameName");
        name.setPromptText(texts.get(SavedGameText.name));
        name.setAccessibleText(texts.get(SavedGameText.name));
        final Label error = new Label();
        error.setId("savedGameError");
        error.getStyleClass().add("name-error");
        final FlowPane actions = new FlowPane(8, 8);
        add(actions, saving ? SavedGameText.save : SavedGameText.load,
                saving ? "saveNew" : "loadSelected", () -> {
                    if (saving) {
                        try {
                            result = new Choice(Action.SAVE, null, Player
                                    .normalizeDisplayName(name.getText()));
                        } catch (IllegalArgumentException invalid) {
                            error.setText(texts.get(SavedGameText.invalidName));
                            return;
                        }
                    } else if (list.getSelectionModel()
                            .getSelectedItem() != null) {
                        result = new Choice(Action.LOAD,
                                list.getSelectionModel().getSelectedItem(),
                                null);
                    } else {
                        return;
                    }
                    close();
                });
        if (!saving) {
            actions.getChildren().get(0).disableProperty().bind(
                    list.getSelectionModel().selectedItemProperty().isNull());
        }
        if (saving) {
            addSelection(actions, SavedGameText.overwrite, "overwriteSave",
                    Action.OVERWRITE, list);
        }
        addSelection(actions, SavedGameText.delete, "deleteSave", Action.DELETE,
                list);
        add(actions,
                saving ? SavedGameText.exportFile : SavedGameText.importFile,
                saving ? "exportSave" : "importSave",
                () -> select(saving ? Action.EXPORT : Action.IMPORT));
        add(actions, SavedGameText.folder, "openSaveFolder",
                () -> select(Action.FOLDER));
        add(actions, SavedGameText.cancel, "savedGamesCancel", this::close);
        final VBox content = new VBox(10,
                new Label(texts.get(SavedGameText.title)), filter, list);
        if (saving) {
            content.getChildren().add(name);
        }
        content.getChildren().addAll(error, actions);
        content.getStyleClass().add("saved-games");
        content.setPadding(new Insets(16));
        content.setPrefSize(
                Math.min(760, getViewUtil().getPrimaryStage().getWidth() - 40),
                Math.min(520,
                        getViewUtil().getPrimaryStage().getHeight() - 40));
        VBox.setVgrow(list, Priority.ALWAYS);
        init(content);
        getScene().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        setTitle(texts.get(SavedGameText.title));
        showAndWait();
        return result;
    }

    private void addSelection(final FlowPane actions, final SavedGameText text,
            final String id, final Action action, final ListView<Entry> list) {
        final Button button = add(actions, text, id, () -> {
            result = new Choice(action,
                    list.getSelectionModel().getSelectedItem(), null);
            close();
        });
        button.disableProperty()
                .bind(javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                    final Entry entry = list.getSelectionModel()
                            .getSelectedItem();
                    return entry == null || entry.kind != Kind.MANUAL;
                }, list.getSelectionModel().selectedItemProperty()));
    }

    private Button add(final FlowPane pane, final SavedGameText text,
            final String id, final Runnable action) {
        final Button button = new Button(texts.get(text));
        button.setId(id);
        button.setOnAction(event -> action.run());
        pane.getChildren().add(button);
        return button;
    }

    private void select(final Action action) {
        result = new Choice(action, null, null);
        close();
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
