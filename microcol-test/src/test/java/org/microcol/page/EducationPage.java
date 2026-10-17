package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import org.testfx.util.WaitForAsyncUtils;

/** Exercise teaching through the visible colony controls. */
public final class EducationPage extends AbstractScreen {
    public EducationPage(TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("educationOpen");
    }

    public void close() {
        clickOnButtonWithId("educationClose");
    }

    public void chooseTeacher(int id) {
        selectUnit("educationTeacher", id);
    }

    public void chooseStudent(int id) {
        selectUnit("educationStudent", id);
    }

    private void selectUnit(String name, int id) {
        ComboBox<Unit> combo = getNodeFinder().lookup("#" + name)
                .queryComboBox();
        int index = java.util.stream.IntStream.range(0, combo.getItems().size())
                .filter(i -> combo.getItems().get(i).getId() == id).findFirst()
                .orElseThrow();
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++)
            getRobot().push(KeyCode.DOWN);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void assign() {
        clickOnButtonWithId("educationAssign");
    }

    public void cancelFirstCourse() {
        ListView<?> list = getNodeFinder().lookup("#educationCourses")
                .queryListView();
        getRobot().clickOn(list).push(KeyCode.HOME);
        clickOnButtonWithId("educationCancelCourse");
    }

    public void verifyPreview(String school, int updates, String nextType) {
        String text = getLabeledById("educationPreview").getText();
        assertTrue(text.contains(school), text);
        assertTrue(text.contains(Integer.toString(updates)), text);
        assertTrue(text.contains(nextType), text);
        assertFalse(getButtonById("educationAssign").isDisabled());
    }

    public void verifyAssignmentUnavailable() {
        assertTrue(getButtonById("educationAssign").isDisabled());
    }

    public void verifyCourses(int count, int completed) {
        var courses = getModel().getColonyById(0).orElseThrow()
                .getEducationCourses();
        assertEquals(count, courses.size());
        ListView<?> list = getNodeFinder().lookup("#educationCourses")
                .queryListView();
        assertEquals(count, list.getItems().size());
        if (count > 0) {
            assertEquals(completed, courses.get(0).getCompletedUpdates());
            assertEquals(0, courses.get(0).getStudentId());
            assertEquals(UnitType.MASTER_CARPENTER, courses.get(0).getSkill());
        }
    }

    public void verifyPaused(boolean expected) {
        var course = getModel().getColonyById(0).orElseThrow()
                .getEducationCourses().get(0);
        assertEquals(expected, course.isPaused());
        String visible = getNodeFinder().lookup(".list-cell").queryAll()
                .stream()
                .filter(node -> node instanceof javafx.scene.control.Labeled)
                .map(node -> ((javafx.scene.control.Labeled) node).getText())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" "));
        assertTrue(visible.contains(expected ? "Paused" : "Active"), visible);
    }

    public void verifyUnitType(int id, UnitType type) {
        assertEquals(type, getModel().getUnitById(id).getType());
    }

    public void verifyCourseFor(int studentId, UnitType skill, int completed) {
        var courses = getModel().getColonyById(0).orElseThrow()
                .getEducationCourses();
        assertEquals(1, courses.size());
        assertEquals(studentId, courses.get(0).getStudentId());
        assertEquals(skill, courses.get(0).getSkill());
        assertEquals(completed, courses.get(0).getCompletedUpdates());
    }

    public void verifyEquippedColonist(int id, UnitType role, boolean veteran,
            int tools, Location at) {
        var unit = (org.microcol.model.unit.UnitFreeColonist) getModel()
                .getUnitById(id);
        assertEquals(role, unit.getType());
        assertEquals(veteran, unit.isVeteran());
        assertEquals(tools, unit.getTools());
        assertFalse(unit.isHoldingGuns());
        assertFalse(unit.isMounted());
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(at, unit.getLocation());
    }

    public void verifyWarehouseTools(int expected) {
        assertEquals(expected, getModel().getColonyById(0).orElseThrow()
                .getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
    }

    public void verifyStudentKeptJob() {
        assertEquals(0,
                getModel().getColonyById(0).orElseThrow()
                        .getConstructionByType(ConstructionType.TOWN_HALL)
                        .getConstructionSlots().get(0).getUnit().getId());
    }

    public void verifySchoolVisible() {
        javafx.scene.Node node = getNodeFinder().lookup("#id_school").query();
        for (; node != null; node = node.getParent())
            assertTrue(node.isVisible());
    }

    public void moveStudentOutside(int id) {
        getRobot().interact(() -> getModel().getUnitById(id).placeToLocation(
                getModel().getColonyById(0).orElseThrow().getLocation()));
    }

    public void verifyStaleAssignmentRejected(int teacherId) {
        assertTrue(getButtonById("educationAssign").isDisabled());
        assertTrue(getLabeledById("educationFeedback").getText()
                .contains("changed"));
        assertTrue(getModel().getColonyById(0).orElseThrow()
                .getEducationCourses().isEmpty());
        assertEquals(ConstructionType.CARPENTERS_STAND,
                getModel().getUnitById(teacherId).getPlaceConstruction()
                        .getConstructionSlot().getConstructionType());
    }
}
