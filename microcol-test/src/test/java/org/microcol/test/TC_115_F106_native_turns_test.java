package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NationalTurnPage;
import org.microcol.page.NativeTurnsPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.gson.JsonElement;

import javafx.stage.Stage;

/**
 * Native nations take real turns before the Europeans. Their braves are real
 * units with a nation and a home, identified on the map only as far as the
 * Dutch have seen them, and the whole position survives Continue from disk.
 * Every nation is content, so no brave raids (R-0156): a brave with a home
 * takes one home-patrol step to its first reachable neighbor in ascending (y,
 * x) order within two squares of home, and the homeless survivor stands.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_115_F106_native_turns_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /**
     * Turns finished from the human's prepared turn: the rest of the round,
     * then the natives in nation order, then France; the human is last.
     */
    private static final List<String> ORDER = List.of("Dutch", "France's King",
            "Dutch's King", "AZTEC", "INCA", "CHEROKEE", "France");

    /** Aztec brave beside the Dutch colony, home is the Aztec capital. */
    private static final int AZTEC_BRAVE_ID = 200;
    private static final Location AZTEC_BRAVE = Location.of(23, 16);
    /** Aztec survivor of a lost home, also beside the colony. */
    private static final int AZTEC_SURVIVOR_ID = 201;
    private static final Location AZTEC_SURVIVOR = Location.of(24, 15);
    /** Inca brave far inland: never inside Dutch sight. */
    private static final int INCA_BRAVE_ID = 202;
    private static final Location INCA_BRAVE = Location.of(16, 22);
    private static final Location INCA_CAPITAL = Location.of(15, 22);
    /** Cherokee brave beyond the coast: never inside Dutch sight. */
    private static final int CHEROKEE_BRAVE_ID = 203;
    private static final Location CHEROKEE_BRAVE = Location.of(31, 18);
    private static final Location CHEROKEE_CAPITAL = Location.of(30, 18);

    /**
     * First home-patrol squares: open grass, the first in (y, x) order beside
     * each brave and within two squares of its home. All three lie outside the
     * sight of the Dutch frigate and colony, so the patrol discloses nothing.
     */
    private static final Location AZTEC_PATROL = Location.of(22, 15);
    private static final Location INCA_PATROL = Location.of(15, 21);
    private static final Location CHEROKEE_PATROL = Location.of(30, 17);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_native_actors_act_before_europeans_and_continue_keeps_them()
            throws Exception {
        // Load the mixed position at the human's prepared turn.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FogPage fog = new FogPage(getContext());
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        final NativeTurnsPage natives = new NativeTurnsPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        fog.centerFrontier();
        audit.verifyCurrentTurn(18);
        audit.verifyDisplayedDate("Year 1508 AD");

        // Four real braves exist, each with its own nation and explicit home.
        natives.verifyBraveCount(4);
        natives.verifyBraveIdentity(AZTEC_BRAVE_ID, NativeNation.AZTEC, 0);
        natives.verifyBraveIdentity(AZTEC_SURVIVOR_ID, NativeNation.AZTEC,
                null);
        natives.verifyBraveIdentity(INCA_BRAVE_ID, NativeNation.INCA, 3);
        natives.verifyBraveIdentity(CHEROKEE_BRAVE_ID, NativeNation.CHEROKEE,
                1);

        // The Aztec brave beside the colony is seen with its nation and its
        // learned home; the survivor is named as homeless.
        fog.hover(AZTEC_BRAVE);
        natives.verifyBraveSeen(AZTEC_BRAVE, AZTEC_BRAVE_ID, true);
        natives.verifyBraveHomeKnown(AZTEC_BRAVE_ID, true);
        fog.verifyStatusBarContains(
                "Native brave: Aztec, home: city, capital.");
        fog.hover(AZTEC_SURVIVOR);
        natives.verifyBraveSeen(AZTEC_SURVIVOR, AZTEC_SURVIVOR_ID, true);
        fog.verifyStatusBarContains("Native brave: Aztec, homeless.");

        // The Inca and Cherokee braves are out of sight: nothing about them
        // or their homes is disclosed.
        fog.hover(INCA_BRAVE);
        natives.verifyBraveSeen(INCA_BRAVE, INCA_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(INCA_BRAVE_ID, false);
        fog.verifyNoNativeSettlementKnown(INCA_CAPITAL);
        fog.verifyStatusBarOmits("Native brave");
        fog.verifyStatusBarOmits("Inca");
        fog.hover(CHEROKEE_BRAVE);
        natives.verifyBraveSeen(CHEROKEE_BRAVE, CHEROKEE_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(CHEROKEE_BRAVE_ID, false);
        fog.verifyNoNativeSettlementKnown(CHEROKEE_CAPITAL);
        fog.verifyStatusBarOmits("Native brave");
        fog.verifyStatusBarOmits("Cherokee");
        fog.saveMapEvidence("TC_115-braves.png");

        // End the human turn: the national round finishes, the natives act in
        // nation order before France and the human, and time advances once.
        natives.observeCallbacks();
        turns.observeTurnOrder();
        game.nextTurnAndCloseDialogs();
        turns.verifyTurnOrder(ORDER);
        natives.verifyNativeCallbacksOncePerRound("AZTEC", "INCA", "CHEROKEE");
        audit.verifyCurrentTurn(19);
        audit.verifyDisplayedDate("Year 1509 AD");

        // Each native turn prepared its braves once. The content braves with a
        // home spent the whole allowance on one home-patrol step; the homeless
        // survivor has no home to patrol and keeps its full movement.
        natives.verifyBraveCount(4);
        natives.verifyBravePatrolledTo(AZTEC_BRAVE_ID, AZTEC_PATROL);
        natives.verifyBravePrepared(AZTEC_SURVIVOR_ID);
        natives.verifyBraveLocation(AZTEC_SURVIVOR_ID, AZTEC_SURVIVOR);
        natives.verifyBravePatrolledTo(INCA_BRAVE_ID, INCA_PATROL);
        natives.verifyBravePatrolledTo(CHEROKEE_BRAVE_ID, CHEROKEE_PATROL);
        final JsonElement prepared = turns.readTurnState();

        // The same identities stay known and nothing hidden leaks: the survivor
        // in sight is still named, while the Aztec brave has patrolled out of
        // Dutch sight and is no longer shown, though its learned home stays
        // known. The Inca brave stays hidden on its new square.
        fog.hover(AZTEC_SURVIVOR);
        natives.verifyBraveSeen(AZTEC_SURVIVOR, AZTEC_SURVIVOR_ID, true);
        fog.verifyStatusBarContains("Native brave: Aztec, homeless.");
        fog.hover(AZTEC_PATROL);
        natives.verifyBraveSeen(AZTEC_PATROL, AZTEC_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(AZTEC_BRAVE_ID, true);
        fog.verifyStatusBarOmits("Native brave");
        fog.hover(INCA_PATROL);
        natives.verifyBraveSeen(INCA_PATROL, INCA_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(INCA_BRAVE_ID, false);
        fog.verifyStatusBarOmits("Native brave");
        fog.verifyStatusBarOmits("Inca");

        // Continue from disk restores the prepared position exactly: no
        // brave is duplicated and nobody is prepared or skipped again.
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        turns.verifyTurnState(prepared);
        fog.centerFrontier();
        audit.verifyCurrentTurn(19);
        audit.verifyDisplayedDate("Year 1509 AD");
        // Continue neither prepares nor patrols again: the spent braves keep
        // their squares with no movement, the survivor keeps its full one.
        natives.verifyBraveCount(4);
        natives.verifyBravePatrolledTo(AZTEC_BRAVE_ID, AZTEC_PATROL);
        natives.verifyBravePatrolledTo(CHEROKEE_BRAVE_ID, CHEROKEE_PATROL);
        natives.verifyBravePrepared(AZTEC_SURVIVOR_ID);
        fog.hover(AZTEC_PATROL);
        natives.verifyBraveSeen(AZTEC_PATROL, AZTEC_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(AZTEC_BRAVE_ID, true);
        fog.verifyStatusBarOmits("Native brave");
        fog.hover(AZTEC_SURVIVOR);
        fog.verifyStatusBarContains("Native brave: Aztec, homeless.");
        fog.hover(CHEROKEE_PATROL);
        natives.verifyBraveSeen(CHEROKEE_PATROL, CHEROKEE_BRAVE_ID, false);
        natives.verifyBraveHomeKnown(CHEROKEE_BRAVE_ID, false);
        fog.verifyStatusBarOmits("Cherokee");

        // Another full round repeats the same order and advances time once.
        natives.observeCallbacks();
        turns.observeTurnOrder();
        game.nextTurnAndCloseDialogs();
        turns.verifyTurnOrder(ORDER);
        natives.verifyNativeCallbacksOncePerRound("AZTEC", "INCA", "CHEROKEE");
        audit.verifyCurrentTurn(20);
        audit.verifyDisplayedDate("Year 1510 AD");
        natives.verifyBraveCount(4);
    }

}
