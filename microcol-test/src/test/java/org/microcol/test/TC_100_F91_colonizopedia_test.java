package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.ColonizopediaPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0206, UC-114: the Colonizopedia opens from the map help button and from H,
 * lists every current rule topic in English and Czech, searches localized title
 * and body text, reports a search without results, scrolls long text and closes
 * with OK or Escape. Opening and closing change no turn, gold, unit or cargo.
 * Fixture purpose: F19 is the Dutch free-play reference game at turn 18 with
 * 1000 gold; the help must leave that state untouched.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_100_F91_colonizopedia_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.microcol");
    private static final int TURN = 18;
    private static final int GOLD = 1000;
    private static final String MOVEMENT = "Movement";
    private static final String CARGO = "Cargo and passengers";
    private static final String COLONIES = "Colonies and work";
    private static final String PRODUCTION = "Production and construction";
    private static final String TRADE = "Trade, taxes and boycotts";
    private static final String DIPLOMACY = "Diplomacy";
    private static final String COMBAT = "Combat";
    private static final String INDEPENDENCE = "Independence";
    private static final String SCORING = "Statistics and score";
    private static final String[] TOPICS = { MOVEMENT, CARGO, COLONIES,
            PRODUCTION, TRADE, DIPLOMACY, COMBAT, INDEPENDENCE, SCORING };
    private static final String MOVEMENT_PART = "movement points";
    private static final String COLONIES_PART = "Colony names must be unique";
    private static final String DIPLOMACY_PART = "tribute";
    private static final String SCORING_PART = "not your final result";
    private static final String SCORING_NOT_AVAILABLE = "Not available yet";
    private static final String SCORING_HALL_OF_FAME = "Hall of Fame";
    private static final String SCORING_FORMULA = "Wealth plus Military plus Gold";
    private static final String SCORING_END = "are also not complete yet";
    private static final String CARGO_ASHORE = "step ashore";
    private static final String CARGO_LIMIT = "at most 100 units";
    private static final String NO_RESULTS = "No topic matches this search. Clear the search to see all topics.";

    private static final String MOVEMENT_CZ = "Pohyb";
    private static final String CARGO_CZ = "Náklad a cestující";
    private static final String DIPLOMACY_CZ = "Diplomacie";
    private static final String PRODUCTION_CZ = "Výroba a stavba";
    private static final String MOVEMENT_PART_CZ = "pohybové body";
    private static final String CARGO_ASHORE_CZ = "na břeh";
    private static final String SCORING_NOT_AVAILABLE_CZ = "Zatím není dostupné";
    private static final String[] TOPICS_CZ = { MOVEMENT_CZ, CARGO_CZ,
            "Kolonie a práce", PRODUCTION_CZ, "Obchod, daně a bojkot",
            DIPLOMACY_CZ, "Boj", "Nezávislost", "Statistiky a skóre" };
    private static final String NO_RESULTS_CZ = "Žádné téma neodpovídá hledání. Vymažte hledaný text a zobrazíte všechna témata.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    /**
     * The help dialog is modal. A scenario that fails while it is open must not
     * leave it in front of the next scenario.
     */
    @AfterEach
    void closeHelpLeftOpen() {
        ColonizopediaPage.of(getContext()).closeIfOpen();
    }

    @Test
    @Tag("local")
    void TC_01_help_button_lists_all_topics_and_keyboard_selection()
            throws Exception {
        // Open the map help button on the reference game.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openFromButton();
        help.verifyDialogOpen();
        help.verifyTopicListHasFocus();

        // Every rule family is listed; the first topic is shown at once.
        help.verifyTopicTitles(TOPICS);
        help.verifySelectedTopic(MOVEMENT, MOVEMENT_PART);

        // Down arrow moves the selection without the mouse.
        help.pressDownArrow(2);
        help.verifySelectedTopic(COLONIES, COLONIES_PART);
        help.pressDownArrow(6);
        help.verifySelectedTopic(SCORING, SCORING_PART);

        // A mouse click selects a topic and shows its own text.
        help.selectTopicWithMouse(DIPLOMACY);
        help.verifySelectedTopic(DIPLOMACY, DIPLOMACY_PART);
        help.closeWithOk();
        help.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_02_search_finds_title_and_body_and_reports_no_results()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openWithHKey();
        help.verifyDialogOpen();

        // A word found only in the body text finds its topic. Surrounding
        // spaces are trimmed before the search.
        help.typeSearch("  tribute  ");
        help.verifyTopicTitles(DIPLOMACY);
        help.verifyNoResultsHidden();

        // Another body-only word finds the cargo topic and its passenger text.
        help.typeSearch("ashore");
        help.verifyTopicTitles(CARGO);
        help.verifyBodyContains(CARGO_ASHORE);
        help.verifyBodyContains(CARGO_LIMIT);

        // The title matches regardless of letter case.
        help.typeSearch("diplomacy");
        help.verifyTopicTitles(DIPLOMACY);

        // A search without a match explains itself and shows no topic.
        help.typeSearch("zzzz");
        help.verifyTopicTitles();
        help.verifyNoResults(NO_RESULTS);

        // Clearing the search shows every topic again.
        help.typeSearch("");
        help.verifyTopicTitles(TOPICS);
        help.verifyNoResultsHidden();
        help.closeWithEscape();
        help.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_03_scoring_states_missing_parts_and_scrolls_to_end()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openFromButton();

        // The scoring topic states the recorded formula and what is missing.
        help.selectTopicWithMouse(SCORING);
        help.verifySelectedTopic(SCORING, SCORING_PART);
        help.verifyBodyContains(SCORING_FORMULA);
        help.verifyBodyContains(SCORING_NOT_AVAILABLE);
        help.verifyBodyContains(SCORING_HALL_OF_FAME);

        // The long text scrolls with the mouse wheel to its last sentence.
        help.scrollBodyDown();
        help.verifyBodyScrolledToEnd();
        help.verifyBodyContains(SCORING_END);
        help.closeWithOk();
        help.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_04_h_escape_and_ok_keep_the_game_unchanged() throws Exception {
        // Record the reference turn, gold, units and cargo before any help
        // action.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.verifyTurnAndGold(TURN, GOLD);

        // H opens the same encyclopedia; Escape closes it and changes nothing.
        help.openWithHKey();
        help.verifyDialogOpen();
        help.selectTopicWithMouse(COMBAT);
        help.verifySelectedTopic(COMBAT, "attack");
        help.closeWithEscape();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyTurnAndGold(TURN, GOLD);
        help.verifyNextTurnEnabled();

        // Focus returns to the game: H opens the dialog again and OK closes it.
        help.openWithHKey();
        help.verifyDialogOpen();
        help.closeWithOk();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyTurnAndGold(TURN, GOLD);
        help.verifyNextTurnEnabled();
    }

    @Test
    @Tag("local")
    void TC_05_czech_topics_search_and_escape() throws Exception {
        // Use the Czech language preference before the game opens.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.verifyLanguageOptionsShown();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openFromButton();
        help.verifyDialogOpen();

        // The Czech headings and the first Czech text are shown.
        help.verifyTopicTitles(TOPICS_CZ);
        help.verifySelectedTopic(MOVEMENT_CZ, MOVEMENT_PART_CZ);

        // Czech body text is searched; accents are ignored when typing.
        help.typeSearch("tribut");
        help.verifyTopicTitles(DIPLOMACY_CZ);
        help.typeSearch("breh");
        help.verifyTopicTitles(CARGO_CZ);
        help.verifyBodyContains(CARGO_ASHORE_CZ);
        help.typeSearch("vyroba");
        help.verifyTopicTitles(PRODUCTION_CZ);

        // The no-results message and the scoring text are in Czech too.
        help.typeSearch("zzzz");
        help.verifyNoResults(NO_RESULTS_CZ);
        help.typeSearch("");
        help.verifyTopicTitles(TOPICS_CZ);
        help.selectTopicWithMouse("Statistiky a skóre");
        help.verifyBodyContains(SCORING_NOT_AVAILABLE_CZ);
        help.closeWithEscape();
        help.verifyDialogClosed();
    }
}
