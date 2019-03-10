package org.microcol.gui.dock;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
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

    private final Label labelAmount;

    private Background background;

    PanelCrateView(final ImageProvider imageProvider) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);

        crateImage = new ImageView();
        crateImage.getStyleClass().add("crate");
        crateImage.setFitWidth(GamePanelView.TILE_WIDTH_IN_PX);
        crateImage.setFitHeight(GamePanelView.TILE_WIDTH_IN_PX);
        crateImage.setPreserveRatio(true);

        cargoImage = new ImageView();
        cargoImage.getStyleClass().add("cargo");
        cargoImage.setFitWidth(GamePanelView.TILE_WIDTH_IN_PX);
        cargoImage.setFitHeight(GamePanelView.TILE_WIDTH_IN_PX);
        cargoImage.setPreserveRatio(true);

        labelAmount = new Label(" ");

        mainPane = new StackPane();
        mainPane.getChildren().addAll(crateImage, labelAmount);
        mainPane.getStyleClass().add(CRATE_CLASS);
    }

    public void showCargoSlot(final CargoSlot cargoSlot) {
        if (cargoSlot.isLoadedGood()) {
            final Goods goodaAmount = cargoSlot.getGoods().get();
            labelAmount.setText(String.valueOf(goodaAmount.getAmount()));
            cargoImage.setImage(imageProvider.getGoodsTypeImage(goodaAmount.getType()));
        } else if (cargoSlot.isLoadedUnit()) {
            final Unit cargoUnit = cargoSlot.getUnit().get();
            labelAmount.setText("");
            cargoImage.setImage(imageProvider.getUnitImage(cargoUnit));
        } else {
            throw new IllegalArgumentException(
                    String.format("Given cargo slot %s doesn't contains anything", cargoSlot));
        }
    }

    public void setBackgroudHighlighted() {
        background = mainPane.getBackground();
        mainPane.setBackground(
                new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void setBackgroudNormal() {
        mainPane.setBackground(background);
        background = null;
    }

    /**
     * Show closed cargo slot.
     */
    public void closeSlot() {
        crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_CLOSED));
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
        crateImage.setImage(imageProvider.getImage(ImageProvider.IMG_CRATE_OPEN));
        if (!mainPane.getChildren().contains(cargoImage)) {
            mainPane.getChildren().add(cargoImage);
        }
    }

    @Override
    public Region getContent() {
        return mainPane;
    }

}
