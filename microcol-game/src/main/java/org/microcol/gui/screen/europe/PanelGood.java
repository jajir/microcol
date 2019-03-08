package org.microcol.gui.screen.europe;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.Loc;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;
import org.microcol.model.Goods;
import org.microcol.model.GoodsTrade;
import org.microcol.model.GoodsType;

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

    private final GoodsType goodsType;

    private final GameModelController gameModelController;

    private final Label labelPrice;

    private final DialogNotEnoughGold dialogNotEnoughGold;

    private final EventBus eventBus;

    private final I18n i18n;

    public PanelGood(final GoodsType goodsType, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final DialogNotEnoughGold dialogNotEnoughGold, final EventBus eventBus,
            final I18n i18n) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions.checkNotNull(dialogNotEnoughGold);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(imageProvider.getGoodsTypeImage(goodsType));
        final Pane paneImage = new Pane(imageView);
        paneImage.setOnDragDetected(this::onDragDetected);
        labelPrice = new Label();
        mainPanel = new VBox();
        mainPanel.getStyleClass().add("panelGoods");
        mainPanel.getChildren().addAll(paneImage, labelPrice);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        final GoodsTrade goodsTrade = getGoodsTrade();
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(Europe.goodsToSell, i18n.get(GoodsTypeName.getNameForGoodsType(goodsType)),
                        goodsTrade.getSellPrice(), goodsTrade.getBuyPrice()),
                Source.EUROPE));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    private void onDragDetected(final MouseEvent event) {
        final Dragboard db = mainPanel.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
        final Goods goods = gameModelController.getMaxBuyableGoods(goodsType);
        if (goods.isZero()) {
            dialogNotEnoughGold.showAndWait();
        } else {
            eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.adjustAmountOfGoods),
                    Source.EUROPE));
            ClipboardWritter.make(db).addImage(imageView.getImage()).addTransferFromEuropeShop()
                    .addGoods(goods).build();
        }
        event.consume();
    }

    @Override
    public void repaint() {
        final GoodsTrade goodsTrade = getGoodsTrade();
        labelPrice.setText(goodsTrade.getSellPrice() + "/" + goodsTrade.getBuyPrice());
    }

    private GoodsTrade getGoodsTrade() {
        return gameModelController.getModel().getEurope().getGoodsTradeForType(goodsType);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
