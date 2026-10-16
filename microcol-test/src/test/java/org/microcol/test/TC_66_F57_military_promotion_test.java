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

/** Promotion feedback, retained specialty and the next actual combat review. */
@ExtendWith(ApplicationExtension.class)
public class TC_66_F57_military_promotion_test extends AbstractMicroColTest {
    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location EXPERT = Location.of(18, 12);
    private static final Location DEFENDER = Location.of(17, 11);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F57-military-promotion.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_then_promote_and_continue_as_veteran() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        MilitaryPromotionPage rank = new MilitaryPromotionPage(getContext());
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.cancel();
        attack.verifyGameplayUnchanged();
        // Saved seed 7 chooses an ordinary victory and the 25-percent promotion
        // branch.
        new NavalBattlePage(getContext()).chooseSavedSeed(7);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.confirm();
        attack.verifyEquipmentOutcome(0, 6, false, false, SOLDIER,
                "was promoted to veteran");
        rank.verifyRank(0, UnitType.COLONIST, true, false);
        rank.captureDialog("statusBarLabel", "promotion-english.png");
        rank.verifyPromotionFeedbackVisible();
        rank.inspect(SOLDIER);
        rank.verifyVisibleRank(0, "Military status: Veteran");
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        rank.verifyRank(0, UnitType.COLONIST, true, false);
        // Restore only movement and opponent equipment to inspect the next
        // battle's saved rank.
        rank.prepareNextReview(0);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.verifyStrengths("Base: 3\nAttack: +50%\nFinal strength: 4.5",
                "Base: 2\nFinal strength: 2", "Attacker win chance: 69.23%");
        attack.cancel();
    }

    @Test
    @Tag("local")
    void TC_02_win_without_promotion_keeps_ordinary_status() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        new NavalBattlePage(getContext()).chooseSavedSeed(3);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.confirm();
        attack.verifyEquipmentOutcome(0, 6, false, false, SOLDIER,
                "lost its muskets");
        new MilitaryPromotionPage(getContext()).verifyRank(0, UnitType.COLONIST,
                false, false);
    }

    @Test
    @Tag("local")
    void TC_03_czech_washington_preserves_expert_and_mounted_rank()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        MilitaryPromotionPage rank = new MilitaryPromotionPage(getContext());
        rank.loadWashingtonFixture();
        AttackPage attack = new AttackPage(getContext());
        // The same nonpromoting ordinary seed promotes with Washington, without
        // erasing the skill.
        new NavalBattlePage(getContext()).chooseSavedSeed(3);
        attack.chooseAttack(EXPERT, DEFENDER);
        attack.confirm();
        attack.verifyEquipmentOutcome(2, 6, false, false, EXPERT,
                "byla povýšena: veterán");
        rank.verifyRank(2, UnitType.MASTER_BLACKSMITH, true, true);
        rank.captureDialog("statusBarLabel", "promotion-czech.png");
        rank.verifyPromotionFeedbackVisible();
        rank.inspect(EXPERT);
        rank.verifyVisibleRank(2, "Vojenská hodnost: veterán");
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        rank.verifyRank(2, UnitType.MASTER_BLACKSMITH, true, true);
        rank.prepareNextReview(2);
        attack.chooseAttack(EXPERT, DEFENDER);
        attack.verifyStrengths("Základ: 4.5\nÚtok: +50%\nVýsledná síla: 6.75",
                "Základ: 2\nVýsledná síla: 2",
                "Šance útočníka na vítězství: 77.14%");
        attack.cancel();
    }
}
