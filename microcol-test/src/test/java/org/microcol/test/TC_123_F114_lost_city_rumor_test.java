package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.LostCityRumorRules.Outcome;
import org.microcol.model.NativeNation;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.LostCityRumorPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.microcol.test.LostCityRumorFixture.Fixture;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A Dutch scout enters a known lost-city rumor on a small island. The saved
 * random state selects the result before the game starts, so the scout's real
 * steps, the report, the one-time effect and Continue from disk are all played
 * without mocking the gameplay. The Aztec capital is two squares from the
 * marker, which makes it the nation a burial site angers.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_123_F114_lost_city_rumor_test extends AbstractMicroColTest {

    private static final Location START = LostCityRumorFixture.START;

    private static final Location RUMOR = LostCityRumorFixture.RUMOR;

    private static final Location OTHER_RUMOR = LostCityRumorFixture.OTHER_RUMOR;

    private static final Location BEYOND = LostCityRumorFixture.BEYOND;

    private static final int SCOUT = LostCityRumorFixture.SCOUT_ID;

    private static final String RUMOR_LABEL = "Lost city rumor";

    private static final String RUMOR_LABEL_CS = "Pověst o ztraceném městě";

    /** The saved game the Load command opens; each scenario picks its own. */
    private volatile File loadTarget;

    @Start
    private void start(final Stage stage) throws Exception {
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

    /** Select the scout that stands alone on the square. */
    private void selectScoutAt(final GamePage game, final Location at) {
        game.selectTile(at);
        game.getRightPanelUnit(0).selectUnit();
        game.moveMouseAtLocation(at);
    }

    @Test
    @Tag("local")
    void TC_01_treasure_is_paid_once_and_continue_never_pays_again()
            throws Exception {
        // A scout stands beside two known, unresolved rumor markers.
        final Fixture fixture = LostCityRumorFixture.create("gold",
                Outcome.GOLD);
        loadTarget = fixture.getFile();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        game.panToTile(START);
        rumor.verifyRumorMarker(RUMOR, true);
        rumor.verifyRumorMarker(OTHER_RUMOR, true);
        rumor.verifyRumorMarkerDrawn(RUMOR, true);
        rumor.verifyRumorMarkerDrawn(OTHER_RUMOR, true);
        rumor.verifyTreasury(fixture.getTreasury());
        rumor.saveMapEvidence("lost-city-rumor-marker-en.png");

        // Selecting the scout and looking at the marker resolves nothing; the
        // status bar says in words what the marker is.
        selectScoutAt(game, START);
        game.moveMouseAtLocation(RUMOR);
        rumor.verifyStatusBarNamesRumor(RUMOR_LABEL, true);
        rumor.verifyRumorMarker(RUMOR, true);
        rumor.verifyTreasury(fixture.getTreasury());
        rumor.verifyRumorNoticeCount(0);

        // The real step onto the marker pays the treasure and opens the report.
        game.moveMouseAtLocation(START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "The explorers found a treasure in a lost city: "
                        + fixture.getGold() + " gold.");
        report.captureDialog("closeButtonId", "lost-city-rumor-gold-en.png");
        report.close();
        game.waitWhileMoving();
        rumor.verifyScoutAt(SCOUT, RUMOR);
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());
        rumor.verifyRumorNoticeCount(1);

        // The used marker is gone; the other one is untouched.
        rumor.verifyRumorMarker(RUMOR, false);
        rumor.verifyRumorMarker(OTHER_RUMOR, true);

        // Stepping away and back is an ordinary move: nothing is paid again.
        selectScoutAt(game, RUMOR);
        game.dragMouseAtLocation(BEYOND);
        rumor.verifyNoReportShown();
        rumor.verifyRumorMarkerDrawn(RUMOR, false);
        rumor.verifyRumorMarkerDrawn(OTHER_RUMOR, true);
        rumor.saveMapEvidence("lost-city-rumor-resolved-en.png");
        game.moveMouseAtLocation(BEYOND);
        game.dragMouseAtLocation(RUMOR);
        rumor.verifyNoReportShown();
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());
        rumor.verifyRumorNoticeCount(1);

        // Continue keeps the removed marker, the treasure and the saved notice.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.panToTile(RUMOR);
        rumor.verifyRumorMarker(RUMOR, false);
        rumor.verifyRumorMarker(OTHER_RUMOR, true);
        rumor.verifyScoutAt(SCOUT, RUMOR);
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());
        rumor.verifyRumorNoticeCount(1);
        rumor.verifyNoReportShown();

        // The pending notice can be read again and pays nothing.
        final ScreenTurnReport pending = game.openTurnReport();
        pending.verifyThatAtLeastOneEventMessageContains(
                "The explorers found a treasure in a lost city: "
                        + fixture.getGold() + " gold.");
        pending.close();
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());

        // A move after Continue still pays nothing from the used marker.
        selectScoutAt(game, RUMOR);
        game.dragMouseAtLocation(BEYOND);
        game.moveMouseAtLocation(BEYOND);
        game.dragMouseAtLocation(RUMOR);
        rumor.verifyNoReportShown();
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());
        rumor.verifyRumorNoticeCount(1);
    }

    @Test
    @Tag("local")
    void TC_02_burial_site_angers_the_nearest_nation_once_and_survives_continue()
            throws Exception {
        // The Dutch have no anger toward the Aztecs before the scout moves.
        final Fixture fixture = LostCityRumorFixture.create("burial",
                Outcome.BURIAL);
        loadTarget = fixture.getFile();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        final NativeRelationsPage relations = new NativeRelationsPage(
                getContext());
        game.panToTile(START);
        rumor.verifyRumorMarkerDrawn(RUMOR, true);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);

        // Entering the marker disturbs the burial site of the nearest nation.
        selectScoutAt(game, START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "The explorers disturbed a native burial site");
        report.captureDialog("closeButtonId", "lost-city-rumor-burial-en.png");
        report.close();
        game.waitWhileMoving();
        rumor.verifyScoutAt(SCOUT, RUMOR);
        rumor.verifyTreasury(fixture.getTreasury());
        rumor.verifyRumorMarker(RUMOR, false);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                30);

        // Revisiting the used marker adds no second disturbance.
        selectScoutAt(game, RUMOR);
        game.dragMouseAtLocation(BEYOND);
        game.moveMouseAtLocation(BEYOND);
        game.dragMouseAtLocation(RUMOR);
        rumor.verifyNoReportShown();
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                30);

        // Continue keeps the anger and does not apply the disturbance again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rumor.verifyRumorMarker(RUMOR, false);
        rumor.verifyRumorNoticeCount(1);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                30);
    }

    @Test
    @Tag("local")
    void TC_03_treasure_report_and_marker_are_shown_in_czech()
            throws Exception {
        // Switch the interface to Czech before the game is loaded.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        final Fixture fixture = LostCityRumorFixture.create("gold",
                Outcome.GOLD);
        loadTarget = fixture.getFile();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        game.panToTile(START);
        rumor.verifyRumorMarkerDrawn(RUMOR, true);
        rumor.saveMapEvidence("lost-city-rumor-marker-cs.png");

        // The status bar names the marker in Czech.
        selectScoutAt(game, START);
        game.moveMouseAtLocation(RUMOR);
        rumor.verifyStatusBarNamesRumor(RUMOR_LABEL_CS, true);

        // The same real step gives the report in Czech with the actual amount.
        game.moveMouseAtLocation(START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "našli poklad: " + fixture.getGold() + " zlatých.");
        report.captureDialog("closeButtonId", "lost-city-rumor-gold-cs.png");
        report.close();
        game.waitWhileMoving();
        rumor.verifyTreasury(fixture.getTreasury() + fixture.getGold());
        rumor.verifyRumorMarker(RUMOR, false);
    }

    @Test
    @Tag("local")
    void TC_04_lost_explorer_leaves_the_game_and_the_marker_is_used_up()
            throws Exception {
        // The scout is the only Dutch unit that stands on the island.
        final Fixture fixture = LostCityRumorFixture.create("lost",
                Outcome.EXPLORER_LOST);
        loadTarget = fixture.getFile();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        game.panToTile(START);
        rumor.verifyRumorMarkerDrawn(RUMOR, true);

        // The step onto the marker loses the expedition and only the explorer.
        selectScoutAt(game, START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "The expedition was lost: the explorer did not return.");
        report.captureDialog("closeButtonId", "lost-city-rumor-lost-en.png");
        report.close();
        game.waitWhileMoving();
        rumor.verifyScoutGone(SCOUT);
        rumor.verifyUnitsAt(RUMOR, 0);
        rumor.verifyTreasury(fixture.getTreasury());
        rumor.verifyRumorMarker(RUMOR, false);
        rumor.verifyRumorMarkerDrawn(RUMOR, false);

        // Without the scout no Dutch unit sees the island any more. The other
        // marker still waits in the game, but the remembered square shows and
        // names no rumor, so it reveals nothing that happens out of sight.
        rumor.verifyRumorMarker(OTHER_RUMOR, true);
        rumor.verifyRumorMarkerDrawn(OTHER_RUMOR, false);
        game.moveMouseAtLocation(OTHER_RUMOR);
        rumor.verifyStatusBarNamesRumor(RUMOR_LABEL, false);
        rumor.saveMapEvidence("lost-city-rumor-lost-map-en.png");

        // Continue keeps the loss; neither the unit nor the marker returns.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rumor.verifyScoutGone(SCOUT);
        rumor.verifyRumorMarker(RUMOR, false);
        rumor.verifyRumorNoticeCount(1);
    }

    @Test
    @Tag("local")
    void TC_05_recruit_joins_the_scout_on_the_explored_square()
            throws Exception {
        // One scout stands beside the marker.
        final Fixture fixture = LostCityRumorFixture.create("recruit",
                Outcome.RECRUIT);
        loadTarget = fixture.getFile();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        game.panToTile(START);
        rumor.verifyUnitsAt(RUMOR, 0);

        // The step onto the marker adds exactly one free colonist there.
        selectScoutAt(game, START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "A lost colonist joined the expedition");
        report.close();
        game.waitWhileMoving();
        rumor.verifyScoutAt(SCOUT, RUMOR);
        rumor.verifyUnitsAt(RUMOR, 2);
        rumor.verifyTreasury(fixture.getTreasury());

        // Continue keeps the one recruit and does not add another.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rumor.verifyUnitsAt(RUMOR, 2);
        rumor.verifyRumorMarker(RUMOR, false);
    }

    @Test
    @Tag("local")
    void TC_06_empty_rumor_is_reported_and_consumed() throws Exception {
        // One scout stands beside the marker.
        final Fixture fixture = LostCityRumorFixture.create("empty",
                Outcome.EMPTY);
        loadTarget = fixture.getFile();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final LostCityRumorPage rumor = new LostCityRumorPage(getContext());
        game.panToTile(START);
        rumor.verifyRumorMarker(RUMOR, true);

        // An empty result is still reported and still consumes the marker.
        selectScoutAt(game, START);
        final ScreenTurnReport report = game
                .dragMouseAtLocationAndOpenReport(RUMOR);
        report.verifyThatAtLeastOneEventMessageContains(
                "The rumor led nowhere: the explorers found nothing.");
        report.close();
        game.waitWhileMoving();
        rumor.verifyScoutAt(SCOUT, RUMOR);
        rumor.verifyTreasury(fixture.getTreasury());
        rumor.verifyUnitsAt(RUMOR, 1);
        rumor.verifyRumorMarker(RUMOR, false);
    }

}
