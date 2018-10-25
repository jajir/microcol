package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodTrade;
import org.microcol.model.GoodType;
import org.microcol.model.GoodsAmount;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public final class PanelGood implements JavaFxComponent, Repaintable {

    private final VBox mainPanel;

    private final ImageView imageView;

    private final GoodType goodType;

    private final GameModelController gameModelController;

    private final Label labelPrice;

    private final DialogNotEnoughGold dialogNotEnoughGold;

    private final EventBus eventBus;

    private final I18n i18n;

    public PanelGood(final GoodType goodType, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final DialogNotEnoughGold dialogNotEnoughGold, final EventBus eventBus,
            final I18n i18n) {
        this.goodType = Preconditions.checkNotNull(goodType);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(imageProvider.getGoodTypeImage(goodType));
        mainPanel = new VBox();
        final Pane paneImage = new Pane(imageView);
        paneImage.setOnDragDetected(this::onDragDetected);
        labelPrice = new Label();
        mainPanel.getChildren().addAll(paneImage, labelPrice);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        final GoodTrade goodTrade = getGoodTrade();
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(Europe.goodsToSell, i18n.get(GoodsTypeName.getNameForGoodType(goodType)),
                        goodTrade.getSellPrice(), goodTrade.getBuyPrice()),
                Source.EUROPE));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    private void onDragDetected(final MouseEvent event) {
        final Dragboard db = mainPanel.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
        final GoodsAmount goodAmount = gameModelController.getMaxBuyableGoodsAmount(goodType);
        if (goodAmount.isZero()) {
            dialogNotEnoughGold.showAndWait();
        } else {
            ClipboardWritter.make(db).addImage(imageView.getImage()).addTransferFromEuropeShop()
                    .addGoodAmount(goodAmount).build();
        }
        event.consume();
    }

    @Override
    public void repaint() {
        final GoodTrade goodTrade = getGoodTrade();
        labelPrice.setText(goodTrade.getSellPrice() + "/" + goodTrade.getBuyPrice());
    }

    private GoodTrade getGoodTrade() {
        return gameModelController.getModel().getEurope().getGoodTradeForType(goodType);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
