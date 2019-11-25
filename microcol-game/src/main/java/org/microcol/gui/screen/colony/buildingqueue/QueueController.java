package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemProgressConstruction;
import org.microcol.model.ColonyBuildingItemProgressUnit;
import org.microcol.model.ColonyBuildingQueue;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class QueueController {

    private final ColonyDialogCallback colonyDialogCallback;

    private final ObservableList<ColonyBuildingItemProgress<?>> observableList;

    @Inject
    public QueueController(final ColonyDialogCallback colonyDialogCallback) {
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        this.observableList = FXCollections.observableArrayList();
    }

    void repaint() {
        final Colony colony = colonyDialogCallback.getColony();
        observableList.setAll(colony.getColonyBuildingQueue().getBuildingQueue());
    }

    /**
     * @return the observableList
     */
    public ObservableList<ColonyBuildingItemProgress<?>> getObservableList() {
        return observableList;
    }

    void removeItemById(final int itemId) {
        final ColonyBuildingItemProgress<?> itemToRemove = getColonyQueue().getItemById(itemId)
                .get();
        getColonyQueue().removeItem(itemToRemove);
        observableList.remove(itemToRemove);
    }

    void addBeforeItem(final ConstructionType constructionType, final int beforeId) {
        final ColonyBuildingItemProgressConstruction item = getColonyQueue()
                .addBeforeItem(constructionType, beforeId);
        if (item != null) {
            final ColonyBuildingItemProgress<?> beforeItem = getColonyQueue().getItemById(beforeId)
                    .get();
            observableList.remove(item);
            final int index = observableList.indexOf(beforeItem);
            observableList.add(index, item);
        }
    }

    void addBeforeItem(final UnitType unitType, final int beforeId) {
        final ColonyBuildingItemProgressUnit item = getColonyQueue().addBeforeItem(unitType,
                beforeId);
        final ColonyBuildingItemProgress<?> beforeItem = getColonyQueue().getItemById(beforeId)
                .get();
        observableList.remove(item);
        final int index = observableList.indexOf(beforeItem);
        observableList.add(index, item);
    }

    void moveItem(final int movingItemId, final int beforeItemId) {
        final ColonyBuildingItemProgress<?> movingItem = getColonyQueue().getItemById(movingItemId)
                .get();
        final ColonyBuildingItemProgress<?> beforeItem = getColonyQueue().getItemById(beforeItemId)
                .get();
        observableList.remove(movingItem);
        final int index = observableList.indexOf(beforeItem);
        observableList.add(index, movingItem);
        getColonyQueue().moveItemBefore(movingItem, beforeItem);
    }

    void moveItemAtTheEnd(final int movingItemId) {
        final ColonyBuildingItemProgress<?> movingItem = getColonyQueue().getItemById(movingItemId)
                .get();
        observableList.remove(movingItem);
        observableList.add(movingItem);
        getColonyQueue().moveItemAtTheEnd(movingItem);
    }

    void addAtEnd(final ConstructionType constructionType) {
        final ColonyBuildingItemProgressConstruction item = getColonyQueue()
                .addAtEnd(constructionType);
        if (item != null) {
            observableList.add(item);
        }
    }

    void addAtEnd(final UnitType unitType) {
        observableList.add(getColonyQueue().addAtEnd(unitType));
    }

    private ColonyBuildingQueue getColonyQueue() {
        return colonyDialogCallback.getColony().getColonyBuildingQueue();
    }
}
