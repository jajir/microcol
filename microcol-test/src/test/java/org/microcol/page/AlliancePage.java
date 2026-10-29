package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Alliance proposal, termination and consequence checks through the political
 * report; direct model edits only simulate a concurrent change or prepare the
 * history a long game would have produced.
 */
public final class AlliancePage extends AbstractScreen {
    private static final File SOURCE = new File(
            "src/test/scenarios/F50-peace-negotiation.microcol");

    public AlliancePage(final TestContext context) {
        super(context);
    }

    /** Dutch-French contact fixture with the given pair states. */
    public void prepareFixture(final String dutchFrance,
            final String dutchSpain, final String franceSpain)
            throws Exception {
        prepareHistoryFixture(dutchFrance, dutchSpain, franceSpain, 0, null,
                null);
    }

    /** The same fixture after some rounds, with optional pair history. */
    public void prepareHistoryFixture(final String dutchFrance,
            final String dutchSpain, final String franceSpain,
            final int playedTurns, final Integer lastTributeRound,
            final Integer lastAggressionRound) throws Exception {
        writeFixture(dutchFrance, dutchSpain, franceSpain, playedTurns,
                lastTributeRound, lastAggressionRound, false);
    }

    /**
     * A multi-round fixture in which the Dutch own a living colony, so the game
     * continues through rounds 19, 20 and 21 without the no-colonies end.
     * Montreal and its corn worker change owner together, keeping saved food.
     */
    public void prepareLongGameFixture(final String dutchFrance,
            final String dutchSpain, final String franceSpain,
            final int playedTurns) throws Exception {
        writeFixture(dutchFrance, dutchSpain, franceSpain, playedTurns, null,
                null, true);
    }

    private void writeFixture(final String dutchFrance, final String dutchSpain,
            final String franceSpain, final int playedTurns,
            final Integer lastTributeRound, final Integer lastAggressionRound,
            final boolean dutchColony) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        if (dutchColony) {
            for (final JsonElement element : model.getAsJsonArray("colonies")) {
                final JsonObject colony = element.getAsJsonObject();
                if (colony.get("id").getAsInt() == 1) {
                    colony.addProperty("ownerName", "Dutch");
                    colony.addProperty("lastColonialAffiliation", "Dutch");
                }
            }
            for (final JsonElement element : model.getAsJsonArray("units")) {
                final JsonObject unit = element.getAsJsonObject();
                if (unit.get("id").getAsInt() == 5) {
                    unit.addProperty("ownerId", "Dutch");
                }
            }
        }
        model.getAsJsonObject("calendar").addProperty("numberOfPlayedTurns",
                playedTurns);
        for (final JsonElement element : model
                .getAsJsonArray("europeanRelations")) {
            final JsonObject row = element.getAsJsonObject();
            final String pair = row.get("firstPlayer").getAsString() + "-"
                    + row.get("secondPlayer").getAsString();
            row.addProperty("state", pair.equals("Dutch-France") ? dutchFrance
                    : pair.equals("Dutch-Spain") ? dutchSpain : franceSpain);
            if (pair.equals("Dutch-France")) {
                if (lastTributeRound != null) {
                    row.addProperty("lastTributeRound", lastTributeRound);
                }
                if (lastAggressionRound != null) {
                    row.addProperty("lastAggressionRound", lastAggressionRound);
                }
            }
        }
        final Path directory = Path.of("target", "f66");
        Files.createDirectories(directory);
        final File file = directory.resolve("european-alliance.microcol")
                .toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    public void review() {
        clickOnButtonWithId("alliance-0-1");
    }

    public void cancel() {
        clickOnButtonWithId("allianceCancel");
    }

    public void later() {
        clickOnButtonWithId("allianceLater");
    }

    public void confirm() {
        clickOnButtonWithId("allianceConfirm");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmTwice() {
        final Button button = getButtonById("allianceConfirm");
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveVisitorAway() {
        getRobot().interact(() -> getModel().getUnitById(0)
                .placeToLocation(Location.of(24, 16)));
    }

    public void setGold(final int gold) {
        getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(gold));
    }

    public void endAlliance() {
        clickOnButtonWithId("allianceEnd-0-1");
    }

    public void keepAlliance() {
        clickOnButtonWithId("allianceEndCancel");
    }

    public void confirmEnd() {
        clickOnButtonWithId("allianceEndConfirm");
    }

    public void escapeEnd() {
        escape();
    }

    /** Simulate a treaty accepted by the AI after the war. */
    public void simulateTreaty() {
        getRobot().interact(() -> getModel().acceptPeace(
                getModel().getPlayerByName("Dutch"),
                getModel().getPlayerByName("France")));
    }

    public void simulateAllianceEnded() {
        getRobot().interact(() -> getModel().terminateAlliance(
                getModel().getPlayerByName("Dutch"),
                getModel().getPlayerByName("France")));
    }

    /** Wait until the confirmed battle has spent the attacker's movement. */
    public void waitForBattle() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().tryGetUnitById(0)
                        .map(unit -> unit.getActionPoints() == 0).orElse(true));
        GamePage.of(getContext()).waitWhileMoving();
    }

    public void verifyQuote(final int price, final int gold, final int after,
            final boolean enabled) {
        final String expected = "France accepts an alliance for " + price
                + " gold.\n"
                + (enabled
                        ? "Your treasury: " + gold + " gold.\nAfter payment: "
                                + after + " gold."
                        : "Your treasury when quoted: " + gold + " gold.");
        assertEquals(expected, getLabeledById("allianceDetail").getText());
        assertEquals(!enabled, getButtonById("allianceConfirm").isDisabled());
    }

    public void verifyQuoteText(final String expected, final boolean enabled) {
        assertEquals(expected, getLabeledById("allianceDetail").getText());
        assertEquals(!enabled, getButtonById("allianceConfirm").isDisabled());
    }

    public void verifyObligationsContain(final String expected) {
        assertTrue(getLabeledById("allianceObligations").getText()
                .contains(expected));
    }

    public void verifyObligations() {
        final String text = getLabeledById("allianceObligations").getText();
        assertTrue(text.contains("cannot attack each other"));
        assertTrue(text.contains("privateers"));
        assertTrue(text.contains("without capturing them"));
        assertTrue(text.contains("workers, cargo or sight"));
        assertTrue(text.contains("go to war against anyone who attacks"));
        assertTrue(text.contains("No units, goods or map knowledge"));
        assertTrue(text.contains("5 rounds"));
    }

    public void verifyFeedback(final String expected, final boolean enabled) {
        assertTrue(getLabeledById("allianceFeedback").getText()
                .contains(expected));
        assertEquals(!enabled, getButtonById("allianceConfirm").isDisabled());
    }

    public void verifyState(final int actorGold, final int rivalGold,
            final EuropeanRelationState relation, final boolean pending) {
        final Model model = getModel();
        final Player actor = model.getPlayerByName("Dutch");
        final Player rival = model.getPlayerByName("France");
        assertEquals(actorGold, actor.getGold());
        assertEquals(rivalGold, rival.getGold());
        assertEquals(relation, model.getEuropeanRelation(actor, rival));
        assertEquals(relation, model.getEuropeanRelation(rival, actor));
        assertEquals(pending,
                model.getPendingAllianceOffer(actor, rival).isPresent());
    }

    public void verifyContactUnavailable() {
        assertTrue(getButtonById("alliance-0-1").isDisabled());
    }

    public void verifyBlocked(final int remaining) {
        final Button action = getButtonById("alliance-0-1");
        assertTrue(action.isDisabled());
        assertEquals("An alliance needs " + remaining
                + " more rounds without tribute or attacks between these nations.",
                action.getAccessibleHelp());
    }

    public void verifyProposalAvailable() {
        assertFalse(getButtonById("alliance-0-1").isDisabled());
    }

    public void verifyProposalAbsent() {
        assertFalse(isCssIdVisible("alliance-0-1"));
    }

    public void verifyStatus(final String pair, final String expected) {
        final VBox row = getNodeFinder().lookup("#relation-" + pair).query();
        assertEquals(expected, ((Label) row.getChildren().get(1)).getText());
    }

    public void verifyEndReview(final String actor, final String partner,
            final int rounds) {
        final String text = getLabeledById("allianceEndMessage").getText();
        assertTrue(text.contains(actor));
        assertTrue(text.contains(partner));
        assertTrue(text.contains(rounds + " rounds"));
        assertTrue(text.contains("Peace remains"));
        assertTrue(getButtonById("allianceEndCancel").isDefaultButton());
        assertFalse(getButtonById("allianceEndConfirm").isDefaultButton());
    }

    public void verifyEndReviewClosed() {
        assertFalse(isCssIdVisible("allianceEndConfirm"));
    }

    public void verifyWait(final int remaining) {
        final Model model = getModel();
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, france));
        assertEquals(remaining, model.getWarDeclarationWait(dutch, france));
        assertTrue(model.isAttackRestrained(dutch, france));
        verifyStatus("0-1", "Peace (war cannot be declared for " + remaining
                + " more rounds)");
        final Button declare = getButtonById("declare-0-1");
        assertTrue(declare.isDisabled());
        assertEquals(
                "War cannot be declared for " + remaining
                        + " more rounds after an alliance ended.",
                declare.getAccessibleHelp());
    }

    public void verifyNoMessage() {
        assertFalse(isCssIdVisible("buttonOk"));
    }

    /** An allied colony is only passed through; nothing about it changes. */
    public void verifyPassageOnly(final Location at) {
        final Model model = getModel();
        final org.microcol.model.Colony colony = model.getColonyById(0)
                .orElseThrow();
        final org.microcol.model.Unit visitor = model.getUnitById(0);
        assertEquals("France", colony.getOwner().getName());
        assertEquals("Dutch", visitor.getOwner().getName());
        assertEquals(at, visitor.getLocation());
        assertTrue(model.getColonies(model.getPlayerByName("Dutch")).isEmpty());
        assertFalse(colony.getUnitsOutSideColony().contains(visitor));
        assertTrue(colony.getUnitsInPort().isEmpty());
        assertTrue(colony.getUnitsInsideColony().stream()
                .allMatch(unit -> unit.getOwner().getName().equals("France")));
        assertEquals(EuropeanRelationState.ALLIANCE,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("France")));
    }

    public void verifyMovementPreviewWithoutBattle(final Location target) {
        final org.microcol.gui.screen.game.gamepanel.MoveModeSupport preview = getContext()
                .getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.MoveModeSupport.class);
        assertNotEquals("FIGHT", String.valueOf(preview.getMoveMode()));
        assertEquals(java.util.List.of(target), preview.getMoveLocations());
    }

    /** Dutch soldier 0 and French soldier 6 legally stand on one square. */
    public void verifySharedSquare(final Location at,
            final EuropeanRelationState relation) {
        final Model model = getModel();
        assertEquals(at, model.getUnitById(0).getLocation());
        assertEquals(at, model.getUnitById(6).getLocation());
        assertEquals("Dutch", model.getUnitById(0).getOwner().getName());
        assertEquals("France", model.getUnitById(6).getOwner().getName());
        assertEquals(relation,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("France")));
        assertFalse(isCssIdVisible("fightConfirm"));
        assertFalse(isCssIdVisible("buttonOk"));
    }

    public void verifyRetainedEvidence(final Location at) {
        final org.microcol.model.store.EuropeanRelationPo row = getModel()
                .save().getEuropeanRelations().stream()
                .filter(pair -> pair.getFirstPlayer().equals("Dutch")
                        && pair.getSecondPlayer().equals("France"))
                .findFirst().orElseThrow();
        assertEquals(java.util.List.of(at), row.getRetainedOverlaps());
    }

    public void verifyPairStates(final EuropeanRelationState dutchFrance,
            final EuropeanRelationState dutchSpain,
            final EuropeanRelationState franceSpain) {
        final Model model = getModel();
        assertEquals(dutchFrance,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("France")));
        assertEquals(dutchSpain,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("Spain")));
        assertEquals(franceSpain,
                model.getEuropeanRelation(model.getPlayerByName("France"),
                        model.getPlayerByName("Spain")));
    }

    public void verifyCrownRound(final int round, final int notices) {
        final Model model = getModel();
        assertEquals(round, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(notices, model
                .getTurnEventsLocalizedMessages(model.getPlayerByName("Dutch"))
                .stream()
                .filter(org.microcol.model.turnevent.TurnEventDiplomacy.class::isInstance)
                .count());
    }

    public void verifyCrownProcessed(final Integer round) {
        assertEquals(round, getModel().save().getCrownWarRound());
    }
}
