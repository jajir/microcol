package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/** R-0147: clearing civilian expertise keeps the same registered person. */
class ClearSpecialtyTest {
    @TempDir
    Path directory;

    private static Stream<UnitType> civilianSkills() {
        return Stream.of(UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
                UnitType.EXPERT_FUR_TRAPPER, UnitType.EXPERT_SILVER_MINER,
                UnitType.EXPERT_LUMBERJACK, UnitType.EXPERT_ORE_MINER,
                UnitType.MASTER_CARPENTER, UnitType.SEASONED_SCOUT,
                UnitType.HARDY_PIONEER, UnitType.MASTER_SUGAR_PLANTER,
                UnitType.MASTER_COTTON_PLANTER, UnitType.MASTER_TOBACCO_PLANTER,
                UnitType.MASTER_DISTILLER, UnitType.MASTER_WEAVER,
                UnitType.MASTER_TOBACCONIST, UnitType.MASTER_FUR_TRADER,
                UnitType.MASTER_BLACKSMITH, UnitType.MASTER_GUNSMITH,
                UnitType.FIREBRAND_PREACHER, UnitType.ELDER_STATESMAN,
                UnitType.JESUIT_MISSIONARY);
    }

    private GameModelPo scenario(final ConstructionType school) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getWorkUpdates() != null)
                .forEach(unit -> unit.setWorkUpdates(0));
        data.getModel().getColonies()
                .forEach(colony -> colony.setEducationCourses(List.of()));
        if (school != null) {
            final ConstructionPo building = new ConstructionPo();
            building.setType(school);
            data.getModel().getColonies().get(0).getConstructions()
                    .add(building);
        }
        return data;
    }

    private void setType(final GameModelPo data, final int id,
            final UnitType type, final boolean veteran) {
        final var unit = data.getModel().getUnits().stream()
                .filter(candidate -> candidate.getId() == id).findFirst()
                .orElseThrow();
        unit.setType(type);
        unit.setVeteran(veteran);
    }

    private Model start(final GameModelPo data) {
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        return model;
    }

    private static Colony colony(final Model model) {
        return model.getColonyById(0).orElseThrow();
    }

    private static JsonElement snapshot(final Model model) {
        return new Gson().toJsonTree(model.save());
    }

    private static void assertRejected(final Model model, final Colony colony,
            final Unit unit, final ClearSpecialtyProblem expected) {
        final JsonElement before = snapshot(model);
        assertEquals(Optional.of(expected),
                colony.getClearSpecialtyProblem(unit));
        assertThrows(IllegalStateException.class,
                () -> colony.clearSpecialty(unit));
        assertEquals(before, snapshot(model));
    }

    @ParameterizedTest
    @MethodSource("civilianSkills")
    void bornExpertBecomesFreeColonistInPlace(final UnitType skill) {
        final GameModelPo data = scenario(null);
        setType(data, 0, skill, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final UnitFreeColonist unit = (UnitFreeColonist) model.getUnitById(0);
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.isWorkedBy(unit)).findFirst()
                .orElseThrow();
        assertEquals(skill, unit.getType());
        assertEquals(Optional.empty(), colony.getClearSpecialtyProblem(unit));

        colony.clearSpecialty(unit);

        assertSame(unit, model.getUnitById(0));
        assertEquals(0, unit.getId());
        assertSame(colony.getOwner(), unit.getOwner());
        assertEquals(UnitType.COLONIST, unit.getType());
        assertFalse(unit.hasCivilianSpecialty());
        assertFalse(unit.isVeteran());
        assertTrue(field.isWorkedBy(unit));
        assertEquals(0, unit.getWorkUpdates());
        assertTrue(unit.getWorkSkill().isEmpty());
        final UnitFreeColonist restored = (UnitFreeColonist) Model
                .make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                .getUnitById(0);
        assertEquals(UnitType.COLONIST, restored.getType());
        assertFalse(restored.isVeteran());
        restored.completeEducation(UnitType.EXPERT_FARMER);
        assertEquals(UnitType.EXPERT_FARMER, restored.getType());
    }

    @ParameterizedTest
    @MethodSource("civilianSkills")
    void learnedExpertBecomesFreeColonistInPlace(final UnitType skill) {
        final Model model = start(scenario(null));
        final Colony colony = colony(model);
        final UnitFreeColonist unit = (UnitFreeColonist) model.getUnitById(5);
        final ConstructionSlot slot = unit.getPlaceConstruction()
                .getConstructionSlot();
        unit.completeEducation(skill);
        assertEquals(skill, unit.getType());

        colony.clearSpecialty(unit);

        assertSame(unit, model.getUnitById(5));
        assertEquals(UnitType.COLONIST, unit.getType());
        assertSame(slot, unit.getPlaceConstruction().getConstructionSlot());
        assertSame(unit, slot.getUnit());
        assertEquals(Optional.of(ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY),
                colony.getClearSpecialtyProblem(unit));
    }

    @Test
    void veteranExpertKeepsRankAcrossClearAndDiskRoundTrip() throws Exception {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.MASTER_CARPENTER, true);
        final Model model = start(data);
        final Colony colony = colony(model);
        final UnitFreeColonist unit = (UnitFreeColonist) model.getUnitById(0);
        assertTrue(unit.isVeteran());

        colony.clearSpecialty(unit);

        assertEquals(UnitType.COLONIST, unit.getType());
        assertTrue(unit.isVeteran());
        final GameModelPoDao dao = new GameModelPoDao();
        final Path file = directory.resolve("cleared.microcol");
        data.setModel(model.save());
        dao.saveToFile(file.toString(), data);
        final Model reloaded = Model.make(
                dao.loadFromFile(file.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist same = (UnitFreeColonist) reloaded
                .getUnitById(0);
        assertEquals(UnitType.COLONIST, same.getType());
        assertTrue(same.isVeteran());
        assertTrue(same.getWorkSkill().isEmpty());
        assertEquals(Optional.of(ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY),
                colony(reloaded).getClearSpecialtyProblem(same));
    }

    @Test
    void equipmentAndOutsidePlacementStayIdentical() {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.EXPERT_FARMER, false);
        setType(data, 1, UnitType.MASTER_GUNSMITH, true);
        final Model model = start(data);
        final Colony colony = colony(model);
        final UnitFreeColonist tooled = (UnitFreeColonist) model.getUnitById(0);
        final UnitFreeColonist armed = (UnitFreeColonist) model.getUnitById(1);
        tooled.placeToLocation(colony.getLocation());
        armed.placeToLocation(colony.getLocation());
        tooled.setTools(100);
        armed.setHoldingGuns(true);
        armed.setMounted(true);
        final Goods tools = colony.getWarehouse().getGoods(GoodsType.TOOLS);
        final Goods muskets = colony.getWarehouse().getGoods(GoodsType.MUSKET);
        final Goods horses = colony.getWarehouse().getGoods(GoodsType.HORSE);

        colony.clearSpecialty(tooled);
        colony.clearSpecialty(armed);

        assertSame(tooled, model.getUnitById(0));
        assertSame(armed, model.getUnitById(1));
        assertEquals(UnitType.COLONIST, tooled.getType());
        assertEquals(UnitType.COLONIST, armed.getType());
        assertEquals(100, tooled.getTools());
        assertFalse(tooled.isHoldingGuns());
        assertTrue(armed.isHoldingGuns());
        assertTrue(armed.isMounted());
        assertTrue(armed.isVeteran());
        assertEquals(0, armed.getTools());
        assertTrue(tooled.isAtPlaceLocation());
        assertEquals(colony.getLocation(), tooled.getLocation());
        assertEquals(colony.getLocation(), armed.getLocation());
        assertEquals(tools, colony.getWarehouse().getGoods(GoodsType.TOOLS));
        assertEquals(muskets, colony.getWarehouse().getGoods(GoodsType.MUSKET));
        assertEquals(horses, colony.getWarehouse().getGoods(GoodsType.HORSE));
    }

    @Test
    void ordinaryAndMilitaryOnlyColonistsAreRejectedUnchanged() {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.COLONIST, true);
        setType(data, 1, UnitType.VETERAN_SOLDIER, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        assertRejected(model, colony, model.getUnitById(0),
                ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY);
        assertRejected(model, colony, model.getUnitById(1),
                ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY);
        assertRejected(model, colony, model.getUnitById(2),
                ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY);
        assertTrue(((UnitFreeColonist) model.getUnitById(0)).isVeteran());
        assertEquals(UnitType.VETERAN_SOLDIER, model.getUnitById(1).getType());
    }

    @Test
    void notOwnerTurnNullShipAndDestroyedTargetsAreRejectedUnchanged() {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.EXPERT_FARMER, false);
        setType(data, 1, UnitType.EXPERT_LUMBERJACK, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit destroyed = model.getUnitById(1);
        final Unit galleon = colony.getOwner().getAllUnits().stream()
                .filter(Unit::isShip).findFirst().orElseThrow();
        assertEquals(Optional.of(ClearSpecialtyProblem.UNAVAILABLE),
                colony.getClearSpecialtyProblem(null));
        assertRejected(model, colony, galleon,
                ClearSpecialtyProblem.UNAVAILABLE);
        destroyed.placeToLocation(colony.getLocation());
        model.destroyUnit(destroyed);
        assertRejected(model, colony, destroyed,
                ClearSpecialtyProblem.UNAVAILABLE);
        final Unit unit = model.getUnitById(0);
        model.endTurn();
        assertNotSame(colony.getOwner(), model.getCurrentPlayer());
        assertRejected(model, colony, unit, ClearSpecialtyProblem.UNAVAILABLE);
        assertEquals(UnitType.EXPERT_FARMER, unit.getType());
    }

    @Test
    void staleTargetFromAnotherModelOrAlreadyClearedIsRejected() {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.EXPERT_FARMER, false);
        final Model first = start(data);
        final Model second = Model.make(first.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        second.startGame();
        final Unit foreign = second.getUnitById(0);
        assertRejected(first, colony(first), foreign,
                ClearSpecialtyProblem.UNAVAILABLE);
        colony(first).clearSpecialty(first.getUnitById(0));
        assertRejected(first, colony(first), first.getUnitById(0),
                ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY);
        assertEquals(UnitType.EXPERT_FARMER, foreign.getType());
    }

    @Test
    void realUnitOfAnotherOwnerInTheSameModelIsRejectedUnchanged() {
        final GameModelPo data = scenario(null);
        final var colonyPo = data.getModel().getColonies().get(0);
        final var workedField = colonyPo.getColonyFields().stream()
                .filter(field -> field.getWorkerId() != null).findFirst()
                .orElseThrow();
        // An independent parse of the same scenario supplies a separate PO.
        final UnitPo foreign = scenario(null).getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        foreign.setId(900);
        foreign.setType(UnitType.EXPERT_FARMER);
        foreign.setOwnerId("Dutch's King");
        foreign.setPlaceColonyField(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(
                colonyPo.getLocation().add(workedField.getDirection()));
        place.setOrientation(Direction.west);
        foreign.setPlaceMap(place);
        data.getModel().getUnits().add(foreign);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit other = model.getUnitById(900);
        assertNotSame(colony.getOwner(), other.getOwner());
        assertRejected(model, colony, other, ClearSpecialtyProblem.UNAVAILABLE);
        final JsonElement before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> ((UnitFreeColonist) other)
                        .clearCivilianSpecialty(colony));
        assertEquals(before, snapshot(model));
        assertEquals(UnitType.EXPERT_FARMER, other.getType());
    }

    @Test
    void directUnitEntryRejectsIncompatibleTeacherAndWrongTurnAtomically() {
        final GameModelPo data = scenario(ConstructionType.SCHOOLHOUSE);
        setType(data, 4, UnitType.MASTER_CARPENTER, false);
        setType(data, 1, UnitType.EXPERT_FARMER, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final UnitFreeColonist teacher = (UnitFreeColonist) model
                .getUnitById(4);
        colony.assignEducation(teacher, model.getUnitById(0),
                UnitType.MASTER_CARPENTER);
        JsonElement before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> teacher.clearCivilianSpecialty(colony));
        assertEquals(before, snapshot(model));
        assertEquals(UnitType.MASTER_CARPENTER, teacher.getType());

        final UnitFreeColonist farmer = (UnitFreeColonist) model.getUnitById(1);
        model.endTurn();
        assertNotSame(colony.getOwner(), model.getCurrentPlayer());
        before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> farmer.clearCivilianSpecialty(colony));
        assertEquals(before, snapshot(model));
        assertEquals(UnitType.EXPERT_FARMER, farmer.getType());
    }

    @Test
    void directUnitEntryRejectsDestroyedUnit() {
        final GameModelPo data = scenario(null);
        setType(data, 1, UnitType.EXPERT_FARMER, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final UnitFreeColonist gone = (UnitFreeColonist) model.getUnitById(1);
        gone.placeToLocation(colony.getLocation());
        model.destroyUnit(gone);
        final JsonElement before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> gone.clearCivilianSpecialty(colony));
        assertEquals(before, snapshot(model));
        assertEquals(UnitType.EXPERT_FARMER, gone.getType());
    }

    @Test
    void savedAndLoadedSchoolOnlyPlanterAtFieldSurvivesContinue()
            throws Exception {
        final GameModelPo data = scenario(null);
        setType(data, 0, UnitType.MASTER_SUGAR_PLANTER, false);
        final Model model = start(data);
        final Path file = directory.resolve("planter.microcol");
        final GameModelPoDao dao = new GameModelPoDao();
        data.setModel(model.save());
        dao.saveToFile(file.toString(), data);
        final Model loaded = Model.make(
                dao.loadFromFile(file.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        loaded.startGame();
        final Unit planter = loaded.getUnitById(0);
        assertEquals(UnitType.MASTER_SUGAR_PLANTER, planter.getType());
        assertTrue(planter.isAtPlaceColonyField());
        colony(loaded).clearSpecialty(planter);
        assertEquals(UnitType.COLONIST, planter.getType());
        assertTrue(planter.isAtPlaceColonyField());
    }

    @Test
    void legalIndoorJobIsRetained() {
        final GameModelPo data = scenario(null);
        setType(data, 4, UnitType.MASTER_CARPENTER, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit unit = model.getUnitById(4);
        final ConstructionSlot slot = unit.getPlaceConstruction()
                .getConstructionSlot();
        colony.clearSpecialty(unit);
        assertEquals(UnitType.COLONIST, unit.getType());
        assertSame(slot, unit.getPlaceConstruction().getConstructionSlot());
        assertSame(unit, slot.getUnit());
    }

    @Test
    void teacherWithCourseAndSchoolSlotIsRejectedUntilEducationIsRemoved() {
        final GameModelPo data = scenario(ConstructionType.SCHOOLHOUSE);
        setType(data, 4, UnitType.MASTER_CARPENTER, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        assertRejected(model, colony, teacher,
                ClearSpecialtyProblem.TEACHER_COURSE);
        assertEquals(1, colony.getEducationCourses().size());
        assertEquals(UnitType.MASTER_CARPENTER, teacher.getType());

        // A paused course still names its teacher.
        teacher.placeToColonyStructureSlot(colony
                .getConstructionByType(ConstructionType.TOBACCONISTS_HOUSE)
                .getFirstEmptySlot().orElseThrow());
        assertTrue(colony.getEducationCourses().get(0).isPaused());
        assertRejected(model, colony, teacher,
                ClearSpecialtyProblem.TEACHER_COURSE);

        colony.cancelEducation(student);
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        colony.cancelEducation(student);
        assertRejected(model, colony, teacher,
                ClearSpecialtyProblem.SCHOOL_SLOT);

        teacher.placeToColonyStructureSlot(colony
                .getConstructionByType(ConstructionType.TOBACCONISTS_HOUSE)
                .getFirstEmptySlot().orElseThrow());
        colony.clearSpecialty(teacher);
        assertEquals(UnitType.COLONIST, teacher.getType());
    }

    @Test
    void veteranTeacherKeepsLegalVeteranCourseInCollege() {
        final GameModelPo data = scenario(ConstructionType.COLLEGE);
        setType(data, 4, UnitType.MASTER_CARPENTER, true);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        colony.assignEducation(teacher, student, UnitType.VETERAN_SOLDIER);

        colony.clearSpecialty(teacher);

        assertEquals(UnitType.COLONIST, teacher.getType());
        assertTrue(((UnitFreeColonist) teacher).isVeteran());
        assertEquals(ConstructionType.COLLEGE, teacher.getPlaceConstruction()
                .getConstructionSlot().getConstructionType());
        assertEquals(1, colony.getEducationCourses().size());
        assertFalse(colony.getEducationCourses().get(0).isPaused());
        assertEquals(UnitType.VETERAN_SOLDIER,
                colony.getEducationCourses().get(0).getSkill());
    }

    @Test
    void clearedPersonCanLearnAnotherSkillThroughSchoolAndWork() {
        final GameModelPo data = scenario(ConstructionType.SCHOOLHOUSE);
        setType(data, 0, UnitType.MASTER_CARPENTER, false);
        setType(data, 4, UnitType.EXPERT_LUMBERJACK, false);
        final Model model = start(data);
        final Colony colony = colony(model);
        final Unit person = model.getUnitById(0);
        colony.clearSpecialty(person);
        colony.assignEducation(model.getUnitById(4), person,
                UnitType.EXPERT_LUMBERJACK);
        for (int update = 0; update < 4; update++) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
            colony.startTurn();
        }
        assertSame(person, model.getUnitById(0));
        assertEquals(UnitType.EXPERT_LUMBERJACK, person.getType());

        // Work experience teaches a third skill from the same person.
        colony.clearSpecialty(person);
        for (int update = 1; update <= 20; update++) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
            colony.startTurn();
        }
        assertSame(person, model.getUnitById(0));
        assertEquals(UnitType.EXPERT_FARMER, person.getType());
    }
}
