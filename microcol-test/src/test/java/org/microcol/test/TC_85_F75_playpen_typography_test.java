package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ColonyNameDialog;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.TypographyPage;
import org.microcol.page.WelcomePage;
import org.microcol.page.WoodenButtonsPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Use the selected light font across real player journeys. */
@ExtendWith(ApplicationExtension.class)
public class TC_85_F75_playpen_typography_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_light_font_survives_languages_navigation_and_dialogs()
            throws Exception {
        final TypographyPage type = new TypographyPage(getContext());
        final WoodenButtonsPage buttons = new WoodenButtonsPage(getContext());
        // Menu and both Settings languages retain the same real bundled face.
        type.verifySceneTypography("buttonSetting");
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        type.verifySceneTypography("settingGrid");
        type.captureDialog("settingGrid", "playpen-settings-czech.png");
        settings.setEnglishLanguage();
        type.verifySceneTypography("settingGrid");
        settings.goBack().verifyMainScreen();
        // The legacy colony layout needs room for its fixed-position artwork.
        type.resizeGameWindow(1280, 900);
        // Loading, opening and cancelling a modal preserve the game state.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        type.verifySceneTypography("europe");
        buttons.click("congress");
        type.verifySceneTypography("congressClose");
        type.captureDialog("congressClose", "playpen-congress.png");
        buttons.click("congressClose");
        final EuropePortScreen europe = game.openEuroperPort();
        europe.verifyGold(10000);
        type.verifySceneTypography("closeButtonId");
        type.captureDialog("closeButtonId", "playpen-europe.png");
        europe.close();
        // Colony production and map captions share the selected canvas font.
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        colony.verifyColonyName("New Amsterdam");
        type.verifyVisibleCaption("colonyName", "New Amsterdam");
        type.verifySceneTypography("closeButtonId");
        type.verifyCanvasTypography();
        type.captureDialog("closeButtonId", "playpen-colony.png");
        type.hoverColonyCaption();
        type.verifyColonyTooltip();
        // Editing a Unicode draft uses the same font; Escape changes no name.
        final ColonyNameDialog rename = colony.rename();
        final String draft = "Příliš žluťoučký → français, Straße, Ĳssel €";
        rename.enterName(draft);
        rename.verifyDraft(draft);
        type.verifySceneTypography("colonyNameInput");
        type.captureDialog("colonyNameInput", "playpen-rename.png");
        rename.escape();
        colony.verifyColonyName("New Amsterdam");
        colony.close();
    }
}
