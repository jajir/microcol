package org.microcol.gui.screen.europe;

import java.util.Optional;

import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public final class PanelPortPier implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final Logger logger = LoggerFactory.getLogger(PanelPortPier.class);

    private final GameModelController gameModelController;

    private final EuropeCallback europeDialog;

    private final ImageProvider imageProvider;

    private final EventBus eventBus;

    private final I18n i18n;

    private final Pane panelUnits;

    @Inject
    public PanelPortPier(final GameModelController gameModelController,
            final EuropeCallback europeDialogCallback, final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.europeDialog = Preconditions.checkNotNull(europeDialogCallback);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        panelUnits = new HBox();
        panelUnits.getStyleClass().add("panel-port-pier");
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(panelUnits,
                this::isItCorrectObject);
        panelUnits.setOnDragEntered(backgroundHighlighter::onDragEntered);
        panelUnits.setOnDragExited(backgroundHighlighter::onDragExited);
        panelUnits.setOnDragOver(this::onDragOver);
        panelUnits.setOnDragDropped(this::onDragDropped);
        panelUnits.setMinHeight(GamePanelView.TILE_WIDTH_IN_PX);
        panelUnits.setOnMouseEntered(this::onMouseEntered);
        panelUnits.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(Europe.statusBarPier), Source.EUROPE));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    @Override
    public void repaint() {
        panelUnits.getChildren().clear();
        gameModelController.getModel().getEurope().getPier()
                .getUnits(gameModelController.getCurrentPlayer()).forEach(unit -> panelUnits
                        .getChildren().add(new PanelPortPierUnit(unit, imageProvider)));
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        // panelUnits.setTitle(i18n.get(Europe.europePier));
    }

    private void onDragOver(final DragEvent event) {
        if (isItCorrectObject(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on ship cargo slot.");
        final Optional<Unit> oUnit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard()).getUnit();
        if (oUnit.isPresent()) {
            oUnit.get().placeToEuropePortPier();
            europeDialog.repaintAfterGoodMoving();
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }
    }

    private boolean isItCorrectObject(final Dragboard db) {
        return ClipboardEval.make(gameModelController.getModel(), db)
                .filterUnit(unit -> !unit.getType().isShip())
                .filterFrom(from -> From.VALUE_FROM_EUROPE_PORT_PIER != from
                        && From.VALUE_FROM_EUROPE_SHOP != from)
                .isNotEmpty();
    }

    @Override
    public Pane getContent() {
        return panelUnits;
    }

}
