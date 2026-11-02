package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.page.ColonyNameDialog;
import org.akropia.page.ColonyScreen;
import org.akropia.page.ExpeditionLoopPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.FreePlaySetupPage;
import org.akropia.page.GamePage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-001 / R-0221: New Game starts the authored North America world for every
 * European nation, the player lands, founds, explores and plays complete turns,
 * Save and Continue keep the world, and a cancelled choice keeps the saved
 * game.
 *
 * <p>
 * North America is 100 x 80 squares. The four nations start on the Atlantic
 * high-sea lane: Dutch (92,30), French (91,45), English (98,54) and Spanish
 * (100,64). The Dutch merchantman sails three squares west to the coastal ocean
 * (89,30), keeps two of its five moves and lands both colonists at once on the
 * Newfoundland coast (88,30). A ship sees two squares and a colonist or colony
 * one, so the south coast of Newfoundland at (86,33) stays unexplored until the
 * ship sails four squares to (88,34).
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_198_F15_north_america_world_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.akropia");
    private static final int NORTH_AMERICA_MAP = 5;
    private static final String PLAYER_NAME = "Jan Amerika";
    private static final String COLONY_NAME = "Nieuw Vinland";
    private static final Map<EuropeanNation, Location> STARTS = Map.of(
            EuropeanNation.DUTCH, Location.of(92, 30), EuropeanNation.FRENCH,
            Location.of(91, 45), EuropeanNation.ENGLISH, Location.of(98, 54),
            EuropeanNation.SPANISH, Location.of(100, 64));
    private static final Location LANDING_SEA = Location.of(89, 30);
    private static final Location SITE = Location.of(88, 30);
    private static final Location COAST = Location.of(88, 34);
    private static final Location SOUTH_COAST = Location.of(86, 33);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_dutch_land_found_explore_and_continue_in_north_america()
            throws Exception {
        // Choose North America; every nation can be selected for it.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(NORTH_AMERICA_MAP);
        setup.verifySelectedMap(NORTH_AMERICA_MAP, "North America");
        setup.verifyGeneratedControlsHidden();
        setup.verifyNationSelection(EuropeanNation.DUTCH, true);
        setup.verifyDifficultySelection(Difficulty.CONQUISTADOR,
                "Conquistador");
        setup.enterName(PLAYER_NAME);
        setup.captureDialog("freePlaySetup", "r0221-north-america-setup.png");
        GamePage game = setup.start();
        setup.verifyNorthAmericaExpedition(EuropeanNation.DUTCH, PLAYER_NAME,
                500, STARTS.get(EuropeanNation.DUTCH));
        setup.verifyNorthAmericaGeography();
        setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");

        // The expedition waits on the Atlantic lane east of Newfoundland.
        final ExpeditionLoopPage trip = new ExpeditionLoopPage(getContext(),
                EuropeanNation.DUTCH);
        final List<Integer> people = trip.passengerIds();
        trip.verifyShipAt(STARTS.get(EuropeanNation.DUTCH));
        trip.verifyPlayedTurns(0);
        trip.verifyTerrainExplored(SITE, false);
        setup.captureDialog("rightPanel", "r0221-north-america-start.png");

        // Sail three squares west and land both colonists on the coast.
        trip.sailTo(game, LANDING_SEA);
        trip.verifyShipAt(LANDING_SEA);
        trip.verifyShipMovesLeft(2);
        trip.verifyTerrainExplored(SITE, true);
        trip.disembark(game, SITE, people.get(0), 1);
        trip.disembarkSolePassenger(game, SITE, 2);
        game.verifyNumberOfUnitsAt(SITE, 2);

        // Every European, Crown and native participant completes the turn.
        game.nextTurnAndCloseDialogs();
        trip.verifyPlayedTurns(1);
        setup.verifyNorthAmericaGeography();

        // Found a colony where the colonists landed.
        trip.selectColonistAt(game, SITE);
        final Unit founder = trip.selectedUnit();
        final int newColonyId = trip.nextColonyId();
        final int previousColonyCount = trip.colonyCount();
        final ColonyNameDialog naming = game.foundColony();
        naming.enterName(COLONY_NAME);
        naming.save();
        naming.verifyFounded(founder, newColonyId, COLONY_NAME, SITE,
                previousColonyCount + 1);
        final ColonyScreen colony = ColonyScreen.of(getContext(), COLONY_NAME);
        colony.verifyNumberOfUnitsOnFields(1);
        game = colony.close();

        // Sail south along the coast to explore southern Newfoundland.
        trip.verifyTerrainExplored(SOUTH_COAST, false);
        trip.sailTo(game, COAST);
        trip.verifyShipAt(COAST);
        trip.verifyTerrainExplored(SOUTH_COAST, true);
        setup.captureDialog("rightPanel", "r0221-north-america-colony.png");

        // Save and Continue keep the colony, the ship and the whole map.
        trip.rememberLedger();
        game.exitToMenu().verifyMainScreen();
        game = new FreePlayAuditPage(getContext()).continueGame();
        trip.verifyLedgerUnchanged();
        setup.verifyNorthAmericaGeography();
        setup.verifySavedNorthAmericaWorld();
        trip.verifyTerrainExplored(SOUTH_COAST, true);

        // The restored world plays another complete turn.
        game.nextTurnAndCloseDialogs();
        trip.verifyPlayedTurns(2);
        trip.verifyShipAt(COAST);
        setup.verifyNorthAmericaGeography();
    }

    @Test
    @Tag("local")
    void TC_02_cancelled_north_america_choice_keeps_the_saved_game()
            throws Exception {
        // Save an existing position before choosing North America.
        WelcomePage.of(getContext()).loadGame().exitToMenu();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final byte[] saved = audit.readAutosave();
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(NORTH_AMERICA_MAP);
        setup.verifySelectedMap(NORTH_AMERICA_MAP, "North America");
        setup.selectNation(EuropeanNation.ENGLISH);
        setup.verifyNationSelection(EuropeanNation.ENGLISH, true);
        setup.selectDifficulty(Difficulty.VICEROY);
        setup.back().verifyMainScreen();
        audit.verifyAutosaveUnchanged(saved);

        // Escape from the choice likewise creates no world.
        setup = WelcomePage.of(getContext()).openFreePlaySetup();
        setup.selectMap(NORTH_AMERICA_MAP);
        setup.verifySelectedMap(NORTH_AMERICA_MAP, "North America");
        setup.escape().verifyMainScreen();
        setup.verifyNoActiveGame();
        audit.verifyAutosaveUnchanged(saved);

        // Continue restores the saved game, not North America.
        final GamePage game = audit.continueGame();
        setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");
        game.openEuroperPort().verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_03_czech_choice_starts_every_nation_on_the_same_world()
            throws Exception {
        // The choice has a Czech name and lets the player pick a nation.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        FreePlaySetupPage setup = settings.goBack().openFreePlaySetup();
        setup.selectMap(NORTH_AMERICA_MAP);
        setup.verifySelectedMap(NORTH_AMERICA_MAP, "Severní Amerika");
        setup.verifySetupLanguage("Začít hru",
                "Vyberte národ, za který budete hrát.");
        setup.verifyNationSelection(EuropeanNation.DUTCH, true);
        setup.captureDialog("freePlaySetup",
                "r0221-north-america-setup-czech.png");

        // Each nation starts on its own Atlantic lane of the same world.
        for (final EuropeanNation nation : EuropeanNation.values()) {
            setup.selectMap(NORTH_AMERICA_MAP);
            setup.selectNation(nation);
            setup.verifyNationSelection(nation, true);
            setup.selectDifficulty(Difficulty.DISCOVERER);
            setup.enterName("Severní výprava");
            final GamePage game = setup.start();
            setup.verifyNorthAmericaExpedition(nation, "Severní výprava", 1000,
                    STARTS.get(nation));
            setup.verifyNorthAmericaGeography();
            setup.verifyGameDifficulty(Difficulty.DISCOVERER, "Objevitel");
            game.exitToMenu();
            setup = WelcomePage.of(getContext()).openFreePlaySetup();
        }
    }
}
