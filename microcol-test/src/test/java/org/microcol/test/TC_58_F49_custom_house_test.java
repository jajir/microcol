package org.microcol.test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Select exports and observe actual ordinary/revolution turn accounting. */
@ExtendWith(ApplicationExtension.class)
public class TC_58_F49_custom_house_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F49-custom-house.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_confirm_export_overflow_and_continue() throws Exception {
        // Draft selections and cancellation leave all saved state untouched.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        CustomHousePage exports = new CustomHousePage(getContext());
        colony.rememberWorld();
        exports.open();
        exports.verifyDraft(Set.of(), false);
        exports.toggle(GoodsType.TOBACCO);
        exports.cancel();
        colony.verifyWorldUnchanged();
        exports.verifySelection(Set.of());
        // Confirming stores settings, but does not sell until the ordinary
        // update.
        exports.open();
        exports.toggle(GoodsType.TOBACCO);
        exports.toggle(GoodsType.ORE);
        exports.captureDialog("customHouseDialog", "r85-export-settings.png");
        exports.confirm();
        exports.verifySelection(Set.of(GoodsType.TOBACCO, GoodsType.ORE));
        exports.verifyStocks(200, 130, 150, 1000);
        game = colony.close();
        var report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(
                "New Amsterdam exported 103 × Tobacco through its custom house for 231 gold.");
        report.verifyThatAtLeastOneEventMessageContains(
                "New Amsterdam exported 30 × Ore through its custom house for 90 gold.");
        report.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifyStocks(100, 100, 150, 1321);
        // Continue retains the settings and the completed sale without replay.
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifySelection(Set.of(GoodsType.TOBACCO, GoodsType.ORE));
        exports.verifyStocks(100, 100, 150, 1321);
        exports.open();
        exports.verifyDraft(Set.of(GoodsType.TOBACCO, GoodsType.ORE), false);
        exports.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifyStocks(100, 100, 150, 1327);
    }

    @Test
    @Tag("local")
    void TC_02_revolution_uses_half_net_while_home_market_is_closed()
            throws Exception {
        // Load a revolution boundary and configure the same physical stocks.
        CustomHousePage exports = new CustomHousePage(getContext());
        exports.prepareFixture(true, true);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifyHomeMarketClosed();
        exports.open();
        exports.verifyDraft(Set.of(), true);
        exports.toggle(GoodsType.TOBACCO);
        exports.toggle(GoodsType.ORE);
        exports.confirm();
        game = colony.close();
        // Half of each ordinary integer net: 231/2=115 and 90/2=45.
        var report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains("for 115 gold.");
        report.verifyThatAtLeastOneEventMessageContains("for 45 gold.");
        report.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifyStocks(100, 100, 150, 1160);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        exports.verifyStocks(100, 100, 150, 1160);
        exports.verifyHomeMarketClosed();
        exports.verifySelection(Set.of(GoodsType.TOBACCO, GoodsType.ORE));
    }

    @Test
    @Tag("local")
    void TC_03_changed_selection_rejects_stale_confirmation() throws Exception {
        // Another command changes the selection while the player's draft is
        // open.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        CustomHousePage exports = new CustomHousePage(getContext());
        exports.open();
        exports.toggle(GoodsType.TOBACCO);
        exports.configureElsewhere(Set.of(GoodsType.ORE));
        colony.rememberWorld();
        exports.confirm();
        exports.verifyChanged();
        colony.verifyWorldUnchanged();
        exports.verifySelection(Set.of(GoodsType.ORE));
        exports.verifyStocks(200, 130, 150, 1000);
        exports.cancel();
    }

    @Test
    @Tag("local")
    void TC_05_real_protest_closes_the_ordinary_export_of_that_goods_only()
            throws Exception {
        // Ore is the most valuable stock (300 * 4), so the demand quotes 100
        // of it. The remaining 200 stay above the export threshold of 100.
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(new TaxDemandPage.Fixture(
                new File("src/test/scenarios/F49-custom-house.microcol"),
                "tax-custom-house")
                .due().expandedWarehouse()
                .warehouse(Map.of(GoodsType.TOBACCO, 200, GoodsType.ORE, 300,
                        GoodsType.SILVER, 150, GoodsType.CORN, 50))
                .exports(List.of(GoodsType.TOBACCO, GoodsType.ORE)));
        WelcomePage.of(getContext()).loadGame();
        CustomHousePage exports = new CustomHousePage(getContext());
        taxes.startUpdate();
        taxes.verifyDemandDialog("Current tax: 25%. The Crown demands 27%.",
                "Accepting raises the tax to 27% and destroys nothing.",
                "Protest destroys 100 Ore in New Amsterdam (stock 300), adds 100 liberty there, boycotts that commodity and records 800 gold of arrears. The tax stays 25%.",
                true);
        taxes.protest();
        taxes.finishUpdate();
        // Tobacco is sold down to the retained 100 for 231 gold. The 200 ore
        // left would be exported, but the boycotted ore is not, and the rate
        // is unchanged.
        taxes.verifyTax(25, Map.of(GoodsType.ORE, 800));
        taxes.verifyBoycottedExportSkipped(GoodsType.TOBACCO, GoodsType.ORE,
                200, 1231);
        exports.verifySelection(Set.of(GoodsType.TOBACCO, GoodsType.ORE));
        // Continue keeps the closed export and sells nothing again.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        taxes.verifyTax(25, Map.of(GoodsType.ORE, 800));
        taxes.verifyBoycottedExportSkipped(GoodsType.TOBACCO, GoodsType.ORE,
                200, 1231);
    }

    @Test
    @Tag("local")
    void TC_06_revolution_export_bypasses_the_boycott_with_half_net()
            throws Exception {
        // The revolutionary custom house exports the closed ore anyway.
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(new TaxDemandPage.Fixture(
                new File("src/test/scenarios/F49-custom-house.microcol"),
                "tax-revolution").independence()
                .arrears(Map.of(GoodsType.ORE, 800))
                .exports(List.of(GoodsType.TOBACCO, GoodsType.ORE)));
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CustomHousePage exports = new CustomHousePage(getContext());
        exports.verifyHomeMarketClosed();
        var report = game.nextTurn().orElseThrow();
        // Half of each ordinary integer net, as without the boycott.
        report.verifyThatAtLeastOneEventMessageContains("for 115 gold.");
        report.verifyThatAtLeastOneEventMessageContains("for 45 gold.");
        report.close();
        game.openColonyAt(COLONY, "New Amsterdam");
        exports.verifyStocks(100, 100, 150, 1160);
        // The boycott itself is not lifted by the exception.
        taxes.verifyTax(25, Map.of(GoodsType.ORE, 800));
    }

    @Test
    @Tag("local")
    void TC_04_without_custom_house_exports_are_unavailable() throws Exception {
        // The player cannot configure exports without the completed structure.
        CustomHousePage exports = new CustomHousePage(getContext());
        exports.prepareFixture(false, false);
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        exports.verifyUnavailable();
        exports.verifySelection(Set.of());
        exports.verifyStocks(200, 130, 150, 1000);
    }
}
