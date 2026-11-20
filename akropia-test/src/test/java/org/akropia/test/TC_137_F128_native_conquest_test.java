package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.FogPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.NativeConquestPage;
import org.akropia.page.NativeTeachingPage;
import org.akropia.page.NativeVisitPage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Attack and conquest of a defended native settlement (R-0174). A Dutch soldier
 * beside the Aztec capital chooses the attack in the chooser, reads the actual
 * defenders, the settlement's defense and the relation consequence, and
 * confirms. Winning against both braves removes the settlement from the map and
 * from the known settlements, moves the soldier onto its square, rehomes the
 * outside brave as homeless, raises the tribe's anger once and records the
 * conquest once. Losing the first battle leaves the settlement and both
 * defenders and costs the soldier its weapons. An unarmed colonist is refused,
 * backing out changes nothing, and Save and Continue never repeat an outcome. A
 * saved notice of a pupil lost with a conquered settlement is shown in the turn
 * report, kept once by Continue and retired at the end of the turn. The role
 * matrix, the exact defense table, the Spanish bonus, the stale terms, the
 * rehoming ties, the claims and the released or lost pupils and missionaries
 * are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_137_F128_native_conquest_test extends AbstractAkropiaTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.akropia");

    /** The Aztec capital, learned by the Dutch, defended by two braves. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Dutch soldier north of the capital. */
    private static final Location SOLDIER_AT = Location.of(23, 14);

    /** Unarmed Dutch colonist west of the capital. */
    private static final Location COLONIST_AT = Location.of(22, 15);

    /** Two braves defend the square. */
    private static final int DEFENDERS = 2;

    /** Anger of a conquered capital: (20 + 20) twice, from zero. */
    private static final int CONQUEST_ANGER = 2;

    /** Anger of a lost attack on the capital: 20 twice, from zero. */
    private static final int DEFEAT_ANGER = 2;

    private static final String AVAILABLE_EN = "Attack settlement: Available now.";

    private static final String ROLE_EN = "Attack settlement: Your unit's role cannot do this.";

    private static final String LINE_EN = "settlement with the armed unit you chose.";

    private static final String DEFENDERS_EN = "Defenders on the square: 2. Settlement defense: +75%. First battle: your strength 3 against";

    private static final String NO_SPAIN_EN = "No Spanish attack bonus applies.";

    private static final String RELATIONS_EN = "An attack lowers this nation’s favour towards you by 3, whoever wins.";

    private static final String CONQUERED_EN = "You overcame 2 defender(s) and conquered the settlement.";

    private static final String PROMOTED_EN = "was promoted to";

    private static final String REPELLED_EN = "The attack ended after 0 defender(s) were overcome. The settlement stands.";

    private static final String LOST_EN = "Your attacking unit lost its battle and was destroyed or left without its weapons.";

    private static final String AVAILABLE_CS = "Zaútočit na sídlo: Nyní k dispozici.";

    private static final String LINE_CS = "ozbrojenou jednotkou, kterou jste zvolili.";

    private static final String DEFENDERS_CS = "Obránců na poli: 2. Obrana sídla: +75 %. První bitva: vaše síla 3 proti";

    private static final String NO_SPAIN_CS = "Bonus Milétu k útoku neplatí.";

    private static final String RELATIONS_CS = "Útok sníží přízeň tohoto národa vůči vám o 3, ať zvítězí kdokoli.";

    private static final String CONQUERED_CS = "Přemohli jste 2 obránce(ů) a sídlo jste dobyli.";

    private static final String PROMOTED_CS = "byla povýšena";

    /** Fragment of the saved pupil loss notice in the turn report. */
    private static final String LOSS_EN = "who was learning in a conquered settlement of the";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_soldier_overcomes_both_defenders_and_conquers_the_settlement_once()
            throws Exception {
        // Load the native position with the soldier beside the capital and two
        // braves on its square.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final NativeTeachingPage settlements = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS);
        fog.centerFrontier();
        conquest.verifyDefenders(CAPITAL, DEFENDERS);
        settlements.openSettlements();
        conquest.verifySettlementListed(CAPITAL, true);
        settlements.closeSettlements();

        // The chooser offers the attack as its own choice, available now.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);

        // The confirmation names the actual defenders, the settlement's
        // defense, the missing Spanish bonus and the relation consequence.
        visit.verifyChooserClosed();
        conquest.verifyAttackLine(LINE_EN);
        conquest.verifyAttackDefenders(DEFENDERS_EN);
        conquest.verifyAttackSpanish(NO_SPAIN_EN);
        conquest.verifyAttackRelations(RELATIONS_EN);
        conquest.verifyAttackFits(900, 750);
        conquest.captureAttack("native-conquest-confirm-en.png");

        // Confirm: both braves are overcome one battle at a time and the
        // result names the promotion the soldier earned.
        conquest.confirmAttack();
        conquest.verifyAttackClosed();
        conquest.verifyResultMessage(CONQUERED_EN);
        conquest.verifyResultMessage(PROMOTED_EN);
        conquest.captureResult("native-conquest-outcome-en.png");
        conquest.closeAttackResult();
        visit.verifyChooserClosed();

        // The settlement is gone from the map and the known list, the soldier
        // stands on its square, the outside brave has no home any more, the
        // tribe's anger rose once and the conquest is recorded once.
        conquest.verifySettlementRemoved(CAPITAL);
        conquest.verifyDefenders(CAPITAL, 0);
        conquest.verifyUnitAt(NativeConquestPage.SOLDIER_ID, CAPITAL);
        conquest.verifyBraveHomeless(NativeConquestPage.EXTERNAL_BRAVE_ID);
        conquest.verifyFavour(CONQUEST_ANGER);
        conquest.verifyConquestRecords(1);
        settlements.openSettlements();
        conquest.verifySettlementListed(CAPITAL, false);
        settlements.closeSettlements();
        fog.saveMapEvidence("native-conquest-map-en.png");

        // Save and Continue do not bring the settlement back and do not repeat
        // the raise or the record.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        conquest.verifySettlementRemoved(CAPITAL);
        conquest.verifyUnitAt(NativeConquestPage.SOLDIER_ID, CAPITAL);
        conquest.verifyBraveHomeless(NativeConquestPage.EXTERNAL_BRAVE_ID);
        conquest.verifyFavour(CONQUEST_ANGER);
        conquest.verifyConquestRecords(1);
    }

    @Test
    @Tag("local")
    void TC_02_soldier_loses_the_first_battle_and_the_settlement_stands()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.defeatState(),
                DEFENDERS);
        fog.centerFrontier();

        // The player confirms the attack through the real chooser.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.confirmAttack();
        conquest.verifyResultMessage(REPELLED_EN);
        conquest.verifyResultMessage(LOST_EN);
        conquest.captureResult("native-conquest-defeat-en.png");
        conquest.closeAttackResult();

        // The chooser returns on the real state: the disarmed soldier is now
        // refused, and nothing of the settlement changed.
        visit.verifyChooserClosed();
        conquest.verifySettlementStands(CAPITAL);
        conquest.verifyDefenders(CAPITAL, DEFENDERS);
        conquest.verifyUnitAt(NativeConquestPage.SOLDIER_ID, SOLDIER_AT);
        conquest.verifyDisarmed(NativeConquestPage.SOLDIER_ID);
        conquest.verifyBraveHome(NativeConquestPage.EXTERNAL_BRAVE_ID, 0);
        conquest.verifyFavour(DEFEAT_ANGER);
        conquest.verifyConquestRecords(0);

        // Save and Continue keep the same state without a second raise.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        conquest.verifySettlementStands(CAPITAL);
        conquest.verifyDefenders(CAPITAL, DEFENDERS);
        conquest.verifyDisarmed(NativeConquestPage.SOLDIER_ID);
        conquest.verifyFavour(DEFEAT_ANGER);
        conquest.verifyConquestRecords(0);
    }

    @Test
    @Tag("local")
    void TC_03_unarmed_colonist_is_refused_and_nothing_changes()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS);
        fog.centerFrontier();
        conquest.rememberWorld();

        // The colonist sees the refusal line and no attack button.
        visit.enterSettlement(COLONIST_AT, CAPITAL);
        visit.verifyChooserClosed();
        conquest.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_04_backing_out_of_the_confirmation_changes_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS);
        fog.centerFrontier();
        conquest.rememberWorld();

        // Back returns to the chooser, which still offers the attack, and
        // Escape then closes it. No draw, movement or anger changed.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.verifyAttackDefenders(DEFENDERS_EN);
        conquest.backFromAttack();
        conquest.verifyAttackClosed();
        visit.verifyChooserClosed();
        conquest.verifyWorldUnchanged();
        conquest.verifySettlementStands(CAPITAL);
        conquest.verifyDefenders(CAPITAL, DEFENDERS);
    }

    @Test
    @Tag("local")
    void TC_05_czech_confirmation_and_outcome_fit_the_dialogs()
            throws Exception {
        // Switch to Czech, load the native position and the initial fixture.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS);
        fog.centerFrontier();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.verifyAttackLine(LINE_CS);
        conquest.verifyAttackDefenders(DEFENDERS_CS);
        conquest.verifyAttackSpanish(NO_SPAIN_CS);
        conquest.verifyAttackRelations(RELATIONS_CS);
        conquest.verifyAttackFits(900, 750);
        conquest.captureAttack("native-conquest-confirm-cs.png");

        conquest.confirmAttack();
        conquest.verifyResultMessage(CONQUERED_CS);
        conquest.verifyResultMessage(PROMOTED_CS);
        conquest.captureResult("native-conquest-outcome-cs.png");
        conquest.closeAttackResult();
        conquest.verifySettlementRemoved(CAPITAL);
        fog.saveMapEvidence("native-conquest-map-cs.png");
    }

}
