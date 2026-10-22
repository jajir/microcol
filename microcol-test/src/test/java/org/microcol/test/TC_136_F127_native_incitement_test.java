package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeIncitementPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Paying to incite a native tribe against a chosen European power from the
 * chooser (R-0166). A Dutch ordinary missionary sees the incitement as its own
 * choice, the picker offers every other power but never the Dutch, and the
 * exact price and chance are shown before anything is paid. A confirmed success
 * charges the price once, raises the target's anger to 75 and installs a
 * twenty-turn order; a confirmed failure charges the same price and installs
 * nothing. Back changes nothing. Save and Continue keep each outcome without a
 * replay or a second debit. The model rules and the native pursuit are checked
 * in the model and AI tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_136_F127_native_incitement_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native mission tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital that the Dutch missionary may incite. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Dutch ordinary missionary, land square north-west of the capital. */
    private static final Location VISITOR_AT = Location.of(22, 14);

    /**
     * Power the tribe is incited against; the owner name is its display name.
     */
    private static final String TARGET = "France";

    /** Requesting power, which may never be selected as the target. */
    private static final String REQUESTER = "Dutch";

    private static final String LIMIT_EN = "Available now.";

    /** Terms at anger 50: price 750, ordinary missionary, 25%, gold 1000. */
    private static final String TERMS_EN = "Price: 750 gold, paid once whether the attempt succeeds or fails. Your missionary is ordinary, and the tribe is angry toward your nation. Your missions in this tribe: 0. Your chance: 25%. Your gold: 1000.";

    private static final String TERMS_CS = "Cena: 750 zlatých";

    private static final String SUCCESS_EN = "Paid 750 gold; you now have 250. The tribe has turned against France and will pursue it for up to 20 native turns, ending sooner if its anger fades.";

    private static final String FAILED_EN = "Paid 750 gold; you now have 250. The missionary failed to turn the tribe, so its earlier plans stand.";

    /** Gold left after one payment of the fixture price. */
    private static final int GOLD_AFTER_PAYMENT = 250;

    /** Gold before any payment. */
    private static final int GOLD_BEFORE = 1000;

    /** Anger of the tribe toward the target after a success. */
    private static final double SUCCESS_ANGER = 75;

    /** Anger of the tribe toward the requester, unchanged by any attempt. */
    private static final double REQUESTER_ANGER = 50;

    /** Anger toward France in the pursuit fixture; success keeps the higher. */
    private static final double PURSUIT_ANGER = 100;

    /** Lowest anger that keeps an order alive: the hostile band. */
    private static final double HOSTILE_FLOOR = 75;

    /**
     * Brave's distance to the French colonist at the start: beyond radius 2.
     */
    private static final int DISTANT = 3;

    /** One grassland step of a brave's single movement point closes one. */
    private static final int AFTER_ONE_STEP = 2;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_a_successful_incitement_pays_once_sets_the_order_and_keeps_it_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        incite.loadIncitementFixture(FIXTURE,
                NativeIncitementPage.stateWithOutcome(true));
        fog.centerFrontier();

        // The chooser offers the incitement as its own choice, available now.
        visit.enterSettlement(VISITOR_AT, CAPITAL);
        incite.verifyIncitementButtonShown();
        incite.verifyIncitementChoiceLimit(LIMIT_EN);
        visit.verifyChooserFits(900, 750);

        // The picker lists the selectable powers, never the requester, and
        // shows the exact terms of the chosen one before anything is paid.
        incite.startIncitementChoice();
        visit.verifyChooserClosed();
        incite.verifyTargetOffered(TARGET);
        incite.verifyTargetNotOffered(REQUESTER);
        incite.selectTarget(TARGET);
        incite.verifyTerms(TERMS_EN);
        incite.verifyIncitementFits(900, 750);
        incite.captureIncitement("native-incite-en.png");

        // Confirm: the price is paid once, the one draw succeeds, and the
        // target's anger and the order follow.
        incite.confirmIncitement();
        incite.verifyIncitementResultMessage(SUCCESS_EN);
        incite.closeIncitementResult();
        visit.escape();
        visit.verifyChooserClosed();
        incite.verifyGold(GOLD_AFTER_PAYMENT);
        incite.verifyTribalAnger(TARGET, SUCCESS_ANGER);
        incite.verifyTribalAnger(REQUESTER, REQUESTER_ANGER);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);

        // Save and Continue keep the order with its clock and the gold; the
        // payment is not repeated.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        incite.verifyGold(GOLD_AFTER_PAYMENT);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);
    }

    @Test
    @Tag("local")
    void TC_02_a_failed_incitement_pays_but_installs_no_order_and_keeps_anger()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        incite.loadIncitementFixture(FIXTURE,
                NativeIncitementPage.stateWithOutcome(false));
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        incite.startIncitementChoice();
        visit.verifyChooserClosed();
        incite.selectTarget(TARGET);
        incite.confirmIncitement();
        incite.verifyIncitementResultMessage(FAILED_EN);
        incite.closeIncitementResult();
        visit.escape();
        visit.verifyChooserClosed();

        // The price is paid, but neither anger nor the order changes.
        incite.verifyGold(GOLD_AFTER_PAYMENT);
        incite.verifyTribalAnger(TARGET, 0);
        incite.verifyTribalAnger(REQUESTER, REQUESTER_ANGER);
        incite.verifyNoOrder();

        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        incite.verifyGold(GOLD_AFTER_PAYMENT);
        incite.verifyNoOrder();
    }

    @Test
    @Tag("local")
    void TC_03_back_from_the_picker_changes_nothing_and_draws_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        final long state = NativeIncitementPage.stateWithOutcome(true);
        incite.loadIncitementFixture(FIXTURE, state);
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        incite.startIncitementChoice();
        visit.verifyChooserClosed();
        incite.selectTarget(TARGET);
        incite.backFromIncitement();

        // Back leaves gold, anger, the order and the random state as they were;
        // the chooser returns with the incitement still offered.
        incite.verifyGold(GOLD_BEFORE);
        incite.verifyNoOrder();
        incite.verifyCombatRandomState(state);
        incite.verifyIncitementButtonShown();
        visit.escape();
        visit.verifyChooserClosed();
        incite.verifyGold(GOLD_BEFORE);
        incite.verifyTribalAnger(TARGET, 0);
        incite.verifyCombatRandomState(state);
    }

    @Test
    @Tag("local")
    void TC_05_a_paid_order_sends_the_brave_after_the_distant_known_unit_and_continue_keeps_the_progress()
            throws Exception {
        // The fixture gives the Aztecs anger 100 toward France, so the peaceful
        // decay of the first turns cannot end the order, and a French colonist
        // that a sentry lookout makes known, three squares from an Aztec brave.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        incite.loadPursuitFixture(FIXTURE,
                NativeIncitementPage.stateWithOutcome(true));
        fog.centerFrontier();
        incite.verifyBraveAt(NativeIncitementPage.BRAVE_ID,
                NativeIncitementPage.BRAVE_AT);
        incite.verifyBraveDistanceTo(NativeIncitementPage.BRAVE_ID,
                NativeIncitementPage.FRENCH_AT, DISTANT);
        incite.verifyNoOrder();

        // The player pays for the incitement against France through the real
        // chooser and picker, and the draw succeeds.
        visit.enterSettlement(VISITOR_AT, CAPITAL);
        incite.startIncitementChoice();
        visit.verifyChooserClosed();
        incite.verifyTargetOffered(TARGET);
        incite.verifyTargetNotOffered(REQUESTER);
        incite.selectTarget(TARGET);
        incite.verifyTerms(TERMS_EN);
        incite.confirmIncitement();
        incite.verifyIncitementResultMessage(SUCCESS_EN);
        incite.closeIncitementResult();
        visit.escape();
        visit.verifyChooserClosed();
        incite.verifyGold(GOLD_AFTER_PAYMENT);
        incite.verifyOrder(EuropeanNation.FRENCH, 20);
        incite.verifyTribalAnger(TARGET, PURSUIT_ANGER);
        incite.verifyTribalAnger(REQUESTER, REQUESTER_ANGER);

        // The human ends the turn. The Aztecs act first in the next round: the
        // brave takes one step toward the known French colonist beyond the
        // two-square raid radius, and the order has used one native turn.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        incite.verifyBraveDistanceTo(NativeIncitementPage.BRAVE_ID,
                NativeIncitementPage.FRENCH_AT, AFTER_ONE_STEP);
        incite.verifyOrder(EuropeanNation.FRENCH, 19);
        incite.verifyTribalAngerAtLeast(TARGET, HOSTILE_FLOOR);

        // Continue keeps the order with its remaining turns and the brave's
        // progress; neither is replayed nor ticked again.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        incite.verifyBraveDistanceTo(NativeIncitementPage.BRAVE_ID,
                NativeIncitementPage.FRENCH_AT, AFTER_ONE_STEP);
        incite.verifyOrder(EuropeanNation.FRENCH, 19);
    }

    @Test
    @Tag("local")
    void TC_04_czech_incitement_chooser_and_picker_fit() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeIncitementPage incite = new NativeIncitementPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        incite.loadIncitementFixture(FIXTURE,
                NativeIncitementPage.stateWithOutcome(true));
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        visit.verifyChooserFits(900, 750);
        incite.startIncitementChoice();
        incite.selectTarget(TARGET);
        incite.verifyTerms(TERMS_CS);
        incite.verifyIncitementFits(900, 750);
        incite.captureIncitement("native-incite-cs.png");
        incite.backFromIncitement();
        visit.verifyChooserFits(900, 750);
        visit.escape();
        visit.verifyChooserClosed();
    }

}
