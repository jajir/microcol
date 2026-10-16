package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Naval review, resolved cargo losses and persistence through the player's UI.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_64_F55_naval_combat_test extends AbstractMicroColTest {
    private static final Location ATTACKER = Location.of(20, 10);
    private static final Location DEFENDER = Location.of(21, 10);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F55-naval-combat.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_then_sink_and_keep_prize_after_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        // Review states the actual ship strengths before any cargo or war
        // mutation.
        attack.rememberWorld();
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyReview("Frigate", "Galleon", "declares war", 0, 6);
        attack.verifyStrengths("Base: 16\nAttack: +50%\nFinal strength: 24",
                "Base: 10\nFinal strength: 10", "Attacker win chance: 70.59%");
        attack.cancel();
        attack.verifyGameplayUnchanged();
        // Saved draws choose victory and sinking; only fitting goods are
        // prizes.
        naval.chooseSavedSeed(7);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.confirm();
        naval.verifyOutcomeText("#6 sank.", "captured: 60 Fur, 7 Ore.",
                "Lost passengers: 1. Lost goods: 150 Silver.");
        naval.captureDialog("navalBattleOutcome", "naval-sinking-english.png");
        naval.closeOutcome();
        naval.verifyAttackerPrizeAndSinking();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyAttackerPrizeAndSinking();
    }

    @Test
    @Tag("local")
    void TC_02_czech_defeat_preserves_passenger_and_saved_repair()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        // The second saved draw damages the losing ship rather than sinking it.
        naval.chooseSavedSeed(22);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.confirm();
        naval.verifyOutcomeText("poškozena", "Evropa", "95",
                "Pasažéři na palubě: 1.");
        naval.captureDialog("navalBattleOutcome", "naval-damage-czech.png");
        naval.closeOutcome();
        naval.verifyAttackerDamaged();
        EuropePortScreen europe = GamePage.of(getContext())
                .openEuroperPort("Evropa");
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        repairs.open();
        naval.verifyRepairOverview(
                "frigata #0 — Evropa\nPřeprava; do zprovoznění zbývá tahů vlastníka: 3");
        repairs.close();
        europe.close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyAttackerDamaged();
    }

    @Test
    @Tag("local")
    void TC_03_changed_cargo_rejects_without_consuming_battle()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        attack.chooseAttack(ATTACKER, DEFENDER);
        // Simulate a live state change after the player saw the cargo-dependent
        // review.
        naval.changeReviewedCargo();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.closeMessage();
        attack.verifyGameplayUnchanged();
    }
}
