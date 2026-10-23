package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.BaseScorePage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The base score preview (R-0195) reads the live nation from the statistics
 * screen. The prepared Dutch treasury holds 4400 gold, so the treasury shows 4
 * points. Hiring a specialist in Europe moves the population by four points and
 * the treasury by two, from 4400 to 2200 gold, while the fathers and national
 * support stay. Reopening keeps the same values, and Save and Continue keep
 * them too. The preview is labelled as the base score, not as the final ranked
 * score.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_160_F195_base_score_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** The specialist bought in Europe is on the pier with this unit ID. */
    private static final int HIRED_ID = 103;

    /** The treasury points before hiring: 4400 gold. */
    private static final long GOLD_POINTS_BEFORE = 4;

    /** The treasury points after the 2200 gold price: 2200 gold. */
    private static final long GOLD_POINTS_AFTER = 2;

    /** A hired specialist adds four population points. */
    private static final long EXPERT_POINTS = 4;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_hiring_a_specialist_changes_the_preview_and_continue_keeps_it()
            throws Exception {
        // The preview opens from the game and shows the treasury points of the
        // starting gold. Reopening after closing shows the same values.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final BaseScorePage base = new BaseScorePage(getContext());
        base.open();
        base.verifyTabSelected();
        base.verifyBaseNotFinal();
        base.verifyTreasuryPoints(GOLD_POINTS_BEFORE);
        base.verifySubtotalIsSumOfComponents();
        base.rememberScore();
        game = base.close();
        base.open();
        base.verifyRememberedScoreKept();
        game = base.close();

        // The player hires a specialist in Europe with a legal purchase. The
        // specialist and the 2200 gold price are visible in the Europe pier.
        final EuropePortScreen europe = game.openEuroperPort();
        final ExpertPurchasePage purchase = new ExpertPurchasePage(
                getContext());
        europe.openBuyUnitScreen();
        purchase.verifyOffer(true, "");
        purchase.confirm();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(HIRED_ID));
        game = europe.close();

        // The preview now counts the specialist and the lower treasury; the
        // subtotal follows the components.
        base.open();
        base.verifyChangeFromRemembered(EXPERT_POINTS,
                GOLD_POINTS_AFTER - GOLD_POINTS_BEFORE);
        base.verifyTreasuryPoints(GOLD_POINTS_AFTER);
        base.verifySubtotalIsSumOfComponents();
        base.rememberScore();
        game = base.close();

        // Save and Continue keep the same preview values without any new
        // payment or unit.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        base.open();
        base.verifyRememberedScoreKept();
        base.verifyTreasuryPoints(GOLD_POINTS_AFTER);
        base.close();
    }
}
