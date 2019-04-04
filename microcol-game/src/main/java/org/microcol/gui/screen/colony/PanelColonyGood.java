package org.microcol.gui.screen.colony;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Goods;
import org.microcol.model.GoodsProductionStats;
import org.microcol.model.GoodsType;

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

    private final Label labelAmount;

    private final Label labelDiff;

    private final GoodsType goodsType;

    private final Image image;

    private final ImageView imageView;

    private final EventBus eventBus;

    private Colony colony;

    private final I18n i18n;

    private final VBox mainPanel;

    public PanelColonyGood(final Image image, final GoodsType goodsType, final EventBus eventBus,
            final I18n i18n) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        this.image = Preconditions.checkNotNull(image);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(image);
        final Pane paneImage = new Pane(imageView);
        paneImage.setOnDragDetected(this::onDragDetected);
        paneImage.getStyleClass().add(IMAGE_GOODS_CLASS);
        labelAmount = new Label();
        labelAmount.getStyleClass().add("amount");
        labelDiff = new Label();
        labelDiff.getStyleClass().add("diff");
        final HBox hlabels = new HBox(labelAmount, labelDiff);
        hlabels.setAlignment(Pos.CENTER);
        mainPanel = new VBox();
        mainPanel.getStyleClass().add("warehouseGoods");
        mainPanel.getChildren().add(paneImage);
        mainPanel.getChildren().add(hlabels);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(GoodsTypeName.getNameForGoodsType(goodsType)) + i18n.get(ColonyMsg.goods),
                Source.COLONY));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    private void onDragDetected(final MouseEvent event) {
        Preconditions.checkNotNull(colony.getWarehouse());
        final Goods couldBemoved = colony.getWarehouse().getTransferableGoods(goodsType,
                CargoSlot.MAX_CARGO_SLOT_CAPACITY);
        if (couldBemoved.isNotZero()) {
            Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
            ClipboardWritter.make(db).addImage(image).addTransferFromColonyWarehouse()
                    .addGoods(couldBemoved).build();
        }
        eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.adjustAmountOfGoods), Source.COLONY));
        event.consume();
    }

    public void setColony(final Colony colony) {
        this.colony = colony;
    }

    public void repaint() {
        final ColonyProductionStats stats = colony.getGoodsStats();
        final GoodsProductionStats goodsStats = stats.getStatsByType(goodsType);

        String txt = String.valueOf(goodsStats.getInWarehouseBefore());
        labelAmount.setText(txt);

        final int diff = goodsStats.getDiff();
        if (diff > 0) {
            txt = "+" + diff;
            labelDiff.getStyleClass().clear();
            labelDiff.getStyleClass().add("diffPositive");
            labelDiff.setText(txt);
        } else if (diff < 0) {
            txt = String.valueOf(diff);
            labelDiff.getStyleClass().clear();
            labelDiff.getStyleClass().add("diffNegative");
            labelDiff.setText(txt);
        } else {
            labelDiff.getStyleClass().clear();
            labelDiff.setText("");
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    };

}
