package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.microcol.model.ColonyField;
import org.microcol.model.Location;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Native land claim dialog and the saved land state behind it. Actions press
 * the real dialog buttons; every observation is read from the dialog or from
 * the model and no method here changes the game.
 */
public final class NativeLandPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    public NativeLandPage(final TestContext context) {
        super(context);
    }

    private Player dutch() {
        return getModel().getPlayerByName(DUTCH);
    }

    /** Pay the quoted price and continue with the action. */
    public void pay() {
        clickOnButtonWithId("nativeLandPay");
    }

    /** Knowingly continue without paying. */
    public void continueUnpaid() {
        clickOnButtonWithId("nativeLandProceed");
    }

    /** Cancel: the action must not start. */
    public void cancel() {
        clickOnButtonWithId("nativeLandCancel");
    }

    public void verifyDialogShows(final String expectedClaimant,
            final String expectedPrice, final String expectedConsequence) {
        assertEquals(expectedClaimant,
                getLabeledById("nativeLandClaimant").getText());
        assertEquals(expectedPrice,
                getLabeledById("nativeLandPrice").getText());
        assertEquals(expectedConsequence,
                getLabeledById("nativeLandConsequence").getText());
    }

    public void verifyPayEnabled(final boolean expectedEnabled) {
        final Button pay = getButtonById("nativeLandPay");
        assertEquals(!expectedEnabled, pay.isDisabled(),
                "Unexpected state of the pay button");
        assertTrue(getButtonById("nativeLandProceed").isVisible(),
                "Continuing unpaid must stay available");
        assertTrue(getButtonById("nativeLandCancel").isVisible(),
                "Canceling must stay available");
    }

    public void verifyDialogMessage(final String expectedMessage) {
        final Label message = (Label) getLabeledById("nativeLandMessage");
        assertEquals(expectedMessage, message.getText());
    }

    public void verifyDialogClosed() {
        assertTrue(getNodeFinder().lookup("#nativeLandClaimant").tryQuery()
                .isEmpty(), "The native land dialog must be closed");
    }

    public void verifyDutchGold(final int expectedGold) {
        assertEquals(expectedGold, dutch().getGold(), "Unexpected Dutch gold");
    }

    /** Treasury of the settlement the Dutch learned at the given square. */
    public void verifyClaimantTreasury(final Location settlement,
            final int expectedTreasury) {
        assertEquals(expectedTreasury,
                getModel().getNativeSettlementKnownTo(dutch(), settlement)
                        .orElseThrow().getTreasury(),
                "Unexpected treasury at " + settlement);
    }

    public void verifyLandStatus(final Location square,
            final NativeLandStatus expectedStatus) {
        assertEquals(
                expectedStatus, getModel().quoteNativeLand(dutch(), square)
                        .orElseThrow().getStatus(),
                "Unexpected land status at " + square);
    }

    public void verifyLandPrice(final Location square,
            final int expectedPrice) {
        assertEquals(
                expectedPrice, getModel().quoteNativeLand(dutch(), square)
                        .orElseThrow().getPrice(),
                "Unexpected land price at " + square);
    }

    /** A claim whose owner the Dutch never learned reveals nothing at all. */
    public void verifyNoLandInformation(final Location square) {
        assertTrue(getModel().quoteNativeLand(dutch(), square).isEmpty(),
                "Unknown claimant must stay hidden at " + square);
    }

    public void verifyPaidSquares(final int expectedCount) {
        assertEquals(expectedCount,
                getModel().save().getNativeLandRights().size(),
                "Unexpected number of paid squares");
    }

    public void verifyTakenSquares(final int expectedCount) {
        assertEquals(expectedCount,
                getModel().save().getNativeLandTakings().size(),
                "Unexpected number of unpaid takings");
    }

    /** The founding question says when a chosen payment is made. */
    public void verifyFoundingNote(final String expectedNote) {
        assertEquals(expectedNote, getLabeledById("nativeLandNote").getText());
    }

    /** The naming dialog repeats the founding land and the decision. */
    public void verifyColonyNameLand(final String expectedText) {
        assertEquals(expectedText, getLabeledById("colonyNameLand").getText());
    }

    public void verifyColonyNameDialogClosed() {
        assertTrue(getNodeFinder().lookup("#colonyNameDialog").tryQuery()
                .isEmpty(), "No colony may be named after canceling");
    }

    public void verifyColonyAt(final Location square,
            final boolean expectedColony) {
        assertEquals(expectedColony, getModel().getColonyAt(square).isPresent(),
                "Unexpected colony state at " + square);
    }

    /** The colony field the given worker works, whichever colony owns it. */
    public void verifyWorkerOnSquare(final int unitId,
            final Location expectedSquare) {
        assertEquals(expectedSquare,
                getModel().getColonies().stream()
                        .flatMap(colony -> colony.getColonyFields().stream())
                        .filter(field -> field.getUnit().map(Unit::getId)
                                .orElse(-1) == unitId)
                        .map(ColonyField::getLocation).findFirst().orElse(null),
                "Unexpected field of worker " + unitId);
    }
}
