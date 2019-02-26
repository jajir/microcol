package org.microcol.gui.screen.europe;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.TitledPanel;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Show list of all available goods.
 */
public final class PanelEuropeGoods implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final Logger logger = LoggerFactory.getLogger(PanelEuropeGoods.class);

    private final HBox mainPanel;

    private final TitledPanel titledPanel;

    private final GameModelController gameModelController;

    private final EuropeCallback europeDialogCallback;

    private final List<PanelGood> panelGoods = new ArrayList<>();

    @Inject
    public PanelEuropeGoods(final EuropeCallback europeDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final DialogNotEnoughGold dialogNotEnoughGold, final EventBus eventBus,
            final I18n i18n) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
        mainPanel = new HBox();
        GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
            final PanelGood panelGood = new PanelGood(goodType, imageProvider, gameModelController,
                    dialogNotEnoughGold, eventBus, i18n);
            panelGoods.add(panelGood);
            mainPanel.getChildren().add(panelGood.getContent());
        });
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(mainPanel,
                this::isItGoodAmount);
        mainPanel.setOnDragEntered(backgroundHighlighter::onDragEntered);
        mainPanel.setOnDragExited(backgroundHighlighter::onDragExited);
        mainPanel.setOnDragOver(this::onDragOver);
        mainPanel.setOnDragDropped(this::onDragDropped);

        titledPanel = new TitledPanel();
        titledPanel.getContentPane().getChildren().add(mainPanel);
        titledPanel.getStyleClass().add("goods");
    }

    @Override
    public void repaint() {
        panelGoods.forEach(PanelGood::repaint);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        titledPanel.setTitle(i18n.get(Europe.goodsPanelTitle));
    }

    private void onDragOver(final DragEvent event) {
        if (isItGoodAmount(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on panel goods.");
        final Dragboard db = event.getDragboard();
        final ClipboardEval eval = ClipboardEval.make(gameModelController.getModel(), db)
                .filterFrom(transferFrom -> From.VALUE_FROM_UNIT == transferFrom);
        if (eval.getCargoSlot().isPresent() && eval.getGoodAmount().isPresent()) {
            gameModelController.getModel().sellGoods(eval.getCargoSlot().get(),
                    eval.getGoodAmount().get());
            europeDialogCallback.repaint();
        }
        event.setDropCompleted(true);
        event.consume();
    }

    private boolean isItGoodAmount(final Dragboard db) {
        logger.debug("Drag over unit id '" + db.getString() + "'.");
        return ClipboardEval.make(gameModelController.getModel(), db)
                .filterFrom(from -> From.VALUE_FROM_UNIT == from).isNotEmpty();
    }

    @Override
    public Region getContent() {
        return titledPanel;
    }

}