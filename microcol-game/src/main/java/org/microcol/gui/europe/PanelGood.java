package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.GoodsAmount;
import org.microcol.model.GoodTrade;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public class PanelGood extends VBox {

    private final ImageView imageView;

    private final GoodType goodType;

    private final GameModelController gameModelController;

    private final Label labelPrice;

    private final DialogNotEnoughGold dialogNotEnoughGold;

    public PanelGood(final GoodType goodType, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final DialogNotEnoughGold dialogNotEnoughGold) {
        this.goodType = Preconditions.checkNotNull(goodType);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        imageView = new ImageView(imageProvider.getGoodTypeImage(goodType));
        Pane paneImage = new Pane(imageView);
        paneImage.setOnDragDetected(this::onDragDetected);
        labelPrice = new Label();
        getChildren().addAll(paneImage, labelPrice);
    }

    private void onDragDetected(final MouseEvent event) {
        final Dragboard db = this.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
        final GoodsAmount goodAmount = gameModelController.getMaxBuyableGoodsAmount(goodType);
        if (goodAmount.isZero()) {
            dialogNotEnoughGold.showAndWait();
        } else {
            ClipboardWritter.make(db).addImage(imageView.getImage()).addTransferFromEuropeShop()
                    .addGoodAmount(goodAmount).build();
        }
        event.consume();
    }

    public void replain() {
        final GoodTrade goodTrade = gameModelController.getModel().getEurope()
                .getGoodTradeForType(goodType);
        labelPrice.setText(goodTrade.getSellPrice() + "/" + goodTrade.getBuyPrice());
    }

}
