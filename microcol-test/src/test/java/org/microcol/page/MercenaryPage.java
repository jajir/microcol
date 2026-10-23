package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.MercenaryDelivery;
import org.microcol.model.MilitaryRole;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ComboBox;

/**
 * The paid veteran mercenary offer as the player meets it (R-0169): the dialog
 * that a real end of turn opens, the owned port choice, the answers, the review
 * action in the relations report, and the observable outcome. Reading the model
 * changes nothing.
 */
public final class MercenaryPage extends AbstractScreen {

    private static final String DIALOG = "mercenaryDialog";

    private int startGold;

    private Set<Integer> startUnits = Set.of();

    private List<Integer> rememberedVeterans = List.of();

    public MercenaryPage(final TestContext context) {
        super(context);
    }

    private Player dutch() {
        return getModel().getPlayerByName("Dutch");
    }

    private Set<Integer> unitIds() {
        return getModel().getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    /** The paid veterans that did not exist when the start was remembered. */
    private List<UnitFreeColonist> paidVeterans() {
        return getModel().getAllUnits().stream()
                .filter(unit -> !startUnits.contains(unit.getId()))
                .filter(unit -> unit.getType() == UnitType.VETERAN_SOLDIER)
                .map(unit -> (UnitFreeColonist) unit)
                .sorted(java.util.Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Colony> portChoice() {
        return (ComboBox<Colony>) getNodeFinder().lookup("#mercenaryPort")
                .queryAs(ComboBox.class);
    }

    // --- player gestures ---------------------------------------------------

    /** Remember the treasury and the units before the answering turn. */
    public void rememberStart() {
        startGold = dutch().getGold();
        startUnits = unitIds();
    }

    /** Wait for the offer window that the finished update opened. */
    public void waitForOffer() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#" + DIALOG).tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Choose the owned port that receives the soldiers. */
    public void choosePort(final String colonyName) {
        final ComboBox<Colony> port = portChoice();
        getRobot().interact(() -> port.getItems().stream()
                .filter(colony -> colony.getName().equals(colonyName))
                .findFirst().ifPresent(port.getSelectionModel()::select));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void accept() {
        clickOnButtonWithId("mercenaryAccept");
    }

    public void decline() {
        clickOnButtonWithId("mercenaryDecline");
    }

    /** Close the window without answering; the offer stays for this turn. */
    public void decideLater() {
        clickOnButtonWithId("mercenaryLater");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the window after an answer; its button then reads "Close". */
    public void close() {
        clickOnButtonWithId("mercenaryLater");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Use the relations report to open the unanswered offer again. */
    public void reviewFromRelations() throws Exception {
        new FreePlayAuditPage(getContext()).openStatistics();
        getRobot().clickOn("#relationsTab");
        WaitForAsyncUtils.waitForFxEvents();
        clickOnButtonWithId("mercenaryResume");
        waitForOffer();
    }

    /** Leave the statistics screen for the map. */
    public void leaveStatistics() {
        clickOnButtonWithId("closeButtonId");
    }

    public void closeReport(final Optional<ScreenTurnReport> report) {
        report.ifPresent(ScreenTurnReport::close);
    }

    /** Remember the identities of the paid soldiers for a later comparison. */
    public void rememberVeterans() {
        rememberedVeterans = paidVeterans().stream().map(Unit::getId)
                .collect(Collectors.toList());
    }

    // --- explicit observations --------------------------------------------

    public void verifyDialogShown(final boolean expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, isCssIdVisible(DIALOG));
    }

    /** The window states the issuer, the full price and the treasury. */
    public void verifyOfferShown(final String issuer, final int price,
            final int treasury) {
        assertEquals(issuer + " offers 2 veteran soldiers for " + price
                + " gold.\nYour treasury: " + treasury
                + " gold.\nAfter payment: " + (treasury - price) + " gold.",
                getLabeledById("mercenaryDetail").getText());
    }

    public void verifyPortOffered(final String colonyName) {
        assertTrue(
                portChoice().getItems().stream().anyMatch(
                        colony -> colony.getName().equals(colonyName)),
                "The port " + colonyName + " must be offered");
    }

    public void verifySelectedPort(final String colonyName) {
        assertEquals(colonyName,
                portChoice().getSelectionModel().getSelectedItem().getName());
    }

    /** The blockade hint is shown exactly for an obstructed selected port. */
    public void verifyBlockedHint(final boolean blocked) {
        assertEquals(blocked
                ? "This port is blocked: the paid soldiers will wait there until the sea route opens."
                : "", getLabeledById("mercenaryHint").getText());
    }

    public void verifyAcceptEnabled(final boolean enabled) {
        assertEquals(!enabled, getButtonById("mercenaryAccept").isDisabled());
    }

    public void verifyDeliveredFeedback() {
        assertEquals(
                "Payment made. The veteran soldiers have landed at the port.",
                getLabeledById("mercenaryFeedback").getText());
        assertTrue(getButtonById("mercenaryAccept").isDisabled());
        assertTrue(getButtonById("mercenaryDecline").isDisabled());
    }

    public void verifyPendingFeedback() {
        assertEquals(
                "Payment made. The veteran soldiers wait at the port until the sea route opens.",
                getLabeledById("mercenaryFeedback").getText());
        assertTrue(getButtonById("mercenaryAccept").isDisabled());
        assertTrue(getButtonById("mercenaryDecline").isDisabled());
    }

    /** The model holds no unanswered offer for the Dutch. */
    public void verifyNoOffer() {
        assertTrue(getModel().getPendingMercenaryOffer(dutch()).isEmpty());
        assertFalse(dutch().getMercenaryOffer().isPresent());
    }

    /** The unanswered offer of the turn is still the quoted one. */
    public void verifyOfferPending(final String issuer, final int price) {
        final var offer = getModel().getPendingMercenaryOffer(dutch());
        assertTrue(offer.isPresent(), "The offer must still be pending");
        assertEquals(issuer, offer.get().getIssuer().getName());
        assertEquals(price, offer.get().getPrice());
    }

    /** The treasury differs from the remembered start by the given amount. */
    public void verifyGoldChange(final int delta) {
        assertEquals(startGold + delta, dutch().getGold());
    }

    /** No paid soldier exists and no group was recorded. */
    public void verifyNothingPaid() {
        assertEquals(List.of(), paidVeterans());
        assertEquals(0, dutch().getMercenaryDeliveries().size());
        assertEquals(startGold, dutch().getGold());
    }

    /**
     * Exactly the given number of paid veteran soldiers stand on the port: each
     * armed, veteran, still ordinary colonial soldiers and not Continentals of
     * the foreign intervention.
     */
    public void verifyVeteransAt(final Location port, final int count) {
        final List<UnitFreeColonist> veterans = paidVeterans();
        assertEquals(count, veterans.size());
        for (final UnitFreeColonist soldier : veterans) {
            assertEquals(dutch(), soldier.getOwner());
            assertTrue(soldier.isVeteran());
            assertTrue(soldier.isHoldingGuns());
            assertEquals(MilitaryRole.COLONIAL, soldier.getMilitaryRole());
            assertFalse(soldier.isInterventionDelivered());
            assertEquals(port, soldier.getLocation());
        }
    }

    /** Exactly the given number of paid veteran soldiers exist. */
    public void verifyPaidSoldierCount(final int count) {
        assertEquals(count, paidVeterans().size());
    }

    /** The paid groups on record and how many of them were placed. */
    public void verifyPaidGroups(final int total, final int delivered) {
        final List<MercenaryDelivery> groups = dutch().getMercenaryDeliveries();
        assertEquals(total, groups.size());
        assertEquals(delivered,
                groups.stream().filter(MercenaryDelivery::isDelivered).count());
    }

    /** The same soldiers, with the same identities, are still the paid ones. */
    public void verifyVeteransKept() {
        assertEquals(rememberedVeterans, paidVeterans().stream()
                .map(Unit::getId).collect(Collectors.toList()));
    }

    /** The update that placed a waiting group reports where it landed. */
    public void verifyArrivalNotice(final Optional<ScreenTurnReport> report,
            final String colonyName) {
        assertTrue(report.isPresent(),
                "The arrival update must show a turn report");
        report.get().verifyThatAtLeastOneEventMessageContains(
                "The paid veteran soldiers have landed at " + colonyName + ".");
    }

    /** A later update has no second arrival notice. */
    public void verifyNoArrivalNotice(final Optional<ScreenTurnReport> report) {
        report.ifPresent(shown -> shown
                .verifyEventMessageCount("The paid veteran soldiers", 0));
    }
}
