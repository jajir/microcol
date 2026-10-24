package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.EducationCoursePo;
import org.microcol.model.turnevent.TurnEventEducationCompleted;

import com.google.gson.Gson;

class EducationTest {
    private static Stream<UnitType> schoolSkills() {
        return Stream.of(UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
                UnitType.EXPERT_FUR_TRAPPER, UnitType.EXPERT_SILVER_MINER,
                UnitType.EXPERT_LUMBERJACK, UnitType.EXPERT_ORE_MINER,
                UnitType.MASTER_CARPENTER, UnitType.SEASONED_SCOUT,
                UnitType.HARDY_PIONEER, UnitType.MASTER_SUGAR_PLANTER,
                UnitType.MASTER_COTTON_PLANTER, UnitType.MASTER_TOBACCO_PLANTER,
                UnitType.MASTER_DISTILLER, UnitType.MASTER_WEAVER,
                UnitType.MASTER_TOBACCONIST, UnitType.MASTER_FUR_TRADER,
                UnitType.MASTER_BLACKSMITH, UnitType.MASTER_GUNSMITH,
                UnitType.VETERAN_SOLDIER, UnitType.FIREBRAND_PREACHER,
                UnitType.ELDER_STATESMAN, UnitType.JESUIT_MISSIONARY);
    }

    private static ConstructionType school(final UnitType skill) {
        return EducationSkills.tier(skill) == 1 ? ConstructionType.SCHOOLHOUSE
                : EducationSkills.tier(skill) == 2 ? ConstructionType.COLLEGE
                        : ConstructionType.UNIVERSITY;
    }

    private GameModelPo scenario(final UnitType teacherType,
            final ConstructionType school) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getUnits().forEach(unit -> {
            if (unit.getType() == UnitType.COLONIST
                    || unit.getType() == UnitType.INDENTURED_SERVANT
                    || unit.getType() == UnitType.PETTY_CRIMINAL
                    || UnitType.EUROPEAN_SPECIALISTS.contains(unit.getType())
                    || UnitType.SCHOOL_ONLY_SPECIALISTS
                            .contains(unit.getType())) {
                unit.setWorkUpdates(0);
            }
        });
        data.getModel().getColonies()
                .forEach(colony -> colony.setEducationCourses(List.of()));
        final var colony = data.getModel().getColonies().get(0);
        final ConstructionPo building = new ConstructionPo();
        building.setType(school);
        colony.getConstructions().add(building);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 4)
                .findFirst().orElseThrow().setType(teacherType);
        // Arctic center: the unattended commons yield nothing, so shortages of
        // staffed inputs stay the only source of the tested output.
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(25, 14), TerrainType.ARCTIC);
        data.getModel().getMap().setTerrainType(terrain);
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @ParameterizedTest
    @MethodSource("schoolSkills")
    void eachMatrixTeacherNeedsItsOwnTier(final UnitType skill) {
        final Model model = load(scenario(skill, school(skill)));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertTrue(colony.canAssignEducation(model.getUnitById(4),
                model.getUnitById(0), skill));
        if (EducationSkills.tier(skill) > 1) {
            final Model smaller = load(scenario(skill,
                    EducationSkills.tier(skill) == 2
                            ? ConstructionType.SCHOOLHOUSE
                            : ConstructionType.COLLEGE));
            smaller.startGame();
            assertFalse(smaller.getColonyById(0).orElseThrow()
                    .canAssignEducation(smaller.getUnitById(4),
                            smaller.getUnitById(0), skill));
        }
    }

    /**
     * Card row (R-0212): the minimum school of each skill admits its teacher,
     * and the school one tier lower refuses the same skill. Schoolhouse skills
     * have no lower school to test.
     */
    @ParameterizedTest
    @CsvSource({ "EXPERT_FARMER,SCHOOLHOUSE,", "EXPERT_FISHERMAN,SCHOOLHOUSE,",
            "EXPERT_FUR_TRAPPER,SCHOOLHOUSE,",
            "EXPERT_SILVER_MINER,SCHOOLHOUSE,",
            "EXPERT_LUMBERJACK,SCHOOLHOUSE,", "EXPERT_ORE_MINER,SCHOOLHOUSE,",
            "MASTER_CARPENTER,SCHOOLHOUSE,", "SEASONED_SCOUT,SCHOOLHOUSE,",
            "HARDY_PIONEER,SCHOOLHOUSE,",
            "MASTER_SUGAR_PLANTER,COLLEGE,SCHOOLHOUSE",
            "MASTER_COTTON_PLANTER,COLLEGE,SCHOOLHOUSE",
            "MASTER_TOBACCO_PLANTER,COLLEGE,SCHOOLHOUSE",
            "MASTER_DISTILLER,COLLEGE,SCHOOLHOUSE",
            "MASTER_WEAVER,COLLEGE,SCHOOLHOUSE",
            "MASTER_TOBACCONIST,COLLEGE,SCHOOLHOUSE",
            "MASTER_FUR_TRADER,COLLEGE,SCHOOLHOUSE",
            "MASTER_BLACKSMITH,COLLEGE,SCHOOLHOUSE",
            "MASTER_GUNSMITH,COLLEGE,SCHOOLHOUSE",
            "VETERAN_SOLDIER,COLLEGE,SCHOOLHOUSE",
            "FIREBRAND_PREACHER,UNIVERSITY,COLLEGE",
            "ELDER_STATESMAN,UNIVERSITY,COLLEGE",
            "JESUIT_MISSIONARY,UNIVERSITY,COLLEGE" })
    void cardSchoolRowAdmitsTheSkillAndRefusesOneTierLower(final String name,
            final String minimum, final String lower) {
        final UnitType skill = UnitType.valueOf(name);
        final Model model = load(
                scenario(skill, ConstructionType.valueOf(minimum)));
        model.startGame();
        assertTrue(model.getColonyById(0).orElseThrow().canAssignEducation(
                model.getUnitById(4), model.getUnitById(0), skill));
        if (lower != null) {
            final Model smaller = load(
                    scenario(skill, ConstructionType.valueOf(lower)));
            smaller.startGame();
            assertFalse(smaller.getColonyById(0).orElseThrow()
                    .canAssignEducation(smaller.getUnitById(4),
                            smaller.getUnitById(0), skill));
        }
    }

    /**
     * The valid scenario of {@link #scenario} with every school building
     * removed, so the colony has no schoolhouse, college or university.
     */
    private GameModelPo scenarioWithoutSchool(final UnitType teacherType) {
        final GameModelPo data = scenario(teacherType,
                ConstructionType.SCHOOLHOUSE);
        data.getModel().getColonies().get(0).getConstructions().removeIf(
                building -> building.getType() == ConstructionType.SCHOOLHOUSE
                        || building.getType() == ConstructionType.COLLEGE
                        || building.getType() == ConstructionType.UNIVERSITY);
        return data;
    }

    /**
     * Tier 1 (R-0212): without any school the teacher cannot be assigned, the
     * command is refused and the saved state does not change.
     */
    @ParameterizedTest
    @ValueSource(strings = { "EXPERT_FARMER", "EXPERT_FISHERMAN",
            "EXPERT_FUR_TRAPPER", "EXPERT_SILVER_MINER", "EXPERT_LUMBERJACK",
            "EXPERT_ORE_MINER", "MASTER_CARPENTER", "SEASONED_SCOUT",
            "HARDY_PIONEER" })
    void schoolhouseSkillsAreRefusedWithoutAnySchool(final String name) {
        final UnitType skill = UnitType.valueOf(name);
        final Model model = load(scenarioWithoutSchool(skill));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        final String before = new Gson().toJson(model.save());
        assertFalse(colony.canAssignEducation(teacher, student, skill));
        assertThrows(IllegalArgumentException.class,
                () -> colony.assignEducation(teacher, student, skill));
        assertEquals(before, new Gson().toJson(model.save()));
        assertTrue(colony.getEducationCourses().isEmpty());
        assertEquals(UnitType.COLONIST, student.getType());
    }

    @Test
    void teacherAssignmentAndFourUpdatesPreserveStudentIdentityAndVeteran() {
        final GameModelPo data = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setVeteran(true);
        final Model model = load(data);
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        assertEquals(1, colony.getEducationOptions(teacher, student).size());
        assertEquals(UnitType.MASTER_CARPENTER,
                colony.getEducationOptions(teacher, student).get(0).getSkill());
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        assertTrue(teacher.isAtPlaceConstructionSlot());
        assertEquals(0,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        colony.startTurn();
        colony.startTurn();
        assertEquals(2,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony resumed = restored.getColonyById(0).orElseThrow();
        final Unit sameStudent = restored.getUnitById(0);
        assertEquals(UnitType.COLONIST, sameStudent.getType());
        assertEquals(2,
                resumed.getEducationCourses().get(0).getCompletedUpdates());
        resumed.startTurn();
        assertEquals(UnitType.COLONIST, sameStudent.getType());
        resumed.startTurn();
        assertSame(sameStudent, restored.getUnitById(0));
        assertEquals(UnitType.MASTER_CARPENTER, sameStudent.getType());
        assertTrue(((org.microcol.model.unit.UnitFreeColonist) sameStudent)
                .isVeteran());
        assertTrue(sameStudent.isAtPlaceColonyField());
        assertTrue(resumed.getEducationCourses().isEmpty());
        assertEquals(1, restored.getTurnEventStore()
                .getForPlayer(resumed.getOwner()).stream()
                .filter(TurnEventEducationCompleted.class::isInstance).count());
    }

    @Test
    void criminalAndServantEachNeedFourStepsBeforeExpertTraining() {
        final GameModelPo data = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(UnitType.PETTY_CRIMINAL);
        final Model model = load(data);
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        colony.assignEducation(model.getUnitById(4), student,
                UnitType.MASTER_CARPENTER);
        final UnitType[] steps = { UnitType.INDENTURED_SERVANT,
                UnitType.COLONIST, UnitType.MASTER_CARPENTER };
        for (final UnitType expected : steps) {
            final UnitType previous = student.getType();
            for (int update = 1; update <= 4; update++) {
                colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
                colony.startTurn();
                assertEquals(update == 4 ? expected : previous,
                        student.getType());
            }
            assertSame(student, model.getUnitById(0));
            assertEquals(expected, student.getType());
        }
        assertTrue(colony.getEducationCourses().isEmpty());
        assertEquals(3, model.getTurnEventStore()
                .getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventEducationCompleted.class::isInstance).count());
    }

    @Test
    void productiveFieldJobNeedsTwentyActualUpdatesAndSurvivesContinue() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        for (int update = 1; update <= 19; update++) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
            colony.startTurn();
            assertEquals(UnitType.COLONIST, student.getType());
            assertEquals(update,
                    ((org.microcol.model.unit.UnitFreeColonist) student)
                            .getWorkUpdates());
        }
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit resumed = restored.getUnitById(0);
        final Colony resumedColony = restored.getColonyById(0).orElseThrow();
        assertEquals(19, ((org.microcol.model.unit.UnitFreeColonist) resumed)
                .getWorkUpdates());
        resumedColony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        resumedColony.startTurn();
        assertSame(resumed, restored.getUnitById(0));
        assertEquals(UnitType.EXPERT_FARMER, resumed.getType());
        assertEquals(0, ((org.microcol.model.unit.UnitFreeColonist) resumed)
                .getWorkUpdates());
        assertEquals(1,
                restored.getTurnEventStore()
                        .getForPlayer(resumedColony.getOwner()).stream()
                        .filter(TurnEventEducationCompleted.class::isInstance)
                        .filter(event -> ((TurnEventEducationCompleted) event)
                                .isWorkExperience()
                                && ((TurnEventEducationCompleted) event)
                                        .getStudentId() == 0)
                        .count());
    }

    @Test
    void manufacturingShortageCreditsOnlyActualOutputToOneWorker() {
        final GameModelPo data = scenario(UnitType.COLONIST,
                ConstructionType.SCHOOLHOUSE);
        data.getModel().getColonies().get(0).getColonyFields().stream()
                .filter(field -> Integer.valueOf(1).equals(field.getWorkerId())
                        || Integer.valueOf(2).equals(field.getWorkerId()))
                .forEach(field -> field.setProducedGoodsType(GoodsType.CORN));
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CIGARS, 0));
        colony.startTurn();
        final var first = (org.microcol.model.unit.UnitFreeColonist) model
                .getUnitById(5);
        final var second = (org.microcol.model.unit.UnitFreeColonist) model
                .getUnitById(4);
        assertEquals(0, first.getWorkUpdates() + second.getWorkUpdates());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 1));
        colony.startTurn();
        assertEquals(1, first.getWorkUpdates() + second.getWorkUpdates());
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.CIGARS).getAmount());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 0));
        colony.startTurn();
        assertEquals(1, first.getWorkUpdates() + second.getWorkUpdates(),
                "The same job with no available input pauses experience");
    }

    @Test
    void teacherLeavingSchoolPausesAndSameSkillReplacementResumes() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        for (int update = 0; update < 2; update++) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
            colony.startTurn();
        }
        assertEquals(2,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        teacher.placeToColonyStructureSlot(colony
                .getConstructionByType(ConstructionType.TOBACCONISTS_HOUSE)
                .getFirstEmptySlot().orElseThrow());
        assertTrue(colony.getEducationCourses().get(0).isPaused());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.startTurn();
        assertEquals(2,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        assertFalse(colony.getEducationCourses().get(0).isPaused());
        assertEquals(2,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        for (int update = 0; update < 2; update++) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
            colony.startTurn();
        }
        assertEquals(UnitType.MASTER_CARPENTER, student.getType());
        assertTrue(colony.getEducationCourses().isEmpty());
    }

    @Test
    void rejectedAndCancelledCoursesDoNotMutateOtherResidents() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        assertFalse(colony.canAssignEducation(teacher, teacher,
                UnitType.MASTER_CARPENTER));
        assertThrows(IllegalArgumentException.class, () -> colony
                .assignEducation(teacher, teacher, UnitType.MASTER_CARPENTER));
        assertEquals(ConstructionType.TOBACCONISTS_HOUSE,
                teacher.getPlaceConstruction().getConstructionSlot()
                        .getConstructionType());
        assertTrue(colony.getEducationCourses().isEmpty());
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        assertThrows(IllegalArgumentException.class,
                () -> colony.assignEducation(teacher, model.getUnitById(1),
                        UnitType.MASTER_CARPENTER));
        assertEquals(1, colony.getEducationCourses().size());
        colony.cancelEducation(student);
        assertTrue(colony.getEducationCourses().isEmpty());
        assertEquals(UnitType.COLONIST, student.getType());
        assertTrue(colony.canAssignEducation(teacher, model.getUnitById(1),
                UnitType.MASTER_CARPENTER));
    }

    @Test
    void invalidDirectSkillsAndCrossRoleAssignmentsLeaveStateUnchanged() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit teacher = model.getUnitById(4);
        final Unit student = model.getUnitById(0);
        final var pupil = (org.microcol.model.unit.UnitFreeColonist) student;
        assertThrows(IllegalArgumentException.class,
                () -> pupil.completeEducation(UnitType.CARAVEL));
        assertThrows(IllegalArgumentException.class,
                () -> pupil.recordWork(UnitType.CARAVEL, true, false));
        assertEquals(UnitType.COLONIST, pupil.getType());
        assertEquals(0, pupil.getWorkUpdates());
        colony.assignEducation(teacher, student, UnitType.MASTER_CARPENTER);
        assertFalse(colony.canAssignEducation(student, model.getUnitById(1),
                UnitType.MASTER_CARPENTER));
        assertFalse(colony.canAssignEducation(model.getUnitById(1), teacher,
                UnitType.MASTER_CARPENTER));
        assertEquals(1, colony.getEducationCourses().size());
    }

    @Test
    void residentDepartureCancelsCourseBeforeSaving() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        colony.assignEducation(model.getUnitById(4), student,
                UnitType.MASTER_CARPENTER);
        student.placeToLocation(colony.getLocation());
        assertTrue(colony.getEducationCourses().isEmpty());
        assertTrue(Model
                .make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                .getColonyById(0).orElseThrow().getEducationCourses()
                .isEmpty());
    }

    @Test
    void studentChangingJobsInsideColonyKeepsCourseProgress() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        colony.assignEducation(model.getUnitById(4), student,
                UnitType.MASTER_CARPENTER);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.startTurn();
        student.placeToColonyStructureSlot(colony
                .getConstructionByType(ConstructionType.TOBACCONISTS_HOUSE)
                .getFirstEmptySlot().orElseThrow());
        assertEquals(1, colony.getEducationCourses().size());
        assertEquals(1,
                colony.getEducationCourses().get(0).getCompletedUpdates());
        assertFalse(colony.getEducationCourses().get(0).isPaused());
    }

    @Test
    void schoolStaffCapacityIsOneTwoThreeByFacility() {
        for (final ConstructionType facility : List.of(
                ConstructionType.SCHOOLHOUSE, ConstructionType.COLLEGE,
                ConstructionType.UNIVERSITY)) {
            final GameModelPo data = scenario(UnitType.MASTER_CARPENTER,
                    facility);
            for (final int id : new int[] { 5, 0 }) {
                data.getModel().getUnits().stream()
                        .filter(unit -> unit.getId() == id).findFirst()
                        .orElseThrow().setType(UnitType.MASTER_CARPENTER);
            }
            final Model model = load(data);
            model.startGame();
            final Colony colony = model.getColonyById(0).orElseThrow();
            final var school = colony.getConstructionByType(facility);
            final int capacity = EducationSkills.tier(facility);
            for (int index = 0; index < 3; index++) {
                final Unit teacher = model
                        .getUnitById(new int[] { 4, 5, 0 }[index]);
                final var slot = school.getSlotAt(index);
                assertEquals(index < capacity, slot.canAccept(teacher),
                        facility + " slot " + index);
                if (index < capacity) {
                    teacher.placeToColonyStructureSlot(slot);
                } else {
                    assertThrows(IllegalStateException.class,
                            () -> teacher.placeToColonyStructureSlot(slot));
                    assertFalse(teacher.isAtPlaceConstructionSlot()
                            && teacher.getPlaceConstruction()
                                    .getConstructionSlot() == slot);
                }
            }
        }
    }

    @Test
    void malformedSavedEducationAndWorkHistoryAreRejected() {
        final GameModelPo missing = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        missing.getModel().getColonies().get(0).setEducationCourses(null);
        assertThrows(IllegalArgumentException.class, () -> load(missing));

        final GameModelPo missingWork = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        missingWork.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow()
                .setWorkUpdates(null);
        assertThrows(IllegalArgumentException.class, () -> load(missingWork));

        final GameModelPo invalid = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        final EducationCoursePo course = new EducationCoursePo();
        course.setTeacherId(4);
        course.setStudentId(0);
        course.setSkill(UnitType.MASTER_CARPENTER);
        course.setCompletedUpdates(4);
        invalid.getModel().getColonies().get(0)
                .setEducationCourses(List.of(course));
        assertThrows(IllegalArgumentException.class, () -> load(invalid));

        course.setCompletedUpdates(0);
        course.setTeacherId(0);
        assertThrows(IllegalArgumentException.class, () -> load(invalid));
        course.setTeacherId(4);
        invalid.getModel().getColonies().get(0)
                .setEducationCourses(List.of(course, course));
        assertThrows(IllegalArgumentException.class, () -> load(invalid));
    }

    @Test
    void educationCommandNeedsAnActiveOwnerTurn() {
        final Model model = load(scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE));
        model.startGame();
        model.endTurn();
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> colony.assignEducation(model.getUnitById(4),
                        model.getUnitById(0), UnitType.MASTER_CARPENTER));
        assertTrue(colony.getEducationCourses().isEmpty());
    }

    @Test
    void productiveSkillInvalidatesUnfinishedCivilianCourse() {
        final GameModelPo data = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setWorkSkill(UnitType.EXPERT_FARMER);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setWorkUpdates(19);
        final Model model = load(data);
        model.startGame();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        colony.assignEducation(model.getUnitById(4), student,
                UnitType.MASTER_CARPENTER);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.startTurn();
        assertEquals(UnitType.EXPERT_FARMER, student.getType());
        assertTrue(colony.getEducationCourses().isEmpty());
        assertTrue(Model
                .make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                .getColonyById(0).orElseThrow().getEducationCourses()
                .isEmpty());
    }

    @Test
    void schoolCompletionWinsOverTwentiethWorkUpdate() {
        final GameModelPo data = scenario(UnitType.MASTER_CARPENTER,
                ConstructionType.SCHOOLHOUSE);
        final var studentPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        studentPo.setWorkSkill(UnitType.EXPERT_FARMER);
        studentPo.setWorkUpdates(19);
        final EducationCoursePo saved = new EducationCoursePo();
        saved.setTeacherId(4);
        saved.setStudentId(0);
        saved.setSkill(UnitType.MASTER_CARPENTER);
        saved.setCompletedUpdates(3);
        data.getModel().getColonies().get(0)
                .setEducationCourses(List.of(saved));
        final var teacherPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 4).findFirst().orElseThrow();
        data.getModel().getColonies().get(0).getConstructions().stream()
                .filter(building -> building
                        .getType() == ConstructionType.TOBACCONISTS_HOUSE)
                .findFirst().orElseThrow().getSlots().stream()
                .filter(slot -> Integer.valueOf(4).equals(slot.getWorkerId()))
                .findFirst().orElseThrow().setWorkerId(null);
        data.getModel().getColonies().get(0).getConstructions().stream().filter(
                building -> building.getType() == ConstructionType.SCHOOLHOUSE)
                .findFirst().orElseThrow().getSlots().get(0).setWorkerId(4);
        teacherPo.setPlaceConstructionSlot(
                new org.microcol.model.store.PlaceConstructionSlotPo());
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit student = model.getUnitById(0);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.startTurn();
        assertEquals(UnitType.MASTER_CARPENTER, student.getType());
        assertEquals(1,
                model.getTurnEventStore().getForPlayer(colony.getOwner())
                        .stream()
                        .filter(TurnEventEducationCompleted.class::isInstance)
                        .filter(event -> ((TurnEventEducationCompleted) event)
                                .getStudentId() == 0)
                        .count());
    }
}
