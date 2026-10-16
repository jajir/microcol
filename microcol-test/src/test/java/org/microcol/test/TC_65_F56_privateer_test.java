package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Privateer combat never changes the European treaty as a side effect. */
@ExtendWith(ApplicationExtension.class)
public class TC_65_F56_privateer_test extends AbstractMicroColTest {
    private static final Location ATTACKER = Location.of(20, 10);
    private static final Location DEFENDER = Location.of(21, 10);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F56-privateer.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_hidden_advisor_still_reviews_privateer_and_preserves_peace()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        PrivateerPage privateer = new PrivateerPage(getContext());
        // The diplomatic explanation remains mandatory even with the optional
        // advisor hidden.
        attack.configureHiddenAdvisor();
        attack.rememberWorld();
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyReview("Privateer", "Galleon", "does not declare war", 0,
                6);
        attack.captureDialog("fightReview", "privateer-english.png");
        attack.cancel();
        attack.verifyGameplayUnchanged();
        // A legal privateer victory takes fitting cargo without declaring
        // hostilities.
        naval.chooseSavedSeed(7);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.confirm();
        naval.verifyOutcomeText("#6 sank.", "captured: 5 Ore.");
        naval.closeOutcome();
        privateer.verifyPeacefulVictory(UnitType.PRIVATEER, 100);
        attack.rememberWorld();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        privateer.verifyPeacefulVictory(UnitType.PRIVATEER, 100);
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_czech_frigate_can_attack_foreign_privateer_without_war()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        PrivateerPage privateer = new PrivateerPage(getContext());
        privateer.loadCounterattackFixture();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        // The same exception applies against a privateer, with observed
        // ownership preserved.
        naval.chooseSavedSeed(7);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyReview("frigata", "korzárská loď", "nevyhlašuje válku", 0,
                6);
        attack.captureDialog("fightReview", "privateer-czech.png");
        attack.confirm();
        naval.verifyOutcomeText("#6 se potopila.", "7 Železná ruda");
        naval.closeOutcome();
        privateer.verifyPeacefulVictory(UnitType.FRIGATE, 100);
    }

    @Test
    @Tag("local")
    void TC_03_relation_changed_during_review_rejects_without_battle()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(ATTACKER, DEFENDER);
        // A concurrent relation change invalidates the original diplomatic
        // review.
        attack.establishExistingWar();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.closeMessage();
        attack.verifyGameplayUnchanged();
    }
}
