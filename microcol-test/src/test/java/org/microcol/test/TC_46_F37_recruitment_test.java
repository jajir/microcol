package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceEuropePier;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Three persistent candidates, independent passage prices and actual travel.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_46_F37_recruitment_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F37-recruitment.microcol");

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_recruit_once_and_continue() throws Exception {
        // Recruitment shows three saved people; cancellation must not reroll
        // them.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        String before = recruit.poolSnapshot();
        recruit.open();
        recruit.verifyOffer(List.of("Free Colonist", "Indentured Servant",
                "Petty Criminal"), 200, true, "Pay passage: ", "");
        recruit.captureDialog("recruitment-dialog", "recruitment-english.png");
        recruit.cancel();
        recruit.verifyPool(before, 500);
        // A queued duplicate confirms only once and replaces only slot two.
        recruit.open();
        recruit.confirmTwice(1);
        recruit.verifyCandidateIds(List.of(1, 4, 3), 300);
        recruit.verifyRecruited(103, "INDENTURED_SERVANT", 300,
                PlaceEuropePier.class);
        europe.verifyGold(300);
        String after = recruit.poolSnapshot();
        recruit.open();
        recruit.verifyOffer(null, 220, true, "Pay passage: ", "");
        recruit.cancel();
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        recruit.verifyPool(after, 300);
        recruit.verifyRecruited(103, "INDENTURED_SERVANT", 300,
                PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_02_unaffordable_passage_keeps_candidates() throws Exception {
        // One gold short disables all passage buttons with an explanation.
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(199));
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        String before = recruit.poolSnapshot();
        recruit.open();
        recruit.verifyOffer(
                List.of("Free Colonist", "Indentured Servant",
                        "Petty Criminal"),
                200, false, "Pay passage: ", "Not enough gold.");
        recruit.cancel();
        recruit.verifyPool(before, 199);
    }

    @Test
    @Tag("local")
    void TC_03_other_passage_invalidates_open_quote() throws Exception {
        // A completed passage elsewhere changes the live price before
        // confirmation.
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        recruit.open();
        getContext().getRobot().interact(() -> {
            org.microcol.model.Player player = getModel()
                    .getPlayerByName("Dutch");
            player.recruit(player.getRecruitmentOffers().get(0));
        });
        String changed = recruit.poolSnapshot();
        recruit.confirm(1);
        recruit.verifyChangedWarning();
        recruit.closeChangedWarning();
        recruit.verifyPool(changed, 300);
        recruit.verifyCandidateIds(List.of(4, 2, 3), 300);
        recruit.verifyRecruited(103, "COLONIST", 300, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_04_czech_keyboard_recruit_embarks_and_survives_continue()
            throws Exception {
        // Select Czech, then pay for the last candidate through keyboard
        // traversal.
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        EuropePortScreen europe = settings.goBack().loadGame()
                .openEuroperPort("Evropa");
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        recruit.open();
        recruit.verifyOffer(List.of("svobodný kolonista", "smluvní služebník",
                "drobný trestanec"), 200, true, "Zaplatit cestu: ", "");
        recruit.captureDialog("recruitment-dialog", "recruitment-czech.png");
        recruit.confirmWithKeyboard(2);
        recruit.verifyCandidateIds(List.of(1, 2, 4), 300);
        recruit.verifyRecruited(103, "PETTY_CRIMINAL", 300,
                PlaceEuropePier.class);
        // Board a real ship and keep the same person and pool through saved
        // transit.
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        europe.selectUnitFromPort(0);
        equipment.embark(103);
        equipment.verifyPassenger(103, 102);
        equipment.depart(102);
        recruit.verifyRecruited(103, "PETTY_CRIMINAL", 300,
                PlaceCargoSlot.class);
        String pool = recruit.poolSnapshot();
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        recruit.verifyPool(pool, 300);
        recruit.verifyRecruited(103, "PETTY_CRIMINAL", 300,
                PlaceCargoSlot.class);
        equipment.verifyPassenger(103, 102);
    }

    @Test
    @Tag("local")
    void TC_05_expense_during_offer_does_not_buy_or_replace() throws Exception {
        // The offer was affordable, but a later expense must be checked again.
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        String before = recruit.poolSnapshot();
        recruit.open();
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(199));
        recruit.confirm(0);
        ExpertPurchasePage warning = new ExpertPurchasePage(getContext());
        warning.verifyInsufficientFundsWarning();
        warning.closeWarning();
        recruit.verifyOffer(
                List.of("Free Colonist", "Indentured Servant",
                        "Petty Criminal"),
                200, false, "Pay passage: ", "Not enough gold.");
        recruit.cancel();
        recruit.verifyPool(before, 199);
    }

    @Test
    @Tag("local")
    void TC_06_university_hiring_does_not_consume_candidates()
            throws Exception {
        // The university charges its own price, independently of passage
        // counts.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        String before = recruit.poolSnapshot();
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(3000));
        europe.openBuyUnitScreen();
        new ExpertPurchasePage(getContext()).confirm();
        recruit.verifyPool(before, 800);
        recruit.open();
        recruit.verifyOffer(List.of("Free Colonist", "Indentured Servant",
                "Petty Criminal"), 200, true, "Pay passage: ", "");
        recruit.cancel();
    }
}
