package org.akropia.gui.screen.europe;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import com.google.inject.Inject;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.PlaceShipRepair;
import org.akropia.model.unit.Ship;
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
        Button close = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Europe.repairsClose));
        close.setId("shipRepairsClose");
        close.setCancelButton(true);
        close.setOnAction(event -> close());
        VBox root = new VBox(12, new Label(i18n.get(Europe.repairsOpen)),
                explanation, scroll, new ModalActions(close));
        root.setPadding(new Insets(12));
        init(root);
        setTitle(i18n.get(Europe.repairsOpen));
        showAndWait();
    }
}
