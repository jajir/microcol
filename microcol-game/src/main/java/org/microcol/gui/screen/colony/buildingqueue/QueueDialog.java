package org.microcol.gui.screen.colony.buildingqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.screen.colony.RepaintColonyEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.UnitType;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class QueueDialog extends AbstractMessageWindow {

    private final EventBus eventBus;
    private final QueueController controller;
    private final I18n i18n;
    private final Label title = new Label();
    private final Label projectsTitle = new Label();
    private final Label queueTitle = new Label();
    private final Label hammerPool = new Label();
    private final Label detail = new Label();
    private final Label feedback = new Label();
    private final ListView<ColonyBuildingItem> projects = new ListView<>();
    private final ListView<ColonyBuildingItemProgress<?>> queue = new ListView<>();
    private final Button add = new Button();
    private final Button up = new Button();
    private final Button down = new Button();
    private final Button remove = new Button();
    private final Button cancel = new Button();
    private List<?> selectionState = List.of();

    @Inject
    QueueDialog(final ViewUtil viewUtil, final I18n i18n,
            final EventBus eventBus, final QueueController controller) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.controller = controller;
        projects.setId("constructionProjects");
        queue.setId("constructionQueue");
        hammerPool.setId("constructionHammerPool");
        hammerPool.setWrapText(true);
        detail.setId("constructionDetail");
        feedback.setId("constructionFeedback");
        add.setId("constructionAdd");
        up.setId("constructionUp");
        down.setId("constructionDown");
        remove.setId("constructionRemove");
        cancel.setId("constructionCancel");
        projects.setCellFactory(list -> new ListCell<ColonyBuildingItem>() {
            @Override
            protected void updateItem(final ColonyBuildingItem item,
                    final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : ConstructionText.name(i18n, item) + "\n"
                                + i18n.get(ConstructionText.cost,
                                        item.getRequiredHammers(),
                                        item.getRequiredTools()));
                setId(empty || item == null ? null
                        : "project-" + item.getName());
            }
        });
        queue.setItems(controller.getObservableList());
        queue.setCellFactory(
                list -> new ListCell<ColonyBuildingItemProgress<?>>() {
                    @Override
                    protected void updateItem(
                            final ColonyBuildingItemProgress<?> item,
                            final boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null
                                : ConstructionText.name(i18n, item.getItem())
                                        + "\n"
                                        + i18n.get(ConstructionText.cost,
                                                item.getRequiredHammers(),
                                                item.getRequiredTools()));
                    }
                });
        projects.getSelectionModel().selectedItemProperty()
                .addListener((o, old, selected) -> select(selected));
        queue.getSelectionModel().selectedItemProperty()
                .addListener((o, old, selected) -> updateQueueButtons());
        add.setDefaultButton(true);
        add.setOnAction(event -> confirm());
        up.setOnAction(event -> changeQueue(-1));
        down.setOnAction(event -> changeQueue(1));
        remove.setOnAction(event -> changeQueue(0));
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        detail.setWrapText(true);
        feedback.setWrapText(true);
        detail.setMinHeight(78);
        feedback.setMinHeight(48);
        final VBox choices = new VBox(8, projectsTitle, projects, detail, add);
        final VBox current = new VBox(8, queueTitle, hammerPool, queue,
                new HBox(8, up, down), remove);
        choices.setPrefWidth(420);
        current.setPrefWidth(310);
        VBox.setVgrow(projects, Priority.ALWAYS);
        VBox.setVgrow(queue, Priority.ALWAYS);
        final HBox panels = new HBox(16, choices, current);
        final VBox root = new VBox(12, title, panels, feedback, cancel);
        root.setPadding(new Insets(18));
        root.setPrefSize(800, 550);
        VBox.setVgrow(panels, Priority.ALWAYS);
        init(root);
        // ListView consumes Escape; cancellation must run before its key
        // handler.
        getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        close();
                        event.consume();
                    }
                });
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    void showColony() {
        controller.begin();
        refresh();
        feedback.setText("");
        showAndWait();
    }

    @Override
    public void updateLanguage(final I18n language) {
        title.setText(language.get(ConstructionText.title,
                controller.getColony().getName()));
        setTitle(title.getText());
        projectsTitle.setText(language.get(ConstructionText.projects));
        queueTitle.setText(language.get(ConstructionText.queue));
        hammerPool.setText(language.get(ConstructionText.hammerPool,
                controller.getColony().getWarehouse()
                        .getGoods(GoodsType.HAMMERS).getAmount()));
        add.setText(language.get(ConstructionText.add));
        up.setText(language.get(ConstructionText.up));
        down.setText(language.get(ConstructionText.down));
        remove.setText(language.get(ConstructionText.remove));
        cancel.setText(language.get(ConstructionText.cancel));
        projects.refresh();
        queue.refresh();
    }

    private void refresh() {
        projects.getSelectionModel().clearSelection();
        controller.repaint();
        hammerPool.setText(i18n.get(ConstructionText.hammerPool,
                controller.getColony().getWarehouse()
                        .getGoods(GoodsType.HAMMERS).getAmount()));
        final List<ColonyBuildingItem> offers = new ArrayList<>();
        ConstructionType.ALL.stream().filter(ConstructionType::canBeConstructed)
                .forEach(type -> offers
                        .add(new ColonyBuildingItemConstruction(type)));
        UnitType.UNIT_TYPES.stream().filter(UnitType::canBeBuildInColony)
                .forEach(type -> offers.add(new ColonyBuildingItemUnit(type)));
        projects.getItems().setAll(offers);
        select(null);
        updateQueueButtons();
    }

    private Optional<String> unavailable(final ColonyBuildingItem item) {
        if (!controller.isCurrent()) {
            return Optional.of(i18n.get(ConstructionText.unavailable));
        }
        final Colony colony = controller.getColony();
        if (item instanceof ColonyBuildingItemConstruction) {
            final ConstructionType type = ((ColonyBuildingItemConstruction) item)
                    .getConstructionType();
            return colony.getColonyBuildingQueue().getUnavailableReason(type)
                    .map(reason -> ConstructionText.reason(i18n, reason, type,
                            colony));
        }
        final UnitType type = ((ColonyBuildingItemUnit) item).getUnitType();
        return colony.getColonyBuildingQueue().getUnitUnavailableReason(type)
                .map(reason -> ConstructionText.reason(i18n, reason, null,
                        colony));
    }

    private void select(final ColonyBuildingItem item) {
        if (item == null) {
            add.setDisable(true);
            detail.setText(i18n.get(ConstructionText.choose));
            selectionState = List.of();
        } else {
            final Optional<String> reason = unavailable(item);
            detail.setText(ConstructionText.name(i18n, item) + "\n"
                    + i18n.get(ConstructionText.cost, item.getRequiredHammers(),
                            item.getRequiredTools())
                    + "\n"
                    + reason.orElse(i18n.get(ConstructionText.available)));
            add.setDisable(reason.isPresent());
            selectionState = currentState();
            feedback.setText("");
        }
    }

    // Copy values shown by the picker; mutable model objects are not a
    // snapshot.
    private List<?> currentState() {
        final Colony colony = controller.getColony();
        return List.of(colony.getOwner(), colony.getUnitsInsideColony().size(),
                colony.getConstructions().stream().map(c -> c.getType())
                        .collect(Collectors.toList()),
                colony.getColonyBuildingQueue().getBuildingQueue().stream()
                        .map(item -> List.of(item.getId(), item.getName()))
                        .collect(Collectors.toList()),
                colony.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount(),
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
    }

    private void confirm() {
        final ColonyBuildingItem selected = projects.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!controller.isCurrent() || !selectionState.equals(currentState())
                || unavailable(selected).isPresent()) {
            rejectChanged();
            return;
        }
        try {
            controller.addAtEnd(selected);
            refresh();
            feedback.setText(i18n.get(ConstructionText.added));
        } catch (IllegalArgumentException rejected) {
            rejectChanged();
        }
    }

    private void rejectChanged() {
        refresh();
        feedback.setText(i18n.get(ConstructionText.changed));
    }

    private void changeQueue(final int direction) {
        final ColonyBuildingItemProgress<?> selected = queue.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            if (direction == 0) {
                controller.removeItem(selected);
            } else {
                controller.moveItem(selected, direction);
            }
            refresh();
            if (direction != 0) {
                queue.getSelectionModel().select(selected);
            }
        } catch (IllegalArgumentException rejected) {
            rejectChanged();
        }
    }

    private void updateQueueButtons() {
        final int index = queue.getSelectionModel().getSelectedIndex();
        up.setDisable(index <= 0 || !controller.isCurrent());
        down.setDisable(index < 0 || index >= queue.getItems().size() - 1
                || !controller.isCurrent());
        remove.setDisable(index < 0 || !controller.isCurrent());
    }

    @Override
    protected void onCancelDialog() {
        close();
    }

    @Override
    public void close() {
        projects.getSelectionModel().clearSelection();
        super.close();
        eventBus.post(new RepaintColonyEvent());
    }
}
