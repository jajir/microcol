package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.page.AttackPage;
import org.microcol.page.CongressPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.InterventionPage;
import org.microcol.page.NavalBattlePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The earned foreign intervention is delivered once (R-0190): a real update
 * delivers two men-of-war, four Continental infantry and two Continental
 * cavalry at the lowest-ID owned coastal colony, the arrival is reported, the
 * player selects and moves a delivered unit, and Save and Continue keep the
 * delivered state without a second delivery. While the Crown's ships hold both
 * sea approaches of that colony the delivery stays pending, and it happens
 * exactly once after the player sinks one of them.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_156_F190_intervention_delivery_test
        extends AbstractMicroColTest {

    /** The year 1747 of the shared independence scenario, as in TC_155. */
    private final static int YEAR_1747 = 700;

    private final static int END_YEAR = 1900;

    /** Full liberty in the four colonies of the scenario, as in TC_155. */
    private final static long FULL = 1600L;

    /** The scenario is Conquistador, so the target is 500 bells. */
    private final static int TARGET = 500;

    /** Lowest-ID owned coastal colony of the scenario, the arrival port. */
    private final static String PORT_NAME = "New Amsterdam";

    private final static Location PORT = Location.of(25, 14);

    /** Land square beside the port where the delivered cavalry rides. */
    private final static Location RIDE_TO = Location.of(24, 14);

    /**
     * The only sea squares beside the port; the Crown's ships hold both in the
     * blocked scenario.
     */
    private final static Location NORTH_APPROACH = Location.of(26, 13);

    private final static Location SOUTH_APPROACH = Location.of(26, 14);

    /**
     * Open sea square beside the south approach, where the Dutch frigate waits.
     */
    private final static Location FRIGATE_AT = Location.of(27, 15);

    private final static int FRIGATE_ID = 100;

    private final static int BLOCKER_NORTH_ID = 101;

    private final static int BLOCKER_SOUTH_ID = 102;

    private final static String DUTCH = "Dutch";

    private final static String CROWN = "Dutch's King";

    /** Two ships, four infantry and two cavalry. */
    private final static int DELIVERED_UNITS = 8;

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_real_update_earns_and_delivers_the_forces_once_and_the_player_moves_one()
            throws Exception {
        // Saved progress stands one bell short of the target, so the next real
        // update of the colonies earns it and delivers the forces in the same
        // update.
        IndependenceFixture.prepareDeclared(getContext(), testFileName,
                "intervention-f190-arrival.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL }, IndependenceFixture
                        .intervention(TARGET, TARGET - 1, false, false));
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        congress.open();
        congress.verifyInterventionProgress(TARGET - 1, TARGET);
        congress.close();

        // The update reports the arrival at the lowest-ID owned coastal colony,
        // and the eight units stand there under rebel control.
        final Optional<ScreenTurnReport> arrival = gamePage.nextTurn();
        intervention.verifyArrivalNotice(arrival, PORT_NAME);
        intervention.closeReport(arrival);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);
        intervention.rememberDeliveredForces();
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.verifyInterventionForcesArrived();
        congress.close();

        // A later update adds no second arrival and no second set of units.
        final Optional<ScreenTurnReport> later = gamePage.nextTurn();
        intervention.verifyNoArrivalNotice(later);
        intervention.closeReport(later);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);

        // Save and Continue keep the same eight units and the delivered state.
        gamePage.exitToMenu();
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();
        intervention.verifyDeliveredIdentityKept(DELIVERED_UNITS);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);

        // The player selects one delivered cavalry and rides it off the port.
        intervention.selectDeliveredCavalry(continued);
        intervention.verifyDeliveredCavalrySelected();
        intervention.moveSelectedUnit(continued, PORT, RIDE_TO);
        intervention.verifyDeliveredCavalryMovedTo(RIDE_TO);
        intervention.verifyDeliveredIdentityKept(DELIVERED_UNITS);
    }

    @Test
    @Tag("local")
    void TC_02_earned_forces_that_wait_arrive_at_a_later_real_update_and_never_twice()
            throws Exception {
        // The earning is saved, but its delivery has not happened yet.
        IndependenceFixture.prepareDeclared(getContext(), testFileName,
                "intervention-f190-pending.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL },
                IndependenceFixture.intervention(TARGET, TARGET, true, false));
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.verifyInterventionForcesPending();
        congress.close();
        intervention.verifyNothingDelivered();

        // The next real update delivers the waiting forces and reports them
        // once.
        final Optional<ScreenTurnReport> arrival = gamePage.nextTurn();
        intervention.verifyArrivalNotice(arrival, PORT_NAME);
        intervention.closeReport(arrival);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);
        intervention.rememberDeliveredForces();
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();

        // A later update never delivers a second time.
        final Optional<ScreenTurnReport> later = gamePage.nextTurn();
        intervention.verifyNoArrivalNotice(later);
        intervention.closeReport(later);
        intervention.verifyDeliveredIdentityKept(DELIVERED_UNITS);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);

        // Save and Continue keep the delivered state and the same identities.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();
        intervention.verifyDeliveredIdentityKept(DELIVERED_UNITS);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);
    }

    @Test
    @Tag("local")
    void TC_03_blocked_port_keeps_the_delivery_pending_until_the_player_sinks_a_blocker()
            throws Exception {
        // The earning is saved. Two Crown ships with empty holds hold both sea
        // squares beside New Amsterdam, and a Dutch frigate waits nearby.
        IndependenceFixture.prepareDeclaredWithShips(getContext(), testFileName,
                "intervention-f190-blocked.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL },
                IndependenceFixture.intervention(TARGET, TARGET, true, false),
                IndependenceFixture.ship(FRIGATE_ID, UnitType.FRIGATE, DUTCH,
                        FRIGATE_AT),
                IndependenceFixture.ship(BLOCKER_NORTH_ID, UnitType.CARAVEL,
                        CROWN, NORTH_APPROACH),
                IndependenceFixture.ship(BLOCKER_SOUTH_ID, UnitType.CARAVEL,
                        CROWN, SOUTH_APPROACH));
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final NavalBattlePage naval = new NavalBattlePage(getContext());
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.verifyInterventionForcesPending();
        congress.close();

        // The first real update finds the port blocked: nothing arrives, no
        // other port is used, and the delivery stays pending.
        final Optional<ScreenTurnReport> blocked = gamePage.nextTurn();
        intervention.verifyNoArrivalNotice(blocked);
        intervention.closeReport(blocked);
        intervention.verifyNothingDelivered();
        intervention.verifyShipAt(BLOCKER_NORTH_ID, NORTH_APPROACH);
        intervention.verifyShipAt(BLOCKER_SOUTH_ID, SOUTH_APPROACH);
        congress.open();
        congress.verifyInterventionForcesPending();
        congress.close();

        // Save and Continue keep the pending delivery and still create nothing.
        gamePage.exitToMenu();
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        congress.open();
        congress.verifyInterventionForcesPending();
        congress.close();
        intervention.verifyNothingDelivered();

        // The player sinks the south blocker with the frigate in a real battle.
        // Nothing is delivered before the owner's next update.
        continued.panToTile(PORT);
        naval.chooseSinkingDraw(FRIGATE_ID, SOUTH_APPROACH);
        attack.chooseAttack(FRIGATE_AT, SOUTH_APPROACH);
        attack.confirm();
        naval.verifyOutcomeText("#" + BLOCKER_SOUTH_ID + " sank.");
        naval.closeOutcome();
        intervention.verifyShipGone(BLOCKER_SOUTH_ID);
        intervention.verifyShipAt(BLOCKER_NORTH_ID, NORTH_APPROACH);
        intervention.verifyNothingDelivered();

        // One free approach is enough: the next update delivers all eight units
        // at the same port and reports the arrival.
        final Optional<ScreenTurnReport> arrival = continued.nextTurn();
        intervention.verifyArrivalNotice(arrival, PORT_NAME);
        intervention.closeReport(arrival);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);
        intervention.rememberDeliveredForces();
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();

        // Save and Continue keep the same eight units, and no second set
        // follows.
        continued.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        intervention.verifyDeliveredIdentityKept(DELIVERED_UNITS);
        intervention.verifyDeliveredForces(PORT, 2, 4, 2);
        congress.open();
        congress.verifyInterventionForcesArrived();
        congress.close();
    }
}
