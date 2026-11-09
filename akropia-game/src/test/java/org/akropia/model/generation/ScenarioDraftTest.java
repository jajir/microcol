package org.akropia.model.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.akropia.model.EuropeanNation;
import org.akropia.model.Difficulty;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.NativeNation;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.generation.ScenarioProblem.Reason;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.NativeSettlementPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.model.store.UnitPo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Authored scenario drafts of the developer map editor (R-0435): bounds, atomic
 * edits under the game's geography rules, settlements, expedition and the
 * separate playability check.
 */
class ScenarioDraftTest {

    private static final Location LAND = Location.of(5, 5);

    private static final Location SEA = Location.of(8, 5);

    private final GameModelPoDao dao = new GameModelPoDao();

    private static Reason reasonOf(final Runnable edit) {
        final ScenarioDraftException rejected = assertThrows(
                ScenarioDraftException.class, edit::run);
        return rejected.getProblems().get(0).getReason();
    }

    private String state(final ScenarioDraft draft) {
        return dao.gameplayState(draft.toScenario()).toString();
    }

    /** A small island with a grassland centre and high sea in the east. */
    private static ScenarioDraft island() {
        final ScenarioDraft draft = ScenarioDraft.ocean(12, 10);
        for (int x = 3; x <= 6; x++) {
            for (int y = 3; y <= 7; y++) {
                draft.setTerrain(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        for (int y = 1; y <= 10; y++) {
            draft.setTerrain(Location.of(12, y), TerrainType.HIGH_SEA);
        }
        return draft;
    }

    @Test
    void dimensionsAreWholeNumbersWithinTheSupportedBounds() {
        assertEquals(ScenarioDraft.MIN_SIDE, ScenarioDraft.parseSide(" 10 "));
        assertEquals(ScenarioDraft.MAX_SIDE, ScenarioDraft.parseSide("100"));
        assertEquals(Reason.DIMENSION_NOT_NUMBER,
                reasonOf(() -> ScenarioDraft.parseSide("12.5")));
        assertEquals(Reason.DIMENSION_NOT_NUMBER,
                reasonOf(() -> ScenarioDraft.parseSide("")));
        assertEquals(Reason.DIMENSION_NOT_NUMBER,
                reasonOf(() -> ScenarioDraft.parseSide("ten")));
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.parseSide("0")));
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.parseSide("-12")));
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.parseSide("9")));
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.parseSide("101")));
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.ocean(10, 1_000_000)));
    }

    @Test
    void newMapIsOceanWithoutSettlementsOrUnits() {
        final ScenarioDraft draft = ScenarioDraft.ocean(10, 14);

        assertEquals(10, draft.getWidth());
        assertEquals(14, draft.getHeight());
        assertTrue(draft.getTerrainMap().isEmpty(), "Only ocean");
        assertTrue(draft.getSettlements().isEmpty());
        assertEquals(EuropeanNation.DUTCH, draft.getNation());
        assertEquals(List.of(Reason.NO_EXPEDITION),
                draft.getPlayabilityProblems().stream()
                        .map(ScenarioProblem::getReason)
                        .collect(Collectors.toList()));
    }

    @Test
    void unfinishedDraftSurvivesSaveAndReopen(@TempDir final Path dir) {
        final ScenarioDraft draft = ScenarioDraft.ocean(10, 10);
        final File file = dir.resolve("draft.akropia").toFile();

        dao.saveScenarioToFile(file.getAbsolutePath(), draft.toScenario());
        final ScenarioDraft reopened = ScenarioDraft
                .of(dao.loadScenarioFromFile(file));

        assertEquals(state(draft), state(reopened));
        assertFalse(reopened.getPlayabilityProblems().isEmpty());
    }

    @Test
    void structurallyUnsoundScenarioIsRejected(@TempDir final Path dir)
            throws Exception {
        final GameModelPo missing = new GameModelPo();
        assertEquals(Reason.STRUCTURE,
                reasonOf(() -> ScenarioDraft.of(missing)));

        final GameModelPo large = ScenarioDraft.ocean(10, 10).toScenario();
        large.getModel().getMap().setMaxX(ScenarioDraft.MAX_SIDE + 1);
        assertEquals(Reason.DIMENSION_OUT_OF_RANGE,
                reasonOf(() -> ScenarioDraft.of(large)));

        final GameModelPo truncated = ScenarioDraft.ocean(10, 10).toScenario();
        truncated.getModel().getMap().setMaxY(11);
        assertEquals(Reason.STRUCTURE,
                reasonOf(() -> ScenarioDraft.of(truncated)));

        final GameModelPo foreign = ScenarioDraft.ocean(10, 10).toScenario();
        final PlayerPo human = foreign.getModel().getGameManager().getPlayers()
                .get(0);
        human.setComputer(true);
        assertEquals(Reason.STRUCTURE,
                reasonOf(() -> ScenarioDraft.of(foreign)));

        final File broken = dir.resolve("broken.akropia").toFile();
        Files.writeString(broken.toPath(), "{\"model\": [1, 2");
        assertThrows(RuntimeException.class,
                () -> ScenarioDraft.of(dao.loadScenarioFromFile(broken)));
    }

    @Test
    void terrainForestFieldRiverAndBonusAreEditedAndRemoved() {
        final ScenarioDraft draft = island();

        draft.setForest(LAND, true);
        draft.setField(Location.of(4, 4), true);
        draft.setRiver(Location.of(3, 3), RiverSize.SMALL);
        draft.setRiver(Location.of(3, 4), RiverSize.LARGE);
        draft.setResource(Location.of(6, 6), ResourceOverlay.ORE);
        draft.setTerrain(Location.of(6, 7), TerrainType.MOUNTAIN);

        assertTrue(draft.getForests().contains(LAND));
        assertTrue(draft.getFields().contains(Location.of(4, 4)));
        assertEquals(RiverSize.LARGE, draft.getRivers().get(Location.of(3, 4)));
        assertEquals(1, draft.getRiverSegments().size(),
                "Neighbouring river squares join");
        assertEquals(ResourceOverlay.ORE,
                draft.getResources().get(Location.of(6, 6)));
        assertEquals(TerrainType.MOUNTAIN,
                draft.getTerrainMap().get(Location.of(6, 7)));

        draft.setForest(LAND, false);
        draft.setField(Location.of(4, 4), false);
        draft.setRiver(Location.of(3, 3), RiverSize.NONE);
        draft.setResource(Location.of(6, 6), ResourceOverlay.NONE);

        assertFalse(draft.getForests().contains(LAND));
        assertFalse(draft.getFields().contains(Location.of(4, 4)));
        assertFalse(draft.getRivers().containsKey(Location.of(3, 3)));
        assertTrue(draft.getRiverSegments().isEmpty());
        assertFalse(draft.getResources().containsKey(Location.of(6, 6)));
    }

    @Test
    void refusedEditsChangeNothing() {
        final ScenarioDraft draft = island();
        draft.setRiver(LAND, RiverSize.SMALL);
        final String before = state(draft);

        assertEquals(Reason.NEEDS_LAND,
                reasonOf(() -> draft.setForest(SEA, true)));
        assertEquals(Reason.GEOGRAPHY,
                reasonOf(() -> draft.setForest(LAND, true)));
        assertEquals(Reason.NEEDS_LAND, reasonOf(
                () -> draft.setResource(SEA, ResourceOverlay.PRIME_FOOD)));
        assertEquals(Reason.GEOGRAPHY, reasonOf(
                () -> draft.setResource(LAND, ResourceOverlay.PRIME_FOOD)));
        assertEquals(Reason.OUTSIDE_MAP, reasonOf(() -> draft
                .setTerrain(Location.of(13, 1), TerrainType.GRASSLAND)));

        assertEquals(before, state(draft));
    }

    @Test
    void seaTerrainRemovesWhatOnlyLandCarries() {
        final ScenarioDraft draft = island();
        draft.setForest(LAND, true);
        draft.setResource(Location.of(4, 4), ResourceOverlay.SILVER);
        draft.setRiver(Location.of(3, 3), RiverSize.SMALL);

        draft.setTerrain(LAND, TerrainType.OCEAN);
        draft.setTerrain(Location.of(4, 4), TerrainType.LAKE);
        draft.setTerrain(Location.of(3, 3), TerrainType.OCEAN);

        assertFalse(draft.getForests().contains(LAND));
        assertFalse(draft.getResources().containsKey(Location.of(4, 4)));
        assertFalse(draft.getRivers().containsKey(Location.of(3, 3)));
    }

    @Test
    void nativeSettlementsArePlacedEditedAndRemoved() {
        final ScenarioDraft draft = island();

        draft.placeSettlement(LAND, NativeNation.INCA, true);
        assertEquals(Reason.SECOND_CAPITAL, reasonOf(() -> draft
                .placeSettlement(Location.of(3, 3), NativeNation.INCA, true)));
        assertEquals(Reason.SETTLEMENT_SITE, reasonOf(
                () -> draft.placeSettlement(SEA, NativeNation.SIOUX, true)));
        assertEquals(Reason.OCCUPIED, reasonOf(
                () -> draft.placeSettlement(LAND, NativeNation.SIOUX, true)));

        draft.updateSettlement(LAND, NativeNation.SIOUX, false,
                UnitType.SEASONED_SCOUT);
        assertEquals(NativeNation.SIOUX,
                draft.getSettlementAt(LAND).orElseThrow().getNation());
        assertEquals("SEASONED_SCOUT",
                draft.getSettlementAt(LAND).orElseThrow().getTeachableSkill());
        assertEquals(List.of(Reason.NO_EXPEDITION, Reason.NO_CAPITAL),
                draft.getPlayabilityProblems().stream()
                        .map(ScenarioProblem::getReason)
                        .collect(Collectors.toList()));
        assertEquals(LAND, draft.getPlayabilityProblems().get(1).getLocation()
                .orElseThrow());
        assertTrue(natives(draft).contains(NativeNation.SIOUX));
        assertFalse(natives(draft).contains(NativeNation.INCA));

        draft.removeSettlement(LAND);
        assertTrue(draft.getSettlements().isEmpty());
        assertTrue(natives(draft).isEmpty());
    }

    private static List<NativeNation> natives(final ScenarioDraft draft) {
        return draft.toScenario().getModel().getGameManager().getPlayers()
                .stream().map(PlayerPo::getNativeNation)
                .filter(nation -> nation != null).collect(Collectors.toList());
    }

    @Test
    void settlementTileKeepsSettlementTerrain() {
        final ScenarioDraft draft = island();
        draft.placeSettlement(LAND, NativeNation.ARAWAK, true);
        final String before = state(draft);

        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.setTerrain(LAND, TerrainType.MOUNTAIN)));
        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.setRiver(LAND, RiverSize.SMALL)));
        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.addUnit(UnitType.COLONIST, LAND)));

        assertEquals(before, state(draft));
    }

    @Test
    void expeditionUnitsAndPassengersFollowTheGameRules() {
        final ScenarioDraft draft = island();

        final int ship = draft.addUnit(UnitType.CARAVEL, SEA);
        final int colonist = draft.addUnit(UnitType.COLONIST, LAND);
        assertEquals(Reason.UNIT_TERRAIN,
                reasonOf(() -> draft.addUnit(UnitType.COLONIST, SEA)));
        assertEquals(Reason.UNIT_TERRAIN,
                reasonOf(() -> draft.addUnit(UnitType.GALLEON, LAND)));
        assertEquals(Reason.UNIT_TYPE,
                reasonOf(() -> draft.addUnit(UnitType.BRAVE, LAND)));
        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.setTerrain(LAND, TerrainType.OCEAN)));
        assertEquals(Reason.NOT_A_SHIP, reasonOf(
                () -> draft.addPassenger(colonist, UnitType.COLONIST)));

        for (int i = 0; i < UnitType.CARAVEL.getCargoCapacity(); i++) {
            draft.addPassenger(ship, UnitType.EXPERT_FARMER);
        }
        final String full = state(draft);
        assertEquals(Reason.SHIP_FULL,
                reasonOf(() -> draft.addPassenger(ship, UnitType.COLONIST)));
        assertEquals(Reason.UNIT_TYPE,
                reasonOf(() -> draft.addPassenger(ship, UnitType.CARAVEL)));
        assertEquals(full, state(draft));
        assertEquals(UnitType.CARAVEL.getCargoCapacity(),
                draft.getPassengers(ship).size());

        final int passenger = draft.getPassengers(ship).get(0).getId();
        draft.removeUnit(passenger);
        assertEquals(UnitType.CARAVEL.getCargoCapacity() - 1,
                draft.getPassengers(ship).size());
        draft.removeUnit(ship);
        assertTrue(draft.getUnitsAt(SEA).isEmpty());
        assertEquals(1, draft.toScenario().getModel().getUnits().size(),
                "The ship leaves with its passengers");
    }

    @Test
    void everyExpeditionTypeIsAcceptedByTheGame() {
        for (final UnitType type : ScenarioDraft.EXPEDITION_TYPES) {
            final ScenarioDraft draft = island();
            draft.addUnit(type, type.isShip() ? SEA : LAND);
            assertEquals(List.of(), draft.getPlayabilityProblems(), type::name);
        }
    }

    @Test
    void nationChangeKeepsTheExpedition() {
        final ScenarioDraft draft = island();
        final int ship = draft.addUnit(UnitType.CARAVEL, SEA);
        draft.addPassenger(ship, UnitType.HARDY_PIONEER);

        draft.setNation(EuropeanNation.FRENCH);

        assertEquals(EuropeanNation.FRENCH, draft.getNation());
        final GameModelPo saved = draft.toScenario();
        assertTrue(saved.getModel().getUnits().stream()
                .allMatch(unit -> "France".equals(unit.getOwnerId())));
        assertTrue(saved.getModel().getGameManager().getPlayers().stream()
                .anyMatch(player -> "France's King".equals(player.getName())
                        && "France".equals(player.getWhosKingThisPlayerIs())));
        assertEquals(UnitType.HARDY_PIONEER,
                draft.getPassengers(ship).get(0).getType());
    }

    @Test
    void playableScenarioIsExactlyTheAuthoredOne(@TempDir final Path dir) {
        final ScenarioDraft draft = island();
        draft.setNation(EuropeanNation.ENGLISH);
        final int ship = draft.addUnit(UnitType.MERCHANTMAN, SEA);
        draft.addPassenger(ship, UnitType.COLONIST);
        draft.addPassenger(ship, UnitType.VETERAN_SOLDIER);
        draft.addUnit(UnitType.SEASONED_SCOUT, Location.of(3, 7));
        draft.placeSettlement(Location.of(5, 4), NativeNation.CHEROKEE, true);
        draft.setForest(Location.of(6, 6), true);
        final File file = dir.resolve("english.akropia").toFile();
        dao.saveScenarioToFile(file.getAbsolutePath(), draft.toScenario());

        final ScenarioDraft reopened = ScenarioDraft
                .of(dao.loadScenarioFromFile(file));
        assertTrue(reopened.getPlayabilityProblems().isEmpty(),
                () -> reopened.getPlayabilityProblems().toString());
        final GameModelPo playable = reopened.playableScenario();
        final Model model = Model.make(playable.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertEquals("England",
                playable.getModel().getGameManager().getCurrentPlayer());
        assertEquals(4, model.getAllUnits().size());
        final ModelPo started = playable.getModel();
        final UnitPo merchantman = started.getUnits().stream()
                .filter(unit -> unit.getId() == ship).findFirst().orElseThrow();
        assertEquals(List.of(UnitType.COLONIST, UnitType.VETERAN_SOLDIER),
                merchantman.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnitId() != null)
                        .map(slot -> started.getUnits().stream()
                                .filter(unit -> unit.getId()
                                        .equals(slot.getUnitId()))
                                .findFirst().orElseThrow().getType())
                        .collect(Collectors.toList()));
        assertEquals(1, started.getNativeSettlements().size());
        assertEquals(SEA, playable.getModel().getFocusedField());
        assertTrue(model.getMap().getTerrainAt(Location.of(6, 6)).isHasTrees());
        final List<UnitPo> natives = playable.getModel().getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.BRAVE)
                .collect(Collectors.toList());
        assertTrue(natives.isEmpty(), "No unauthored braves");
    }

    /** A generated world: four nations, native settlements and braves. */
    private static GameModelPo generatedScenario() {
        final GameModelPo out = new GameModelPo();
        out.setCampaignName("freePlay");
        out.setMissionName("freePlay");
        out.setModel(GeneratedWorld.create(
                new GeneratedWorldSettings(7, GeneratedClimate.TEMPERATE,
                        GeneratedLandShare.MEDIUM),
                EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
        return out;
    }

    @Test
    void openedSettlementKeepsItsStateBravesAndOtherAlarm() {
        final GameModelPo source = generatedScenario();
        final List<NativeSettlementPo> settlements = source.getModel()
                .getNativeSettlements();
        final NativeSettlementPo edited = settlements.get(0);
        final NativeSettlementPo other = settlements.get(1);
        source.getModel().getNativeLocalAlarm().stream()
                .filter(entry -> entry.getSettlementId().equals(other.getId()))
                .forEach(entry -> entry.setValue(30.0));
        final NativeNation absent = java.util.Arrays
                .stream(NativeNation.values())
                .filter(nation -> settlements.stream()
                        .noneMatch(found -> found.getNation() == nation))
                .findFirst().orElseThrow();
        final ScenarioDraft draft = ScenarioDraft.of(source);
        final int braves = (int) source.getModel().getUnits().stream().filter(
                unit -> edited.getId().equals(unit.getNativeHomeSettlementId()))
                .count();
        assertTrue(braves > 0, "The generated settlement has braves");

        draft.updateSettlement(edited.getLocation(), absent, true,
                UnitType.EXPERT_FARMER);

        final ModelPo updated = draft.toScenario().getModel();
        final NativeSettlementPo kept = updated.getNativeSettlements().stream()
                .filter(found -> found.getId().equals(edited.getId()))
                .findFirst().orElseThrow();
        assertEquals(absent, kept.getNation());
        assertEquals(edited.getTreasury(), kept.getTreasury());
        assertEquals(edited.getWantedGoods(), kept.getWantedGoods());
        assertEquals(braves, updated.getUnits().stream().filter(
                unit -> edited.getId().equals(unit.getNativeHomeSettlementId())
                        && absent.name().equals(unit.getOwnerId()))
                .count());
        assertEquals(30.0, alarmOf(updated, other.getId()));
        assertEquals(List.of(), draft.getPlayabilityProblems());

        draft.removeSettlement(edited.getLocation());

        final ModelPo removed = draft.toScenario().getModel();
        assertTrue(removed.getUnits().stream().noneMatch(unit -> edited.getId()
                .equals(unit.getNativeHomeSettlementId())));
        assertTrue(removed.getNativeLocalAlarm().stream().noneMatch(
                entry -> entry.getSettlementId().equals(edited.getId())));
        assertTrue(removed.getGameManager().getPlayers().stream()
                .noneMatch(player -> player.getNativeNation() == absent));
        assertEquals(30.0, alarmOf(removed, other.getId()));
        assertEquals(List.of(), draft.getPlayabilityProblems());
    }

    private static double alarmOf(final ModelPo model, final int settlement) {
        return model.getNativeLocalAlarm().stream()
                .filter(entry -> entry.getSettlementId() == settlement
                        && entry.getEuropean() == EuropeanNation.DUTCH)
                .findFirst().orElseThrow().getValue();
    }

    @Test
    void coloniesKeepTheirSquareAndTheNation() {
        final GameModelPo source = dao()
                .loadFromClassPath("/maps/free-play.akropia");
        final Location colony = source.getModel().getColonies().get(0)
                .getLocation();
        final ScenarioDraft draft = ScenarioDraft.of(source);
        final String before = state(draft);

        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.setTerrain(colony, TerrainType.HILL)));
        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.addUnit(UnitType.COLONIST, colony)));
        assertEquals(Reason.OCCUPIED, reasonOf(() -> draft
                .placeSettlement(colony, NativeNation.ARAWAK, true)));
        assertEquals(Reason.NATION_FIXED,
                reasonOf(() -> draft.setNation(EuropeanNation.ENGLISH)));
        assertEquals(Reason.NATION_TAKEN,
                reasonOf(() -> draft.setNation(EuropeanNation.FRENCH)));

        assertEquals(before, state(draft));
    }

    @Test
    void europeanUnitsDoNotJoinForeignUnits() {
        final GameModelPo source = generatedScenario();
        final UnitPo brave = source.getModel().getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.BRAVE).findFirst()
                .orElseThrow();
        final ScenarioDraft draft = ScenarioDraft.of(source);

        assertEquals(Reason.OCCUPIED,
                reasonOf(() -> draft.addUnit(UnitType.COLONIST,
                        brave.getPlaceMap().getLocation())));
    }

    private GameModelPoDao dao() {
        return dao;
    }

}
