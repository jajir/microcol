package org.akropia.gui.screen.colony;

import java.util.List;

import org.akropia.gui.dialog.ChooseGoodsDialog;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.BackgroundHighlighter;
import org.akropia.gui.util.ClipboardEval;
import org.akropia.gui.util.From;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;
import org.akropia.model.CargoSlot;
import org.akropia.model.Colony;
import org.akropia.model.ColonyWarehouse;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    private final DialogWarehouseTransfer transferWarning;

    private final I18n i18n;

    private ColonyWarehouse colonyWarehouse;

    private final VBox content = new VBox();

    @Inject
    public PanelColonyGoods(final GameModelController gameModelController,
            final ImageProvider imageProvider,
            final ChooseGoodsDialog chooseGoods, final EventBus eventBus,
            final I18n i18n, final DialogWarehouseTransfer transferWarning) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
        this.transferWarning = Preconditions.checkNotNull(transferWarning);
        this.i18n = Preconditions.checkNotNull(i18n);
        panelColonyGoods = GoodsType.GOOD_TYPES.stream().map(goodsType -> {
            final PanelColonyGood out = new PanelColonyGood(
                    imageProvider.getGoodsTypeImage(goodsType), goodsType,
                    eventBus, i18n);
            mainPanel.getChildren().add(out.getContent());
            HBox.setHgrow(out.getContent(),
                    javafx.scene.layout.Priority.ALWAYS);
            out.getContent().setMaxWidth(Double.MAX_VALUE);
            return out;
        }).collect(ImmutableList.toImmutableList());
        content.getStyleClass().add("panel-colony-goods-column");
        content.setPickOnBounds(false);
        content.getChildren().add(mainPanel);

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(
                mainPanel, this::isItGoods);

        mainPanel.getStyleClass().add("panel-colony-goods");
        mainPanel.setOnDragEntered(backgroundHighlighter::onDragEntered);
        mainPanel.setOnDragExited(backgroundHighlighter::onDragExited);

        mainPanel.setOnDragOver(this::onDragOver);
        mainPanel.setOnDragDropped(this::onDragDropped);
    }

    public void setColony(final Colony colony) {
        Preconditions.checkNotNull(colony);
        colonyWarehouse = colony.getWarehouse();
        panelColonyGoods
                .forEach(panelColonyGood -> panelColonyGood.setColony(colony));
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
        if (eval.getGoods().isPresent() && eval.getCargoSlot().isPresent()
                && event.getGestureSource() instanceof Node) {
            final Goods originalGoods = eval.getGoods().get();
            final CargoSlot fromCargoSlot = eval.getCargoSlot().get();

            final ColonyWarehouse target = colonyWarehouse;
            final boolean partialTransfer = event
                    .getTransferMode() == TransferMode.LINK;
            // Finish native drag/drop before opening a modal dialog.
            final Node source = (Node) event.getGestureSource();
            source.addEventHandler(DragEvent.DRAG_DONE,
                    new EventHandler<DragEvent>() {
                        @Override
                        public void handle(final DragEvent done) {
                            source.removeEventHandler(DragEvent.DRAG_DONE,
                                    this);
                            if (done.getTransferMode() != null) {
                                Platform.runLater(() -> unload(target,
                                        originalGoods, fromCargoSlot,
                                        partialTransfer));
                            }
                        }
                    });
            event.setDropCompleted(true);
        }
        event.consume();
    }

    private void unload(final ColonyWarehouse target, final Goods originalGoods,
            final CargoSlot fromCargoSlot, final boolean partialTransfer) {
        Goods requested = originalGoods;
        boolean refreshed = false;
        while (true) {
            final Goods available;
            try {
                available = target.getUnloadableGoods(requested, fromCargoSlot);
            } catch (final IllegalArgumentException ex) {
                if (!refreshed) {
                    transferWarning.show(i18n.get(ColonyMsg.unloadChanged));
                }
                break;
            }
            if (available.isZero()) {
                if (!refreshed) {
                    transferWarning.show(i18n.get(ColonyMsg.unloadFull));
                }
                break;
            }
            if (refreshed) {
                eventBus.post(new RepaintColonyEvent());
            }
            final Goods chosen = adjustedAmount(requested, available,
                    partialTransfer || refreshed);
            if (chosen.isZero()) {
                break;
            }
            try {
                if (requested.equals(fromCargoSlot.getGoods().orElse(null))
                        && target.getUnloadableGoods(chosen, fromCargoSlot)
                                .equals(chosen)) {
                    target.moveToWarehouse(chosen, fromCargoSlot);
                    break;
                }
            } catch (final IllegalArgumentException ex) {
                // The modal choice may outlive its source or destination.
            }
            transferWarning.show(i18n.get(ColonyMsg.unloadChanged));
            final java.util.Optional<Goods> current = fromCargoSlot.getGoods();
            if (current.isEmpty() || !current.get().getType()
                    .equals(originalGoods.getType())) {
                break;
            }
            requested = current.get();
            refreshed = true;
        }
        if (gameModelController.getModel().getColonies().stream()
                .noneMatch(colony -> colony.getWarehouse() == target)) {
            eventBus.post(new org.akropia.gui.screen.ShowScreenEvent(
                    org.akropia.gui.screen.Screen.GAME));
        } else {
            eventBus.post(new RepaintColonyEvent());
        }
    }

    private Goods adjustedAmount(final Goods originalAmount,
            final Goods available, final boolean specialOperationWasSelected) {
        if (specialOperationWasSelected || !available.equals(originalAmount)
                || available.getType() == GoodsType.GRAIN) {
            chooseGoods.init(available, available.getType() == GoodsType.GRAIN
                    ? i18n.get(ColonyMsg.unloadFood, available.getAmount())
                    : available.equals(originalAmount) ? ""
                            : i18n.get(ColonyMsg.unloadLimited,
                                    available.getAmount()));
            return chooseGoods.getActualValue();
        } else {
            return originalAmount;
        }
    }

    private boolean isItGoods(final Dragboard db) {
        final ClipboardEval eval = ClipboardEval
                .make(gameModelController.getModel(), db)
                .filterFrom(from -> from == From.VALUE_FROM_UNIT);
        if (!eval.getGoods().isPresent() || !eval.getCargoSlot().isPresent()) {
            return false;
        }
        try {
            colonyWarehouse.getUnloadableGoods(eval.getGoods().get(),
                    eval.getCargoSlot().get());
            return true;
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public Region getContent() {
        return content;
    }

}
