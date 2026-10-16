package org.microcol.gui.screen.colony.buildingqueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.model.*;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

class QueueControllerTest {
    private Colony colony;
    private ColonyDialogCallback callback;
    private GameModelController game;
    private QueueController controller;

    @BeforeEach
    void prepare() {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
        colony = model.getColonyByName("Delft").orElseThrow();
        callback = mock(ColonyDialogCallback.class);
        game = mock(GameModelController.class);
        when(callback.getColony()).thenReturn(colony);
        when(game.getModel()).thenReturn(model);
        when(game.isGameModelReady()).thenReturn(true);
        controller = new QueueController(callback, game);
        controller.begin();
    }

    @Test
    void moveBothDirectionsKeepsTheModelAndPresentationInTheSameOrder() {
        controller.addAtEnd(
                new ColonyBuildingItemConstruction(ConstructionType.WAREHOUSE));
        controller.addAtEnd(
                new ColonyBuildingItemConstruction(ConstructionType.DOCK));
        controller.addAtEnd(new ColonyBuildingItemUnit(UnitType.WAGON));
        final ColonyBuildingItemProgress<?> first = controller
                .getObservableList().get(0);
        colony.getWarehouse()
                .removeGoods(colony.getWarehouse().getGoods(GoodsType.HAMMERS));
        colony.getWarehouse().addGoods(Goods.of(GoodsType.HAMMERS, 17));
        controller.moveItem(first, 1);
        assertEquals(List.of(1, 0, 2), ids());
        controller.moveItem(first, 1);
        assertEquals(List.of(1, 2, 0), ids());
        controller.moveItem(first, -1);
        assertEquals(List.of(1, 0, 2), ids());
        assertEquals(17,
                colony.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(colony.getColonyBuildingQueue().getBuildingQueue(),
                controller.getObservableList());
    }

    @Test
    void removedItemCannotReorderOrDisturbThePresentation() {
        controller.addAtEnd(
                new ColonyBuildingItemConstruction(ConstructionType.WAREHOUSE));
        final ColonyBuildingItemProgress<?> removed = controller
                .getObservableList().get(0);
        controller.removeItem(removed);
        controller.addAtEnd(
                new ColonyBuildingItemConstruction(ConstructionType.DOCK));
        final List<?> before = List.copyOf(controller.getObservableList());
        assertThrows(IllegalArgumentException.class,
                () -> controller.moveItem(removed, 1));
        assertEquals(before, controller.getObservableList());
        assertEquals(before,
                colony.getColonyBuildingQueue().getBuildingQueue());
    }

    @Test
    void rejectedAdmissionDoesNotPublishAnItem() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.addAtEnd(new ColonyBuildingItemConstruction(
                        ConstructionType.BLACKSMITHS_SHOP)));
        assertTrue(controller.getObservableList().isEmpty());
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }

    @Test
    void replacedModelCannotModifyTheOldColony() {
        when(game.getModel()).thenReturn(mock(Model.class));
        assertFalse(controller.isCurrent());
        assertThrows(IllegalArgumentException.class,
                () -> controller.addAtEnd(new ColonyBuildingItemConstruction(
                        ConstructionType.WAREHOUSE)));
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
        assertTrue(controller.getObservableList().isEmpty());
    }

    @Test
    void changedCallbackDoesNotRebindOnRepaint() {
        when(callback.getColony()).thenReturn(mock(Colony.class));
        controller.repaint();
        assertSame(colony, controller.getColony());
        assertFalse(controller.isCurrent());
        assertThrows(IllegalArgumentException.class,
                () -> controller.addAtEnd(new ColonyBuildingItemConstruction(
                        ConstructionType.WAREHOUSE)));
    }

    @Test
    void stoppedGameRejectsWithoutReadingItsMissingModel() {
        when(game.isGameModelReady()).thenReturn(false);
        when(game.getModel())
                .thenThrow(new IllegalStateException("Model is not ready"));
        assertFalse(controller.isCurrent());
        assertThrows(IllegalArgumentException.class,
                () -> controller.addAtEnd(new ColonyBuildingItemConstruction(
                        ConstructionType.WAREHOUSE)));
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }

    private List<Integer> ids() {
        return controller.getObservableList().stream()
                .map(ColonyBuildingItemProgress::getId)
                .collect(Collectors.toList());
    }
}
