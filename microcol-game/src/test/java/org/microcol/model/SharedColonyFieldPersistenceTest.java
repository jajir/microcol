package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.*;

class SharedColonyFieldPersistenceTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo fixture() {
        return dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private ColonyFieldPo field(GameModelPo data, int colony,
            Direction direction) {
        return data.getModel().getColonies().get(colony).getColonyFields()
                .stream()
                .filter(f -> f.getDirection().equals(direction.getVector()))
                .findFirst().orElseThrow();
    }

    private UnitPo unit(GameModelPo data, int id) {
        return data.getModel().getUnits().stream().filter(u -> u.getId() == id)
                .findFirst().orElseThrow();
    }

    private void rejectedWithoutWriting(GameModelPo data) throws Exception {
        Path file = directory.resolve("invalid.microcol");
        dao.saveToFile(file.toString(), data);
        byte[] before = Files.readAllBytes(file);
        GameModelPo loaded = dao.loadFromFile(file.toFile());
        assertThrows(IllegalArgumentException.class, () -> load(loaded));
        assertArrayEquals(before, Files.readAllBytes(file));
        Path unchanged = directory.resolve("unchanged.microcol");
        dao.saveToFile(unchanged.toString(), loaded);
        assertArrayEquals(before, Files.readAllBytes(unchanged));
    }

    @Test
    void validAssignmentsRoundTripWithoutChangingWorkersCropsOrStocks()
            throws Exception {
        GameModelPo data = fixture();
        data.setModel(load(data).save());
        Path first = directory.resolve("first.microcol");
        dao.saveToFile(first.toString(), data);
        GameModelPo restoredData = dao.loadFromFile(first.toFile());
        Model restored = load(restoredData);
        restoredData.setModel(restored.save());
        Path second = directory.resolve("second.microcol");
        dao.saveToFile(second.toString(), restoredData);
        assertArrayEquals(Files.readAllBytes(first),
                Files.readAllBytes(second));
        ColonyField west = restored.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.west);
        assertEquals(0, west.getUnit().orElseThrow().getId());
        assertEquals(GoodsType.CORN,
                west.getProduction().orElseThrow().getType());
        assertEquals(6,
                restored.getColonyById(1).orElseThrow()
                        .getColonyFieldInDirection(Direction.south).getUnit()
                        .orElseThrow().getId());
    }

    @Test
    void sameOwnerCannotRestoreTwoWorkersOnOneWorldSquare() throws Exception {
        GameModelPo data = fixture();
        data.getModel().getColonies().get(1).setLocation(Location.of(24, 13));
        rejectedWithoutWriting(data);
    }

    @Test
    void differentOwnersCannotRestoreTwoWorkersOnOneWorldSquare()
            throws Exception {
        GameModelPo data = fixture();
        data.getModel().getColonies().get(1).setOwnerName("Dutch's King");
        unit(data, 6).setOwnerId("Dutch's King");
        assertNotNull(load(data));
        data.getModel().getColonies().get(1).setLocation(Location.of(24, 13));
        rejectedWithoutWriting(data);
    }

    @Test
    void oneWorkerCannotHaveTwoOutdoorJobs() throws Exception {
        GameModelPo data = fixture();
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(Location.of(25, 13), TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        field(data, 0, Direction.north).setWorkerId(0);
        field(data, 0, Direction.north).setProducedGoodsType(GoodsType.CORN);
        rejectedWithoutWriting(data);
    }

    @Test
    void oneWorkerCannotHaveBothOutdoorAndIndoorJobs() throws Exception {
        GameModelPo data = fixture();
        data.getModel().getColonies().get(0).getConstructions().stream()
                .filter(c -> c.getType() == ConstructionType.TOBACCONISTS_HOUSE)
                .findFirst().orElseThrow().getSlots().get(0).setWorkerId(0);
        rejectedWithoutWriting(data);
    }

    @Test
    void fieldMustReferToAnExistingWorker() throws Exception {
        GameModelPo data = fixture();
        field(data, 0, Direction.west).setWorkerId(99999);
        rejectedWithoutWriting(data);
    }

    @Test
    void fieldWorkerMustBelongToItsColony() throws Exception {
        GameModelPo data = fixture();
        unit(data, 0).setOwnerId("Dutch's King");
        rejectedWithoutWriting(data);
    }

    @Test
    void fieldReferenceCannotOverrideAMapPlace() throws Exception {
        GameModelPo data = fixture();
        unit(data, 0).setPlaceColonyField(null);
        PlaceMapPo map = new PlaceMapPo();
        map.setLocation(Location.of(25, 14));
        map.setOrientation(Direction.west);
        unit(data, 0).setPlaceMap(map);
        rejectedWithoutWriting(data);
    }

    @Test
    void outdoorPlaceRequiresAFieldReference() throws Exception {
        GameModelPo data = fixture();
        field(data, 0, Direction.west).setWorkerId(null);
        field(data, 0, Direction.west).setProducedGoodsType(null);
        rejectedWithoutWriting(data);
    }

    @Test
    void localFieldDirectionsMustBeUnique() throws Exception {
        GameModelPo data = fixture();
        ColonyFieldPo duplicate = new ColonyFieldPo();
        duplicate.setDirection(Direction.west.getVector());
        data.getModel().getColonies().get(0).getColonyFields().add(duplicate);
        rejectedWithoutWriting(data);
    }

    @ParameterizedTest
    @ValueSource(strings = { "map", "cargo", "construction", "high-seas",
            "Europe" })
    void outdoorPlaceCannotCoexistWithAnotherSavedPlace(String place)
            throws Exception {
        GameModelPo data = fixture();
        UnitPo worker = unit(data, 0);
        switch (place) {
        case "map":
            worker.setPlaceMap(new PlaceMapPo());
            break;
        case "cargo":
            worker.setPlaceCargoSlot(new PlaceCargoSlotPo());
            break;
        case "construction":
            worker.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
            break;
        case "high-seas":
            worker.setPlaceHighSeas(new PlaceHighSeasPo());
            break;
        case "Europe":
            worker.setPlaceEuropePort(new PlaceEuropePortPo());
            break;
        default:
            throw new AssertionError(place);
        }
        rejectedWithoutWriting(data);
    }

    @ParameterizedTest
    @ValueSource(strings = { "missing-direction", "center-direction",
            "empty-crop", "missing-crop", "unsupported-crop", "off-map",
            "wrong-role", "duplicate-id", "missing-id" })
    void malformedAssignmentsAreRejectedBeforeCreatingLiveObjects(String fault)
            throws Exception {
        GameModelPo data = fixture();
        ColonyFieldPo west = field(data, 0, Direction.west);
        switch (fault) {
        case "missing-direction":
            west.setDirection(null);
            break;
        case "center-direction":
            west.setDirection(Location.of(0, 0));
            break;
        case "empty-crop":
            field(data, 0, Direction.north)
                    .setProducedGoodsType(GoodsType.CORN);
            break;
        case "missing-crop":
            west.setProducedGoodsType(null);
            break;
        case "unsupported-crop":
            west.setProducedGoodsType(GoodsType.MUSKET);
            break;
        case "off-map":
            data.getModel().getColonies().get(0).setLocation(Location.of(0, 0));
            break;
        case "wrong-role":
            unit(data, 0).setType(UnitType.WAGON);
            break;
        case "duplicate-id":
            unit(data, 1).setId(0);
            break;
        case "missing-id":
            unit(data, 0).setId(null);
            break;
        default:
            throw new AssertionError(fault);
        }
        rejectedWithoutWriting(data);
    }

    @Test
    void emptyFieldRecordsMayBeOmittedAndExpertsMayWorkOutside() {
        GameModelPo data = fixture();
        data.getModel().getColonies().forEach(c -> c.getColonyFields()
                .removeIf(f -> f.getWorkerId() == null));
        unit(data, 0).setType(UnitType.MASTER_BLACKSMITH);
        Model model = load(data);
        assertEquals(UnitType.MASTER_BLACKSMITH,
                model.getUnitById(0).getType());
        assertEquals(8,
                model.getColonyById(0).orElseThrow().getColonyFields().size());
    }

    @Test
    void noColoniesIsAValidSavedState() {
        GameModelPo data = fixture();
        data.getModel().getColonies().clear();
        data.getModel().getUnits().removeIf(u -> u.getPlaceColonyField() != null
                || u.getPlaceConstructionSlot() != null);
        assertTrue(load(data).getColonies().isEmpty());
    }

    @Test
    void restoringAssignmentsDoesNotRequireTheOwnersLiveTurn() {
        final GameModelPo data = fixture();
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        data.getModel().getGameManager().setGameStarted(false);
        final Model model = load(data);
        assertFalse(model.isGameRunning());
        assertEquals(0,
                model.getColonyById(0).orElseThrow()
                        .getColonyFieldInDirection(Direction.west).getUnit()
                        .orElseThrow().getId());
    }
}
