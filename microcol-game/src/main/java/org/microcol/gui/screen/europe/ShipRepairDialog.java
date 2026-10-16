package org.microcol.gui.screen.europe;

import com.google.inject.Inject;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.PlaceShipRepair;
import org.microcol.model.unit.Ship;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Read-only view of the model's current repair progress. */
public final class ShipRepairDialog extends AbstractMessageWindow {
    private final I18n i18n;
    private final GameModelController controller;

    @Inject
    ShipRepairDialog(ViewUtil viewUtil, I18n i18n,
            GameModelController controller) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.controller = controller;
    }

    public void showRepairs() {
        VBox rows = new VBox(12);
        rows.setId("shipRepairRows");
        controller.getHumanPlayer().getAllUnits().stream()
                .filter(unit -> unit instanceof Ship).map(unit -> (Ship) unit)
                .filter(Ship::isDamaged)
                .sorted(java.util.Comparator.comparingInt(Ship::getId))
                .forEach(ship -> {
                    PlaceShipRepair repair = ship.getRepair().orElseThrow();
                    String destination = repair.isEuropeDestination()
                            ? i18n.get(Europe.voyageDestinationEurope)
                            : controller.getModel()
                                    .getColonyById(
                                            repair.getDestinationColonyId())
                                    .map(Colony::getName).orElse(i18n.get(
                                            Europe.repairsDestinationLost));
                    Label row = new Label(i18n.get(Europe.repairsRow,
                            i18n.get(UnitTypes.getUnitName(ship.getType())),
                            ship.getId(), destination,
                            i18n.get(repair
                                    .getPhase() == PlaceShipRepair.Phase.TRANSFER
                                            ? Europe.repairsTransfer
                                            : Europe.repairsRepair),
                            repair.getRemainingOwnerUpdates()));
                    row.setId("shipRepair-" + ship.getId());
                    row.setWrapText(true);
                    row.setMaxWidth(540);
                    rows.getChildren().add(row);
                });
        if (rows.getChildren().isEmpty()) {
            Label empty = new Label(i18n.get(Europe.repairsEmpty));
            empty.setId("shipRepairsEmpty");
            rows.getChildren().add(empty);
        }
        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportWidth(560);
        scroll.setPrefViewportHeight(280);
        Label explanation = new Label(i18n.get(Europe.repairsRestriction));
        explanation.setWrapText(true);
        explanation.setMaxWidth(560);
        Button close = new Button(i18n.get(Europe.repairsClose));
        close.setId("shipRepairsClose");
        close.setCancelButton(true);
        close.setOnAction(event -> close());
        VBox root = new VBox(12, new Label(i18n.get(Europe.repairsOpen)),
                explanation, scroll, close);
        root.setPadding(new Insets(12));
        init(root);
        setTitle(i18n.get(Europe.repairsOpen));
        showAndWait();
    }
}
