package org.akropia.gui.screen.colony;

import org.akropia.gui.UnitTypes;
import org.akropia.gui.dialog.Dialog;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Model;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteAssignment;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.Unit;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import org.akropia.model.Colony;
import org.akropia.model.TradeRouteStop;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.VBox;

/** One carrier's draft selection; the model changes only on confirmation. */
public final class DialogCarrierTradeRoute extends AbstractMessageWindow {
    public DialogCarrierTradeRoute(final ViewUtil viewUtil, final I18n i18n,
            final Model model, final Unit carrier, final Runnable refreshed) {
        super(viewUtil, i18n);
        final TradeRouteAssignment expected = model
                .getTradeRouteAssignment(carrier).orElse(null);
        final Label current = new Label(describe(i18n, model, carrier));
        current.setId("carrierRouteCurrent");
        current.setWrapText(true);
        final Label rule = new Label(
                i18n.get(TradeRouteText.carrierRouteNearest));
        rule.setWrapText(true);
        final Label problem = new Label();
        problem.setId("carrierRouteProblem");
        problem.setWrapText(true);
        final ToggleGroup selection = new ToggleGroup();
        final VBox choices = new VBox(6);
        choices.setId("carrierRouteChoices");
        final ScrollPane routes = new ScrollPane(choices);
        routes.setId("carrierRouteScroll");
        routes.setFitToWidth(true);
        routes.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        routes.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        routes.setPrefViewportHeight(
                Math.min(240, viewUtil.getPrimaryStage().getHeight() * 0.35));
        routes.setMinHeight(60);
        VBox.setVgrow(routes, Priority.ALWAYS);
        model.getTradeRoutes(carrier.getOwner()).stream()
                .filter(route -> route
                        .getKind() == (carrier.isShip() ? TradeRouteKind.SEA
                                : TradeRouteKind.LAND))
                .forEach(route -> {
                    final ToggleButton choice = ActionButtons
                            .toggle(ButtonIcon.TRADE, routeLabel(model, route));
                    choice.setId("carrierRouteChoice" + route.getId());
                    choice.setUserData(route);
                    choice.setToggleGroup(selection);
                    choice.setMaxWidth(Double.MAX_VALUE);
                    choice.setAlignment(Pos.CENTER_LEFT);
                    choice.setWrapText(true);
                    choices.getChildren().add(choice);
                    if (expected != null
                            && route.getId() == expected.getRouteId()) {
                        choice.setSelected(true);
                    }
                });
        if (choices.getChildren().isEmpty()) {
            choices.getChildren()
                    .add(new Label(i18n.get(TradeRouteText.carrierRouteEmpty)));
        }
        final Button ok = ActionButtons.button(ButtonIcon.CONFIRM,
                i18n.get(Dialog.ok));
        ok.setId("carrierRouteOk");
        ok.setDefaultButton(true);
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Dialog.cancel));
        cancel.setId("carrierRouteCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        final Runnable update = () -> {
            final TradeRoute route = selection.getSelectedToggle() == null
                    ? null
                    : (TradeRoute) selection.getSelectedToggle().getUserData();
            final var reason = route == null
                    ? java.util.Optional.<org.akropia.model.TradeRouteAssignmentProblem>empty()
                    : model.getTradeRouteChangeProblem(carrier, route);
            ok.setDisable(route == null || reason.isPresent());
            problem.setText(reason
                    .map(value -> i18n.get(TradeRouteText
                            .valueOf("assignProblem" + value.name())))
                    .orElse(""));
        };
        selection.selectedToggleProperty()
                .addListener((o, before, after) -> update.run());
        update.run();
        ok.setOnAction(event -> {
            try {
                model.changeTradeRoute(carrier, (TradeRoute) selection
                        .getSelectedToggle().getUserData(), expected);
                refreshed.run();
                close();
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                problem.setText(i18n.get(TradeRouteText.changed));
                ok.setDisable(true);
            }
        });
        final VBox root = new VBox(12, current, rule, routes, problem,
                new ModalActions(ok, cancel));
        root.setId("carrierRouteDialog");
        root.setPrefWidth(460);
        root.setPadding(new Insets(18));
        setTitle(i18n.get(TradeRouteText.carrierRouteTitle));
        init(root);
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancel.fire();
                event.consume();
            }
        });
    }

    static String routeLabel(final Model model, final TradeRoute route) {
        final List<String> colonies = route.getStops().stream()
                .filter(stop -> !stop.isEurope())
                .map(TradeRouteStop::getColonyId).distinct()
                .map(id -> model.getColonyById(id).map(Colony::getName)
                        .orElse(""))
                .filter(name -> !name.isEmpty()).collect(Collectors.toList());
        return route.getName() + (colonies.isEmpty() ? ""
                : " — " + colonies.stream().limit(3)
                        .collect(Collectors.joining(", "))
                        + (colonies.size() > 3 ? ", …" : ""));
    }

    public static String describe(final I18n i18n, final Model model,
            final Unit carrier) {
        final String name = model.getTradeRouteAssignment(carrier)
                .flatMap(assignment -> model.getTradeRoutes(carrier.getOwner())
                        .stream()
                        .filter(route -> route.getId() == assignment
                                .getRouteId())
                        .findFirst())
                .map(TradeRoute::getName)
                .orElse(i18n.get(TradeRouteText.carrierRouteNone));
        return i18n.get(TradeRouteText.carrierRouteCurrent,
                i18n.get(UnitTypes.getUnitName(carrier.getType())),
                carrier.getId(), name);
    }
}
