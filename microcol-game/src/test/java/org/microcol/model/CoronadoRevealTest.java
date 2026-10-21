package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

/**
 * Coronado's arrival explores existing European colonies once, for the founder
 * only. Foreign colonies are remembered, current sight is never granted and
 * colonies founded later are not included.
 */
class CoronadoRevealTest {

    /** France's colonist that stands on the free-play map at Quebec. */
    private static final int FOUNDER_ID = 6;

    private final Gson gson = new Gson();

    /** Free play where Dutch is the founder and holds the given bells. */
    private static ModelPo dutchCoronadoSave(final int bells) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if ("Dutch".equals(player.getName())) {
                final CongressPo congress = Congress.initial();
                congress.setActivated(true);
                congress.setBells(bells);
                congress.setGeneration(1);
                congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                        Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                        Founder.WILLIAM_PENN, Founder.FRANCISCO_DE_CORONADO));
                congress.setSelected(Founder.FRANCISCO_DE_CORONADO);
                player.setCongress(congress);
            }
        }
        return data.getModel();
    }

    private static Model load(final int bells) {
        return Model.make(dutchCoronadoSave(bells),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Continued free play with France to move, as in FoundingPreconditionsTest:
     * France's colonist stands on the site, which and its neighbors are
     * grassland.
     */
    private static Model withFounderAt(final Location site) {
        final ModelPo po = dutchCoronadoSave(100);
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("France");
        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        site.getNeighbors().stream()
                .filter(at -> at.getX() >= 1
                        && at.getX() <= po.getMap().getMaxX() && at.getY() >= 1
                        && at.getY() <= po.getMap().getMaxY())
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        terrain.put(site, TerrainType.GRASSLAND);
        po.getMap().setTerrainType(terrain);
        po.getMap().setTrees(Collections.emptySet());
        final UnitPo founder = po.getUnits().stream()
                .filter(unit -> unit.getId() == FOUNDER_ID).findFirst()
                .orElseThrow();
        // The free-play colonist already stands on the map (no colony field
        // or cargo slot to release); only relocate it and give it a move.
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(site);
        place.setOrientation(Direction.west);
        founder.setPlaceMap(place);
        founder.setAvailableMoves(1);
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Set<Location> explored(final Player player) {
        return player.save().getVisible().getVisibilitySet();
    }

    private static Set<Location> currentSight(final Model model,
            final Player player) {
        final Set<Location> out = new HashSet<>();
        for (int x = 0; x <= model.getMap().getMaxLocationX(); x++) {
            for (int y = 0; y <= model.getMap().getMaxLocationY(); y++) {
                final Location location = Location.of(x, y);
                if (player.isCurrentlyVisible(location)) {
                    out.add(location);
                }
            }
        }
        return out;
    }

    private static List<Colony> foreignColonies(final Model model,
            final Player player) {
        return model.getColonies().stream()
                .filter(colony -> colony.getOwner() != player)
                .collect(Collectors.toList());
    }

    /** Ends the earlier nations' turns until the founder's turn begins. */
    private static void reachTurnOf(final Model model, final Player player) {
        for (int i = 0; i < 4 && model.getCurrentPlayer() != player; i++) {
            model.endTurn();
        }
        assertSame(player, model.getCurrentPlayer());
    }

    /** Ends turns until the calendar moves on, as a Continue does. */
    private static void endRound(final Model model) {
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        for (int i = 0; i < 12 && model.getCalendar()
                .getNumberOfPlayedTurns() == before; i++) {
            model.endTurn();
        }
        assertTrue(model.getCalendar().getNumberOfPlayedTurns() > before);
    }

    @Test
    void arrivalRemembersForeignColoniesWithTheirClippedNeighborsOnly() {
        final Model model = load(100);
        final Player dutch = model.getPlayerByName("Dutch");
        model.startGame();
        final List<Colony> foreign = foreignColonies(model, dutch);
        assertFalse(foreign.isEmpty());
        for (final Colony colony : foreign) {
            assertFalse(dutch.isVisible(colony.getLocation()));
            assertTrue(dutch.getKnownColonyAt(colony.getLocation()).isEmpty());
        }

        reachTurnOf(model, dutch);
        for (final Colony colony : foreign) {
            final Location center = colony.getLocation();
            for (int x = center.getX() - 1; x <= center.getX() + 1; x++) {
                for (int y = center.getY() - 1; y <= center.getY() + 1; y++) {
                    final Location area = Location.of(x, y);
                    if (model.isValid(area)) {
                        assertTrue(dutch.isVisible(area), area.toString());
                    }
                }
            }
            final ColonySighting known = dutch.getKnownColonyAt(center)
                    .orElseThrow();
            assertEquals(colony.getName(), known.getColonyName());
            assertSame(colony.getOwner(), known.getOwner());
        }

        final Set<Location> remembered = dutch.save().getColonySightings()
                .stream().map(sighting -> sighting.getLocation())
                .collect(Collectors.toSet());
        assertTrue(remembered.containsAll(foreign.stream()
                .map(Colony::getLocation).collect(Collectors.toSet())));
        assertTrue(model.getColonies().stream()
                .filter(colony -> colony.getOwner() == dutch)
                .map(Colony::getLocation).noneMatch(remembered::contains));
    }

    @Test
    void arrivalGrantsNoCurrentSightAndLeavesOtherPlayersUntouched() {
        final Model model = load(100);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        model.startGame();
        final Set<Location> sightBefore = currentSight(model, dutch);
        final Set<Location> franceExploredBefore = explored(france);
        final String franceSightingsBefore = gson
                .toJson(france.save().getColonySightings());

        reachTurnOf(model, dutch);
        assertEquals(sightBefore, currentSight(model, dutch));
        assertEquals(franceExploredBefore, explored(france));
        assertEquals(franceSightingsBefore,
                gson.toJson(france.save().getColonySightings()));
        for (final Colony colony : foreignColonies(model, dutch)) {
            assertFalse(dutch.isCurrentlyVisible(colony.getLocation()));
            // Foreign units on and around the colony stay unobserved.
            assertTrue(
                    dutch.getObservedUnitsAt(colony.getLocation()).isEmpty());
        }
    }

    @Test
    void selectingWithoutEarningTheFounderRevealsNothing() {
        final Model model = load(0);
        final Player dutch = model.getPlayerByName("Dutch");
        model.startGame();
        final Set<Location> exploredBefore = explored(dutch);
        final List<Colony> foreign = foreignColonies(model, dutch);

        reachTurnOf(model, dutch);
        assertEquals(Optional.of(Founder.FRANCISCO_DE_CORONADO),
                dutch.getSelectedFounder());
        assertTrue(dutch.getCongressMembers().isEmpty());
        assertEquals(exploredBefore, explored(dutch));
        for (final Colony colony : foreign) {
            assertFalse(dutch.isVisible(colony.getLocation()));
            assertTrue(dutch.getKnownColonyAt(colony.getLocation()).isEmpty());
        }
    }

    @Test
    void savedArrivalIsRestoredWithoutReplay() {
        final Model model = load(100);
        final Player dutch = model.getPlayerByName("Dutch");
        model.startGame();
        reachTurnOf(model, dutch);
        final Set<Location> exploredAfterArrival = explored(dutch);
        final String sightingsAfterArrival = gson
                .toJson(dutch.save().getColonySightings());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player restoredDutch = restored.getPlayerByName("Dutch");
        assertEquals(exploredAfterArrival, explored(restoredDutch));
        assertEquals(sightingsAfterArrival,
                gson.toJson(restoredDutch.save().getColonySightings()));
        assertEquals(List.of(Founder.FRANCISCO_DE_CORONADO),
                restoredDutch.getCongressMembers());
        assertEquals(dutch.getCongressBells(),
                restoredDutch.getCongressBells());
        for (final Colony colony : foreignColonies(restored, restoredDutch)) {
            assertEquals(colony.getName(),
                    restoredDutch.getKnownColonyAt(colony.getLocation())
                            .orElseThrow().getColonyName());
        }
    }

    @Test
    void mapEdgeColonyRevealsOnlyItsValidNeighborsAndNothingBeyond() {
        final Location corner = Location.of(1, 1);
        final Model model = withFounderAt(corner);
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        // Founded before the arrival, so it exists when Coronado joins.
        model.buildColony(france, model.getUnitById(FOUNDER_ID), "Edge Post");
        final Set<Location> exploredBefore = explored(dutch);

        reachTurnOf(model, dutch);
        final Set<Location> expected = new HashSet<>(exploredBefore);
        // The edge colony contributes exactly its four valid cells.
        expected.addAll(List.of(corner, Location.of(2, 1), Location.of(1, 2),
                Location.of(2, 2)));
        // Every other colony existing at arrival is surveyed the same way.
        for (final Colony colony : model.getColonies()) {
            final Location center = colony.getLocation();
            for (int x = center.getX() - 1; x <= center.getX() + 1; x++) {
                for (int y = center.getY() - 1; y <= center.getY() + 1; y++) {
                    if (model.isValid(Location.of(x, y))) {
                        expected.add(Location.of(x, y));
                    }
                }
            }
        }
        assertEquals(expected, explored(dutch));
        assertFalse(dutch.isVisible(Location.of(3, 3)));
        assertEquals("Edge Post",
                dutch.getKnownColonyAt(corner).orElseThrow().getColonyName());
        assertSame(france,
                dutch.getKnownColonyAt(corner).orElseThrow().getOwner());
    }

    @Test
    void colonyFoundedAfterArrivalStaysUndiscoveredThroughUpdatesAndReload() {
        final Location site = Location.of(44, 26);
        final Model model = withFounderAt(site);
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        reachTurnOf(model, dutch);
        final Set<Location> exploredAfterArrival = explored(dutch);
        final String sightingsAfterArrival = gson
                .toJson(dutch.save().getColonySightings());
        final List<Colony> foreignAtArrival = foreignColonies(model, dutch);
        assertFalse(dutch.isCurrentlyVisible(site));

        // France founds at its own turn, after Coronado has already joined.
        reachTurnOf(model, france);
        model.buildColony(france, model.getUnitById(FOUNDER_ID), "Late Fort");
        assertTrue(model.getColonyAt(site).isPresent());
        reachTurnOf(model, dutch);
        assertTrue(dutch.getKnownColonyAt(site).isEmpty());
        assertEquals(exploredAfterArrival, explored(dutch));
        assertEquals(sightingsAfterArrival,
                gson.toJson(dutch.save().getColonySightings()));

        // Save and Continue keep the arrival result and hide the later colony.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player restoredDutch = restored.getPlayerByName("Dutch");
        assertTrue(restored.getColonyAt(site).isPresent());
        assertEquals(exploredAfterArrival, explored(restoredDutch));
        assertTrue(restoredDutch.getKnownColonyAt(site).isEmpty());
        for (final Colony colony : foreignAtArrival) {
            assertEquals(colony.getName(),
                    restoredDutch.getKnownColonyAt(colony.getLocation())
                            .orElseThrow().getColonyName());
        }
        endRound(restored);
        reachTurnOf(restored, restoredDutch);
        assertTrue(restoredDutch.getKnownColonyAt(site).isEmpty());
        assertEquals(exploredAfterArrival, explored(restoredDutch));
        assertEquals(List.of(Founder.FRANCISCO_DE_CORONADO),
                restoredDutch.getCongressMembers());
    }
}
