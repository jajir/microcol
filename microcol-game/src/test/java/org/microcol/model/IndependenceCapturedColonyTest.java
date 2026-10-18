package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * R-0105 with a captured colony: after the Dutch take French Quebec, its stored
 * liberty counts for the Dutch with the Dutch Bolivar bonus only. Quebec has
 * one resident, so liberty 100 is 50 percent and 80 is 40 percent before the 20
 * points of a Bolivar nation. The Dutch own only this single, coastal colony,
 * in 1490.
 */
class IndependenceCapturedColonyTest {

    private static final Location QUEBEC = Location.of(17, 10);

    private static final Location WATER = Location.of(18, 9);

    private GameModelPo data(final String bolivarNation, final long liberty) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .setLiberty(liberty);
        if (!"none".equals(bolivarNation)) {
            giveBolivar(data, bolivarNation);
        }
        // One idle field beside Quebec becomes water: its only coast.
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(WATER, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        trees.remove(WATER);
        data.getModel().getMap().setTrees(trees);
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        fields.remove(WATER);
        data.getModel().getMap().setFields(fields);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        return data;
    }

    private void giveBolivar(final GameModelPo data, final String nation) {
        final PlayerPo player = data.getModel().getGameManager().getPlayers()
                .stream().filter(saved -> nation.equals(saved.getName()))
                .findFirst().orElseThrow();
        // A fully valid active Congress: Bolivar is the only member and every
        // category offers exactly one other founder.
        final CongressPo congress = player.getCongress();
        congress.setActivated(true);
        congress.setGeneration(1);
        congress.setBells(0);
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        congress.setMembers(List.of(Founder.SIMON_BOLIVAR));
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder founder : Founder.values()) {
                if (founder.getCategory() == category
                        && founder != Founder.SIMON_BOLIVAR) {
                    candidates.add(founder);
                    break;
                }
            }
        }
        congress.setCandidates(candidates);
    }

    /** The Dutch armed veterans standing on the colony square, by ID. */
    private void addVeterans(final Model model, final Colony colony,
            final int count) {
        for (int index = 0; index < count; index++) {
            final UnitFreeColonist veteran = (UnitFreeColonist) model
                    .addUnitOutSideColony(colony);
            veteran.setHoldingGuns(true);
            veteran.completeEducation(UnitType.VETERAN_SOLDIER);
            assertSame(colony.getOwner(), veteran.getOwner());
        }
    }

    private List<Integer> roleHolders(final Model model) {
        final List<Integer> out = new ArrayList<>();
        model.getAllUnits().stream().filter(UnitFreeColonist.class::isInstance)
                .map(UnitFreeColonist.class::cast)
                .filter(unit -> unit.getMilitaryRole().isSpecial())
                .forEach(unit -> out.add(unit.getId()));
        out.sort(Integer::compare);
        return out;
    }

    @ParameterizedTest
    @CsvSource({ "none,100,50,true,1", "Dutch,100,70,true,2",
            "France,100,50,true,1", "none,80,40,false,0", "Dutch,80,60,true,1",
            "France,80,40,false,0" })
    void aCapturedColonyCountsForTheConquerorWithItsOwnBolivarOnly(
            final String bolivarNation, final long liberty,
            final int expectedPercent, final boolean eligible,
            final int expectedMustered) {
        final Model model = Model.make(data(bolivarNation, liberty).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final Colony quebec = model.getColonyById(0).orElseThrow();
        final Colony montreal = model.getColonyById(1).orElseThrow();
        assertEquals(1, quebec.getPopulation());
        model.declareHostilities(france, dutch);
        // The armed French colonist guarding the square would block capture.
        model.getUnitById(6).placeToLocation(Location.of(18, 10));
        assertEquals(Optional.of(IndependenceProblem.NO_COASTAL_COLONY),
                dutch.getIndependenceProblem());

        // The Dutch capture Quebec by a real battle for the colony.
        final Unit invader = model.addUnitOutSideColony(quebec);
        invader.takeOver(dutch);
        ColonyBattles.capture(model, quebec, invader);
        assertSame(dutch, quebec.getOwner());
        assertSame(dutch, quebec.getLastColonialAffiliation());
        assertEquals(liberty, quebec.getLiberty());
        assertEquals(expectedPercent,
                quebec.getRebelSupport().getDisplayPercent());
        assertEquals(quebec.getRebelSupport(), dutch.getNationalRebelSupport());
        assertEquals(montreal.getRebelSupport(),
                france.getNationalRebelSupport());
        assertTrue(model.isOwnedCoastalPort(dutch, QUEBEC));
        assertEquals(1, dutch.getColonies().size());

        addVeterans(model, quebec, 3);
        ColonyBattles.setCurrentPlayer(model, dutch);
        assertEquals(eligible, dutch.isPossibleToDecalareIndependence());
        assertEquals(
                eligible ? Optional.empty()
                        : Optional.of(IndependenceProblem.INSUFFICIENT_SUPPORT),
                dutch.getIndependenceProblem());
        if (!eligible) {
            assertThrows(IllegalStateException.class,
                    dutch::declareIndependence);
            assertEquals(List.of(), roleHolders(model));
            return;
        }

        // Exact local support, not the national percentage, sets the share:
        // floor(3 * support) of the three veterans, lowest ID first.
        final List<Integer> expected = new ArrayList<>();
        model.getUnitsAt(QUEBEC).stream()
                .filter(UnitFreeColonist.class::isInstance)
                .map(UnitFreeColonist.class::cast)
                .filter(unit -> unit.isVeteran() && unit.isHoldingGuns())
                .map(Unit::getId).sorted().limit(expectedMustered)
                .forEach(expected::add);
        assertEquals(expectedMustered, expected.size());
        dutch.declareIndependence();
        assertTrue(dutch.isDeclaredIndependence());
        assertEquals(expected, roleHolders(model));
    }
}
