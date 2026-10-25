package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.UnitArtworkPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** R-0285: all 35 unit types and five states at actual map size. */
@ExtendWith(ApplicationExtension.class)
public class TC_190_F223_unit_artwork_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F223-unit-artwork.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_map_artwork_movement_and_continue() throws Exception {
        // Inspect every type and equipment/damage state in the real map.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final FogPage map = new FogPage(getContext());
        final UnitArtworkPage units = new UnitArtworkPage(getContext());
        map.resizeMapWindow(1200, 900);
        map.centerOn(Location.of(23, 14));
        map.inspect(Location.of(23, 13));
        units.verifyRosterAndTurn();
        units.verifyMapArtwork();
        map.saveMapEvidence("R-0285-all-unit-artwork.png");

        // Move the colonist with the normal map gesture and inspect the result.
        map.move(Location.of(16, 10), Location.of(17, 10), 1000);
        units.verifyUnitLocation(1000, Location.of(17, 10));
        map.inspect(Location.of(23, 13));
        units.verifyMapArtwork();

        // Continue retains the roster, position, equipment and shared pictures.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        map.centerOn(Location.of(23, 14));
        map.inspect(Location.of(23, 13));
        units.verifyUnitLocation(1000, Location.of(17, 10));
        units.verifyRosterAndTurn();
        units.verifyMapArtwork();
    }
}
