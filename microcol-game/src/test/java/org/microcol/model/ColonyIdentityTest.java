package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.turnevent.TurnEventColonyWasDestroyed;
import org.microcol.model.turnevent.TurnEventProvider;

class ColonyIdentityTest {

    @TempDir
    Path directory;

    private GameModelPo fixture() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final ModelPo data) {
        return Model.make(data, org.microcol.model.campaign.MissionImpl
                .getPredefinedGameOverEvaluators());
    }

    @Test
    void renamePreservesIdentityWorkersOwnerAndJson() {
        final GameModelPo data = fixture();
        final Model model = load(data.getModel());
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Player owner = colony.getOwner();
        final List<Unit> workers = colony.getUnitsInsideColony();
        model.renameColony(owner, colony, "\u2003 Žluťoučký přístav 🌊 \u00a0");
        assertEquals("Žluťoučký přístav 🌊", colony.getName());
        assertEquals(0, colony.getId());
        assertSame(owner, colony.getOwner());
        assertEquals(workers, colony.getUnitsInsideColony());
        data.setModel(model.save());
        final GameModelPoDao dao = new GameModelPoDao();
        final Path saved = directory.resolve("colony.microcol");
        dao.saveToFile(saved.toString(), data);
        final Model restored = load(
                dao.loadFromFile(saved.toFile()).getModel());
        final Colony restoredColony = restored.getColonyById(0).orElseThrow();
        assertEquals(colony.getName(), restoredColony.getName());
        assertEquals(owner.getName(), restoredColony.getOwner().getName());
        assertEquals(
                workers.stream().map(Unit::getId)
                        .collect(java.util.stream.Collectors.toList()),
                restoredColony.getUnitsInsideColony().stream().map(Unit::getId)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void duplicatesAndWrongOwnerDoNotChangeName() {
        final Model model = load(fixture().getModel());
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> model.renameColony(colony.getOwner(), colony, " dElFt "));
        assertThrows(IllegalArgumentException.class,
                () -> model.renameColony(org.mockito.Mockito.mock(Player.class),
                        colony, "Another"));
        assertEquals("New Amsterdam", colony.getName());
        model.renameColony(colony.getOwner(), colony, " New Amsterdam ");
        assertEquals("New Amsterdam", colony.getName());
    }

    @Test
    void destroyedIdsRemainReservedAfterReloadAndReportsRetainIdentity() {
        final Model model = load(fixture().getModel());
        final Colony colony = model.getColonyById(2).orElseThrow();
        final TurnEventColonyWasDestroyed report = (TurnEventColonyWasDestroyed) TurnEventProvider
                .getColonyWasDestroyed(colony.getOwner(), colony);
        model.renameColony(colony.getOwner(), colony, "Renamed");
        assertEquals(2, report.getColonyId());
        assertEquals("Haarlem", report.getColonyName());
        final TurnEventColonyWasDestroyed restoredReport = TurnEventColonyWasDestroyed
                .tryLoad(report.save());
        assertEquals(2, restoredReport.getColonyId());
        assertEquals("Haarlem", restoredReport.getColonyName());
        colony.getUnitsInsideColony()
                .forEach(unit -> unit.placeToLocation(Location.of(19, 11)));
        if (model.getColonyById(2).isPresent()) {
            model.destroyColony(colony);
        }
        assertThrows(IllegalArgumentException.class,
                () -> model.renameColony(colony.getOwner(), colony, "Ghost"));
        final Model restored = load(model.save());
        assertEquals(3, restored.save().getNextColonyId());
        assertTrue(restored.getColonyById(2).isEmpty());
        final Unit founder = restored.getUnitsAt(Location.of(19, 11)).stream()
                .filter(restored::canUnitBuildColony).findFirst().orElseThrow();
        final Colony created = restored.buildColony(founder.getOwner(), founder,
                "New harbour");
        assertEquals(3, created.getId());
        assertEquals(4, restored.save().getNextColonyId());
        assertThrows(IllegalArgumentException.class, () -> restored
                .buildColony(founder.getOwner(), founder, "Again"));
        assertEquals(4, restored.save().getNextColonyId());
    }

    @Test
    void missingDuplicateNegativeIdsAndInvalidCounterAreRejected() {
        final ModelPo missing = fixture().getModel();
        missing.setNextColonyId(null);
        assertThrows(IllegalArgumentException.class, () -> load(missing));
        final ModelPo duplicate = fixture().getModel();
        duplicate.getColonies().get(1).setId(0);
        assertThrows(IllegalArgumentException.class, () -> load(duplicate));
        final ModelPo negative = fixture().getModel();
        negative.getColonies().get(0).setId(-1);
        assertThrows(IllegalArgumentException.class, () -> load(negative));
        final ModelPo absent = fixture().getModel();
        absent.getColonies().get(0).setId(null);
        assertThrows(IllegalArgumentException.class, () -> load(absent));
        final ModelPo counter = fixture().getModel();
        counter.setNextColonyId(2);
        assertThrows(IllegalArgumentException.class, () -> load(counter));
    }

    @Test
    void firstColonyStartsAtZeroAndExhaustionCannotWrap() {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        // This case needs an empty world, independent of bundled opponents.
        data.getColonies().clear();
        data.getUnits().removeIf(unit -> !"Dutch".equals(unit.getOwnerId()));
        data.setNextColonyId(0);
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer("Dutch");
        data.getUnits().get(0).setAvailableMoves(1);
        final Model model = load(data);
        final Unit founder = model.getUnitById(0);
        founder.placeToLocation(Location.of(19, 11));
        assertThrows(IllegalArgumentException.class,
                () -> model.buildColony(founder.getOwner(), founder, " "));
        assertEquals(0, model.save().getNextColonyId());
        assertEquals(0, model.buildColony(founder.getOwner(), founder, "First")
                .getId());
        assertEquals(1, model.save().getNextColonyId());

        final ModelPo exhaustedData = fixture().getModel();
        exhaustedData.setNextColonyId(Integer.MAX_VALUE);
        final Model exhausted = load(exhaustedData);
        final Unit otherFounder = exhausted.getUnitById(0);
        otherFounder.placeToLocation(Location.of(19, 11));
        assertThrows(IllegalStateException.class,
                () -> exhausted.buildColony(otherFounder.getOwner(),
                        otherFounder, "Too many"));
        assertEquals(Integer.MAX_VALUE, exhausted.save().getNextColonyId());
        assertTrue(otherFounder.isAtPlaceLocation());
    }

    @Test
    void captureKeepsIdAndOldOwnerCannotRename() {
        final Model model = load(fixture().getModel());
        final Colony colony = model.getColonyById(2).orElseThrow();
        final Player oldOwner = colony.getOwner();
        final Unit capturing = model.getUnitById(0);
        final Player king = model.getPlayerByName("Dutch's King");
        capturing.placeToLocation(colony.getLocation());
        capturing.takeOver(king);
        colony.captureColony(capturing);
        assertSame(king, colony.getOwner());
        assertEquals(2, colony.getId());
        assertEquals("Haarlem", colony.getName());
        assertThrows(IllegalArgumentException.class,
                () -> model.renameColony(oldOwner, colony, "Lost"));
        assertThrows(IllegalArgumentException.class,
                () -> model.renameColony(king, colony, "Not your turn"));
        assertEquals(2,
                load(model.save()).getColonyById(2).orElseThrow().getId());
    }
}
