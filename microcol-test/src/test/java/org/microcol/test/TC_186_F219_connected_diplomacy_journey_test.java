package org.microcol.test;

import static org.microcol.test.DiplomacyJourneyFixture.AZTEC_BRAVE;
import static org.microcol.test.DiplomacyJourneyFixture.AZTEC_CAPITAL;
import static org.microcol.test.DiplomacyJourneyFixture.BRAVE_AT;
import static org.microcol.test.DiplomacyJourneyFixture.DUTCH_GOLD;
import static org.microcol.test.DiplomacyJourneyFixture.FORT_AT;
import static org.microcol.test.DiplomacyJourneyFixture.FORT_ORLEANS;
import static org.microcol.test.DiplomacyJourneyFixture.FORT_WORKER;
import static org.microcol.test.DiplomacyJourneyFixture.FRENCH_GALLEON;
import static org.microcol.test.DiplomacyJourneyFixture.FRENCH_GOLD;
import static org.microcol.test.DiplomacyJourneyFixture.FRENCH_TARGET;
import static org.microcol.test.DiplomacyJourneyFixture.FRENCH_TARGET_AT;
import static org.microcol.test.DiplomacyJourneyFixture.FRIGATE;
import static org.microcol.test.DiplomacyJourneyFixture.FRIGATE_AT;
import static org.microcol.test.DiplomacyJourneyFixture.FRIGATE_SUGAR;
import static org.microcol.test.DiplomacyJourneyFixture.GALLEON_AT;
import static org.microcol.test.DiplomacyJourneyFixture.HIDDEN_QUEBEC;
import static org.microcol.test.DiplomacyJourneyFixture.HIDDEN_QUEBEC_AT;
import static org.microcol.test.DiplomacyJourneyFixture.HOSTILE_ANGER;
import static org.microcol.test.DiplomacyJourneyFixture.MISSIONARY_AT;
import static org.microcol.test.DiplomacyJourneyFixture.OBSERVER;
import static org.microcol.test.DiplomacyJourneyFixture.OBSERVER_AT;
import static org.microcol.test.DiplomacyJourneyFixture.PIONEER_AT;
import static org.microcol.test.DiplomacyJourneyFixture.PRIVATEER_AT;
import static org.microcol.test.DiplomacyJourneyFixture.PRIVATEER_SHIP;
import static org.microcol.test.DiplomacyJourneyFixture.RAID_SOLDIER;
import static org.microcol.test.DiplomacyJourneyFixture.RAID_SOLDIER_AT;
import static org.microcol.test.DiplomacyJourneyFixture.ROUTE;
import static org.microcol.test.DiplomacyJourneyFixture.SCOUT;
import static org.microcol.test.DiplomacyJourneyFixture.SCOUT_AT;
import static org.microcol.test.DiplomacyJourneyFixture.SOLDIER;
import static org.microcol.test.DiplomacyJourneyFixture.SOLDIER_AT;
import static org.microcol.test.DiplomacyJourneyFixture.UNEASY_ANGER;
import static org.microcol.test.DiplomacyJourneyFixture.WAGON;
import static org.microcol.test.DiplomacyJourneyFixture.WAGON_AT;
import static org.microcol.test.DiplomacyJourneyFixture.WAGON_CORN;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.GoodsType;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.NativeNation;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.AttackPage;
import org.microcol.page.ColonyDefensePage;
import org.microcol.page.DiplomacyJourneyPage;
import org.microcol.page.EuropeanRelationsPage;
import org.microcol.page.FogPage;
import org.microcol.page.ForeignColonyTradePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeIncitementPage;
import org.microcol.page.NativeLandPage;
import org.microcol.page.NativeMarketPage;
import org.microcol.page.NativeRaidPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.NativeTeachingPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.NavalBattlePage;
import org.microcol.page.PeacePage;
import org.microcol.page.PlowPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.microcol.test.DiplomacyJourneyFixture.Variant;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * One connected diplomacy journey on one world (R-0219, UC-117), built before
 * loading by {@link DiplomacyJourneyFixture} from the native turns scenario.
 * After loading, every change is a player command or an owner turn of the
 * natives or of France; the test patches no relation, owner, square or seed.
 *
 * <p>
 * Independent milestone ledger, every value derived from the initial data and
 * the accepted rules, not read from the game. The saved Europe market bids 2
 * for corn and 3 for sugar to every nation. The Aztecs do not want corn, so
 * they pay max(1, 2 * 3 / 4) = 1 a piece: 50 corn bring 50 gold from their
 * treasury. A completed trade at a capital lowers its local alarm and the
 * tribe's anger by 2 * 2, so the anger toward the Dutch falls from 30 to 26 and
 * the incitement costs ceil(50 * (100 + 26) / (10 + 0)) = 630. A foreign colony
 * pays its own Europe bid: 2 sugar bring 6 gold. Peace costs ceil(200 * max(1,
 * French armed strength / Dutch)) = 200, since France has no armed land unit. A
 * capture moves no gold, and no owner turn of this world pays or charges
 * anyone: there is no tax, no Europe trade and no French purchase. Sugar is
 * neither produced nor consumed in Hidden Quebec.
 * </p>
 * <ul>
 * <li>Turn 0, peace: an attack on the French galleon is offered only as a
 * declaration of war, which is cancelled. Land: Dutch 2,000 to 1,800, Aztec
 * treasury 2,000 to 2,200, paid squares 8 to 9. The scout makes contact and
 * starts learning Expert Farmer for 4 owner updates. Native sale: Dutch 1,850,
 * treasury 2,150, anger 26. French sale of 2 sugar: Dutch 1,856, France 994,
 * Quebec sugar 2, frigate sugar 2. Incitement 630: Dutch 1,226, order 20, anger
 * toward France 100. Checkpoint A keeps all of it.</li>
 * <li>Turn 1: the brave is 2 squares from the French colonist, order 19, course
 * 1 of 4. Turn 2: beside it, order 18, course 2; the attack on Fort Orleans is
 * offered only as a declaration of war and cancelled.</li>
 * <li>Turn 3: the brave has destroyed the French colonist, order 17, course 3.
 * War is declared; the attack on the galleon is now ordinary. The soldier takes
 * Fort Orleans with its worker and its stock of 50 muskets and 50 tools; France
 * keeps Hidden Quebec and its route still names the lost stop. Purses
 * unchanged. Checkpoint B keeps all of it.</li>
 * <li>Turn 4: the skill is granted once, order 16. Peace for 200: Dutch 1,026,
 * France 1,194; the attack on the galleon again needs a declaration. Resumed
 * trade of 2 sugar: Dutch 1,032, France 1,188, Quebec sugar 4, frigate sugar 0.
 * Checkpoint C keeps all of it, and the uninterrupted reference reaches the
 * same literal end state.</li>
 * </ul>
 * The route operations on the lost stop are exercised by
 * {@code TradeRouteNavigationTest.capturedStopIsLostForTheFormerOwnerRouteWithoutMovingTheWagon}.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_186_F219_connected_diplomacy_journey_test
        extends AbstractMicroColTest {

    private static final File SOURCE = new File(
            "src/test/scenarios/F106-native-turns.microcol");
    private static final String PREPARED = "diplomacy-journey-prepared.microcol";

    private static final int PEACE_PRICE = 200;
    private static final int SOLD_SUGAR = 2;

    /** Ledger milestones of the Dutch purse; see the class comment. */
    private static final int DUTCH_AFTER_LAND = 1800;
    private static final int DUTCH_AFTER_NATIVE_SALE = 1850;
    private static final int DUTCH_AFTER_FIRST_TRADE = 1856;
    private static final int DUTCH_AFTER_INCITEMENT = 1226;
    private static final int DUTCH_AFTER_PEACE = 1026;
    private static final int DUTCH_FINAL = 1032;

    /** Ledger milestones of the French purse. */
    private static final int FRENCH_AFTER_FIRST_TRADE = 994;
    private static final int FRENCH_AFTER_PEACE = 1194;
    private static final int FRENCH_FINAL = 1188;

    /** Aztec capital treasury after the land payment and after the sale. */
    private static final int TREASURY_AFTER_LAND = 2200;
    private static final int TREASURY_AFTER_SALE = 2150;

    /**
     * Aztec anger toward the Dutch after the completed trade at the capital.
     */
    private static final double ANGER_AFTER_SALE = 26;

    /** Stock of Fort Orleans that changes hands with the colony. */
    private static final int FORT_MUSKETS = 50;
    private static final int FORT_TOOLS = 50;

    private static final String CLAIMANT = "This square is claimed by: Aztec city, capital.";
    private static final String CONSEQUENCE = "If you use it without paying, the settlement's alarm toward you rises by 5 once at the first use and by 2 each turn while the square stays in use.";
    private static final String CONFIRMATION_EN = "A Free Colonist learns Expert Farmer after 4 owner updates. Meanwhile the colonist stays with the natives and cannot move, work or fight.";
    private static final String REPORT_EN = "A colonist learned Expert Farmer from the natives.";
    private static final String GIVE_TWO_SUGAR = "You give: 2 Sugar cane.";
    private static final String RECEIVE_NOTHING = "You receive no goods back.";
    private static final String INCITEMENT_TERMS = "Price: 630 gold, paid once whether the attempt succeeds or fails.";
    private static final String INCITEMENT_RESULT = "Paid 630 gold; you now have 1226. The tribe has turned against France and will pursue it for up to 20 native turns, ending sooner if its anger fades.";
    private static final String DECLARES_WAR = "declares war";
    private static final String FRANCE = "France";
    private static final String DUTCH = "Dutch";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    private static String progressEn(final int updates) {
        return "Learning Expert Farmer here: " + updates
                + " of 4 owner updates.";
    }

    @Test
    @Tag("local")
    void TC_01_contact_land_teaching_trade_incitement_war_capture_peace_and_resumed_trade_with_continue()
            throws Exception {
        DiplomacyJourneyFixture.prepare(getContext(), SOURCE, PREPARED,
                Variant.JOURNEY);
        final FogPage fog = new FogPage(getContext());
        final PlowPage plow = new PlowPage(getContext());
        final NativeLandPage land = new NativeLandPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final ForeignColonyTradePage trade = new ForeignColonyTradePage(
                getContext());
        final EuropeanRelationsPage europeans = new EuropeanRelationsPage(
                getContext());
        final PeacePage peace = new PeacePage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final ColonyDefensePage defense = new ColonyDefensePage(getContext());
        final DiplomacyJourneyPage journey = new DiplomacyJourneyPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        fog.centerFrontier();
        trade.adopt(HIDDEN_QUEBEC_AT, FRIGATE_AT);

        // Start of the ledger: peace, the purses, the plow square unpaid, no
        // contact, no pursuit, and the French route and colonies in place.
        journey.verifyDutchGold(DUTCH_GOLD);
        journey.verifyFrenchGold(FRENCH_GOLD);
        journey.verifyTreatyKept();
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyLandStatus(PIONEER_AT, NativeLandStatus.UNPAID);
        land.verifyPaidSquares(8);
        market.verifyNotIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        incite.verifyNoOrder();
        incite.verifyTribalAnger(DUTCH, UNEASY_ANGER);
        incite.verifyTribalAnger(FRANCE, 100);
        journey.verifyColonyOwner(HIDDEN_QUEBEC, FRANCE);
        journey.verifyColonyOwner(FORT_ORLEANS, FRANCE);
        journey.verifyColonyListed(FRANCE, FORT_ORLEANS, true);
        journey.verifyColonyListed(DUTCH, FORT_ORLEANS, false);
        journey.verifyRouteStops(FRANCE, ROUTE, FORT_ORLEANS, HIDDEN_QUEBEC);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 0);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.MUSKET, FORT_MUSKETS);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.TOOLS, FORT_TOOLS);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, FRIGATE_SUGAR);

        // In peace the frigate's attack on the French galleon is offered only
        // as a declaration of war; cancelling it changes nothing.
        attack.rememberWorld();
        attack.chooseAttack(FRIGATE_AT, GALLEON_AT);
        attack.verifyReview("Frigate", "Galleon", DECLARES_WAR, FRIGATE,
                FRENCH_GALLEON);
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        journey.verifyTreatyKept();

        // Turn 0, land: the pioneer on the unpaid square of the capital asks
        // for the price, which moves 200 gold to the capital.
        game.selectTile(PIONEER_AT);
        plow.useControl();
        land.verifyDialogShows(CLAIMANT,
                "Price: 200 gold. You have " + DUTCH_GOLD + " gold.",
                CONSEQUENCE);
        land.verifyPayEnabled(true);
        land.pay();
        land.verifyDialogClosed();
        plow.verifyUnitWork(PIONEER_AT, 0, UnitActionType.plowField, 0, 80);
        land.verifyDutchGold(DUTCH_AFTER_LAND);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, TREASURY_AFTER_LAND);
        land.verifyLandStatus(PIONEER_AT, NativeLandStatus.PAID);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);

        // Turn 0, contact and teaching: the scout introduces the Dutch to the
        // Aztecs, then learns Expert Farmer and leaves the map for 4 updates.
        visit.enterSettlement(SCOUT_AT, AZTEC_CAPITAL);
        market.verifyIntroductionOffered(true);
        market.introduce();
        market.verifyIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        visit.verifyTeachButtonShown();
        visit.startTeachingChoice();
        visit.verifyTeachingConfirmation(CONFIRMATION_EN);
        visit.confirmTeaching();
        visit.verifyTeachingClosed();
        visit.verifyChooserClosed();
        visit.verifyCourseStarted(SCOUT, AZTEC_CAPITAL);

        // Turn 0, native trade: the uneasy Aztecs admit the wagon and buy all
        // 50 corn for 50 gold from their treasury; the trade calms the tribe.
        visit.enterSettlement(WAGON_AT, AZTEC_CAPITAL);
        market.openMarket();
        market.verifySaleOffered(WAGON, AZTEC_CAPITAL, GoodsType.CORN,
                WAGON_CORN);
        market.sell();
        market.verifySaleConfirmation(WAGON, AZTEC_CAPITAL, GoodsType.CORN,
                WAGON_CORN);
        market.confirmDeal();
        market.verifyHeld(WAGON, GoodsType.CORN, 0);
        market.verifyReceived(AZTEC_CAPITAL, GoodsType.CORN, WAGON_CORN);
        visit.escape();
        visit.verifyChooserClosed();
        journey.verifyDutchGold(DUTCH_AFTER_NATIVE_SALE);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, TREASURY_AFTER_SALE);
        incite.verifyTribalAnger(DUTCH, ANGER_AFTER_SALE);

        // Turn 0, trade with France in peace: the frigate sells two sugar to
        // Hidden Quebec at the colony's bid of 3 a piece.
        trade.openShipTrade();
        trade.verifyDialogOpen(true);
        trade.chooseGive(GoodsType.SUGAR);
        trade.setGiveAmount(SOLD_SUGAR);
        trade.verifyLines(GIVE_TWO_SUGAR, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(6));
        trade.confirm();
        trade.verifyDialogOpen(false);
        journey.verifyDutchGold(DUTCH_AFTER_FIRST_TRADE);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, 2);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 2);
        trade.verifyColonyOwner(FRANCE);
        journey.verifyTreatyKept();

        // Turn 0, provocation: the missionary pays 630 gold to turn the Aztecs
        // against France, which the draw allows.
        visit.enterSettlement(MISSIONARY_AT, AZTEC_CAPITAL);
        incite.startIncitementChoice();
        visit.verifyChooserClosed();
        incite.verifyTargetOffered(FRANCE);
        incite.verifyTargetNotOffered(DUTCH);
        incite.selectTarget(FRANCE);
        incite.verifyTerms(INCITEMENT_TERMS);
        incite.verifyTerms("Your chance: 25%.");
        incite.confirmIncitement();
        incite.verifyIncitementResultMessage(INCITEMENT_RESULT);
        incite.closeIncitementResult();
        visit.escape();
        visit.verifyChooserClosed();
        incite.verifyGold(DUTCH_AFTER_INCITEMENT);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);
        incite.verifyTribalAnger(FRANCE, 100);
        incite.verifyTribalAnger(DUTCH, ANGER_AFTER_SALE);
        incite.verifyBraveDistanceTo(AZTEC_BRAVE, FRENCH_TARGET_AT, 3);

        // Checkpoint A, Save and Continue: the purses, the contact, the course,
        // the order and the unmoved brave all survive, and nothing is replayed.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyDutchGold(DUTCH_AFTER_INCITEMENT);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);
        journey.verifyTreatyKept();
        market.verifyIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        visit.verifyCourseStarted(SCOUT, AZTEC_CAPITAL);
        teaching.openSettlements();
        teaching.verifyProgress(progressEn(0));
        teaching.closeSettlements();
        market.verifyReceived(AZTEC_CAPITAL, GoodsType.CORN, WAGON_CORN);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, TREASURY_AFTER_SALE);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);
        incite.verifyTribalAnger(DUTCH, ANGER_AFTER_SALE);
        incite.verifyBraveDistanceTo(AZTEC_BRAVE, FRENCH_TARGET_AT, 3);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 2);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, 2);
        land.verifyLandStatus(PIONEER_AT, NativeLandStatus.PAID);
        land.verifyPaidSquares(9);

        // Turn 1: the brave walks toward the known French colonist beyond the
        // raid radius; the order and the course each count one update.
        game.nextTurnAndCloseDialogs();
        incite.verifyBraveDistanceTo(AZTEC_BRAVE, FRENCH_TARGET_AT, 2);
        incite.verifyOrder(EuropeanNation.FRENCH, 19);
        teaching.verifyLearning(SCOUT, AZTEC_CAPITAL, 1);
        journey.verifyTreatyKept();
        journey.verifyDutchGold(DUTCH_AFTER_INCITEMENT);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);

        // Turn 2: the brave stands beside the colonist with no movement left to
        // attack. Without a declaration the soldier's attack on Fort Orleans
        // is only offered as a declaration of war, which the player cancels.
        game.nextTurnAndCloseDialogs();
        incite.verifyBraveDistanceTo(AZTEC_BRAVE, FRENCH_TARGET_AT, 1);
        incite.verifyOrder(EuropeanNation.FRENCH, 18);
        teaching.verifyLearning(SCOUT, AZTEC_CAPITAL, 2);
        // The French pioneer is still working on its initial square.
        journey.verifyUnitAt(FRENCH_TARGET, FRENCH_TARGET_AT);
        game.panToTile(FORT_AT);
        game.panToTile(SOLDIER_AT);
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER_AT, FORT_AT);
        attack.verifyReview("Free Colonist", "Colonists of Fort Orleans",
                DECLARES_WAR, SOLDIER, FORT_WORKER);
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        journey.verifyTreatyKept();
        journey.verifyColonyOwner(FORT_ORLEANS, FRANCE);

        // Turn 3: the brave attacked and destroyed the French colonist. The
        // Dutch declare war in the relations report; from then on the
        // frigate's attack on the galleon is an ordinary attack.
        game.nextTurnAndCloseDialogs();
        journey.verifyUnitDestroyed(FRENCH_TARGET);
        incite.verifyOrder(EuropeanNation.FRENCH, 17);
        teaching.verifyLearning(SCOUT, AZTEC_CAPITAL, 3);
        europeans.open();
        europeans.review("0-1");
        europeans.confirm();
        game = europeans.close();
        journey.verifyRelation(EuropeanRelationState.WAR);
        game.panToTile(GALLEON_AT);
        attack.rememberWorld();
        attack.chooseAttack(FRIGATE_AT, GALLEON_AT);
        attack.verifyOrdinaryAttackReview("Frigate", "Galleon");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // Turn 3, capture: the soldier wins the battle and takes Fort Orleans
        // with its worker and its stock. France keeps Hidden Quebec, its route
        // still names the lost stop, and no gold changes hands.
        game.panToTile(FORT_AT);
        game.panToTile(SOLDIER_AT);
        attack.chooseAttack(SOLDIER_AT, FORT_AT);
        defense.verifyColonyReview("Colonists of Fort Orleans", "Base: 1",
                "captures the whole colony");
        attack.confirm();
        defense.closeCapturedColonyMessage();
        journey.verifyColonyCaptured(FORT_ORLEANS, DUTCH, 1, SOLDIER, FORT_AT);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.MUSKET, FORT_MUSKETS);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.TOOLS, FORT_TOOLS);
        journey.verifyColonyOwner(HIDDEN_QUEBEC, FRANCE);
        journey.verifyColonyListed(DUTCH, FORT_ORLEANS, true);
        journey.verifyColonyListed(FRANCE, FORT_ORLEANS, false);
        journey.verifyRouteStopLost(FRANCE, ROUTE, FORT_ORLEANS, DUTCH);
        journey.verifyDutchGold(DUTCH_AFTER_INCITEMENT);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);

        // Checkpoint B, Save and Continue: war, ownership, stock, purses and
        // the lost route stop survive; nothing is captured again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyRelation(EuropeanRelationState.WAR);
        journey.verifyColonyOwner(FORT_ORLEANS, DUTCH);
        journey.verifyColonyResidents(FORT_ORLEANS, 1);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.MUSKET, FORT_MUSKETS);
        journey.verifyColonyListed(DUTCH, FORT_ORLEANS, true);
        journey.verifyColonyListed(FRANCE, FORT_ORLEANS, false);
        journey.verifyRouteStopLost(FRANCE, ROUTE, FORT_ORLEANS, DUTCH);
        journey.verifyUnitAt(SOLDIER, FORT_AT);
        journey.verifyUnitDestroyed(FRENCH_TARGET);
        journey.verifyDutchGold(DUTCH_AFTER_INCITEMENT);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);
        incite.verifyOrder(EuropeanNation.FRENCH, 17);

        // Turn 4: the course ends, and the skill is granted once.
        final ScreenTurnReport report = teaching.nextTurnWithReport();
        teaching.verifyReportAnnounces(report, REPORT_EN);
        teaching.closeReport(report);
        teaching.verifyLearnedAndReleased(SCOUT, AZTEC_CAPITAL, true);
        incite.verifyOrder(EuropeanNation.FRENCH, 16);
        journey.verifyDutchGold(DUTCH_AFTER_INCITEMENT);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);

        // Turn 4, peace: the observer stands beside Hidden Quebec, France
        // offers peace for 200, and the Dutch pay it once.
        europeans.open();
        peace.review();
        peace.verifyQuote(PEACE_PRICE, DUTCH_AFTER_INCITEMENT,
                DUTCH_AFTER_PEACE, true);
        peace.confirm();
        journey.verifyPeaceSettled(DUTCH_AFTER_INCITEMENT,
                FRENCH_AFTER_FIRST_TRADE, PEACE_PRICE);
        game = europeans.close();
        journey.verifyDutchGold(DUTCH_AFTER_PEACE);
        journey.verifyFrenchGold(FRENCH_AFTER_PEACE);
        journey.verifyColonyOwner(FORT_ORLEANS, DUTCH);
        journey.verifyUnitAt(OBSERVER, OBSERVER_AT);

        // Peace changes the attack legality again: the same gesture is only
        // offered as a new declaration of war, which the player cancels.
        game.panToTile(GALLEON_AT);
        attack.rememberWorld();
        attack.chooseAttack(FRIGATE_AT, GALLEON_AT);
        attack.verifyReview("Frigate", "Galleon", DECLARES_WAR, FRIGATE,
                FRENCH_GALLEON);
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        journey.verifyTreatyKept();

        // Turn 4, resumed trade: with peace restored the frigate sells its
        // last two sugar to Hidden Quebec again.
        trade.openShipTrade();
        trade.verifyDialogOpen(true);
        trade.chooseGive(GoodsType.SUGAR);
        trade.setGiveAmount(SOLD_SUGAR);
        trade.verifyLines(GIVE_TWO_SUGAR, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(6));
        trade.confirm();
        trade.verifyDialogOpen(false);
        journey.verifyDutchGold(DUTCH_FINAL);
        journey.verifyFrenchGold(FRENCH_FINAL);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, 0);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 4);
        journey.verifyColonyOwner(HIDDEN_QUEBEC, FRANCE);

        // Checkpoint C, Save and Continue: treaty, ownership, purses and
        // stock survive, and neither peace nor the trade is applied twice.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyTreatyKept();
        journey.verifyDutchGold(DUTCH_FINAL);
        journey.verifyFrenchGold(FRENCH_FINAL);
        journey.verifyColonyOwner(FORT_ORLEANS, DUTCH);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.MUSKET, FORT_MUSKETS);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.TOOLS, FORT_TOOLS);
        journey.verifyColonyListed(FRANCE, FORT_ORLEANS, false);
        journey.verifyRouteStopLost(FRANCE, ROUTE, FORT_ORLEANS, DUTCH);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 4);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, 0);
        teaching.verifyLearnedAndReleased(SCOUT, AZTEC_CAPITAL, true);
        incite.verifyOrder(EuropeanNation.FRENCH, 16);
    }

    @Test
    @Tag("local")
    void TC_02_uninterrupted_reference_path_reaches_the_same_ledger()
            throws Exception {
        DiplomacyJourneyFixture.prepare(getContext(), SOURCE, PREPARED,
                Variant.JOURNEY);
        final FogPage fog = new FogPage(getContext());
        final PlowPage plow = new PlowPage(getContext());
        final NativeLandPage land = new NativeLandPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final ForeignColonyTradePage trade = new ForeignColonyTradePage(
                getContext());
        final EuropeanRelationsPage europeans = new EuropeanRelationsPage(
                getContext());
        final PeacePage peace = new PeacePage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final ColonyDefensePage defense = new ColonyDefensePage(getContext());
        final DiplomacyJourneyPage journey = new DiplomacyJourneyPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        fog.centerFrontier();
        trade.adopt(HIDDEN_QUEBEC_AT, FRIGATE_AT);

        // The same player commands as the first test, in one session.
        game.selectTile(PIONEER_AT);
        plow.useControl();
        land.pay();
        land.verifyDialogClosed();
        journey.verifyDutchGold(DUTCH_AFTER_LAND);
        visit.enterSettlement(SCOUT_AT, AZTEC_CAPITAL);
        market.introduce();
        visit.startTeachingChoice();
        visit.confirmTeaching();
        visit.verifyCourseStarted(SCOUT, AZTEC_CAPITAL);
        visit.enterSettlement(WAGON_AT, AZTEC_CAPITAL);
        market.openMarket();
        market.sell();
        market.confirmDeal();
        visit.escape();
        visit.verifyChooserClosed();
        journey.verifyDutchGold(DUTCH_AFTER_NATIVE_SALE);
        incite.verifyTribalAnger(DUTCH, ANGER_AFTER_SALE);
        trade.openShipTrade();
        trade.chooseGive(GoodsType.SUGAR);
        trade.setGiveAmount(SOLD_SUGAR);
        trade.confirm();
        trade.verifyDialogOpen(false);
        journey.verifyDutchGold(DUTCH_AFTER_FIRST_TRADE);
        journey.verifyFrenchGold(FRENCH_AFTER_FIRST_TRADE);
        visit.enterSettlement(MISSIONARY_AT, AZTEC_CAPITAL);
        incite.startIncitementChoice();
        incite.selectTarget(FRANCE);
        incite.verifyTerms(INCITEMENT_TERMS);
        incite.confirmIncitement();
        incite.closeIncitementResult();
        visit.escape();
        visit.verifyChooserClosed();
        incite.verifyGold(DUTCH_AFTER_INCITEMENT);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);

        // Turns 1 to 3: the brave closes in on the French colonist and
        // destroys it on the third native turn; the war is declared and the
        // soldier takes Fort Orleans.
        game.nextTurnAndCloseDialogs();
        incite.verifyOrder(EuropeanNation.FRENCH, 19);
        game.nextTurnAndCloseDialogs();
        incite.verifyOrder(EuropeanNation.FRENCH, 18);
        game.nextTurnAndCloseDialogs();
        journey.verifyUnitDestroyed(FRENCH_TARGET);
        incite.verifyOrder(EuropeanNation.FRENCH, 17);
        europeans.open();
        europeans.review("0-1");
        europeans.confirm();
        game = europeans.close();
        journey.verifyRelation(EuropeanRelationState.WAR);
        game.panToTile(FORT_AT);
        game.panToTile(SOLDIER_AT);
        attack.chooseAttack(SOLDIER_AT, FORT_AT);
        attack.confirm();
        defense.closeCapturedColonyMessage();
        journey.verifyColonyCaptured(FORT_ORLEANS, DUTCH, 1, SOLDIER, FORT_AT);
        journey.verifyRouteStopLost(FRANCE, ROUTE, FORT_ORLEANS, DUTCH);

        // Turn 4: the skill is granted, peace is bought once and the trade
        // resumes.
        final ScreenTurnReport report = teaching.nextTurnWithReport();
        teaching.verifyReportAnnounces(report, REPORT_EN);
        teaching.closeReport(report);
        europeans.open();
        peace.review();
        peace.verifyQuote(PEACE_PRICE, DUTCH_AFTER_INCITEMENT,
                DUTCH_AFTER_PEACE, true);
        peace.confirm();
        game = europeans.close();
        journey.verifyDutchGold(DUTCH_AFTER_PEACE);
        journey.verifyFrenchGold(FRENCH_AFTER_PEACE);
        game.panToTile(GALLEON_AT);
        attack.chooseAttack(FRIGATE_AT, GALLEON_AT);
        attack.verifyReview("Frigate", "Galleon", DECLARES_WAR, FRIGATE,
                FRENCH_GALLEON);
        attack.cancel();
        attack.verifyReviewClosed();
        trade.openShipTrade();
        trade.chooseGive(GoodsType.SUGAR);
        trade.setGiveAmount(SOLD_SUGAR);
        trade.confirm();
        trade.verifyDialogOpen(false);

        // The same literal end state as after Checkpoint C.
        journey.verifyTreatyKept();
        journey.verifyDutchGold(DUTCH_FINAL);
        journey.verifyFrenchGold(FRENCH_FINAL);
        journey.verifyColonyOwner(FORT_ORLEANS, DUTCH);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.MUSKET, FORT_MUSKETS);
        journey.verifyColonyStock(FORT_ORLEANS, GoodsType.TOOLS, FORT_TOOLS);
        journey.verifyColonyListed(FRANCE, FORT_ORLEANS, false);
        journey.verifyRouteStopLost(FRANCE, ROUTE, FORT_ORLEANS, DUTCH);
        journey.verifyColonyStock(HIDDEN_QUEBEC, GoodsType.SUGAR, 4);
        trade.verifyCarried(FRIGATE, GoodsType.SUGAR, 0);
        teaching.verifyLearnedAndReleased(SCOUT, AZTEC_CAPITAL, true);
        incite.verifyOrder(EuropeanNation.FRENCH, 16);
    }

    @Test
    @Tag("local")
    void TC_03_hostile_aztecs_raid_the_soldier_beside_their_brave_and_continue_keeps_it()
            throws Exception {
        DiplomacyJourneyFixture.prepare(getContext(), SOURCE, PREPARED,
                Variant.HOSTILE);
        final FogPage fog = new FogPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        final NativeRaidPage raid = new NativeRaidPage(getContext());
        final DiplomacyJourneyPage journey = new DiplomacyJourneyPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        fog.centerFrontier();

        // Start: the Aztecs are hostile toward the Dutch, the brave stands on
        // its hill and an armed soldier stands beside it.
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                HOSTILE_ANGER);
        journey.verifyUnitAt(AZTEC_BRAVE, BRAVE_AT);
        journey.verifyUnitAt(RAID_SOLDIER, RAID_SOLDIER_AT);
        journey.verifyTreatyKept();

        // The hostile brave raids the soldier on the native turn, which loses
        // its muskets but stays where it stood.
        game.nextTurnAndCloseDialogs();
        raid.verifySoldierLostMusketsToRaid(RAID_SOLDIER, RAID_SOLDIER_AT,
                AZTEC_BRAVE, BRAVE_AT);
        journey.verifyTreatyKept();

        // Save and Continue keep the raided soldier without a new raid.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        raid.verifySoldierLostMusketsToRaid(RAID_SOLDIER, RAID_SOLDIER_AT,
                AZTEC_BRAVE, BRAVE_AT);
        journey.verifyTreatyKept();
    }

    @Test
    @Tag("local")
    void TC_04_privateer_attacks_the_french_galleon_in_peace_without_declaring_war()
            throws Exception {
        DiplomacyJourneyFixture.prepare(getContext(), SOURCE, PREPARED,
                Variant.PRIVATEER);
        final FogPage fog = new FogPage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final NavalBattlePage naval = new NavalBattlePage(getContext());
        final DiplomacyJourneyPage journey = new DiplomacyJourneyPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        fog.centerFrontier();
        journey.verifyTreatyKept();

        // The review names the exception and is cancelled without a change.
        attack.rememberWorld();
        attack.chooseAttack(PRIVATEER_AT, GALLEON_AT);
        attack.verifyReview("Privateer", "Galleon", "does not declare war",
                PRIVATEER_SHIP, FRENCH_GALLEON);
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // The confirmed attack is fought and leaves the peace untouched.
        attack.chooseAttack(PRIVATEER_AT, GALLEON_AT);
        attack.confirm();
        naval.closeOutcome();
        journey.verifyTreatyKept();
        journey.verifyMovementSpent(PRIVATEER_SHIP);

        // Save and Continue keep the peace and the spent privateer.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyTreatyKept();
        journey.verifyMovementSpent(PRIVATEER_SHIP);
    }
}
