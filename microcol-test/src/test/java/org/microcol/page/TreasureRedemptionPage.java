package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.unit.UnitTreasure;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.model.CargoSlot;
import javafx.scene.control.Label;
import org.testfx.util.WaitForAsyncUtils;

/** Player controls and explicit checks for both treasure redemption routes. */
public final class TreasureRedemptionPage extends AbstractScreen {
    public TreasureRedemptionPage(final TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("treasureOpen");
    }

    public void close() {
        clickOnButtonWithId("treasureCancel");
    }

    public void confirm() {
        clickOnButtonWithId("treasureConfirm-" + TreasureCargoPage.TRAIN_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public int gold() {
        return getModel().getPlayerByName("Dutch").getGold();
    }

    public int expectedNet(final boolean royal) {
        return (royal ? 250 : 500)
                * (100 - getModel().getPlayerByName("Dutch").getTaxRate())
                / 100;
    }

    public void verifyGold(final int expected) {
        assertEquals(expected, gold());
    }

    public void verifyTrainPresent(final boolean expected) {
        assertEquals(expected, getModel()
                .tryGetUnitById(TreasureCargoPage.TRAIN_ID).isPresent());
    }

    public void verifyPending(final int expected) {
        assertEquals(expected,
                ((UnitTreasure) getModel()
                        .getUnitById(TreasureCargoPage.TRAIN_ID))
                        .getPickupNet());
    }

    public void verifySixHoldsFree() {
        assertTrue(((UnitWithCargo) getModel()
                .getUnitById(TreasureCargoPage.GALLEON_ID)).getCargo()
                .getSlots().stream().allMatch(CargoSlot::isEmpty));
    }

    public void verifyTerms(final boolean royal, final int net) {
        final int transport = royal ? 250 : 0;
        assertEquals("Train #6000: value 500, transport " + transport + ", tax "
                + (500 - transport - net) + ". You receive " + net + " gold.",
                getNodeFinder().lookup("#treasureTerms-6000")
                        .queryAs(Label.class).getText());
    }

    public void verifyCzechTerms(final int net) {
        assertEquals(
                "Povoz č. 6000: hodnota 500, přeprava 250, daň " + (250 - net)
                        + ". Obdržíte " + net + " zlatých.",
                getNodeFinder().lookup("#treasureTerms-6000")
                        .queryAs(Label.class).getText());
    }

    public void capture(final String filename) throws java.io.IOException {
        final javafx.scene.Scene scene = getNodeFinder()
                .lookup("#treasureTerms-6000").queryAs(Label.class).getScene();
        final java.nio.file.Path output = java.nio.file.Path.of("target",
                "warehouse-qa", filename);
        java.nio.file.Files.createDirectories(output.getParent());
        final javafx.scene.image.WritableImage image = WaitForAsyncUtils
                .waitForAsyncFx(5000, () -> scene.snapshot(null));
        getRobot().robotContext().getCaptureSupport()
                .saveImage(getRobot().capture(image).getImage(), output);
    }
}
