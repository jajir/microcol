package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * R-0163: missionary ordination admission, the persisted role and the shared
 * effective-expertise query. Ordination is free, uses no movement and changes
 * only the missionary role.
 * <p>
 * Fixture (mission 2 map): colony 0 at (25,14) belongs to Dutch and has a
 * chapel; unit 0 works a field, unit 4 works the town hall, unit 3 is a galleon
 * at sea carrying colonist 16.
 * </p>
 */
class MissionaryOrdinationTest {

    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    private static final int PIER_ID = 900;

    private static final Location FREE_LAND = Location.of(24, 14);

    private static Stream<UnitType> eligibleColonistClasses() {
        return Stream.of(UnitType.INDENTURED_SERVANT, UnitType.PETTY_CRIMINAL,
                UnitType.EXPERT_FARMER, UnitType.MASTER_BLACKSMITH);
    }

    private CongressPo congress(final List<Founder> members) {
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setBells(0);
        po.setMembers(members);
        po.setCandidates(OFFER);
        po.setSelected(null);
        po.setGeneration(1);
        return po;
    }

    private GameModelPo scenario(final List<Founder> dutchMembers) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getName().equals("Dutch")) {
                player.setCongress(congress(dutchMembers));
            }
        }
        return data;
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    /** Replace the colony's religious building with the given one or none. */
    private static void setReligiousBuilding(final GameModelPo data,
            final ConstructionType type) {
        final var constructions = data.getModel().getColonies().get(0)
                .getConstructions();
        constructions.removeIf(construction -> construction
                .getType() == ConstructionType.CHURCH
                || construction.getType() == ConstructionType.CATHEDRAL
                || construction.getType() == ConstructionType.CHAPEL);
        if (type != null) {
            final ConstructionPo building = new ConstructionPo();
            building.setType(type);
            constructions.add(building);
        }
    }

    /** Add a fresh colonist class to the owner's European pier. */
    private static void addPierColonist(final GameModelPo data,
            final UnitType type) {
        final UnitPo po = new UnitPo();
        po.setId(PIER_ID);
        po.setOwnerId("Dutch");
        po.setType(type);
        po.setWorkUpdates(0);
        po.setMissionary(false);
        po.setAction(new UnitActionNoActionPo());
        po.setAvailableMoves(1);
        po.setPlaceEuropePort(new PlaceEuropePortPo());
        data.getModel().getUnits().add(po);
    }

    private static Model start(final GameModelPo data) {
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        return model;
    }

    private static Colony colony(final Model model) {
        return model.getColonyById(0).orElseThrow();
    }

    private static UnitFreeColonist colonist(final Model model, final int id) {
        return (UnitFreeColonist) model.getUnitById(id);
    }

    /** The unit leaves its job and stands on the colony tile. */
    private static UnitFreeColonist outside(final Model model, final int id) {
        model.getUnitById(id).placeToMap(colony(model).getLocation());
        return colonist(model, id);
    }

    private static JsonElement snapshot(final Model model) {
        return new Gson().toJsonTree(model.save());
    }

    private static JsonObject saved(final Unit unit) {
        return new Gson().toJsonTree(unit.save()).getAsJsonObject();
    }

    private static void assertRejected(final Model model, final Unit unit,
            final OrdinationProblem expected) {
        final JsonElement before = snapshot(model);
        assertEquals(Optional.of(expected), model.getOrdinationProblem(unit));
        assertThrows(IllegalStateException.class,
                () -> model.ordainMissionary(unit));
        assertEquals(before, snapshot(model));
    }

    /** Ordain and require that only the missionary role changed. */
    private static void assertOrdainedChangingOnlyTheRole(final Model model,
            final UnitFreeColonist unit) {
        final JsonObject expected = saved(unit);
        expected.addProperty("missionary", true);
        assertEquals(Optional.empty(), model.getOrdinationProblem(unit));

        model.ordainMissionary(unit);

        assertTrue(unit.isMissionary());
        assertEquals(expected, saved(unit));
        assertSame(unit, model.getUnitById(unit.getId()));
    }

    @Test
    void churchColonyOrdainsOrdinaryColonistChangingOnlyTheRole() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        final JsonElement before = snapshot(model);

        assertEquals(Optional.empty(), model.getOrdinationProblem(unit));
        assertEquals(before, snapshot(model));

        assertOrdainedChangingOnlyTheRole(model, unit);
        assertFalse(unit.hasEffectiveMissionaryExpertise());
        assertSame(UnitType.COLONIST, unit.getType());
    }

    @Test
    void cathedralColonyAcceptsOrdination() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CATHEDRAL);
        final Model model = start(data);

        assertOrdainedChangingOnlyTheRole(model, outside(model, 0));
    }

    @Test
    void chapelAloneIsNotEnoughAndChangesNothing() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHAPEL);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);

        assertRejected(model, unit, OrdinationProblem.NO_CHURCH);
        assertFalse(unit.isMissionary());
    }

    @Test
    void colonyWithoutReligiousBuildingIsRejectedUnchanged() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, null);
        final Model model = start(data);

        assertRejected(model, outside(model, 0), OrdinationProblem.NO_CHURCH);
    }

    @Test
    void europePierAcceptsWithoutAnyReligiousBuilding() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, null);
        addPierColonist(data, UnitType.COLONIST);
        final Model model = start(data);
        final UnitFreeColonist unit = colonist(model, PIER_ID);
        assertTrue(unit.isAtEuropePier());

        assertOrdainedChangingOnlyTheRole(model, unit);
        assertTrue(unit.isAtEuropePier());
    }

    @ParameterizedTest
    @MethodSource("eligibleColonistClasses")
    void everyColonistClassIsEligibleOnThePier(final UnitType type) {
        final GameModelPo data = scenario(List.of());
        addPierColonist(data, type);
        final Model model = start(data);
        final UnitFreeColonist unit = colonist(model, PIER_ID);

        assertOrdainedChangingOnlyTheRole(model, unit);
        assertSame(type, unit.getType());
    }

    @ParameterizedTest
    @MethodSource("eligibleColonistClasses")
    void everyColonistClassIsEligibleOutsideAChurchColony(final UnitType type) {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        unitPo(data, 0).setType(type);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);

        assertOrdainedChangingOnlyTheRole(model, unit);
        assertSame(type, unit.getType());
    }

    @Test
    void equipmentAndExactMovementAreKept() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist armed = outside(model, 0);
        armed.setHoldingGuns(true);
        armed.setMounted(true);
        final UnitFreeColonist carrier = outside(model, 1);
        carrier.setTools(20);
        final int armedMovement = armed.getMovementThirds();
        final int carrierMovement = carrier.getMovementThirds();

        assertOrdainedChangingOnlyTheRole(model, armed);
        assertOrdainedChangingOnlyTheRole(model, carrier);

        assertTrue(armed.isHoldingGuns());
        assertTrue(armed.isMounted());
        assertEquals(armedMovement, armed.getMovementThirds());
        assertEquals(20, carrier.getTools());
        assertEquals(carrierMovement, carrier.getMovementThirds());
    }

    @Test
    void assignedFieldWorkerMustLeaveItsJobFirst() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist worker = colonist(model, 0);
        assertTrue(worker.isAtPlaceColonyField());

        assertRejected(model, worker, OrdinationProblem.ASSIGNED_WORKER);
    }

    @Test
    void assignedBuildingWorkerMustLeaveItsJobFirst() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist worker = colonist(model, 4);
        assertTrue(worker.isAtPlaceConstructionSlot());

        assertRejected(model, worker, OrdinationProblem.ASSIGNED_WORKER);
    }

    @Test
    void workerCanBeOrdainedAfterLeavingItsJob() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist worker = colonist(model, 4);
        assertEquals(Optional.of(OrdinationProblem.ASSIGNED_WORKER),
                model.getOrdinationProblem(worker));

        outside(model, 4);

        assertOrdainedChangingOnlyTheRole(model, worker);
    }

    @Test
    void cargoOnAShipAtSeaIsRejected() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist passenger = colonist(model, 16);
        assertTrue(passenger.isAtCargoSlot());

        assertRejected(model, passenger,
                OrdinationProblem.NOT_IN_EUROPE_OR_COLONY);
    }

    @Test
    void shipIsNotAColonist() {
        final GameModelPo data = scenario(List.of());
        final Model model = start(data);

        assertRejected(model, model.getUnitById(3),
                OrdinationProblem.UNAVAILABLE);
    }

    @Test
    void colonistAwayFromAnyColonyIsRejected() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        model.getUnitById(0).placeToMap(FREE_LAND);

        assertRejected(model, colonist(model, 0),
                OrdinationProblem.NOT_IN_EUROPE_OR_COLONY);
    }

    @Test
    void activeSentryOrderIsRejectedUntilCancelled() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        model.setSentry(unit);

        assertRejected(model, unit, OrdinationProblem.ACTIVE_ORDER);

        model.activateSentry(unit);
        assertEquals(Optional.empty(), model.getOrdinationProblem(unit));
    }

    @Test
    void jesuitAndExistingMissionaryAreAlreadyMissionaries() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        unitPo(data, 0).setType(UnitType.JESUIT_MISSIONARY);
        unitPo(data, 1).setMissionary(true);
        final Model model = start(data);
        final UnitFreeColonist jesuit = outside(model, 0);
        final UnitFreeColonist missionary = outside(model, 1);

        assertRejected(model, jesuit, OrdinationProblem.ALREADY_MISSIONARY);
        assertRejected(model, missionary, OrdinationProblem.ALREADY_MISSIONARY);
        assertSame(UnitType.JESUIT_MISSIONARY, jesuit.getType());
    }

    @Test
    void repeatedOrdinationCannotDuplicateTheRole() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        final int gold = model.getPlayerByName("Dutch").getGold();
        model.ordainMissionary(unit);
        final JsonElement afterFirst = snapshot(model);

        assertRejected(model, unit, OrdinationProblem.ALREADY_MISSIONARY);
        assertEquals(afterFirst, snapshot(model));
        assertEquals(gold, model.getPlayerByName("Dutch").getGold());
    }

    @Test
    void staleJobAssignmentBetweenPreviewAndConfirmIsRejected() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        assertEquals(Optional.empty(), model.getOrdinationProblem(unit));

        // The colonist takes a building job after the preview was computed.
        unit.placeToColonyStructureSlot(
                colony(model).getConstructionByType(ConstructionType.TOWN_HALL)
                        .getFirstEmptySlot().orElseThrow());

        assertRejected(model, unit, OrdinationProblem.ASSIGNED_WORKER);
    }

    @Test
    void staleOwnershipBetweenPreviewAndConfirmIsRejected() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        assertEquals(Optional.empty(), model.getOrdinationProblem(unit));

        // Another nation takes the colonist over after the preview.
        unit.takeOver(model.getPlayerByName("Dutch's King"));
        final JsonObject before = saved(unit);

        assertEquals(Optional.of(OrdinationProblem.UNAVAILABLE),
                model.getOrdinationProblem(unit));
        assertThrows(IllegalStateException.class,
                () -> model.ordainMissionary(unit));
        assertFalse(unit.isMissionary());
        assertEquals(before, saved(unit));
    }

    @Test
    void unstartedGameRejectsOrdinationWithoutMutation() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        addPierColonist(data, UnitType.COLONIST);
        // The bundled map is saved as started; this game has not begun.
        data.getModel().getGameManager().setGameStarted(false);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist unit = colonist(model, PIER_ID);
        final JsonObject before = saved(unit);

        assertFalse(model.isGameRunning());
        assertEquals(Optional.of(OrdinationProblem.UNAVAILABLE),
                model.getOrdinationProblem(unit));
        assertThrows(IllegalStateException.class,
                () -> model.ordainMissionary(unit));
        assertEquals(before, saved(unit));
    }

    @Test
    void specialistKeepsProductionExpertiseAfterOrdination() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        unitPo(data, 0).setType(UnitType.EXPERT_FARMER);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);

        assertOrdainedChangingOnlyTheRole(model, unit);

        assertSame(UnitType.EXPERT_FARMER, unit.getType());
        assertEquals(2.0F,
                unit.getType().getProductionModifier(GoodsType.CORN));
    }

    @Test
    void ordinaryMissionaryIsExpertOnlyWhileOwnerHoldsBrebeuf() {
        final GameModelPo ordinary = scenario(List.of());
        setReligiousBuilding(ordinary, ConstructionType.CHURCH);
        final Model before = start(ordinary);
        final UnitFreeColonist plain = outside(before, 0);
        before.ordainMissionary(plain);
        assertFalse(plain.hasEffectiveMissionaryExpertise());

        final GameModelPo brebeuf = scenario(List.of(Founder.JEAN_DE_BREBEUF));
        setReligiousBuilding(brebeuf, ConstructionType.CHURCH);
        final Model after = start(brebeuf);
        final UnitFreeColonist future = outside(after, 0);
        after.ordainMissionary(future);
        assertTrue(future.hasEffectiveMissionaryExpertise());
    }

    @Test
    void existingMissionaryBecomesExpertWhenBrebeufIsPresent() {
        final GameModelPo plain = scenario(List.of());
        unitPo(plain, 0).setMissionary(true);
        assertFalse(
                colonist(start(plain), 0).hasEffectiveMissionaryExpertise());

        final GameModelPo brebeuf = scenario(List.of(Founder.JEAN_DE_BREBEUF));
        unitPo(brebeuf, 0).setMissionary(true);
        assertTrue(
                colonist(start(brebeuf), 0).hasEffectiveMissionaryExpertise());
    }

    @Test
    void brebeufJoiningThroughOwnerUpdateMakesAnExistingMissionaryExpert() {
        final GameModelPo data = scenario(List.of());
        setReligiousBuilding(data, ConstructionType.CHURCH);
        unitPo(data, 0).setMissionary(true);
        RealFounderJoin.readyFor(RealFounderJoin.congressOf(data, "Dutch"),
                Founder.JEAN_DE_BREBEUF);
        final Model model = start(data);
        assertFalse(colonist(model, 0).hasEffectiveMissionaryExpertise());

        RealFounderJoin.joinThroughOwnerUpdate(model, Founder.JEAN_DE_BREBEUF);
        assertTrue(colonist(model, 0).hasEffectiveMissionaryExpertise());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertTrue(colonist(restored, 0).isMissionary());
        assertTrue(colonist(restored, 0).hasEffectiveMissionaryExpertise());
    }

    @Test
    void brebeufDoesNotMakeAnOrdinaryColonistAMissionary() {
        final GameModelPo data = scenario(List.of(Founder.JEAN_DE_BREBEUF));
        final UnitFreeColonist unit = colonist(start(data), 0);

        assertFalse(unit.isMissionary());
        assertFalse(unit.hasEffectiveMissionaryExpertise());
    }

    @Test
    void jesuitIsExpertWithoutBrebeufAndStaysExpertWithIt() {
        final GameModelPo withoutBrebeuf = scenario(List.of());
        unitPo(withoutBrebeuf, 0).setType(UnitType.JESUIT_MISSIONARY);
        final UnitFreeColonist plain = colonist(start(withoutBrebeuf), 0);
        assertTrue(plain.isMissionary());
        assertTrue(plain.hasEffectiveMissionaryExpertise());

        // A Jesuit is one expert, whatever the founder membership.
        final GameModelPo withBrebeuf = scenario(
                List.of(Founder.JEAN_DE_BREBEUF));
        unitPo(withBrebeuf, 0).setType(UnitType.JESUIT_MISSIONARY);
        assertTrue(colonist(start(withBrebeuf), 0)
                .hasEffectiveMissionaryExpertise());
    }

    @Test
    void anotherOwnersBrebeufDoesNotMakeThisMissionaryExpert() {
        final GameModelPo data = scenario(List.of(Founder.JEAN_DE_BREBEUF));
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        model.ordainMissionary(unit);
        assertTrue(unit.hasEffectiveMissionaryExpertise());

        // The colonist now belongs to a nation without that membership.
        unit.takeOver(model.getPlayerByName("Dutch's King"));

        assertTrue(unit.isMissionary());
        assertFalse(unit.hasEffectiveMissionaryExpertise());
    }

    @Test
    void saveAndContinuePreservesRoleAndBrebeufMembership() {
        final GameModelPo data = scenario(List.of(Founder.JEAN_DE_BREBEUF));
        setReligiousBuilding(data, ConstructionType.CHURCH);
        final Model model = start(data);
        final UnitFreeColonist unit = outside(model, 0);
        model.ordainMissionary(unit);
        final JsonObject before = saved(unit);

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());

        final UnitFreeColonist loaded = colonist(restored, 0);
        assertEquals(before, saved(loaded));
        assertTrue(loaded.isMissionary());
        assertTrue(loaded.hasEffectiveMissionaryExpertise());
        assertTrue(restored.getPlayerByName("Dutch").getCongressMembers()
                .contains(Founder.JEAN_DE_BREBEUF));
    }

    @Test
    void savedColonistWithoutMissionaryRoleIsRejectedOnLoad() {
        final GameModelPo data = scenario(List.of());
        unitPo(data, 0).setMissionary(null);

        assertThrows(IllegalArgumentException.class, () -> start(data));
    }

    @Test
    void savedShipWithMissionaryRoleIsRejectedOnLoad() {
        final GameModelPo data = scenario(List.of());
        unitPo(data, 3).setMissionary(false);

        assertThrows(IllegalArgumentException.class, () -> start(data));
    }
}
