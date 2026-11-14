package org.akropia.gui.screen.colony;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteProblem;
import org.akropia.model.TradeRouteStop;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Editor for one new trade route from an owned colony. Opened from a colony it
 * starts there; opened from the map planner the player also chooses the
 * starting colony. Every choice comes from the model; the dialog only stages it
 * and reports the model's first refusal.
 */
public final class DialogTradeRoute extends AbstractMessageWindow {

    /**
     * Stop rows shown when the editor opens. The model has no stop limit, so a
     * further row appears whenever every row is chosen and another legal stop
     * remains.
     */
    private static final int STOP_ROWS = 4;

    private final GameModelController game;
    private final I18n i18n;
    private final Label caption = new Label();
    private final Label existing = new Label();
    private final Label message = new Label();
    private final Label start = new Label();
    private final Label startPickLabel = new Label();
    /** Starting colony, offered only when the editor opens from the map. */
    private final ComboBox<Colony> startBox = new ComboBox<>();
    private final HBox startPick;
    private final TextField name = new TextField();
    private final ComboBox<TradeRouteKind> kind = new ComboBox<>();
    private final List<ComboBox<TradeRouteStop>> stops = new ArrayList<>();
    private final Button create = ActionButtons.button(ButtonIcon.ADD);
    private final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
    private final Label rule = new Label();
    private final Label nameLabel = new Label();
    private final Label kindLabel = new Label();
    private final List<Label> stopLabels = new ArrayList<>();
    private final VBox stopRows = new VBox(10);
    private Model model;
    private Player player;
    private Colony colony;
    /** Route stored by the last press of Create since this editor opened. */
    private TradeRoute created;
    /** Set while rows are rebuilt, so their own events do not recurse. */
    private boolean refreshing;

    @Inject
    public DialogTradeRoute(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController game) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.game = game;
        caption.setId("tradeRouteTitle");
        caption.setWrapText(true);
        rule.setWrapText(true);
        rule.setMaxWidth(480);
        start.setId("tradeRouteStart");
        existing.setId("tradeRouteExisting");
        existing.setWrapText(true);
        existing.setMaxWidth(480);
        message.setId("tradeRouteError");
        message.setWrapText(true);
        message.setMinHeight(40);
        message.getStyleClass().add("name-error");
        name.setId("tradeRouteName");
        nameLabel.setLabelFor(name);
        name.textProperty().addListener((observable, previous, current) -> {
            validate();
        });
        kind.setId("tradeRouteKind");
        kind.getItems().setAll(TradeRouteKind.values());
        kind.setConverter(new StringConverter<TradeRouteKind>() {
            @Override
            public String toString(final TradeRouteKind value) {
                return value == null ? ""
                        : i18n.get(value == TradeRouteKind.LAND
                                ? TradeRouteText.land
                                : TradeRouteText.sea);
            }

            @Override
            public TradeRouteKind fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        kind.setOnAction(event -> {
            if (!refreshing) {
                refreshStops();
            }
        });
        startBox.setId("tradeRouteStartColony");
        startPickLabel.setLabelFor(startBox);
        startBox.setConverter(new StringConverter<Colony>() {
            @Override
            public String toString(final Colony value) {
                return value == null ? "" : value.getName();
            }

            @Override
            public Colony fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        startBox.setOnAction(event -> {
            if (!refreshing) {
                colony = startBox.getValue();
                refreshStops();
            }
        });
        startPick = new HBox(12, startPickLabel, startBox);
        startPick.managedProperty().bind(startPick.visibleProperty());
        start.managedProperty().bind(start.visibleProperty());
        final VBox root = new VBox(10, caption, rule, start, startPick,
                existing, nameLabel, name, kindLabel, kind);
        for (int index = 0; index < STOP_ROWS; index++) {
            addStopRow();
        }
        root.getChildren().add(stopRows);
        cancel.setId("tradeRouteCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        create.setId("tradeRouteCreate");
        create.setDefaultButton(true);
        create.setOnAction(event -> saveRoute());
        root.getChildren().addAll(message, new ModalActions(create, cancel));
        root.setId("tradeRouteDialog");
        root.setPrefWidth(500);
        init(root);
    }

    private void addStopRow() {
        final int index = stops.size();
        stops.add(makeStopBox(index));
        final Label label = new Label();
        stopLabels.add(label);
        if (model != null) {
            final String text = i18n.get(TradeRouteText.stopLabel, index + 1);
            label.setText(text);
            stops.get(index).setAccessibleText(text);
        }
        stopRows.getChildren().add(new HBox(12, label, stops.get(index)));
        // A grown list keeps the dialog large enough for its last row.
        if (getScene() != null && getScene().getWindow() != null) {
            getScene().getWindow().sizeToScene();
        }
    }

    private ComboBox<TradeRouteStop> makeStopBox(final int index) {
        final ComboBox<TradeRouteStop> box = new ComboBox<>();
        box.setId("tradeRouteStop" + index);
        box.setConverter(new StringConverter<TradeRouteStop>() {
            @Override
            public String toString(final TradeRouteStop stop) {
                return stop == null ? "" : stopText(stop);
            }

            @Override
            public TradeRouteStop fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        box.setOnAction(event -> {
            if (!refreshing) {
                refreshStops();
            }
        });
        return box;
    }

    private String stopText(final TradeRouteStop stop) {
        if (stop.isEurope()) {
            return i18n.get(TradeRouteText.europe);
        }
        return model.getColonyById(stop.getColonyId()).map(Colony::getName)
                .orElse("");
    }

    /** Sets the fixed texts in the language current when the editor opens. */
    private void translate() {
        rule.setText(i18n.get(TradeRouteText.rule));
        nameLabel.setText(i18n.get(TradeRouteText.nameLabel));
        name.setAccessibleText(nameLabel.getText());
        kindLabel.setText(i18n.get(TradeRouteText.kindLabel));
        cancel.setText(i18n.get(TradeRouteText.cancel));
        create.setText(i18n.get(TradeRouteText.create));
        startPickLabel.setText(i18n.get(TradeRouteText.startPickLabel));
        startBox.setAccessibleText(startPickLabel.getText());
        for (int index = 0; index < stops.size(); index++) {
            final String text = i18n.get(TradeRouteText.stopLabel, index + 1);
            stopLabels.get(index).setText(text);
            stops.get(index).setAccessibleText(text);
        }
    }

    /**
     * Opens the editor for one owned colony. Nothing is changed unless the
     * player presses Create.
     *
     * @param start required colony the route starts from
     */
    public void showFor(final Colony start) {
        this.model = game.getModel();
        this.player = game.getHumanPlayer();
        translate();
        setTitle(i18n.get(TradeRouteText.title, start.getName()));
        caption.setText(i18n.get(TradeRouteText.title, start.getName()));
        this.start
                .setText(i18n.get(TradeRouteText.startLabel, start.getName()));
        this.start.setVisible(true);
        startPick.setVisible(false);
        open(start, false);
    }

    /**
     * Opens the editor from the map planner, independently of any colony
     * screen. The player chooses the starting colony among the owned ones; the
     * first one is preselected. Nothing is changed unless the player presses
     * Create.
     */
    public void showNew() {
        this.model = game.getModel();
        this.player = game.getHumanPlayer();
        translate();
        setTitle(i18n.get(TradeRouteText.newTitle));
        caption.setText(i18n.get(TradeRouteText.newTitle));
        this.start.setVisible(false);
        startPick.setVisible(true);
        final List<Colony> owned = model.getColonies(player);
        refreshing = true;
        try {
            startBox.getItems().setAll(owned);
            startBox.setValue(owned.isEmpty() ? null : owned.get(0));
        } finally {
            refreshing = false;
        }
        open(startBox.getValue(), true);
    }

    /**
     * @return the route stored by Create since the editor last opened, or empty
     *         when the player canceled
     */
    public Optional<TradeRoute> getCreatedRoute() {
        return Optional.ofNullable(created);
    }

    private void open(final Colony first, final boolean pickStart) {
        this.colony = first;
        this.created = null;
        final List<TradeRoute> routes = model.getTradeRoutes(player);
        existing.setText(routes.isEmpty() ? i18n.get(TradeRouteText.noRoutes)
                : i18n.get(TradeRouteText.existing,
                        routes.stream().map(TradeRoute::getName)
                                .collect(Collectors.joining(", "))));
        // Every opening is a new draft: a created or canceled earlier draft
        // must not leave its choices behind in this shared dialog.
        refreshing = true;
        try {
            name.setText("");
            kind.setValue(TradeRouteKind.LAND);
            while (stops.size() > STOP_ROWS) {
                stops.remove(stops.size() - 1);
                stopLabels.remove(stopLabels.size() - 1);
                stopRows.getChildren()
                        .remove(stopRows.getChildren().size() - 1);
            }
            for (final ComboBox<TradeRouteStop> box : stops) {
                box.setValue(null);
                box.getItems().clear();
            }
        } finally {
            refreshing = false;
        }
        refreshStops();
        Platform.runLater(() -> {
            if (pickStart) {
                startBox.requestFocus();
            } else {
                name.requestFocus();
            }
        });
        showAndWait();
    }

    /**
     * Rebuilds the stop rows from the model. Each row offers the legal next
     * stops after the rows above it; a row after an empty one stays closed.
     */
    private void refreshStops() {
        refreshing = true;
        try {
            final List<TradeRouteStop> chain = new ArrayList<>();
            boolean open = true;
            for (final ComboBox<TradeRouteStop> box : stops) {
                if (!open) {
                    box.getItems().clear();
                    box.setValue(null);
                    box.setDisable(true);
                    continue;
                }
                final TradeRouteStop previous = box.getValue();
                final List<TradeRouteStop> options = colony == null ? List.of()
                        : model.getTradeRouteStopOptions(player,
                                kind.getValue(), colony.getId(), chain);
                box.getItems().setAll(options);
                box.setValue(options.contains(previous) ? previous : null);
                box.setDisable(false);
                if (box.getValue() == null) {
                    open = false;
                } else {
                    chain.add(box.getValue());
                }
            }
            // Every row is chosen: offer one more while a legal stop remains.
            if (open && colony != null
                    && !model.getTradeRouteStopOptions(player, kind.getValue(),
                            colony.getId(), chain).isEmpty()) {
                addStopRow();
                final ComboBox<TradeRouteStop> next = stops
                        .get(stops.size() - 1);
                next.getItems().setAll(model.getTradeRouteStopOptions(player,
                        kind.getValue(), colony.getId(), chain));
            }
        } finally {
            refreshing = false;
        }
        validate();
    }

    private List<TradeRouteStop> chosenChain() {
        final List<TradeRouteStop> chain = new ArrayList<>();
        for (final ComboBox<TradeRouteStop> box : stops) {
            if (box.getValue() == null) {
                break;
            }
            chain.add(box.getValue());
        }
        return chain;
    }

    private boolean validate() {
        if (model == null || player == null || kind.getValue() == null) {
            return false;
        }
        if (colony == null) {
            // Opened from the map without any owned colony to start from.
            message.setText(i18n.get(TradeRouteText.noStartColony));
            create.setDisable(true);
            return false;
        }
        final Optional<TradeRouteProblem> problem = model.getTradeRouteProblem(
                player, name.getText(), kind.getValue(), colony.getId(),
                chosenChain());
        // An empty stop list is simply not finished yet; no error is shown.
        final Optional<TradeRouteProblem> shown = problem
                .filter(value -> value != TradeRouteProblem.STOP_COUNT);
        name.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"),
                shown.isPresent() && shown.get().name().startsWith("NAME_"));
        message.setText(shown.map(this::problemText).orElse(""));
        create.setDisable(problem.isPresent());
        return problem.isEmpty();
    }

    private String problemText(final TradeRouteProblem problem) {
        switch (problem) {
        case NAME_EMPTY:
            return i18n.get(TradeRouteText.nameEMPTY);
        case NAME_TOO_LONG:
            return i18n.get(TradeRouteText.nameTOO_LONG);
        case NAME_INVALID:
            return i18n.get(TradeRouteText.nameINVALID);
        case NAME_DUPLICATE:
            return i18n.get(TradeRouteText.nameDUPLICATE);
        case NOT_OWNER:
            return i18n.get(TradeRouteText.notOwner);
        case ID_EXHAUSTED:
            return i18n.get(TradeRouteText.idExhausted);
        case START_UNAVAILABLE:
            return i18n.get(TradeRouteText.startUnavailable);
        case START_NOT_PORT:
            return i18n.get(TradeRouteText.startNotPort);
        case EUROPE_BY_LAND:
            return i18n.get(TradeRouteText.europeByLand);
        case SAME_STOP:
            return i18n.get(TradeRouteText.sameStop);
        case STOP_NOT_REACHABLE:
            return i18n.get(TradeRouteText.stopNotReachable);
        default:
            return i18n.get(TradeRouteText.stopUnavailable);
        }
    }

    private void saveRoute() {
        if (!validate()) {
            return;
        }
        try {
            created = model.createTradeRoute(player, name.getText(),
                    kind.getValue(), colony.getId(), chosenChain());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            // Another change may have made a stop or name stale; nothing was
            // stored, so the staged choices are rebuilt from the model.
            message.setText(i18n.get(TradeRouteText.changed));
            refreshStops();
            return;
        }
        close();
    }
}
