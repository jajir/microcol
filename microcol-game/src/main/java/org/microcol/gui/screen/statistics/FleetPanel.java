package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.i18n.I18n;
import org.microcol.model.FleetReport;
import org.microcol.model.FleetReport.Destination;
import org.microcol.model.FleetReport.ForeignShip;
import org.microcol.model.FleetReport.OwnShip;
import org.microcol.model.FleetReport.Repair;
import org.microcol.model.FleetReport.Voyage;
import org.microcol.model.GoodsType;
import org.microcol.model.LaborLocation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.UnitType;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Fleet report (R-0202): every own ship with its place, voyage or repair,
 * passengers, goods and holds, then the foreign ships in present sight with the
 * count of their occupied holds only. Every showing rebuilds the figures from
 * the live model; the panel keeps no figure of its own. Only the Show buttons
 * leave the report, and they change nothing.
 * <p>
 * Sentences wrap within the visible width of the scroll pane, so a long line is
 * never clipped at the edge of the statistics screen.
 * </p>
 */
final class FleetPanel extends VBox {
    private final GameModelController game;
    private final I18n i18n;
    private final FleetNavigator navigator;
    private final Label note = label("fleetNote", "");
    private final Label total = label("fleetTotal", "");
    private final VBox ships = new VBox(12);
    private final Label foreignTitle = label("fleetForeignTitle", "");
    private final VBox foreignShips = new VBox(12);
    private final VBox content = new VBox(12, ships, foreignTitle,
            foreignShips);
    private final ScrollPane scroll = new ScrollPane(content);
    private final Label feedback = label("fleetFeedback", "");

    /** The game the shown rows were built from; Show is valid only for it. */
    private Model shownModel;

    FleetPanel(final GameModelController game, final I18n i18n,
            final FleetNavigator navigator) {
        super(12);
        this.game = game;
        this.i18n = i18n;
        this.navigator = navigator;
        setId("fleet");
        getStyleClass().add("relations-report");
        scroll.setId("fleetScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(330);
        scroll.setPrefViewportWidth(400);
        getChildren().addAll(fitted(note, this), fitted(total, this), scroll,
                fitted(feedback, this));
    }

    /** Rebuilds the own ships and the visible foreign ships from the model. */
    void showReport() {
        ships.getChildren().clear();
        foreignShips.getChildren().clear();
        feedback.setText("");
        if (!game.isGameModelReady()) {
            shownModel = null;
            note.setText("");
            total.setText("");
            foreignTitle.setText("");
            feedback.setText(i18n.get(FleetText.unavailable));
            return;
        }
        note.setText(i18n.get(FleetText.note));
        shownModel = game.getModel();
        final FleetReport report = shownModel
                .getFleetReport(game.getHumanPlayer());
        total.setText(i18n.get(FleetText.total, report.getOwnShips().size()));
        if (report.getOwnShips().isEmpty()) {
            ships.getChildren()
                    .add(label("fleetEmpty", i18n.get(FleetText.empty)));
        }
        for (final OwnShip ship : report.getOwnShips()) {
            ships.getChildren().add(ownSection(ship));
        }
        foreignTitle.setText(i18n.get(FleetText.foreignTitle));
        if (report.getForeignShips().isEmpty()) {
            foreignShips.getChildren().add(label("fleetForeignEmpty",
                    i18n.get(FleetText.foreignEmpty)));
        }
        for (final ForeignShip ship : report.getForeignShips()) {
            foreignShips.getChildren().add(foreignSection(ship));
        }
    }

    private VBox ownSection(final OwnShip ship) {
        final int id = ship.getUnitId();
        final VBox section = section("fleetShip-" + id);
        section.getChildren()
                .add(fitted(label("fleetName-" + id,
                        i18n.get(FleetText.shipTitle, typeName(ship), id)),
                        section));
        section.getChildren().add(
                fitted(label("fleetPlace-" + id, placeText(ship.getLocation())),
                        section));
        ship.getVoyage()
                .ifPresent(voyage -> section.getChildren()
                        .add(fitted(
                                label("fleetVoyage-" + id, voyageText(voyage)),
                                section)));
        ship.getRepair()
                .ifPresent(repair -> section.getChildren()
                        .add(fitted(
                                label("fleetRepair-" + id, repairText(repair)),
                                section)));
        section.getChildren().add(
                fitted(label("fleetCargo-" + id, cargoText(ship)), section));
        section.getChildren().add(
                fitted(label("fleetHolds-" + id, holdsText(ship)), section));
        final Button show = ActionButtons.button(ButtonIcon.CENTER,
                i18n.get(FleetText.show));
        show.setId("fleetShow-" + id);
        show.setDisable(!ship.getLocation().isNavigable());
        show.setOnAction(event -> openOwn(ship));
        section.getChildren().add(show);
        return section;
    }

    private VBox foreignSection(final ForeignShip ship) {
        final int id = ship.getUnitId();
        final VBox section = section("fleetForeign-" + id);
        section.getChildren()
                .add(fitted(
                        label("fleetForeignName-" + id,
                                i18n.get(
                                        UnitTypes.getUnitName(ship.getType()))),
                        section));
        section.getChildren().add(fitted(
                label("fleetForeignPlace-" + id, mapTile(ship.getLocation())),
                section));
        section.getChildren()
                .add(fitted(label("fleetForeignHolds-" + id, i18n
                        .get(FleetText.foreignHolds, ship.getOccupiedHolds())),
                        section));
        final Button show = ActionButtons.button(ButtonIcon.EUROPE,
                i18n.get(FleetText.show));
        show.setId("fleetForeignShow-" + id);
        show.setOnAction(event -> openForeign(ship));
        section.getChildren().add(show);
        return section;
    }

    private String typeName(final OwnShip ship) {
        return i18n.get(UnitTypes.getUnitName(ship.getType()));
    }

    private String placeText(final LaborLocation location) {
        switch (location.getKind()) {
        case MAP:
            return location.getLocation().map(this::mapTile)
                    .orElse(i18n.get(FleetText.placeOther));
        case EUROPE:
            return i18n.get(FleetText.placeEurope);
        case AT_SEA:
            return i18n.get(FleetText.placeSea);
        case REPAIR:
            return i18n.get(FleetText.placeRepair);
        default:
            return i18n.get(FleetText.placeOther);
        }
    }

    private String mapTile(final Location tile) {
        return i18n.get(FleetText.placeMap, tile.getX(), tile.getY());
    }

    /** Names the destination and the turns left, as the model reports them. */
    private String voyageText(final Voyage voyage) {
        if (voyage.isWaiting()) {
            return i18n.get(FleetText.voyageWaiting);
        }
        final String text = i18n
                .get(voyage.getDestination() == Destination.EUROPE
                        ? FleetText.voyageEurope
                        : FleetText.voyageColonies, voyage.getRemainingTurns());
        return voyage.isReversed() ? i18n.get(FleetText.reversed, text) : text;
    }

    private String repairText(final Repair repair) {
        final String destination = repair.isEuropeDestination()
                ? i18n.get(FleetText.repairToEurope)
                : repair.getColonyName()
                        .map(name -> i18n.get(FleetText.repairToColony, name))
                        .orElse(i18n.get(FleetText.repairToLost));
        return i18n.get(FleetText.repair, repair.getRemainingOwnerUpdates(),
                destination);
    }

    /**
     * Names each passenger type with its count, so a treasure train or a
     * specialist is not hidden inside a total; goods follow in goods order.
     */
    private String cargoText(final OwnShip ship) {
        final List<String> passengers = new ArrayList<>();
        for (final Map.Entry<UnitType, Integer> entry : ship
                .getPassengersByType().entrySet()) {
            passengers.add(i18n.get(FleetText.passengerItem,
                    i18n.get(UnitTypes.getUnitName(entry.getKey())),
                    entry.getValue()));
        }
        final List<String> goods = new ArrayList<>();
        for (final Map.Entry<GoodsType, Integer> entry : ship.getGoods()
                .entrySet()) {
            goods.add(i18n.get(FleetText.goodsItem,
                    i18n.get(GoodsTypeName.getNameForGoodsType(entry.getKey())),
                    entry.getValue()));
        }
        final String passengerText = passengers.isEmpty()
                ? String.valueOf(ship.getPassengers())
                : ship.getPassengers() + " (" + String.join(", ", passengers)
                        + ")";
        return i18n.get(FleetText.cargo, passengerText,
                goods.isEmpty() ? i18n.get(FleetText.goodsNone)
                        : String.join(", ", goods));
    }

    private String holdsText(final OwnShip ship) {
        return i18n.get(FleetText.holds, ship.getOccupiedHolds(),
                ship.getCapacity(), ship.getFreeHolds());
    }

    /**
     * Opens the place of an own ship. A ship that has left the report is
     * reported after the list is rebuilt; so is a place without destination.
     */
    private void openOwn(final OwnShip shown) {
        final FleetNavigator.Result result = navigator.openOwn(shownModel,
                shown);
        if (result == FleetNavigator.Result.OPENED) {
            return;
        }
        showReport();
        feedback.setText(i18n.get(result == FleetNavigator.Result.UNAVAILABLE
                ? FleetText.shipUnavailable
                : FleetText.noDestination));
    }

    /** Opens the tile of a foreign ship that is still in present sight. */
    private void openForeign(final ForeignShip shown) {
        final FleetNavigator.Result result = navigator.openForeign(shownModel,
                shown);
        if (result == FleetNavigator.Result.OPENED) {
            return;
        }
        showReport();
        feedback.setText(i18n.get(FleetText.foreignUnavailable));
    }

    private static VBox section(final String id) {
        final VBox section = new VBox(6);
        section.setId(id);
        section.getStyleClass().add("relation-row");
        return section;
    }

    /**
     * Limits a sentence to the visible width of the scroll pane inside the
     * container, so it wraps there even when a parent is wider.
     */
    private <T extends Region> T fitted(final T region,
            final Region container) {
        region.maxWidthProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> Math.max(0,
                                scroll.getViewportBounds().getWidth()
                                        - container.getInsets().getLeft()
                                        - container.getInsets().getRight()),
                        scroll.viewportBoundsProperty(),
                        container.insetsProperty()));
        return region;
    }

    /** A sentence that wraps within the width it is given. */
    private static Label label(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        return label;
    }
}
