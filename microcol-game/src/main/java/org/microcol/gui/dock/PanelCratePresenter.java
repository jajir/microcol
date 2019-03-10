package org.microcol.gui.dock;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

/**
 * Container represents one open or close crate.
 */
public final class PanelCratePresenter implements JavaFxComponent {

    public final static String CRATE_CLASS = "cratePanel";

    private final I18n i18n;

    private final EventBus eventBus;

    private CargoSlot cargoSlot;

    private final PanelDockBehavior panelDockBehavior;

    private final Source source;

    private final PanelCrateView view;

    PanelCratePresenter(final PanelDockBehavior panelDockBehavior, final I18n i18n,
            final EventBus eventBus, final Source source, final PanelCrateView view) {
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.view = Preconditions.checkNotNull(view);

        getContent().setOnDragDetected(this::onDragDetected);
        getContent().setOnDragEntered(this::onDragEntered);
        getContent().setOnDragExited(this::onDragExited);
        getContent().setOnDragOver(this::onDragOver);
        getContent().setOnDragDropped(this::onDragDropped);
    }

    public void showCargoSlot(final CargoSlot cargoSlot) {
        setIsClosed(false);
        this.cargoSlot = cargoSlot;
        if (cargoSlot.isEmpty()) {
            view.emptyCargo();
        } else {
            view.showCargoSlot(cargoSlot);
        }
    }

    public void setIsClosed(final boolean isClosed) {
        if (isClosed) {
            cargoSlot = null;
            view.closeSlot();
        } else {
            view.openSlot();
        }
    }

    private void onDragEntered(final DragEvent event) {
        if (isCorrectObject(event.getDragboard())) {
            view.setBackgroudHighlighted();
        }
    }

    @SuppressWarnings("unused")
    private void onDragExited(final DragEvent event) {
        view.setBackgroudNormal();
    }

    private void onDragOver(final DragEvent event) {
        if (isCorrectObject(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        panelDockBehavior.onDragDropped(cargoSlot, event);
    }

    private void onDragDetected(final MouseEvent event) {
        if (cargoSlot == null) {
            /*
             * It's closed cargo slot, because ship is not selected or doesn't
             * contain so many slots.
             */
        } else {
            if (cargoSlot.getGoods().isPresent()) {
                eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.adjustAmountOfGoods), source));
            }
            panelDockBehavior.onDragDetected(cargoSlot, event, view.getContent());
        }
        event.consume();
    }

    private boolean isCorrectObject(final Dragboard db) {
        return cargoSlot != null && panelDockBehavior.isCorrectObject(cargoSlot, db);
    }

    @Override
    public Region getContent() {
        return view.getContent();
    }

}
