package org.microcol.test;

import static org.microcol.test.IndependenceJourneyFixture.GOLD;
import static org.microcol.test.IndependenceJourneyFixture.NEW_AMSTERDAM;
import static org.microcol.test.IndependenceJourneyFixture.NEW_AMSTERDAM_AT;
import static org.microcol.test.IndependenceJourneyFixture.NEW_AMSTERDAM_NAME;
import static org.microcol.test.IndependenceJourneyFixture.ORE;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.CongressPage;
import org.microcol.page.CustomHousePage;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.HallOfFamePage;
import org.microcol.page.IndependenceJourneyPage;
import org.microcol.page.IndependencePage;
import org.microcol.page.InterventionPage;
import org.microcol.page.RebellionVictoryPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * One connected journey from a legal colonial start to a scored independence
 * (R-0220, UC-118), on one world built before loading by
 * {@link IndependenceJourneyFixture} from the independence scenario F03. After
 * loading, every change is a player command or an owner turn of the Dutch or of
 * the Crown; the test patches no fund, flag, army, support, relation, seed or
 * position, and it declares no victory, intervention or score.
 *
 * <p>
 * Independent ledger of the derived part, every value from the initial data and
 * the accepted rules, not read from the game. Each colony has eight residents;
 * the armed veterans and the other colonists on the colony squares are not
 * residents, so the national capacity is 4 x 1,600 = 6,400 liberty points. A
 * town hall makes 1 unattended bell plus 3 per worker; at 50 percent support
 * every worker makes one more, and five Tories stay below the Discoverer
 * threshold of 10. Bells become liberty one for one. The calendar counts years
 * until 1600 (turn 110) and then four turns a year: turns 698 to 701 are 1747.
 * </p>
 * <ul>
 * <li>Turn 698: 1,500 gold; 4 x 762 = 3,048 of 6,400 (47 percent), so no
 * declaration is offered; 40 + 12 = 52 Dutch colonists; 160 ore; the Discoverer
 * force of 4 infantry, 2 cavalry, 2 artillery and 2 men-of-war in reserve.</li>
 * <li>Turn 699, after one update of 4 x 2 x (1 + 3 x 6) = 152 bells: 3,200 of
 * 6,400, exactly 50 percent. The declaration in 1747 musters floor(4 x 1/2) = 2
 * veterans per garrisoned colony, the two lowest identities: 500, 501, 504,
 * 505, 508 and 509. Home Europe closes, and the player selects the export of
 * ore. Nothing is sold before the update.</li>
 * <li>Turn 700, after the first update since the declaration: the target of 375
 * bells starts with 4 x 2 x (1 + 3 x 7) = 176 bells; liberty is 3,376; the
 * custom house sells 160 - 100 = 60 ore at the bid of 4 and no tax, and the
 * closed home market halves the 240 to 120, so the Dutch hold 1,620 gold and
 * 100 ore. The first royal turn put all eight ground units on the two
 * men-of-war: six on the first and two on the second.</li>
 * <li>Victory: one Dutch completion first (+100 percent), 1780 - 1747 = 33
 * early points, Discoverer 50 percent, no destroyed settlement, no father, and
 * the bells after the target are the counted bells less 375. The result is the
 * only, first entry of a Hall of Fame that was empty.</li>
 * </ul>
 * <p>
 * The course of the war is not part of the literal ledger. Each royal crossing
 * lasts 2 + weather turns, and the weather is drawn from an unseeded random
 * source when the Crown dispatches its ships, so the landing turn, the
 * occupation, the bells counted until the intervention and the victory turn
 * vary from run to run. Those cells are checked by their rules, not by
 * literals; see the R-0220 evidence.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_187_F220_independence_journey_test
        extends AbstractMicroColTest {

    private static final File SOURCE = new File(
            "src/test/scenarios/F03-independence.microcol");
    private static final String PREPARED = "independence-journey-prepared.microcol";

    private static final int TARGET = 375;
    private static final int TURN_LIMIT = 40;

    /** Derived ledger; see the class comment. */
    private static final int START_TURN = 698;
    private static final int YEAR = 1747;
    private static final int START_LIBERTY = 3048;
    private static final int DECLARATION_LIBERTY = 3200;
    private static final int LIBERTY_AFTER_FIRST_UPDATE = 3376;
    private static final int CAPACITY = 6400;
    private static final int START_COLONISTS = 52;
    private static final int FIRST_UPDATE_BELLS = 176;
    private static final int GOLD_AFTER_EXPORT = 1620;
    private static final int ORE_AFTER_EXPORT = 100;
    private static final int ROSTER_MEMBERS = 10;
    private static final int INTERVENTION_UNITS = 8;

    /** Rule-fixed terms of the final result; see the class comment. */
    private static final long FATHER_POINTS = 0;
    private static final int ORDER_BONUS_PERCENT = 100;
    private static final long EARLY_POINTS = 33;
    private static final int DIFFICULTY_PERCENT = 50;

    private static final String SUPPORT_LINE = "National support for independence: 50%.";
    private static final String REASON_TEXT = "every surviving rebel colony is yours";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        HallOfFameFixture.removeRanking();
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_support_declaration_closed_europe_trade_intervention_war_and_score_with_continue()
            throws Exception {
        IndependenceJourneyFixture.prepare(getContext(), SOURCE, PREPARED);
        final IndependenceJourneyPage journey = new IndependenceJourneyPage(
                getContext());
        final IndependencePage independence = new IndependencePage(
                getContext());
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        final CustomHousePage exports = new CustomHousePage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();

        // Start of the ledger: the purse, 49 percent liberty, the people, the
        // whole Discoverer force in Europe, and no declaration offered.
        journey.verifyTurn(START_TURN, YEAR);
        journey.verifyDutchGold(GOLD);
        journey.verifyNationalSupport(START_LIBERTY, CAPACITY);
        journey.verifyDutchPersons(START_COLONISTS);
        journey.verifyColonyStock(NEW_AMSTERDAM, GoodsType.ORE, ORE);
        journey.verifyRosterMembers(ROSTER_MEMBERS);
        journey.verifyRosterCount(Family.INFANTRY, State.RESERVE, 4);
        journey.verifyRosterCount(Family.CAVALRY, State.RESERVE, 2);
        journey.verifyRosterCount(Family.ARTILLERY, State.RESERVE, 2);
        journey.verifyRosterCount(Family.MAN_OF_WAR, State.RESERVE, 2);
        independence.verifyDeclared(false);
        independence.verifyDeclareButtonVisible(false);

        // Support: one update of the town halls brings every colony to exactly
        // half of its liberty, which is the accepted threshold.
        game.nextTurnAndCloseDialogs();
        journey.verifyTurn(START_TURN + 1, YEAR);
        journey.verifyNationalSupport(DECLARATION_LIBERTY, CAPACITY);
        journey.verifyDutchGold(GOLD);
        independence.verifyDeclareButtonVisible(true);

        // Declaration: the confirmation names the support; the confirmed
        // declaration is dated, musters the veterans and fixes the four
        // colonies as the members of the rebellion.
        game.declareIndependence();
        journey.verifySupportLine(SUPPORT_LINE);
        independence.confirm();
        independence.verifyConfirmationClosed();
        final DialogMessagePage message = DialogMessagePage.of(getContext());
        message.verifyReady();
        // Free play emits one declaration message (the campaign emits two).
        message.close();
        game = GamePage.of(getContext());
        independence.verifyDeclared(true);
        independence.verifyMustered(500, 501, 504, 505, 508, 509);
        journey.verifyDeclarationYear(YEAR);
        journey.verifyRebellionMembers(0, 1, 2, 3);

        // Home Europe is closed to the declared Dutch.
        final EuropePortScreen europe = game.openEuroperPort();
        independence.verifyHomeEuropeClosed();
        game = europe.close();

        // The legal replacement trade: New Amsterdam exports its ore above the
        // reserve through the custom house. Nothing is sold before the update.
        journey.exportOreThroughCustomHouse(game, NEW_AMSTERDAM_AT,
                NEW_AMSTERDAM_NAME);
        exports.verifySelection(Set.of(GoodsType.ORE));
        journey.verifyDutchGold(GOLD);
        journey.verifyColonyStock(NEW_AMSTERDAM, GoodsType.ORE, ORE);
        congress.open();
        congress.verifyInterventionAwaiting();
        congress.close();

        // First update since the declaration: 176 bells count toward the
        // target,
        // the export sells 60 ore for half of 240, and the first royal turn
        // shipped the whole force.
        game.nextTurnAndCloseDialogs();
        journey.verifyTurn(START_TURN + 2, YEAR);
        journey.verifyInterventionBells(FIRST_UPDATE_BELLS);
        journey.verifyDutchGold(GOLD_AFTER_EXPORT);
        journey.verifyColonyStock(NEW_AMSTERDAM, GoodsType.ORE,
                ORE_AFTER_EXPORT);
        journey.verifyNationalSupport(LIBERTY_AFTER_FIRST_UPDATE, CAPACITY);
        journey.verifyDutchPersons(START_COLONISTS);
        journey.verifyRosterMembers(ROSTER_MEMBERS);
        journey.verifyRosterCount(Family.INFANTRY, State.SHIPPED, 4);
        journey.verifyRosterCount(Family.CAVALRY, State.SHIPPED, 2);
        journey.verifyRosterCount(Family.ARTILLERY, State.SHIPPED, 2);
        journey.verifyRosterCount(Family.MAN_OF_WAR, State.SHIPPED, 2);
        journey.verifyRosterCount(Family.INFANTRY, State.RESERVE, 0);
        journey.verifyInterventionDelivered(false);

        // Checkpoint A, Save and Continue: the bells, the purse, the export
        // choice and the crossing royal force all survive, and nothing is
        // replayed.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyTurn(START_TURN + 2, YEAR);
        journey.verifyInterventionBells(FIRST_UPDATE_BELLS);
        journey.verifyDutchGold(GOLD_AFTER_EXPORT);
        journey.verifyColonyStock(NEW_AMSTERDAM, GoodsType.ORE,
                ORE_AFTER_EXPORT);
        exports.verifySelection(Set.of(GoodsType.ORE));
        journey.verifyNationalSupport(LIBERTY_AFTER_FIRST_UPDATE, CAPACITY);
        journey.verifyRosterMembers(ROSTER_MEMBERS);
        journey.verifyRosterCount(Family.INFANTRY, State.SHIPPED, 4);
        journey.verifyRosterCount(Family.MAN_OF_WAR, State.SHIPPED, 2);
        journey.verifyInterventionDelivered(false);

        // Hold the receiving port and withdraw the other garrisons. Leave
        // occupied colonies to the Crown until intervention has arrived, so
        // the war cannot finish before that milestone.
        final Optional<ScreenTurnReport> arrival = journey
                .playTurnsUntilInterventionArrives(game, TURN_LIMIT);
        intervention.verifyArrivalNotice(arrival, NEW_AMSTERDAM_NAME);
        intervention.closeReport(arrival);
        intervention.verifyDeliveredForces(NEW_AMSTERDAM_AT, 2, 4, 2);
        intervention.rememberDeliveredForces();
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.verifyInterventionForcesArrived();
        congress.close();

        // Checkpoint B, Save and Continue in the war: the single delivery and
        // its units are kept, and nothing is delivered again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();
        intervention.verifyDeliveredIdentityKept(INTERVENTION_UNITS);
        intervention.verifyDeliveredForces(NEW_AMSTERDAM_AT, 2, 4, 2);

        // The Dutch fight the real royal soldiers; the Crown takes a colony,
        // and the battle that ends the war is won by the rebels.
        journey.playTurnsUntilTheGameEnds(game, TURN_LIMIT);
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyVictoryRecorded();
        journey.verifyLivingRoyalGroundAtMost(2);
        journey.verifyOccupiedAndReclaimed();
        journey.verifyCrownHoldsNoColony();
        journey.verifyInterventionArrivedOnce(INTERVENTION_UNITS);
        journey.verifyFrozenScoreLedger(FATHER_POINTS, TARGET,
                ORDER_BONUS_PERCENT, EARLY_POINTS, DIFFICULTY_PERCENT,
                GOLD_AFTER_EXPORT);

        // The result is recorded once and shown as the first place.
        victory.closeVictoryDialog(REASON_TEXT);
        hall.awaitResultDialog();
        hall.verifyResultDialogShown();
        hall.verifyRankedAt(1);
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(1);
        hall.closeResultDialog();
        journey.closeCaptureNoticeIfShown();
        campaign.verifyCampaignScreen();

        // Reopening the ranking from the menu shows the same single entry.
        campaign.back();
        hall.openFromMenu();
        hall.verifyRankingRows(1);
        hall.verifyStoredEntries(1);
        hall.closeMenuDialog();

        // Continue opens the last saved open position of the war: no second
        // victory is shown and the ranking keeps its single entry.
        new FreePlayAuditPage(getContext()).continueGame();
        victory.verifyNoVictoryDialogShown(REASON_TEXT);
        hall.openFromStatistics();
        hall.verifyRankingRows(1);
        hall.verifyStoredEntries(1);
        hall.closeStatistics();
    }

    @Test
    @Tag("local")
    void TC_02_uninterrupted_reference_path_reaches_the_same_ledger()
            throws Exception {
        IndependenceJourneyFixture.prepare(getContext(), SOURCE, PREPARED);
        final IndependenceJourneyPage journey = new IndependenceJourneyPage(
                getContext());
        final IndependencePage independence = new IndependencePage(
                getContext());
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        final CustomHousePage exports = new CustomHousePage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();

        // The same player commands as the first test, in one session.
        journey.verifyNationalSupport(START_LIBERTY, CAPACITY);
        independence.verifyDeclareButtonVisible(false);
        game.nextTurnAndCloseDialogs();
        journey.verifyNationalSupport(DECLARATION_LIBERTY, CAPACITY);
        independence.verifyDeclareButtonVisible(true);
        game.declareIndependence();
        journey.verifySupportLine(SUPPORT_LINE);
        independence.confirm();
        independence.verifyConfirmationClosed();
        final DialogMessagePage message = DialogMessagePage.of(getContext());
        message.verifyReady();
        // Free play emits one declaration message (the campaign emits two).
        message.close();
        game = GamePage.of(getContext());
        independence.verifyDeclared(true);
        independence.verifyMustered(500, 501, 504, 505, 508, 509);
        journey.verifyDeclarationYear(YEAR);
        final EuropePortScreen europe = game.openEuroperPort();
        independence.verifyHomeEuropeClosed();
        game = europe.close();
        journey.exportOreThroughCustomHouse(game, NEW_AMSTERDAM_AT,
                NEW_AMSTERDAM_NAME);
        exports.verifySelection(Set.of(GoodsType.ORE));
        game.nextTurnAndCloseDialogs();

        // The same literal state as at Checkpoint A, without a reload.
        journey.verifyTurn(START_TURN + 2, YEAR);
        journey.verifyInterventionBells(FIRST_UPDATE_BELLS);
        journey.verifyDutchGold(GOLD_AFTER_EXPORT);
        journey.verifyColonyStock(NEW_AMSTERDAM, GoodsType.ORE,
                ORE_AFTER_EXPORT);
        journey.verifyNationalSupport(LIBERTY_AFTER_FIRST_UPDATE, CAPACITY);
        journey.verifyDutchPersons(START_COLONISTS);
        journey.verifyRosterMembers(ROSTER_MEMBERS);
        journey.verifyRosterCount(Family.INFANTRY, State.SHIPPED, 4);
        journey.verifyRosterCount(Family.CAVALRY, State.SHIPPED, 2);
        journey.verifyRosterCount(Family.ARTILLERY, State.SHIPPED, 2);
        journey.verifyRosterCount(Family.MAN_OF_WAR, State.SHIPPED, 2);

        // Save without interrupting this route; both branches will share the
        // voyage weather.
        IndependenceJourneyFixture.saveCheckpoint(getContext(), game);

        // The war and the single intervention, without a reload.
        final Optional<ScreenTurnReport> arrival = journey
                .playTurnsUntilInterventionArrives(game, TURN_LIMIT);
        intervention.verifyArrivalNotice(arrival, NEW_AMSTERDAM_NAME);
        intervention.closeReport(arrival);
        intervention.verifyDeliveredForces(NEW_AMSTERDAM_AT, 2, 4, 2);
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.verifyInterventionForcesArrived();
        congress.close();

        // The earned victory, with the same rule-fixed result terms.
        journey.playTurnsUntilTheGameEnds(game, TURN_LIMIT);
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyVictoryRecorded();
        journey.verifyLivingRoyalGroundAtMost(2);
        journey.verifyOccupiedAndReclaimed();
        journey.verifyCrownHoldsNoColony();
        journey.verifyInterventionArrivedOnce(INTERVENTION_UNITS);
        journey.verifyFrozenScoreLedger(FATHER_POINTS, TARGET,
                ORDER_BONUS_PERCENT, EARLY_POINTS, DIFFICULTY_PERCENT,
                GOLD_AFTER_EXPORT);
        journey.rememberFinalLedger();
        victory.closeVictoryDialog(REASON_TEXT);
        hall.awaitResultDialog();
        hall.verifyResultDialogShown();
        hall.verifyRankedAt(1);
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(1);
        hall.closeResultDialog();
        journey.closeCaptureNoticeIfShown();
        campaign.verifyCampaignScreen();
        campaign.back();
        hall.openFromMenu();
        hall.verifyRankingRows(1);
        hall.verifyStoredEntries(1);
        hall.closeMenuDialog();
        // Replay that exact checkpoint, with Continue after the aid arrives.
        game = WelcomePage.of(getContext()).loadGame();
        final IndependenceJourneyPage replay = new IndependenceJourneyPage(
                getContext());
        final Optional<ScreenTurnReport> replayArrival = replay
                .playTurnsUntilInterventionArrives(game, TURN_LIMIT);
        intervention.verifyArrivalNotice(replayArrival, NEW_AMSTERDAM_NAME);
        intervention.closeReport(replayArrival);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        replay.playTurnsUntilTheGameEnds(game, TURN_LIMIT);
        victory.verifyVictoryDialog(REASON_TEXT);
        replay.verifyOccupiedAndReclaimed();
        replay.verifyFrozenScoreLedger(FATHER_POINTS, TARGET,
                ORDER_BONUS_PERCENT, EARLY_POINTS, DIFFICULTY_PERCENT,
                GOLD_AFTER_EXPORT);
        journey.verifyRememberedFinalLedger();
        victory.closeVictoryDialog(REASON_TEXT);
        hall.awaitResultDialog();
        hall.closeResultDialog();
        replay.closeCaptureNoticeIfShown();
        campaign.verifyCampaignScreen();
        campaign.back();
    }
}
