package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRouteKind;
import org.microcol.model.TradeRouteStop;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.TradeRouteDialog;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Trade route creation from the colony screen. New Amsterdam is inland; Delft
 * stands on the coast and is the only sea port. Each test starts from the same
 * saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_116_F107_trade_route_creation_test
        extends AbstractMicroColTest {

    private static final TradeRouteStop NEW_AMSTERDAM = TradeRouteStop
            .ofColony(0);
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    private static final TradeRouteStop EUROPE = TradeRouteStop.europe();

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F107-trade-route-creation.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_land_route_is_created_cancel_keeps_it_and_continue_restores_it()
            throws Exception {
        // Wagons reach only colonies; Delft is the one other colony.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.captureDialog("tradeRouteOpen", "trade-route-colony.png");
        route.open();
        route.verifyOpenedFor("New Amsterdam");
        route.verifyKind(TradeRouteKind.LAND);
        route.verifyStopOptions(0, "Delft");
        route.verifyCreateEnabled(false);

        // Name and stops are only staged until Create is pressed.
        route.enterName("Wagon Road");
        route.chooseStop(0, DELFT);
        route.verifyStopOptions(1, "New Amsterdam");
        route.chooseStop(1, NEW_AMSTERDAM);
        route.verifyCreateEnabled(true);
        route.captureDialog("tradeRouteDialog", "trade-route-editor.png");
        route.verifyStoredRoutes(0);
        route.create();
        route.verifyDialogClosed();
        route.verifyStoredRoute("Wagon Road", TradeRouteKind.LAND, "Delft",
                "New Amsterdam");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 100));

        // Cancel after staging changes nothing.
        route.open();
        route.enterName("Unused Road");
        route.chooseStop(0, DELFT);
        route.cancel();
        route.verifyDialogClosed();
        route.verifyStoredRoutes(1);
        game = colony.close();

        // Save and Continue restore the route with its name and stop order.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        route.open();
        route.verifyExistingRoutes("Your routes: Wagon Road");
        route.verifyStoredRoute("Wagon Road", TradeRouteKind.LAND, "Delft",
                "New Amsterdam");
        route.cancel();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_02_invalid_text_and_transport_choices_are_explained()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();

        // With a valid name, inland New Amsterdam cannot start a sea route.
        route.enterName("Sea Trial");
        route.chooseKind(TradeRouteKind.SEA);
        route.verifyMessage("A sea route must start at a coastal port.");
        route.verifyCreateEnabled(false);

        // Back on land the route is only unfinished; Europe is never offered
        // to a wagon. A blank name is then the first problem.
        route.chooseKind(TradeRouteKind.LAND);
        route.verifyMessage("");
        route.verifyStopOptions(0, "Delft");
        route.enterName("");
        route.verifyMessage("Enter a route name.");
        route.verifyCreateEnabled(false);
        route.enterName("a".repeat(65));
        route.verifyMessage("The route name can have at most 64 characters.");
        route.enterName("   Wagon   Road  ");
        route.verifyMessage("");
        route.verifyCreateEnabled(false);
        route.chooseStop(0, DELFT);
        route.verifyCreateEnabled(true);
        route.cancel();
        route.verifyDialogClosed();
        route.verifyStoredRoutes(0);
        game = colony.close();

        // Delft is the port: a sea route offers Europe first, then only the
        // legal stops after it.
        colony = game.openColonyAt(Location.of(28, 16), "Delft");
        route.open();
        route.verifyOpenedFor("Delft");
        route.chooseKind(TradeRouteKind.SEA);
        route.verifyStopOptions(0, "Europe");
        route.enterName("Ocean Run");
        route.chooseStop(0, EUROPE);
        route.verifyStopOptions(1, "Delft");
        route.chooseStop(1, DELFT);
        route.verifyStopOptions(2, "Europe");
        route.chooseStop(2, EUROPE);
        route.verifyCreateEnabled(true);
        route.create();
        route.verifyDialogClosed();
        route.verifyStoredRoute("Ocean Run", TradeRouteKind.SEA, "Europe",
                "Delft", "Europe");
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_03_duplicate_names_are_refused_and_stops_use_the_keyboard()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Silver Road");
        route.chooseStop(0, DELFT);
        route.chooseStop(1, NEW_AMSTERDAM);
        route.create();
        route.verifyStoredRoutes(1);

        // Trimming and case folding make this a duplicate for the same owner.
        route.open();
        route.verifyNoStopsChosen();
        route.enterName("  silver ROAD ");
        route.verifyMessage("You already have a route with this name.");
        route.verifyCreateEnabled(false);
        route.enterName("Silver Lane");
        route.verifyMessage("");
        route.chooseStop(0, DELFT);
        route.verifyCreateEnabled(true);
        route.cancel();
        route.verifyDialogClosed();
        route.verifyStoredRoutes(1);

        // Player text keeps its accents and symbols; only the edges are
        // trimmed. The canceled draft leaves no stops behind.
        route.open();
        route.verifyNoStopsChosen();
        route.enterName("  Přístav svobody 🌊  ");
        route.chooseStop(0, DELFT);
        route.verifyCreateEnabled(true);
        route.create();
        route.verifyDialogClosed();
        route.verifyStoredRoutes(2);
        route.verifyStoredRoute("Přístav svobody 🌊", TradeRouteKind.LAND,
                "Delft");
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_04_czech_editor_explains_choices_and_closes_with_the_mouse()
            throws Exception {
        // The editor and its refusals follow the chosen language.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.captureDialog("tradeRouteOpen", "trade-route-colony-czech.png");
        route.open();
        route.verifyTitle("Obchodní trasa z kolonie New Amsterdam");
        route.verifyMessage("Zadejte název trasy.");
        route.enterName("Mořská cesta");
        route.chooseKind(TradeRouteKind.SEA);
        route.verifyMessage("Námořní trasa musí začínat v přístavu u moře.");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, DELFT);
        route.verifyCreateEnabled(true);
        route.captureDialog("tradeRouteDialog", "trade-route-editor-czech.png");

        // Cancel leaves no route, and the colony still closes by its button.
        route.cancel();
        route.verifyDialogClosed();
        route.verifyStoredRoutes(0);
        colony.close();
    }
}
