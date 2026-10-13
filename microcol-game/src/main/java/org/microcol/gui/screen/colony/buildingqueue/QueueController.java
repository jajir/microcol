package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.model.Model;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.ColonyBuildingQueue;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class QueueController {

    private final ColonyDialogCallback colonyDialogCallback;
    private final GameModelController game;
    private final ObservableList<ColonyBuildingItemProgress<?>> observableList;

    private Colony originalColony;
    private Model boundModel;

    @Inject
    public QueueController(final ColonyDialogCallback colonyDialogCallback,
            final GameModelController game) {
        this.colonyDialogCallback = Preconditions
                .checkNotNull(colonyDialogCallback);
        this.game = Preconditions.checkNotNull(game);
        this.observableList = FXCollections.observableArrayList();
    }

    /**
     * Capture the current model and colony from the dialog and game controller.
     */
    public void begin() {
        this.boundModel = game.getModel();
        this.originalColony = colonyDialogCallback.getColony();
        repaint();
    }

    /**
     * @return true if the dialog is still bound to the same colony and model.
     */
    public boolean isCurrent() {
        return originalColony != null
                && colonyDialogCallback.getColony() == originalColony
                && game.isGameModelReady() && game.getModel() == boundModel;
    }

    public Colony getColony() {
        return originalColony;
    }

    public ObservableList<ColonyBuildingItemProgress<?>> getObservableList() {
        return observableList;
    }

    /**
     * Refresh the observable list from the underlying colony queue.
     */
    public void repaint() {
        if (originalColony != null) {
            observableList.setAll(
                    originalColony.getColonyBuildingQueue().getBuildingQueue());
        }
    }

    /**
     * Append a new building item to the end of the queue.
     */
    public void addAtEnd(final ColonyBuildingItem item) {
        Preconditions.checkArgument(isCurrent(), "Colony view has changed");
        if (item instanceof ColonyBuildingItemConstruction) {
            ConstructionType ct = ((ColonyBuildingItemConstruction) item)
                    .getConstructionType();
            originalColony.getColonyBuildingQueue().addAtEnd(ct);
        } else if (item instanceof ColonyBuildingItemUnit) {
            UnitType ut = ((ColonyBuildingItemUnit) item).getUnitType();
            originalColony.getColonyBuildingQueue().addAtEnd(ut);
        } else {
            throw new IllegalArgumentException(
                    "Unknown colony building item type");
        }
        repaint();
    }

    /**
     * Remove a specific item from the queue.
     */
    public void removeItem(final ColonyBuildingItemProgress<?> item) {
        Preconditions.checkArgument(isCurrent(), "Colony view has changed");
        originalColony.getColonyBuildingQueue().removeItem(item);
        repaint();
    }

    /**
     * Move an item up or down one position.
     *
     * @param item      the item to move
     * @param direction -1 to move up, +1 to move down
     */
    public void moveItem(final ColonyBuildingItemProgress<?> item,
            final int direction) {
        Preconditions.checkArgument(isCurrent(), "Colony view has changed");
        Preconditions.checkArgument(direction == -1 || direction == 1,
                "direction must be -1 or +1");
        ColonyBuildingQueue queue = originalColony.getColonyBuildingQueue();
        List<ColonyBuildingItemProgress<?>> list = queue.getBuildingQueue();
        int index = list.indexOf(item);
        Preconditions.checkArgument(index >= 0,
                "Queue item is no longer present");
        if (direction == -1 && index > 0) {
            queue.moveItemBefore(item, list.get(index - 1));
        } else if (direction == 1) {
            if (index == list.size() - 2) {
                queue.moveItemAtTheEnd(item);
            } else if (index < list.size() - 1) {
                queue.moveItemBefore(item, list.get(index + 2));
            }
        }
        repaint();
    }

}
