package org.microcol.gui.screen.europe;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.market.Market;
import org.microcol.gui.screen.market.MarketPurchaseText;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsTrade;
import org.microcol.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Contains image of of type of good.
 */
public final class PanelGood implements JavaFxComponent, Repaintable {

    private final VBox mainPanel;

    private final ImageView imageView;

    private final GoodsType goodsType;

    private final GameModelController gameModelController;

    private final Label labelPrice;

    private final QuickBuyShortcut quickBuyShortcut;

    private final EventBus eventBus;

    private final I18n i18n;

    public PanelGood(final GoodsType goodsType,
            final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final QuickBuyShortcut quickBuyShortcut, final EventBus eventBus,
            final I18n i18n) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.quickBuyShortcut = Preconditions.checkNotNull(quickBuyShortcut);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(imageProvider.getGoodsTypeImage(goodsType));
        final Pane paneImage = new Pane(imageView);
        paneImage.setMinWidth(Region.USE_PREF_SIZE);
        paneImage.setOnDragDetected(this::onDragDetected);
        labelPrice = new Label();
        labelPrice.setId("europePrice" + goodsType.name());
        labelPrice.setWrapText(true);
        labelPrice.setAlignment(Pos.CENTER);
        labelPrice.setTextAlignment(TextAlignment.CENTER);
        mainPanel = new VBox();
        mainPanel.setFillWidth(false);
        mainPanel.setUserData(this);
        mainPanel.getStyleClass().add("panelGoods");
        mainPanel.setId("europeGood" + goodsType.name());
        mainPanel.getChildren().addAll(paneImage, labelPrice);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(
            @SuppressWarnings("unused") final MouseEvent event) {
        final GoodsTrade goodsTrade = getGoodsTrade();
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(Europe.goodsToSell,
                        i18n.get(GoodsTypeName.getNameForGoodsType(goodsType)),
                        goodsTrade.getSellPrice(), goodsTrade.getBuyPrice()),
                Source.EUROPE));
    }

    private void onMouseExited(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    private void onDragDetected(final MouseEvent event) {
        if (gameModelController.getHumanPlayer().isBoycotted(goodsType)) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Europe.goodsBoycotted,
                            i18n.get(GoodsTypeName
                                    .getNameForGoodsType(goodsType))),
                    Source.EUROPE));
            event.consume();
            return;
        }
        // The drop on a hold explains a missing budget with the model limit.
        mainPanel.startFullDrag();
        eventBus.post(
                new StatusBarMessageEvent(
                        i18n.get(Market.dragToBuy,
                                MarketPurchaseText.quickBuyKey(i18n,
                                        quickBuyShortcut.isMac())),
                        Source.EUROPE));
        event.consume();
    }

    @Override
    public void repaint() {
        final GoodsTrade goodsTrade = getGoodsTrade();
        final boolean boycotted = gameModelController.getHumanPlayer()
                .isBoycotted(goodsType);
        labelPrice.setText(
                goodsTrade.getSellPrice() + "/" + goodsTrade.getBuyPrice()
                        + (boycotted
                                ? "\n" + i18n.get(Europe.goodsBoycottedMark)
                                : ""));
        mainPanel.getStyleClass().remove("boycotted");
        if (boycotted) {
            mainPanel.getStyleClass().add("boycotted");
        }
    }

    GoodsType getGoodsType() {
        return goodsType;
    }

    private GoodsTrade getGoodsTrade() {
        return gameModelController.getEuropeGoodsTradeForType(goodsType);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
