package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.microcol.model.LaborLocation.Kind;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * R-0198 labor report: every living colonist of a colonial nation is counted
 * once by its unit type, wherever it stands, with its current place and flags.
 * Ships, braves, other nations' people and Crown-seized people are not labor.
 * The report is rebuilt from the live state, so a changed place is shown at
 * once and a saved and reloaded nation shows the same places.
 */
class LaborReportTest {

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String FRANCE = "France";

    private static final String DUTCH = "Dutch";

    /** IDs above the free-play map's units, so none is reused. */
    private static final int PIER_PERSON_ID = 9001;

    private static final int PASSENGER_ID = 9002;

    private static final int FRENCH_SHIP_ID = 9003;

    private static final int DUTCH_SHIP_ID = 9004;

    private static final int DUTCH_PASSENGER_ID = 9005;

    private static final int VETERAN_EXPERT_ID = 9006;

    private static final int SOLDIER_ID = 9007;

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

    /** A saved person; the loader requires its work history and role. */
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
        ship.setPlaceEuropePort(new PlaceEuropePortPo());
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
        worker.placeToColonyField(emptyField(montreal), GoodsType.CORN);
        return worker;
    }

    /** A colonist standing on the colony square, a map placement. */
    private static Unit addMapColonist(final Model model) {
        return model.addUnitOutSideColony(model.getColonyById(1).orElseThrow());
    }

    private static ColonyField emptyField(final Colony colony) {
        return colony.getEmptyFieldsWithMaxCornProduction().stream().findFirst()
                .orElseThrow();
    }

    private static Kind kindOf(final LaborReport report, final int unitId) {
        return report.findMember(unitId).orElseThrow().getLocation().getKind();
    }

    @Test
    void mixedPlacesAreCountedOnceWithTheirCurrentPlace() {
        final Model freePlayModel = load(freePlay());
        final LaborReport base = freePlayModel
                .getLaborReport(france(freePlayModel));
        final Model model = load(placedData());
        final Player france = france(model);
        final LaborReport europe = model.getLaborReport(france);
        assertEquals(base.getTotal() + 2, europe.getTotal());
        assertEquals(Kind.EUROPE, kindOf(europe, PIER_PERSON_ID));
        assertEquals(Optional.of(UnitType.CARAVEL),
                europe.findMember(PASSENGER_ID).orElseThrow().getLocation()
                        .getCarrierType());
        assertEquals(Kind.EUROPE, kindOf(europe, PASSENGER_ID));
        assertFalse(europe.findMember(FRENCH_SHIP_ID).isPresent());
        assertFalse(europe.findMember(DUTCH_PASSENGER_ID).isPresent());

        final Unit mapColonist = addMapColonist(model);
        final Unit fieldWorker = addFieldWorker(model);
        final LaborReport all = model.getLaborReport(france);
        assertEquals(base.getTotal() + 4, all.getTotal());
        assertEquals(Kind.MAP, kindOf(all, mapColonist.getId()));
        assertEquals(mapColonist.getLocation(),
                all.findMember(mapColonist.getId()).orElseThrow().getLocation()
                        .getLocation().orElseThrow());
        assertEquals(Kind.COLONY, kindOf(all, fieldWorker.getId()));
        assertEquals(Optional.of(GoodsType.CORN),
                all.findMember(fieldWorker.getId()).orElseThrow().getLocation()
                        .getFieldGoods());
        assertEquals(base.getCount(UnitType.COLONIST) + 4,
                all.getCount(UnitType.COLONIST));

        // Each row lists its people by stable ID and the rows add up to the
        // total, so no person is counted in two places.
        final int rows = all.getTypes().stream()
                .mapToInt(type -> all.getMembers(type).size()).sum();
        assertEquals(all.getTotal(), rows);
        assertEquals(all.getTotal(),
                all.getTypes().stream()
                        .flatMap(type -> all.getMembers(type).stream())
                        .map(LaborMember::getUnitId).distinct().count());
    }

    @Test
    void veteranAndEquipmentDescribeAPersonWithoutReplacingItsCivilianType() {
        final Model freePlayModel = load(freePlay());
        final LaborReport base = freePlayModel
                .getLaborReport(france(freePlayModel));
        final GameModelPo data = freePlay();
        final UnitPo veteranExpert = personInEurope(VETERAN_EXPERT_ID, FRANCE,
                UnitType.EXPERT_FARMER, true);
        veteranExpert.setVeteran(true);
        veteranExpert.setTools(30);
        data.getModel().getUnits().add(veteranExpert);
        data.getModel().getUnits().add(personInEurope(SOLDIER_ID, FRANCE,
                UnitType.VETERAN_SOLDIER, true));
        final Model model = load(data);
        final LaborReport report = model.getLaborReport(france(model));

        assertEquals(base.getTotal() + 2, report.getTotal());
        assertEquals(1, report.getCount(UnitType.EXPERT_FARMER));
        assertEquals(base.getCount(UnitType.COLONIST),
                report.getCount(UnitType.COLONIST));
        final LaborMember expert = report.findMember(VETERAN_EXPERT_ID)
                .orElseThrow();
        assertEquals(UnitType.EXPERT_FARMER, expert.getType());
        assertTrue(expert.isVeteran());
        assertEquals(Integer.valueOf(30),
                expert.getEquipment().get(GoodsType.TOOLS));
        final LaborMember soldier = report.findMember(SOLDIER_ID).orElseThrow();
        assertEquals(UnitType.VETERAN_SOLDIER, soldier.getType());
        assertTrue(soldier.isVeteran());
    }

    @Test
    void aNativeConvertIsOneColonistWithItsFlag() {
        final Model model = load(freePlay());
        final LaborReport before = model.getLaborReport(france(model));
        final UnitFreeColonist convert = (UnitFreeColonist) addMapColonist(
                model);
        convert.becomeNativeConvert(NativeNation.ARAWAK);

        final LaborReport after = model.getLaborReport(france(model));
        assertEquals(before.getTotal() + 1, after.getTotal());
        assertEquals(before.getCount(UnitType.COLONIST) + 1,
                after.getCount(UnitType.COLONIST));
        final LaborMember member = after.findMember(convert.getId())
                .orElseThrow();
        assertTrue(member.isNativeConvert());
        assertEquals(UnitType.COLONIST, member.getType());
    }

    @Test
    void crownSeizedPeopleAreExcludedAndOtherPlacesAreUnchanged() {
        final Model openModel = load(placedData());
        final LaborReport open = openModel.getLaborReport(france(openModel));

        final GameModelPo data = placedData();
        RebellionFixtures.set(data.getModel(), playerPo(data, FRANCE), true);
        ClosedEuropeFixtures.seizeHomeEurope(data.getModel());
        final Model declared = load(data);
        final LaborReport seized = declared.getLaborReport(france(declared));
        assertFalse(seized.findMember(PIER_PERSON_ID).isPresent());
        assertFalse(seized.findMember(PASSENGER_ID).isPresent());
        assertEquals(open.getTotal() - 2, seized.getTotal());
    }

    @Test
    void theReportShowsOnlyTheViewersPeople() {
        final Model model = load(placedData());
        final LaborReport french = model.getLaborReport(france(model));
        final LaborReport dutch = model
                .getLaborReport(model.getPlayerByName(DUTCH));
        assertTrue(french.findMember(PIER_PERSON_ID).isPresent());
        assertFalse(french.findMember(DUTCH_PASSENGER_ID).isPresent());
        assertTrue(dutch.findMember(DUTCH_PASSENGER_ID).isPresent());
        assertFalse(dutch.findMember(PIER_PERSON_ID).isPresent());
        assertFalse(dutch.findMember(PASSENGER_ID).isPresent());

        final Player king = model.getPlayers().stream().filter(Player::isKing)
                .findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> model.getLaborReport(king));
    }

    @Test
    void aChangedPlaceIsShownByTheNextReportWithoutLosingThePerson() {
        final Model model = load(placedData());
        final Player france = france(model);
        final Unit colonist = addMapColonist(model);
        final LaborReport onMap = model.getLaborReport(france);
        assertEquals(Kind.MAP, kindOf(onMap, colonist.getId()));

        final Colony montreal = model.getColonyById(1).orElseThrow();
        colonist.placeToColonyField(emptyField(montreal), GoodsType.CORN);
        final LaborReport inField = model.getLaborReport(france);
        assertEquals(onMap.getTotal(), inField.getTotal());
        assertEquals(Kind.COLONY, kindOf(inField, colonist.getId()));
        assertEquals(onMap.getCount(UnitType.COLONIST),
                inField.getCount(UnitType.COLONIST));
    }

    /** The thrive reference game: Delft, its town hall and galleon 3. */
    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final int GALLEON_ID = 3;

    private static final int NATIVE_PERSON_ID = 200;

    /**
     * A saved game of a person who is off the map in a course or mission that
     * never learned its settlement. The person has no sight, so loading learns
     * nothing; only the saved knowledge is removed, never a model rule.
     */
    private static Model withoutLearnedSettlements(final Model model) {
        final ModelPo saved = model.save();
        saved.setNativeSettlementKnowledge(List.of());
        return make(saved);
    }

    private static LaborLocation nativePlace(final Model model) {
        return model
                .getLaborReport(
                        model.getPlayerByName(NativeRelationsScene.DUTCH))
                .findMember(NATIVE_PERSON_ID).orElseThrow().getLocation();
    }

    @Test
    void aColonistWorkingABuildingNamesTheColonyAndTheBuilding() {
        final Model model = make(
                new GameModelPoDao().loadFromClassPath(THRIVE).getModel());
        final Colony delft = model.getColonyByName("Delft").orElseThrow();
        final Unit worker = model.addUnitOutSideColony(delft);
        worker.placeToColonyStructureSlot(
                delft.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getFirstEmptySlot().orElseThrow());

        final LaborLocation place = model.getLaborReport(delft.getOwner())
                .findMember(worker.getId()).orElseThrow().getLocation();
        assertEquals(Kind.COLONY, place.getKind());
        assertEquals(delft, place.getColony().orElseThrow());
        assertEquals(ConstructionType.TOWN_HALL,
                place.getConstruction().orElseThrow());
        assertTrue(place.isNavigable());
    }

    @Test
    void aPassengerOfAShipUnderRepairNamesTheShipAndHasNoDestination() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(THRIVE);
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == GALLEON_ID).findFirst()
                .orElseThrow();
        ship.setPlaceEuropePort(null);
        final PlaceMapPo map = new PlaceMapPo();
        map.setLocation(Location.of(26, 14));
        map.setOrientation(Direction.east);
        ship.setPlaceMap(map);
        final ConstructionPo drydock = new ConstructionPo();
        drydock.setType(ConstructionType.DRYDOCK);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(drydock);
        final Model model = make(data.getModel());
        model.startGame();
        final Ship galleon = (Ship) model.getUnitById(GALLEON_ID);
        final int passengerId = galleon.getCargo().getSlotByIndex(0).getUnit()
                .orElseThrow().getId();
        model.planShipRepair(galleon);
        model.damageShip(galleon);

        final LaborLocation place = model.getLaborReport(galleon.getOwner())
                .findMember(passengerId).orElseThrow().getLocation();
        assertEquals(Kind.REPAIR, place.getKind());
        assertEquals(Optional.of(GALLEON_ID), place.getCarrierId());
        assertEquals(Optional.of(galleon.getType()), place.getCarrierType());
        assertFalse(place.isNavigable());
    }

    @Test
    void aPassengerAboardAShipOnTheHighSeasNamesTheShipWithoutDestination() {
        final Model model = load(placedData());
        model.getUnitById(FRENCH_SHIP_ID).placeToHighSeas(false);

        final LaborLocation place = model.getLaborReport(france(model))
                .findMember(PASSENGER_ID).orElseThrow().getLocation();
        assertEquals(Kind.AT_SEA, place.getKind());
        assertEquals(Optional.of(FRENCH_SHIP_ID), place.getCarrierId());
        assertEquals(Optional.of(UnitType.CARAVEL), place.getCarrierType());
        assertFalse(place.isNavigable());
    }

    @Test
    void aNativeCourseNamesItsSettlementOnlyOnceTheOwnerHasLearnedIt() {
        final Model model = NativeRelationsScene.island(NativeNation.AZTEC)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(1, 0),
                        NativeRelationsScene.UNARMED)
                .toModel();
        final Unit pupil = model.getUnitById(NATIVE_PERSON_ID);
        final NativeSettlement site = NativeRelationsScene.settlement(model);
        model.startNativeTeaching(pupil, site, site.getTeachableSkill());

        // The pupil stood beside the settlement, so the owner really learned it
        // by sight; the course names it and the jump is allowed.
        final LaborLocation known = nativePlace(model);
        assertEquals(Kind.NATIVE_COURSE, known.getKind());
        assertEquals(site.getId(),
                known.getNativeSettlement().orElseThrow().getId());
        assertEquals(site.getLocation(), known.getLocation().orElseThrow());
        assertTrue(known.isNavigable());

        // A saved course whose settlement was never learned reveals nothing.
        final LaborLocation unknown = nativePlace(
                withoutLearnedSettlements(model));
        assertEquals(Kind.NATIVE_COURSE, unknown.getKind());
        assertTrue(unknown.getNativeSettlement().isEmpty());
        assertTrue(unknown.getLocation().isEmpty());
        assertFalse(unknown.isNavigable());
    }

    @Test
    void aNativeMissionNamesItsSettlementOnlyOnceTheOwnerHasLearnedIt() {
        final Model model = NativeRelationsScene.island(NativeNation.AZTEC)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(1, 0),
                        unit -> unit.setMissionary(true))
                .toModel();
        final Unit missionary = model.getUnitById(NATIVE_PERSON_ID);
        final NativeSettlement site = NativeRelationsScene.settlement(model);
        model.establishNativeMission(missionary, site,
                model.getNativeMissionPreview(missionary, site).orElseThrow());

        final LaborLocation known = nativePlace(model);
        assertEquals(Kind.NATIVE_MISSION, known.getKind());
        assertEquals(site.getId(),
                known.getNativeSettlement().orElseThrow().getId());
        assertEquals(site.getLocation(), known.getLocation().orElseThrow());
        assertTrue(known.isNavigable());

        final LaborLocation unknown = nativePlace(
                withoutLearnedSettlements(model));
        assertEquals(Kind.NATIVE_MISSION, unknown.getKind());
        assertTrue(unknown.getNativeSettlement().isEmpty());
        assertTrue(unknown.getLocation().isEmpty());
        assertFalse(unknown.isNavigable());
    }

    @Test
    void aPersonTakenByAnotherNationLeavesTheReportAtOnce() {
        final Model model = load(placedData());
        final LaborReport before = model.getLaborReport(france(model));
        model.getUnitById(PIER_PERSON_ID)
                .takeOver(model.getPlayerByName(DUTCH));

        final LaborReport after = model.getLaborReport(france(model));
        assertEquals(before.getTotal() - 1, after.getTotal());
        assertEquals(before.getCount(UnitType.COLONIST) - 1,
                after.getCount(UnitType.COLONIST));
        assertFalse(after.findMember(PIER_PERSON_ID).isPresent());
        assertTrue(model.getLaborReport(model.getPlayerByName(DUTCH))
                .findMember(PIER_PERSON_ID).isPresent());
    }

    @Test
    void equipmentAndNativeConvertChangeFlagsButNotTheCount() {
        final Model model = load(placedData());
        final LaborReport before = model.getLaborReport(france(model));
        final UnitFreeColonist person = (UnitFreeColonist) model
                .getUnitById(PIER_PERSON_ID);
        person.setTools(40);
        person.becomeNativeConvert(NativeNation.ARAWAK);

        final LaborReport after = model.getLaborReport(france(model));
        assertEquals(before.getTotal(), after.getTotal());
        assertEquals(before.getCount(UnitType.COLONIST),
                after.getCount(UnitType.COLONIST));
        final LaborMember member = after.findMember(PIER_PERSON_ID)
                .orElseThrow();
        assertEquals(UnitType.COLONIST, member.getType());
        assertTrue(member.isNativeConvert());
        assertEquals(Integer.valueOf(40),
                member.getEquipment().get(GoodsType.TOOLS));
    }

    @Test
    void anOwnerOfAnotherModelIsRejectedInsteadOfReadingAnUnrelatedGame() {
        final Model model = load(placedData());
        final Model other = load(freePlay());
        // The other game's player has the same name, which Player.equals
        // accepts, so only the model-local identity rejects it.
        assertEquals(france(model), france(other));
        assertThrows(IllegalArgumentException.class,
                () -> model.getLaborReport(france(other)));
        assertEquals(model.getLaborReport(france(model)).getTotal(),
                model.getLaborReport(model.getPlayerByName(FRANCE)).getTotal());
    }

    @Test
    void savedAndReloadedPeopleKeepTheirCurrentPlaces() {
        final Model model = load(placedData());
        final int mapId = addMapColonist(model).getId();
        final int fieldId = addFieldWorker(model).getId();
        final LaborReport before = model.getLaborReport(france(model));

        final Model restored = make(model.save());
        final LaborReport after = restored.getLaborReport(france(restored));
        assertEquals(before.getTotal(), after.getTotal());
        assertEquals(before.getCount(UnitType.COLONIST),
                after.getCount(UnitType.COLONIST));
        assertEquals(Kind.EUROPE, kindOf(after, PIER_PERSON_ID));
        assertEquals(Kind.EUROPE, kindOf(after, PASSENGER_ID));
        assertEquals(Kind.MAP, kindOf(after, mapId));
        assertEquals(Kind.COLONY, kindOf(after, fieldId));
    }
}
