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
import javafx.scene.control.Button;
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
    private final VBox schoolSummary = new VBox(4);
    private final Label politics = new Label();
    private final Provider<QueueDialog> queue;
    private final Provider<DialogEducation> education;
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
            final Provider<DialogEducation> education) {
        this.queue = queue;
        this.education = education;
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
        final Label outsideTitle = new Label();
        outsideTitle.setId("colony-outside-title");
        dockDetail.getChildren().addAll(panelDock.getContent(),
                new javafx.scene.control.Separator(), outsideTitle, outside);
        dockDetail.setId("colony-cargo-depot");
        dockDetail.getStyleClass().add("colony-depot");
        dockDetail.getStyleClass().add("colony-harbor");
        overview.setId("colony-overviews");
        detail.setId("colony-detail");
        detail.widthProperty().addListener(
                (observable, oldWidth, newWidth) -> reserveDetailHeight());
        detail.getStyleClass().add("colony-detail");
        politics.setId("colony-politics");
        addSection("area", colonyFields.getOverview());
        addSection("construction", construction.getContent());
        addSection("school", schoolSummary);
        overview.getChildren().add(dockDetail);
        HBox.setHgrow(dockDetail, Priority.ALWAYS);
        dockDetail.setMinWidth(320);
        dockDetail.setPrefWidth(480);
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
            detail.getChildren().stream().filter(SchoolPanel.class::isInstance)
                    .map(SchoolPanel.class::cast).forEach(SchoolPanel::refresh);
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
        detail.getChildren().setAll(colonyFields.getContent());
        colonyFields.getContent().setVisible(key.equals("area"));
        colonyFields.getContent().setManaged(key.equals("area"));

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
        case "school":
            if (colony != null)
                detail.getChildren().add(education.get().embedded(colony,
                        () -> selectSection("area")));
            break;
        default:
            detail.getChildren().add(new Label(i18n.get(ColonyMsg.peopleHint)));
        }
    }

    private void refreshOverview() {
        final int support = colony.getRebelSupport().getDisplayPercent();
        politics.setText(i18n.get(ColonyMsg.overviewPolitics,
                colony.getPopulation(), support, 100 - support));
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
        sections.get("area").setText(i18n.get(ColonyMsg.overviewArea));
        sections.get("construction")
                .setText(i18n.get(ColonyMsg.buildingSection));

        sections.get("school").setText(i18n.get(ColonyMsg.schoolSection));
        ((Label) dockDetail.lookup("#colony-outside-title"))
                .setText(i18n.get(ColonyMsg.overviewPeople));
        repaint();
    }
}
