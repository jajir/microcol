package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.ConstructionType;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyNameDialog;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpeditionLoopPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.RecruitmentPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-115 / R-0217: a fresh Discoverer reference expedition of every nation
 * explores, lands, founds a named colony, feeds two colonists, manufactures
 * cigars, sails them to Europe, sells them, pays for a recruit and brings the
 * recruit home, using only ordinary player actions after New Game.
 *
 * <p>
 * Reference world (bundled free-play map, 50 x 30, seed 134): the island's east
 * coast has grassland at (25,13), (24,14), (25,14), (24,15), (25,15) and
 * (26,15); (24,13) is forested. The colony site (25,14) touches the sea at
 * (26,13) and (26,14). The four nations start on the eastern sea lane at Dutch
 * (48,2), French (50,11), English (50,20) and Spanish (50,29): the 82 eastern
 * lane squares sorted by row then column, picked at indices 0, 27, 54 and 81.
 * The three computer nations cannot leave their lane squares or (Dutch) settle
 * far in the north, so they never reach the route.
 * </p>
 *
 * <p>
 * Food and manufacture ledger per colony update (one update at the start of
 * each own turn after founding): each colonist eats 2 corn; the founder works a
 * 4-corn grassland field and the second colonist a 3-tobacco grassland field,
 * so corn stays at 0 and tobacco rises by 3. After one update the second
 * colonist moves into the tobacconist's house, which turns 3 tobacco into 3
 * cigars per update. The unstaffed chapel credits one cross per update.
 * </p>
 *
 * <p>
 * Trade ledger: the treasury starts at 1000 (Discoverer) and nothing is bought.
 * Three cigars sell at 11 gold with a 0 % tax. The recruit costs 150 gold (200
 * x 75 %) reduced in proportion to the crosses earned out of the immigration
 * target (75 crosses, 50 for England), never below 50.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_101_F92_four_nation_expedition_loop_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");
    private static final int REFERENCE_MAP = 3;
    private static final int START_GOLD = 1000;
    private static final int CIGARS = 3;
    private static final int CIGAR_BID = 11;
    private static final String PLAYER_NAME = "Jan Výprava";

    /** Colony square on the island's east coast. */
    private static final Location SITE = Location.of(25, 14);
    /** Sea square next to the site where every ship stops and lands people. */
    private static final Location LANDING_SEA = Location.of(26, 14);
    /** Last high-sea square before the Atlantic departure step. */
    private static final Location LANE = Location.of(48, 14);
    /** The eastward step that starts the crossing to Europe. */
    private static final Location EXIT = Location.of(49, 14);

    /**
     * Fixed legs, each exactly one turn of sailing (4 squares for a caravel, 5
     * for the Dutch merchantman) over open sea.
     */
    private enum Route {
        DUTCH(EuropeanNation.DUTCH, UnitType.MERCHANTMAN, "Nieuw Zeeland",
                Location.of(48, 2),
                List.of(Location.of(43, 7), Location.of(38, 12),
                        Location.of(33, 14), Location.of(28, 14), LANDING_SEA),
                List.of(Location.of(30, 14), Location.of(35, 14),
                        Location.of(40, 14), Location.of(45, 14), LANE),
                List.of(Location.of(43, 14), Location.of(38, 14),
                        Location.of(33, 14), Location.of(28, 14), SITE)),
        FRENCH(EuropeanNation.FRENCH, UnitType.CARAVEL, "Nouvelle Étoile",
                Location.of(50, 11),
                List.of(Location.of(46, 12), Location.of(42, 13),
                        Location.of(38, 14), Location.of(34, 14),
                        Location.of(30, 14), LANDING_SEA),
                caravelEast(), caravelHome()),
        ENGLISH(EuropeanNation.ENGLISH, UnitType.CARAVEL, "New Brighton",
                Location.of(50, 20),
                List.of(Location.of(46, 19), Location.of(42, 18),
                        Location.of(38, 17), Location.of(34, 16),
                        Location.of(30, 15), LANDING_SEA),
                caravelEast(), caravelHome()),
        SPANISH(EuropeanNation.SPANISH, UnitType.CARAVEL, "Nueva Almería",
                Location.of(50, 29),
                List.of(Location.of(46, 25), Location.of(42, 21),
                        Location.of(38, 17), Location.of(34, 14),
                        Location.of(30, 14), LANDING_SEA),
                caravelEast(), caravelHome());

        private final EuropeanNation nation;
        private final UnitType shipType;
        private final String colonyName;
        private final Location start;
        private final List<Location> outward;
        private final List<Location> eastward;
        private final List<Location> homeward;

        Route(final EuropeanNation nation, final UnitType shipType,
                final String colonyName, final Location start,
                final List<Location> outward, final List<Location> eastward,
                final List<Location> homeward) {
            this.nation = nation;
            this.shipType = shipType;
            this.colonyName = colonyName;
            this.start = start;
            this.outward = outward;
            this.eastward = eastward;
            this.homeward = homeward;
        }

        /**
         * Movement the ship keeps after its last outward leg. A ship at zero
         * cannot start a map drag, so landing then waits for the next turn.
         */
        private int movesLeftOnArrival() {
            final Location before = outward.get(outward.size() - 2);
            final Location last = outward.get(outward.size() - 1);
            return shipType.getSpeed()
                    - Math.max(Math.abs(before.getX() - last.getX()),
                            Math.abs(before.getY() - last.getY()));
        }

        private static List<Location> caravelEast() {
            return List.of(Location.of(29, 14), Location.of(33, 14),
                    Location.of(37, 14), Location.of(41, 14),
                    Location.of(45, 14), LANE);
        }

        private static List<Location> caravelHome() {
            return List.of(Location.of(44, 14), Location.of(40, 14),
                    Location.of(36, 14), Location.of(32, 14),
                    Location.of(28, 14), SITE);
        }
    }

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_dutch_expedition_with_checkpoints() throws Exception {
        playExpedition(Route.DUTCH, true);
    }

    @Test
    @Tag("local")
    void TC_02_french_expedition_with_checkpoints() throws Exception {
        playExpedition(Route.FRENCH, true);
    }

    @Test
    @Tag("local")
    void TC_03_english_expedition_with_checkpoints() throws Exception {
        playExpedition(Route.ENGLISH, true);
    }

    @Test
    @Tag("local")
    void TC_04_spanish_expedition_with_checkpoints() throws Exception {
        playExpedition(Route.SPANISH, true);
    }

    @Test
    @Tag("local")
    void TC_05_french_expedition_without_interruption() throws Exception {
        playExpedition(Route.FRENCH, false);
    }

    /**
     * Crosses earned after the founding turn lower the passage price by the
     * R-0075 rule: 150 x (target - crosses) / target, rounded up, at least 50.
     */
    private static int expectedPassage(final EuropeanNation nation,
            final int crosses) {
        final int target = nation == EuropeanNation.ENGLISH ? 50 : 75;
        return Math.max(50, (150 * (target - crosses) + target - 1) / target);
    }

    private void playExpedition(final Route route,
            final boolean saveAndContinue) throws Exception {
        final EuropeanNation nation = route.nation;
        // The last leg ends on turn outward.size() - 1. A caravel spends all 4
        // moves on it and cannot drag again, so it lands one turn later; the
        // Dutch merchantman keeps 3 of 5 moves and lands at once. People land
        // on the landing turn and the colony is founded on the next turn.
        final boolean waitsToLand = route.movesLeftOnArrival() == 0;
        final int landingTurn = route.outward.size() - 1
                + (waitsToLand ? 1 : 0);
        final int founded = landingTurn + 1;

        // Start a fresh Discoverer game on the reference expedition map.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(REFERENCE_MAP);
        setup.verifySelectedMap(REFERENCE_MAP,
                "Reference expedition (four nations)");
        setup.selectDifficulty(Difficulty.DISCOVERER);
        setup.verifyDifficultySelection(Difficulty.DISCOVERER, "Discoverer");
        setup.selectNation(nation);
        setup.verifyNationSelection(nation, true);
        setup.enterName(PLAYER_NAME);
        GamePage game = setup.start();
        setup.verifyReferenceExpedition(nation, PLAYER_NAME, START_GOLD);
        setup.verifyGameDifficulty(Difficulty.DISCOVERER, "Discoverer");

        // Initial nation, ship, crew, funds, date and unexplored coast.
        final ExpeditionLoopPage trip = new ExpeditionLoopPage(getContext(),
                nation);
        final List<Integer> people = trip.passengerIds();
        trip.verifyShipAt(route.start);
        trip.verifyShipType(route.shipType);
        trip.verifyPassengerIds(people);
        trip.verifyGold(START_GOLD);
        trip.verifyPlayedTurns(0);
        trip.verifyTerrainExplored(SITE, false);

        // Checkpoint 1: Continue restores the untouched start.
        if (saveAndContinue) {
            trip.rememberLedger();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            trip.verifyLedgerUnchanged();
        }

        // Sail one turn per leg across unexplored sea to the east coast.
        for (int leg = 0; leg < route.outward.size(); leg++) {
            trip.verifyPlayedTurns(leg);
            trip.sailTo(game, route.outward.get(leg));
            trip.verifyShipAt(route.outward.get(leg));
            if (leg < route.outward.size() - 1) {
                game.nextTurnAndCloseDialogs();
            }
        }

        // The coast is now explored; land both colonists next to the sea.
        trip.verifyTerrainExplored(SITE, true);
        trip.verifyShipMovesLeft(route.movesLeftOnArrival());
        if (waitsToLand) {
            game.nextTurnAndCloseDialogs();
            trip.verifyShipMovesLeft(route.shipType.getSpeed());
        }
        trip.verifyPlayedTurns(landingTurn);
        trip.disembark(game, SITE, people.get(0), 1);
        trip.disembark(game, SITE, people.get(1), 2);
        game.verifyNumberOfUnitsAt(SITE, 2);

        // Landing spends the colonists' moves, so the colony starts next turn.
        game.nextTurnAndCloseDialogs();
        trip.verifyPlayedTurns(founded);
        trip.selectColonistAt(game, SITE);
        final Unit founder = trip.selectedUnit();
        final int worker = people.get(0) == founder.getId() ? people.get(1)
                : people.get(0);
        final int newColonyId = trip.nextColonyId();
        final int previousColonyCount = trip.colonyCount();
        final ColonyNameDialog naming = game.foundColony();
        naming.enterName(route.colonyName);
        naming.save();
        naming.verifyFounded(founder, newColonyId, route.colonyName, SITE,
                previousColonyCount + 1);
        ColonyScreen colony = ColonyScreen.of(getContext(), route.colonyName);
        colony.verifyNumberOfUnitsOnFields(1);
        colony.verifyThatProductionIs(GoodsType.CORN, 2);
        colony.verifyNumberOfUnitsAtPier(1);

        // The second colonist grows tobacco, so food stays balanced at zero.
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(worker);
        jobs.select(Kind.BEST_FIELD, GoodsType.TOBACCO);
        jobs.confirm();
        jobs.verifyClosed();
        trip.verifyWorkerOutput(route.colonyName, worker,
                Goods.of(GoodsType.TOBACCO, 3));
        colony.verifyThatProductionIs(GoodsType.CORN, 0);
        colony.verifyThatProductionIs(GoodsType.TOBACCO, 3);
        game = colony.close();

        // The ship enters the colony port so cargo can be loaded later.
        trip.sailTo(game, SITE);
        trip.verifyShipAt(SITE);
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CORN, 0));
        trip.verifyColonyStock(route.colonyName,
                Goods.of(GoodsType.TOBACCO, 0));

        // Checkpoint 2: Continue keeps the working colony and the docked ship.
        if (saveAndContinue) {
            trip.rememberLedger();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            trip.verifyLedgerUnchanged();
        }

        // First update: three tobacco are stored and no food is lost.
        game.nextTurnAndCloseDialogs();
        trip.verifyPlayedTurns(founded + 1);
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CORN, 0));
        trip.verifyColonyStock(route.colonyName,
                Goods.of(GoodsType.TOBACCO, 3));
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CIGARS, 0));

        // Move the second colonist into the tobacconist's house.
        game.panToTile(SITE);
        colony = game.openColonyAt(SITE, route.colonyName);
        trip.openFieldJobChoice(colony, worker);
        jobs.select(Kind.INDOOR, GoodsType.CIGARS);
        jobs.confirm();
        jobs.verifyClosed();
        trip.verifyWorkerIndoors(route.colonyName, worker,
                ConstructionType.TOBACCONISTS_HOUSE);
        colony.verifyThatProductionIs(GoodsType.CIGARS, CIGARS);
        colony.verifyThatProductionIs(GoodsType.TOBACCO, -CIGARS);
        colony.verifyThatProductionIs(GoodsType.CORN, 0);
        game = colony.close();

        // Second update: the tobacco became cigars; food is still balanced.
        game.nextTurnAndCloseDialogs();
        trip.verifyPlayedTurns(founded + 2);
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CORN, 0));
        trip.verifyColonyStock(route.colonyName,
                Goods.of(GoodsType.TOBACCO, 0));
        trip.verifyColonyStock(route.colonyName,
                Goods.of(GoodsType.CIGARS, CIGARS));

        // Load the cigars from the warehouse into the docked ship.
        game.panToTile(SITE);
        colony = game.openColonyAt(SITE, route.colonyName);
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromWarehouseToShipCargoSlot(
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.CIGARS), 0);
        trip.waitUntilShipCarries(0, GoodsType.CIGARS);
        trip.verifyShipCargo(0, Goods.of(GoodsType.CIGARS, CIGARS));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CIGARS, 0));
        game = colony.close();

        // Sail east one turn per leg and start the Atlantic crossing.
        for (int leg = 0; leg < route.eastward.size(); leg++) {
            trip.verifyPlayedTurns(founded + 2 + leg);
            trip.sailTo(game, route.eastward.get(leg));
            trip.verifyShipAt(route.eastward.get(leg));
            if (leg < route.eastward.size() - 1) {
                game.nextTurnAndCloseDialogs();
            }
        }
        final int departure = founded + 2 + route.eastward.size() - 1;
        trip.verifyPlayedTurns(departure);
        trip.sailTo(game, EXIT);
        final int outwardTurns = trip.verifyVoyage(true);
        final int outwardWeather = trip.voyageWeather();
        trip.verifyShipCargo(0, Goods.of(GoodsType.CIGARS, CIGARS));

        // Checkpoint 3: Continue keeps the voyage, its weather and the cargo.
        if (saveAndContinue) {
            trip.rememberLedger();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            trip.verifyLedgerUnchanged();
            trip.verifyVoyageProgress(outwardWeather, outwardTurns,
                    outwardTurns, true);
        }

        // The crossing lasts exactly the sampled number of turns.
        for (int remaining = outwardTurns - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                trip.verifyVoyageProgress(outwardWeather, outwardTurns,
                        remaining, true);
            }
        }
        final int arrival = departure + outwardTurns;
        trip.verifyPlayedTurns(arrival);
        trip.verifyShipInEurope(true);

        // Sell the cigars at the quoted bid; no tax is charged yet.
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyGold(START_GOLD);
        europe.verifyGoodsBidAndAsk(GoodsType.CIGARS, "11/12");
        europe.selectUnitFromPort(0);
        europe.sellGoods(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.CIGARS));
        trip.waitUntilShipSlotIsEmpty(0);
        europe.verifyCargoSlotEmpty(0, 0);
        europe.verifyGold(START_GOLD + CIGARS * CIGAR_BID);

        // Pay for one recruit; each update since founding earned one cross.
        final int passage = expectedPassage(nation, arrival - founded);
        final RecruitmentPage recruit = new RecruitmentPage(getContext());
        recruit.open();
        recruit.verifyOffer(null, passage, true, "Pay passage: ", "");
        recruit.confirm(0);
        final int recruitId = trip.waitForRecruitOnPier();
        europe.verifyGold(START_GOLD + CIGARS * CIGAR_BID - passage);
        trip.verifyGold(START_GOLD + CIGARS * CIGAR_BID - passage);

        // Put the recruit on the ship and send it back to the New World.
        new EuropeEquipmentPage(getContext()).embark(recruitId);
        trip.verifyPassengerAboard(recruitId);
        europe.selectUnitFromPort(0);
        trip.sendShipBack();
        final int returnTurns = trip.verifyVoyage(false);
        final int returnWeather = trip.voyageWeather();
        game = europe.close();

        // Checkpoint 4: Continue keeps the sale, the recruit and the voyage.
        if (saveAndContinue) {
            trip.rememberLedger();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            trip.verifyLedgerUnchanged();
            trip.verifyVoyageProgress(returnWeather, returnTurns, returnTurns,
                    false);
        }
        for (int remaining = returnTurns - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                trip.verifyVoyageProgress(returnWeather, returnTurns, remaining,
                        false);
            }
        }
        final int back = arrival + returnTurns;
        trip.verifyPlayedTurns(back);
        trip.verifyShipAt(LANE);
        trip.verifyPassengerAboard(recruitId);

        // Sail west one turn per leg; entering the port lands the recruit.
        for (int leg = 0; leg < route.homeward.size(); leg++) {
            trip.verifyPlayedTurns(back + leg);
            trip.sailTo(game, route.homeward.get(leg));
            trip.verifyShipAt(route.homeward.get(leg));
            if (leg < route.homeward.size() - 1) {
                game.nextTurnAndCloseDialogs();
            }
        }
        trip.verifyUnitAt(recruitId, SITE);
        trip.verifyShipCargo(0, Goods.of(GoodsType.CIGARS, 0));
        game.verifyNumberOfUnitsAt(SITE, 2);

        // The first tax demand falls on colony update 20 (turn founded + 20);
        // the longest crossings end on update 19, so no demand has appeared.
        trip.verifyNoTaxDecisionPending();

        // The balances at the end follow the ledger exactly.
        trip.verifyGold(START_GOLD + CIGARS * CIGAR_BID - passage);
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CORN, 0));
        trip.verifyColonyStock(route.colonyName,
                Goods.of(GoodsType.TOBACCO, 0));
        trip.verifyColonyStock(route.colonyName, Goods.of(GoodsType.CIGARS, 0));

        // Checkpoint 5: Continue keeps the arrived recruit and the balances.
        if (saveAndContinue) {
            trip.rememberLedger();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            trip.verifyLedgerUnchanged();
            trip.verifyUnitAt(recruitId, SITE);
        }
    }
}
