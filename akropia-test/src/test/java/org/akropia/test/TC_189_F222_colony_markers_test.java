package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.ColonyMarkersPage;
import org.akropia.page.FogPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_189_F222_colony_markers_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F222-colony-markers.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_population_defense_preview_and_continue() throws Exception {
        // Twelve ordinary colonies expose the three sizes and four completed
        // defenses.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FogPage map = new FogPage(getContext());
        final ColonyMarkersPage markers = new ColonyMarkersPage(getContext());
        map.resizeMapWindow(1200, 900);
        map.centerFrontier();
        markers.verifyMapMarkers();
        markers.verifyNationalFlags();
        // A click still opens the colony with its real name and Escape returns
        // to the map.
        game = game.openColonyAt(Location.of(28, 19), "D3").closeWithEscape();
        markers.verifySidebar(3, 3);
        map.centerFrontier();
        markers.verifyMapMarkers();
        map.saveMapEvidence("R-0280-twelve-colony-markers.png");
        markers.captureDialog("rightPanel", "R-0280-colony-sidebar.png");
        // Continue restores the population and completed fortifications without
        // changing the artwork.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        map.centerFrontier();
        markers.verifyMapMarkers();
    }
}
