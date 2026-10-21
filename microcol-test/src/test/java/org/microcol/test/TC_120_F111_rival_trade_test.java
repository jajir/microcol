package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RivalColonyPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Rival colony-to-Europe supply cycle through real France owner turns. France
 * founds a colony at (21,2) with its own founder, its ship waits in the colony
 * port, the warehouse holds 100 lumber and France knows the whole map, so its
 * route to the sea lane is not limited by exploration. The Dutch hold a colony
 * as well; without one the game would end by the no-colony rule once more than
 * fifteen turns were played, long before a transatlantic round trip is over.
 *
 * The first France turn ships the 50 lumber surplus and keeps the 50 lumber
 * reserve. Lumber in the colony and the ship together stays 100 while the cargo
 * travels, and Save/Continue keeps the same shipment. In Europe France sells
 * the lumber and buys tools; the treasury events recorded by the model give the
 * exact taxed sale and purchase at the prices of that moment. The tools return
 * to the colony warehouse, and Save/Continue midvoyage replays no sale or
 * purchase. The Atlantic voyage duration is random, so each leg waits for its
 * observed state within a finite number of rounds instead of a guessed arrival
 * turn, and a stalled or ended journey is reported with its situation.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_120_F111_rival_trade_test extends AbstractMicroColTest {

    private static final Location SITE = Location.of(21, 2);
    /** Land beside the Dutch ship, so the human keeps a colony. */
    private static final Location DUTCH_SITE = Location.of(31, 15);
    private static final int COLONY_ID = 0;
    private static final int FOUNDER_ID = 4;
    private static final int SHIP_ID = 5;
    private static final int DUTCH_SETTLER_ID = 0;
    private static final int STOCK = 100;
    private static final int RESERVE = 50;
    private static final int FIXTURE_GOLD = 260;
    private static final int NOTHING_SUPPLIED = 0;
    /** Finite bound for each waiting leg; a stuck cycle fails here. */
    private static final int MAX_ROUNDS = 40;

    /** The saved game the Load command opens; this scenario swaps it. */
    private volatile File loadTarget;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenAnswer(invocation -> loadTarget);
            binder.bind(FileSelectingService.class).toInstance(selecting);
        });
    }

    /**
     * Scoped reference: both founders settle through the model, France's ship
     * stands in the colony port, the warehouse holds 100 lumber, France holds a
     * known treasury and knows the whole map. The Dutch settle on a small
     * grassland coast beside their ship. The save records the state before the
     * first France turn.
     */
    private static File rivalTradeFixture() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        final Map<Location, TerrainType> terrain = new HashMap<>(
                data.getModel().getMap().getTerrainMap());
        for (int x = DUTCH_SITE.getX(); x <= DUTCH_SITE.getX() + 1; x++) {
            for (int y = DUTCH_SITE.getY() - 1; y <= DUTCH_SITE.getY()
                    + 1; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                known.add(Location.of(x, y));
            }
        }
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow().getVisible().setVisibility(known, 50, 30);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            model.startGame();
            final Player france = model.getPlayerByName("France");
            if (model.getCurrentPlayer() != france) {
                model.getCurrentPlayer().endTurn();
            }
            final Unit settler = model.getUnitById(FOUNDER_ID);
            settler.placeToLocation(SITE);
            model.buildColony(france, settler,
                    model.suggestColonyName(Set.of()));
            model.getUnitById(SHIP_ID).placeToLocation(SITE);
            model.getColonyById(COLONY_ID).orElseThrow().getWarehouse()
                    .addGoods(Goods.of(GoodsType.LUMBER, STOCK));
            france.setGold(FIXTURE_GOLD);
            france.endTurn();

            // On the human turn the Dutch settle beside their own ship.
            final Player dutch = model.getPlayerByName("Dutch");
            for (int guard = 0; guard < 4
                    && model.getCurrentPlayer() != dutch; guard++) {
                model.getCurrentPlayer().endTurn();
            }
            if (model.getCurrentPlayer() != dutch) {
                throw new IllegalStateException("The human turn never came");
            }
            final Unit dutchSettler = model.getUnitById(DUTCH_SETTLER_ID);
            dutchSettler.placeToLocation(DUTCH_SITE);
            dutchSettler.startTurn();
            model.buildColony(dutch, dutchSettler,
                    model.suggestColonyName(model.getColonies().stream()
                            .map(Colony::getName).collect(Collectors.toSet())));
            data.setModel(model.save());
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve("F111-rival-trade.microcol");
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    @Test
    @Tag("local")
    void TC_01_rival_ships_surplus_lumber_sells_in_europe_and_returns_tools()
            throws Exception {
        loadTarget = rivalTradeFixture();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.observeRivalTreasury();
        final int start = rival.readRound();
        rival.verifyRivalShipmentReady(COLONY_ID, SITE, SHIP_ID, STOCK);
        rival.verifyRivalTreasury(0);

        // The first France turn loads exactly the surplus above the reserve.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(start + 1);
        rival.verifyRivalShipmentLoaded(start + 1, COLONY_ID, SHIP_ID, RESERVE,
                STOCK - RESERVE);

        // Save and Continue keep the same shipment; nothing is loaded twice.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.observeRivalTreasury();
        rival.waitForHumanRound(start + 1);
        rival.verifyRivalShipmentLoaded(start + 1, COLONY_ID, SHIP_ID, RESERVE,
                STOCK - RESERVE);

        // The next turn keeps the cargo and the reserve on the way out.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(start + 2);
        rival.verifyRivalLumberConserved(start + 2, COLONY_ID, SHIP_ID, STOCK);

        // Each France turn returns control. Before the sale nothing touches
        // the treasury; the wait ends when the lumber is sold in Europe.
        for (int turn = 0; turn < MAX_ROUNDS
                && !rival.isRivalLumberSold(SHIP_ID); turn++) {
            rival.verifyRivalJourneyRunning("Outbound turn " + turn, SHIP_ID);
            rival.verifyRivalTreasury(0);
            final int next = rival.readRound() + 1;
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
        }
        rival.verifyRivalJourneyRunning("Europe stop", SHIP_ID);

        // The two treasury events are the taxed sale of the 50 lumber and the
        // largest tool purchase that keeps 200 gold; the ship turned home.
        rival.verifyRivalEuropeSale(SHIP_ID, RESERVE, NOTHING_SUPPLIED);
        final int bought = rival.readRivalHoldTools(SHIP_ID);

        // Save and Continue midvoyage replay no sale and no purchase.
        final int soldRound = rival.readRound();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.observeRivalTreasury();
        rival.waitForHumanRound(soldRound);
        rival.verifyRivalVoyageUnchanged(SHIP_ID, bought);

        // The way home is finite and adds no treasury event; the tools end in
        // the colony warehouse beside the lumber reserve.
        for (int turn = 0; turn < MAX_ROUNDS
                && !rival.isRivalToolsDelivered(COLONY_ID, bought); turn++) {
            rival.verifyRivalJourneyRunning("Return turn " + turn, SHIP_ID);
            rival.verifyRivalTreasury(2);
            final int next = rival.readRound() + 1;
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
        }
        rival.verifyRivalJourneyRunning("Delivery", SHIP_ID);
        rival.verifyRivalToolsDelivered(COLONY_ID, SHIP_ID, bought, RESERVE);
    }
}
