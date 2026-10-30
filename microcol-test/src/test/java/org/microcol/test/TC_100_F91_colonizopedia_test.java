package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.ColonizopediaPage;
import org.microcol.page.MainScreenLayoutPage;
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
            PRODUCTION, TRADE, DIPLOMACY, COMBAT, INDEPENDENCE, SCORING,
            "Terrain", "Terrain bonuses", "Units", "Buildings" };
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
            DIPLOMACY_CZ, "Boj", "Nezávislost", "Statistiky a skóre", "Terén",
            "Bonusy terénu", "Jednotky", "Budovy" };
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
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 850);
        help.verifyFillsGameWindow("Colonizopedia");
        help.captureDialog("colonizopediaPage", "r0329-help-full-en.png");
        help.verifyHandDrawnFramesWithoutStripes();
        window.resize(900, 700);
        help.verifyFillsGameWindow("Colonizopedia");
        // Snapshot flushes the native controls' layout after resizing.
        help.captureDialog("colonizopediaPage", "r0329-help-small-en.png");
        help.verifyHandDrawnFramesWithoutStripes();

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

        // The final sentence remains visible after scrolling, even when the
        // prose fits.
        help.scrollBodyDown();
        help.verifyBodyEndVisible();
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
        help.typeSearch("attack");
        help.selectTopicWithMouse(COMBAT);
        help.verifySelectedTopic(COMBAT, "attack");
        help.closeWithEscape();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyTurnAndGold(TURN, GOLD);
        help.verifyNextTurnEnabled();

        // Focus returns to the game: H opens help again and clears the search.
        help.openWithHKey();
        help.verifyDialogOpen();
        help.verifyFillsGameWindow("Colonizopedia");
        help.verifyTopicTitles(TOPICS);
        help.verifyTopicListHasFocus();
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

    @Test
    @Tag("local")
    void TC_06_illustrated_terrain_and_bonus_reference() throws Exception {
        // Open help without changing the running game's state.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openWithHKey();

        // Find terrain by a table entry and inspect all open/forest
        // illustrations.
        help.typeSearch("tundra");
        help.verifyTopicTitles("Terrain");
        help.verifySelectedTopic("Terrain", "base output per worker");
        help.verifyReferenceRows(14);
        help.verifyTerrainIllustrations();
        help.verifySelectedTerrainArtwork();
        help.verifyReferenceProperties(
                "colonizopediaTerrainProperties_GRASSLAND",
                "Move cost: 1; defense: +0%", "Corn 4", "With forest",
                "Move cost: 2; defense: +50%", "Lumber 6", "Fields: yes");
        help.verifyReferenceProperties(
                "colonizopediaTerrainProperties_MOUNTAIN",
                "Move cost: 3; defense: +150%", "Silver 1", "Fields: no");
        help.verifyReferenceProperties("colonizopediaTerrainProperties_ARCTIC",
                "Base production: none");
        help.captureDialog("colonizopediaReference", "r0274-terrain-en.png");
        help.scrollReferenceToEnd();
        help.verifyBodyScrolledToEnd();
        help.captureDialog("colonizopediaReference",
                "r0274-terrain-end-en.png");

        // Resource names are searchable; the legend uses selected map pictures
        // and actual
        // reserves.
        help.typeSearch("silver deposit");
        help.verifyTopicTitles("Terrain bonuses");
        help.verifyReferenceRows(12);
        help.verifyResourceIllustrations();
        help.verifyReferenceProperties("colonizopediaResourceProperties_SILVER",
                "Silver +2", "Silver 100", "Grass land", "Mountains");
        help.verifyReferenceProperties(
                "colonizopediaResourceProperties_MIXED_MINERALS",
                "Ore +2, Silver +2", "Ore 200, Silver 200");
        help.verifyReferenceProperties(
                "colonizopediaResourceProperties_FISHERY",
                "Allowed terrain: Ocean, Lake", "Corn +2", "Does not deplete.");
        help.captureDialog("colonizopediaReference", "r0274-bonuses-en.png");
        help.scrollReferenceToEnd();
        help.verifyBodyScrolledToEnd();
        help.captureDialog("colonizopediaReference",
                "r0274-bonuses-end-en.png");
        help.closeWithOk();
        help.verifyGameStateUnchanged(before);
    }

    @Test
    @Tag("local")
    void TC_07_czech_terrain_and_bonus_reference() throws Exception {
        // Use the same player path in Czech, including accent-insensitive
        // search.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openFromButton();
        help.typeSearch("teren");
        help.verifyTopicTitles(MOVEMENT_CZ, PRODUCTION_CZ, "Terén",
                "Bonusy terénu");
        help.selectTopicWithMouse("Terén");
        help.verifyReferenceRows(14);
        help.verifyTerrainIllustrations();
        help.verifySelectedTerrainArtwork();
        help.verifyReferenceProperties(
                "colonizopediaTerrainProperties_GRASSLAND",
                "Cena pohybu: 1; obrana: +0%", "Jídlo 4", "S lesem", "Dříví 6",
                "Pole: ano");
        help.captureDialog("colonizopediaReference", "r0274-terrain-cs.png");

        // Search a localized bonus name without accents and inspect the
        // translated legend.
        help.typeSearch("stribrne");
        help.verifyTopicTitles("Bonusy terénu");
        help.verifyReferenceRows(12);
        help.verifyResourceIllustrations();
        help.verifyReferenceProperties("colonizopediaResourceProperties_SILVER",
                "Stříbro +2", "Počáteční zásoba bonusu: Stříbro 100");
        help.verifyReferenceProperties(
                "colonizopediaResourceProperties_FISHERY",
                "Povolený terén: Oceán, Jezero", "Jídlo +2", "Nevyčerpává se.");
        help.captureDialog("colonizopediaReference", "r0274-bonuses-cs.png");
        help.closeWithEscape();
        help.verifyGameStateUnchanged(before);
        help.verifyNextTurnEnabled();
    }

    @Test
    @Tag("local")
    void TC_08_units_reference_lists_types_and_base_properties()
            throws Exception {
        // Open the same help from the running reference game.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openWithHKey();
        help.selectTopicWithMouse("Units");
        help.verifySelectedTopic("Units",
                "Equipment can change movement and combat");
        help.verifyReferenceRows(35);
        help.verifyUnitIllustrations();
        // Check ships, civilians, native fighters, treasure and specialists.
        help.verifyReferenceProperties("colonizopediaUnitProperties_FRIGATE",
                "base points per turn: 6", "Cargo holds: 4", "4000 gold",
                "combat: yes");
        help.verifyReferenceProperties("colonizopediaUnitProperties_CARAVEL",
                "base points per turn: 4", "Cargo holds: 2", "combat: no");
        help.verifyReferenceProperties("colonizopediaUnitProperties_MAN_OF_WAR",
                "Cargo holds: 6", "Europe purchase: unavailable");
        help.verifyReferenceProperties("colonizopediaUnitProperties_COLONIST",
                "Transport by ship: 1 hold(s)", "with muskets",
                "plow: yes; roads: yes; clear forest: yes");
        help.verifyReferenceProperties("colonizopediaUnitProperties_BRAVE",
                "Native combatant", "Transport by ship: unavailable");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_TREASURE_TRAIN",
                "Transport by ship: 6 hold(s)",
                "cannot work or found a colony");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_EXPERT_ORE_MINER", "Ore ×2.0");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_EXPERT_FARMER",
                "land fields only");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_EXPERT_FISHERMAN",
                "water fields only");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_HARDY_PIONEER",
                "completes improvements faster");
        help.verifyHandDrawnFramesWithoutStripes();
        help.captureDialog("colonizopediaPage", "r0329-help-units-en.png");
        help.captureDialog("colonizopediaReference", "r0284-units-en.png");
        // Unit names are searchable; the full reference remains accessible.
        help.typeSearch("ore miner");
        help.verifyTopicTitles("Units");
        help.verifyReferenceRows(35);
        // The sketched native thumb remains draggable to the last unit.
        help.verifyFillsGameWindow("Colonizopedia");
        help.verifyHandDrawnBodyScrollbar();
        help.captureDialog("colonizopediaPage", "r0325-help-units-en.png");
        help.dragBodyThumbToEnd();
        help.verifyBodyScrolledToEnd();
        help.scrollReferenceToEnd();
        help.scrollReferenceToEnd();
        help.scrollReferenceToEnd();
        help.verifyBodyScrolledToEnd();
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_MASTER_TOBACCO_PLANTER",
                "Europe purchase: unavailable", "Tobacco ×2.0");
        help.captureDialog("colonizopediaReference", "r0284-units-end-en.png");
        help.closeWithOk();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
    }

    @Test
    @Tag("local")
    void TC_09_czech_units_search_and_escape() throws Exception {
        // Select Czech before opening the game, then search without accents.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openFromButton();
        help.typeSearch("karavela");
        help.verifyTopicTitles("Jednotky");
        help.verifySelectedTopic("Jednotky", "Výstroj může měnit pohyb a boj");
        help.verifyReferenceRows(35);
        help.verifyUnitIllustrations();
        help.verifyReferenceProperties("colonizopediaUnitProperties_CARAVEL",
                "základní body za tah: 4", "Nákladové oddíly: 2", "1000 zlata",
                "Může zahájit boj: ne");
        help.verifyReferenceProperties(
                "colonizopediaUnitProperties_MASTER_BLACKSMITH", "Nářadí ×2.0");
        help.captureDialog("colonizopediaReference", "r0284-units-cs.png");
        help.typeSearch("delostrelectvo");
        help.verifyTopicTitles("Jednotky");
        help.verifyReferenceProperties("colonizopediaUnitProperties_ARTILLERY",
                "Poškození je oslabuje");
        help.closeWithEscape();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyNextTurnEnabled();
    }

    /**
     * Every building level has current artwork, rules and full upgrade costs.
     */
    @Test
    @Tag("local")
    void TC_10_buildings_reference_and_upgrade_costs() throws Exception {
        // Open the new topic through the existing in-game help.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openWithHKey();
        help.selectTopicWithMouse("Buildings");
        help.verifySelectedTopic("Buildings",
                "not the difference between levels");
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 850);
        help.verifyFillsGameWindow("Colonizopedia");
        help.verifyReferenceRows(41);
        help.verifyBuildingReference("Cost: %s hammers, %s tools",
                "Minimum population: %s", "Present from colony founding.");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_LUMBER_MILL",
                "52 hammers, 0 tools", "Minimum population: 3",
                "Base output per worker per turn: Hammers 6",
                "Base input per worker per turn: Lumber 6", "Worker places: 3");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_IRON_WORKS",
                "240 hammers, 100 tools", "Adam Smith", "Tools 9", "Ore 6");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_CATHEDRAL",
                "176 hammers, 100 tools", "Crosses 6",
                "Additional output without workers per turn: Crosses 3");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_ARSENAL",
                "half the tool input", "Musket 6", "Tools 3");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_WAREHOUSE_EXPANSION", "300",
                "80 hammers, 20 tools");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_FORTRESS", "200%",
                "320 hammers, 200 tools");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_LARGE_STABLES",
                "no further breeding bonus", "149 hammers, 80 tools");
        help.captureDialog("colonizopediaPage", "r0352-buildings-en.png");

        // Name search keeps the whole upgrade chain available in a narrow
        // window.
        help.typeSearch("IRON WORKS");
        help.verifyTopicTitles("Buildings");
        window.resize(900, 700);
        help.verifyFillsGameWindow("Colonizopedia");
        help.verifyReferenceRows(41);
        help.verifyBuildingReference("Cost: %s hammers, %s tools",
                "Minimum population: %s", "Present from colony founding.");
        help.captureDialog("colonizopediaPage", "r0352-buildings-small-en.png");
        help.dragBodyThumbToEnd();
        help.verifyBodyScrolledToEnd();
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_CUSTOM_HOUSE",
                "160 hammers, 50 tools", "Peter Stuyvesant", "export settings");
        help.captureDialog("colonizopediaPage", "r0352-buildings-end-en.png");
        help.closeWithOk();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyNextTurnEnabled();
    }

    /**
     * Czech building names are searchable without accents; Escape is read-only.
     */
    @Test
    @Tag("local")
    void TC_11_czech_buildings_search_and_escape() throws Exception {
        // Read the localized reference from the same running game.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        final String before = help.snapshotGameState();
        help.openFromButton();
        help.selectTopicWithMouse("Budovy");
        help.verifySelectedTopic("Budovy", "nikoli rozdíl mezi úrovněmi");
        help.verifyReferenceRows(41);
        help.verifyBuildingReference("Cena: %s kladiv, %s nářadí",
                "Nejméně obyvatel: %s", "Součást kolonie od jejího založení.");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_LUMBER_MILL",
                "52 kladiv, 0 nářadí", "Vylepšení budovy: Truhlářství",
                "Nejméně obyvatel: 3", "Kladiva 6");
        help.captureDialog("colonizopediaPage", "r0352-buildings-cs.png");

        // Fold accents and case without changing the game or losing upgrades.
        help.typeSearch("KOVARSKA MANUFAKTURA");
        help.verifyTopicTitles("Budovy");
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_IRON_WORKS",
                "240 kladiv, 100 nářadí", "Adam Smith", "Nářadí 9");
        help.scrollReferenceToEnd();
        help.verifyBodyScrolledToEnd();
        help.verifyReferenceProperties(
                "colonizopediaBuildingProperties_CUSTOM_HOUSE",
                "160 kladiv, 50 nářadí", "Peter Stuyvesant",
                "nastavení vývozu");
        help.captureDialog("colonizopediaPage", "r0352-buildings-end-cs.png");
        help.closeWithEscape();
        help.verifyDialogClosed();
        help.verifyGameStateUnchanged(before);
        help.verifyNextTurnEnabled();
    }
}
