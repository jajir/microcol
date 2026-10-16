package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/** Player actions and separate observations for the construction picker. */
public final class ConstructionPage extends AbstractScreen {
    private javafx.stage.Window picker;

    public ConstructionPage(final TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("constructionOpen");
        picker = getButtonById("constructionAdd").getScene().getWindow();
    }

    public void openWithKeyboard() {
        pressWithKeyboard("constructionOpen");
        picker = getButtonById("constructionAdd").getScene().getWindow();
    }

    private void pressWithKeyboard(final String id) {
        final Button button = getButtonById(id);
        for (int i = 0; i < 40 && !button.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!button.isFocused()) {
            throw new IllegalStateException(
                    "Construction control is not keyboard reachable: " + id);
        }
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    public void select(final String type) {
        final ListView<ColonyBuildingItem> list = getNodeFinder()
                .lookup("#constructionProjects").queryAs(ListView.class);
        final ColonyBuildingItem item = list.getItems().stream()
                .filter(value -> value.getName().equals(type)).findFirst()
                .orElseThrow();
        getRobot().interact(() -> list.scrollTo(item));
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().clickOn("#project-" + type);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("constructionAdd");
    }

    public void confirmWithKeyboard() {
        pressWithKeyboard("constructionAdd");
    }

    public void confirmTwice() {
        final Button button = getButtonById("constructionAdd");
        getRobot().clickOn(button);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("constructionCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void selectQueue(final int index) {
        getRobot().clickOn("#constructionQueue");
        getRobot().push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveUp() {
        clickOnButtonWithId("constructionUp");
    }

    public void moveDown() {
        clickOnButtonWithId("constructionDown");
    }

    public void remove() {
        clickOnButtonWithId("constructionRemove");
    }

    public void assignCarpenter(final int unitId) throws Exception {
        javafx.scene.Parent building = getNodeFinder().lookup("#id_carpenter")
                .queryAs(javafx.scene.Parent.class);
        javafx.scene.canvas.Canvas canvas = (javafx.scene.canvas.Canvas) building
                .getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.canvas.Canvas)
                .findFirst().orElseThrow();
        getRobot().drag("#colonyUnit-" + unitId, MouseButton.PRIMARY)
                .dropTo(canvas.localToScreen(41, 82));
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId)
                        .getPlace() instanceof org.microcol.model.PlaceConstructionSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Colony colony() {
        return getModel().getColonyAt(Location.of(24, 16)).orElseThrow();
    }

    public void verifyPickerClosed() {
        assertFalse(picker.isShowing(),
                "Cancel/Escape must close the actual modal window");
    }

    public void verifyOffer(final boolean available, final String reason,
            final int hammers, final int tools) {
        assertEquals(!available, getButtonById("constructionAdd").isDisabled());
        final String detail = getLabeledById("constructionDetail").getText();
        assertTrue(detail.contains(reason), detail);
        assertTrue(
                detail.contains(
                        "Cost: " + hammers + " hammers · " + tools + " tools"),
                detail);
    }

    public void verifyQueue(final List<String> expectedNames,
            final List<Integer> expectedIds, final int expectedHammerPool) {
        final List<ColonyBuildingItemProgress<?>> entries = colony()
                .getColonyBuildingQueue().getBuildingQueue();
        assertEquals(expectedNames,
                entries.stream().map(ColonyBuildingItemProgress::getName)
                        .collect(Collectors.toList()));
        assertEquals(expectedIds,
                entries.stream().map(ColonyBuildingItemProgress::getId)
                        .collect(Collectors.toList()));
        assertEquals(expectedHammerPool, colony().getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
    }

    public void verifyOutsideUnits(final org.microcol.model.UnitType type,
            final long expected) {
        assertEquals(expected, getModel().getUnitsAt(colony().getLocation())
                .stream().filter(unit -> unit.getType() == type).count());
    }

    public void verifySharedHammerDisplay(final int expected) {
        assertEquals("Shared work: " + expected + " hammers.",
                getLabeledById("constructionHammerPool").getText());
    }

    public void verifyStocksAndPopulation(final int hammers, final int tools,
            final int population) {
        assertEquals(hammers, colony().getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(tools,
                colony().getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(population, colony().getUnitsInsideColony().size());
        assertEquals(10000, colony().getOwner().getGold());
    }

    public void verifyBuilding(final ConstructionType type, final int count) {
        assertEquals(count, colony().getConstructions().stream()
                .filter(c -> c.getType() == type).count());
    }

    public void verifyStatus(final String expected) {
        assertEquals(expected, getLabeledById("constructionStatus").getText());
    }

    public void verifyChangedOffer() {
        assertEquals(
                "The colony has changed. Select a project again to review its current requirements.",
                getLabeledById("constructionFeedback").getText());
        assertTrue(getButtonById("constructionAdd").isDisabled());
    }

    public void verifyCzechOffer() {
        assertEquals("Potvrdit a přidat",
                getButtonById("constructionAdd").getText());
        assertFalse(getButtonById("constructionAdd").isDisabled());
        assertTrue(getLabeledById("constructionDetail").getText()
                .contains("Cena: 52 kladiv · 0 nástrojů"));
    }

    public void verifyReplacedModelUnchanged(
            final org.microcol.model.Model previous) {
        assertNotSame(previous, getModel());
        assertTrue(previous.getColonyAt(Location.of(24, 16)).orElseThrow()
                .getColonyBuildingQueue().getBuildingQueue().isEmpty());
        assertTrue(
                colony().getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }
}
