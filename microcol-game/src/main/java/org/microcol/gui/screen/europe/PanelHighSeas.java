package org.microcol.gui.screen.europe;

import java.util.Optional;

import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.TitledPanel;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 */
public final class PanelHighSeas<T extends Enum<T> & MessageKeyResource>
        implements JavaFxComponent, Repaintable, UpdatableLanguage {

    private boolean isShownShipsTravelingToEurope;

    private final EuropeCallback europeDialog;

    private final HBox shipsContainer;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final TitledPanel titledPanel;

    private final EventBus eventBus;

    private final I18n i18n;

    private T titleKey;

    private T onMouseEnteredKey;

    @Inject
    public PanelHighSeas(final EuropeCallback europeDialog, final ImageProvider imageProvider,
            final GameModelController gameModelController, final EventBus eventBus,
            final I18n i18n) {
        this.europeDialog = Preconditions.checkNotNull(europeDialog);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);

        shipsContainer = new HBox();
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(
                shipsContainer, this::isItCorrectObject);
        shipsContainer.setOnDragEntered(backgroundHighlighter::onDragEntered);
        shipsContainer.setOnDragExited(backgroundHighlighter::onDragExited);
        shipsContainer.setOnDragOver(this::onDragOver);
        shipsContainer.setOnDragDropped(this::onDragDropped);
        shipsContainer.minHeightProperty().set(60);

        titledPanel = new TitledPanel();
        titledPanel.getStyleClass().add("ships-container");
        titledPanel.getChildren().add(shipsContainer);
        titledPanel.setOnMouseEntered(this::onMouseEntered);
        titledPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        if (onMouseEnteredKey != null) {
            eventBus.post(new StatusBarMessageEvent(i18n.get(onMouseEnteredKey), Source.EUROPE));
        }
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    public void addStyle(final String style) {
        titledPanel.getStyleClass().add(style);
    }

    @Override
    public void repaint() {
        shipsContainer.getChildren().clear();
        showShips();
    }

    private void onDragOver(final DragEvent event) {
        if (isItCorrectObject(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private boolean isItCorrectObject(final Dragboard db) {
        if (!isShownShipsTravelingToEurope && db.hasString()) {
            return ClipboardEval.make(gameModelController.getModel(), db)
                    .filterUnit(unit -> unit.getType().isShip()).getUnit().isPresent();
        } else {
            return false;
        }
    }

    private void onDragDropped(final DragEvent event) {
        final Optional<Unit> oUnit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard()).getUnit();
        if (oUnit.isPresent()) {
            final Unit unit = oUnit.get();
            Preconditions.checkState(unit.getType().isShip(),
                    "Only ships could be send to high seas");
            unit.placeToHighSeas(false);
            europeDialog.repaint();
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }
    }

    private void showShips() {
        gameModelController.getModel().getHighSea()
                .getUnitsTravelingTo(gameModelController.getCurrentPlayer(),
                        isShownShipsTravelingToEurope)
                .forEach(unit -> {
                    shipsContainer.getChildren()
                            .add(new ImageView(imageProvider.getUnitImage(unit)));
                });
    }

    /**
     * @param isShownShipsTravelingToEurope
     *            the isShownShipsTravelingToEurope to set
     */
    public void setShownShipsTravelingToEurope(final boolean isShownShipsTravelingToEurope) {
        this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
    }

    @Override
    public Region getContent() {
        return titledPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        titledPanel.setTitle(i18n.get(titleKey));
    }

    /**
     * @param titleKey
     *            the titleKey to set
     */
    public void setTitleKey(T titleKey) {
        this.titleKey = Preconditions.checkNotNull(titleKey);
    }

    /**
     * @param onMouseOverKey
     *            the onMouseOverKey to set
     */
    public void setOnMouseEnteredKey(T onMouseOverKey) {
        this.onMouseEnteredKey = Preconditions.checkNotNull(onMouseOverKey);
    }

}
