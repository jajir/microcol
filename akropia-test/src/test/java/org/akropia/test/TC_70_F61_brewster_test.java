package org.akropia.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Founder;
import org.akropia.model.UnitType;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Real founder filtering and sequential earned arrivals and discarded
 * unfinished choices.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_70_F61_brewster_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F61-brewster-arrival.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_actual_brewster_arrival_filters_pool_once_without_changing_people()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        BrewsterPage brewster = new BrewsterPage(getContext());
        // Select Brewster and earn his arrival through ordinary owl
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
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openSave();
        saves.saveName("Before earned choices");
        saves.rememberGame();
        // Arrival preparation pauses before food, production and replenishment.
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        brewster.captureDialog("recruitment-dialog", "brewster-english.png");
        brewster.rememberPool();
        brewster.deferChoice();
        // Reopening the deferred chooser preserves all candidates and torches.
        recruitment.open();
        brewster.verifyPoolUnchanged();
        brewster.deferChoice();
        brewster.returnToMap();
        brewster.verifyTurnCommandsUnavailable();
        // Discard the unfinished turn; Continue returns to the saved position.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.action("saveDiscard");
        new FreePlayAuditPage(getContext()).continueGame();
        saves.verifyRestoredGame();
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        brewster.verifyPoolUnchanged();
        // One choice uses 100 torches and only replaces the selected slot.
        recruitment.confirm(1);
        brewster.verifyWaiting(130, 1, List.of(1, 4, 3), "Choose for free");
        brewster.rememberPool();
        brewster.deferChoice();
        brewster.returnToMap();
        // The first choice was not saved either: restore and replay it.
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.action("saveDiscard");
        new FreePlayAuditPage(getContext()).continueGame();
        saves.verifyRestoredGame();
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        recruitment.confirm(1);
        brewster.verifyWaiting(130, 1, List.of(1, 4, 3), "Choose for free");
        // The second choice uses 125; only now do the chapel produces one
        // torch.
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
    void TC_04_earned_choices_come_before_the_tax_demand_and_production()
            throws Exception {
        // The same arrival checkpoint, but the nation is also due for a demand.
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(new TaxDemandPage.Fixture(
                new File("src/test/scenarios/F62-brewster-choice.akropia"),
                "tax-brewster").due());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        BrewsterPage brewster = new BrewsterPage(getContext());
        RecruitmentPage recruitment = new RecruitmentPage(getContext());
        brewster.rememberColony();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openSave();
        saves.saveName("Before earned choices");
        saves.rememberGame();
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        recruitment.confirm(1);
        brewster.verifyWaiting(130, 1, List.of(1, 4, 3), "Choose for free");
        // The second arrival opens the demand, still before any production.
        recruitment.confirm(2);
        taxes.waitForDemand();
        taxes.verifyEarnedArrivals(2, 5);
        taxes.verifyPaused(0, 0);
        // Discarding the unfinished turn also discards both earned choices.
        taxes.defer();
        taxes.returnToMap();
        saves.openMenu();
        saves.chooseMenu("menuMain");
        saves.verifyLeaveConfirmation(true);
        saves.action("saveDiscard");
        new FreePlayAuditPage(getContext()).continueGame();
        saves.verifyRestoredGame();
        brewster.startArrival();
        brewster.verifyWaiting(230, 0, List.of(1, 2, 3), "Choose for free");
        recruitment.confirm(1);
        recruitment.confirm(2);
        taxes.waitForDemand();
        taxes.verifyEarnedArrivals(2, 5);
        taxes.verifyPaused(0, 0);
        taxes.accept();
        brewster.finishArrivalReport();
        taxes.verifyTax(2, java.util.Map.of());
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
