package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.FogPage;
import org.akropia.page.MainScreenLayoutPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.akropia.page.WorldMinimapPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

/**
 * R-0350/0355/0356, UC-114/119: world navigation respects explored terrain.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_196_F228_world_minimap_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F228-world-minimap.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_navigation_preserves_gameplay_and_follows_the_view()
            throws Exception {
        // Open the partially explored world; the headingless map starts the
        // sidebar and all map bounds fit it.
        WelcomePage.of(getContext()).loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 900);
        map.verifyLayoutAndLanguage("Minimap",
                "Click, drag or arrow keys to pan");
        map.verifyTerrain(Location.of(25, 16), "#649b97", true);
        map.verifyTerrain(Location.of(40, 16), "#9c9585", false);
        map.rememberGameAndView();
        // Hovering explains the minimap in the status bar without any effect.
        map.hoverMinimap();
        map.verifyStatusMessage(
                "Minimap. It never really caught on in ancient Greece. They were waiting for the invention of the monitor.");
        map.leaveMinimap();
        map.verifyRightPanelStatus();
        map.verifyGameUnchanged();
        // Clicking the center and both extreme corners only moves the camera.
        map.clickWorld(.5, .5);
        map.verifyCentered(.5, .5);
        map.verifyViewport();
        map.verifyMapFocus(true);
        window.saveEvidence("r0356-minimap-focused.png");
        map.clickWorld(.99, .99);
        map.verifyCentered(.99, .99);
        map.verifyViewport();
        map.clickWorld(.01, .01);
        map.verifyCentered(.01, .01);
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
        map.verifyMapFocus(false);
        map.verifyGameUnchanged();
        // Existing map panning and window resize keep the rectangle aligned.
        new FogPage(getContext()).centerOn(Location.of(30, 16));
        map.verifyViewport();
        window.resize(900, 700);
        window.saveEvidence("r0356-minimap-small.png");
        map.verifyLayoutAndLanguage("Minimap",
                "Click, drag or arrow keys to pan");
        map.verifyViewport();
        window.resize(1440, 1000);
        map.verifyViewport();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0356-minimap.png");
    }

    @Test
    @Tag("local")
    void TC_02_discovery_and_czech_help_update_the_overview() throws Exception {
        // Language is chosen through the existing settings screen.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        map.verifyLayoutAndLanguage("Minimapa",
                "Posun: kliknutí, tažení či šipky");
        map.verifyTerrain(Location.of(28, 16), "#9c9585", false);
        map.hoverMinimap();
        map.verifyStatusMessage(
                "Minimapa. Ve starém Řecku se příliš neujala. Čekalo se na vynález monitoru.");
        map.leaveMinimap();
        map.verifyRightPanelStatus();
        // A real exploration move reveals the same sea in both map views.
        final FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.move(Location.of(25, 16), Location.of(26, 16), 3);
        map.verifyTerrain(Location.of(28, 16), "#649b97", true);
        map.verifyViewport();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0356-minimap-czech.png");
    }

    @Test
    @Tag("local")
    void TC_03_drag_tracks_pointer_until_release_without_changing_gameplay()
            throws Exception {
        // The camera follows native pointer movement before the button is
        // released.
        WelcomePage.of(getContext()).loadGame();
        new MainScreenLayoutPage(getContext()).resize(1280, 900);
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        map.rememberGameAndView();
        map.beginDrag(.35, .4, MouseButton.PRIMARY);
        map.verifyCentered(.35, .4);
        map.movePointer(.6, .6);
        map.verifyCentered(.6, .6);
        map.verifyViewport();
        map.movePointer(.01, .01);
        map.verifyCentered(.01, .01);
        map.verifyViewport();
        // The native grab continues outside the minimap and stops on release
        // there.
        map.movePointer(.5, 1.4);
        map.verifyCentered(.5, 1);
        map.verifyViewport();
        map.endDrag(MouseButton.PRIMARY);
        map.verifyGameUnchanged();
        map.rememberGameAndView();
        map.movePointer(.3, .3);
        map.verifyPan(0, 0);
        // Secondary-button and paper-margin gestures must not move the camera.
        map.beginDrag(.5, .5, MouseButton.SECONDARY);
        map.movePointer(.7, .7);
        map.endDrag(MouseButton.SECONDARY);
        map.verifyPan(0, 0);
        map.beginDrag(.5, -.1, MouseButton.PRIMARY);
        map.movePointer(.3, .3);
        map.endDrag(MouseButton.PRIMARY);
        map.verifyPan(0, 0);
        map.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_04_world_edges_and_inaccessible_margin_survive_resize()
            throws Exception {
        // Open the world and keep a full snapshot of gameplay state.
        WelcomePage.of(getContext()).loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 900);
        map.rememberGameAndView();

        // Drag past all corners; the camera center stops at the world edge.
        map.beginDrag(.5, .5, MouseButton.PRIMARY);
        map.movePointer(-.1, -.1);
        map.verifyCentered(0, 0);
        map.verifyViewport();
        map.movePointer(1.1, -.1);
        map.verifyCentered(1, 0);
        map.verifyViewport();
        map.movePointer(1.1, 1.1);
        map.verifyCentered(1, 1);
        map.verifyViewport();
        map.movePointer(-.1, 1.1);
        map.verifyCentered(0, 1);
        map.verifyViewport();
        map.movePointer(-.1, -.1);
        map.verifyCentered(0, 0);
        map.verifyViewport();
        map.endDrag(MouseButton.PRIMARY);
        map.verifyCentered(0, 0);
        map.verifyUnknownMargin();
        window.saveEvidence("r0412-northwest-margin.png");

        // The margin cannot select or move a unit, including a pixel at the
        // edge.
        map.clickOutsideWorld();
        map.verifyGameUnchanged();
        map.verifySelectionUnchanged();
        window.resize(900, 700);
        map.verifyViewport();
        map.verifyUnknownMargin();
        window.resize(1440, 1000);
        map.verifyViewport();
        map.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_05_hamburger_opens_menu_and_resume_preserves_game()
            throws Exception {
        // The visible Menu button uses the approved icon in its existing
        // footprint.
        WelcomePage.of(getContext()).loadGame();
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        final org.akropia.page.SavedGamesPage menu = new org.akropia.page.SavedGamesPage(
                getContext());
        map.rememberGameAndView();
        map.verifyHamburgerMenu();
        new MainScreenLayoutPage(getContext())
                .saveEvidence("r0411-hamburger.png");

        // Open the existing menu, then resume the same game without mutation.
        menu.openMenu();
        menu.verifyMenu();
        menu.chooseMenu("menuResume");
        map.verifyHamburgerMenu();
        map.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_06_scrolling_keeps_terrain_and_fog_aligned() throws Exception {
        // Open the known coast beside unexplored ocean and keep game state.
        WelcomePage.of(getContext()).loadGame();
        new MainScreenLayoutPage(getContext()).resize(1280, 900);
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        final FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        map.rememberGameAndView();

        // Capture while each WASD direction is held, at default and maximum
        // speed. A settled screenshot alone would miss a one-frame mismatch.
        for (final int speed : new int[] { 2, 9 }) {
            map.captureScrollingFrame(KeyCode.D, speed, Location.of(26, 15));
            map.verifyScrollingFrame(speed * 2, 0);
            map.captureScrollingFrame(KeyCode.A, speed, Location.of(26, 15));
            map.verifyScrollingFrame(-speed * 2, 0);
            map.captureScrollingFrame(KeyCode.S, speed, Location.of(26, 15));
            map.verifyScrollingFrame(0, speed * 2);
            map.captureScrollingFrame(KeyCode.W, speed, Location.of(26, 15));
            map.verifyScrollingFrame(0, -speed * 2);
        }
        map.verifyGameUnchanged();
    }

    @Test
    @Tag("local")
    void TC_07_floating_panel_keeps_map_navigation_and_minimap_available()
            throws Exception {
        // The wide map continues behind and beyond the centered panel.
        WelcomePage.of(getContext()).loadGame();
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        final WorldMinimapPage map = new WorldMinimapPage(getContext());
        window.resize(1800, 1000);
        map.verifyFloatingPanel(false);
        map.centerSelection();
        map.verifySelectionCenteredOutsidePanel();
        map.rememberGameAndView();
        map.clickPanelBorder();
        map.scrollPanel("mapDetailsScroll");
        map.verifyCameraUnchanged();
        map.verifyGameUnchanged();
        map.verifyCompactPanel();
        window.saveEvidence("floating-map-wide.png");

        // Low windows preserve the minimap while details scroll below it.
        window.resize(1024, 400);
        map.verifyMinimumWindow();
        map.verifyFloatingPanel(false);
        map.scrollPanel("mapDetailsScroll");
        map.verifyFloatingPanel(false);
        window.saveEvidence("floating-map-low.png");

        // Planning uses the same panel and keeps minimap navigation available.
        map.togglePlanner();
        map.verifyFloatingPanel(true);
        map.rememberGameAndView();
        map.scrollPanel("tradeRoutePlanningScroll");
        map.verifyFloatingPanel(true);
        map.verifyCameraUnchanged();
        map.verifyGameUnchanged();
        map.clickWorld(.5, .5);
        map.verifyCentered(.5, .5);
        map.verifyViewport();
        window.saveEvidence("floating-planner-low.png");
        map.togglePlanner();
        map.verifyFloatingPanel(false);
        map.verifyGameUnchanged();
    }

}
