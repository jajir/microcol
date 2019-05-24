package org.microcol.gui;

import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Player;
import org.microcol.model.Unit;
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
            final LocalizationHelper localizationHelper, final Player humanPlayer, final Unit unit,
            final boolean selected) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);

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

    private Region makeUnitDescription(final Player humanPlayer, final Unit unit) {
        final VBox box = new VBox();

        final Label labelUnitType = new Label(localizationHelper.getUnitName(unit.getType()));
        labelUnitType.getStyleClass().add("unitType");
        box.getChildren().add(labelUnitType);

        if (isUnitOwnedBy(unit, humanPlayer)) {
            final Label labelMoves = new Label(
                    i18n.get(Loc.unitsPanel_availableMoves) + " " + unit.getActionPoints());
            labelMoves.getStyleClass().add("unitMoves");
            box.getChildren().add(labelMoves);
        }

        final Label labelOwner = new Label(
                i18n.get(Loc.unitsPanel_owner) + " " + unit.getOwner().getName());
        labelOwner.getStyleClass().add("unitOwner");
        box.getChildren().add(labelOwner);
        return box;
    }

    private boolean isUnitOwnedBy(final Unit unit, final Player player) {
        return unit.getOwner().equals(player);
    }

    private ImageView getImageViewForCargoSlot(final CargoSlot cargoSlot) {
        if (cargoSlot.isLoadedUnit()) {
            return new ImageView(imageProvider.getUnitImage(cargoSlot.getUnit().get()));
        } else {
            return new ImageView(imageProvider.getGoodsTypeImage(cargoSlot.getGoods().get()));
        }
    }

    private HBox makeGoodsPanel(final UnitWithCargo unit) {
        HBox box = new HBox();
        box.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL_VIEW);
        if (unit.getCargo().isEmpty()) {
            box.getChildren().add(new Label(i18n.get(Loc.unitsPanel_empty)));
        } else {
            box.getChildren().add(new Label(i18n.get(Loc.unitsPanel_with)));
            unit.getCargo().getSlots().stream().filter(cargoSlot -> !cargoSlot.isEmpty())
                    .forEach(cargoSlot -> {
                        final ImageView imageView = getImageViewForCargoSlot(cargoSlot);
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
