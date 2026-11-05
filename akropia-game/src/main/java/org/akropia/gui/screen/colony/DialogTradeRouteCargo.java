package org.akropia.gui.screen.colony;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.InGamePage;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import com.google.inject.Singleton;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteAssignment;
import org.akropia.model.TradeRouteAssignmentProblem;
import org.akropia.model.TradeRouteProblem;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.TradeRouteStopProblem;
import org.akropia.model.TradeRouteStopResult;
import org.akropia.model.TradeRouteTransfer;
import org.akropia.model.unit.UnitWithCargo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.util.StringConverter;

/**
 * Cargo of the stops of the player's saved trade routes. The dialog stages the
 * ordered unload and load goods of one stop and stores them only when the
 * player presses Save. A ship or wagon standing at the stop is processed only
 * by the explicit button; opening the dialog or loading a game moves nothing.
 * Every rule, quantity and price comes from the model, whose result lists each
 * configured entry with its reason.
 */
@Singleton
public final class DialogTradeRouteCargo implements GameScreen {

    private final GameModelController game;
    private final I18n i18n;
    private final EventBus eventBus;
    private final Label caption = new Label();
    private final Label rule = new Label();
    private final Label routeLabel = new Label();
    private final Label stopLabel = new Label();
    private final ComboBox<TradeRoute> routeBox = new ComboBox<>();
    private final ComboBox<Integer> stopBox = new ComboBox<>();
    private final TradeRouteGoodsList unload;
    private final TradeRouteGoodsList load;
    private final Button save = ActionButtons.button(ButtonIcon.SAVE);
    private final Label message = new Label();
    private final Label carrierLabel = new Label();
    private final ComboBox<UnitWithCargo> carrierBox = new ComboBox<>();
    private final Label carrierStatus = new Label();
    private final Button process = ActionButtons.button(ButtonIcon.PLAY);
    private final Label assignRule = new Label();
    private final Button assign = ActionButtons.button(ButtonIcon.CONFIRM);
    private final Label assignStatus = new Label();
    private final Label assignedHeading = new Label();
    private final Label assigned = new Label();
    private final Button cancelAssignment = ActionButtons
            .button(ButtonIcon.REMOVE);
    private final Button deleteRoute = ActionButtons.button(ButtonIcon.DELETE);
    private final Label replaceStopLabel = new Label();
    private final ComboBox<TradeRouteStop> replaceBox = new ComboBox<>();
    private final Button replaceButton = ActionButtons
            .button(ButtonIcon.REPLACE);
    private final Label replaceStatus = new Label();
    private final Label assignedPickLabel = new Label();
    /** Every carrier that follows the route, wherever it is now. */
    private final ComboBox<UnitWithCargo> assignedBox = new ComboBox<>();
    private final Label deleteInfo = new Label();
    private final InGamePage page;
    private final org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning planning;
    private Colony originColony;
    private final Label stopSummary = new Label();
    private final Label routeSummary = new Label();
    private final Label goodsStop = new Label();
    private final Label goodsHeading = new Label();
    private final Label carriersHeading = new Label();
    private final Label processRule = new Label();
    private final TitledPane assignedPane = new TitledPane();
    private final TitledPane deletePane = new TitledPane();
    /** Route whose deletion was asked for and awaits the second press. */
    private Integer deleteAskedFor;
    private final Label resultHeading = new Label();
    private final Label result = new Label();
    private final Button close = ActionButtons.button(ButtonIcon.CANCEL);
    private Model model;
    private Player player;
    /** Set while controls are rebuilt, so their own events do not recurse. */
    private boolean refreshing;

    @Inject
    public DialogTradeRouteCargo(final I18n i18n,
            final GameModelController game, final EventBus eventBus,
            final InGamePage page,
            final org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning planning) {
        this.planning = planning;
        this.page = page;
        this.i18n = i18n;
        this.game = game;
        this.eventBus = eventBus;
        caption.setId("tradeRouteCargoTitle");
        caption.setWrapText(true);
        rule.setId("tradeRouteCargoRule");
        rule.setWrapText(true);
        rule.setMaxWidth(740);
        routeBox.setId("tradeRouteCargoRoute");
        routeBox.setConverter(new StringConverter<TradeRoute>() {
            @Override
            public String toString(final TradeRoute route) {
                return route == null ? "" : route.getName();
            }

            @Override
            public TradeRoute fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        routeBox.setOnAction(event -> {
            if (!refreshing) {
                deleteAskedFor = null;
                fillStops(0);
                loadStop();
            }
        });
        stopBox.setId("tradeRouteCargoStop");
        stopBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(final Integer index) {
                return index == null ? "" : stopText(index);
            }

            @Override
            public Integer fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        stopBox.setOnAction(event -> {
            if (!refreshing) {
                loadStop();
            }
        });
        unload = new TradeRouteGoodsList("tradeRouteUnload", i18n,
                this::updateState);
        load = new TradeRouteGoodsList("tradeRouteLoad", i18n,
                this::updateState);
        save.setId("tradeRouteCargoSave");
        save.setOnAction(event -> saveCargo());
        message.setId("tradeRouteCargoMessage");
        message.setWrapText(true);
        message.setMinHeight(20);
        message.setMaxWidth(740);
        carrierBox.setId("tradeRouteCargoCarrier");
        carrierBox.setConverter(new StringConverter<UnitWithCargo>() {
            @Override
            public String toString(final UnitWithCargo carrier) {
                return carrier == null ? ""
                        : i18n.get(TradeRouteText.carrierItem,
                                i18n.get(UnitTypes
                                        .getUnitName(carrier.getType())),
                                carrier.getId());
            }

            @Override
            public UnitWithCargo fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        carrierBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
        carrierStatus.setId("tradeRouteCargoCarrierStatus");
        carrierStatus.setWrapText(true);
        carrierStatus.setMaxWidth(740);
        process.setId("tradeRouteCargoProcess");
        process.setOnAction(event -> processStop());
        assignRule.setId("tradeRouteAssignRule");
        assignRule.setWrapText(true);
        assignRule.setMaxWidth(740);
        assign.setId("tradeRouteAssign");
        assign.setDisable(true);
        assign.setOnAction(event -> assignCarrier());
        assignStatus.setId("tradeRouteAssignStatus");
        assignStatus.setWrapText(true);
        assignStatus.setMaxWidth(740);
        assignedHeading.setId("tradeRouteAssignedHeading");
        assigned.setId("tradeRouteAssigned");
        assigned.setWrapText(true);
        assigned.setMaxWidth(740);
        assignedBox.setId("tradeRouteAssignedCarrier");
        assignedBox.setConverter(new StringConverter<UnitWithCargo>() {
            @Override
            public String toString(final UnitWithCargo carrier) {
                return carrier == null ? "" : carrierName(carrier);
            }

            @Override
            public UnitWithCargo fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        assignedBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
        cancelAssignment.setId("tradeRouteCancelAssignment");
        cancelAssignment.setOnAction(event -> cancelAssignment());
        deleteRoute.setId("tradeRouteDelete");
        deleteRoute.setOnAction(event -> deleteRoute());
        deleteInfo.setId("tradeRouteDeleteInfo");
        deleteInfo.setWrapText(true);
        deleteInfo.setMaxWidth(740);
        replaceBox.setId("tradeRouteReplaceStop");
        replaceBox.setConverter(new StringConverter<TradeRouteStop>() {
            @Override
            public String toString(final TradeRouteStop stop) {
                return stop == null ? "" : stopName(stop);
            }

            @Override
            public TradeRouteStop fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        replaceBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
        replaceButton.setId("tradeRouteReplaceButton");
        replaceButton.setOnAction(event -> replaceStop());
        replaceStatus.setId("tradeRouteReplaceStatus");
        replaceStatus.setWrapText(true);
        replaceStatus.setMaxWidth(740);
        resultHeading.setId("tradeRouteCargoResultHeading");
        result.setId("tradeRouteCargoResult");
        result.setWrapText(true);
        result.setMaxWidth(740);
        close.setId("tradeRouteCargoClose");
        close.setCancelButton(true);
        close.setOnAction(event -> close());
        final VBox stops = new VBox(10, stopLabel, stopSummary, stopBox,
                replaceStopLabel, replaceBox, replaceButton, replaceStatus);
        stops.setId("tradeRouteStopsColumn");
        final VBox goods = new VBox(10, goodsHeading, goodsStop, rule,
                unload.getNode(), load.getNode(), save, message);
        goods.setId("tradeRouteGoodsColumn");
        assignedPane.setContent(new VBox(10, assigned, assignedPickLabel,
                assignedBox, cancelAssignment));
        assignedPane.setId("tradeRouteAssignedPane");
        assignedPane.setAnimated(false);
        assignedPane.setExpanded(false);
        deletePane.setContent(new VBox(10, deleteRoute, deleteInfo));
        deletePane.setId("tradeRouteDeletePane");
        deletePane.setAnimated(false);
        deletePane.setExpanded(false);
        final VBox carriers = new VBox(10, carriersHeading, carrierLabel,
                carrierBox, carrierStatus, assignRule, assign, assignStatus,
                assignedPane, processRule, process, resultHeading, result,
                deletePane);
        carriers.setId("tradeRouteCarriersColumn");
        for (final VBox column : List.of(stops, goods, carriers)) {
            column.setPrefWidth(340);
            column.setMinWidth(280);
            column.getStyleClass().add("trade-route-column");
        }
        final FlowPane columns = new FlowPane(20, 20, stops, goods, carriers);
        final VBox root = new VBox(12, columns);
        root.setId("tradeRouteCargoDialog");
        stopSummary.setWrapText(true);
        stopSummary.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        routeSummary.setWrapText(true);
        processRule.setWrapText(true);
        page.setTitle(caption);
        caption.setWrapText(true);
        page.setToolbar(
                new VBox(4, new FlowPane(12, 6, close, routeLabel, routeBox),
                        routeSummary));
        page.setContent(root);
        page.getContent().lookupAll(".screen-scroll")
                .forEach(node -> node.setId("tradeRouteCargoScroll"));
        page.getContent().setId("tradeRouteCargoPage");
        page.getContent().getStylesheets().add(getClass()
                .getResource("/gui/TradeRouteCargo.css").toExternalForm());
        page.getContent().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
    }

    public void close() {
        planning.invalidate();
        eventBus.post(originColony == null ? new ShowScreenEvent(Screen.GAME)
                : new ShowScreenEvent(Screen.COLONY, originColony));
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void beforeShow() {
        page.beforeShow();
    }

    @Override
    public void beforeHide() {
    }

    @Override
    public void updateLanguage(final I18n texts) {
        page.updateLanguage(texts);
        translate();
    }

    private String carrierName(final UnitWithCargo carrier) {
        return i18n.get(TradeRouteText.carrierItem,
                i18n.get(UnitTypes.getUnitName(carrier.getType())),
                carrier.getId());
    }

    private String stopText(final int index) {
        final TradeRoute route = routeBox.getValue();
        if (route == null || index < 0 || index >= route.getStops().size()) {
            return "";
        }
        return i18n.get(TradeRouteText.cargoStopItem, index + 1,
                stopName(route.getStops().get(index)));
    }

    private String stopName(final TradeRouteStop stop) {
        if (stop.isEurope()) {
            return i18n.get(TradeRouteText.europe);
        }
        return model.getColonyById(stop.getColonyId()).map(Colony::getName)
                .orElse(i18n.get(TradeRouteText.stopLost));
    }

    /** Sets the fixed texts in the language current when the dialog opens. */
    private void translate() {
        goodsHeading.setText(i18n.get(TradeRouteText.goodsSection));
        carriersHeading.setText(i18n.get(TradeRouteText.carriersSection));
        processRule.setText(i18n.get(TradeRouteText.processRule));
        assignedPane.setText(i18n.get(TradeRouteText.assignedHeading));
        deletePane.setText(i18n.get(TradeRouteText.deleteRoute));
        caption.setText(i18n.get(TradeRouteText.cargoTitle));
        rule.setText(i18n.get(TradeRouteText.cargoRule));
        routeLabel.setText(i18n.get(TradeRouteText.cargoRouteLabel));
        stopLabel.setText(i18n.get(TradeRouteText.cargoStopLabel));
        routeBox.setAccessibleText(routeLabel.getText());
        stopBox.setAccessibleText(stopLabel.getText());
        unload.translate(i18n.get(TradeRouteText.unloadHeading));
        load.translate(i18n.get(TradeRouteText.loadHeading));
        save.setText(i18n.get(TradeRouteText.saveCargo));
        carrierLabel.setText(i18n.get(TradeRouteText.carrierLabel));
        carrierBox.setAccessibleText(carrierLabel.getText());
        process.setText(i18n.get(TradeRouteText.processNow));
        resultHeading.setText(i18n.get(TradeRouteText.resultHeading));
        close.setText(i18n.get(TradeRouteText.closeCargo));
        assignRule.setText(i18n.get(TradeRouteText.assignRule));
        assign.setText(i18n.get(TradeRouteText.assignCarrier));
        assignedHeading.setText(i18n.get(TradeRouteText.assignedHeading));
        assignedPickLabel.setText(i18n.get(TradeRouteText.assignedPickLabel));
        assignedBox.setAccessibleText(assignedPickLabel.getText());
        cancelAssignment.setText(i18n.get(TradeRouteText.cancelAssignment));
        deleteRoute.setText(i18n.get(TradeRouteText.deleteRoute));
        replaceStopLabel.setText(i18n.get(TradeRouteText.replaceStopLabel));
        replaceBox.setAccessibleText(replaceStopLabel.getText());
        replaceButton.setText(i18n.get(TradeRouteText.replaceStop));
    }

    /**
     * Opens the dialog over the colony screen. Nothing is changed unless the
     * player presses Save or the processing button.
     *
     * @param opened required colony screen the dialog was opened from
     */
    public void showFor(final Colony opened) {
        originColony = opened;
        open(Optional.empty());
    }

    /**
     * Opens the dialog from the map planner with one route chosen. The same
     * stops, goods and carrier controls apply; nothing is changed unless the
     * player presses one of the dialog's own actions.
     *
     * @param route required route chosen in the planner
     */
    public void showForRoute(final TradeRoute route) {
        originColony = null;
        open(Optional.of(route.getId()));
    }

    private void open(final Optional<Integer> routeId) {
        this.model = game.getModel();
        this.player = game.getHumanPlayer();
        translate();
        message.setText("");
        result.setText("");
        resultHeading.setVisible(false);
        deleteAskedFor = null;
        refreshing = true;
        try {
            // A choice or a carrier left from an earlier game is dropped.
            replaceBox.setValue(null);
            replaceBox.getItems().setAll(replacementChoices());
            assignedBox.getItems().clear();
            carrierBox.getItems().clear();
        } finally {
            refreshing = false;
        }
        reloadRoutes(routeId, 0);
        eventBus.post(new ShowScreenEvent(Screen.TRADE_ROUTE_CARGO));
        Platform.runLater(() -> routeBox.requestFocus());

    }

    /** Rebuilds the route list from the model, keeping the chosen route. */
    private void reloadRoutes(final Optional<Integer> keepRouteId,
            final int keepStop) {
        refreshing = true;
        try {
            final List<TradeRoute> routes = model.getTradeRoutes(player);
            routeBox.getItems().setAll(routes);
            routeBox.setValue(routes.stream()
                    .filter(route -> keepRouteId.isPresent()
                            && route.getId() == keepRouteId.get())
                    .findFirst()
                    .orElse(routes.isEmpty() ? null : routes.get(0)));
            fillStops(keepStop);
        } finally {
            refreshing = false;
        }
        loadStop();
    }

    private void fillStops(final int keep) {
        final boolean wasRefreshing = refreshing;
        refreshing = true;
        try {
            final TradeRoute route = routeBox.getValue();
            final List<Integer> indexes = new ArrayList<>();
            if (route != null) {
                for (int index = 0; index < route.getStops().size(); index++) {
                    indexes.add(index);
                }
            }
            stopBox.getItems().setAll(indexes);
            stopBox.setValue(indexes.isEmpty() ? null
                    : indexes.get(keep < indexes.size() ? keep : 0));
            // The first stop starts at the chosen stop; the player may change
            // it without touching the stored route.
        } finally {
            refreshing = wasRefreshing;
        }
    }

    /** Shows the stored cargo of the chosen stop and its carriers. */
    private void loadStop() {
        final TradeRoute selectedRoute = routeBox.getValue();
        routeSummary.setText(selectedRoute == null ? ""
                : i18n.get(TradeRouteText.routeSummary, i18n.get(selectedRoute
                        .getKind() == org.akropia.model.TradeRouteKind.SEA
                                ? TradeRouteText.sea
                                : TradeRouteText.land),
                        selectedRoute.getStops().size()));
        stopSummary.setText(stopBox.getItems().stream().map(this::stopText)
                .collect(Collectors.joining("\n")));
        goodsStop.setText(
                stopBox.getValue() == null ? "" : stopText(stopBox.getValue()));
        refreshing = true;
        try {
            final Optional<TradeRouteStop> stop = chosenStop();
            unload.setGoods(
                    stop.map(TradeRouteStop::getUnloadGoods).orElse(List.of()));
            load.setGoods(
                    stop.map(TradeRouteStop::getLoadGoods).orElse(List.of()));
            unload.setEnabled(stop.isPresent());
            load.setEnabled(stop.isPresent());
            result.setText("");
            resultHeading.setVisible(false);
            refreshCarriers();
        } finally {
            refreshing = false;
        }
        updateState();
    }

    private Optional<TradeRouteStop> chosenStop() {
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        if (route == null || index == null) {
            return Optional.empty();
        }
        return Optional.of(route.getStops().get(index));
    }

    private void refreshCarriers() {
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        final UnitWithCargo previous = carrierBox.getValue();
        final List<UnitWithCargo> carriers = route == null || index == null
                ? List.of()
                : model.getTradeRouteCarriers(player, route, index);
        carrierBox.getItems().setAll(carriers);
        carrierBox.setValue(carriers.contains(previous) ? previous
                : carriers.isEmpty() ? null : carriers.get(0));
    }

    private boolean isDirty() {
        final Optional<TradeRouteStop> stop = chosenStop();
        return stop.isPresent()
                && (!unload.getGoods().equals(stop.get().getUnloadGoods())
                        || !load.getGoods().equals(stop.get().getLoadGoods()));
    }

    /** Enables the buttons that fit the staged and stored state. */
    private void updateState() {
        if (refreshing) {
            return;
        }
        final boolean dirty = isDirty();
        save.setDisable(!dirty);
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        final UnitWithCargo carrier = carrierBox.getValue();
        String status = "";
        boolean ready = false;
        if (route == null) {
            status = i18n.get(TradeRouteText.cargoNoRoutes);
        } else if (carrier == null) {
            status = i18n.get(TradeRouteText.noCarrier);
        } else if (dirty) {
            status = i18n.get(TradeRouteText.cargoUnsaved);
        } else {
            final Optional<TradeRouteStopProblem> problem = model
                    .getTradeRouteStopProblem(carrier, route, index);
            status = problem.map(this::problemText)
                    .orElse(i18n.get(TradeRouteText.carrierReady));
            ready = problem.isEmpty();
        }
        carrierStatus.setText(status);
        process.setDisable(!ready);
        carrierBox.setDisable(carrierBox.getItems().isEmpty());
        updateAssignment(route, carrier);
        updateRouteControls(route, index);
    }

    /**
     * Enables cancel, delete and stop replacement only where the model accepts
     * them, and explains a refused replacement. Canceling this dialog never
     * reaches these actions.
     */
    private void updateRouteControls(final TradeRoute route,
            final Integer index) {
        final UnitWithCargo picked = assignedBox.getValue();
        cancelAssignment.setDisable(picked == null
                || model.getTradeRouteCancelProblem(picked).isPresent());
        final boolean asked = route != null && deleteAskedFor != null
                && deleteAskedFor == route.getId();
        deleteRoute.setText(i18n.get(asked ? TradeRouteText.confirmDeleteRoute
                : TradeRouteText.deleteRoute));
        deleteRoute.setDisable(route == null
                || model.getTradeRouteDeleteProblem(player, route).isPresent());
        deleteInfo.setText(route == null ? ""
                : i18n.get(
                        asked ? TradeRouteText.deleteConfirm
                                : TradeRouteText.deleteReleases,
                        affectedCarriers(route)));
        final TradeRouteStop choice = replaceBox.getValue();
        final boolean same = route != null && index != null && choice != null
                && route.getStops().get(index).equals(choice);
        final Optional<TradeRouteProblem> problem = route == null
                || index == null || choice == null || same ? Optional.empty()
                        : model.getTradeRouteStopEditProblem(player, route,
                                index, choice);
        replaceButton.setDisable(route == null || index == null
                || choice == null || same || problem.isPresent());
        if (route == null || index == null || choice == null || same) {
            replaceStatus.setText("");
        } else {
            replaceStatus.setText(problem.map(this::editProblemText)
                    .orElseGet(() -> i18n.get(TradeRouteText.replaceAffects,
                            affectedCarriers(route))));
        }
    }

    /** Names of the carriers that follow the route, or a sentence for none. */
    private String affectedCarriers(final TradeRoute route) {
        final List<String> names = model
                .getTradeRouteAssignedCarriers(player, route).stream()
                .map(this::carrierName).collect(Collectors.toList());
        return names.isEmpty() ? i18n.get(TradeRouteText.affectedNone)
                : String.join(", ", names);
    }

    /**
     * Owned colonies and Europe; the model decides which one a stop may take.
     */
    private List<TradeRouteStop> replacementChoices() {
        final List<TradeRouteStop> choices = new ArrayList<>();
        for (final Colony colony : model.getColonies(player)) {
            choices.add(TradeRouteStop.ofColony(colony.getId()));
        }
        choices.add(TradeRouteStop.europe());
        return choices;
    }

    private String editProblemText(final TradeRouteProblem problem) {
        return i18n.get(TradeRouteText.valueOf("editProblem" + problem.name()));
    }

    /**
     * Cancels only the carrier selected in this dialog. Other carriers of the
     * route keep their orders, and the carrier does not move.
     */
    private void cancelAssignment() {
        final UnitWithCargo carrier = assignedBox.getValue();
        if (carrier == null) {
            return;
        }
        message.setText("");
        deleteAskedFor = null;
        try {
            model.cancelTradeRouteAssignment(carrier);
            message.setText(i18n.get(TradeRouteText.cancelAssignmentDone,
                    carrierName(carrier)));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            message.setText(i18n.get(TradeRouteText.changed));
        }
        refreshCarriers();
        updateState();
    }

    /**
     * Deletes the chosen route. Assigned carriers return to manual orders where
     * they stand; the message says how many were released.
     */
    private void deleteRoute() {
        final TradeRoute route = routeBox.getValue();
        if (route == null) {
            return;
        }
        message.setText("");
        // The first press only names the affected carriers; the second press
        // on the same route deletes it, so a stray key press cannot.
        if (deleteAskedFor == null || deleteAskedFor != route.getId()) {
            deleteAskedFor = route.getId();
            updateState();
            return;
        }
        deleteAskedFor = null;
        final int released = model.getTradeRouteAssignedCarriers(player, route)
                .size();
        try {
            model.deleteTradeRoute(player, route);
            reloadRoutes(Optional.empty(), 0);
            message.setText(i18n.get(TradeRouteText.routeDeleted, released));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            reloadRoutes(Optional.of(route.getId()), 0);
            message.setText(i18n.get(TradeRouteText.changed));
        }
    }

    /**
     * Replaces the identity of the chosen stop. Carriers already on a leg keep
     * it; they take the change at their next stop.
     */
    private void replaceStop() {
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        final TradeRouteStop choice = replaceBox.getValue();
        if (route == null || index == null || choice == null) {
            return;
        }
        message.setText("");
        try {
            final TradeRoute stored = model.editTradeRouteStop(player, route,
                    index, choice);
            reloadRoutes(Optional.of(stored.getId()), index);
            message.setText(i18n.get(TradeRouteText.replaceStopDone));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            reloadRoutes(Optional.of(route.getId()), index);
            message.setText(i18n.get(TradeRouteText.changed));
        }
    }

    /**
     * Enables assigning only when the model accepts the chosen carrier and
     * first stop, explains the refusal otherwise, and lists where each carrier
     * of the route is heading or why it waits.
     */
    private void updateAssignment(final TradeRoute route,
            final UnitWithCargo carrier) {
        final Integer first = route == null || carrier == null ? null
                : model.getNearestTradeRouteStop(carrier, route).stream()
                        .boxed().findFirst().orElse(null);
        Optional<TradeRouteAssignmentProblem> problem = Optional.empty();
        if (route != null && carrier != null) {
            problem = first == null
                    ? model.getTradeRouteChangeProblem(carrier, route)
                    : model.getTradeRouteAssignmentProblem(carrier, route,
                            first);
        }
        assign.setDisable(route == null || carrier == null || first == null
                || problem.isPresent());
        assignStatus.setText(carrier == null ? ""
                : problem.map(this::assignProblemText).orElse(""));
        assigned.setText(route == null ? "" : assignedText(route));
        refreshAssignedBox(route);
    }

    /**
     * Lists every carrier that follows the route, wherever it is, so one at sea
     * or on the road can be canceled as well as one standing at a stop.
     */
    private void refreshAssignedBox(final TradeRoute route) {
        final UnitWithCargo previous = assignedBox.getValue();
        final List<UnitWithCargo> carriers = route == null ? List.of()
                : model.getTradeRouteAssignedCarriers(player, route);
        refreshing = true;
        try {
            assignedBox.getItems().setAll(carriers);
            assignedBox.setValue(carriers.contains(previous) ? previous
                    : carriers.isEmpty() ? null : carriers.get(0));
        } finally {
            refreshing = false;
        }
        assignedBox.setDisable(carriers.isEmpty());
    }

    private String assignProblemText(
            final TradeRouteAssignmentProblem problem) {
        return i18n
                .get(TradeRouteText.valueOf("assignProblem" + problem.name()));
    }

    private String assignedText(final TradeRoute route) {
        final List<UnitWithCargo> carriers = model
                .getTradeRouteAssignedCarriers(player, route);
        if (carriers.isEmpty()) {
            return i18n.get(TradeRouteText.assignedNone);
        }
        final List<String> lines = new ArrayList<>();
        for (final UnitWithCargo carrier : carriers) {
            final TradeRouteAssignment assignment = model
                    .getTradeRouteAssignment(carrier).orElseThrow();
            final String name = carrierName(carrier);
            // The leg in progress is the stop the carrier accepted, which a
            // later edit of the route reaches only after that stop. A carrier
            // that has not started its leg already counts the saved route.
            final int at = assignment.getStopIndex();
            final TradeRouteStop accepted = assignment.getStops().get(at);
            final TradeRouteStop saved = route.getStops().get(at);
            final TradeRouteStop heading = model
                    .getTradeRouteHeadingStop(carrier).orElse(accepted);
            final String stop = stopName(heading);
            if (assignment.getPause().isPresent() && !accepted.equals(saved)) {
                // Repaired while waiting: it resumes on its next owner turn.
                lines.add(i18n.get(TradeRouteText.assignedRepaired, name,
                        stopName(accepted), stopName(saved)));
            } else if (assignment.getPause().isPresent()) {
                lines.add(i18n.get(TradeRouteText.assignedPaused, name, stop,
                        i18n.get(TradeRouteText.valueOf("pause"
                                + assignment.getPause().get().name()))));
            } else if (!heading.equals(saved)) {
                lines.add(i18n.get(TradeRouteText.assignedPending, name, stop,
                        stopName(saved)));
            } else {
                lines.add(i18n.get(TradeRouteText.assignedMoving, name, stop));
            }
        }
        return String.join("\n", lines);
    }

    private void assignCarrier() {
        final TradeRoute route = routeBox.getValue();
        final UnitWithCargo carrier = carrierBox.getValue();
        final Integer first = route == null || carrier == null ? null
                : model.getNearestTradeRouteStop(carrier, route).stream()
                        .boxed().findFirst().orElse(null);
        if (route == null || first == null || carrier == null) {
            return;
        }
        message.setText("");
        try {
            model.assignTradeRoute(carrier, route, first);
            message.setText(i18n.get(TradeRouteText.assignDone));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            // Another change made the choice stale; nothing was assigned.
            message.setText(i18n.get(TradeRouteText.changed));
        }
        refreshCarriers();
        updateState();
    }

    private String problemText(final TradeRouteStopProblem problem) {
        return i18n.get(TradeRouteText.valueOf("problem" + problem.name()));
    }

    private void saveCargo() {
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        if (route == null || index == null || !isDirty()) {
            return;
        }
        try {
            final TradeRoute stored = model.configureTradeRouteStopCargo(player,
                    route, index, unload.getGoods(), load.getGoods());
            reloadRoutes(Optional.of(stored.getId()), index);
            message.setText(i18n.get(TradeRouteText.cargoSaved));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            // The route or colony changed; nothing was stored, so the staged
            // choice is replaced by the stored state.
            reloadRoutes(Optional.of(route.getId()), index);
            message.setText(i18n.get(TradeRouteText.changed));
        }
    }

    private void processStop() {
        final TradeRoute route = routeBox.getValue();
        final Integer index = stopBox.getValue();
        final UnitWithCargo carrier = carrierBox.getValue();
        if (route == null || index == null || carrier == null || isDirty()) {
            return;
        }
        message.setText("");
        final TradeRouteStopResult outcome;
        try {
            outcome = model.processTradeRouteStop(carrier, route, index);
        } catch (final IllegalStateException stale) {
            message.setText(i18n.get(TradeRouteText.changed));
            refreshCarriers();
            updateState();
            return;
        }
        resultHeading.setVisible(true);
        result.setText(describe(outcome));
        // The colony screen behind this dialog shows the moved goods.
        eventBus.post(new RepaintColonyEvent());
        refreshCarriers();
        updateState();
    }

    private String describe(final TradeRouteStopResult outcome) {
        if (outcome.getProblem().isPresent()) {
            return problemText(outcome.getProblem().get());
        }
        if (outcome.getTransfers().isEmpty()) {
            return i18n.get(TradeRouteText.noEntries);
        }
        return outcome.getTransfers().stream().map(this::entryText)
                .collect(Collectors.joining("\n"));
    }

    private String entryText(final TradeRouteTransfer entry) {
        final String verb = i18n
                .get(entry.isUnload() ? TradeRouteText.verbUnload
                        : TradeRouteText.verbLoad);
        final String goods = i18n
                .get(GoodsTypeName.getNameForGoodsType(entry.getGoodsType()));
        final String reason = i18n.get(
                TradeRouteText.valueOf("reason" + entry.getReason().name()))
                + entry.getDetail().map(detail -> ": " + detail).orElse("");
        if (entry.getGoldChange() == 0) {
            return i18n.get(TradeRouteText.entryLine, verb, goods,
                    entry.getAmount(), reason);
        }
        return i18n.get(TradeRouteText.entryGold, verb, goods,
                entry.getAmount(), reason,
                String.format("%+d", entry.getGoldChange()));
    }
}
