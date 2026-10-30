package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/** Specialist hiring actions and observations through the European offer. */
public final class ExpertPurchasePage extends AbstractScreen {
    public ExpertPurchasePage(TestContext context) {
        super(context);
    }

    public void confirm() {
        revealSpecialist("MASTER_BLACKSMITH");
        clickOnButtonWithId("buy-unit-MASTER_BLACKSMITH");
    }

    /**
     * Hire the named specialist with the mouse after scrolling its entry into
     * view.
     *
     * @param type unit type name of an entry in the offer
     */
    public void buySpecialist(final String type) {
        revealSpecialist(type);
        clickOnButtonWithId("buy-unit-" + type);
    }

    public void confirmAndDeliverDuplicateAction() {
        revealSpecialist("MASTER_BLACKSMITH");
        Button purchase = getButtonById("buy-unit-MASTER_BLACKSMITH");
        getRobot().clickOn(purchase);
        WaitForAsyncUtils.waitForFxEvents();
        // A queued second action on the closed offer must not buy another unit.
        getRobot().interact(purchase::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void revealSpecialist(String type) {
        javafx.scene.control.ScrollPane offers = getNodeFinder()
                .lookup("#buy-unit-offers")
                .queryAs(javafx.scene.control.ScrollPane.class);
        Button button = getButtonById("buy-unit-" + type);
        getRobot().interact(() -> {
            javafx.geometry.Bounds bounds = offers.getContent().sceneToLocal(
                    button.localToScene(button.getBoundsInLocal()));
            double overflow = offers.getContent().getBoundsInLocal().getHeight()
                    - offers.getViewportBounds().getHeight();
            offers.setVvalue(overflow <= 0 ? 0
                    : Math.max(0, Math.min(1, (bounds.getCenterY()
                            - offers.getViewportBounds().getHeight() / 2)
                            / overflow)));
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySpecialistOffer(String type, String name, int price,
            String action) {
        assertEquals(name, getLabeledById("buy-unit-name-" + type).getText());
        Button button = getButtonById("buy-unit-" + type);
        assertEquals(action + price, button.getText());
        assertFalse(button.isDisabled());
        javafx.scene.control.ScrollPane offers = getNodeFinder()
                .lookup("#buy-unit-offers")
                .queryAs(javafx.scene.control.ScrollPane.class);
        javafx.scene.Node viewport = offers.lookup(".viewport");
        assertTrue(viewport.localToScreen(viewport.getBoundsInLocal())
                .contains(button.localToScreen(button.getBoundsInLocal())));
    }

    public void verifyExcludedPlanters() {
        for (String type : List.of("MASTER_SUGAR_PLANTER",
                "MASTER_COTTON_PLANTER", "MASTER_TOBACCO_PLANTER")) {
            assertTrue(getNodeFinder().lookup("#buy-unit-" + type).queryAll()
                    .isEmpty());
        }
    }

    public void confirmSpecialistWithKeyboard(String type) {
        Button button = getButtonById("buy-unit-" + type);
        for (int i = 0; i < 40 && !button.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!button.isFocused()) {
            throw new IllegalStateException(
                    "Specialist must be keyboard reachable: " + type);
        }
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPurchasedSpecialist(int id, String type, int gold) {
        Unit unit = getModel().getUnitById(id);
        assertSame(UnitType.valueOf(type), unit.getType());
        assertTrue(unit.isAtEuropePier());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(gold, unit.getOwner().getGold());
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeWarning() {
        javafx.scene.Parent warning = getNodeFinder()
                .lookup("You don't have enough gold.").queryLabeled().getScene()
                .getRoot();
        Button close = getNodeFinder().from(warning).lookup("#buttonOk")
                .queryButton();
        getRobot().clickOn(close);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmWithKeyboard() {
        Button button = getButtonById("buy-unit-MASTER_BLACKSMITH");
        for (int i = 0; i < 40 && !button.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!button.isFocused()) {
            throw new IllegalStateException(
                    "The hiring button must be keyboard reachable");
        }
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeUnavailableWarning() {
        javafx.scene.Parent warning = getNodeFinder().lookup(
                "This purchase is no longer available. Reopen the offer on your turn while you have access to Europe.")
                .queryLabeled().getScene().getRoot();
        getRobot().clickOn(getNodeFinder().from(warning).lookup("#buttonOk")
                .queryButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyUnavailableWarning() {
        assertTrue(getNodeFinder().lookup(
                "This purchase is no longer available. Reopen the offer on your turn while you have access to Europe.")
                .queryLabeled().isVisible());
    }

    public void verifyReplacedGameUntouched(org.microcol.model.Model previous,
            int expectedGold) {
        assertNotSame(previous, getModel());
        assertEquals(expectedGold, previous.getPlayerByName("Dutch").getGold());
        assertEquals(expectedGold,
                getModel().getPlayerByName("Dutch").getGold());
        assertTrue(previous.getPlayerByName("Dutch").getAllUnits().stream()
                .noneMatch(
                        unit -> unit.getType() == UnitType.MASTER_BLACKSMITH));
        assertTrue(getModel().getPlayerByName("Dutch").getAllUnits().stream()
                .noneMatch(
                        unit -> unit.getType() == UnitType.MASTER_BLACKSMITH));
    }

    public void verifyCzechOffer() {
        assertEquals("Najmout odborníka za 2200",
                getButtonById("buy-unit-MASTER_BLACKSMITH").getText());
        assertFalse(getButtonById("buy-unit-MASTER_BLACKSMITH").isDisabled());
        assertTrue(getNodeFinder().lookup("mistr kovář").queryLabeled()
                .isVisible());
    }

    public void verifyOffer(boolean available, String reason) {
        assertEquals("Hire specialist for 2200",
                getButtonById("buy-unit-MASTER_BLACKSMITH").getText());
        assertEquals(!available,
                getButtonById("buy-unit-MASTER_BLACKSMITH").isDisabled());
        assertEquals(reason,
                getLabeledById("buy-unit-reason-MASTER_BLACKSMITH").getText());
        if (!available) {
            assertTrue(getLabeledById("buy-unit-reason-MASTER_BLACKSMITH")
                    .isVisible());
        }
    }

    public void verifyTreasuryAndPierExperts(int gold,
            List<Integer> expertIds) {
        assertEquals(gold, getModel().getPlayerByName("Dutch").getGold());
        List<Unit> experts = getModel().getPlayerByName("Dutch").getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.MASTER_BLACKSMITH)
                .collect(Collectors.toList());
        assertEquals(expertIds, experts.stream().map(Unit::getId).sorted()
                .collect(Collectors.toList()));
        for (Unit expert : experts) {
            assertTrue(expert.isAtEuropePier());
            assertSame(getModel().getPlayerByName("Dutch"), expert.getOwner());
            assertEquals(2.0F, expert.getType()
                    .getProductionModifier(org.microcol.model.GoodsType.TOOLS));
        }
        UnitFreeColonist ordinary = (UnitFreeColonist) getModel()
                .getUnitById(100);
        UnitFreeColonist pioneer = (UnitFreeColonist) getModel()
                .getUnitById(101);
        assertSame(UnitType.COLONIST, ordinary.getType());
        assertSame(UnitType.COLONIST, pioneer.getType());
        assertTrue(ordinary.isAtEuropePier());
        assertTrue(pioneer.isAtEuropePier());
        assertEquals(0, ordinary.getTools());
        assertEquals(37, pioneer.getTools());
    }

    public void assignToSmithy(int unitId) throws Exception {
        getRobot()
                .drag("#colonyUnit-" + unitId,
                        javafx.scene.input.MouseButton.PRIMARY)
                .dropTo("#id_blacksmith");
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId)
                        .getPlace() instanceof org.microcol.model.PlaceConstructionSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverSmithy() {
        getRobot().moveTo(".outside").moveTo("#id_blacksmith");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void leaveSmithy(int unitId) throws Exception {
        org.microcol.model.PlaceConstructionSlot place = (org.microcol.model.PlaceConstructionSlot) getModel()
                .getUnitById(unitId).getPlace();
        int slot = place.getColony().getConstructions().stream()
                .filter(construction -> construction.getConstructionSlots()
                        .contains(place.getConstructionSlot()))
                .findFirst().orElseThrow().getConstructionSlots()
                .indexOf(place.getConstructionSlot());
        javafx.scene.Parent building = getNodeFinder().lookup("#id_blacksmith")
                .queryAs(javafx.scene.Parent.class);
        javafx.scene.canvas.Canvas canvas = (javafx.scene.canvas.Canvas) building
                .getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.canvas.Canvas)
                .findFirst().orElseThrow();
        // Use the non-overlapping part of the worker icon in each painted slot.
        javafx.geometry.Point2D source = canvas.localToScreen(22 + 46 * slot,
                104);
        getRobot().drag(source, javafx.scene.input.MouseButton.PRIMARY)
                .dropTo(".outside");
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).isAtPlaceLocation());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyWorkplaceDescription(String expected) {
        assertEquals(expected, getLabeledById("statusBarLabel").getText());
    }

    public void verifyExpertState(int id, int gold, boolean guns,
            Class<?> placeType) {
        UnitFreeColonist expert = (UnitFreeColonist) getModel().getUnitById(id);
        assertSame(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertSame(getModel().getPlayerByName("Dutch"), expert.getOwner());
        assertEquals(gold, expert.getOwner().getGold());
        assertEquals(guns, expert.isHoldingGuns());
        assertFalse(expert.isMounted());
        assertEquals(0, expert.getTools());
        assertTrue(placeType.isInstance(expert.getPlace()));
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(unit -> unit.getId() == id).count());
        assertEquals(2.0F, expert.getType()
                .getProductionModifier(org.microcol.model.GoodsType.TOOLS));
    }

    /**
     * Any hired specialist keeps its identity, type, owner and single charge.
     *
     * @param id        unit identifier of the hired specialist
     * @param type      expected specialist type
     * @param gold      treasury of the buyer after the purchase
     * @param placeType expected place class of the specialist
     */
    public void verifySpecialistState(final int id, final UnitType type,
            final int gold, final Class<?> placeType) {
        Unit unit = getModel().getUnitById(id);
        assertSame(type, unit.getType());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(gold, unit.getOwner().getGold());
        assertTrue(placeType.isInstance(unit.getPlace()));
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(candidate -> candidate.getId() == id).count());
    }

    public void verifySmithyWorker(int id, int tools, int ore) {
        org.microcol.model.PlaceConstructionSlot place = (org.microcol.model.PlaceConstructionSlot) getModel()
                .getUnitById(id).getPlace();
        org.microcol.model.Colony colony = place.getColony();
        assertEquals(org.microcol.model.Location.of(24, 16),
                colony.getLocation());
        assertSame(getModel().getUnitById(id),
                place.getConstructionSlot().getUnit());
        assertEquals(2.0F, place.getConstructionSlot().getProductionModifier());
        assertEquals(tools, colony.getWarehouse()
                .getGoods(org.microcol.model.GoodsType.TOOLS).getAmount());
        assertEquals(ore, colony.getWarehouse()
                .getGoods(org.microcol.model.GoodsType.ORE).getAmount());
        assertEquals(50, colony.getWarehouse()
                .getGoods(org.microcol.model.GoodsType.MUSKET).getAmount());
    }

    public void verifyInsufficientFundsWarning() {
        assertTrue(getNodeFinder().lookup("You don't have enough gold.")
                .queryLabeled().isVisible());
    }
}
