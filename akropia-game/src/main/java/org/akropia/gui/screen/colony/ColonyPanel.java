package org.akropia.gui.screen.colony;

import org.akropia.gui.MainStageBuilder;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.event.UnitMovedToColonyFieldEvent;
import org.akropia.model.event.UnitMovedToConstructionEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import com.google.inject.Provider;
import org.akropia.gui.screen.colony.buildingqueue.QueueDialog;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.UnitTypes;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitWithCargo;
import org.akropia.model.EducationCourse;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * The colony workspace, using shared model operations and cargo controls.
 */
@Listener
public final class ColonyPanel implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainPanel;
    private final HBox overview = new HBox(8);
    private final VBox detail = new VBox(8);
    private final VBox dockDetail = new VBox();
    private final VBox cargoSummary = new VBox(4);
    private final VBox schoolSummary = new VBox(4);
    private final Label politics = new Label();
    private final Provider<QueueDialog> queue;
    private final Provider<DialogEducation> education;
    private final ImageProvider images;
    private final java.util.Map<String, Button> sections = new java.util.LinkedHashMap<>();
    private String selected = "area";
    private final java.util.List<Region> detailSizes = new java.util.ArrayList<>();
    private boolean sizingScheduled;

    void reserveDetailHeight() {
        if (sizingScheduled || detailSizes.isEmpty())
            return;
        sizingScheduled = true;
        javafx.application.Platform.runLater(() -> {
            sizingScheduled = false;
            if (detail.getScene() == null || detail.getWidth() <= 0)
                return;
            final double width = Math.max(0,
                    detail.getWidth() - detail.getInsets().getLeft()
                            - detail.getInsets().getRight());
            double height = 0;
            // Measure the editors in the live stylesheet, without displaying
            // them
            // or leaving duplicate control IDs in the scene graph.
            for (Region editor : detailSizes) {
                editor.setVisible(false);
                editor.setManaged(false);
                detail.getChildren().add(editor);
                editor.applyCss();
                height = Math.max(height, Math.max(editor.minHeight(width),
                        editor.prefHeight(width)));
                detail.getChildren().remove(editor);
            }
            detail.setMinHeight(height + detail.getInsets().getTop()
                    + detail.getInsets().getBottom());
        });
    }

    private final Label goodsTitle = new Label();
    private final PanelColonyConstruction construction;
    private final I18n i18n;

    private final PanelColonyName panelColonyName;

    private final PanelColonyFields colonyFields;

    private final PanelColonyStructures colonyStructures;

    private final PanelColonyGoods goods;

    private final PanelDockColony panelDock;

    private final PanelOutsideColony panelOutsideColony;

    private Colony colony;
    private final ColonyButtonsPanel buttons;

    private final ColonyQuickCargo quickCargo;

    private final EventBus eventBus;

    @Inject
    public ColonyPanel(final PanelColonyFields panelColonyFields,
            final PanelColonyStructures panelColonyStructures,
            final PanelOutsideColony panelOutsideColony,
            final PanelColonyGoods panelColonyGoods,
            final ColonyButtonsPanel colonyButtonsPanel,
            final PanelDockColony panelDockColony,
            final PanelColonyName panelColonyName,
            final ColonyQuickCargo colonyQuickCargo, final EventBus eventBus,
            final PanelColonyConstruction construction, final I18n i18n,
            final Provider<QueueDialog> queue,
            final Provider<DialogEducation> education,
            final ImageProvider images) {
        this.queue = queue;
        this.education = education;
        this.images = images;
        this.construction = construction;
        this.i18n = i18n;
        this.buttons = colonyButtonsPanel;
        this.quickCargo = Preconditions.checkNotNull(colonyQuickCargo);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        colonyButtonsPanel.addQuickCargoButtons(quickCargo.getButtons());
        this.colonyFields = Preconditions.checkNotNull(panelColonyFields);
        this.colonyStructures = Preconditions
                .checkNotNull(panelColonyStructures);
        this.panelDock = Preconditions.checkNotNull(panelDockColony);
        this.goods = Preconditions.checkNotNull(panelColonyGoods);
        this.panelOutsideColony = Preconditions
                .checkNotNull(panelOutsideColony);
        this.panelColonyName = Preconditions.checkNotNull(panelColonyName);

        final ScrollPane outside = new ScrollPane(
                panelOutsideColony.getContent());
        outside.setId("colony-outside-scroll");
        outside.setFitToHeight(true);
        outside.setFitToWidth(true);
        outside.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outside.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        outside.getStyleClass().add("colony-unit-scroll");
        dockDetail.getChildren().add(panelDock.getContent());
        dockDetail.getStyleClass().add("colony-harbor");
        overview.setId("colony-overviews");
        detail.setId("colony-detail");
        detail.widthProperty().addListener(
                (observable, oldWidth, newWidth) -> reserveDetailHeight());
        detail.getStyleClass().add("colony-detail");
        politics.setId("colony-politics");
        addSection("area", colonyFields.getOverview());
        addSection("construction", construction.getContent());
        addSection("cargo", cargoSummary);
        addSection("people", outside);
        addSection("school", schoolSummary);
        colonyFields.onSelect(() -> selectSection("area"));
        final VBox stock = new VBox(2, goodsTitle, goods.getContent());
        stock.getStyleClass().add("colony-stock");
        mainPanel = new VBox(10, politics, colonyStructures.getContent(), stock,
                overview, detail);
        mainPanel.getStyleClass().add("colony-layout");
        mainPanel.setMinWidth(860);
        mainPanel.setMaxWidth(1440);
        selectSection("area");
        updateLanguage(i18n);

        mainPanel.getStylesheets().add(MainStageBuilder.STYLE_SHEET_AKROPIA);
    }

    @Subscribe
    private void onUnitMovedOutsideColony(
            @SuppressWarnings("unused") final UnitMovedOutsideColonyEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToConstruction(
            final UnitMovedToConstructionEvent event) {
        // Keep the native drop target attached until its event finishes.
        javafx.application.Platform.runLater(this::repaint);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToField(final UnitMovedToColonyFieldEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onRepainColonyEvent(final RepaintColonyEvent event) {
        repaint();
    }

    /**
     * Method should show given colony.
     *
     * @param colony required colony to show
     */
    void showColony(final Colony colony) {
        if (this.colony != colony)
            selectSection("area");
        this.colony = Preconditions.checkNotNull(colony);
        panelColonyName.setColony(colony);
        goods.setColony(colony);
        detailSizes.clear();
        detailSizes.add(queue.get().embedded(() -> selectSection("area")));
        detailSizes.add(
                education.get().embedded(colony, () -> selectSection("area")));
        reserveDetailHeight();
        repaint();
    }

    void repaint() {
        if (colony != null) {
            if (!colony.isValid()) {
                colony = null;
                eventBus.post(new ShowScreenEvent(Screen.GAME));
                return;
            }
            buttons.setColony(colony);
            colonyFields.setColony(colony);
            goods.repaint();
            panelDock.repaint();
            colonyStructures.repaint(colony);
            construction.repaint(colony);
            panelOutsideColony.setColony(colony);
            panelOutsideColony.repaint();
            refreshOverview();
        }
    }

    private void addSection(final String key, final Region body) {
        final ButtonIcon icon = java.util.Map.of("area", ButtonIcon.LAND,
                "construction", ButtonIcon.LABOR, "cargo", ButtonIcon.CARGO,
                "people", ButtonIcon.WAIT, "school", ButtonIcon.EDUCATION)
                .get(key);
        final Button title = ActionButtons.button(icon);
        title.setId("colony-section-" + key);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setMinWidth(0);
        title.setWrapText(true);
        title.setOnAction(event -> selectSection(key));
        sections.put(key, title);
        final HBox header = new HBox(title);
        HBox.setHgrow(title, Priority.ALWAYS);
        final VBox card = new VBox(5, header, body);
        card.setId("colony-card-" + key);
        card.getStyleClass().add("colony-overview");
        card.setMinWidth(0);
        card.setPrefWidth(240);
        HBox.setHgrow(card, Priority.ALWAYS);
        header.setOnDragDetected(event -> {
            final javafx.scene.input.ClipboardContent clipboard = new javafx.scene.input.ClipboardContent();
            clipboard.putString("colony-section:" + key);
            header.startDragAndDrop(javafx.scene.input.TransferMode.MOVE)
                    .setContent(clipboard);
            event.consume();
        });
        header.setOnDragOver(event -> {
            if (event.getGestureSource() != header
                    && event.getDragboard().hasString() && event.getDragboard()
                            .getString().startsWith("colony-section:")) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                event.consume();
            }
        });
        header.setOnDragDropped(event -> {
            final String source = event.getDragboard().getString();
            if (source != null && source.startsWith("colony-section:")) {
                final javafx.scene.Node moving = overview
                        .lookup("#colony-card-" + source.substring(15));
                if (moving != null && moving != card) {
                    final int target = overview.getChildren().indexOf(card);
                    overview.getChildren().remove(moving);
                    overview.getChildren().add(target, moving);
                    event.setDropCompleted(true);
                }
                event.consume();
            }
        });
        overview.getChildren().add(card);
    }

    boolean closeEditor() {
        if ("construction".equals(selected) || "school".equals(selected)) {
            selectSection("area");
            return true;
        }
        return false;
    }

    private void selectSection(final String key) {
        if (key.equals(selected) && !detail.getChildren().isEmpty())
            return;
        selected = key;
        detail.getChildren().setAll(colonyFields.getContent(), dockDetail);
        colonyFields.getContent().setVisible(key.equals("area"));
        colonyFields.getContent().setManaged(key.equals("area"));
        dockDetail.setVisible(key.equals("cargo"));
        dockDetail.setManaged(key.equals("cargo"));
        sections.forEach((name, button) -> button.pseudoClassStateChanged(
                javafx.css.PseudoClass.getPseudoClass("selected"),
                name.equals(key)));
        switch (key) {
        case "area":
            break;
        case "construction":
            if (colony != null)
                detail.getChildren()
                        .add(queue.get().embedded(() -> selectSection("area")));
            break;
        case "cargo":
            break;
        case "school":
            if (colony != null)
                detail.getChildren().add(education.get().embedded(colony,
                        () -> selectSection("area")));
            break;
        default:
            detail.getChildren().add(new Label(i18n.get(ColonyMsg.peopleHint)));
        }
    }

    private Label summary(final String text) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private ImageView portrait(final Unit unit) {
        final ImageView image = new ImageView(images.getUnitImage(unit));
        image.setFitWidth(28);
        image.setFitHeight(28);
        image.setPreserveRatio(true);
        return image;
    }

    private void refreshOverview() {
        final int support = colony.getRebelSupport().getDisplayPercent();
        politics.setText(i18n.get(ColonyMsg.overviewPolitics,
                colony.getPopulation(), support, 100 - support));
        cargoSummary.getChildren().clear();
        for (UnitWithCargo transport : colony.getUnitsInPort()) {
            final Button choose = ActionButtons.button(ButtonIcon.CARGO,
                    i18n.get(UnitTypes.getUnitName(transport.getType())) + " #"
                            + transport.getId());
            choose.setGraphic(portrait(transport));
            choose.setOnAction(event -> {
                selectSection("cargo");
                panelDock.selectShip(transport);
            });
            cargoSummary.getChildren().add(choose);
            final String cargo = transport.getCargo().getSlots().stream()
                    .filter(slot -> !slot.isEmpty())
                    .map(slot -> slot.getGoods().map(goods -> goods.getAmount()
                            + " " + i18n.get(org.akropia.gui.GoodsTypeName
                                    .getNameForGoodsType(goods.getType())))
                            .orElseGet(() -> slot.getUnit()
                                    .map(unit -> i18n
                                            .get(UnitTypes.getUnitName(
                                                    unit.getType()))
                                            + " #" + unit.getId())
                                    .orElse("")))
                    .collect(java.util.stream.Collectors.joining(" · "));
            cargoSummary.getChildren().add(summary(
                    cargo.isEmpty() ? i18n.get(ColonyMsg.emptyCargo) : cargo));
        }
        if (cargoSummary.getChildren().isEmpty())
            cargoSummary.getChildren()
                    .add(summary(i18n.get(ColonyMsg.emptyHarbor)));
        schoolSummary.getChildren().clear();
        for (EducationCourse course : colony.getEducationCourses()) {
            final Label people = summary(i18n.get(EducationText.course,
                    course.getTeacherId() == null ? "—" : course.getTeacherId(),
                    course.getStudentId(),
                    i18n.get(UnitTypes.getUnitName(course.getSkill())),
                    course.getCompletedUpdates(), course.getRequiredUpdates(),
                    i18n.get(course.isPaused() ? EducationText.paused
                            : EducationText.active)));
            final HBox portraits = new HBox(4);
            colony.getUnitsInsideColony().stream()
                    .filter(unit -> java.util.Objects.equals(unit.getId(),
                            course.getTeacherId())
                            || unit.getId() == course.getStudentId())
                    .forEach(unit -> portraits.getChildren()
                            .add(portrait(unit)));
            final ProgressBar progress = new ProgressBar(
                    (double) course.getCompletedUpdates()
                            / course.getRequiredUpdates());
            progress.getStyleClass().add("construction-progress");
            progress.setMaxWidth(Double.MAX_VALUE);
            schoolSummary.getChildren().addAll(portraits, people, progress);
            colony.getUnitsInsideColony().stream()
                    .filter(unit -> unit.getId() == course.getStudentId())
                    .findFirst().ifPresent(unit -> {
                        final String workplace;
                        if (unit.getPlace() instanceof org.akropia.model.PlaceColonyField) {
                            final org.akropia.model.PlaceColonyField field = unit
                                    .getPlaceColonyField();
                            workplace = i18n.get(ColonyMsg.studentField,
                                    field.getLocation().getX(),
                                    field.getLocation().getY());
                        } else if (unit
                                .getPlace() instanceof org.akropia.model.PlaceConstructionSlot) {
                            workplace = i18n.get(ConstructionTypeName
                                    .getNameForType(unit.getPlaceConstruction()
                                            .getConstructionSlot()
                                            .getConstructionType()));
                        } else {
                            workplace = i18n.get(ColonyMsg.outsideSection);
                        }
                        schoolSummary.getChildren().add(summary(i18n
                                .get(ColonyMsg.studentWorkplace, workplace)));
                    });
        }
        if (schoolSummary.getChildren().isEmpty())
            schoolSummary.getChildren()
                    .add(summary(i18n.get(ColonyMsg.emptySchool)));
    }

    public Colony getColony() {
        return colony;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        panelDock.updateLanguage(i18n);
        quickCargo.updateLanguage(i18n);
        goodsTitle.setText(i18n.get(ColonyMsg.stockSection));
        sections.get("area").setText("1 · " + i18n.get(ColonyMsg.overviewArea));
        sections.get("construction")
                .setText("2 · " + i18n.get(ColonyMsg.buildingSection));
        sections.get("cargo")
                .setText("3 · " + i18n.get(ColonyMsg.overviewCargo));
        sections.get("people")
                .setText("4 · " + i18n.get(ColonyMsg.overviewPeople));
        sections.get("school").setText(i18n.get(ColonyMsg.schoolSection));
        repaint();
    }
}
