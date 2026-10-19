package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.ColonyJobChoice.UnavailableReason;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

class ColonyJobChoiceTest {
    private GameModelPo data() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final ColonyPo colony = data.getModel().getColonies().get(0);
        colony.getColonyFields().forEach(f -> {
            final boolean source = f.getDirection()
                    .equals(Direction.west.getVector());
            f.setWorkerId(source ? 0 : null);
            f.setProducedGoodsType(source ? GoodsType.LUMBER : null);
        });
        colony.getConstructions()
                .forEach(c -> c.getSlots().forEach(s -> s.setWorkerId(null)));
        for (int id : new int[] { 0, 1, 2, 4, 5 }) {
            final UnitPo unit = unit(data, id);
            unit.setPlaceConstructionSlot(null);
            unit.setPlaceColonyField(null);
            unit.setPlaceMap(null);
            unit.setTools(0);
            if (id == 0) {
                unit.setPlaceColonyField(new PlaceColonyFieldPo());
            } else {
                final PlaceMapPo place = new PlaceMapPo();
                place.setLocation(colony.getLocation());
                place.setOrientation(Direction.west);
                unit.setPlaceMap(place);
            }
        }
        final ColonyPo neighbor = data.getModel().getColonies().get(1);
        neighbor.setLocation(colony.getLocation().add(Location.of(2, 0)));
        neighbor.getColonyFields().forEach(f -> {
            final boolean shared = f.getDirection()
                    .equals(Direction.west.getVector());
            f.setWorkerId(shared ? 6 : null);
            f.setProducedGoodsType(shared ? GoodsType.LUMBER : null);
        });
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        colony.getLocation().getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.ARCTIC));
        terrain.put(colony.getLocation(), TerrainType.GRASSLAND);
        terrain.put(neighbor.getLocation(), TerrainType.GRASSLAND);
        // Lumber exists only in forest. The forested lumber levels of the aid
        // card are 2 (desert), 4 (tundra) and 6 (grassland, plains): the
        // current west field is the poorest, north is the next best and
        // north-west is the best eligible one. The east square shared with the
        // neighbor yields 6 as well; no legal square yields more.
        final Location west = colony.getLocation()
                .add(Direction.west.getVector());
        final Location north = colony.getLocation()
                .add(Direction.north.getVector());
        final Location northWest = colony.getLocation()
                .add(Direction.northWest.getVector());
        final Location east = colony.getLocation()
                .add(Direction.east.getVector());
        terrain.put(west, TerrainType.DESERT);
        terrain.put(north, TerrainType.TUNDRA);
        terrain.put(northWest, TerrainType.GRASSLAND);
        terrain.put(east, TerrainType.PLAINS);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Set.of(west, north, northWest, east));
        data.getModel().getMap().setFields(Collections.emptySet());
        colony.getColonyWarehouse().put("ORE", 1);
        return data;
    }

    private UnitPo unit(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream().filter(u -> u.getId() == id)
                .findFirst().orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Model model() {
        return load(data());
    }

    private Colony colony(final Model model) {
        return model.getColonyById(0).orElseThrow();
    }

    private ColonyField field(final Model model, final Direction direction) {
        return colony(model).getColonyFieldInDirection(direction);
    }

    private ColonyJobChoice choice(final Model model, final int id,
            final Kind kind, final GoodsType goods) {
        return colony(model).getJobChoices(model.getUnitById(id)).stream()
                .filter(c -> c.getKind() == kind && c.getGoodsType() == goods)
                .findFirst().orElseThrow();
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private void rejected(final Model model, final ColonyJobChoice choice) {
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, choice::execute);
        assertEquals(before, snapshot(model));
    }

    @Test
    void previewsAreImmutableReadOnlyAndExcludeTheBetterSharedSquare() {
        final Model model = model();
        final String before = snapshot(model);
        final List<ColonyJobChoice> choices = colony(model)
                .getJobChoices(model.getUnitById(0));
        assertThrows(UnsupportedOperationException.class, choices::clear);
        final ColonyJobChoice best = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        assertEquals(Goods.of(GoodsType.LUMBER, 2),
                best.getCurrentFieldYield().orElseThrow());
        assertEquals(Goods.of(GoodsType.LUMBER, 6),
                best.getBestFieldYield().orElseThrow());
        assertEquals(field(model, Direction.northWest).getLocation(),
                best.getTargetLocation().orElseThrow());
        assertEquals(6, field(model, Direction.east)
                .getGoodsTypeProduction(GoodsType.LUMBER));
        assertTrue(field(model, Direction.east).isWorkedByAnotherColony());
        assertEquals(before, snapshot(model));
    }

    @Test
    void aStrictlyBetterSharedSquareIsNeverOfferedAsTheBestEligibleField() {
        final GameModelPo data = data();
        final Location center = data.getModel().getColonies().get(0)
                .getLocation();
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        // Forested tundra yields 4 and forested desert 2 lumber, so the
        // forested plains square shared with the neighbor (6) is strictly
        // better than every square this colony may take.
        terrain.put(center.add(Direction.northWest.getVector()),
                TerrainType.TUNDRA);
        terrain.put(center.add(Direction.north.getVector()),
                TerrainType.DESERT);
        data.getModel().getMap().setTerrainType(terrain);
        final Model model = load(data);
        assertEquals(6, field(model, Direction.east)
                .getGoodsTypeProduction(GoodsType.LUMBER));
        assertTrue(field(model, Direction.east).isWorkedByAnotherColony());
        final ColonyJobChoice best = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        assertEquals(Goods.of(GoodsType.LUMBER, 2),
                best.getCurrentFieldYield().orElseThrow());
        assertEquals(Goods.of(GoodsType.LUMBER, 4),
                best.getBestFieldYield().orElseThrow());
        assertEquals(field(model, Direction.northWest).getLocation(),
                best.getTargetLocation().orElseThrow());
    }

    @Test
    void cropChangeStaysOnCurrentFieldAndMoveToBestIsExplicit() {
        final Model model = model();
        final Unit worker = model.getUnitById(0);
        final Place source = worker.getPlace();
        choice(model, 0, Kind.CURRENT_FIELD, GoodsType.CORN).execute();
        assertSame(source, worker.getPlace());
        assertEquals(GoodsType.CORN,
                worker.getPlaceColonyField().getProducedGoodsType());
        final ColonyJobChoice best = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        best.execute();
        assertTrue(field(model, Direction.west).isEmpty());
        assertSame(worker,
                field(model, Direction.northWest).getUnit().orElseThrow());
        assertEquals(best.getPotential(),
                field(model, Direction.northWest).getProduction());
        final String before = snapshot(model);
        choice(model, 0, Kind.BEST_FIELD, GoodsType.LUMBER).execute();
        assertEquals(before, snapshot(model));
    }

    @Test
    void centerAndIndoorWorkersCanChooseBestOutdoorJob() {
        for (boolean indoors : new boolean[] { false, true }) {
            final Model model = model();
            final Unit worker = model.getUnitById(4);
            if (indoors) {
                choice(model, 4, Kind.INDOOR, GoodsType.BELL).execute();
            }
            final ColonyJobChoice best = choice(model, 4, Kind.BEST_FIELD,
                    GoodsType.LUMBER);
            assertTrue(best.getCurrentFieldYield().isEmpty());
            best.execute();
            assertSame(worker,
                    field(model, Direction.northWest).getUnit().orElseThrow());
        }
    }

    @Test
    void tiedFieldsUseStableColonyOrder() {
        final GameModelPo data = data();
        final Location center = data.getModel().getColonies().get(0)
                .getLocation();
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(center.add(Direction.north.getVector()),
                TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final Model model = load(data);
        final Location first = colony(model).getColonyFields().stream()
                .filter(f -> f.getDirection() == Direction.north
                        || f.getDirection() == Direction.northWest)
                .findFirst().orElseThrow().getLocation();
        assertEquals(first, choice(model, 4, Kind.BEST_FIELD, GoodsType.LUMBER)
                .getTargetLocation().orElseThrow());
    }

    @Test
    void occupiedFieldsKeepTheJobVisibleWithoutInventingOutput() {
        final Model model = model();
        model.getUnitById(1).placeToColonyField(field(model, Direction.north),
                GoodsType.LUMBER);
        model.getUnitById(2).placeToColonyField(
                field(model, Direction.northWest), GoodsType.LUMBER);
        final ColonyJobChoice unavailable = choice(model, 4, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        assertEquals(UnavailableReason.NO_ELIGIBLE_FIELD,
                unavailable.getUnavailableReason().orElseThrow());
        assertTrue(unavailable.getPotential().isEmpty());
        assertTrue(unavailable.getBestFieldYield().isEmpty());
        rejected(model, unavailable);
    }

    @Test
    void unsupportedCurrentCropHasAReasonAndTheBestAlternative() {
        final Model model = model();
        final ColonyJobChoice unavailable = colony(model)
                .getJobChoices(model.getUnitById(0)).stream()
                .filter(c -> c.getUnavailableReason()
                        .orElse(null) == UnavailableReason.CROP_UNSUPPORTED)
                .findFirst().orElseThrow();
        assertTrue(unavailable.getPotential().isEmpty());
        assertTrue(unavailable.getCurrentFieldYield().isEmpty());
        rejected(model, unavailable);
    }

    @Test
    void sourceCropChangedOnTheSamePlaceRejectsTheOldOffer() {
        final Model model = model();
        final ColonyJobChoice offered = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        final Place original = model.getUnitById(0).getPlace();
        field(model, Direction.west).setProducedGoodsType(GoodsType.CORN);
        assertSame(original, model.getUnitById(0).getPlace());
        rejected(model, offered);
    }

    @Test
    void movedWorkerAndOccupiedDestinationRejectWithoutRetargeting() {
        final Model moved = model();
        final ColonyJobChoice oldSource = choice(moved, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        moved.getUnitById(0).placeToColonyField(field(moved, Direction.north),
                GoodsType.LUMBER);
        rejected(moved, oldSource);
        final Model occupied = model();
        final ColonyJobChoice oldTarget = choice(occupied, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        occupied.getUnitById(4).placeToColonyField(
                field(occupied, Direction.northWest), GoodsType.LUMBER);
        assertTrue(field(occupied, Direction.north)
                .canAccept(occupied.getUnitById(0), GoodsType.LUMBER));
        rejected(occupied, oldTarget);
    }

    @Test
    void freeingABetterAlternativeDoesNotRedirectAnExactOffer() {
        final Model model = model();
        model.getUnitById(4).placeToColonyField(
                field(model, Direction.northWest), GoodsType.LUMBER);
        final ColonyJobChoice offer = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.LUMBER);
        assertEquals(field(model, Direction.north).getLocation(),
                offer.getTargetLocation().orElseThrow());
        model.getUnitById(4).placeToMap(colony(model).getLocation());
        offer.execute();
        assertSame(model.getUnitById(0),
                field(model, Direction.north).getUnit().orElseThrow());
        assertTrue(field(model, Direction.northWest).isEmpty());
    }

    @Test
    void changedPotentialRejectsWithoutChangingTheCurrentJob() {
        // Plowing adds food only on bare land, so the best corn square (bare
        // grassland, 4) must not carry a forest.
        final GameModelPo data = data();
        final Location northWest = data.getModel().getColonies().get(0)
                .getLocation().add(Direction.northWest.getVector());
        final java.util.Set<Location> trees = new java.util.HashSet<>(
                data.getModel().getMap().getTreeSet());
        trees.remove(northWest);
        data.getModel().getMap().setTrees(trees);
        final Model model = load(data);
        final ColonyJobChoice offer = choice(model, 0, Kind.BEST_FIELD,
                GoodsType.CORN);
        assertEquals(northWest, offer.getTargetLocation().orElseThrow());
        assertEquals(Goods.of(GoodsType.CORN, 4),
                offer.getPotential().orElseThrow());
        model.getMap().plowFiled(offer.getTargetLocation().orElseThrow());
        rejected(model, offer);
    }

    @Test
    void ordinaryAndExpertPotentialMatchActualOutputAndKeepShortageAdvisory() {
        for (boolean expert : new boolean[] { false, true }) {
            final GameModelPo data = data();
            if (expert)
                unit(data, 4).setType(UnitType.MASTER_BLACKSMITH);
            final Model model = load(data);
            final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                    GoodsType.TOOLS);
            assertEquals(Goods.of(GoodsType.TOOLS, expert ? 6 : 3),
                    offer.getPotential().orElseThrow());
            assertEquals(Goods.of(GoodsType.ORE, expert ? 6 : 3),
                    offer.getInputNeeded().orElseThrow());
            assertEquals(Goods.of(GoodsType.ORE, 1),
                    offer.getCurrentStock().orElseThrow());
            assertEquals(expert, offer.isSpecialty());
            offer.execute();
            final ConstructionSlot slot = model.getUnitById(4)
                    .getPlaceConstruction().getConstructionSlot();
            assertEquals(offer.getPotential(),
                    slot.getProduction(Goods.of(GoodsType.ORE, expert ? 6 : 3))
                            .getProducedGoods());
            assertEquals(Goods.of(GoodsType.TOOLS, 1),
                    slot.getProduction(Goods.of(GoodsType.ORE, 1))
                            .getProducedGoods().orElseThrow());
            assertEquals(Goods.of(GoodsType.ORE, 1),
                    slot.getProduction(Goods.of(GoodsType.ORE, 1))
                            .getConsumedGoods().orElseThrow());
            final String before = snapshot(model);
            choice(model, 4, Kind.INDOOR, GoodsType.TOOLS).execute();
            assertEquals(before, snapshot(model));
        }
    }

    @Test
    void changedInputStockAloneDoesNotInvalidateAssignment() {
        final Model model = model();
        final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        colony(model).getWarehouse().addGoods(Goods.of(GoodsType.ORE, 5));
        offer.execute();
        assertEquals(Goods.of(GoodsType.ORE, 6),
                colony(model).getWarehouse().getGoods(GoodsType.ORE));
    }

    @Test
    void noInputJobExcludesAutomaticBaseProduction() {
        final Model model = model();
        final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                GoodsType.BELL);
        offer.execute();
        assertTrue(offer.getInputNeeded().isEmpty());
        assertEquals(offer.getPotential(),
                model.getUnitById(4).getPlaceConstruction()
                        .getConstructionSlot().getProduction()
                        .getProducedGoods());
        assertEquals(Goods.of(GoodsType.BELL, 3),
                offer.getPotential().orElseThrow());
    }

    @Test
    void fullBuildingAndChangedSlotRejectWithoutUsingAnotherSlot() {
        final Model model = model();
        final Construction building = colony(model)
                .getConstructionByType(ConstructionType.TOWN_HALL);
        final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                GoodsType.BELL);
        model.getUnitById(1).placeToColonyStructureSlot(building.getSlotAt(0));
        rejected(model, offer);
        model.getUnitById(2).placeToColonyStructureSlot(building.getSlotAt(1));
        model.getUnitById(5).placeToColonyStructureSlot(building.getSlotAt(2));
        final ColonyJobChoice full = choice(model, 4, Kind.INDOOR,
                GoodsType.BELL);
        assertEquals(UnavailableReason.BUILDING_FULL,
                full.getUnavailableReason().orElseThrow());
        assertTrue(full.getPotential().isEmpty());
        rejected(model, full);
    }

    @Test
    void upgradedBuildingAndRemovedColonyRejectOldOffers() {
        final Model upgraded = model();
        final ColonyJobChoice offer = choice(upgraded, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        colony(upgraded)
                .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE)
                .upgrade();
        rejected(upgraded, offer);
        final Model removed = model();
        final ColonyJobChoice lost = choice(removed, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        removed.destroyColony(colony(removed));
        rejected(removed, lost);
    }

    @Test
    void ownerChangeStoppedGameAndUnregisteredWorkerRejectOldOffers() {
        final Model changed = model();
        final ColonyJobChoice changedOwner = choice(changed, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        changed.getUnitById(4)
                .takeOver(changed.getPlayers().stream()
                        .filter(p -> p != colony(changed).getOwner())
                        .findFirst().orElseThrow());
        rejected(changed, changedOwner);
        final Model stopped = model();
        final ColonyJobChoice stoppedOffer = choice(stopped, 0,
                Kind.CURRENT_FIELD, GoodsType.LUMBER);
        stopped.stop();
        rejected(stopped, stoppedOffer);
        final Model removed = model();
        final ColonyJobChoice removedWorker = choice(removed, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        removed.destroyUnit(removed.getUnitById(4));
        rejected(removed, removedWorker);
    }

    @Test
    void foreignIdentityAndWrongRoleCannotReceiveUsableOffers() {
        final Model model = model();
        final Model other = model();
        final String before = snapshot(model);
        final String otherBefore = snapshot(other);
        assertTrue(colony(model).getJobChoices(null).isEmpty());
        for (ColonyJobChoice offer : colony(model)
                .getJobChoices(other.getUnitById(4))) {
            assertEquals(UnavailableReason.ACTOR_UNAVAILABLE,
                    offer.getUnavailableReason().orElseThrow());
            rejected(model, offer);
        }
        for (ColonyJobChoice offer : colony(model)
                .getJobChoices(model.getUnitById(3))) {
            assertEquals(UnavailableReason.ACTOR_UNAVAILABLE,
                    offer.getUnavailableReason().orElseThrow());
        }
        assertEquals(before, snapshot(model));
        assertEquals(otherBefore, snapshot(other));
    }

    @Test
    void equipmentOverflowDisablesMovesAndStaleCapacityRejectsAtomically() {
        final GameModelPo data = data();
        unit(data, 4).setTools(80);
        data.getModel().getColonies().get(0).getColonyWarehouse().put("TOOLS",
                20);
        final Model model = load(data);
        final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        colony(model).getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, 1));
        rejected(model, offer);
        final ColonyJobChoice disabled = choice(model, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        assertEquals(UnavailableReason.EQUIPMENT_BLOCKED,
                disabled.getUnavailableReason().orElseThrow());
        assertTrue(disabled.getPotential().isEmpty());
    }

    @Test
    void endingTheOwnerTurnInvalidatesAnOtherwiseUnchangedOffer() {
        final Model model = model();
        final ColonyJobChoice offer = choice(model, 4, Kind.INDOOR,
                GoodsType.TOOLS);
        model.endTurn();
        assertNotSame(colony(model).getOwner(), model.getCurrentPlayer());
        rejected(model, offer);
    }

    @Test
    void twoSmithsShareOreWithoutDoubleSpendingAndPreviewMatchesTurn() {
        for (int ore : new int[] { 0, 3, 6, 9 }) {
            final GameModelPo data = data();
            final ColonyPo saved = data.getModel().getColonies().get(0);
            saved.getColonyWarehouse().put("CORN", 100);
            saved.getColonyWarehouse().put("ORE", ore);
            saved.getColonyWarehouse().put("TOOLS", 0);
            unit(data, 4).setType(UnitType.MASTER_BLACKSMITH);
            final Model model = load(data);
            assertEquals("Dutch", model.getCurrentPlayer().getName());
            choice(model, 4, Kind.INDOOR, GoodsType.TOOLS).execute();
            choice(model, 5, Kind.INDOOR, GoodsType.TOOLS).execute();
            final Colony smithColony = colony(model);
            final Construction smithy = smithColony
                    .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE);
            final ConstructionTurnProduction production = smithy
                    .getProduction(Goods.of(GoodsType.ORE, ore));
            final int expectedTools = Math.min(ore, 9);
            assertEquals(Goods.of(GoodsType.ORE, expectedTools),
                    production.getConsumedGoods().orElseThrow());
            assertEquals(Goods.of(GoodsType.TOOLS, expectedTools),
                    production.getProducedGoods().orElseThrow());
            final ColonyProductionStats preview = smithColony.getGoodsStats();
            assertEquals(expectedTools,
                    preview.getStatsByType(GoodsType.ORE).getConsumed());
            assertEquals(expectedTools,
                    preview.getStatsByType(GoodsType.TOOLS).getRowProduction());
            final int previewOre = preview.getStatsByType(GoodsType.ORE)
                    .getInWarehouseAfter();
            final int previewTools = preview.getStatsByType(GoodsType.TOOLS)
                    .getInWarehouseAfter();
            assertEquals(ore - expectedTools, previewOre);
            assertEquals(expectedTools, previewTools);
            model.endTurn();
            model.endTurn();
            assertEquals("Dutch", model.getCurrentPlayer().getName());
            assertEquals(Goods.of(GoodsType.ORE, previewOre),
                    smithColony.getWarehouse().getGoods(GoodsType.ORE));
            assertEquals(Goods.of(GoodsType.TOOLS, previewTools),
                    smithColony.getWarehouse().getGoods(GoodsType.TOOLS));
        }
    }

    @Test
    void savedCompletedBoundaryDoesNotRepeatSmithyProduction() {
        final GameModelPo data = data();
        final ColonyPo saved = data.getModel().getColonies().get(0);
        saved.getColonyWarehouse().put("CORN", 100);
        saved.getColonyWarehouse().put("ORE", 9);
        saved.getColonyWarehouse().put("TOOLS", 0);
        unit(data, 4).setType(UnitType.MASTER_BLACKSMITH);
        final Model model = load(data);
        choice(model, 4, Kind.INDOOR, GoodsType.TOOLS).execute();
        choice(model, 5, Kind.INDOOR, GoodsType.TOOLS).execute();
        final ColonyProductionStats preview = colony(model).getGoodsStats();
        assertEquals(0,
                preview.getStatsByType(GoodsType.ORE).getInWarehouseAfter());
        assertEquals(9,
                preview.getStatsByType(GoodsType.TOOLS).getInWarehouseAfter());
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        assertEquals(Goods.of(GoodsType.ORE, 0),
                colony(model).getWarehouse().getGoods(GoodsType.ORE));
        assertEquals(Goods.of(GoodsType.TOOLS, 9),
                colony(model).getWarehouse().getGoods(GoodsType.TOOLS));
        data.setModel(model.save());
        final Model resumed = load(data);
        final String completed = snapshot(resumed);
        resumed.startGame();
        resumed.startGame();
        assertEquals(completed, snapshot(resumed));
        assertEquals(Goods.of(GoodsType.ORE, 0),
                colony(resumed).getWarehouse().getGoods(GoodsType.ORE));
        assertEquals(Goods.of(GoodsType.TOOLS, 9),
                colony(resumed).getWarehouse().getGoods(GoodsType.TOOLS));
    }

}
