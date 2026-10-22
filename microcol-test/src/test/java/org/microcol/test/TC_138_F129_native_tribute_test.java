package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeTributePage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Tribute demand on a defended native settlement (R-0162). A Dutch soldier
 * beside the Aztec capital chooses the demand in the chooser, reads the force,
 * the defenders with the settlement's defense, the model's payment or refusal
 * and the relation consequence, and confirms. Two soldiers against two braves
 * pay the capital's cap of 200 once, while one soldier is refused and still
 * provokes the tribe. Backing out changes nothing, an unarmed colonist gets no
 * demand, a settlement without defenders is paid at its cap as an empty square
 * is open to a demand, an empty treasury pays nothing, and the wait of ten
 * turns is shown for nine turns and offered again after ten. Save and Continue
 * never repeat a payment or a provocation. The exact strengths, equality, the
 * adjacent force, the caps, the stale terms and the saved history are checked
 * in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_138_F129_native_tribute_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, learned by the Dutch, defended by braves. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Dutch soldier north of the capital. */
    private static final Location SOLDIER_AT = Location.of(23, 14);

    /** Unarmed Dutch colonist west of the capital. */
    private static final Location COLONIST_AT = Location.of(22, 15);

    /** Game turn of the saved position and of the cooldown checks. */
    private static final int TURN = 18;

    private static final String AVAILABLE_EN = "Demand tribute: Available now.";

    private static final String ROLE_EN = "Demand tribute: Your unit's role cannot do this.";

    private static final String COOLDOWN_EN = "Demand tribute: This European already demanded tribute here within the last ten turns.";

    private static final String LINE_EN = "with the unit you chose.";

    private static final String FORCE_EN = "Your force: 4,";

    private static final String FORCE_SINGLE_EN = "Your force: 2,";

    private static final String DEFENDERS_EN = "Defenders on the square: 2, with strength 3.5.";

    private static final String NO_DEFENDERS_EN = "Defenders on the square: 0, with strength 0.";

    private static final String DEFENSE_EN = "The settlement's defense bonus is +75%.";

    private static final String PAID_EN = "Paid if your force reaches the defenders: the settlement gives up to 200 gold from its treasury of 2000.";

    private static final String REFUSED_EN = "Refused if your force is below the defenders: no gold is paid.";

    private static final String EMPTY_EN = "Accepted, but the settlement's treasury is empty, so no gold is paid.";

    private static final String RELATIONS_EN = "rise. No second demand to this settlement for ten turns.";

    private static final String RESULT_PAID_EN = "The settlement paid 200 gold.";

    private static final String RESULT_REFUSED_EN = "The settlement refused the tribute. Nothing was paid, but its anger rose.";

    private static final String RESULT_EMPTY_EN = "The settlement paid nothing: its treasury is empty.";

    private static final String AVAILABLE_CS = "Požadovat tribut: Nyní k dispozici.";

    private static final String LINE_CS = "jednotkou, kterou jste zvolili.";

    private static final String FORCE_CS = "Vaše síla: 4,";

    private static final String DEFENSE_CS = "Obranný bonus sídla je +75 %.";

    private static final String PAID_CS = "Při dosažení síly obránců sídlo vydá až 200 zlata z pokladny 2000.";

    private static final String RELATIONS_CS = "Další požadavek na toto sídlo bude možný za deset tahů.";

    private static final String RESULT_PAID_CS = "Sídlo zaplatilo 200 zlata.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_paid_demand_pays_the_capital_cap_once_and_provokes_the_tribe()
            throws Exception {
        // Load the native position with two soldiers beside the capital and two
        // braves on its square; the Dutch hold no earlier demand.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 2, 2, 2000, TURN, null);
        fog.centerFrontier();
        tribute.rememberPurse();

        // The chooser offers the demand as its own choice, available now.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeButtonShown();
        tribute.verifyTributeChoiceLine(AVAILABLE_EN);
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-tribute-chooser-en.png");

        // The confirmation names the force, the defenders, the payment and the
        // relation consequence before anything is paid.
        tribute.startTributeChoice();
        visit.verifyChooserClosed();
        tribute.verifyTributeLine(LINE_EN);
        tribute.verifyTributeForce(FORCE_EN);
        tribute.verifyTributeForce(DEFENDERS_EN);
        tribute.verifyTributeDefense(DEFENSE_EN);
        tribute.verifyTributeOutcome(PAID_EN);
        tribute.verifyTributeRelations(RELATIONS_EN);
        tribute.verifyTributeFits(900, 750);
        tribute.captureTribute("native-tribute-confirm-en.png");

        // Confirm: the capital pays its cap once and the result names the gold.
        tribute.confirmTribute();
        tribute.verifyTributeClosed();
        tribute.verifyResultMessage(RESULT_PAID_EN);
        tribute.captureResult("native-tribute-outcome-en.png");
        tribute.closeTributeResult();

        // The chooser reopens on the actual state: the wait is shown and the
        // demand is no longer offered.
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyPaidExactly(200);
        tribute.verifyRelationsRaisedByTen();
        tribute.verifyTributeRecord(TURN);
        tribute.verifyCombatRandomStateUnchanged();

        // Save and Continue keep the same gold, the same provocation and the
        // same record, and the chooser shows the wait.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        tribute.verifyPaidExactly(200);
        tribute.verifyRelationsRaisedByTen();
        tribute.verifyTributeRecord(TURN);
        fog.centerFrontier();
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyPaidExactly(200);
    }

    @Test
    @Tag("local")
    void TC_02_refused_demand_pays_nothing_and_still_provokes_the_tribe()
            throws Exception {
        // One soldier, force 2, is below the two braves of 3.5.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 1, 2, 2000, TURN, null);
        fog.centerFrontier();
        tribute.rememberPurse();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeChoiceLine(AVAILABLE_EN);
        tribute.startTributeChoice();
        visit.verifyChooserClosed();
        tribute.verifyTributeForce(FORCE_SINGLE_EN);
        tribute.verifyTributeOutcome(REFUSED_EN);
        tribute.verifyTributeRelations(RELATIONS_EN);
        tribute.captureTribute("native-tribute-refused-en.png");

        tribute.confirmTribute();
        tribute.verifyTributeClosed();
        tribute.verifyResultMessage(RESULT_REFUSED_EN);
        tribute.captureResult("native-tribute-refused-outcome-en.png");
        tribute.closeTributeResult();

        // The refusal also starts the wait: the chooser reopens on the actual
        // state and no longer offers the demand.
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyRefusedNothingPaid();
        tribute.verifyRelationsRaisedByTen();
        tribute.verifyTributeRecord(TURN);
        tribute.verifyCombatRandomStateUnchanged();
    }

    @Test
    @Tag("local")
    void TC_03_backing_out_of_the_confirmation_changes_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 2, 2, 2000, TURN, null);
        fog.centerFrontier();
        tribute.rememberWorld();

        // Back returns to the chooser, which still offers the demand, and
        // Escape
        // then closes it. No gold, anger, record or random state changed.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.startTributeChoice();
        tribute.verifyTributeForce(FORCE_EN);
        tribute.backFromTribute();
        tribute.verifyTributeClosed();
        tribute.verifyTributeButtonShown();
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_04_unarmed_colonist_gets_no_demand_and_nothing_changes()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadUnarmedFixture(FIXTURE);
        fog.centerFrontier();
        tribute.rememberWorld();

        // The colonist sees the refusal line and no demand button.
        visit.enterSettlement(COLONIST_AT, CAPITAL);
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(ROLE_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_05_settlement_without_defenders_pays_the_cap_once_and_provokes_the_tribe()
            throws Exception {
        // One soldier and no brave on the square: an empty square has defense
        // zero, so the demand is open, accepted and paid at the capital's cap.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 1, 0, 2000, TURN, null);
        fog.centerFrontier();
        tribute.rememberPurse();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeButtonShown();
        tribute.verifyTributeChoiceLine(AVAILABLE_EN);
        tribute.startTributeChoice();
        visit.verifyChooserClosed();
        tribute.verifyTributeForce(FORCE_SINGLE_EN);
        tribute.verifyTributeForce(NO_DEFENDERS_EN);
        tribute.verifyTributeDefense(DEFENSE_EN);
        tribute.verifyTributeOutcome(PAID_EN);
        tribute.verifyTributeRelations(RELATIONS_EN);
        tribute.captureTribute("native-tribute-no-defenders-en.png");

        // Demand: the capital pays its cap of 200 once; nothing is drawn.
        tribute.confirmTribute();
        tribute.verifyResultMessage(RESULT_PAID_EN);
        tribute.closeTributeResult();

        // The same European waits for ten turns, as with any defended
        // settlement: the chooser reopens on the actual state.
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyPaidExactly(200);
        tribute.verifyRelationsRaisedByTen();
        tribute.verifyTributeRecord(TURN);
        tribute.verifyCombatRandomStateUnchanged();
    }

    @Test
    @Tag("local")
    void TC_06_wait_blocks_the_ninth_turn_and_offers_the_demand_on_the_tenth()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());

        // A demand made on turn 9 is nine turns old on turn 18: still waiting.
        tribute.loadTributeFixture(FIXTURE, 2, 2, 2000, TURN, 9);
        fog.centerFrontier();
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeButtonHidden();
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();

        // On turn 19 the same demand is ten turns old: offered again. Loading
        // the
        // later turn advances nothing.
        tribute.loadTributeFixture(FIXTURE, 2, 2, 2000, TURN + 1, 9);
        fog.centerFrontier();
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeButtonShown();
        tribute.verifyTributeChoiceLine(AVAILABLE_EN);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_07_empty_treasury_is_accepted_and_pays_nothing() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 2, 2, 0, TURN, null);
        fog.centerFrontier();
        tribute.rememberPurse();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.startTributeChoice();
        tribute.verifyTributeOutcome(EMPTY_EN);
        tribute.confirmTribute();
        tribute.verifyResultMessage(RESULT_EMPTY_EN);
        tribute.closeTributeResult();

        // The chooser reopens on the actual state; close it before checking.
        tribute.verifyTributeChoiceLine(COOLDOWN_EN);
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyPaidExactly(0);
        tribute.verifyRelationsRaisedByTen();
        tribute.verifyTributeRecord(TURN);
    }

    @Test
    @Tag("local")
    void TC_08_czech_confirmation_and_result_fit_the_dialogs()
            throws Exception {
        // Switch to Czech, load the tribute position and the initial fixture.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTributePage tribute = new NativeTributePage(getContext());
        final FogPage fog = new FogPage(getContext());
        tribute.loadTributeFixture(FIXTURE, 2, 2, 2000, TURN, null);
        fog.centerFrontier();
        tribute.rememberPurse();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        tribute.verifyTributeChoiceLine(AVAILABLE_CS);
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-tribute-chooser-cs.png");
        tribute.startTributeChoice();
        tribute.verifyTributeLine(LINE_CS);
        tribute.verifyTributeForce(FORCE_CS);
        tribute.verifyTributeDefense(DEFENSE_CS);
        tribute.verifyTributeOutcome(PAID_CS);
        tribute.verifyTributeRelations(RELATIONS_CS);
        tribute.verifyTributeFits(900, 750);
        tribute.captureTribute("native-tribute-confirm-cs.png");

        tribute.confirmTribute();
        tribute.verifyResultMessage(RESULT_PAID_CS);
        tribute.captureResult("native-tribute-outcome-cs.png");
        tribute.closeTributeResult();

        // The chooser reopens on the actual state; close it before checking.
        visit.escape();
        visit.verifyChooserClosed();
        tribute.verifyPaidExactly(200);
    }

}
