package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.JettisonPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Player jettison of chosen goods (R-0138): the goods hold, quantity and
 * confirmation on a map sea square and during an Atlantic crossing, Escape and
 * Cancel without change, a stale confirmation refused without discarding, and
 * the saved reduced goods after loading from disk. Passengers, the other goods
 * hold, the voyage and the treasury stay unchanged.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_107_F98_jettison_test extends AbstractMicroColTest {

    private static final File ATLANTIC_SOURCE = new File(
            "src/test/scenarios/F35-voyage-reversal.microcol");

    private static final File MAP_SOURCE = new File(
            "src/test/scenarios/F52-fortification.microcol");

    private static final Location SEA = Location.of(25, 15);

    private static final int SHIP = JettisonFixture.SHIP_ID;

    private static final String CHANGED = "The ship or its hold changed, so nothing was discarded. Review the ship and try again.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), ATLANTIC_SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_map_sea_escape_cancel_confirm_stale_and_saved_continue()
            throws Exception {
        // The galleon at sea carries 12 ore, a passenger and 6 tobacco.
        JettisonFixture.prepare(getContext(), MAP_SOURCE,
                "F98-jettison-map.microcol", 12);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final JettisonPage jettison = new JettisonPage(getContext());
        final int gold = getModel().getUnitById(SHIP).getOwner().getGold();
        game.panToTile(SEA);
        game.selectTile(SEA);
        jettison.verifySelectedUnit(Optional.of(SHIP));
        jettison.verifyMapControlVisible(true);
        jettison.verifyMapControlEnabled(true);

        // Cancelling the hold list changes nothing.
        jettison.useMapControl();
        jettison.verifyHoldChoice(
                "Choose the goods hold of Galleon to empty in part. Nothing changes yet.");
        jettison.cancelHold();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 12));

        // Escape at the quantity step changes nothing.
        jettison.useMapControl();
        jettison.chooseHold(1);
        jettison.verifyQuantityExplanation(
                "Discard goods from hold 1 of Galleon. The ship stays at its place and receives no gold.");
        jettison.verifyQuantity(12);
        jettison.escapeQuantity();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 12));

        // Cancel at the confirmation keeps all ore.
        jettison.useMapControl();
        jettison.chooseHold(1);
        jettison.lowerQuantity(7);
        jettison.verifyQuantity(5);
        jettison.confirmQuantity();
        jettison.verifyConfirmation("Jettison goods",
                "Discard 5 Ore from hold 1 of Galleon? The ship stays at its place and receives no gold. Passengers and other holds stay aboard.");
        jettison.cancelConfirm();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 12));

        // Confirm discards exactly five ore; tobacco, passenger and treasury
        // stay.
        jettison.useMapControl();
        jettison.chooseHold(1);
        jettison.lowerQuantity(7);
        jettison.confirmQuantity();
        jettison.confirm();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 7));
        jettison.verifyHold(SHIP, JettisonFixture.OTHER_HOLD,
                Goods.of(GoodsType.TOBACCO, 6));
        jettison.verifyPassengerAboard(JettisonFixture.PASSENGER_ID, SHIP);
        jettison.verifyGold(SHIP, gold);
        jettison.verifyShipAtMapSquare(SHIP, SEA);
        jettison.verifySelectedUnit(Optional.of(SHIP));

        // A change of the ore while the confirmation waits is refused as stale.
        jettison.useMapControl();
        jettison.chooseHold(1);
        jettison.confirmQuantity();
        WaitForAsyncUtils
                .asyncFx(() -> ((UnitWithCargo) getModel().getUnitById(SHIP))
                        .getCargo().getSlotByIndex(JettisonFixture.ORE_HOLD)
                        .removeCargo(Goods.of(GoodsType.ORE, 2)))
                .get();
        jettison.confirm();
        jettison.verifyMessage(CHANGED);
        jettison.closeMessage();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 5));

        // Save to disk, return to the menu and load: the reduced goods remain
        // and
        // the command is not repeated.
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        final File savedFile = new File(System.getProperty("user.home"),
                "jettison-map.microcol");
        when(files.saveFile(any(File.class), anyString()))
                .thenReturn(savedFile);
        when(files.loadFile(any(File.class))).thenReturn(savedFile);
        game.saveGame();
        game.verifySavedGame(savedFile);
        game = game.exitToMenu().loadGame();
        game.panToTile(SEA);
        game.selectTile(SEA);
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 5));
        jettison.verifyHold(SHIP, JettisonFixture.OTHER_HOLD,
                Goods.of(GoodsType.TOBACCO, 6));
        jettison.verifyPassengerAboard(JettisonFixture.PASSENGER_ID, SHIP);
        jettison.verifyGold(SHIP, gold);
    }

    @Test
    @Tag("local")
    void TC_02_atlantic_crossing_keeps_voyage_passenger_and_other_hold()
            throws Exception {
        // The merchantman on its crossing carries 7 ore, a passenger and 6
        // tobacco.
        JettisonFixture.prepare(getContext(), ATLANTIC_SOURCE,
                "F98-jettison-atlantic.microcol", 7);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final JettisonPage jettison = new JettisonPage(getContext());
        final int gold = getModel().getUnitById(SHIP).getOwner().getGold();
        EuropePortScreen europe = game.openEuroperPort();
        jettison.verifyEuropeControl(SHIP, true);

        // Escape at the quantity step of the tobacco hold changes nothing.
        jettison.useEuropeControl(SHIP);
        jettison.chooseHold(3);
        jettison.verifyQuantityExplanation(
                "Discard goods from hold 3 of Merchantman. The ship stays at its place and receives no gold.");
        jettison.escapeQuantity();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.OTHER_HOLD,
                Goods.of(GoodsType.TOBACCO, 6));

        // Cancel at the confirmation keeps the ore.
        jettison.useEuropeControl(SHIP);
        jettison.chooseHold(1);
        jettison.lowerQuantity(4);
        jettison.verifyQuantity(3);
        jettison.confirmQuantity();
        jettison.verifyConfirmation("Jettison goods",
                "Discard 3 Ore from hold 1 of Merchantman? The ship stays at its place and receives no gold. Passengers and other holds stay aboard.");
        jettison.cancelConfirm();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 7));

        // Confirm discards three ore; the voyage keeps its direction and turns.
        jettison.useEuropeControl(SHIP);
        jettison.chooseHold(1);
        jettison.lowerQuantity(4);
        jettison.confirmQuantity();
        jettison.confirm();
        jettison.verifyDialogsClosed();
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 4));
        jettison.verifyHold(SHIP, JettisonFixture.OTHER_HOLD,
                Goods.of(GoodsType.TOBACCO, 6));
        jettison.verifyPassengerAboard(JettisonFixture.PASSENGER_ID, SHIP);
        jettison.verifyTransit(SHIP, true, 1, 3);
        jettison.verifyGold(SHIP, gold);

        // Save to disk, return and load: the crossing and the reduced ore
        // remain.
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        final File savedFile = new File(System.getProperty("user.home"),
                "jettison-atlantic.microcol");
        when(files.saveFile(any(File.class), anyString()))
                .thenReturn(savedFile);
        when(files.loadFile(any(File.class))).thenReturn(savedFile);
        game = europe.close();
        game.saveGame();
        game.verifySavedGame(savedFile);
        game = game.exitToMenu().loadGame();
        europe = game.openEuroperPort();
        jettison.verifyEuropeControl(SHIP, true);
        jettison.verifyHold(SHIP, JettisonFixture.ORE_HOLD,
                Goods.of(GoodsType.ORE, 4));
        jettison.verifyHold(SHIP, JettisonFixture.OTHER_HOLD,
                Goods.of(GoodsType.TOBACCO, 6));
        jettison.verifyTransit(SHIP, true, 1, 3);
    }
}
