package org.microcol.gui.screen.colony;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TradeRoute;
import org.microcol.model.TradeRouteAssignment;
import org.microcol.model.TradeRouteAssignmentProblem;
import org.microcol.model.TradeRouteProblem;
import org.microcol.model.TradeRouteStop;
import org.microcol.model.TradeRouteStopProblem;
import org.microcol.model.TradeRouteStopResult;
import org.microcol.model.TradeRouteTransfer;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.StringConverter;

/**
 * Cargo of the stops of the player's saved trade routes. The dialog stages the
 * ordered unload and load goods of one stop and stores them only when the
 * player presses Save. A ship or wagon standing at the stop is processed only
 * by the explicit button; opening the dialog or loading a game moves nothing.
 * Every rule, quantity and price comes from the model, whose result lists each
 * configured entry with its reason.
 */
public final class DialogTradeRouteCargo extends AbstractMessageWindow {

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
    private final Label firstStopLabel = new Label();
    private final ComboBox<Integer> firstStopBox = new ComboBox<>();
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
    private final VBox content;
    private final ScrollPane scroll = new ScrollPane();
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
    public DialogTradeRouteCargo(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController game, final EventBus eventBus) {
        super(viewUtil, i18n);
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
        firstStopBox.setId("tradeRouteAssignFirstStop");
        firstStopBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(final Integer index) {
                return index == null ? "" : stopText(index);
            }

            @Override
            public Integer fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        firstStopBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
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
        final VBox root = new VBox(10, caption, rule,
                new HBox(12, routeLabel, routeBox, stopLabel, stopBox),
                new HBox(20, unload.getNode(), load.getNode()),
                new HBox(12, save, message),
                new HBox(12, carrierLabel, carrierBox), carrierStatus,
                new HBox(12, process, close), resultHeading, result, assignRule,
                new HBox(12, firstStopLabel, firstStopBox, assign),
                assignStatus, assignedHeading, assigned,
                new HBox(12, assignedPickLabel, assignedBox, cancelAssignment),
                new HBox(12, replaceStopLabel, replaceBox, replaceButton),
                replaceStatus, new HBox(12, deleteRoute), deleteInfo);
        root.setId("tradeRouteCargoDialog");
        root.setPrefWidth(760);
        content = root;
        // The expanded dialog may be taller than a small screen; it scrolls
        // instead of pushing the buttons out of reach.
        scroll.setId("tradeRouteCargoScroll");
        scroll.setContent(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        init(scroll);
    }

    /** Fits the visible part to the screen; the rest scrolls. */
    private void fitToScreen() {
        final double available = Screen.getPrimary().getVisualBounds()
                .getHeight() - 140;
        final double wanted = content.prefHeight(content.getPrefWidth()) + 24;
        scroll.setPrefViewportWidth(content.getPrefWidth() + 20);
        scroll.setPrefViewportHeight(
                Math.max(320, Math.min(wanted, available)));
        scroll.setVvalue(0);
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
        setTitle(i18n.get(TradeRouteText.cargoTitle));
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
        firstStopLabel.setText(i18n.get(TradeRouteText.assignFirstStopLabel));
        firstStopBox.setAccessibleText(firstStopLabel.getText());
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
        reloadRoutes(Optional.empty(), 0);
        fitToScreen();
        Platform.runLater(() -> routeBox.requestFocus());
        showAndWait();
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
            firstStopBox.getItems().setAll(indexes);
            firstStopBox.setValue(stopBox.getValue());
        } finally {
            refreshing = wasRefreshing;
        }
    }

    /** Shows the stored cargo of the chosen stop and its carriers. */
    private void loadStop() {
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
        final Integer first = firstStopBox.getValue();
        Optional<TradeRouteAssignmentProblem> problem = Optional.empty();
        if (route != null && carrier != null && first != null) {
            problem = model.getTradeRouteAssignmentProblem(carrier, route,
                    first);
        }
        assign.setDisable(route == null || carrier == null || first == null
                || problem.isPresent());
        firstStopBox.setDisable(route == null);
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
        final Integer first = firstStopBox.getValue();
        final UnitWithCargo carrier = carrierBox.getValue();
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
