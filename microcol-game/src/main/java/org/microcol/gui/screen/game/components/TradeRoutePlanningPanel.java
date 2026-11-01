package org.microcol.gui.screen.game.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.screen.colony.DialogTradeRoute;
import org.microcol.gui.screen.colony.DialogTradeRouteCargo;
import org.microcol.gui.screen.colony.TradeRouteText;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.game.gamepanel.TradeRouteColors;
import org.microcol.gui.screen.game.gamepanel.TradeRoutePlanning;
import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRoute;
import org.microcol.model.TradeRouteKind;
import org.microcol.model.TradeRoutePreview;
import org.microcol.model.TradeRouteStop;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Right panel of the map trade route planner. It replaces the normal right
 * panel while the planner is on: a keyboard list of the player's routes, which
 * also separates routes drawn over the same squares, and the selected route's
 * stops, goods, carriers and paths. Editing opens the existing route windows,
 * whose model operations decide every change; canceling them changes nothing.
 * The panel also owns the planner's map toggle and the toolbar's New trade
 * route button.
 */
@Singleton
public final class TradeRoutePlanningPanel
        implements JavaFxComponent, UpdatableLanguage {

    private final GameModelController game;
    private final TradeRoutePlanning planning;
    private final DialogTradeRoute newRouteDialog;
    private final DialogTradeRouteCargo routeDialog;
    private final EventBus eventBus;
    private I18n i18n;

    private final VBox content = new VBox(8);
    private final Label title = new Label();
    private final Label rule = new Label();
    private final Label empty = new Label();
    private final Label message = new Label();
    private final Label listLabel = new Label();
    private final ListView<TradeRoutePreview> list = new ListView<>();
    private final VBox detail = new VBox(6);
    private final Label name = new Label();
    private final Label kind = new Label();
    private final Label carriers = new Label();
    private final Label previewNote = new Label();
    private final Label stopsHeading = new Label();
    private final Label stops = new Label();
    private final Label legsHeading = new Label();
    private final Label legs = new Label();
    private final Button edit = ActionButtons.button(ButtonIcon.CARGO);
    private final Button back = ActionButtons.button(ButtonIcon.BACK);
    private final Button toggle = ActionButtons.button(ButtonIcon.TRADE);
    private final Button newRoute = ActionButtons.button(ButtonIcon.TRADE);
    /** Set while the list is rebuilt, so its own events do not recurse. */
    private boolean refreshing;

    @Inject
    TradeRoutePlanningPanel(final GameModelController game,
            final TradeRoutePlanning planning,
            final DialogTradeRoute newRouteDialog,
            final DialogTradeRouteCargo routeDialog, final EventBus eventBus,
            final I18n i18n) {
        this.game = Preconditions.checkNotNull(game);
        this.planning = Preconditions.checkNotNull(planning);
        this.newRouteDialog = Preconditions.checkNotNull(newRouteDialog);
        this.routeDialog = Preconditions.checkNotNull(routeDialog);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);

        content.setId("tradeRoutePlanningPanel");
        content.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);
        content.getStyleClass().add("trade-route-planning-panel");
        content.setPrefWidth(260);
        content.setMinWidth(240);
        content.setMaxWidth(260);
        title.setId("tradeRoutePlanningTitle");
        title.getStyleClass().add("trade-route-planning-title");
        rule.setId("tradeRoutePlanningRule");
        empty.setId("tradeRoutePlanningEmpty");
        message.setId("tradeRoutePlanningMessage");
        name.setId("tradeRoutePlanningName");
        name.getStyleClass().add("trade-route-planning-title");
        kind.setId("tradeRoutePlanningKind");
        carriers.setId("tradeRoutePlanningCarriers");
        previewNote.setId("tradeRoutePlanningPreviewFor");
        stops.setId("tradeRoutePlanningStops");
        legs.setId("tradeRoutePlanningLegs");
        for (final Label label : List.of(rule, empty, message, name, kind,
                carriers, previewNote, stopsHeading, stops, legsHeading,
                legs)) {
            label.setWrapText(true);
            label.setMinHeight(Region.USE_PREF_SIZE);
            label.setMaxWidth(240);
        }
        for (final Label label : List.of(empty, message)) {
            label.managedProperty().bind(label.visibleProperty());
        }

        list.setId("tradeRoutePlanningList");
        list.setPrefHeight(150);
        list.setMinHeight(90);
        listLabel.setLabelFor(list);
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(final TradeRoutePreview item,
                    final boolean isEmpty) {
                super.updateItem(item, isEmpty);
                if (isEmpty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setId(null);
                } else {
                    final Rectangle swatch = new Rectangle(14, 14,
                            TradeRouteColors.colorOf(item));
                    swatch.setStroke(javafx.scene.paint.Color.web("#34332b"));
                    setGraphic(swatch);
                    setText(item.getRoute().getName());
                    setId("tradeRoutePlanningItem-" + item.getRoute().getId());
                }
            }
        });
        list.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> {
                    if (!refreshing) {
                        planning.select(current == null ? null
                                : current.getRoute().getId());
                    }
                });

        edit.setId("tradeRoutePlanningEdit");
        edit.setOnAction(event -> editSelected());
        back.setId("tradeRoutePlanningBack");
        back.setOnAction(event -> planning.setActive(false));
        toggle.setId("tradeRoutePlanningToggle");
        toggle.getStyleClass().add("trade-route-planning-toggle");
        toggle.setOnAction(event -> planning.setActive(!planning.isActive()));
        newRoute.setId("tradeRoutePlanningNew");
        newRoute.setOnAction(event -> createRoute());
        newRoute.setVisible(false);
        newRoute.setManaged(false);
        for (final Button button : List.of(toggle, newRoute, edit)) {
            button.setOnMouseEntered(
                    event -> eventBus.post(new StatusBarMessageEvent(
                            button.getAccessibleHelp(), Source.GAME)));
            button.setOnMouseExited(event -> eventBus
                    .post(new StatusBarMessageEvent(Source.GAME)));
            button.accessibleTextProperty().bind(button.textProperty());
        }
        back.accessibleTextProperty().bind(back.textProperty());

        detail.setId("tradeRoutePlanningDetail");
        detail.getChildren().addAll(name, kind, carriers, previewNote,
                stopsHeading, stops, legsHeading, legs, rule);
        final ScrollPane scroll = new ScrollPane(detail);
        scroll.setId("tradeRoutePlanningScroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Only the long detail scrolls; it gives up height first, so the
        // actions below stay reachable in a small window.
        scroll.setMinHeight(60);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        // The route's edit action and the way back stay in a fixed area
        // below the scrolling detail, in every window size and language.
        for (final Button button : List.of(edit, back)) {
            button.setWrapText(true);
            button.setMaxWidth(240);
            button.setMinHeight(Region.USE_PREF_SIZE);
        }
        final VBox actions = new VBox(6, edit, back);
        actions.setId("tradeRoutePlanningActions");
        actions.setMinHeight(Region.USE_PREF_SIZE);
        content.getChildren().addAll(title, empty, message, listLabel, list,
                scroll, actions);
        planning.addListener(this::refresh);
        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return content;
    }

    /** @return map control that switches the planner on and off */
    public Button getToggle() {
        return toggle;
    }

    /** @return toolbar control shown while the planner is on */
    public Button getNewRouteButton() {
        return newRoute;
    }

    /**
     * Enables the planner's own controls together with the rest of the game
     * bar; they are disabled while the human may not act.
     *
     * @param disable whether the controls are disabled
     */
    public void setControlsDisabled(final boolean disable) {
        toggle.setDisable(disable);
        newRoute.setDisable(disable);
        edit.setDisable(disable || planning.getSelected().isEmpty());
    }

    @Override
    public void updateLanguage(final I18n value) {
        i18n = Preconditions.checkNotNull(value);
        title.setText(i18n.get(TradeRoutePlanningText.title));
        rule.setText(i18n.get(TradeRoutePlanningText.rule));
        empty.setText(i18n.get(TradeRoutePlanningText.empty));
        listLabel.setText(i18n.get(TradeRoutePlanningText.listLabel));
        list.setAccessibleText(listLabel.getText());
        stopsHeading.setText(i18n.get(TradeRoutePlanningText.stopsHeading));
        legsHeading.setText(i18n.get(TradeRoutePlanningText.legsHeading));
        edit.setText(i18n.get(TradeRoutePlanningText.edit));
        edit.setAccessibleHelp(i18n.get(TradeRoutePlanningText.editHelp));
        back.setText(i18n.get(TradeRoutePlanningText.back));
        newRoute.setText(i18n.get(TradeRoutePlanningText.newRoute));
        newRoute.setAccessibleHelp(
                i18n.get(TradeRoutePlanningText.newRouteHelp));
        toggle.setAccessibleHelp(i18n.get(TradeRoutePlanningText.toggleHelp));
        refresh();
    }

    /**
     * Rebuilds the toggle, the list and the selected route's detail from the
     * planner's current previews.
     */
    private void refresh() {
        final boolean active = planning.isActive();
        toggle.setText(i18n.get(active ? TradeRoutePlanningText.toggleClose
                : TradeRoutePlanningText.toggleOpen));
        toggle.setGraphic(
                (active ? ButtonIcon.BACK : ButtonIcon.TRADE).createView());
        newRoute.setVisible(active);
        newRoute.setManaged(active);
        if (!active) {
            message.setText("");
            message.setVisible(false);
            return;
        }
        final List<TradeRoutePreview> previews = planning.getPreviews();
        final Optional<TradeRoutePreview> selected = planning.getSelected();
        refreshing = true;
        try {
            list.getItems().setAll(previews);
            if (selected.isPresent()) {
                final int index = previews.indexOf(selected.get());
                list.getSelectionModel().select(index);
                list.scrollTo(index);
            } else {
                list.getSelectionModel().clearSelection();
            }
        } finally {
            refreshing = false;
        }
        empty.setVisible(previews.isEmpty());
        list.setDisable(previews.isEmpty());
        message.setVisible(!message.getText().isEmpty());
        showDetail(selected);
    }

    private void showDetail(final Optional<TradeRoutePreview> selected) {
        edit.setDisable(selected.isEmpty() || !game.isHumanTurnReady());
        if (selected.isEmpty()) {
            name.setText(i18n.get(TradeRoutePlanningText.noSelection));
            for (final Label label : List.of(kind, carriers, previewNote, stops,
                    legs)) {
                label.setText("");
            }
            stopsHeading.setVisible(false);
            legsHeading.setVisible(false);
            return;
        }
        final TradeRoutePreview preview = selected.get();
        final TradeRoute route = preview.getRoute();
        stopsHeading.setVisible(true);
        legsHeading.setVisible(true);
        name.setText(route.getName());
        kind.setText(kindText(route.getKind()));
        carriers.setText(carriersText(route));
        previewNote.setText(preview.getCarrier()
                .map(carrier -> i18n.get(TradeRoutePlanningText.previewFor,
                        carrierName(carrier)))
                .orElseGet(
                        () -> i18n.get(TradeRoutePlanningText.previewUnassigned,
                                kindText(route.getKind()))));
        final List<String> stopLines = new ArrayList<>();
        for (int index = 0; index < route.getStops().size(); index++) {
            final TradeRouteStop stop = route.getStops().get(index);
            stopLines.add(i18n.get(TradeRoutePlanningText.stopLine, index + 1,
                    stopName(stop), goodsText(stop.getUnloadGoods()),
                    goodsText(stop.getLoadGoods())));
        }
        stops.setText(String.join("\n", stopLines));
        legs.setText(preview.getLegs().stream().map(this::legText)
                .collect(Collectors.joining("\n")));
    }

    /**
     * Status text for a hovered map square: the name and basic details of each
     * route drawn over it. Empty when no route passes the square.
     *
     * @param location required hovered square
     * @return the text, or empty
     */
    public Optional<String> hoverText(final Location location) {
        if (!planning.isActive()) {
            return Optional.empty();
        }
        final List<String> routes = planning.getRoutesAt(location).stream()
                .map(preview -> i18n.get(TradeRoutePlanningText.hoverRoute,
                        preview.getRoute().getName(),
                        kindText(preview.getRoute().getKind()),
                        preview.getRoute().getStops().size(),
                        carriersText(preview.getRoute())))
                .collect(Collectors.toList());
        if (routes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(routes.size() == 1 ? routes.get(0)
                : i18n.get(TradeRoutePlanningText.hoverRoutes,
                        String.join(" ", routes)));
    }

    private String legText(final TradeRoutePreview.Leg leg) {
        final String from = stopName(leg.getFrom());
        final String to = stopName(leg.getTo());
        if (leg.getProblem().isPresent()) {
            return i18n.get(
                    TradeRoutePlanningText.valueOf(
                            "legProblem" + leg.getProblem().get().name()),
                    from, to);
        }
        return leg.getPaths().stream()
                .map(path -> i18n.get(
                        TradeRoutePlanningText
                                .valueOf("leg" + path.getKind().name()),
                        from, to, path.getSquares().size() - 1,
                        pointsText(path.getCostThirds())))
                .collect(Collectors.joining("\n"));
    }

    private String pointsText(final int thirds) {
        final int whole = thirds / 3;
        final int rest = thirds % 3;
        if (rest == 0) {
            return i18n.get(TradeRoutePlanningText.pointsWhole, whole);
        }
        if (whole == 0) {
            return i18n.get(TradeRoutePlanningText.pointsThirds, rest);
        }
        return i18n.get(TradeRoutePlanningText.points, whole, rest);
    }

    private String kindText(final TradeRouteKind value) {
        return i18n.get(TradeRoutePlanningText.valueOf("kind" + value.name()));
    }

    private String carriersText(final TradeRoute route) {
        final List<UnitWithCargo> assigned = game.getModel()
                .getTradeRouteAssignedCarriers(game.getHumanPlayer(), route);
        if (assigned.isEmpty()) {
            return i18n.get(TradeRoutePlanningText.carriersNone);
        }
        return i18n.get(TradeRoutePlanningText.carriers, assigned.stream()
                .map(this::carrierName).collect(Collectors.joining(", ")));
    }

    private String carrierName(final UnitWithCargo carrier) {
        return i18n.get(TradeRouteText.carrierItem,
                i18n.get(UnitTypes.getUnitName(carrier.getType())),
                carrier.getId());
    }

    private String stopName(final TradeRouteStop stop) {
        if (stop.isEurope()) {
            return i18n.get(TradeRouteText.europe);
        }
        return game.getModel().getColonyById(stop.getColonyId())
                .filter(colony -> colony.getOwner() == game.getHumanPlayer())
                .map(Colony::getName)
                .orElse(i18n.get(TradeRoutePlanningText.stopLost));
    }

    private String goodsText(final List<GoodsType> goods) {
        if (goods.isEmpty()) {
            return i18n.get(TradeRoutePlanningText.goodsNone);
        }
        return goods.stream()
                .map(type -> i18n.get(GoodsTypeName.getNameForGoodsType(type)))
                .collect(Collectors.joining(", "));
    }

    /**
     * Opens the shared route window with the selected route. Whatever the
     * player saves there is read back into the map and this panel.
     */
    private void editSelected() {
        final Optional<TradeRoutePreview> selected = planning.getSelected();
        if (selected.isEmpty()) {
            return;
        }
        message.setText("");
        routeDialog.showForRoute(selected.get().getRoute());
        planning.invalidate();
    }

    /**
     * Opens the shared route editor without a colony screen. A created route is
     * drawn and selected; a canceled draft changes nothing.
     */
    private void createRoute() {
        if (!planning.isActive()) {
            return;
        }
        newRouteDialog.showNew();
        final Optional<TradeRoute> created = newRouteDialog.getCreatedRoute();
        if (created.isPresent()) {
            message.setText(i18n.get(TradeRoutePlanningText.created,
                    created.get().getName()));
            planning.invalidate();
            planning.select(created.get().getId());
        }
        refresh();
    }
}
