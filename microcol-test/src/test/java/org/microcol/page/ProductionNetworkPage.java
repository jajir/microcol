package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ConstructionType;
import org.microcol.model.EducationCourse;
import org.microcol.model.GoodsType;
import org.microcol.model.UnitType;

/**
 * Independent observations of the connected production network (R-0218). The
 * existing education and construction pages read the one colony of their own
 * fixture; these read any colony by its identity and only look at the model.
 */
public final class ProductionNetworkPage extends AbstractScreen {

    public ProductionNetworkPage(final TestContext context) {
        super(context);
    }

    private Colony colony(final int colonyId) {
        return getModel().getColonyById(colonyId).orElseThrow();
    }

    public void verifyStock(final int colonyId, final GoodsType type,
            final int expected) {
        assertEquals(expected,
                colony(colonyId).getWarehouse().getGoods(type).getAmount(),
                type + " in colony " + colonyId);
    }

    /** Residents are counted, so a birth or a famine victim shows here. */
    public void verifyResidents(final int colonyId, final int expected) {
        assertEquals(expected, colony(colonyId).getUnitsInsideColony().size());
    }

    public void verifyResident(final int colonyId, final int unitId) {
        assertTrue(
                colony(colonyId).getUnitsInsideColony().stream()
                        .anyMatch(unit -> unit.getId() == unitId),
                "Unit " + unitId + " lives in colony " + colonyId);
    }

    public void verifyColonyFed(final int colonyId) {
        assertTrue(
                colony(colonyId).getWarehouse().getGoods(GoodsType.CORN)
                        .getAmount() > 0,
                "Colony " + colonyId + " still has food");
    }

    public void verifyBuilding(final int colonyId, final ConstructionType type,
            final int expected) {
        assertEquals(expected,
                colony(colonyId).getConstructions().stream()
                        .filter(construction -> construction.getType() == type)
                        .count());
    }

    public void verifyQueue(final int colonyId, final String... expected) {
        final List<ColonyBuildingItemProgress<?>> entries = colony(colonyId)
                .getColonyBuildingQueue().getBuildingQueue();
        assertEquals(List.of(expected),
                entries.stream().map(ColonyBuildingItemProgress::getName)
                        .collect(Collectors.toList()));
    }

    /** The one course of the colony, by teacher, student and progress. */
    public void verifyCourse(final int colonyId, final int teacherId,
            final int studentId, final UnitType skill, final int completed,
            final int required) {
        final List<EducationCourse> courses = colony(colonyId)
                .getEducationCourses();
        assertEquals(1, courses.size());
        assertEquals(Integer.valueOf(teacherId), courses.get(0).getTeacherId());
        assertEquals(studentId, courses.get(0).getStudentId());
        assertEquals(skill, courses.get(0).getSkill());
        assertEquals(completed, courses.get(0).getCompletedUpdates());
        assertEquals(required, courses.get(0).getRequiredUpdates());
        assertFalse(courses.get(0).isPaused());
    }

    public void verifyNoCourse(final int colonyId) {
        assertTrue(colony(colonyId).getEducationCourses().isEmpty());
    }

    public void verifyUnitType(final int unitId, final UnitType expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getType());
    }
}
