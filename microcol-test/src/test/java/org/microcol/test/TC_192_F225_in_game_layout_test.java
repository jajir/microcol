package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.GamePage;
import org.microcol.page.GameBarWidthPage;
import org.microcol.page.InGameLayoutPage;
import org.microcol.page.MainScreenLayoutPage;
import org.microcol.page.SavedGamesPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** R-0307, UC-114/115/119: navigation has fixed controls and live status. */
@ExtendWith(ApplicationExtension.class)
public class TC_192_F225_in_game_layout_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, this.getClass(),
                new File("src/test/scenarios/F221-game-toolbar.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_reports_colony_and_europe_keep_the_frame() throws Exception {
        // Open the agreed position and retain the same turn and balance.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1000, 925);
        // Goals and every statistics tab share the frame.
        page.open("goals");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.saveEvidence("r0307-goals.png");
        page.open("closeButtonId");
        page.open("statistics");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("coloniesTab");
        page.verifySelectedTab("coloniesTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.saveEvidence("r0307-statistics.png");
        page.open("relationsTab");
        page.verifySelectedTab("relationsTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("tradeTab");
        page.verifySelectedTab("tradeTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("baseScoreTab");
        page.verifySelectedTab("baseScoreTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("hallOfFameTab");
        page.verifySelectedTab("hallOfFameTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("laborTab");
        page.verifySelectedTab("laborTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("religionTab");
        page.verifySelectedTab("religionTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("fleetTab");
        page.verifySelectedTab("fleetTab");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.open("closeButtonId");
        // Colony and port retain their actions above the illustrated content.
        game.openColonyWithKeyboard(0, "New Amsterdam");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.verifyColonyNameVisible("New Amsterdam");
        page.saveEvidence("r0307-colony.png");
        // Escape also works when focus is in the shared toolbar.
        new org.microcol.page.PaperButtonsPage(getContext())
                .focus("closeButtonId");
        page.closeWithEscape();
        page.verifyMapReturned();
        game.openEuroperPort();
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.saveEvidence("r0307-europe.png");
        page.open("closeButtonId");
        // A smaller window scrolls only the report body.
        game.openTurnReport();
        window.resize(900, 700);
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.scrollToBottom();
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("closeButtonId", "Back to game");
        page.saveEvidence("r0307-turn-report-small.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        // Help and in-game Settings share the same visible return label.
        page.open("help");
        page.verifyBackToGame("buttonOk", "Back to game");
        page.open("buttonOk");
        page.verifyMapReturned();
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        page.verifyBackToGame("buttonBack", "Back to game");
        page.open("buttonBack");
        page.verifyMapReturned();
        game.verifyGameInFront();
        page.verifyTurnUnchanged(2, 484);
    }

    @Test
    @Tag("local")
    void TC_03_atlas_charts_mouse_keyboard_resize_and_return()
            throws Exception {
        // The atlas starts with the real recorded treasury, not a mock value.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 850);
        page.open("statistics");
        page.verifyAtlasLayout("Gold");
        page.verifyAtlasHistory(198, 484, 1649,
                "Last recorded value: 484 · Year 1649");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0323-atlas-gold-en.png");

        // Repeated selection keeps its cue; arrows select the next report.
        page.open("goldTab");
        page.verifySelectedTab("goldTab");
        page.selectNextReportWithKeyboard();
        page.verifySelectedTab("militaryTab");
        page.verifyAtlasHistory(198, 27, 1649,
                "Last recorded value: 27 · Year 1649");
        page.open("wealthTab");
        page.verifyAtlasHistory(198, 1551, 1649,
                "Last recorded value: 1551 · Year 1649");
        page.open("scoreTab");
        page.verifyAtlasHistory(198, 2062, 1649,
                "Last recorded value: 2062 · Year 1649");

        // A small window keeps the last index entry and the report reachable.
        window.resize(900, 700);
        page.open("fleetTab");
        page.verifyAtlasLayout("Fleet");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0323-atlas-fleet-small.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        page.verifyTurnUnchanged(2, 484);
        game.verifyGameInFront();
    }

    @Test
    @Tag("local")
    void TC_04_czech_atlas_empty_and_single_record_refresh_on_reopen()
            throws Exception {
        // Czech labels and an empty saved history have an explicit explanation.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        new MainScreenLayoutPage(getContext()).resize(1000, 800);
        page.loadAtlasHistoryFixture(0);
        page.open("statistics");
        page.verifyAtlasLayout("Zlato");
        page.verifyAtlasHistory(0, 0, 0,
                "Historie je zatím prázdná. Záznamy přibudou po dokončení tahu.");
        page.saveEvidence("r0323-atlas-empty-cs.png");
        page.closeWithEscape();

        // Reopening the same selected tab reads the replacement game's record.
        page.loadAtlasHistoryFixture(1);
        page.open("statistics");
        page.verifyAtlasLayout("Zlato");
        page.verifyAtlasHistory(1, 2108, 1600,
                "Poslední zaznamenaná hodnota: 2108 · Rok 1600");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0323-atlas-single-cs.png");
        page.open("closeButtonId");
        page.verifyMapReturned();
        page.verifyTurnUnchanged(2, 484);
    }

    @Test
    @Tag("local")
    void TC_02_czech_settings_congress_and_help_keep_the_frame()
            throws Exception {
        // Choose Czech through the existing startup settings.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        new MainScreenLayoutPage(getContext()).resize(1000, 925);
        // Shared return controls retain their Czech text after changing
        // language.
        page.open("goals");
        page.verifyBackToGame("closeButtonId", "Zpět do hry");
        page.open("closeButtonId");
        page.open("statistics");
        page.verifyBackToGame("closeButtonId", "Zpět do hry");
        page.open("closeButtonId");
        game.openColonyWithKeyboard(0, "New Amsterdam");
        page.verifyBackToGame("closeButtonId", "Zpět do hry");
        page.closeWithEscape();
        game.openEuroperPort("Evropa");
        page.verifyBackToGame("closeButtonId", "Zpět do hry");
        page.open("closeButtonId");
        game.openTurnReport();
        page.verifyBackToGame("closeButtonId", "Zpět do hry");
        page.closeWithEscape();
        page.verifyMapReturned();

        // In-game Settings returns to the same game through its header.
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("buttonBack", "Zpět do hry");
        page.saveEvidence("r0307-settings-czech.png");
        page.open("buttonBack");
        // Congress and help retain their keyboard cancellation.
        final org.microcol.page.CongressPage congress = new org.microcol.page.CongressPage(
                getContext());
        congress.open();
        congress.verifyFillsGameWindow("Zpět do hry");
        congress.verifyHeading("Kongres");
        page.verifyBackToGame("congressClose", "Zpět do hry");
        page.verifyFrame(484, 1600);
        page.scrollToBottom();
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-congress-czech.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        page.open("help");
        org.microcol.page.ColonizopediaPage.of(getContext())
                .verifyFillsGameWindow("Colonizopedia");
        page.verifyFrame(484, 1600);
        page.verifyBackToGame("buttonOk", "Zpět do hry");
        page.saveEvidence("r0325-help-czech.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        game.verifyGameInFront();
        page.verifyTurnUnchanged(2, 484);
    }

    @Test
    @Tag("local")
    void TC_05_wide_bars_keep_shared_limit_and_shrink_back() throws Exception {
        // Open the agreed position and exercise both sides of the width limit.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final GameBarWidthPage bars = new GameBarWidthPage(getContext());
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        bars.resizeContent(1472, 900);
        bars.verifyContents(1472, 1440);
        bars.resizeContent(1470, 900);
        bars.verifyContents(1470, 1438);
        bars.resizeContent(1000, 900);
        bars.verifyContents(1000, 968);

        // Reports and work screens retain the same centered frame.
        page.open("goals");
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.open("closeButtonId");
        page.open("statistics");
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0340-wide-statistics.png");
        bars.resizeContent(1000, 900);
        page.closeWithEscape();
        game.openColonyWithKeyboard(0, "New Amsterdam");
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.closeWithEscape();
        game.openEuroperPort();
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0340-wide-europe.png");
        // All Europe action names fit both wide and supported narrow windows.
        page.verifyEuropeActionLabels("Recruit", "Buy units", "Ship repairs",
                "Redeem treasure", "Back to game");
        page.saveEvidence("r0347-europe-labels-en.png");
        bars.resizeContent(1000, 900);
        page.verifyEuropeActionLabels("Recruit", "Buy units", "Ship repairs",
                "Redeem treasure", "Back to game");
        page.saveEvidence("r0347-europe-narrow-en.png");
        bars.resizeContent(1280, 900);
        page.verifyEuropeActionLabels("Recruit", "Buy units", "Ship repairs",
                "Redeem treasure", "Back to game");
        bars.resizeContent(1000, 900);
        page.open("closeButtonId");
        game.openTurnReport();
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.closeWithEscape();

        // Shrinking remains possible after navigating through capped toolbars.
        bars.resizeContent(1000, 900);
        bars.verifyContents(1000, 968);
        page.verifyMapReturned();
        page.verifyTurnUnchanged(2, 484);
    }

    @Test
    @Tag("local")
    void TC_06_czech_wide_readers_and_settings_keep_navigation()
            throws Exception {
        // Longer Czech labels use the same limit and existing controls.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final GameBarWidthPage bars = new GameBarWidthPage(getContext());
        final InGameLayoutPage page = new InGameLayoutPage(getContext());
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.open("buttonBack");
        new org.microcol.page.CongressPage(getContext()).open();
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.closeWithEscape();
        page.open("help");
        bars.resizeContent(2560, 900);
        bars.verifyContents(2560, 1440);
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0340-wide-help-czech.png");
        bars.resizeContent(1000, 900);
        bars.verifyContents(1000, 968);
        page.verifyFrame(484, 1600);
        bars.resizeContent(1000, 900);
        page.closeWithEscape();
        page.verifyMapReturned();
        // Czech action names also survive narrowing and reopening the port.
        page.open("europe");
        bars.resizeContent(1280, 900);
        page.verifyEuropeActionLabels("Nábor", "Koupit jednotky", "Opravy lodí",
                "Vyplatit poklad", "Zpět do hry");
        page.saveEvidence("r0347-europe-labels-cs.png");
        bars.resizeContent(1000, 900);
        page.verifyEuropeActionLabels("Nábor", "Koupit jednotky", "Opravy lodí",
                "Vyplatit poklad", "Zpět do hry");
        bars.resizeContent(1280, 900);
        page.open("closeButtonId");
        page.open("europe");
        page.verifyEuropeActionLabels("Nábor", "Koupit jednotky", "Opravy lodí",
                "Vyplatit poklad", "Zpět do hry");
        page.open("closeButtonId");
        page.verifyMapReturned();
        page.verifyTurnUnchanged(2, 484);
    }

}
