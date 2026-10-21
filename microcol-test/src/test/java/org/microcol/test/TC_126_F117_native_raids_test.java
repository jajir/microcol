package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeCombatPage;
import org.microcol.page.NativeRaidPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.NativeTurnsPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Native braves act on their own turn. A hostile Aztec brave on the hill raids
 * the Dutch soldier beside it when the human ends the turn. The raid commits no
 * relation effect of its own, since only attacks on natives do; the relations
 * move only by the ordinary peaceful Dutch update. A calmer Aztec nation raids
 * nothing, but its brave still takes the shared home patrol. A stockaded gun on
 * the colony square holds against the same hostile brave with the native raid
 * bonus, and Continue keeps every result. The native raid against a soldier and
 * against a gun are decided by saved draws chosen from the quoted odds, and the
 * model tests cover the choice of targets and the patrol.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_126_F117_native_raids_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** Aztec brave on a hill beside the Dutch colony; home is the capital. */
    private static final int AZTEC_BRAVE_ID = 200;
    private static final Location AZTEC_BRAVE = Location.of(23, 16);

    /**
     * First square of the calm patrol. The brave is inside two squares of its
     * home, so it takes the first reachable neighbor in ascending (y, x) order
     * that stays within two squares of the home at (23, 15).
     */
    private static final Location AZTEC_PATROL_STEP = Location.of(22, 15);

    /** Armed Dutch soldier added by the initial fixture, south of the brave. */
    private static final int SOLDIER_ID = 300;
    private static final Location SOLDIER = Location.of(23, 17);

    /** Full gun added on the colony square, inside its stockade. */
    private static final int GUN_ID = 310;
    private static final Location COLONY = Location.of(24, 16);

    /** Anger of the Aztecs toward the Dutch that makes the brave hostile. */
    private static final double HOSTILE_ANGER = 80;

    /** Anger of the Aztecs toward the Dutch that makes the brave calmer. */
    private static final double CALM_ANGER = 10;

    /** The Aztec capital, the only Aztec settlement, learned by the Dutch. */
    private static final Location AZTEC_CAPITAL = Location.of(23, 15);

    /*
     * A native attack on a European commits no relation effect of its own, but
     * ending the turn also runs the one peaceful Dutch update when the Dutch
     * turn is prepared again. The capital's local alarm starts at 0, decays by
     * 1 (staying at 0) and adds the Dutch pressure. Every scenario has the same
     * base: the armed frigate within two squares adds 2, the two colony workers
     * within the city's home radius of two add 0.25 each, and the unpaid colony
     * square and worked corn field the capital claims add 2 each, so 6.5. The
     * Aztec tribal anger then decays by 1 and gains the mean Aztec local alarm
     * divided by 20. The local factor of the Dutch is 1.
     */
    private static final double BASE_ALARM = 2 + 2 * 0.25 + 2 * 2;

    /** A land unit within the home radius. */
    private static final double RESIDENT = 0.25;

    /** An armed unit within two squares. */
    private static final double ARMED = 2;

    /** The raided soldier has lost its muskets: it counts only as resident. */
    private static final double RAIDED_ALARM = BASE_ALARM + RESIDENT;

    /** The unharmed soldier, or the gun, counts as resident and as armed. */
    private static final double ARMED_ALARM = BASE_ALARM + RESIDENT + ARMED;

    private static double angerAfterDutchUpdate(final double before,
            final double alarm) {
        return before - 1 + alarm / 20;
    }

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_hostile_brave_raids_adjacent_soldier_and_continue_keeps_it()
            throws Exception {
        // Load the native position, then the prepared fixture with hostile
        // Aztecs and a soldier beside the brave; the saved draw lets the brave
        // win the raid.
        WelcomePage.of(getContext()).loadGame();
        final NativeRaidPage raid = new NativeRaidPage(getContext());
        final NativeTurnsPage natives = new NativeTurnsPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        raid.loadFixture(FIXTURE, HOSTILE_ANGER, SOLDIER_ID, SOLDIER);
        fog.centerFrontier();
        natives.verifyBraveLocation(AZTEC_BRAVE_ID, AZTEC_BRAVE);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                HOSTILE_ANGER);

        // The human ends the turn; the hostile brave raids the soldier beside
        // it, which loses its muskets, and the brave stays on its square.
        // The raid commits no relation effect; only the peaceful Dutch update
        // moves the relations, counting the disarmed soldier as a resident.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        raid.verifySoldierLostMusketsToRaid(SOLDIER_ID, SOLDIER, AZTEC_BRAVE_ID,
                AZTEC_BRAVE);
        combat.verifyRelationOutcomeCount(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                RAIDED_ALARM);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                angerAfterDutchUpdate(HOSTILE_ANGER, RAIDED_ALARM));

        // Continue from disk keeps the raided position without a new raid or
        // another update.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        raid.verifySoldierLostMusketsToRaid(SOLDIER_ID, SOLDIER, AZTEC_BRAVE_ID,
                AZTEC_BRAVE);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                RAIDED_ALARM);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                angerAfterDutchUpdate(HOSTILE_ANGER, RAIDED_ALARM));
        combat.verifyRelationOutcomeCount(0);
    }

    @Test
    @Tag("local")
    void TC_02_calmer_brave_raids_nothing_and_takes_the_home_patrol()
            throws Exception {
        // The same position with calmer Aztecs. The soldier is left alone; the
        // brave raids nothing, but the shared home patrol still moves it.
        WelcomePage.of(getContext()).loadGame();
        final NativeRaidPage raid = new NativeRaidPage(getContext());
        final NativeTurnsPage natives = new NativeTurnsPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        raid.loadFixture(FIXTURE, CALM_ANGER, SOLDIER_ID, SOLDIER);
        fog.centerFrontier();

        // No battle and no relation effect; the peaceful Dutch update counts
        // the armed soldier as resident and armed.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        raid.verifySoldierUnharmed(SOLDIER_ID, SOLDIER);
        raid.verifyBraveOnHomePatrol(AZTEC_BRAVE_ID);
        natives.verifyBravePatrolledTo(AZTEC_BRAVE_ID, AZTEC_PATROL_STEP);
        combat.verifyRelationOutcomeCount(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                ARMED_ALARM);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                angerAfterDutchUpdate(CALM_ANGER, ARMED_ALARM));
    }

    @Test
    @Tag("local")
    void TC_03_fortified_gun_holds_against_hostile_brave_and_continue_keeps_it()
            throws Exception {
        // Hostile Aztecs with the survivor removed, and a full gun inside the
        // colony stockade. The saved draw lies just above the correct raid
        // odds, so the gun holds only with the stockade and native-raid
        // bonuses applied and no hill ambush from the brave's own tile.
        WelcomePage.of(getContext()).loadGame();
        final NativeRaidPage raid = new NativeRaidPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        raid.loadFortifiedGunFixture(FIXTURE, GUN_ID, COLONY);
        fog.centerFrontier();

        // The inputs of the raid quote are the documented ones before anything
        // happens: base 1 brave, grass colony tile, stockade, full gun.
        raid.verifyRaidInputs(AZTEC_BRAVE_ID, GUN_ID, COLONY);

        // The hill brave raids the stockaded gun: the gun holds at full
        // strength and the brave is destroyed. The raid commits no relation
        // effect; the peaceful Dutch update counts the gun as resident and
        // armed, and the stockade itself adds no alarm.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        raid.verifyGunDefendedAgainstRaid(AZTEC_BRAVE_ID, GUN_ID, COLONY);
        combat.verifyRelationOutcomeCount(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                ARMED_ALARM);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                angerAfterDutchUpdate(HOSTILE_ANGER, ARMED_ALARM));

        // Continue from disk keeps the held gun, the destroyed brave and the
        // relations, without another raid or update.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        raid.verifyGunDefendedAgainstRaid(AZTEC_BRAVE_ID, GUN_ID, COLONY);
        combat.verifyRelationOutcomeCount(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                ARMED_ALARM);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                angerAfterDutchUpdate(HOSTILE_ANGER, ARMED_ALARM));
    }
}
