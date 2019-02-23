package org.microcol.gui.screen.colony;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.GoodProductionStats;
import org.microcol.model.GoodType;
import org.microcol.model.GoodsAmount;

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

    private final Label labelAmount;

    private final Label labelDiff;

    private final GoodType goodType;

    private final Image image;

    private final ImageView imageView;

    private final EventBus eventBus;

    private Colony colony;

    private final I18n i18n;

    private final VBox mainPanel;

    public PanelColonyGood(final Image image, final GoodType goodType, final EventBus eventBus,
            final I18n i18n) {
        this.goodType = Preconditions.checkNotNull(goodType);
        this.image = Preconditions.checkNotNull(image);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        imageView = new ImageView(image);
        final Pane paneImage = new Pane(imageView);
        paneImage.setOnDragDetected(this::onDragDetected);
        labelAmount = new Label();
        labelDiff = new Label();
        final HBox hlabels = new HBox(labelAmount, labelDiff);
        hlabels.setAlignment(Pos.CENTER);
        mainPanel = new VBox();
        mainPanel.getChildren().add(paneImage);
        mainPanel.getChildren().add(hlabels);
        mainPanel.setOnMouseEntered(this::onMouseEntered);
        mainPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(GoodsTypeName.getNameForGoodType(goodType))
                + i18n.get(ColonyMsg.goods), Source.COLONY));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    private void onDragDetected(final MouseEvent event) {
        Preconditions.checkNotNull(colony.getColonyWarehouse());
        final int amount = colony.getColonyWarehouse().getTransferableGoodsAmount(goodType);
        if (amount > 0) {
            Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
            ClipboardWritter.make(db).addImage(image).addTransferFromColonyWarehouse()
                    .addGoodAmount(new GoodsAmount(goodType, amount)).build();
        }
        event.consume();
    }

    public void setColony(final Colony colony) {
        this.colony = colony;
    }

    public void repaint() {
        ColonyProductionStats stats = colony.getGoodsStats();
        GoodProductionStats goodsStats = stats.getStatsByType(goodType);

        String txt = String.valueOf(goodsStats.getInWarehouseBefore());
        labelAmount.setText(txt);

        int diff = goodsStats.getInWarehouseAfter() - goodsStats.getInWarehouseBefore();
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
