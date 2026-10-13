package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.model.Location;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.Unit;
import org.microcol.model.Colony;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Player actions and explicit observations for requirement refinement. Gap
 * observations describe the baseline, not acceptance of the missing rule.
 */
public final class FreePlayAuditPage extends AbstractScreen {
    public FreePlayAuditPage(final TestContext context) {
        super(context);
    }

    public void press(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openStatistics() {
        clickOnButtonWithId("statistics");
    }

    public GamePage closeWithEscape() {
        press(KeyCode.ESCAPE);
        return GamePage.of(getContext());
    }

    public GamePage closeReportWithClick() {
        clickOnButtonWithId("closeButtonId");
        return GamePage.of(getContext());
    }

    public void verifyReportScreen(final String expectedTitle) {
        assertTrue(getListOfNodes(".main-title").stream()
                .filter(n -> n instanceof Label).map(n -> ((Label) n).getText())
                .anyMatch(t -> t.contains(expectedTitle)), "Report title");
        assertTrue(isCssIdVisible("closeButtonId"));
    }

    public void verifyFreePlayButtonEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("buttonStartFreeGame").isDisabled());
    }

    public void verifyContinueButtonEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("buttonContinue").isDisabled());
    }

    public GamePage continueGame() {
        clickOnButtonWithId("buttonContinue");
        return GamePage.of(getContext());
    }

    public byte[] readAutosave() throws Exception {
        return Files.readAllBytes(
                getContext().getClassFromGuice(PersistingTool.class)
                        .getAutoSaveFile().toPath());
    }

    public void verifyAutosaveUnchanged(final byte[] previous)
            throws Exception {
        assertArrayEquals(previous, readAutosave(),
                "Baseline does not save on menu entry");
    }

    public void clickPierColonist(final MouseButton button) {
        getRobot().clickOn(getListOfNodes(".panel-port-pier .unit-icon").get(0),
                button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPierEquipmentBaseline(final int expectedUnits,
            final int expectedTools) {
        assertEquals(expectedUnits,
                getListOfNodes(".panel-port-pier .unit-icon").size());
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(100);
        assertTrue(PlaceEuropePier.class.isInstance(unit.getPlace()));
        assertEquals(expectedTools, unit.getTools());
        assertFalse(unit.isHoldingGuns());
        assertFalse(unit.isMounted());
        assertTrue(getListOfNodes(".context-menu").isEmpty(),
                "No equipment menu appears");
    }

    public void verifyPurchaseOffers(final List<String> expected) {
        final List<String> names = getListOfNodes(".unitPanel .name").stream()
                .map(n -> ((Label) n).getText()).collect(Collectors.toList());
        assertEquals(expected, names, "Complete purchase roster");
    }

    public void closePurchaseDialog() {
        clickOnButtonWithId("buttonOk");
    }

    public void embarkPierColonist() throws Exception {
        getRobot()
                .drag(getListOfNodes(".panel-port-pier .unit-icon").get(0),
                        MouseButton.PRIMARY)
                .dropTo(PageComponentPanelDock.of(getContext())
                        .getListOfCrates().get(0));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(100).getPlace() instanceof PlaceCargoSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void disembarkToPier() throws Exception {
        getRobot()
                .drag(PageComponentPanelDock.of(getContext()).getListOfCrates()
                        .get(0), MouseButton.PRIMARY)
                .dropTo(getListOfNodes(".panel-port-pier").get(0));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(100).getPlace() instanceof PlaceEuropePier);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPassengerPlace(final int id, final Class<?> placeType,
            final int tools, final boolean guns) {
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(id);
        assertTrue(placeType.isInstance(unit.getPlace()));
        assertEquals(tools, unit.getTools());
        assertEquals(guns, unit.isHoldingGuns());
    }

    public void verifyWagonArtworkUnsupported() {
        final Unit wagon = org.mockito.Mockito.mock(Unit.class);
        org.mockito.Mockito.when(wagon.getType())
                .thenReturn(org.microcol.model.UnitType.WAGON);
        final org.microcol.gui.image.ImageProvider provider = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        assertNull(provider.getUnitImage(org.microcol.model.UnitType.WAGON));
        final IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> provider.getUnitImage(wagon,
                        org.microcol.model.Direction.east));
        assertTrue(error.getMessage().contains("WAGON"));
    }

    public void verifyConstructionQueueUnavailable(final int population) {
        final Colony colony = getModel().getColonyAt(Location.of(24, 16)).get();
        assertEquals((long) population,
                colony.getColonyFields().stream()
                        .filter(field -> field.getUnit().isPresent()).count()
                        + colony.getConstructions().stream()
                                .flatMap(construction -> construction
                                        .getConstructionSlots().stream())
                                .filter(slot -> !slot.isEmpty()).count());
        assertFalse(getNodeFinder().lookup(".constructionPaneCarpenter").query()
                .isVisible(), "Baseline hides the queue control");
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }
}
