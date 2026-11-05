package org.akropia.gui.screen.colony;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.Loc;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.util.ClipboardWritter;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;
import org.akropia.model.CargoSlot;
import org.akropia.model.Colony;
import org.akropia.model.ColonyProductionStats;
import org.akropia.model.Goods;
import org.akropia.model.GoodsProductionStats;
import org.akropia.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public final class PanelColonyGood implements JavaFxComponent {

    public static final String IMAGE_GOODS_CLASS = "imageGoods";

    public static final String FAMINE_FORECAST_ID = "famineForecast";

    private final Label name = new Label();

    private final Label labelAmount;

    private final Label labelDiff;

    private final GoodsType goodsType;

    private final Image image;

    private final ImageView imageView;

    private final EventBus eventBus;

    private Colony colony;

    private final I18n i18n;

    private final VBox mainPanel;

    PanelColonyGood(final Image image, final GoodsType goodsType,
            final EventBus eventBus, final I18n i18n) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.image = Preconditions.checkNotNull(image);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(image);
        final Pane paneImage = new Pane(imageView);
        if (goodsType.isCanBeBought()) {
            paneImage.setOnDragDetected(this::onDragDetected);
        }
        paneImage.getStyleClass().add(IMAGE_GOODS_CLASS);
        labelAmount = new Label();
        labelAmount.getStyleClass().add("amount");
        labelAmount.setMinWidth(Region.USE_PREF_SIZE);
        labelDiff = new Label();
        if (GoodsType.GRAIN.equals(goodsType)) {
            labelDiff.setId(FAMINE_FORECAST_ID);
        }
        labelDiff.getStyleClass().add("diff");
        labelDiff.setMinWidth(Region.USE_PREF_SIZE);
        // Stock and the next-turn change share one row under the name.
        final HBox hlabels = new HBox(3, labelAmount, labelDiff);
        hlabels.getStyleClass().add("goods-values");
        hlabels.setAlignment(Pos.BASELINE_CENTER);
        mainPanel = new VBox();
        mainPanel.setId("colony-good-" + goodsType.name());
        mainPanel.getStyleClass().add("warehouseGoods");
        mainPanel.getChildren().add(paneImage);
        name.getStyleClass().add("goods-name");
        name.setWrapText(true);
        mainPanel.getChildren().addAll(name, hlabels);
        mainPanel.setAlignment(Pos.TOP_CENTER);
        paneImage.setMinSize(image.getWidth(), image.getHeight());
        paneImage.setPrefSize(image.getWidth(), image.getHeight());
        paneImage.setMaxSize(image.getWidth(), image.getHeight());
        mainPanel.setMinHeight(Region.USE_PREF_SIZE);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(GoodsTypeName.getNameForGoodsType(goodsType))
                        + i18n.get(goodsType.isCanBeBought() ? ColonyMsg.goods
                                : ColonyMsg.localProduction),
                Source.COLONY));
    }

    private void onMouseExited(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    private void onDragDetected(final MouseEvent event) {
        Preconditions.checkNotNull(colony.getWarehouse());
        final Goods couldBemoved = colony.getWarehouse().getTransferableGoods(
                goodsType, CargoSlot.MAX_CARGO_SLOT_CAPACITY);
        if (couldBemoved.isNotZero()) {
            Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE,
                    TransferMode.LINK);
            ClipboardWritter.make(db).addImage(image)
                    .addTransferFromColonyWarehouse().addGoods(couldBemoved)
                    .build();
        }
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(Loc.adjustAmountOfGoods), Source.COLONY));
        event.consume();
    }

    public void setColony(final Colony colony) {
        this.colony = colony;
    }

    void repaint() {
        name.setText(i18n.get(GoodsTypeName.getNameForGoodsType(goodsType)));
        final ColonyProductionStats stats = colony.getGoodsStats();
        final GoodsProductionStats goodsStats = stats.getStatsByType(goodsType);
        final boolean shared = GoodsType.TORCHES.equals(goodsType)
                || GoodsType.OWLS.equals(goodsType);
        labelAmount.setText(shared ? ""
                : String.valueOf(goodsStats.getInWarehouseBefore()));
        labelAmount.setManaged(!shared);
        labelAmount.setVisible(!shared);
        final int diff = goodsStats.getDiff();
        labelDiff.getStyleClass().setAll("diff");
        if (diff != 0) {
            labelDiff.getStyleClass()
                    .add(diff > 0 ? "diffPositive" : "diffNegative");
        }
        labelDiff.setText(diff > 0 ? "+" + diff
                : diff < 0 ? "-" + Math.abs((long) diff) : "");
        labelDiff.setManaged(diff != 0);
        labelDiff.setVisible(diff != 0);
        if (GoodsType.GRAIN.equals(goodsType)
                && goodsStats.getInWarehouseAfter() < 0) {
            labelDiff.setText(labelDiff.getText() + " ⚠");
            final String warning = i18n.get(ColonyMsg.faminePreview);
            labelDiff.setAccessibleHelp(warning);
            labelDiff.setAccessibleText(warning);
        } else {
            labelDiff.setAccessibleHelp(null);
            labelDiff.setAccessibleText(null);
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    };

}
