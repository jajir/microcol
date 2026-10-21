package org.microcol.gui.screen.colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TradeRoute;
import org.microcol.model.TradeRouteKind;
import org.microcol.model.TradeRouteProblem;
import org.microcol.model.TradeRouteStop;

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
 * Editor for one new trade route from an owned colony. Every choice comes from
 * the model; the dialog only stages it and reports the model's first refusal.
 */
public final class DialogTradeRoute extends AbstractMessageWindow {

    /** Stops offered by the editor; the model accepts up to four. */
    private static final int STOP_ROWS = 4;

    private final GameModelController game;
    private final I18n i18n;
    private final Label caption = new Label();
    private final Label existing = new Label();
    private final Label message = new Label();
    private final Label start = new Label();
    private final TextField name = new TextField();
    private final ComboBox<TradeRouteKind> kind = new ComboBox<>();
    private final List<ComboBox<TradeRouteStop>> stops = new ArrayList<>();
    private final Button create = new Button();
    private final Button cancel = new Button();
    private final Label rule = new Label();
    private final Label nameLabel = new Label();
    private final Label kindLabel = new Label();
    private final List<Label> stopLabels = new ArrayList<>();
    private Model model;
    private Player player;
    private Colony colony;
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
        final VBox root = new VBox(10, caption, rule, start, existing,
                nameLabel, name, kindLabel, kind);
        for (int index = 0; index < STOP_ROWS; index++) {
            stops.add(makeStopBox(index));
            final Label label = new Label();
            stopLabels.add(label);
            root.getChildren().add(new HBox(12, label, stops.get(index)));
        }
        cancel.setId("tradeRouteCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        create.setId("tradeRouteCreate");
        create.setDefaultButton(true);
        create.setOnAction(event -> saveRoute());
        root.getChildren().addAll(message, new HBox(12, cancel, create));
        root.setId("tradeRouteDialog");
        root.setPrefWidth(500);
        init(root);
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
        this.colony = start;
        this.model = game.getModel();
        this.player = game.getHumanPlayer();
        translate();
        setTitle(i18n.get(TradeRouteText.title, start.getName()));
        caption.setText(i18n.get(TradeRouteText.title, start.getName()));
        this.start
                .setText(i18n.get(TradeRouteText.startLabel, start.getName()));
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
            for (final ComboBox<TradeRouteStop> box : stops) {
                box.setValue(null);
                box.getItems().clear();
            }
        } finally {
            refreshing = false;
        }
        refreshStops();
        Platform.runLater(() -> {
            name.requestFocus();
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
                final List<TradeRouteStop> options = model
                        .getTradeRouteStopOptions(player, kind.getValue(),
                                colony.getId(), chain);
                box.getItems().setAll(options);
                box.setValue(options.contains(previous) ? previous : null);
                box.setDisable(false);
                if (box.getValue() == null) {
                    open = false;
                } else {
                    chain.add(box.getValue());
                }
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
            model.createTradeRoute(player, name.getText(), kind.getValue(),
                    colony.getId(), chosenChain());
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
