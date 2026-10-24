package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RoyalBattlePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0204: the notices of a finished colony project and of goods thrown away by
 * a full warehouse. Each comes from the actual owner update, names the colony,
 * the object and the quantity, offers Open colony without spending anything, is
 * not repeated by reopening the report or by Continue, and refuses a colony
 * that is no longer the player's.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_180_F204_project_and_overflow_notices_test
        extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F24-construction-eligibility.microcol");
    private static final Location COLONY = Location.of(24, 16);
    private static final int COLONY_ID = 0;
    private static final String COLONY_NAME = "New Amsterdam";
    private static final String WAITING_NOTICE = "Colony New Amsterdam: "
            + "Blacksmith shop has enough hammers but is waiting for tools "
            + "(1 missing).";
    private static final String DONE_NOTICE = "Colony New Amsterdam finished "
            + "Blacksmith shop.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_finished_project_names_colony_opens_it_and_is_not_repeated()
            throws Exception {
        // The smithy is finished by the owner update that finds its last tool.
        final ScreenTurnReport report = finishSmithyAndGetReport();

        // The report states the finished project once, and no longer reminds of
        // the tool shortage that the last update ended.
        report.verifyEventMessageCount(DONE_NOTICE, 1);
        report.verifyEventMessageCount(WAITING_NOTICE, 0);
        report.verifyOpenColonyAction(COLONY_ID, "Open colony");

        // Open colony changes nothing: the smithy stands and the tools are
        // used.
        ColonyScreen colony = report.openColonyWithMouse(COLONY_ID,
                COLONY_NAME);
        final ConstructionPage construction = new ConstructionPage(
                getContext());
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 0));
        GamePage game = colony.close();

        // Reopening the report adds no second notice.
        ScreenTurnReport reopened = game.openTurnReport();
        reopened.verifyEventMessageCount(DONE_NOTICE, 1);
        reopened.close();
        game = GamePage.of(getContext());

        // Continue keeps the one notice and does not build the smithy again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        final ScreenTurnReport restored = game.openTurnReport();
        restored.verifyEventMessageCount(DONE_NOTICE, 1);
        restored.close();
        game = GamePage.of(getContext());
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 0));
    }

    @Test
    @Tag("local")
    void TC_02_finished_project_action_refuses_a_colony_lost_after_the_report()
            throws Exception {
        // The report offers Open colony for the finished smithy.
        final ScreenTurnReport report = finishSmithyAndGetReport();
        report.verifyOpenColonyAction(COLONY_ID, "Open colony");
        report.retainOpenColonyAction(COLONY_ID);

        // The galleon sails out and the king captures the colony while the
        // report is still open.
        getContext().getRobot().interact(() -> getModel().getUnitById(3)
                .placeToLocation(Location.of(25, 16)));
        new RoyalBattlePage(getContext()).captureColony(COLONY, 200);

        // The retained action opens nothing, the report stays and keeps the
        // notice, and the loss is added.
        report.clickRetainedOpenColonyAction();
        report.verifyColonyScreenNotShown();
        report.verifyShown();
        report.verifyOpenColonyActionAbsent(COLONY_ID);
        report.verifyEventMessageCount(DONE_NOTICE, 1);
        report.verifyThatAtLeastOneEventMessageContains(
                "Colony New Amsterdam was lost.");
    }

    @Test
    @Tag("local")
    void TC_03_full_stock_notice_names_colony_goods_and_quantity()
            throws Exception {
        // A full tobacco stock throws away what the tobacco worker produces.
        OverflowFixture.prepare(getContext(), FIXTURE, "overflow-f204.microcol",
                COLONY_NAME, GoodsType.TOBACCO, 100);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOBACCO, 100));
        colony.verifyNetProductionAtLeast(GoodsType.TOBACCO, 2);
        final int thrownAway = colony.getNetProduction(GoodsType.TOBACCO);
        game = colony.close();

        // The update fills nothing more: the report names colony, goods and the
        // quantity the colony screen had promised, and offers Open colony.
        final String notice = "Colony New Amsterdam: not enough warehouse "
                + "space, " + thrownAway
                + " units of Tobacco were thrown away.";
        final Optional<ScreenTurnReport> report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.get().verifyEventMessageCount(notice, 1);
        report.get().verifyOpenColonyAction(COLONY_ID, "Open colony");

        // Open colony leaves the stock at its capacity.
        colony = report.get().openColonyWithMouse(COLONY_ID, COLONY_NAME);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOBACCO, 100));
        game = colony.close();

        // Reopening and Continue keep exactly one notice.
        final ScreenTurnReport reopened = game.openTurnReport();
        reopened.verifyEventMessageCount(notice, 1);
        reopened.close();
        game = GamePage.of(getContext());
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        final ScreenTurnReport restored = game.openTurnReport();
        restored.verifyEventMessageCount(notice, 1);

        // The colony is lost while the report is open; the action refuses it.
        restored.retainOpenColonyAction(COLONY_ID);
        getContext().getRobot().interact(() -> getModel().getUnitById(3)
                .placeToLocation(Location.of(25, 16)));
        new RoyalBattlePage(getContext()).captureColony(COLONY, 200);
        restored.clickRetainedOpenColonyAction();
        restored.verifyColonyScreenNotShown();
        restored.verifyShown();
        restored.verifyOpenColonyActionAbsent(COLONY_ID);
        restored.verifyEventMessageCount(notice, 1);
    }

    /**
     * Queue the smithy, let the first update wait for its last tool, bring the
     * tool from the galleon and end the turn that finishes it.
     */
    private ScreenTurnReport finishSmithyAndGetReport() throws Exception {
        // Four residents make six hammers a turn; confirm only the smithy.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        final ConstructionPage construction = new ConstructionPage(
                getContext());
        construction.assignCarpenter(4);
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.confirm();
        construction.cancel();
        construction.verifyPickerClosed();
        game = colony.close();

        // The first update reaches the hammers but waits for one tool.
        final Optional<ScreenTurnReport> waiting = game.nextTurn();
        game.verifyTurnReportPresent(waiting);
        waiting.get().verifyEventMessageCount(WAITING_NOTICE, 1);
        waiting.get().verifyEventMessageCount(DONE_NOTICE, 0);
        waiting.get().close();
        game = GamePage.of(getContext());

        // Bring the last tool from the galleon and end the turn.
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 14);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 20));
        game = colony.close();
        final Optional<ScreenTurnReport> finished = game.nextTurn();
        game.verifyTurnReportPresent(finished);
        return finished.get();
    }
}
