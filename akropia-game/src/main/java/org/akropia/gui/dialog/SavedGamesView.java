package org.akropia.gui.dialog;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.akropia.gui.Loc;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.util.ActionButtons;
import org.akropia.i18n.I18n;
import org.akropia.model.Calendar;
import org.akropia.model.EuropeanNation;
import org.akropia.model.campaign.CampaignNames;
import org.akropia.model.campaign.po.SaveArchiveException;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames.Entry;
import org.akropia.model.campaign.po.SavedGames.Kind;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Saved-game list, detail and actions shared by the Save dialog and the Load
 * page (R-0382, R-0383). The narrower left column lists the saves; the wider
 * right column shows the full name, map preview and summary. Every chosen
 * action is passed to its host; persistence stays with
 * {@link PersistingService}.
 */
public final class SavedGamesView {

    private static final double PREVIEW_WIDTH = 480;
    private static final double PREVIEW_HEIGHT = 270;

    private final I18n texts;
    private final boolean saving;
    private final ListView<Entry> list = new ListView<>();
    private final TextField name = new TextField();
    private final Label error = new Label();
    private final Label detailName = new Label();
    private final ImageView preview = new ImageView();
    private final Label previewMissing = new Label();
    private final StackPane previewBox = new StackPane(preview, previewMissing);
    private final Label summary = new Label();
    private SaveSummaryPo current;
    private byte[] currentImage;
    private final VBox content;
    private final Consumer<SavedGamesDialog.Choice> chosen;
    private final Function<Entry, Optional<byte[]>> previews;

    /**
     * @param texts    localized labels
     * @param saving   show the new-save name and save actions
     * @param chosen   receives every chosen action
     * @param cancel   optional cancel action shown with the other actions
     * @param previews reads the preview of one selected save
     */
    public SavedGamesView(final I18n texts, final boolean saving,
            final Consumer<SavedGamesDialog.Choice> chosen,
            final Runnable cancel,
            final Function<Entry, Optional<byte[]>> previews) {
        this.texts = texts;
        this.saving = saving;
        this.chosen = chosen;
        this.previews = previews;
        list.setId("savedGamesList");
        list.setAccessibleText(texts.get(SavedGameText.title));
        final Label empty = new Label(texts.get(SavedGameText.empty));
        empty.setWrapText(true);
        empty.setMinHeight(Region.USE_PREF_SIZE);
        empty.maxWidthProperty()
                .bind(Bindings.max(1, list.widthProperty().subtract(28)));
        list.setPlaceholder(empty);
        list.setMinWidth(0);
        list.setCellFactory(ignored -> new ListCell<Entry>() {
            private final Label filename = new Label();
            private final Button delete = ActionButtons
                    .button(ButtonIcon.DELETE);
            private final HBox row = new HBox(8, filename, delete);
            {
                prefWidthProperty().bind(list.widthProperty().subtract(28));
                filename.setWrapText(true);
                filename.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(filename, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.prefWidthProperty().bind(widthProperty().subtract(20));
                delete.setMinWidth(Region.USE_PREF_SIZE);
                delete.setId("deleteSaveRow");
                delete.setAccessibleText(texts.get(SavedGameText.delete));
                delete.setOnAction(event -> {
                    choose(new SavedGamesDialog.Choice(
                            SavedGamesDialog.Action.DELETE, getItem(), null));
                    event.consume();
                });
                delete.setOnMouseClicked(event -> event.consume());
                setOnMouseClicked(event -> {
                    if (saving && !isEmpty()
                            && event.getButton() == MouseButton.PRIMARY
                            && event.getClickCount() == 2) {
                        name.setText(filename(getItem()));
                        save();
                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(final Entry entry, final boolean empty) {
                super.updateItem(entry, empty);
                setText(null);
                setGraphic(!empty && entry != null ? row : null);
                if (entry != null && !empty) {
                    filename.setText(entry.file.getName());
                }
            }
        });
        name.setId("savedGameName");
        name.setPromptText(texts.get(SavedGameText.name));
        name.setAccessibleText(texts.get(SavedGameText.name));
        error.setId("savedGameError");
        error.getStyleClass().add("name-error");
        error.setWrapText(true);
        detailName.setId("savedGameDetailName");
        detailName.getStyleClass().add("saved-game-name");
        detailName.setWrapText(true);
        preview.setId("savedGamePreview");
        preview.setPreserveRatio(true);
        preview.setSmooth(false);
        previewMissing.setId("savedGamePreviewMissing");
        previewMissing.setText(texts.get(SavedGameText.previewMissing));
        previewMissing.setWrapText(true);
        previewBox.getStyleClass().add("saved-game-preview");
        previewBox.setMinSize(0, 0);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        summary.setId("savedGameSummary");
        summary.setWrapText(true);
        // Keep full wrapped text; the hosting scroll pane handles short
        // windows.
        for (final Label label : new Label[] { detailName, previewMissing,
                summary, error }) {
            label.setMinHeight(Region.USE_PREF_SIZE);
        }
        previewBox.minHeightProperty()
                .bind(Bindings.when(preview.visibleProperty())
                        .then(PREVIEW_HEIGHT + 2).otherwise(60));

        final FlowPane primaryActions = new FlowPane(8, 8);
        primaryActions.setAlignment(Pos.CENTER_RIGHT);
        final FlowPane actions = new FlowPane(8, 8);
        final Button primary = add(primaryActions,
                saving ? SavedGameText.save : SavedGameText.load,
                saving ? "saveNew" : "loadSelected", () -> {
                    if (saving) {
                        save();
                    } else if (isLoadable(
                            list.getSelectionModel().getSelectedItem())) {
                        choose(new SavedGamesDialog.Choice(
                                SavedGamesDialog.Action.LOAD,
                                list.getSelectionModel().getSelectedItem(),
                                null));
                    }
                });
        if (!saving) {
            // Unreadable and unsupported saves can never be confirmed.
            primary.disableProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> !isLoadable(
                                    list.getSelectionModel().getSelectedItem()),
                            list.getSelectionModel().selectedItemProperty()));
        }
        final FlowPane folderActions = new FlowPane();
        add(folderActions, SavedGameText.folder, "openSaveFolder",
                () -> choose(new SavedGamesDialog.Choice(
                        SavedGamesDialog.Action.FOLDER, null, null)));
        if (cancel != null) {
            add(actions, SavedGameText.cancel, "savedGamesCancel", cancel);
        }

        final VBox left = new VBox(10, list, folderActions);
        left.setMinWidth(0);
        VBox.setVgrow(list, Priority.ALWAYS);
        final VBox detail = new VBox(10);
        detail.setId("savedGameDetail");
        detail.getStyleClass().add("saved-game-detail");
        detail.setMinWidth(0);
        // A positive fit width keeps even an unlaid detail from growing to the
        // image size.
        preview.fitWidthProperty().bind(Bindings.max(1, Bindings
                .min(PREVIEW_WIDTH, detail.widthProperty().subtract(24))));
        preview.setFitHeight(PREVIEW_HEIGHT);
        detail.getChildren().addAll(detailName, previewBox, summary);
        if (saving) {
            final Label nameLabel = new Label(texts.get(SavedGameText.name));
            nameLabel.setLabelFor(name);
            final Label warning = new Label(
                    texts.get(SavedGameText.managedWarning));
            warning.setId("savedGameManagedWarning");
            warning.setWrapText(true);
            warning.setMinHeight(Region.USE_PREF_SIZE);
            warning.visibleProperty().bind(Bindings.createBooleanBinding(
                    () -> name.getText().toLowerCase(java.util.Locale.ROOT)
                            .matches(
                                    "(autosave(?:-[0-9]+)?|quick|latest-turn|previous-decade)(\\.akropia)?"),
                    name.textProperty()));
            warning.managedProperty().bind(warning.visibleProperty());
            detail.getChildren().addAll(nameLabel, name, warning);
        }
        final Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        detail.getChildren().addAll(error, spacer, primaryActions);

        // About 1:2 for the list and the detail; both shrink without
        // horizontal overflow.
        final GridPane columns = new GridPane();
        columns.setHgap(12);
        final ColumnConstraints listColumn = new ColumnConstraints();
        listColumn.setPercentWidth(100d / 3);
        listColumn.setMinWidth(0);
        final ColumnConstraints detailColumn = new ColumnConstraints();
        detailColumn.setPercentWidth(200d / 3);
        detailColumn.setMinWidth(0);
        columns.getColumnConstraints().addAll(listColumn, detailColumn);
        final RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        columns.getRowConstraints().add(row);
        columns.add(left, 0, 0);
        columns.add(detail, 1, 0);
        columns.setMinWidth(0);

        content = new VBox(10);
        VBox.setVgrow(content, Priority.ALWAYS);
        content.getChildren().add(columns);
        if (!actions.getChildren().isEmpty()) {
            content.getChildren().add(actions);
        }
        content.getStyleClass().add("saved-games");
        VBox.setVgrow(columns, Priority.ALWAYS);
        list.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetail(newValue));
        if (!saving) {
            showDetail(null);
        }
    }

    /**
     * Show the current game initially; selecting a row shows that saved game.
     *
     * @param current       summary of the current game
     * @param currentImage  optional preview of the current game
     * @param suggestedName prefilled editable name
     */
    public void setCurrentGame(final SaveSummaryPo current,
            final byte[] currentImage, final String suggestedName) {
        this.current = current;
        this.currentImage = currentImage;
        name.setText(suggestedName == null ? "" : suggestedName);
        showDetail(list.getSelectionModel().getSelectedItem());
    }

    /**
     * Replace the listed saves, keeping the selected file.
     *
     * @param entries current saved games
     */
    public void setEntries(final List<Entry> entries) {
        showEntries(entries);
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

    private void showEntries(final List<Entry> entries) {
        final Entry selected = list.getSelectionModel().getSelectedItem();
        list.getItems().setAll(entries);
        if (selected != null) {
            list.getItems().stream()
                    .filter(entry -> entry.file.equals(selected.file))
                    .findFirst()
                    .ifPresent(entry -> list.getSelectionModel().select(entry));
        }
        // A removed save never keeps its detail.
        showDetail(list.getSelectionModel().getSelectedItem());
    }

    private void showDetail(final Entry entry) {
        detailName.setVisible(saving || entry == null);
        detailName.setManaged(saving || entry == null);
        if (saving && entry == null) {
            detailName.setText(texts.get(SavedGameText.currentGame));
            showPreview(Optional.ofNullable(currentImage),
                    texts.get(SavedGameText.currentGame));
            summary.setText(describe(current, null));
            return;
        }
        if (saving) {
            name.setText(filename(entry));
        }
        if (entry == null) {
            detailName.setText(texts.get(SavedGameText.detailPrompt));
            previewBox.setVisible(false);
            previewBox.setManaged(false);
            summary.setText("");
            return;
        }
        detailName.setText(displayName(entry));
        if (entry.metadata == null) {
            previewBox.setVisible(false);
            previewBox.setManaged(false);
            summary.setText(texts.get(
                    entry.problem == SaveArchiveException.Reason.UNSUPPORTED
                            ? SavedGameText.unsupported
                            : SavedGameText.corrupt)
                    + savedDetails(entry));
            return;
        }
        // Read synchronously for the current selection only, so the image
        // and data always belong to the selected save.
        showPreview(previews.apply(entry), displayName(entry));
        summary.setText(describe(entry.metadata.getSummary(), entry));
    }

    private static String filename(final Entry entry) {
        final String filename = entry.file.getName();
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private void save() {
        try {
            choose(new SavedGamesDialog.Choice(SavedGamesDialog.Action.SAVE,
                    null, org.akropia.model.campaign.po.SavedGames
                            .manualName(name.getText())));
        } catch (IllegalArgumentException invalid) {
            error.setText(texts.get(SavedGameText.invalidName));
        }
    }

    private void showPreview(final Optional<byte[]> png, final String label) {
        previewBox.setVisible(true);
        previewBox.setManaged(true);
        Image image = null;
        if (png.isPresent()) {
            image = new Image(new ByteArrayInputStream(png.get()));
            if (image.isError()) {
                image = null;
            }
        }
        preview.setImage(image);
        preview.setVisible(image != null);
        preview.setManaged(image != null);
        previewMissing.setVisible(image == null);
        previewMissing.setManaged(image == null);
        previewBox.setAccessibleText(
                image == null ? texts.get(SavedGameText.previewMissing)
                        : label);
    }

    private static boolean isLoadable(final Entry entry) {
        return entry != null && entry.metadata != null;
    }

    private void choose(final SavedGamesDialog.Choice choice) {
        error.setText("");
        chosen.accept(choice);
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

    /**
     * @param texts  localized labels
     * @param millis real time of a save
     * @return the time in the local zone and the current language
     */
    static String formatTime(final I18n texts, final long millis) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(texts.getCurrentLocale()).format(Instant
                        .ofEpochMilli(millis).atZone(ZoneId.systemDefault()));
    }

    /**
     * Localize the stored summary; the values themselves come from the save.
     */
    private String describe(final SaveSummaryPo value, final Entry entry) {
        final String saved = entry == null ? "" : savedDetails(entry);
        if (value == null) {
            return texts.get(SavedGameText.summaryMissing) + saved;
        }
        try {
            String year = String.valueOf(value.getYear()) + " AD";
            if (value.getSeason() != null) {
                year += " " + texts.get(Loc.getTerrainDescription(
                        Calendar.Season.valueOf(value.getSeason())));
            }
            final String mission = CampaignNames.defaultCampaign.toString()
                    .equals(value.getCampaignName())
                            ? texts.get(SavedGameText.campaign) + " · "
                                    + texts.get(GameMenu.get(
                                            CampaignNames.defaultCampaign,
                                            value.getMissionName()))
                            : texts.get(SavedGameText.freePlay);
            return texts.get(SavedGameText.summaryPlayer, value.getPlayerName(),
                    nationName(value.getNation())) + "\n"
                    + texts.get(SavedGameText.summaryYear, year) + "\n"
                    + texts.get(SavedGameText.summaryGold, value.getGold())
                    + "\n"
                    + texts.get(SavedGameText.summarySupport,
                            value.getRebelSupportPercent())
                    + "\n"
                    + texts.get(SavedGameText.summaryCounts,
                            value.getPeopleCount(), value.getColonyCount(),
                            value.getShipCount())
                    + "\n" + texts.get(SavedGameText.summaryGameType, mission)
                    + saved;
        } catch (RuntimeException invalid) {
            return texts.get(SavedGameText.summaryMissing) + saved;
        }
    }

    private String savedDetails(final Entry entry) {
        return "\n" + texts.get(SavedGameText.summarySaved,
                formatTime(texts, entry.modified));
    }

    private String nationName(final String nation) {
        try {
            switch (EuropeanNation.valueOf(nation)) {
            case DUTCH:
                return texts.get(GameMenu.setupNationDutch);
            case FRENCH:
                return texts.get(GameMenu.setupNationFrench);
            case ENGLISH:
                return texts.get(GameMenu.setupNationEnglish);
            case SPANISH:
                return texts.get(GameMenu.setupNationSpanish);
            default:
                return nation;
            }
        } catch (IllegalArgumentException | NullPointerException unknown) {
            return String.valueOf(nation);
        }
    }

    private String displayName(final Entry entry) {
        if (entry.kind == Kind.AUTOMATIC) {
            switch (entry.file.getName()) {
            case "autosave.akropia":
                return texts.get(GameMenu.recoveryLatestPosition);
            case "latest-turn.akropia":
                return texts.get(GameMenu.recoveryLatestTurn);
            case "previous-decade.akropia":
                return texts.get(GameMenu.recoveryPreviousDecade);
            default:
                break;
            }
        }
        return entry.getName();
    }
}
