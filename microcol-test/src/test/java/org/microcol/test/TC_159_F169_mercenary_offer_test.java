package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.page.AttackPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.InterventionPage;
import org.microcol.page.MercenaryPage;
import org.microcol.page.NavalBattlePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A European power offers a paid group of two veteran soldiers (R-0169). The
 * tenth rebel update of the Dutch brings the offer from France, who is at peace
 * with them. The window states the issuer, the full price and the owned port
 * choice. The player pays once and the soldiers land, or closes the window and
 * finds the same offer again, or declines and pays nothing. While the Crown's
 * ships hold both sea approaches of the chosen port the paid group stays
 * pending, and it lands exactly once after the player sinks one of them. Save
 * and Continue keep every state without a second payment or a second group.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_159_F169_mercenary_offer_test extends AbstractMicroColTest {

    /** The scenario is Conquistador, so the bundle costs 1000 gold. */
    private final static int PRICE = 1000;

    /** The Dutch treasury of the prepared scenario. */
    private final static int TREASURY = 5000;

    private final static String ISSUER = "France";

    /** Lowest-ID owned coastal colony, the default port. */
    private final static String PORT_NAME = "New Amsterdam";

    private final static Location PORT = Location.of(25, 14);

    /** A second owned coastal colony that opens to the northern ocean. */
    private final static String DELFT_NAME = "Delft";

    private final static Location DELFT = Location.of(22, 12);

    /** The only sea squares beside New Amsterdam, as in TC_156. */
    private final static Location NORTH_APPROACH = Location.of(26, 13);

    private final static Location SOUTH_APPROACH = Location.of(26, 14);

    /** Open sea square beside the south approach, where the frigate waits. */
    private final static Location FRIGATE_AT = Location.of(27, 15);

    private final static int FRIGATE_ID = 100;

    private final static int BLOCKER_NORTH_ID = 101;

    private final static int BLOCKER_SOUTH_ID = 102;

    private final static String DUTCH = "Dutch";

    private final static String CROWN = "Dutch's King";

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_tenth_update_offers_two_veterans_and_the_player_pays_once_for_the_chosen_port()
            throws Exception {
        // The Dutch have declared independence, stand one rebel update before
        // the tenth, hold 5000 gold and know France, who is at peace with them.
        MercenaryFixture.prepare(getContext(), testFileName,
                "mercenary-f169-accept.microcol");
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final MercenaryPage mercenary = new MercenaryPage(getContext());
        mercenary.rememberStart();
        mercenary.verifyNoOffer();

        // The player ends the turn. The tenth rebel update brings the offer,
        // and the window states issuer, full price, treasury and port choice.
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        mercenary.waitForOffer();
        mercenary.verifyOfferShown(ISSUER, PRICE, TREASURY);
        mercenary.verifyPortOffered(PORT_NAME);
        mercenary.verifyPortOffered(DELFT_NAME);
        mercenary.verifySelectedPort(PORT_NAME);
        mercenary.verifyAcceptEnabled(true);
        mercenary.verifyBlockedHint(false);

        // The player chooses Delft and pays. The window says where the soldiers
        // landed.
        mercenary.choosePort(DELFT_NAME);
        mercenary.verifySelectedPort(DELFT_NAME);
        mercenary.accept();
        mercenary.verifyDeliveredFeedback();
        mercenary.close();
        mercenary.verifyDialogShown(false);
        mercenary.closeReport(report);

        // The full price was paid once, two armed veterans stand in Delft and
        // no offer is left.
        mercenary.verifyGoldChange(-PRICE);
        mercenary.verifyVeteransAt(DELFT, 2);
        mercenary.verifyPaidGroups(1, 1);
        mercenary.verifyNoOffer();
        mercenary.rememberVeterans();

        // Save and Continue keep the same two soldiers and ask for nothing.
        gamePage.exitToMenu();
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        mercenary.verifyDialogShown(false);
        mercenary.verifyGoldChange(-PRICE);
        mercenary.verifyVeteransAt(DELFT, 2);
        mercenary.verifyVeteransKept();
        mercenary.verifyPaidGroups(1, 1);

        // The next update is not an interval: no offer, no second payment and
        // no second group.
        final Optional<ScreenTurnReport> later = continued.nextTurn();
        mercenary.verifyDialogShown(false);
        mercenary.verifyNoOffer();
        mercenary.verifyNoArrivalNotice(later);
        mercenary.closeReport(later);
        mercenary.verifyGoldChange(-PRICE);
        mercenary.verifyVeteransKept();
        mercenary.verifyPaidGroups(1, 1);
    }

    @Test
    @Tag("local")
    void TC_02_closing_keeps_the_offer_and_declining_charges_nothing()
            throws Exception {
        MercenaryFixture.prepare(getContext(), testFileName,
                "mercenary-f169-decline.microcol");
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final MercenaryPage mercenary = new MercenaryPage(getContext());
        mercenary.rememberStart();

        // The tenth rebel update brings the offer; the player decides later.
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        mercenary.waitForOffer();
        mercenary.verifyOfferShown(ISSUER, PRICE, TREASURY);
        mercenary.decideLater();
        mercenary.verifyDialogShown(false);
        mercenary.closeReport(report);

        // Closing is not an answer: the same offer is pending and nothing was
        // paid or placed.
        mercenary.verifyOfferPending(ISSUER, PRICE);
        mercenary.verifyNothingPaid();

        // The relations report opens the same offer again in the same turn.
        mercenary.reviewFromRelations();
        mercenary.verifyOfferShown(ISSUER, PRICE, TREASURY);
        mercenary.decideLater();
        mercenary.verifyDialogShown(false);
        mercenary.leaveStatistics();
        mercenary.verifyOfferPending(ISSUER, PRICE);

        // Save and Continue keep the pending choice: the window comes back
        // with the same terms and still nothing has been paid.
        gamePage.exitToMenu();
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        mercenary.waitForOffer();
        mercenary.verifyOfferShown(ISSUER, PRICE, TREASURY);
        mercenary.verifyNothingPaid();

        // The player declines. Nothing is paid, nothing is placed and the
        // offer is gone.
        mercenary.decline();
        mercenary.verifyDialogShown(false);
        mercenary.verifyNoOffer();
        mercenary.verifyNothingPaid();

        // The next update is not an interval, so no other offer appears.
        final Optional<ScreenTurnReport> later = continued.nextTurn();
        mercenary.verifyDialogShown(false);
        mercenary.verifyNoOffer();
        mercenary.closeReport(later);
        mercenary.verifyNothingPaid();
    }

    @Test
    @Tag("local")
    void TC_03_blocked_port_keeps_the_paid_group_pending_until_the_player_sinks_a_blocker()
            throws Exception {
        // Two Crown ships with empty holds hold both sea squares beside New
        // Amsterdam, and a Dutch frigate waits nearby.
        MercenaryFixture.prepare(getContext(), testFileName,
                "mercenary-f169-blocked.microcol",
                IndependenceFixture.ship(FRIGATE_ID, UnitType.FRIGATE, DUTCH,
                        FRIGATE_AT),
                IndependenceFixture.ship(BLOCKER_NORTH_ID, UnitType.CARAVEL,
                        CROWN, NORTH_APPROACH),
                IndependenceFixture.ship(BLOCKER_SOUTH_ID, UnitType.CARAVEL,
                        CROWN, SOUTH_APPROACH));
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final MercenaryPage mercenary = new MercenaryPage(getContext());
        final InterventionPage ships = new InterventionPage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final NavalBattlePage naval = new NavalBattlePage(getContext());
        mercenary.rememberStart();

        // The window warns about the blocked default port, but not about Delft.
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        mercenary.waitForOffer();
        mercenary.verifySelectedPort(PORT_NAME);
        mercenary.verifyBlockedHint(true);
        mercenary.choosePort(DELFT_NAME);
        mercenary.verifyBlockedHint(false);
        mercenary.choosePort(PORT_NAME);
        mercenary.verifyBlockedHint(true);

        // The player pays for the blocked port anyway: the soldiers wait.
        mercenary.accept();
        mercenary.verifyPendingFeedback();
        mercenary.close();
        mercenary.verifyDialogShown(false);
        mercenary.closeReport(report);
        mercenary.verifyGoldChange(-PRICE);
        mercenary.verifyPaidSoldierCount(0);
        mercenary.verifyPaidGroups(1, 0);
        mercenary.verifyNoOffer();

        // Save and Continue keep the paid, pending group without a second
        // payment, a second window or any soldier.
        gamePage.exitToMenu();
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        mercenary.verifyDialogShown(false);
        mercenary.verifyGoldChange(-PRICE);
        mercenary.verifyPaidSoldierCount(0);
        mercenary.verifyPaidGroups(1, 0);

        // The player sinks the south blocker with the frigate in a real battle.
        // Nothing lands before the owner's next update.
        continued.panToTile(PORT);
        naval.chooseSinkingDraw(FRIGATE_ID, SOUTH_APPROACH);
        attack.chooseAttack(FRIGATE_AT, SOUTH_APPROACH);
        attack.confirm();
        naval.verifyOutcomeText("#" + BLOCKER_SOUTH_ID + " sank.");
        naval.closeOutcome();
        ships.verifyShipGone(BLOCKER_SOUTH_ID);
        ships.verifyShipAt(BLOCKER_NORTH_ID, NORTH_APPROACH);
        mercenary.verifyPaidSoldierCount(0);

        // One free approach is enough: the next update lands both soldiers at
        // the chosen port and reports the arrival once.
        final Optional<ScreenTurnReport> arrival = continued.nextTurn();
        mercenary.verifyArrivalNotice(arrival, PORT_NAME);
        mercenary.closeReport(arrival);
        mercenary.verifyVeteransAt(PORT, 2);
        mercenary.verifyPaidGroups(1, 1);
        mercenary.verifyGoldChange(-PRICE);
        mercenary.rememberVeterans();

        // A later update lands no second group and charges nothing more.
        final Optional<ScreenTurnReport> later = continued.nextTurn();
        mercenary.verifyNoArrivalNotice(later);
        mercenary.closeReport(later);
        mercenary.verifyVeteransKept();
        mercenary.verifyGoldChange(-PRICE);

        // Save and Continue keep the same two soldiers.
        continued.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        mercenary.verifyDialogShown(false);
        mercenary.verifyVeteransKept();
        mercenary.verifyVeteransAt(PORT, 2);
        mercenary.verifyPaidGroups(1, 1);
    }
}
