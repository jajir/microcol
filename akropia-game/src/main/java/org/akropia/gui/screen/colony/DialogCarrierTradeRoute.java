package org.akropia.gui.screen.colony;

import org.akropia.gui.UnitTypes;
import org.akropia.gui.dialog.Dialog;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
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
        final ListView<TradeRoute> routes = new ListView<>();
        routes.setId("carrierRouteChoices");
        routes.setPrefHeight(180);
        routes.getItems()
                .setAll(model.getTradeRoutes(carrier.getOwner()).stream()
                        .filter(route -> route.getKind() == (carrier.isShip()
                                ? TradeRouteKind.SEA
                                : TradeRouteKind.LAND))
                        .collect(java.util.stream.Collectors.toList()));
        routes.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(final TradeRoute route,
                    final boolean empty) {
                super.updateItem(route, empty);
                setText(empty || route == null ? null : route.getName());
            }
        });
        routes.setPlaceholder(
                new Label(i18n.get(TradeRouteText.carrierRouteEmpty)));
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
            final TradeRoute route = routes.getSelectionModel()
                    .getSelectedItem();
            final var reason = route == null
                    ? java.util.Optional.<org.akropia.model.TradeRouteAssignmentProblem>empty()
                    : model.getTradeRouteChangeProblem(carrier, route);
            ok.setDisable(route == null || reason.isPresent());
            problem.setText(reason
                    .map(value -> i18n.get(TradeRouteText
                            .valueOf("assignProblem" + value.name())))
                    .orElse(""));
        };
        routes.getSelectionModel().selectedItemProperty()
                .addListener((o, before, after) -> update.run());
        if (expected != null) {
            routes.getItems().stream()
                    .filter(route -> route.getId() == expected.getRouteId())
                    .findFirst().ifPresent(routes.getSelectionModel()::select);
        }
        update.run();
        ok.setOnAction(event -> {
            try {
                model.changeTradeRoute(carrier,
                        routes.getSelectionModel().getSelectedItem(), expected);
                refreshed.run();
                close();
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                problem.setText(i18n.get(TradeRouteText.changed));
                ok.setDisable(true);
            }
        });
        final VBox root = new VBox(12, current, rule, routes, problem,
                new HBox(12, ok, cancel));
        root.setId("carrierRouteDialog");
        root.setPrefWidth(460);
        root.setPadding(new Insets(18));
        setTitle(i18n.get(TradeRouteText.carrierRouteTitle));
        init(root);
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
