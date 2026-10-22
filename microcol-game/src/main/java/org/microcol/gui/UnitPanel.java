package org.microcol.gui;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.MovementPoints;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionBuildRoad;
import org.microcol.model.unit.UnitTreasure;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Display one unit from selected tile.
 */
class UnitPanel {

    private final ImageProvider imageProvider;

    private final LocalizationHelper localizationHelper;

    private final I18n i18n;

    private final VBox box = new VBox();

    UnitPanel(final ImageProvider imageProvider, final I18n i18n,
            final LocalizationHelper localizationHelper,
            final Player humanPlayer, final Unit unit, final boolean selected) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);

        box.getStyleClass().add("unitPanel");
        box.getChildren().add(makeUnitImage(humanPlayer, unit));
        if (isUnitOwnedBy(unit, humanPlayer) && unit.canHoldCargo()) {
            box.getChildren().add(makeGoodsPanel((UnitWithCargo) unit));
        }
        if (selected) {
            box.getStyleClass().add("selected");
        }
    }

    void setOnMouseClicked(final EventHandler<? super MouseEvent> value) {
        box.setOnMouseClicked(value);
    }

    private HBox makeUnitImage(final Player humanPlayer, final Unit unit) {
        HBox box = new HBox();
        box.getChildren().add(new ImageView(imageProvider.getUnitImage(unit)));
        box.getChildren().add(makeUnitDescription(humanPlayer, unit));
        return box;
    }

    private Region makeUnitDescription(final Player humanPlayer,
            final Unit unit) {
        final VBox box = new VBox();
        // Stable identity of the described unit, for the tests of the panel.
        box.setId("unitPanel-" + unit.getId());

        final Label labelUnitType = new Label(
                localizationHelper.getUnitName(unit.getType()));
        labelUnitType.getStyleClass().add("unitType");
        box.getChildren().add(labelUnitType);
        if (unit instanceof org.microcol.model.unit.UnitFreeColonist
                && ((org.microcol.model.unit.UnitFreeColonist) unit)
                        .isVeteran()) {
            final Label veteran = new Label(i18n.get(Loc.unitsPanel_veteran));
            veteran.setId("unitVeteran-" + unit.getId());
            veteran.setWrapText(true);
            veteran.setMinHeight(Region.USE_PREF_SIZE);
            box.getChildren().add(veteran);
        }
        if (unit instanceof org.microcol.model.unit.UnitFreeColonist
                && ((org.microcol.model.unit.UnitFreeColonist) unit)
                        .getMilitaryRole().isSpecial()) {
            final Label role = new Label(
                    i18n.get(((org.microcol.model.unit.UnitFreeColonist) unit)
                            .getMilitaryRole() == org.microcol.model.MilitaryRole.CONTINENTAL
                                    ? Loc.unitsPanel_continental
                                    : Loc.unitsPanel_royal));
            role.setId("unitRole-" + unit.getId());
            role.setWrapText(true);
            role.setMinHeight(Region.USE_PREF_SIZE);
            box.getChildren().add(role);
        }
        // The convert's native affiliation and unassigned count (R-0149); the
        // unit keeps the colonist image and the colonist type name.
        if (unit instanceof org.microcol.model.unit.UnitFreeColonist
                && ((org.microcol.model.unit.UnitFreeColonist) unit)
                        .isNativeConvert()) {
            final org.microcol.model.unit.UnitFreeColonist convert = (org.microcol.model.unit.UnitFreeColonist) unit;
            final Label convertLabel = new Label(i18n.get(
                    Loc.unitsPanel_convert,
                    i18n.get(StatusBarMessageService.nationLabel(
                            convert.getConvertNation().orElseThrow())),
                    String.valueOf(convert.getConvertUnassignedUpdates())));
            convertLabel.setId("unitConvert-" + unit.getId());
            convertLabel.setWrapText(true);
            convertLabel.setMinHeight(Region.USE_PREF_SIZE);
            box.getChildren().add(convertLabel);
        }

        if (isUnitOwnedBy(unit, humanPlayer)) {
            final Label labelMoves = new Label(
                    i18n.get(Loc.unitsPanel_availableMoves) + " "
                            + formatMovement(unit.getMovementThirds()));
            labelMoves.getStyleClass().add("unitMoves");
            box.getChildren().add(labelMoves);
        }
        if (unit.getAction() instanceof UnitActionBuildRoad
                && isUnitOwnedBy(unit, humanPlayer)) {
            // Remaining owner updates of the road work, shown while it runs.
            final Label roadWork = new Label(i18n.get(Loc.unitsPanel_roadWork,
                    ((UnitActionBuildRoad) unit.getAction())
                            .getRemainingTurns()));
            roadWork.setId("unitRoadWork-" + unit.getId());
            roadWork.setWrapText(true);
            box.getChildren().add(roadWork);
        }

        // The face value belongs to the train itself, shown to every viewer.
        if (unit instanceof UnitTreasure) {
            final Label treasure = new Label(
                    i18n.get(Loc.unitsPanel_treasureValue,
                            ((UnitTreasure) unit).getValue()));
            treasure.setId("unitTreasure-" + unit.getId());
            treasure.setWrapText(true);
            treasure.setMinHeight(Region.USE_PREF_SIZE);
            box.getChildren().add(treasure);
        }
        final Label labelOwner = new Label(i18n.get(Loc.unitsPanel_owner) + " "
                + unit.getOwner().getName());
        labelOwner.getStyleClass().add("unitOwner");
        box.getChildren().add(labelOwner);
        return box;
    }

    /**
     * Shows whole points, followed by the exact fraction when one is left.
     *
     * @param movementThirds remaining movement in thirds of a point
     * @return text such as {@code 2} or {@code 1 1/3}
     */
    private static String formatMovement(final int movementThirds) {
        final int points = movementThirds / MovementPoints.THIRDS_PER_POINT;
        final int thirds = movementThirds % MovementPoints.THIRDS_PER_POINT;
        if (thirds == 0) {
            return String.valueOf(points);
        }
        return points + " " + thirds + "/" + MovementPoints.THIRDS_PER_POINT;
    }

    private boolean isUnitOwnedBy(final Unit unit, final Player player) {
        return unit.getOwner().equals(player);
    }

    private ImageView getImageViewForCargoSlot(final CargoSlot cargoSlot) {
        if (cargoSlot.isLoadedUnit()) {
            return new ImageView(
                    imageProvider.getUnitImage(cargoSlot.getUnit().get()));
        } else {
            return new ImageView(imageProvider
                    .getGoodsTypeImage(cargoSlot.getGoods().get()));
        }
    }

    private HBox makeGoodsPanel(final UnitWithCargo unit) {
        HBox box = new HBox();
        box.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);
        if (unit.getCargo().isEmpty()) {
            box.getChildren().add(new Label(i18n.get(Loc.unitsPanel_empty)));
        } else {
            box.getChildren().add(new Label(i18n.get(Loc.unitsPanel_with)));
            // A multi-hold unit is listed once, on its own hold only.
            unit.getCargo().getSlots().stream()
                    .filter(cargoSlot -> !cargoSlot.isEmpty()
                            && !cargoSlot.isReservedHold())
                    .forEach(cargoSlot -> {
                        final ImageView imageView = getImageViewForCargoSlot(
                                cargoSlot);
                        imageView.setFitHeight(15);
                        imageView.setFitWidth(15);
                        box.getChildren().add(imageView);
                    });
        }
        return box;
    }

    /**
     * @return the box
     */
    public VBox getBox() {
        return box;
    }

}
