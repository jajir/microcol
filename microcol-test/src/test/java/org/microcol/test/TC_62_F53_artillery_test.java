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

/** Artillery purchase, construction, shipment and persistent battle damage. */
@ExtendWith(ApplicationExtension.class)
public class TC_62_F53_artillery_test extends AbstractMicroColTest {
    private static final Location GUN = Location.of(18, 10);
    private static final Location DEFENDER = Location.of(17, 11);
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F53-artillery.microcol"));
    }

    @Test
    @Tag("local")
    void TC_04_czech_artillery_preview_and_cancel() throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        AttackPage attack = new AttackPage(getContext());
        // Localized strength and loss explanation remain visible before cancel.
        attack.rememberWorld();
        attack.chooseAttack(GUN, DEFENDER);
        attack.verifyReview("Dělostřelectvo", "svobodný kolonista",
                "vyhlásíte válku", 0, 6);
        attack.verifyStrengths(
                "Základ: 7\nÚtok: +50%\nDělostřelectvo mimo opevněnou kolonii: -75%\nVýsledná síla: 5.25",
                "Základ: 2\nVýsledná síla: 2",
                "Šance útočníka na vítězství: 72.41%");
        attack.captureDialog("fightReview", "artillery-czech.png");
        attack.cancel();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_01_preview_cancel_damage_continue_and_second_defeat()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        ArtilleryPage artillery = new ArtilleryPage(getContext());
        // The review explains outdoor strength before any movement or damage.
        attack.rememberWorld();
        attack.chooseAttack(GUN, DEFENDER);
        attack.verifyReview("Artillery", "Free Colonist", "declares war", 0, 6);
        attack.verifyStrengths(
                "Base: 7\nAttack: +50%\nArtillery outside a fortified colony: -75%\nFinal strength: 5.25",
                "Base: 2\nFinal strength: 2", "Attacker win chance: 72.41%");
        attack.captureDialog("fightReview", "artillery-preview.png");
        attack.cancel();
        attack.verifyGameplayUnchanged();
        // A first defeat preserves the gun and its location with lower
        // strength.
        attack.chooseExistingOutcome(0, false);
        attack.chooseAttack(GUN, DEFENDER);
        attack.confirm();
        artillery.verifyDamaged(0, GUN, 0);
        artillery.verifyFeedback("artillery was damaged");
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        artillery.verifyDamaged(0, GUN, 0);
        // Give both participants a fresh move to isolate the second loss
        // branch.
        artillery.prepareNextBattle();
        attack.chooseExistingOutcome(0, false);
        attack.chooseAttack(GUN, DEFENDER);
        attack.verifyStrengths(
                "Base: 5\nAttack: +50%\nArtillery outside a fortified colony: -75%\nFinal strength: 3.75",
                "Base: 2\nFinal strength: 2", "Attacker win chance: 65.22%");
        attack.confirm();
        artillery.verifyDestroyed(0);
        artillery.verifyFeedback("damaged artillery was destroyed");
    }

    @Test
    @Tag("local")
    void TC_02_cancel_purchase_then_buy_ship_and_continue() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        new AttackPage(getContext()).replaceGame(new File(
                "src/test/scenarios/F39-ship-purchase-prices.microcol"));
        EuropePortScreen europe = GamePage.of(getContext()).openEuroperPort();
        ArtilleryPage artillery = new ArtilleryPage(getContext());
        ExpertPurchasePage offer = new ExpertPurchasePage(getContext());
        europe.openBuyUnitScreen();
        offer.revealSpecialist("ARTILLERY");
        offer.verifySpecialistOffer("ARTILLERY", "Artillery", 500, "Buy for ");
        offer.escape();
        artillery.verifyPurchase(0, 2500, 500);
        // Only the confirmed gun changes its type's price; it occupies one
        // hold.
        europe.openBuyUnitScreen();
        artillery.buy();
        artillery.verifyPurchase(1, 2000, 550);
        new PassengerTransferPage(getContext()).embarkInPort(103, 0, true);
        artillery.verifyCargo(103, 102, 0);
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        artillery.verifyPurchase(1, 2000, 550);
        artillery.verifyCargo(103, 102, 0);
    }

    @Test
    @Tag("local")
    void TC_03_armory_gate_then_complete_one_gun() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        ArtilleryPage artillery = new ArtilleryPage(getContext());
        artillery.loadConstructionFixture(false);
        ColonyScreen colony = GamePage.of(getContext()).openColonyAt(COLONY,
                "New Amsterdam");
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("ARTILLERY");
        building.verifyOffer(false, "Requires an armory", 200, 100);
        building.cancel();
        colony.close();
        // With the prerequisite present, the real construction control
        // completes it.
        artillery.loadConstructionFixture(true);
        colony = GamePage.of(getContext()).openColonyAt(COLONY,
                "New Amsterdam");
        building.open();
        building.select("ARTILLERY");
        building.verifyOffer(true, "", 200, 100);
        building.confirm();
        ConstructionRushPage rush = new ConstructionRushPage(getContext());
        rush.open();
        rush.confirm();
        building.verifyOutsideUnits(UnitType.ARTILLERY, 1);
        building.verifyStocksAndPopulation(0, 0, 3);
        building.cancel();
        colony.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        building.verifyOutsideUnits(UnitType.ARTILLERY, 1);
    }
}
