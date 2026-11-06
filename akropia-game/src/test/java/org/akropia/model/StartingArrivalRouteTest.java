package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.CampaignModule;
import org.akropia.model.campaign.FreePlayMap;
import org.akropia.model.campaign.GameModelDao;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.campaign.store.CampaignsDao;
import org.akropia.model.campaign.store.CampaignsPo;
import org.akropia.model.generation.GeneratedClimate;
import org.akropia.model.generation.GeneratedLandShare;
import org.akropia.model.generation.GeneratedWorldSettings;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;

/**
 * R-0424: a new free-play expedition remembers the sea route it sailed from
 * Europe, so its ships can take a go-to order to Europe at once. The games are
 * created by the same {@link GameModelDao} operations as New Game. The authored
 * maps start the Dutch galleon west of the eastern sea lane (x 48-50) and
 * outside its sight; the other worlds start the ships on the lane.
 */
class StartingArrivalRouteTest {

    /** First column of the eastern sea lane on the authored maps. */
    private static final int AUTHORED_LANE_X = 48;

    private static final FreePlayMap[] AUTHORED = { FreePlayMap.ORIGINAL,
            FreePlayMap.TWO_ISLANDS, FreePlayMap.RIVAL_EXPEDITION };

    private static GameModelDao games() {
        final CampaignsDao campaigns = mock(CampaignsDao.class);
        when(campaigns.load()).thenReturn(new CampaignsPo());
        return Guice
                .createInjector(new CampaignModule(),
                        binder -> binder.bind(CampaignsDao.class)
                                .toInstance(campaigns))
                .getInstance(GameModelDao.class);
    }

    /** A game created by New Game, before it starts. */
    static Model newGame(final FreePlayMap map, final EuropeanNation nation) {
        return games().createFreePlay(map, "Player", Difficulty.CONQUISTADOR,
                nation, new EventBus()).getModel();
    }

    /** The bundled data of an authored map loaded without New Game. */
    private static Model loadedOnly(final FreePlayMap map) {
        return Model.make(
                new GameModelPoDao().loadFromClassPath(map.getResource())
                        .getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Set<Location> known(final Model model, final Player player) {
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= model.getMap().getMaxLocationX(); x++) {
            for (int y = 1; y <= model.getMap().getMaxLocationY(); y++) {
                if (player.isVisible(Location.of(x, y))) {
                    known.add(Location.of(x, y));
                }
            }
        }
        return known;
    }

    private static List<Unit> startingShips(final Model model) {
        return model.getAllUnits().stream()
                .filter(unit -> unit.isShip() && unit.isAtPlaceLocation()
                        && unit.getOwner().isColonialNation())
                .collect(Collectors.toList());
    }

    private static void verifyEveryShipKnowsEurope(final Model model) {
        final List<Unit> ships = startingShips(model);
        assertFalse(ships.isEmpty());
        for (final Unit ship : ships) {
            assertTrue(((AbstractUnit) ship).findEuropeGoToRoute().isPresent(),
                    () -> "No known route to Europe for " + ship);
        }
    }

    @Test
    void everyNewFreePlayWorldKnowsTheSeaRouteOfEachNation() {
        for (final FreePlayMap map : AUTHORED) {
            verifyEveryShipKnowsEurope(newGame(map, EuropeanNation.DUTCH));
        }
        for (final EuropeanNation nation : EuropeanNation.values()) {
            verifyEveryShipKnowsEurope(newGame(FreePlayMap.REFERENCE, nation));
            verifyEveryShipKnowsEurope(
                    newGame(FreePlayMap.NORTH_AMERICA, nation));
        }
        verifyEveryShipKnowsEurope(games().createGeneratedFreePlay(
                new GeneratedWorldSettings(1, GeneratedClimate.TEMPERATE,
                        GeneratedLandShare.MEDIUM),
                "Player", Difficulty.CONQUISTADOR, EuropeanNation.DUTCH,
                new EventBus()).getModel());
    }

    @Test
    void onlyTheMinimalRouteIsRememberedWithoutPresentSight() {
        for (final FreePlayMap map : AUTHORED) {
            final Model before = loadedOnly(map);
            final Model created = newGame(map, EuropeanNation.DUTCH);
            for (final Player player : created.getPlayers()) {
                final Set<Location> added = known(created, player);
                added.removeAll(known(before,
                        before.getPlayerByName(player.getName())));
                final List<Unit> ships = startingShips(created).stream()
                        .filter(ship -> ship.getOwner() == player)
                        .collect(Collectors.toList());
                if (ships.isEmpty()) {
                    // Crowns and native nations sailed no route.
                    assertEquals(Set.of(), added, player.getName());
                    continue;
                }
                // One shortest route per ship: no more squares than columns
                // between the ship and the sea lane, all of them water that
                // the player does not see now.
                final int columns = ships.stream().mapToInt(
                        ship -> AUTHORED_LANE_X - ship.getLocation().getX())
                        .sum();
                assertFalse(added.isEmpty(), player.getName());
                assertTrue(added.size() <= columns, player.getName());
                for (final Location at : added) {
                    assertTrue(created.getMap().getTerrainTypeAt(at).isSea());
                    assertFalse(player.isCurrentlyVisible(at));
                }
            }
        }
    }

    @Test
    void loadingAuthoredDataOrASaveAddsNothing() {
        // The bundled data alone still lacks the route: only New Game adds it.
        final Model loaded = loadedOnly(FreePlayMap.ORIGINAL);
        final Unit loadedShip = loaded.getUnitById(3);
        assertTrue(((AbstractUnit) loadedShip).findEuropeGoToRoute().isEmpty());

        // Saving and loading the new game keeps exactly its exploration.
        final Model created = newGame(FreePlayMap.ORIGINAL,
                EuropeanNation.DUTCH);
        final Player dutch = created.getUnitById(3).getOwner();
        created.startGame();
        final Model restored = Model.make(created.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit ship = restored.getUnitById(3);
        assertEquals(known(created, dutch), known(restored, ship.getOwner()));
        assertTrue(((AbstractUnit) ship).findEuropeGoToRoute().isPresent());
    }

    @Test
    void startedGameIsRejectedUnchanged() {
        final Model model = newGame(FreePlayMap.RIVAL_EXPEDITION,
                EuropeanNation.DUTCH);
        model.startGame();
        final Player dutch = model.getUnitById(3).getOwner();
        final Set<Location> before = known(model, dutch);
        assertThrows(IllegalStateException.class,
                model::rememberStartingArrivalRoutes);
        assertEquals(before, known(model, dutch));
    }

    @Test
    void campaignMissionKeepsItsAuthoredExploration() {
        final String mission = "/maps/default-campaign-mission-0-findNewWorld.akropia";
        final Model loaded = Model.make(
                new GameModelPoDao().loadFromClassPath(mission).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Model campaign = games()
                .loadFromClassPath(mission, new EventBus()).getModel();
        for (final Player player : campaign.getPlayers()) {
            assertEquals(
                    known(loaded, loaded.getPlayerByName(player.getName())),
                    known(campaign, player), player.getName());
        }
    }
}
