package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;

/**
 * R-0195 placements: a living person is counted once wherever it stands, in a
 * colony, on the map, in a ship hold or on the European side. Crown-seized
 * people are excluded, other owners' people are not counted, and a saved and
 * reloaded nation keeps the same components. The people are added by saved
 * units, which the strict loader validates, or by the real model operations.
 */
class BaseScorePlacementTest {

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String FRANCE = "France";

    private static final String DUTCH = "Dutch";

    /** IDs above the free-play map's units, so none is reused. */
    private static final int PIER_PERSON_ID = 9001;

    private static final int PASSENGER_ID = 9002;

    private static final int FRENCH_SHIP_ID = 9003;

    private static final int DUTCH_SHIP_ID = 9004;

    private static final int DUTCH_PASSENGER_ID = 9005;

    /**
     * Population of the two French people placed in Europe, two points each.
     */
    private static final long EUROPE_POINTS = 4;

    private static GameModelPo freePlay() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath(FREE_PLAY);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(FRANCE);
        return data;
    }

    /**
     * Free-play plus a French colonist on the pier, a French colonist in a
     * French caravel in port, and a Dutch colonist in a Dutch caravel in port.
     */
    private static GameModelPo placedData() {
        final GameModelPo data = freePlay();
        final List<UnitPo> units = data.getModel().getUnits();
        units.add(personInEurope(PIER_PERSON_ID, FRANCE, UnitType.COLONIST,
                true));
        units.add(ship(FRENCH_SHIP_ID, FRANCE, PASSENGER_ID));
        units.add(passenger(PASSENGER_ID, FRANCE));
        units.add(ship(DUTCH_SHIP_ID, DUTCH, DUTCH_PASSENGER_ID));
        units.add(passenger(DUTCH_PASSENGER_ID, DUTCH));
        return data;
    }

    /** Fields every saved unit carries; people and ships add their own. */
    private static UnitPo unit(final int id, final String owner,
            final UnitType type) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(owner);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        unit.setCargo(new CargoPo());
        return unit;
    }

    /**
     * A saved person. The loader requires the work history and the missionary
     * role of every colonist and rejects both on any other unit, so they are
     * set here and never on a ship. The combat role stays the ordinary default.
     */
    private static UnitPo person(final int id, final String owner,
            final UnitType type) {
        final UnitPo person = unit(id, owner, type);
        person.setWorkUpdates(0);
        person.setMissionary(false);
        return person;
    }

    /** A colonist standing on the European pier. */
    private static UnitPo personInEurope(final int id, final String owner,
            final UnitType type, final boolean onPier) {
        final UnitPo person = person(id, owner, type);
        final PlaceEuropePortPo port = new PlaceEuropePortPo();
        port.setOnPier(onPier);
        person.setPlaceEuropePort(port);
        return person;
    }

    /** A colonist whose hold is named by the ship's cargo slot. */
    private static UnitPo passenger(final int id, final String owner) {
        final UnitPo person = person(id, owner, UnitType.COLONIST);
        person.setPlaceCargoSlot(new PlaceCargoSlotPo());
        return person;
    }

    /** A caravel in the port with one hold carrying the given passenger. */
    private static UnitPo ship(final int id, final String owner,
            final int passengerId) {
        final UnitPo ship = unit(id, owner, UnitType.CARAVEL);
        final PlaceEuropePortPo port = new PlaceEuropePortPo();
        ship.setPlaceEuropePort(port);
        for (int slot = 0; slot < UnitType.CARAVEL.getCargoCapacity(); slot++) {
            ship.getCargo().getSlots().add(new CargoSlotPo());
        }
        ship.getCargo().getSlots().get(0).setUnitId(passengerId);
        return ship;
    }

    private static Model make(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Model load(final GameModelPo data) {
        return make(data.getModel());
    }

    private static Player france(final Model model) {
        return model.getPlayerByName(FRANCE);
    }

    private static PlayerPo playerPo(final GameModelPo data,
            final String name) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    /** A colonist of Montreal working a field, by the real colony operation. */
    private static Unit addFieldWorker(final Model model) {
        final Colony montreal = model.getColonyById(1).orElseThrow();
        final Unit worker = model.addUnitOutSideColony(montreal);
        final ColonyField field = montreal.getEmptyFieldsWithMaxCornProduction()
                .stream().findFirst().orElseThrow();
        worker.placeToColonyField(field, GoodsType.CORN);
        return worker;
    }

    /** A colonist standing on the colony square, a map placement. */
    private static Unit addMapColonist(final Model model) {
        return model.addUnitOutSideColony(model.getColonyById(1).orElseThrow());
    }

    @Test
    void aPersonInColonyOnMapInHoldOrOnEuropeCountsOnceAndOthersAreExcluded() {
        final BaseScore base = france(load(freePlay())).getBaseScore();
        final Model model = load(placedData());
        final Player france = france(model);
        final BaseScore europe = france.getBaseScore();
        assertEquals(base.getPopulationPoints() + EUROPE_POINTS,
                europe.getPopulationPoints());
        assertEquals(base.getColonists() + 2, europe.getColonists());

        assertTrue(model.getUnitById(PIER_PERSON_ID).isAtEuropePier());
        assertTrue(model.getUnitById(PASSENGER_ID).isAtCargoSlot());

        assertTrue(addMapColonist(model).isAtPlaceLocation());
        assertEquals(europe.getPopulationPoints() + 2,
                france.getBaseScore().getPopulationPoints());
        assertTrue(addFieldWorker(model).isAtPlaceColonyField());
        final BaseScore all = france.getBaseScore();
        assertEquals(base.getPopulationPoints() + EUROPE_POINTS + 2 + 2,
                all.getPopulationPoints());
        assertEquals(base.getColonists() + 4, all.getColonists());
        assertEquals(base.getExperts(), all.getExperts());
        assertEquals(base.getServants(), all.getServants());
        assertEquals(1, france.getAllUnits().stream()
                .filter(unit -> unit.getId() == PASSENGER_ID).count());

        // The Dutch passenger counts for the Dutch, not for France; ships are
        // never people.
        final BaseScore dutchBase = load(freePlay()).getPlayerByName(DUTCH)
                .getBaseScore();
        final BaseScore dutch = model.getPlayerByName(DUTCH).getBaseScore();
        assertEquals(dutchBase.getColonists() + 1, dutch.getColonists());
        assertEquals(dutchBase.getPopulationPoints() + 2,
                dutch.getPopulationPoints());
    }

    @Test
    void crownSeizedPeopleInPortAndAboardAreNotCounted() {
        final BaseScore open = france(load(placedData())).getBaseScore();

        final GameModelPo data = placedData();
        RebellionFixtures.set(data.getModel(), playerPo(data, FRANCE), true);
        ClosedEuropeFixtures.seizeHomeEurope(data.getModel());
        final Model declared = load(data);
        assertTrue(declared.getUnitById(PIER_PERSON_ID).isCrownSeized());
        assertTrue(declared.getUnitById(PASSENGER_ID).isCrownSeized());
        final BaseScore seized = france(declared).getBaseScore();
        assertEquals(open.getPopulationPoints() - EUROPE_POINTS,
                seized.getPopulationPoints());
        assertEquals(open.getColonists() - 2, seized.getColonists());
    }

    @Test
    void aMilitaryOnlyVeteranCountsTwoPointsAndACivilianExpertVeteranFour() {
        final BaseScore base = france(load(freePlay())).getBaseScore();

        final GameModelPo soldier = freePlay();
        soldier.getModel().getUnits().add(personInEurope(PIER_PERSON_ID, FRANCE,
                UnitType.VETERAN_SOLDIER, true));
        final BaseScore military = france(load(soldier)).getBaseScore();
        assertEquals(base.getColonists() + 1, military.getColonists());
        assertEquals(base.getExperts(), military.getExperts());
        assertEquals(base.getPopulationPoints() + 2,
                military.getPopulationPoints());

        final GameModelPo expert = freePlay();
        final UnitPo veteran = personInEurope(PIER_PERSON_ID, FRANCE,
                UnitType.EXPERT_FARMER, true);
        veteran.setVeteran(true);
        expert.getModel().getUnits().add(veteran);
        final BaseScore civilian = france(load(expert)).getBaseScore();
        assertEquals(base.getExperts() + 1, civilian.getExperts());
        assertEquals(base.getColonists(), civilian.getColonists());
        assertEquals(base.getPopulationPoints() + 4,
                civilian.getPopulationPoints());
    }

    @Test
    void savedAndReloadedPeopleKeepEveryCountedPlace() {
        final Model model = load(placedData());
        final int mapId = addMapColonist(model).getId();
        final int fieldId = addFieldWorker(model).getId();
        final BaseScore before = france(model).getBaseScore();

        final Model restored = make(model.save());
        // Every counted place survives with the same stable identity.
        assertTrue(restored.getUnitById(PIER_PERSON_ID).isAtEuropePier());
        assertTrue(restored.getUnitById(PASSENGER_ID).isAtCargoSlot());
        assertTrue(restored.getUnitById(mapId).isAtPlaceLocation());
        assertTrue(restored.getUnitById(fieldId).isAtPlaceColonyField());
        final BaseScore after = france(restored).getBaseScore();
        assertEquals(before.getServants(), after.getServants());
        assertEquals(before.getColonists(), after.getColonists());
        assertEquals(before.getExperts(), after.getExperts());
        assertEquals(before.getPopulationPoints(), after.getPopulationPoints());
        assertEquals(before.getSubtotal(), after.getSubtotal());
    }
}
