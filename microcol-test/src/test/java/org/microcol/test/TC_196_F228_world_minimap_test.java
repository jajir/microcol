package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.MainScreenLayoutPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.microcol.page.WorldMinimapPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * R-0350, UC-114/119: a full-world camera control respects explored terrain.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_196_F228_world_minimap_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F228-world-minimap.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_navigation_preserves_gameplay_and_follows_the_view()
            throws Exception {
        // Open the partially explored world; all map bounds fit the sidebar.
        WelcomePage.of(getContext()).loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 900);
        map.verifyLayoutAndLanguage("World map",
                "Click to move view · Arrow keys pan");
        map.verifyTerrain(Location.of(25, 16), "#649b97", true);
        map.verifyTerrain(Location.of(40, 16), "#9c9585", false);
        map.rememberGameAndView();
        // Clicking the center and both extreme corners only moves the camera.
        map.clickWorld(.5, .5);
        map.verifyCentered(.5, .5);
        map.verifyViewport();
        map.clickWorld(.99, .99);
        map.verifyCentered(1, 1);
        map.verifyViewport();
        map.clickWorld(.01, .01);
        map.verifyCentered(0, 0);
        map.verifyViewport();
        map.verifyGameUnchanged();
        // Arrow keys pan one tile; Tab leaves focus without cycling a unit.
        map.clickWorld(.5, .5);
        map.rememberGameAndView();
        map.press(KeyCode.RIGHT);
        map.verifyPan(45, 0);
        map.verifyViewport();
        map.press(KeyCode.TAB);
        map.verifyFocusLeftMinimap();
        map.verifyGameUnchanged();
        // Existing map panning and window resize keep the rectangle aligned.
        new FogPage(getContext()).centerOn(Location.of(30, 16));
        map.verifyViewport();
        window.resize(900, 700);
        map.verifyLayoutAndLanguage("World map",
                "Click to move view · Arrow keys pan");
        map.verifyViewport();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0350-minimap-small.png");
        window.resize(1440, 1000);
        map.verifyViewport();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0350-minimap.png");
    }

    @Test
    @Tag("local")
    void TC_02_discovery_and_czech_help_update_the_overview() throws Exception {
        // Language is chosen through the existing settings screen.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        map.verifyLayoutAndLanguage("Mapa světa",
                "Kliknutím přesunete výřez · Šipky posouvají");
        map.verifyTerrain(Location.of(28, 16), "#9c9585", false);
        // A real exploration move reveals the same sea in both map views.
        final FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.move(Location.of(25, 16), Location.of(26, 16), 3);
        map.verifyTerrain(Location.of(28, 16), "#649b97", true);
        map.verifyViewport();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0350-minimap-czech.png");
    }
}
