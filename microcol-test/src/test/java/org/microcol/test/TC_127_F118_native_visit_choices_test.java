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
import org.microcol.model.UnitType;
import org.microcol.page.AttackPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeCombatPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Entering a known native settlement opens a chooser before anything else
 * happens. A Dutch ordinary scout, a petty criminal and a soldier each stand
 * beside the Aztec capital and drag onto it, the gesture that would otherwise
 * move or attack there. The chooser names the skill the capital offers to teach
 * and lists every action with the limit that applies to the visitor's role.
 * Teaching is available to the eligible scout and soldier (R-0148); the other
 * admitted actions are still pending. Closing the chooser with Escape or its
 * button leaves the visitor, the braves, the relations and the saved game as
 * they were, and Continue does not bring the chooser back. The role rules, the
 * knowledge and range gates and the seeded choice of the skill are checked in
 * the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_127_F118_native_visit_choices_test
        extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, learned by the Dutch, offering the expert farmer. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Unarmed mounted colonist, the ordinary scout, west of the capital. */
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 15);

    /** Petty criminal south-west of the capital. */
    private static final int CRIMINAL_ID = 301;
    private static final Location CRIMINAL_AT = Location.of(22, 16);

    /** Armed soldier north of the capital. */
    private static final int SOLDIER_ID = 302;
    private static final Location SOLDIER_AT = Location.of(23, 14);

    /** Aztec braves beside the capital that an attack could have hit. */
    private static final int HILL_BRAVE_ID = 200;
    private static final Location HILL_BRAVE = Location.of(23, 16);
    private static final int FOREST_BRAVE_ID = 201;
    private static final Location FOREST_BRAVE = Location.of(24, 15);

    /** Armed role, but the settlement has no defender to attack (R-0174). */
    private static final String NO_DEFENDER_EN = "No defender of this settlement can be attacked from here.";
    private static final String AVAILABLE_EN = "Available now.";
    private static final String ROLE_EN = "Your unit's role cannot do this.";
    private static final String AVAILABLE_CS = "Nyní k dispozici.";
    private static final String ROLE_CS = "Role jednotky to neumožňuje.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_ordinary_scout_enters_settlement_and_escape_changes_nothing()
            throws Exception {
        // Load the native position and the initial fixture with the visitors.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final AttackPage world = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();
        world.rememberWorld();
        visit.rememberMovement(SCOUT_ID);
        visit.verifyOfferedSkill(CAPITAL, UnitType.EXPERT_FARMER);

        // The scout drags onto the capital. The chooser opens instead of a
        // move; the scout may meet the chief and learn, but cannot fight.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChooser("Offers to teach: Expert Farmer.",
                "Visit the chief: " + AVAILABLE_EN,
                "Learn the offered skill: " + AVAILABLE_EN, "Trade: " + ROLE_EN,
                "Demand tribute: " + AVAILABLE_EN,
                "Establish a mission: " + ROLE_EN,
                "Attack settlement: " + ROLE_EN);
        visit.verifyTeachButtonShown();
        visit.verifyCloseButtonFocused();
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-visit-scout-en.png");

        // Escape leaves the scout, braves, relations and saved game alone.
        visit.escape();
        visit.verifyChooserClosed();
        world.verifyGameplayUnchanged();
        visit.verifyVisitorUnmoved(SCOUT_ID, SCOUT_AT);
        visit.verifyBraveStays(HILL_BRAVE_ID, HILL_BRAVE);
        visit.verifyBraveStays(FOREST_BRAVE_ID, FOREST_BRAVE);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);
        combat.verifyRelationOutcomeCount(0);
        visit.verifyOfferedSkill(CAPITAL, UnitType.EXPERT_FARMER);
        fog.saveMapEvidence("native-visit-after-escape-en.png");
    }

    @Test
    @Tag("local")
    void TC_02_criminal_is_refused_every_action_and_ok_closes_the_chooser()
            throws Exception {
        // Load the native position and the initial fixture with the visitors.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final AttackPage world = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();
        world.rememberWorld();
        visit.rememberMovement(CRIMINAL_ID);

        // A petty criminal may not learn from natives and has no other role,
        // so every line explains that its role cannot do the action.
        visit.enterSettlement(CRIMINAL_AT, CAPITAL);
        visit.verifyChooser("Offers to teach: Expert Farmer.",
                "Visit the chief: " + ROLE_EN,
                "Learn the offered skill: " + ROLE_EN, "Trade: " + ROLE_EN,
                "Demand tribute: " + ROLE_EN, "Establish a mission: " + ROLE_EN,
                "Attack settlement: " + ROLE_EN);
        // A criminal has no teaching button, so only the refusal is shown.
        visit.verifyTeachButtonHidden();

        // The button closes the chooser without any change.
        visit.clickOk();
        visit.verifyChooserClosed();
        world.verifyGameplayUnchanged();
        visit.verifyVisitorUnmoved(CRIMINAL_ID, CRIMINAL_AT);
    }

    @Test
    @Tag("local")
    void TC_03_soldier_does_not_attack_and_continue_does_not_reopen_the_chooser()
            throws Exception {
        // Load the native position and the initial fixture with the visitors.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();
        visit.rememberMovement(SOLDIER_ID);

        // The armed soldier drags onto the capital. The tribute demand is open
        // with no defender on the square, the attack has no defender to strike,
        // the soldier is no scout, and no battle or payment is made.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        visit.verifyChooser("Offers to teach: Expert Farmer.",
                "Visit the chief: " + ROLE_EN,
                "Learn the offered skill: " + AVAILABLE_EN, "Trade: " + ROLE_EN,
                "Demand tribute: " + AVAILABLE_EN,
                "Establish a mission: " + ROLE_EN,
                "Attack settlement: " + NO_DEFENDER_EN);
        visit.verifyTeachButtonShown();
        visit.escape();
        visit.verifyChooserClosed();
        visit.verifyVisitorUnmoved(SOLDIER_ID, SOLDIER_AT);
        visit.verifyBraveStays(HILL_BRAVE_ID, HILL_BRAVE);
        visit.verifyBraveStays(FOREST_BRAVE_ID, FOREST_BRAVE);
        combat.verifyRelationOutcomeCount(0);

        // Continue from disk restores the same position, keeps the offered
        // skill, and shows no pending chooser.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.verifyChooserClosed();
        visit.verifyVisitorUnmoved(SOLDIER_ID, SOLDIER_AT);
        visit.verifyBraveStays(HILL_BRAVE_ID, HILL_BRAVE);
        visit.verifyOfferedSkill(CAPITAL, UnitType.EXPERT_FARMER);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);
        combat.verifyRelationOutcomeCount(0);
    }

    @Test
    @Tag("local")
    void TC_04_czech_chooser_of_the_ordinary_scout_and_escape()
            throws Exception {
        // Switch to Czech, load the native position and the initial fixture.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final AttackPage world = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();
        world.rememberWorld();
        visit.rememberMovement(SCOUT_ID);

        // The Czech chooser names the skill and the same role limits.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChooser("Nabízí výuku: zkušený farmář.",
                "Navštívit náčelníka: " + AVAILABLE_CS,
                "Naučit se nabízenou dovednost: " + AVAILABLE_CS,
                "Obchodovat: " + ROLE_CS, "Požadovat tribut: " + AVAILABLE_CS,
                "Založit misi: " + ROLE_CS, "Zaútočit na sídlo: " + ROLE_CS);
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-visit-scout-cs.png");

        // Escape changes nothing.
        visit.escape();
        visit.verifyChooserClosed();
        world.verifyGameplayUnchanged();
        visit.verifyVisitorUnmoved(SCOUT_ID, SCOUT_AT);
        fog.saveMapEvidence("native-visit-after-escape-cs.png");
    }
}
