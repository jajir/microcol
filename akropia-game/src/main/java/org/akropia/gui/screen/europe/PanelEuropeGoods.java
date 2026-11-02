package org.akropia.gui.screen.europe;

import java.util.ArrayList;
import java.util.List;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.screen.market.ScreenMarketSellContext;
import org.akropia.gui.util.BackgroundHighlighter;
import org.akropia.gui.util.ClipboardEval;
import org.akropia.gui.util.From;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.Repaintable;
import org.akropia.gui.util.TitledPanel;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import org.akropia.model.CargoSlot;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Show list of all available goods.
 */
public final class PanelEuropeGoods
        implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final Logger logger = LoggerFactory
            .getLogger(PanelEuropeGoods.class);

    private final HBox mainPanel;

    private final Label tradeRestriction = new Label();

    private final TitledPanel titledPanel;

    private final StackPane frame = new StackPane();

    private final GameModelController gameModelController;

    private final EuropeCallback europeDialogCallback;

    private final List<PanelGood> panelGoods = new ArrayList<>();

    private final EventBus eventBus;

    private final I18n i18n;

    @Inject
    public PanelEuropeGoods(final EuropeCallback europeDialogCallback,
            final GameModelController gameModelController,
            final ImageProvider imageProvider,
            final QuickBuyShortcut quickBuyShortcut, final EventBus eventBus,
            final I18n i18n) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.europeDialogCallback = Preconditions
                .checkNotNull(europeDialogCallback);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        mainPanel = new HBox();
        mainPanel.setId("europeMarketGoods");
        tradeRestriction.setId("europeMarketRestriction");
        tradeRestriction.setWrapText(true);
        GoodsType.BUYABLE_GOOD_TYPES.forEach(goodsType -> {
            final PanelGood panelGood = new PanelGood(goodsType, imageProvider,
                    gameModelController, quickBuyShortcut, eventBus, i18n);
            if (!panelGoods.isEmpty()) {
                final Region gap = new Region();
                HBox.setHgrow(gap, javafx.scene.layout.Priority.ALWAYS);
                mainPanel.getChildren().add(gap);
            }
            panelGoods.add(panelGood);
            mainPanel.getChildren().add(panelGood.getContent());
        });
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(
                mainPanel, this::isItGoods);
        mainPanel.setOnDragEntered(backgroundHighlighter::onDragEntered);
        mainPanel.setOnDragExited(backgroundHighlighter::onDragExited);
        mainPanel.setOnDragOver(this::onDragOver);
        mainPanel.setOnDragDropped(this::onDragDropped);

        titledPanel = new TitledPanel();
        final VBox content = new VBox(mainPanel, tradeRestriction);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        titledPanel.getContentPane().getChildren().add(content);
        titledPanel.getStyleClass().add("game-bar-content");
        frame.setId("europeMarketFrame");
        frame.getStyleClass().add("goods");
        frame.getChildren().add(titledPanel);
    }

    @Override
    public void repaint() {
        panelGoods.forEach(PanelGood::repaint);
        final boolean available = gameModelController.getHumanPlayer()
                .canTradeInEurope();
        mainPanel.setDisable(!available);
        tradeRestriction.setVisible(!available);
        tradeRestriction.setManaged(!available);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        titledPanel.setTitle(i18n.get(Europe.goodsPanelTitle));
        tradeRestriction.setText(i18n.get(Europe.equipmentTRADE_RESTRICTED));
    }

    private void onDragOver(final DragEvent event) {
        if (isItGoods(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        if (!gameModelController.getHumanPlayer().canTradeInEurope()) {
            event.setDropCompleted(false);
            event.consume();
            repaint();
            return;
        }
        logger.debug("Object was dropped on panel goods.");
        final Dragboard db = event.getDragboard();
        final ClipboardEval eval = ClipboardEval
                .make(gameModelController.getModel(), db).filterFrom(
                        transferFrom -> From.VALUE_FROM_UNIT == transferFrom);
        final boolean specialOperatonWasSelected = event.getTransferMode()
                .equals(TransferMode.LINK);
        if (eval.getCargoSlot().isPresent() && eval.getGoods().isPresent()) {
            final Goods goodsToSell = eval.getGoods().get();
            final CargoSlot sourceCargoSlot = eval.getCargoSlot().get();
            if (gameModelController.getHumanPlayer()
                    .isBoycotted(goodsToSell.getType())) {
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(Europe.goodsBoycotted,
                                i18n.get(GoodsTypeName.getNameForGoodsType(
                                        goodsToSell.getType()))),
                        Source.EUROPE));
                event.setDropCompleted(false);
                event.consume();
                return;
            }
            if (specialOperatonWasSelected) {
                // request showing screen with market sell
                eventBus.post(new ShowScreenEvent(Screen.MARKET_SELL,
                        new ScreenMarketSellContext(goodsToSell,
                                sourceCargoSlot)));
                return;
            } else {
                gameModelController.getModel().sellGoods(sourceCargoSlot,
                        goodsToSell);
                europeDialogCallback.repaint();
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    private boolean isItGoods(final Dragboard db) {
        if (!gameModelController.getHumanPlayer().canTradeInEurope()) {
            return false;
        }
        logger.debug("Drag over unit id '" + db.getString() + "'.");
        return ClipboardEval.make(gameModelController.getModel(), db)
                .filterFrom(from -> From.VALUE_FROM_UNIT == from).isNotEmpty();
    }

    @Override
    public Region getContent() {
        return frame;
    }

}
