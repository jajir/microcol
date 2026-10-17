package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.model.UnitType;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Real founder filtering and sequential, saveable earned arrivals. */
@ExtendWith(ApplicationExtension.class)
public class TC_70_F61_brewster_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F61-brewster-arrival.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_actual_brewster_arrival_filters_pool_once_without_changing_people()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        BrewsterPage brewster = new BrewsterPage(getContext());
        // Select Brewster and earn his arrival through ordinary bell
        // production.
        congress.open();
        congress.select(Founder.WILLIAM_BREWSTER);
        congress.choose();
        congress.close();
        game.nextTurnAndCloseDialogs();
        brewster.verifyFilteredArrival();
        brewster.rememberPool();
        // Continue must not filter again or transform an existing servant.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        brewster.verifyFilteredArrival();
        brewster.verifyPoolUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_defer_and_continue_between_two_earned_choices_before_production()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        BrewsterPage brewster = new BrewsterPage(getContext());
        RecruitmentPage recruitment = new RecruitmentPage(getContext());
        brewster.loadChoiceFixture();
        brewster.rememberColony();
        // Arrival preparation pauses before food, production and replenishment.
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        brewster.captureDialog("recruitment-dialog", "brewster-english.png");
        brewster.rememberPool();
        brewster.deferChoice();
        // Reopening the deferred chooser preserves all candidates and crosses.
        recruitment.open();
        brewster.verifyPoolUnchanged();
        brewster.deferChoice();
        brewster.returnToMap();
        brewster.verifyTurnCommandsUnavailable();
        game.exitToMenu();
        brewster.continuePending();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        brewster.verifyPoolUnchanged();
        // One choice uses 100 crosses and only replaces the selected slot.
        recruitment.confirm(1);
        brewster.verifyWaiting(130, 1, List.of(1, 4, 3), "Choose for free");
        brewster.rememberPool();
        brewster.deferChoice();
        brewster.returnToMap();
        game.exitToMenu();
        brewster.continuePending();
        brewster.verifyWaiting(130, 1, List.of(1, 4, 3), "Choose for free");
        brewster.verifyPoolUnchanged();
        // The second choice uses 125; only now do the chapel produces one
        // cross.
        recruitment.confirm(2);
        brewster.finishArrivalReport();
        brewster.verifyCompleted(6, 2, 1, UnitType.MASTER_CARPENTER,
                UnitType.EXPERT_FARMER);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        brewster.verifyCompleted(6, 2, 1, UnitType.MASTER_CARPENTER,
                UnitType.EXPERT_FARMER);
    }

    @Test
    @Tag("local")
    void TC_03_czech_earned_choice_is_keyboard_accessible() throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        BrewsterPage brewster = new BrewsterPage(getContext());
        RecruitmentPage recruitment = new RecruitmentPage(getContext());
        brewster.loadChoiceFixture();
        brewster.rememberColony();
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Vybrat zdarma");
        brewster.captureDialog("recruitment-dialog", "brewster-czech.png");
        // Both earned choices work without mouse-only controls.
        recruitment.confirmWithKeyboard(2);
        brewster.verifyWaiting(130, 1, List.of(1, 2, 4), "Vybrat zdarma");
        recruitment.confirmWithKeyboard(0);
        brewster.finishArrivalReport();
        brewster.verifyCompleted(6, 2, 1, UnitType.EXPERT_FARMER,
                UnitType.COLONIST);
    }
}
