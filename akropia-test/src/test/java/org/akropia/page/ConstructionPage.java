package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import org.akropia.model.Colony;
import org.akropia.model.ColonyBuildingItem;
import org.akropia.model.ColonyBuildingItemProgress;
import org.akropia.model.ConstructionType;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/** Player actions and separate observations for the construction picker. */
public final class ConstructionPage extends AbstractScreen {
    private javafx.scene.Node picker;

    public ConstructionPage(final TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("colony-section-construction");
        picker = getButtonById("constructionAdd");
    }

    public void openWithKeyboard() {
        pressWithKeyboard("colony-section-construction");
        picker = getButtonById("constructionAdd");
    }

    private void pressWithKeyboard(final String id) {
        final Button button = getButtonById(id);
        revealGameContent(button);
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
    public void focusList(final String id) {
        javafx.scene.Node list = getNodeFinder().lookup("#" + id).query();
        revealGameContent(list);
        getRobot().interact(list::requestFocus);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPaperListFocus(final String id) {
        ListView<?> list = getNodeFinder().lookup("#" + id)
                .queryAs(ListView.class);
        assertTrue(list.isFocused());
        assertEquals(javafx.scene.paint.Color.web("#FFF9EC"),
                list.getBackground().getFills().get(0).getFill());
    }

    public void verifyProjectOffered(final String type,
            final boolean expected) {
        ListView<ColonyBuildingItem> list = getNodeFinder()
                .lookup("#constructionProjects").queryAs(ListView.class);
        assertEquals(expected, list.getItems().stream()
                .anyMatch(item -> item.getName().equals(type)));
    }

    public void select(final String type) {
        final ListView<ColonyBuildingItem> list = getNodeFinder()
                .lookup("#constructionProjects").queryAs(ListView.class);
        final ColonyBuildingItem item = list.getItems().stream()
                .filter(value -> value.getName().equals(type)).findFirst()
                .orElseThrow();
        revealGameContent(list);
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
        revealGameContent(button);
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
        revealGameContent(getNodeFinder().lookup("#constructionQueue").query());
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
        revealGameContent(
                getNodeFinder().lookup("#colonyUnit-" + unitId).query(),
                canvas);
        getRobot().drag("#colonyUnit-" + unitId, MouseButton.PRIMARY)
                .dropTo(canvas.localToScreen(51, 16));
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId)
                        .getPlace() instanceof org.akropia.model.PlaceConstructionSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Colony colony() {
        return getModel().getColonyAt(Location.of(24, 16)).orElseThrow();
    }

    public void verifyPickerClosed() {
        assertNull(picker.getScene(),
                "Cancel/Escape must close the construction editor");
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

    public void verifyOutsideUnits(final org.akropia.model.UnitType type,
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

    /** Verify the colony at the fixture location in the given game. */
    public void verifyOwnerAndStocks(final org.akropia.model.Model model,
            final String ownerName, final int hammers, final int tools) {
        final Colony checked = model.getColonyAt(Location.of(24, 16))
                .orElseThrow();
        assertEquals(ownerName, checked.getOwner().getName());
        assertEquals(hammers,
                checked.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(tools,
                checked.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(10000, model.getPlayerByName("Dutch").getGold());
    }

    public void verifyBuilding(final ConstructionType type, final int count) {
        assertEquals(count, colony().getConstructions().stream()
                .filter(c -> c.getType() == type).count());
    }

    public void verifyResourceProgress(final String id, final String amount,
            final String rate, final String time, final double fraction,
            final boolean required) {
        final javafx.scene.control.ProgressBar bar = getNodeFinder()
                .lookup("#" + id + "Bar")
                .queryAs(javafx.scene.control.ProgressBar.class);
        assertEquals(fraction, bar.getProgress(), 0.001);
        assertEquals(amount, getLabeledById(id + "Amount").getText());
        assertEquals(rate, getLabeledById(id + "Rate").getText());
        assertEquals(time, getLabeledById(id + "Time").getText());
        assertEquals(required ? 1 : 0.5, bar.getParent().getOpacity(), 0.001);
        assertTrue(bar.isVisible());
        for (final String suffix : List.of("Name", "Amount", "Rate", "Time")) {
            final javafx.scene.control.Labeled label = getLabeledById(
                    id + suffix);
            final javafx.scene.text.Text rendered = (javafx.scene.text.Text) label
                    .lookup(".text");
            assertEquals(label.getText(), rendered.getText(),
                    id + suffix + " must not be truncated");
        }

        assertTrue(bar.getWidth() >= 70);
        assertTrue(bar.getAccessibleText().contains(amount));
        final javafx.scene.image.ImageView icon = (javafx.scene.image.ImageView) getLabeledById(
                id + "Name").getGraphic();
        assertSame(getContext()
                .getClassFromGuice(org.akropia.gui.image.ImageCache.class)
                .getImage(id.equals("constructionWork")
                        ? "construction-hammer.png"
                        : "construction-tools.png"),
                icon.getImage());
    }

    public void verifyForecast(final String expected) {
        assertEquals(expected,
                getLabeledById("constructionForecast").getText());
    }

    public void verifyEmptyProgress() {
        verifyStatus("No construction selected.");
        for (String id : List.of("constructionWork", "constructionTools")) {
            final javafx.scene.control.ProgressBar bar = getNodeFinder()
                    .lookup("#" + id + "Bar")
                    .queryAs(javafx.scene.control.ProgressBar.class);
            assertEquals(0, bar.getProgress(), 0.001);
            assertTrue(bar.isVisible());
            assertEquals("—", getLabeledById(id + "Amount").getText());
            assertTrue(bar.getAccessibleText()
                    .contains("No construction selected."));
        }
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
            final org.akropia.model.Model previous) {
        assertNotSame(previous, getModel());
        assertTrue(previous.getColonyAt(Location.of(24, 16)).orElseThrow()
                .getColonyBuildingQueue().getBuildingQueue().isEmpty());
        assertTrue(
                colony().getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }
}
