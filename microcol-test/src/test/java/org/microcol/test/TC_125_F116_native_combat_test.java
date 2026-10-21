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
import org.microcol.page.AttackPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeCombatPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A Dutch soldier attacks native braves beside New Amsterdam. The review shows
 * the shared strength breakdown with the native ambush of the defender's
 * square, cancelling changes nothing, and a confirmed battle follows the saved
 * random draw: a defeated brave is destroyed with its equipment, a defeated
 * soldier loses its muskets, and the native relation effect is recorded once
 * and survives Continue. Native attackers and the fates of unarmed Europeans
 * are checked in the model tests until native raids exist in play.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_125_F116_native_combat_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** Armed Dutch soldier added by the initial fixture. */
    private static final int SOLDIER_ID = 300;
    /** Soldier east of the forest survivor, beside the colony. */
    private static final Location FOREST_SOLDIER = Location.of(25, 15);
    /** Homeless Aztec brave in the forest north of the colony. */
    private static final int SURVIVOR_ID = 201;
    private static final Location SURVIVOR = Location.of(24, 15);
    /** Soldier on open grass south of the hill brave. */
    private static final Location HILL_SOLDIER = Location.of(23, 17);
    /** Aztec brave on a hill, home is the Aztec capital. */
    private static final int HILL_BRAVE_ID = 200;
    private static final Location HILL_BRAVE = Location.of(23, 16);

    /** Soldier 3 against brave 1 with the forest ambush of 50%. */
    private static final double FOREST_ODDS = 3.0 / 4.5;
    /** Soldier 3 against brave 1 with the hill ambush of 100%. */
    private static final double HILL_ODDS = 3.0 / 5.0;

    private static final String LOSS_EN = "A defeated native brave is destroyed together with its equipment. A defeated unarmed colonist dies and is not captured. An armed European loses horses or muskets; artillery is damaged first. Both participants spend their remaining movement.";
    private static final String ANGER_EN = "Attacking a native brave raises the anger of its nation toward you, whoever wins.";
    private static final String LOSS_CS = "Poražený domorodý bojovník je zničen i s výstrojí. Poražený neozbrojený kolonista zahyne a nebude zajat. Ozbrojený Evropan ztratí koně nebo muškety; dělo se nejprve poškodí. Oba účastníci spotřebují zbývající pohyb.";
    private static final String ANGER_CS = "Útok na domorodého bojovníka zvýší hněv jeho kmene vůči vám, ať zvítězí kdokoli.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_soldier_cancels_then_defeats_forest_brave_and_continue_keeps_it()
            throws Exception {
        // Load the native position, then the initial fixture whose saved draw
        // lets the soldier win.
        WelcomePage.of(getContext()).loadGame();
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        combat.loadFixture(FIXTURE, SOLDIER_ID, FOREST_SOLDIER, FOREST_ODDS,
                true);
        fog.centerFrontier();

        // The review explains the brave's forest ambush and the loser fates
        // before anything happens.
        attack.rememberWorld();
        attack.chooseAttack(FOREST_SOLDIER, SURVIVOR);
        combat.verifyReview("Free Colonist", "Native brave", SOLDIER_ID,
                SURVIVOR_ID);
        attack.verifyStrengths("Base: 2\nAttack: +50%\nFinal strength: 3",
                "Base: 1\nNative ambush: +50%\nFinal strength: 1.5",
                "Attacker win chance: 66.67%");
        combat.verifyNativeNotes(LOSS_EN, ANGER_EN);
        combat.verifyDialogFits(900, 750);
        attack.captureDialog("fightReview", "native-combat-preview-en.png");

        // Cancelling leaves units, movement, random state and relations alone.
        attack.cancel();
        attack.verifyGameplayUnchanged();
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);
        combat.verifyRelationOutcomeCount(0);

        // Confirming the same attack lets the saved draw decide: the brave is
        // destroyed with its equipment and the soldier takes the square.
        attack.chooseAttack(FOREST_SOLDIER, SURVIVOR);
        attack.confirm();
        combat.verifyBraveDestroyed(SURVIVOR_ID, SOLDIER_ID, SURVIVOR);
        combat.verifyFeedback(
                "a native brave was destroyed together with its equipment");
        fog.saveMapEvidence("native-combat-result-en.png");

        // The attack angered the Aztecs once, whoever it was fought against.
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        combat.verifyRelationOutcomeCount(1);

        // Continue from disk restores the same position and the single effect.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        combat.verifyBraveDestroyed(SURVIVOR_ID, SOLDIER_ID, SURVIVOR);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        combat.verifyRelationOutcomeCount(1);
    }

    @Test
    @Tag("local")
    void TC_02_czech_review_of_a_hill_brave_and_cancel() throws Exception {
        // Switch to Czech, load the native position and the initial fixture.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        combat.loadFixture(FIXTURE, SOLDIER_ID, HILL_SOLDIER, HILL_ODDS, true);
        fog.centerFrontier();

        // The Czech review shows the hill ambush of 100% and the loser fates.
        attack.rememberWorld();
        attack.chooseAttack(HILL_SOLDIER, HILL_BRAVE);
        combat.verifyReview("svobodný kolonista", "Domorodý bojovník",
                SOLDIER_ID, HILL_BRAVE_ID);
        attack.verifyStrengths("Základ: 2\nÚtok: +50%\nVýsledná síla: 3",
                "Základ: 1\nLéčka domorodců: +100%\nVýsledná síla: 2",
                "Šance útočníka na vítězství: 60%");
        combat.verifyNativeNotes(LOSS_CS, ANGER_CS);
        combat.verifyDialogFits(900, 750);
        attack.captureDialog("fightReview", "native-combat-preview-cs.png");

        // Cancelling changes nothing.
        attack.cancel();
        attack.verifyGameplayUnchanged();
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);
        combat.verifyRelationOutcomeCount(0);
    }

    @Test
    @Tag("local")
    void TC_03_soldier_loses_to_hill_brave_and_continue_keeps_the_result()
            throws Exception {
        // Load the native position and an initial fixture whose saved draw
        // lets the brave win.
        WelcomePage.of(getContext()).loadGame();
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final AttackPage attack = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        combat.loadFixture(FIXTURE, SOLDIER_ID, HILL_SOLDIER, HILL_ODDS, false);
        fog.centerFrontier();
        combat.rememberHomeAlarm(HILL_BRAVE_ID);

        // The review shows the hill ambush before the soldier commits.
        attack.chooseAttack(HILL_SOLDIER, HILL_BRAVE);
        combat.verifyReview("Free Colonist", "Native brave", SOLDIER_ID,
                HILL_BRAVE_ID);
        attack.verifyStrengths("Base: 2\nAttack: +50%\nFinal strength: 3",
                "Base: 1\nNative ambush: +100%\nFinal strength: 2",
                "Attacker win chance: 60%");
        attack.confirm();

        // The soldier loses its muskets and survives unarmed; the brave stays.
        combat.verifySoldierLostMuskets(HILL_BRAVE_ID, HILL_BRAVE, SOLDIER_ID,
                HILL_SOLDIER);
        combat.verifyFeedback("lost its muskets");
        fog.saveMapEvidence("native-combat-loss-en.png");

        // The brave's nation and home settlement were angered exactly once.
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        combat.verifyHomeAlarmRaised(10);
        combat.verifyRelationOutcomeCount(1);

        // Continue from disk keeps the result without repeating the effect.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        combat.verifySoldierLostMuskets(HILL_BRAVE_ID, HILL_BRAVE, SOLDIER_ID,
                HILL_SOLDIER);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        combat.verifyHomeAlarmRaised(10);
        combat.verifyRelationOutcomeCount(1);
    }
}
