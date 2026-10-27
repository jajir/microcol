package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.GamePage;
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
        page.saveEvidence("r0307-goals.png");
        page.open("closeButtonId");
        page.open("statistics");
        page.verifyFrame(484, 1600);
        page.open("coloniesTab");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-statistics.png");
        page.open("relationsTab");
        page.verifyFrame(484, 1600);
        page.open("tradeTab");
        page.verifyFrame(484, 1600);
        page.open("baseScoreTab");
        page.verifyFrame(484, 1600);
        page.open("hallOfFameTab");
        page.verifyFrame(484, 1600);
        page.open("laborTab");
        page.verifyFrame(484, 1600);
        page.open("religionTab");
        page.verifyFrame(484, 1600);
        page.open("fleetTab");
        page.verifyFrame(484, 1600);
        page.open("closeButtonId");
        // Colony and port retain their actions above the illustrated content.
        game.openColonyWithKeyboard(0, "New Amsterdam");
        page.verifyFrame(484, 1600);
        page.verifyColonyNameVisible("New Amsterdam");
        page.saveEvidence("r0307-colony.png");
        page.open("closeButtonId");
        game.openEuroperPort();
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-europe.png");
        page.open("closeButtonId");
        // A smaller window scrolls only the report body.
        game.openTurnReport();
        window.resize(900, 700);
        page.verifyFrame(484, 1600);
        page.scrollToBottom();
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-turn-report-small.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        game.verifyGameInFront();
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
        // In-game Settings returns to the same game through its header.
        final SavedGamesPage saves = new SavedGamesPage(getContext());
        saves.openMenu();
        saves.chooseMenu("menuSettings");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-settings-czech.png");
        page.open("buttonBack");
        // Congress and help retain their keyboard cancellation.
        page.open("congress");
        page.verifyScreenTitle("Kongres");
        page.verifyFrame(484, 1600);
        page.scrollToBottom();
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-congress-czech.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        page.open("help");
        page.verifyScreenTitle("Colonizopedia");
        page.verifyFrame(484, 1600);
        page.saveEvidence("r0307-help-czech.png");
        page.closeWithEscape();
        page.verifyMapReturned();
        game.verifyGameInFront();
        page.verifyTurnUnchanged(2, 484);
    }
}
