package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class FoundingPreconditionsTest {
    private static final Location SITE = Location.of(26, 15);

    private Model prepare(final Location site, final TerrainType center,
            final TerrainType surroundings) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        site.getNeighbors().stream()
                .filter(at -> at.getX() >= 1 && at.getX() <= 50
                        && at.getY() >= 1 && at.getY() <= 30)
                .forEach(at -> terrain.put(at, surroundings));
        terrain.put(site, center);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(java.util.Collections.emptySet());
        final UnitPo founder = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 4).findFirst().orElseThrow();
        founder.setPlaceCargoSlot(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(site);
        place.setOrientation(Direction.west);
        founder.setPlaceMap(place);
        founder.setAvailableMoves(1);
        data.getModel().getUnits().stream().filter(u -> u.getId() == 5)
                .findFirst().orElseThrow().getCargo().getSlots().get(0)
                .setUnitId(null);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private Colony found(final Model model) {
        return model.buildColony(model.getPlayerByName("France"),
                model.getUnitById(4), "Québec");
    }

    @Test
    void seaIsRejectedBeforeChangingTheWorld() {
        final Model model = prepare(SITE, TerrainType.OCEAN,
                TerrainType.GRASSLAND);
        final String before = snapshot(model);
        assertFalse(model.canUnitBuildColony(model.getUnitById(4)));
        assertThrows(IllegalArgumentException.class, () -> found(model));
        assertEquals(before, snapshot(model));
    }

    @Test
    void mountainsAreRejectedBeforeChangingTheWorld() {
        final Model model = prepare(SITE, TerrainType.MOUNTAIN,
                TerrainType.GRASSLAND);
        final String before = snapshot(model);
        assertFalse(model.canUnitBuildColony(model.getUnitById(4)));
        assertThrows(IllegalArgumentException.class, () -> found(model));
        assertEquals(before, snapshot(model));
    }

    @Test
    void stoppedModelRejectsFoundingWithoutConsumingAnId() {
        final Model model = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        model.stop();
        final String before = snapshot(model);
        assertFalse(model.canUnitBuildColony(model.getUnitById(4)));
        assertThrows(IllegalStateException.class, () -> found(model));
        assertEquals(before, snapshot(model));
    }

    @Test
    void equipmentThatCannotFitRejectsBeforeRegistrationOrUnequipping() {
        final Model model = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final UnitFreeColonist founder = (UnitFreeColonist) model
                .getUnitById(4);
        founder.setHoldingGuns(true);
        founder.setTools(101);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> found(model));
        assertEquals(before, snapshot(model));
    }

    @ParameterizedTest
    @CsvSource({ "1,15", "50,15", "25,1", "25,30", "1,1", "50,30" })
    void legalMapEdgeCanBeFoundedAndRestoredWithoutOffMapProduction(final int x,
            final int y) {
        final Location edge = Location.of(x, y);
        final Model model = prepare(edge, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final Colony colony = found(model);
        assertEquals(edge, colony.getLocation());
        assertEquals(0, colony.getId());
        assertEquals(1, model.save().getNextColonyId());
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertSame(model.getUnitById(4), colony.getUnitsInsideColony().get(0));
        assertTrue(colony.getColonyFields().stream()
                .filter(field -> field.getUnit().isPresent()).allMatch(
                        field -> model.getMap().isValid(field.getLocation())));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(edge,
                restored.getColonyById(0).orElseThrow().getLocation());
        assertEquals(4, restored.getColonyById(0).orElseThrow()
                .getUnitsInsideColony().get(0).getId());
    }

    @Test
    void offMapAssignmentRejectsWithoutDetachingTheResident() {
        final Model model = prepare(Location.of(1, 15), TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final Colony colony = found(model);
        final String before = snapshot(model);
        final ColonyField outside = colony
                .getColonyFieldInDirection(Direction.west);
        assertFalse(outside.isValid());
        assertFalse(outside.canProduce(GoodsType.CORN));
        assertEquals(0, outside.getGoodsTypeProduction(GoodsType.CORN));
        assertThrows(IllegalStateException.class, () -> model.getUnitById(4)
                .placeToColonyField(outside, GoodsType.CORN));
        assertEquals(before, snapshot(model));
    }

    @Test
    void unproductiveLegalLandStillAllowsAResidentInside() {
        final Model model = prepare(SITE, TerrainType.ARCTIC,
                TerrainType.ARCTIC);
        final Colony colony = found(model);
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertSame(model.getUnitById(4), colony.getUnitsInsideColony().get(0));
        assertTrue(colony.getColonyFields().stream()
                .allMatch(ColonyField::isEmpty));
        assertEquals(0, colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getRowProduction());
    }

    @Test
    void validEquipmentIsReturnedAndFounderIdentityIsPreserved() {
        final Model model = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final UnitFreeColonist founder = (UnitFreeColonist) model
                .getUnitById(4);
        founder.setHoldingGuns(true);
        founder.setMounted(true);
        final Colony colony = found(model);
        assertSame(founder, colony.getUnitsInsideColony().get(0));
        assertEquals(50,
                colony.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertEquals(50,
                colony.getWarehouse().getGoods(GoodsType.HORSE).getAmount());
        assertFalse(founder.isHoldingGuns());
        assertFalse(founder.isMounted());
        assertEquals(6, model.getAllUnits().size());
    }

    @Test
    void prospectiveSiteQueryAllowsPlanningBeforeLandingWithoutMutation() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit founder = model.getUnitById(4);
        final String before = snapshot(model);
        assertEquals(0, founder.getActionPoints());
        assertTrue(model.canFoundColonyAt(founder, SITE));
        assertFalse(model.canUnitBuildColony(founder));
        assertEquals(before, snapshot(model));
    }

    @Test
    void prospectiveQueryRejectsForeignObjectsInvalidTilesAndForeignOccupants() {
        final Model model = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final Model other = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        assertFalse(model.canFoundColonyAt(other.getUnitById(4), SITE));
        assertFalse(model.canFoundColonyAt(null, SITE));
        assertFalse(model.canFoundColonyAt(model.getUnitById(4), null));
        assertFalse(model.canFoundColonyAt(model.getUnitById(4),
                Location.of(0, 15)));
        assertFalse(model.canFoundColonyAt(model.getUnitById(5), SITE));
        model.getUnitById(0).placeToLocation(SITE);
        final String before = snapshot(model);
        assertFalse(model.canFoundColonyAt(model.getUnitById(4), SITE));
        assertThrows(IllegalArgumentException.class, () -> found(model));
        assertEquals(before, snapshot(model));
    }

    @Test
    void currentTurnAndActionPointsAreRequiredOnlyForImmediateFounding() {
        final Model initial = prepare(SITE, TerrainType.GRASSLAND,
                TerrainType.GRASSLAND);
        final org.microcol.model.store.ModelPo data = initial.save();
        data.getGameManager().setCurrentPlayer("Dutch");
        final Model otherTurn = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(otherTurn.canFoundColonyAt(otherTurn.getUnitById(4), SITE));
        assertFalse(otherTurn.canUnitBuildColony(otherTurn.getUnitById(4)));
        final String before = snapshot(otherTurn);
        assertThrows(IllegalArgumentException.class, () -> found(otherTurn));
        assertEquals(before, snapshot(otherTurn));
        data.getGameManager().setCurrentPlayer("France");
        data.getUnits().stream().filter(unit -> unit.getId() == 4).findFirst()
                .orElseThrow().setAvailableMoves(0);
        final Model exhausted = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(exhausted.canFoundColonyAt(exhausted.getUnitById(4), SITE));
        assertFalse(exhausted.canUnitBuildColony(exhausted.getUnitById(4)));
        final String exhaustedBefore = snapshot(exhausted);
        assertThrows(IllegalArgumentException.class, () -> found(exhausted));
        assertEquals(exhaustedBefore, snapshot(exhausted));
    }

    @Test
    void newFounderNeverTakesAnotherColonysWorkedField() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit founder = model.getUnitById(6);
        final Location site = Location.of(15, 10);
        final Location worked = Location.of(16, 10);
        founder.placeToLocation(site);
        final Colony original = model.getColonyById(0).orElseThrow();
        assertEquals(worked,
                original.getColonyFields().stream()
                        .filter(field -> field.getUnit().isPresent())
                        .findFirst().orElseThrow().getLocation());
        assertFalse(model.getAvailableColonyFoodFields(founder.getOwner(), site)
                .contains(worked));
        final Colony colony = model.buildColony(founder.getOwner(), founder,
                "Rouen");
        assertTrue(colony.getColonyFields().stream()
                .filter(field -> field.getUnit().isPresent())
                .noneMatch(field -> field.getLocation().equals(worked)));
        assertEquals(4, original.getColonyFields().stream()
                .filter(field -> field.getUnit().isPresent()).findFirst()
                .orElseThrow().getUnit().orElseThrow().getId());
        assertFalse(model.canUnitBuildColony(founder));
        assertFalse(model.canFoundColonyAt(founder, site));
        assertFalse(model.canFoundColonyAt(founder, Location.of(15, 11)));
    }

}
