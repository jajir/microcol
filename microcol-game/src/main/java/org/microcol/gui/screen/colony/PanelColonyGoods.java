package org.microcol.gui.screen.colony;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Show list of all available goods.
 */
@Singleton
public final class PanelColonyGoods implements JavaFxComponent {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final List<PanelColonyGood> panelColonyGoods;

    private final HBox mainPanel = new HBox();

    private final ChooseGoodsDialog chooseGoods;

    private ColonyWarehouse colonyWarehouse;

    @Inject
    public PanelColonyGoods(final GameModelController gameModelController,
            final ImageProvider imageProvider, final ChooseGoodsDialog chooseGoods,
            final EventBus eventBus, final I18n i18n) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
        panelColonyGoods = GoodsType.BUYABLE_GOOD_TYPES.stream().map(goodsType -> {
            final PanelColonyGood out = new PanelColonyGood(
                    imageProvider.getGoodsTypeImage(goodsType), goodsType, eventBus, i18n);
            mainPanel.getChildren().add(out.getContent());
            return out;
        }).collect(ImmutableList.toImmutableList());

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(mainPanel,
                this::isItGoods);

        mainPanel.getStyleClass().add("panel-colony-goods");
        mainPanel.setOnDragEntered(backgroundHighlighter::onDragEntered);
        mainPanel.setOnDragExited(backgroundHighlighter::onDragExited);

        mainPanel.setOnDragOver(this::onDragOver);
        mainPanel.setOnDragDropped(this::onDragDropped);
    }

    public void setColony(final Colony colony) {
        colonyWarehouse = Preconditions.checkNotNull(colony).getWarehouse();
        panelColonyGoods.forEach(panelColonyGood -> panelColonyGood.setColony(colony));
    }

    void repaint() {
        panelColonyGoods.forEach(panelColonyGood -> panelColonyGood.repaint());
    };

    private void onDragOver(final DragEvent event) {
        if (isItGoods(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        final ClipboardEval eval = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .filterFrom(from -> From.VALUE_FROM_UNIT == from);
        if (eval.getGoods().isPresent() && eval.getCargoSlot().isPresent()) {
            final Goods originalGoods = eval.getGoods().get();
            final CargoSlot fromCargoSlot = eval.getCargoSlot().get();

            final Goods goods = adjustedAmount(originalGoods,
                    event.getTransferMode().equals(TransferMode.LINK));

            colonyWarehouse.moveToWarehouse(goods, fromCargoSlot);
            event.setDropCompleted(true);
            eventBus.post(new RepaintColonyEvent());
        }
        event.consume();
    }

    private Goods adjustedAmount(final Goods originalAmount,
            final boolean specialOperationWasSelected) {
        if (specialOperationWasSelected) {
            chooseGoods.init(originalAmount);
            return chooseGoods.getActualValue();
        } else {
            return originalAmount;
        }
    }

    private boolean isItGoods(final Dragboard db) {
        return ClipboardEval.make(gameModelController.getModel(), db).getGoods().isPresent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
