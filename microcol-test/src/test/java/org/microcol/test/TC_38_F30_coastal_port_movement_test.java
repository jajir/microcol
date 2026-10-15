package org.microcol.test;

import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.UnitPo;
import org.microcol.page.CoastalPortPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Verify sailing admission, cancellation and cargo continuity at coastal ports.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_38_F30_coastal_port_movement_test extends AbstractMicroColTest {
    private static final Location WATER = Location.of(25, 16);
    private static final Location PORT = Location.of(24, 16);
    private static final Location INLAND = Location.of(23, 16);
    private static final Location SHORE = Location.of(24, 15);
    private static final File FIXTURE = new File(
            "src/test/scenarios/F30-coastal-port-movement.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_loaded_port_entry_cancel_save_and_return() throws Exception {
        // Preview an own coastal port, then cancel before spending any
        // movement.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final CoastalPortPage port = new CoastalPortPage(getContext());
        final FogPage map = new FogPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        port.verifyPort(PORT, true);
        port.rememberWorld();
        map.preview(WATER, PORT);
        port.verifyPreview(PORT, "MOVE");
        port.cancelPreview();
        port.verifyPreviewCanceled();
        port.verifyWorldUnchanged();

        // Sail into the colony with all goods and equipped passengers still
        // aboard.
        map.move(WATER, PORT, 3);
        passengers.verifyShip(PORT, 5);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);

        // Continue retains the legal port position, movement and exact cargo
        // slots.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        passengers.verifyShip(PORT, 5);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);

        // Escape closes the colony menu normally and leaves that control
        // focused.
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.openColonyMenu();
        port.verifyColoniesMenuShowing(true);
        audit.press(javafx.scene.input.KeyCode.ESCAPE);
        port.verifyColoniesMenuShowing(false);
        port.verifyColoniesMenuFocused();

        // Leaving the port spends one further ship point, without unloading
        // anyone.
        map.move(PORT, WATER, 3);
        passengers.verifyShip(WATER, 4);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);

        // Ordinary land offers explicit passenger landing, not sailing; cancel
        // it.
        port.rememberWorld();
        map.preview(WATER, SHORE);
        port.verifyPreview(SHORE, "ANCHOR");
        port.cancelPreview();
        port.verifyPreviewCanceled();
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.verifyChoice(4, true);
        passengers.cancel();
        port.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_inland_colony_rejects_preview_and_sailing() throws Exception {
        // A scripted neighboring colony is inland even though the first is a
        // port.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final CoastalPortPage port = new CoastalPortPage(getContext());
        final FogPage map = new FogPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        map.move(WATER, PORT, 3);
        port.verifyPort(PORT, true);
        port.verifyPort(INLAND, false);
        port.verifyNoSailingRoute(INLAND);
        port.rememberWorld();

        // There is no inland sailing or landing preview; cancel preserves
        // everything.
        map.preview(PORT, INLAND);
        map.verifyPreviewEmpty();
        port.cancelPreview();
        port.verifyPreviewCanceled();
        port.verifyWorldUnchanged();

        // The actual drag explains rejection and preserves movement and all
        // cargo.
        game.moveMouseAtLocation(PORT);
        game.dragMouseAtLocation(INLAND);
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        port.verifyWorldUnchanged();
        passengers.verifyShip(PORT, 5);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
    }

    @Test
    @Tag("local")
    void TC_03_empty_ship_cannot_sail_onto_ordinary_land() throws Exception {
        // Load the same water/port geometry with all passengers safely on the
        // pier.
        useVariant(WATER, true);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final CoastalPortPage port = new CoastalPortPage(getContext());
        final FogPage map = new FogPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        port.verifyEmptyShip();
        port.verifyNoSailingRoute(SHORE);
        port.rememberWorld();
        map.preview(WATER, SHORE);
        map.verifyPreviewEmpty();
        port.cancelPreview();
        port.verifyPreviewCanceled();
        port.verifyWorldUnchanged();

        // Releasing the move still rejects land without spending a ship point.
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        port.verifyWorldUnchanged();
        passengers.verifyShip(WATER, 6);
        port.verifyEmptyShip();
    }

    @Test
    @Tag("local")
    void TC_04_diagonal_isolated_lake_qualifies_as_port() throws Exception {
        // Only the northeast diagonal is water; no ocean connection is needed.
        final Location lake = Location.of(25, 15);
        useVariant(lake, false);
        WelcomePage.of(getContext()).loadGame();
        final CoastalPortPage port = new CoastalPortPage(getContext());
        final FogPage map = new FogPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        port.verifyPort(PORT, true);
        port.verifyPort(INLAND, false);
        map.preview(lake, PORT);
        port.verifyPreview(PORT, "MOVE");
        port.cancelPreview();
        port.verifyPreviewCanceled();
        map.move(lake, PORT, 3);
        passengers.verifyShip(PORT, 5);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        map.move(PORT, lake, 3);
        passengers.verifyShip(lake, 4);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
    }

    private void useVariant(final Location water, final boolean empty)
            throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(FIXTURE);
        data.getModel().setFocusedField(water);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(WATER, TerrainType.GRASSLAND);
        terrain.put(water, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        for (final UnitPo unit : data.getModel().getUnits()) {
            if (unit.getId() == 3) {
                unit.getPlaceMap().setLocation(water);
                if (empty)
                    unit.getCargo().getSlots().forEach(slot -> {
                        slot.setUnitId(null);
                        slot.setGoodsType(null);
                        slot.setAmount(0);
                    });
            } else if (empty && (unit.getId() == 4 || unit.getId() == 5)) {
                unit.setPlaceCargoSlot(null);
                final PlaceEuropePortPo pier = new PlaceEuropePortPo();
                pier.setOnPier(true);
                unit.setPlaceEuropePort(pier);
            }
        }
        final File file = new File(System.getProperty("user.home"),
                "coastal-port.microcol");
        dao.saveToFile(file.toString(), data);
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }
}
