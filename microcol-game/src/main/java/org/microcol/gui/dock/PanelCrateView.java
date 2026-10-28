package org.microcol.gui.dock;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.css.PseudoClass;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineJoin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PanelCrateView implements JavaFxComponent {

    private final StackPane mainPane;

    public final static String CRATE_CLASS = "cratePanel";

    private final ImageProvider imageProvider;

    private final ImageView crateImage;

    private final ImageView cargoImage;

    private final SVGPath harborCrate;

    private static final String CRATE_OUTLINE = "M 3 3 L 42 2 L 43 42 L 2 43 Z ";

    private final Label labelAmount;

    private Background background;

    PanelCrateView(final ImageProvider imageProvider, final boolean harbor) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);

        crateImage = new ImageView();
        crateImage.getStyleClass().add("crate");
        crateImage.setFitWidth(TILE_WIDTH_IN_PX);
        crateImage.setFitHeight(TILE_WIDTH_IN_PX);
        crateImage.setPreserveRatio(true);

        harborCrate = harbor ? new SVGPath() : null;
        if (harborCrate != null) {
            harborCrate.getStyleClass().addAll("crate", "harbor-crate-face");
            harborCrate.setFill(Color.web("#C99638"));
            harborCrate.setStroke(Color.web("#25231F"));
            harborCrate.setStrokeWidth(2);
            harborCrate.setStrokeLineJoin(StrokeLineJoin.ROUND);
            harborCrate.setMouseTransparent(true);
        }

        cargoImage = new ImageView();
        cargoImage.getStyleClass().add("cargo");
        cargoImage.setFitWidth(TILE_WIDTH_IN_PX);
        cargoImage.setFitHeight(TILE_WIDTH_IN_PX);
        cargoImage.setPreserveRatio(true);

        labelAmount = new Label(" ");
        labelAmount.getStyleClass().add("cargo-amount");
        labelAmount.setMouseTransparent(true);
        labelAmount.visibleProperty()
                .bind(labelAmount.textProperty().isNotEmpty());
        StackPane.setAlignment(labelAmount, Pos.BOTTOM_RIGHT);
        if (harbor) {
            StackPane.setMargin(labelAmount, new Insets(0, 3, 3, 0));
        }

        mainPane = new StackPane();
        mainPane.getChildren().addAll(
                harborCrate == null ? crateImage : harborCrate, labelAmount);
        mainPane.getStyleClass().add(CRATE_CLASS);
    }

    public void showCargoSlot(final CargoSlot cargoSlot) {
        if (cargoSlot.isLoadedGood()) {
            final Goods goodaAmount = cargoSlot.getGoods().get();
            labelAmount.setText(String.valueOf(goodaAmount.getAmount()));
            cargoImage.setImage(
                    imageProvider.getGoodsTypeImage(goodaAmount.getType()));
        } else if (cargoSlot.isLoadedUnit()) {
            final Unit cargoUnit = cargoSlot.getUnit().get();
            labelAmount.setText("");
            cargoImage.setImage(imageProvider.getUnitImage(cargoUnit));
        } else if (cargoSlot.isReservedHold()) {
            // Occupied by a multi-hold unit; the unit's own hold carries the
            // label and the drag source, so this hold shows the image only.
            labelAmount.setText("");
            cargoImage.setImage(imageProvider
                    .getUnitImage(cargoSlot.getReservedUnit().get()));
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given cargo slot %s doesn't contains anything",
                    cargoSlot));
        }
    }

    public void setBackgroundHighlighted() {
        background = mainPane.getBackground();
        mainPane.setBackground(new Background(new BackgroundFill(Color.WHITE,
                CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void setBackgroundNormal() {
        mainPane.setBackground(background);
        background = null;
    }

    /**
     * Show closed cargo slot.
     */
    public void closeSlot() {
        if (harborCrate == null) {
            crateImage.setImage(
                    imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
        } else {
            harborCrate
                    .setContent(CRATE_OUTLINE + "M 3 3 L 43 42 M 42 2 L 2 43");
            mainPane.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("closed"), true);
        }
        emptyCargo();
    }

    /**
     * Hide cargo image.
     */
    public void emptyCargo() {
        mainPane.getChildren().remove(cargoImage);
        labelAmount.setText("");
    }

    /**
     * Open as empty cargo.
     */
    public void openSlot() {
        if (harborCrate == null) {
            crateImage.setImage(
                    imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
        } else {
            harborCrate.setContent(
                    CRATE_OUTLINE + "M 13 3 L 12 43 M 32 2 L 33 42");
            mainPane.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("closed"), false);
        }
        if (!mainPane.getChildren().contains(cargoImage)) {
            mainPane.getChildren().add(1, cargoImage);
        }
    }

    @Override
    public Region getContent() {
        return mainPane;
    }

}
