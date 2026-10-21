package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Native settlements are learned only by sight. Their nation and capital status
 * stay known after the Dutch lose sight of them and across Continue, and the
 * hidden Inca capital is never disclosed.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_112_F103_native_settlement_identity_test
        extends AbstractMicroColTest {

    /** Aztec capital next to the Dutch colony: in sight at the start. */
    private static final Location AZTEC_CAPITAL = Location.of(23, 15);
    /** Cherokee capital seen from the frigate at the coast. */
    private static final Location CHEROKEE_CAPITAL = Location.of(30, 18);
    /** Cherokee ordinary village seen from the same coast. */
    private static final Location CHEROKEE_VILLAGE = Location.of(30, 19);
    /** Inca capital far inland, never inside Dutch sight. */
    private static final Location INCA_CAPITAL = Location.of(15, 22);

    private static final int FRIGATE_ID = 3;
    private static final Location FRIGATE_START = Location.of(25, 16);
    private static final Location FRIGATE_COAST = Location.of(28, 18);
    /** Three squares from the Cherokee sites: outside the frigate's sight. */
    private static final Location FRIGATE_OFFSHORE = Location.of(27, 18);

    private static final Color AZTEC_FILL = Color.web("#2A9D8F");
    private static final Color CHEROKEE_FILL = Color.web("#588157");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F103-native-settlement-identity.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_identity_is_learned_by_sight_and_kept_after_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();

        // The Aztec capital is inside the colony's sight, so it is named.
        fog.hover(AZTEC_CAPITAL);
        fog.verifyNativeSettlementCurrentlyVisible(AZTEC_CAPITAL, true);
        fog.verifyNativeSettlementKnown(AZTEC_CAPITAL, NativeNation.AZTEC,
                true);
        fog.verifyStatusBarContains("Native capital: Aztec, city.");
        fog.verifyNativeSettlementPixels(AZTEC_CAPITAL, AZTEC_FILL, true);

        // The Cherokee sites are beyond sight: nothing about them is known.
        fog.hover(CHEROKEE_CAPITAL);
        fog.verifyNoNativeSettlementKnown(CHEROKEE_CAPITAL);
        fog.verifyStatusBarOmits("Cherokee");

        // The Inca capital stays hidden for the whole scenario.
        fog.hover(INCA_CAPITAL);
        fog.verifyNoNativeSettlementKnown(INCA_CAPITAL);
        fog.verifyStatusBarOmits("Inca");

        // The frigate reaches the coast: both Cherokee sites are learned.
        fog.move(FRIGATE_START, FRIGATE_COAST, FRIGATE_ID);
        fog.verifyNativeSettlementCurrentlyVisible(CHEROKEE_CAPITAL, true);
        fog.hover(CHEROKEE_CAPITAL);
        fog.verifyNativeSettlementKnown(CHEROKEE_CAPITAL, NativeNation.CHEROKEE,
                true);
        fog.verifyStatusBarContains("Native capital: Cherokee, village.");
        fog.verifyNativeSettlementPixels(CHEROKEE_CAPITAL, CHEROKEE_FILL, true);
        fog.hover(CHEROKEE_VILLAGE);
        fog.verifyNativeSettlementKnown(CHEROKEE_VILLAGE, NativeNation.CHEROKEE,
                false);
        fog.verifyStatusBarContains("Native settlement: Cherokee, village.");
        fog.verifyNativeSettlementPixels(CHEROKEE_VILLAGE, CHEROKEE_FILL,
                false);
        fog.verifyNoNativeSettlementKnown(INCA_CAPITAL);
        fog.saveMapEvidence("TC_112-coast.png");

        // The frigate sails away: the Cherokee capital is no longer in sight,
        // yet its public identity stays known and named.
        fog.move(FRIGATE_COAST, FRIGATE_OFFSHORE, FRIGATE_ID);
        fog.verifyNativeSettlementCurrentlyVisible(CHEROKEE_CAPITAL, false);
        fog.hover(CHEROKEE_CAPITAL);
        fog.verifyNativeSettlementKnown(CHEROKEE_CAPITAL, NativeNation.CHEROKEE,
                true);
        fog.verifyStatusBarContains("Native capital: Cherokee, village.");

        // Continue from disk keeps the learned identities and hides the Inca.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fog.centerFrontier();
        fog.verifyNativeSettlementCurrentlyVisible(CHEROKEE_CAPITAL, false);
        fog.hover(CHEROKEE_CAPITAL);
        fog.verifyNativeSettlementKnown(CHEROKEE_CAPITAL, NativeNation.CHEROKEE,
                true);
        fog.verifyStatusBarContains("Native capital: Cherokee, village.");
        fog.hover(CHEROKEE_VILLAGE);
        fog.verifyNativeSettlementKnown(CHEROKEE_VILLAGE, NativeNation.CHEROKEE,
                false);
        fog.verifyStatusBarContains("Native settlement: Cherokee, village.");
        fog.hover(INCA_CAPITAL);
        fog.verifyNoNativeSettlementKnown(INCA_CAPITAL);
        fog.verifyStatusBarOmits("Inca");
    }
}
