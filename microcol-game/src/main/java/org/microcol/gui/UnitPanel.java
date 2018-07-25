package org.microcol.gui;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Text;
import org.microcol.model.CargoSlot;
import org.microcol.model.Player;
import org.microcol.model.Unit;

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
public class UnitPanel {

    private final ImageProvider imageProvider;

    private final LocalizationHelper localizationHelper;

    private final Text text;

    private final VBox box = new VBox();

    public UnitPanel(final ImageProvider imageProvider, final Text text,
            final LocalizationHelper localizationHelper, final Player humanPlayer, final Unit unit,
            final boolean selected) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.text = Preconditions.checkNotNull(text);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);

        box.getStyleClass().add("unitPanel");
        box.getChildren().add(makeUnitImage(humanPlayer, unit));
        if (isUnitOwnedBy(unit, humanPlayer) && unit.getType().getCargoCapacity() > 0) {
            box.getChildren().add(makeGoodsPanel(unit));
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
        box.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
        box.getChildren().add(makeUnitDescription(humanPlayer, unit));
        return box;
    }

    private Region makeUnitDescription(final Player humanPlayer, final Unit unit) {
        VBox box = new VBox();
        final StringBuilder sb = new StringBuilder(200);
        sb.append(localizationHelper.getUnitName(unit.getType()));
        sb.append("\n");
        if (isUnitOwnedBy(unit, humanPlayer)) {
            sb.append(text.get("unitsPanel.availableMoves"));
            sb.append(" ");
            sb.append(unit.getActionPoints());
            sb.append("\n");
        }
        sb.append(text.get("unitsPanel.owner"));
        sb.append(" ");
        sb.append(unit.getOwner().getName());
        box.getChildren().add(new Label(sb.toString()));
        return box;
    }

    private boolean isUnitOwnedBy(final Unit unit, final Player player) {
        return unit.getOwner().equals(player);
    }

    private ImageView getImageViewForCargoSlot(final CargoSlot cargoSlot) {
        if (cargoSlot.isLoadedUnit()) {
            return new ImageView(imageProvider.getUnitImage(cargoSlot.getUnit().get()));
        } else {
            return new ImageView(imageProvider.getGoodTypeImage(cargoSlot.getGoods().get()));
        }
    }

    private HBox makeGoodsPanel(final Unit unit) {
        HBox box = new HBox();
        box.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL_VIEW);
        if (unit.getCargo().isEmpty()) {
            box.getChildren().add(new Label(text.get("unitsPanel.empty")));
        } else {
            box.getChildren().add(new Label(text.get("unitsPanel.with")));
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
