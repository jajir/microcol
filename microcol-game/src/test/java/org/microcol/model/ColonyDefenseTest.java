package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * R-0096: the defense of a colony by its armed units, its inhabitants as one
 * civilian group and a Paul Revere emergency militiaman, and the capture of the
 * whole colony. Battle results are controlled by choosing the saved combat
 * random state, never by weakening a precondition.
 */
class ColonyDefenseTest {

    private static final Location COLONY = Location.of(17, 10);
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location GARRISON_AWAY = Location.of(17, 11);
    private static final Location WEST_WATER = Location.of(16, 9);
    private static final Location EAST_WATER = Location.of(17, 9);

    private static final Location FIELD = Location.of(17, 9);
    private static final Location WEST_FIELD = Location.of(16, 10);

    private final Gson gson = new Gson();
    /** Keep France at peace with the Dutch instead of declaring war. */
    private boolean peace;
    private Model model;
    private Colony quebec;
    private UnitFreeColonist attacker;
    private Player dutch;
    private Player france;
    private final List<UnitAttackedEvent> attacks = new ArrayList<>();
    private final List<ColonyWasCapturedEvent> captures = new ArrayList<>();

    /** What a test needs on the French colony of Quebec. */
    private static final class Spec {
        private int fieldWorkers;
        private int indoorWorkers;
        private boolean revere;
        private boolean garrison;
        private boolean coastal;
        private boolean stockade;
        private boolean washington;
        private long liberty;

        /** Colony with a completed stockade, the lowest fortification. */
        private Spec withStockade() {
            stockade = true;
            return this;
        }

        /** Give France George Washington as well. */
        private Spec withWashington() {
            washington = true;
            return this;
        }

        /** Keep the French soldier on the colony tile. */
        private Spec withGarrison() {
            garrison = true;
            return this;
        }

        /** Give France Paul Revere. */
        private Spec withRevere() {
            revere = true;
            return this;
        }
    }

    private static Spec spec() {
        return new Spec();
    }

    private Spec civilians(final int fields, final int indoors) {
        final Spec out = new Spec();
        out.fieldWorkers = fields;
        out.indoorWorkers = indoors;
        return out;
    }

    private UnitPo resident(final int id, final boolean field) {
        final UnitPo po = new UnitPo();
        po.setId(id);
        po.setType(UnitType.COLONIST);
        po.setOwnerId("France");
        final CargoPo cargo = new CargoPo();
        cargo.setSlots(new ArrayList<>());
        po.setCargo(cargo);
        po.setAction(new UnitActionNoAction().save());
        po.setAvailableMoves(1);
        po.setWorkUpdates(0);
        po.setMissionary(false);
        if (field) {
            po.setPlaceColonyField(new PlaceColonyFieldPo());
        } else {
            po.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        }
        return po;
    }

    private void giveFounders(final ModelPo data, final String nation,
            final List<Founder> members) {
        final PlayerPo player = data.getGameManager().getPlayers().stream()
                .filter(candidate -> nation.equals(candidate.getName()))
                .findFirst().orElseThrow();
        final CongressPo congress = player.getCongress();
        congress.setActivated(true);
        congress.setGeneration(1);
        congress.setBells(0);
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        congress.setMembers(members);
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder other : Founder.values()) {
                if (other.getCategory() == category
                        && !members.contains(other)) {
                    candidates.add(other);
                    break;
                }
            }
        }
        congress.setCandidates(candidates);
    }

    /**
     * Quebec at (17,10) with a French garrison soldier on the tile or not,
     * workers in the eight fields and in the town hall, and a Dutch armed
     * colonist next to it.
     */
    private ModelPo data(final Spec spec) {
        final GameModelPo saved = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        final ModelPo data = saved.getModel();
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer("Dutch");
        final Map<Location, TerrainType> terrain = data.getMap()
                .getTerrainMap();
        COLONY.getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        if (spec.coastal) {
            terrain.put(WEST_WATER, TerrainType.OCEAN);
            terrain.put(EAST_WATER, TerrainType.OCEAN);
        }
        data.getMap().setTerrainType(terrain);
        final List<Founder> founders = new ArrayList<>();
        if (spec.revere) {
            founders.add(Founder.PAUL_REVERE);
        }
        if (spec.washington) {
            founders.add(Founder.GEORGE_WASHINGTON);
        }
        if (!founders.isEmpty()) {
            giveFounders(data, "France", founders);
        }
        final ColonyPo colonyPo = data.getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        colonyPo.setLiberty(spec.liberty);
        if (spec.stockade) {
            final ConstructionPo defense = new ConstructionPo();
            defense.setType(ConstructionType.STOCKADE);
            colonyPo.getConstructions().add(defense);
        }
        int nextId = 100;
        int placed = 0;
        for (final ColonyFieldPo field : colonyPo.getColonyFields()) {
            if (field.getWorkerId() == null && placed < spec.fieldWorkers) {
                field.setWorkerId(nextId);
                field.setProducedGoodsType(GoodsType.CORN);
                data.getUnits().add(resident(nextId++, true));
                placed++;
            }
        }
        int indoor = 0;
        for (final ConstructionPo construction : colonyPo.getConstructions()) {
            if (construction.getType() != ConstructionType.TOWN_HALL
                    && construction
                            .getType() != ConstructionType.CARPENTERS_STAND) {
                continue;
            }
            for (int index = 0; index < construction.getSlots().size()
                    && indoor < spec.indoorWorkers; index++) {
                construction.getSlots().get(index).setWorkerId(nextId);
                data.getUnits().add(resident(nextId++, false));
                indoor++;
            }
        }
        final UnitPo garrison = data.getUnits().stream()
                .filter(unit -> unit.getId() == 6).findFirst().orElseThrow();
        if (!spec.garrison) {
            final PlaceMapPo away = garrison.getPlaceMap();
            away.setLocation(GARRISON_AWAY);
        }
        data.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        return data;
    }

    private Model build(final Spec spec) {
        return build(data(spec));
    }

    private Model build(final ModelPo data) {
        model = Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
        dutch = model.getPlayerByName("Dutch");
        france = model.getPlayerByName("France");
        quebec = model.getColonyById(0).orElseThrow();
        attacker = (UnitFreeColonist) model.getUnitById(0);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        if (!peace) {
            model.declareHostilities(dutch, france);
        }
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                attacks.add(event);
            }

            @Override
            public void onColonyWasCaptured(
                    final ColonyWasCapturedEvent event) {
                captures.add(event);
            }
        });
        return model;
    }

    /**
     * A saved random state whose first draw gives the wanted result for the
     * quoted odds, and whose second draw would not promote a winner.
     */
    private static long stateFor(final AttackOrder order,
            final boolean attackerWins) {
        return CombatDraws.stateFor(order.getCombatStrengths().orElseThrow()
                .getAttackerWinProbability(), attackerWins);
    }

    private void setState(final long state)
            throws ReflectiveOperationException {
        final Field field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(model, state);
    }

    private AttackOrder quote() {
        return attacker.prepareAttack(COLONY, false).orElseThrow();
    }

    private void fight(final boolean attackerWins)
            throws ReflectiveOperationException {
        fightAt(COLONY, attackerWins);
    }

    private void fightAt(final Location target, final boolean attackerWins)
            throws ReflectiveOperationException {
        final AttackOrder order = attacker.prepareAttack(target, false)
                .orElseThrow();
        setState(stateFor(order, attackerWins));
        order.execute();
    }

    private String snapshot() {
        return gson.toJson(model.save());
    }

    private Model reloaded() {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private ConstructionSlot freeSlot(final Colony colony) {
        for (final Construction construction : colony.getConstructions()) {
            for (final ConstructionSlot slot : construction
                    .getConstructionSlots()) {
                if (slot.isEmpty()) {
                    return slot;
                }
            }
        }
        throw new AssertionError("No free job in " + colony.getName());
    }

    private UnitFreeColonist armedOnColonyTile() {
        final UnitFreeColonist soldier = (UnitFreeColonist) model
                .addUnitOutSideColony(quebec);
        soldier.setHoldingGuns(true);
        return soldier;
    }

    // ------------------------------------------------------------ roster

    @Test
    void civilianGroupDefendsOnceFromTheColonyCenterAtBaseOne() {
        build(spec());
        final String before = snapshot();
        // The map-only occupant query never lists a colony resident.
        assertTrue(model.getEnemyUnitsAt(dutch, COLONY).isEmpty());
        assertTrue(attacker.isPossibleToAttackAt(COLONY));
        final AttackOrder order = quote();
        final ColonyDefense defense = order.getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.CIVILIANS, defense.getKind());
        assertEquals(COLONY, defense.getBattlefield());
        assertEquals(COLONY, order.getBattlefield());
        assertSame(model.getUnitById(4), defense.getDefender());
        assertSame(model.getUnitById(4), order.getDefender());
        assertEquals(List.of(model.getUnitById(4)), defense.getRoster());
        assertTrue(defense.capturesColony());
        assertEquals(0, defense.getRevereMuskets());
        final CombatStrength defending = order.getCombatStrengths()
                .orElseThrow().getDefender();
        assertEquals(1.0, defending.getBase());
        assertEquals(0, defending.getFortificationBonus());
        assertEquals(0, defending.getArtilleryBonus());
        // Previewing and cancelling change nothing, not even the random state.
        assertEquals(before, snapshot());
    }

    @Test
    void allEightFieldWorkersAndIndoorJobsDefendAndTheLowestIdRepresents() {
        build(civilians(7, 3));
        assertEquals(11, quebec.getPopulation());
        quebec.getColonyFields().forEach(field -> assertFalse(field.isEmpty()));
        final ColonyDefense defense = quote().getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.CIVILIANS, defense.getKind());
        assertEquals(11, defense.getRoster().size());
        assertTrue(
                defense.getRoster().containsAll(quebec.getUnitsInsideColony()));
        // Every civilian has base 1, so the stable unit identity decides.
        assertSame(model.getUnitById(4), defense.getDefender());
    }

    @Test
    void armedUnitsDefendFirstAndTheStrongestEligibleOneRepresents() {
        build(spec().withGarrison());
        final UnitFreeColonist mounted = armedOnColonyTile();
        mounted.setMounted(true);
        final UnitFreeColonist plain = armedOnColonyTile();
        final ColonyDefense defense = quote().getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.ARMED, defense.getKind());
        assertFalse(defense.capturesColony());
        // The mounted soldier is stronger than the garrison soldier 6 even
        // though its identity is higher; equal soldiers fall back to identity.
        assertSame(mounted, defense.getDefender());
        assertEquals(List.of(mounted, model.getUnitById(6), plain),
                defense.getRoster());
        // The resident civilian never defends while armed units exist.
        assertFalse(defense.getRoster().contains(model.getUnitById(4)));
    }

    @Test
    void armedResidentsJoinTheArmedTierWithTheirActualPlace() {
        final Spec withFields = civilians(1, 0).withGarrison();
        final ModelPo data = data(withFields);
        data.getUnits().stream().filter(unit -> unit.getId() == 100).findFirst()
                .orElseThrow().setHoldingGuns(true);
        build(data);
        final ColonyDefense defense = quote().getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.ARMED, defense.getKind());
        assertEquals(List.of(model.getUnitById(6), model.getUnitById(100)),
                defense.getRoster());
        assertTrue(model.getUnitById(100).isAtPlaceColonyField());
    }

    @Test
    void ordinaryMovementCannotBypassTheCivilianBattle() {
        build(spec());
        final String before = snapshot();
        assertFalse(attacker.isPossibleToMoveAt(COLONY));
        assertFalse(attacker.isPossibleToMoveAt(COLONY, true));
        assertTrue(attacker.getPath(COLONY).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> attacker.moveOneStep(COLONY));
        assertThrows(IllegalArgumentException.class,
                () -> model.moveUnit(attacker, Path.of(List.of(COLONY))));
        assertEquals(before, snapshot());
        assertSame(france, quebec.getOwner());
    }

    // ------------------------------------------------------ capture result

    @Test
    void civilianVictoryTransfersTheWholeColonyWithEveryJobExactlyOnce()
            throws ReflectiveOperationException {
        final Spec big = civilians(7, 3);
        big.liberty = 150L;
        build(big);
        final List<Unit> residents = new ArrayList<>(
                quebec.getUnitsInsideColony());
        final int units = model.getAllUnits().size();
        final String stores = gson.toJson(quebec.save().getColonyWarehouse());
        fight(true);
        assertSame(dutch, quebec.getOwner());
        assertEquals(0, quebec.getId());
        assertEquals("Quebec", quebec.getName());
        assertEquals(residents, quebec.getUnitsInsideColony());
        residents.forEach(worker -> assertSame(dutch, worker.getOwner()));
        quebec.getColonyFields().forEach(field -> assertSame(dutch,
                field.getUnit().orElseThrow().getOwner()));
        assertEquals(units, model.getAllUnits().size());
        assertEquals(stores, gson.toJson(quebec.save().getColonyWarehouse()));
        // The capturer stands on the colony tile, outside every job.
        assertTrue(attacker.isAtPlaceLocation());
        assertEquals(COLONY, attacker.getLocation());
        assertEquals(0, attacker.getActionPoints());
        assertTrue(attacker.isHoldingGuns());
        // R-0100: liberty is kept and the conqueror becomes the affiliation.
        assertEquals(150L, quebec.getLiberty());
        assertSame(dutch, quebec.getLastColonialAffiliation());
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertEquals(1, attacks.size());
        assertEquals(ColonyBattleOutcome.Result.COLONY_CAPTURED,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        assertEquals(COLONY,
                attacks.get(0).getOriginalDefenderLocation().orElseThrow());
        assertEquals(1, captures.size());
        // Save and Continue keep identity, workers and ownership.
        final Model restored = reloaded();
        final Colony reloadedColony = restored.getColonyById(0).orElseThrow();
        assertSame(restored.getPlayerByName("Dutch"),
                reloadedColony.getOwner());
        assertEquals(11, reloadedColony.getPopulation());
        assertEquals(gson.toJson(model.save()), gson.toJson(restored.save()));
    }

    @Test
    void civilianDefeatCostsTheAttackerItsMusketsAndChangesNoOwner()
            throws ReflectiveOperationException {
        build(civilians(2, 1));
        final List<Unit> residents = new ArrayList<>(
                quebec.getUnitsInsideColony());
        fight(false);
        assertSame(france, quebec.getOwner());
        assertEquals(residents, quebec.getUnitsInsideColony());
        assertFalse(attacker.isHoldingGuns());
        assertEquals(ORIGIN, attacker.getLocation());
        assertEquals(0, attacker.getActionPoints());
        assertEquals(0, model.getUnitById(4).getActionPoints());
        assertEquals(1, attacks.size());
        assertEquals(ColonyBattleOutcome.Result.ATTACKER_EQUIPMENT_LOST,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        assertTrue(captures.isEmpty());
    }

    @Test
    void victoryOverTheLastArmedDefenderDoesNotSkipTheCivilianBattle()
            throws ReflectiveOperationException {
        build(civilians(1, 0).withGarrison());
        final UnitFreeColonist garrison = (UnitFreeColonist) model
                .getUnitById(6);
        fight(true);
        assertFalse(garrison.isHoldingGuns());
        assertSame(france, quebec.getOwner());
        assertEquals(ORIGIN, attacker.getLocation());
        assertTrue(captures.isEmpty());
        assertEquals(ColonyBattleOutcome.Result.DEFENDER_EQUIPMENT_LOST,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        // The unarmed garrison unit and the workers are now civilians; a
        // second battle on a later turn faces the group, not nothing.
        attacker.setActionPoints(1);
        final ColonyDefense next = quote().getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.CIVILIANS, next.getKind());
        assertTrue(next.getRoster().contains(garrison));
        assertTrue(next.getRoster().contains(model.getUnitById(100)));
    }

    // -------------------------------------------------------------- Revere

    @Test
    void revereArmsOneWorkerOnlyWithoutAnArmedDefenderAndCostsFiftyMuskets()
            throws ReflectiveOperationException {
        build(civilians(1, 0).withRevere());
        final String before = snapshot();
        final AttackOrder order = quote();
        final ColonyDefense defense = order.getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.REVERE, defense.getKind());
        assertEquals(50, defense.getRevereMuskets());
        assertSame(model.getUnitById(4), defense.getDefender());
        // Preview and cancel are read-only.
        assertEquals(before, snapshot());
        assertEquals(50,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertFalse(((UnitFreeColonist) model.getUnitById(4)).isHoldingGuns());
        setState(stateFor(order, true));
        order.execute();
        // A confirmed battle consumed exactly the stored muskets; the
        // defeated militiaman keeps none, and the colony is captured anyway.
        assertEquals(0,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertFalse(((UnitFreeColonist) model.getUnitById(4)).isHoldingGuns());
        assertSame(dutch, quebec.getOwner());
        assertSame(dutch, model.getUnitById(4).getOwner());
        // The same confirmation cannot consume muskets or draw a second time.
        final String after = snapshot();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(after, snapshot());
    }

    @Test
    void revereNeedsFiftyMusketsAndNoExistingArmedDefender() {
        build(civilians(1, 0).withRevere());
        quebec.getWarehouse().removeGoods(Goods.of(GoodsType.MUSKET, 1));
        assertEquals(ColonyDefense.Kind.CIVILIANS,
                quote().getColonyDefense().orElseThrow().getKind());
        quebec.getWarehouse().addGoods(Goods.of(GoodsType.MUSKET, 1));
        assertEquals(ColonyDefense.Kind.REVERE,
                quote().getColonyDefense().orElseThrow().getKind());
        armedOnColonyTile();
        final ColonyDefense armed = quote().getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.ARMED, armed.getKind());
        assertEquals(0, armed.getRevereMuskets());
    }

    @Test
    void existingArmedDefenderNeverConsumesTheStoredMuskets()
            throws ReflectiveOperationException {
        build(civilians(1, 0).withRevere().withGarrison());
        fight(false);
        assertEquals(50,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertFalse(((UnitFreeColonist) model.getUnitById(4)).isHoldingGuns());
    }

    @Test
    void strongestEligibleWorkerBecomesTheEmergencyMilitiaThenLowestId() {
        build(civilians(2, 0).withRevere());
        // Equal workers: the stable identity decides.
        assertSame(model.getUnitById(4),
                quote().getColonyDefense().orElseThrow().getDefender());
        // A veteran worker is the stronger militiaman despite its identity.
        final ModelPo data = data(civilians(2, 0).withRevere());
        data.getUnits().stream().filter(unit -> unit.getId() == 101).findFirst()
                .orElseThrow().setVeteran(true);
        build(data);
        final ColonyDefense veteran = quote().getColonyDefense().orElseThrow();
        assertSame(model.getUnitById(101), veteran.getDefender());
        assertEquals(3.0, quote().getCombatStrengths().orElseThrow()
                .getDefender().getBase());
    }

    @Test
    void victoriousMilitiaKeepsMusketsAndJoinsTheGarrisonWhenAnotherResidentRemains()
            throws ReflectiveOperationException {
        final Spec two = civilians(1, 0).withRevere();
        two.liberty = 300L;
        build(two);
        final Unit militia = model.getUnitById(4);
        assertEquals(2, quebec.getPopulation());
        assertTrue(quote().getColonyDefense().orElseThrow().isRevereExit());
        fight(false);
        assertSame(france, quebec.getOwner());
        assertTrue(((UnitFreeColonist) militia).isHoldingGuns());
        assertTrue(militia.isAtPlaceLocation());
        assertEquals(COLONY, militia.getLocation());
        assertEquals(1, quebec.getPopulation());
        // The completed exit caps liberty to one resident exactly once.
        assertEquals(200L, quebec.getLiberty());
        assertTrue(attacks.get(0).getColonyOutcome().orElseThrow()
                .isMilitiaExited());
        assertFalse(attacker.isHoldingGuns());
        assertEquals(0,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
    }

    @Test
    void victoriousMilitiaStaysInsideAsTheLastResident()
            throws ReflectiveOperationException {
        final Spec one = spec().withRevere();
        one.liberty = 150L;
        build(one);
        final Unit militia = model.getUnitById(4);
        assertEquals(1, quebec.getPopulation());
        assertFalse(quote().getColonyDefense().orElseThrow().isRevereExit());
        fight(false);
        assertSame(france, quebec.getOwner());
        assertTrue(((UnitFreeColonist) militia).isHoldingGuns());
        assertTrue(militia.isAtPlaceColonyField());
        assertEquals(1, quebec.getPopulation());
        assertEquals(150L, quebec.getLiberty());
        assertFalse(attacks.get(0).getColonyOutcome().orElseThrow()
                .isMilitiaExited());
        // Save and Continue keep the armed last resident inside its job.
        final Colony restored = reloaded().getColonyById(0).orElseThrow();
        assertEquals(1, restored.getPopulation());
        assertTrue(((UnitFreeColonist) restored.getUnitsInsideColony().get(0))
                .isHoldingGuns());
    }

    // ------------------------------------------------- stale and blocked

    @Test
    void staleQuoteRejectsWithoutChangingAnythingNotEvenTheRandomState() {
        build(civilians(1, 0).withRevere());
        final AttackOrder revere = quote();
        quebec.getWarehouse().removeGoods(Goods.of(GoodsType.MUSKET, 1));
        final String changed = snapshot();
        assertThrows(IllegalStateException.class, revere::execute);
        assertEquals(changed, snapshot());
        quebec.getWarehouse().addGoods(Goods.of(GoodsType.MUSKET, 1));
        final AttackOrder roster = quote();
        armedOnColonyTile();
        final String armed = snapshot();
        assertThrows(IllegalStateException.class, roster::execute);
        assertEquals(armed, snapshot());
    }

    @Test
    void movedResidentMakesTheQuoteStale() {
        build(civilians(1, 1));
        final AttackOrder order = quote();
        // The defending colony's own worker leaves its job: the owner's
        // job permissions are untouched, only the roster changes.
        final Unit resident = model.getUnitById(101);
        resident.placeToLocation(COLONY);
        assertEquals(2, quebec.getPopulation());
        final String changed = snapshot();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(changed, snapshot());
    }

    private Unit ship(final int id, final String owner, final Location at) {
        final UnitPo data = new UnitPo();
        data.setId(id);
        data.setType(UnitType.GALLEON);
        data.setOwnerId(owner);
        data.setAction(new UnitActionNoAction().save());
        data.setAvailableMoves(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(Location.of(1, 3));
        place.setOrientation(Direction.east);
        data.setPlaceMap(place);
        final Unit created = model.createUnit(model.save(), data);
        created.placeToLocation(at);
        return created;
    }

    @Test
    void blockedShipEscapeRejectsTheBattleBeforeAnyDrawOrOwnerChange() {
        final Spec port = spec();
        port.coastal = true;
        build(port);
        ship(200, "France", COLONY);
        ship(201, "Dutch", WEST_WATER);
        ship(202, "Dutch", EAST_WATER);
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(COLONY));
        assertTrue(attacker.prepareAttack(COLONY, true).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(COLONY));
        assertEquals(before, snapshot());
    }

    @Test
    void freeEscapeMovesTheShipAndKeepsItsCargoWhenTheColonyFalls()
            throws ReflectiveOperationException {
        final Spec port = spec();
        port.coastal = true;
        build(port);
        final Unit vessel = ship(200, "France", COLONY);
        ((UnitWithCargo) vessel).getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 7));
        final String cargo = gson.toJson(vessel.save().getCargo());
        fight(true);
        assertSame(dutch, quebec.getOwner());
        assertSame(france, vessel.getOwner());
        assertTrue(
                List.of(WEST_WATER, EAST_WATER).contains(vessel.getLocation()));
        assertEquals(cargo, gson.toJson(vessel.save().getCargo()));
    }

    // ------------------------------------------------------ saved state

    @Test
    void malformedCapturedStateIsRejectedWithoutRepairWhenContinuing()
            throws ReflectiveOperationException {
        build(civilians(1, 1));
        fight(true);
        assertSame(dutch, quebec.getOwner());
        // A worker still owned by the former owner contradicts the colony.
        final ModelPo field = model.save();
        field.getUnits().stream().filter(unit -> unit.getId() == 100)
                .findFirst().orElseThrow().setOwnerId("France");
        assertThrows(IllegalArgumentException.class, () -> Model.make(field,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final ModelPo building = model.save();
        building.getUnits().stream().filter(unit -> unit.getId() == 101)
                .findFirst().orElseThrow().setOwnerId("France");
        assertThrows(IllegalArgumentException.class, () -> Model.make(building,
                MissionImpl.getPredefinedGameOverEvaluators()));
        // One worker cannot hold two jobs after the transfer either.
        final ModelPo twice = model.save();
        final ColonyPo colonyPo = twice.getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        colonyPo.getConstructions().stream()
                .filter(construction -> construction
                        .getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow().getSlots().get(1).setWorkerId(100);
        assertThrows(IllegalArgumentException.class, () -> Model.make(twice,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    // ------------------------------------------- peaceful declaration review

    @Test
    void peacefulColonyWithOnlyResidentsNeedsTheReviewAndConfirmationDeclaresWar()
            throws ReflectiveOperationException {
        peace = true;
        build(spec());
        final String before = snapshot();
        // Without the explicit declaration review there is no attack.
        assertFalse(attacker.isPossibleToAttackAt(COLONY));
        final AttackOrder order = attacker.prepareAttack(COLONY, true)
                .orElseThrow();
        assertTrue(order.isDeclarationRequired());
        assertEquals(ColonyDefense.Kind.CIVILIANS,
                order.getColonyDefense().orElseThrow().getKind());
        // Reviewing and cancelling declare nothing and draw nothing.
        assertEquals(before, snapshot());
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, france));
        setState(stateFor(order, true));
        order.execute();
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertSame(dutch, quebec.getOwner());
    }

    @Test
    void staleDeclarationReviewChangesNothing() {
        peace = true;
        build(spec());
        final AttackOrder order = attacker.prepareAttack(COLONY, true)
                .orElseThrow();
        model.declareHostilities(dutch, france);
        final String changed = snapshot();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(changed, snapshot());
        assertSame(france, quebec.getOwner());
    }

    // ----------------------------------------------- independent field worker

    @Test
    void independentAttackOnAFieldWorkerQuotesOnlyThatWorkerOnItsFieldTile() {
        build(civilians(1, 1));
        final String before = snapshot();
        assertTrue(model.getEnemyUnitsAt(dutch, FIELD).isEmpty());
        assertTrue(attacker.isPossibleToAttackAt(FIELD));
        final AttackOrder order = attacker.prepareAttack(FIELD, false)
                .orElseThrow();
        final ColonyDefense defense = order.getColonyDefense().orElseThrow();
        assertEquals(ColonyDefense.Kind.FIELD, defense.getKind());
        assertEquals(FIELD, defense.getBattlefield());
        assertEquals(FIELD, order.getBattlefield());
        assertSame(model.getUnitById(100), defense.getDefender());
        assertEquals(List.of(model.getUnitById(100)), defense.getRoster());
        assertFalse(defense.capturesColony());
        assertEquals(1.0, order.getCombatStrengths().orElseThrow().getDefender()
                .getBase());
        assertEquals(before, snapshot());
    }

    @Test
    void wonFieldBattleCapturesOnlyThatWorkerWithTheCompletedExitCap()
            throws ReflectiveOperationException {
        final Spec three = civilians(1, 1);
        three.liberty = 600L;
        build(three);
        final Unit worker = model.getUnitById(100);
        assertEquals(3, quebec.getPopulation());
        fightAt(FIELD, true);
        assertSame(dutch, worker.getOwner());
        assertTrue(worker.isAtPlaceLocation());
        assertEquals(ORIGIN, worker.getLocation());
        // The colony and its other jobs stay; the cap applied exactly once.
        assertSame(france, quebec.getOwner());
        assertEquals(2, quebec.getPopulation());
        quebec.getUnitsInsideColony()
                .forEach(resident -> assertSame(france, resident.getOwner()));
        assertEquals(400L, quebec.getLiberty());
        assertEquals(ORIGIN, attacker.getLocation());
        assertEquals(0, attacker.getActionPoints());
        assertEquals(1, attacks.size());
        assertEquals(ColonyBattleOutcome.Result.WORKER_CAPTURED,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        assertEquals(FIELD,
                attacks.get(0).getOriginalDefenderLocation().orElseThrow());
        assertTrue(captures.isEmpty());
        // Save and Continue keep the captured worker with its new owner.
        final Model restored = reloaded();
        assertSame(restored.getPlayerByName("Dutch"),
                restored.getUnitById(100).getOwner());
        assertEquals(ORIGIN, restored.getUnitById(100).getLocation());
        assertEquals(gson.toJson(model.save()), gson.toJson(restored.save()));
    }

    @Test
    void lostFieldBattleDisarmsTheAttackerAndKeepsTheWorker()
            throws ReflectiveOperationException {
        build(civilians(1, 1));
        fightAt(FIELD, false);
        assertFalse(attacker.isHoldingGuns());
        assertSame(france, model.getUnitById(100).getOwner());
        assertTrue(model.getUnitById(100).isAtPlaceColonyField());
        assertEquals(3, quebec.getPopulation());
    }

    @Test
    void capturingTheLastResidentRemovesTheColonyThroughTheSharedRules()
            throws ReflectiveOperationException {
        build(spec());
        attacker.placeToLocation(FIELD);
        attacker.getOwner().revealMapForUnit(attacker);
        final Unit worker = model.getUnitById(4);
        fightAt(WEST_FIELD, true);
        assertTrue(model.getColonyById(0).isEmpty());
        assertSame(dutch, worker.getOwner());
        assertEquals(FIELD, worker.getLocation());
        final Model restored = reloaded();
        assertTrue(restored.getColonyById(0).isEmpty());
        assertSame(restored.getPlayerByName("Dutch"),
                restored.getUnitById(4).getOwner());
    }

    @Test
    void armedFieldWorkerFightsWithItsOwnBaseAndOnlyLosesEquipment()
            throws ReflectiveOperationException {
        final ModelPo data = data(civilians(1, 1));
        data.getUnits().stream().filter(unit -> unit.getId() == 100).findFirst()
                .orElseThrow().setHoldingGuns(true);
        build(data);
        final AttackOrder order = attacker.prepareAttack(FIELD, false)
                .orElseThrow();
        assertEquals(2.0, order.getCombatStrengths().orElseThrow().getDefender()
                .getBase());
        setState(stateFor(order, true));
        order.execute();
        final UnitFreeColonist worker = (UnitFreeColonist) model
                .getUnitById(100);
        assertFalse(worker.isHoldingGuns());
        assertSame(france, worker.getOwner());
        assertTrue(worker.isAtPlaceColonyField());
        assertEquals(ColonyBattleOutcome.Result.DEFENDER_EQUIPMENT_LOST,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
    }

    @Test
    void peacefulFieldWorkerNeedsTheDeclarationReview() {
        peace = true;
        build(civilians(1, 1));
        assertFalse(attacker.isPossibleToAttackAt(FIELD));
        final AttackOrder order = attacker.prepareAttack(FIELD, true)
                .orElseThrow();
        assertTrue(order.isDeclarationRequired());
        assertEquals(ColonyDefense.Kind.FIELD,
                order.getColonyDefense().orElseThrow().getKind());
    }

    // -------------------------------------------------------- promotion

    private static long stateFor(final AttackOrder order,
            final boolean attackerWins, final boolean promoted) {
        return CombatDraws.stateFor(order.getCombatStrengths().orElseThrow()
                .getAttackerWinProbability(), attackerWins, promoted);
    }

    @Test
    void revereMilitiaWinnerIsPromotedByTheOrdinaryQuarterChance()
            throws ReflectiveOperationException {
        build(spec().withRevere());
        final AttackOrder order = quote();
        setState(stateFor(order, false, true));
        order.execute();
        final UnitFreeColonist militia = (UnitFreeColonist) model
                .getUnitById(4);
        assertTrue(militia.isVeteran());
        assertTrue(attacks.get(0).getPromotion().isPresent());
        // The last resident stays inside with its muskets and its new rank.
        assertTrue(militia.isAtPlaceColonyField());
        assertTrue(militia.isHoldingGuns());
    }

    @Test
    void revereMilitiaWinnerStaysUnpromotedWhenTheDrawDeclines()
            throws ReflectiveOperationException {
        build(spec().withRevere());
        final AttackOrder order = quote();
        setState(stateFor(order, false, false));
        order.execute();
        assertFalse(((UnitFreeColonist) model.getUnitById(4)).isVeteran());
        assertTrue(attacks.get(0).getPromotion().isEmpty());
    }

    @Test
    void washingtonPromotesTheMilitiaWinnerWithoutAnotherDraw()
            throws ReflectiveOperationException {
        build(spec().withRevere().withWashington());
        final AttackOrder order = quote();
        final long state = stateFor(order, false, false);
        setState(state);
        order.execute();
        assertTrue(((UnitFreeColonist) model.getUnitById(4)).isVeteran());
        assertTrue(attacks.get(0).getPromotion().isPresent());
        // Only the combat draw was consumed.
        assertEquals(state + 0x9E3779B97F4A7C15L,
                model.save().getCombatRandomState());
    }

    @Test
    void previewCancelAndLossGiveNoPromotion()
            throws ReflectiveOperationException {
        build(spec().withRevere().withWashington());
        final String before = snapshot();
        final AttackOrder order = quote();
        assertEquals(before, snapshot());
        // The defeated militia is not promoted, only a winner can be.
        setState(stateFor(order, true, true));
        order.execute();
        assertFalse(((UnitFreeColonist) model.getUnitById(4)).isVeteran());
    }

    // ------------------------------------------------ forced battle exit

    @Test
    void fortifiedColonyOfThreeLetsTheVictoriousMilitiaJoinTheGarrison()
            throws ReflectiveOperationException {
        final Spec fortified = civilians(1, 1).withRevere().withStockade();
        fortified.liberty = 600L;
        build(fortified);
        final Unit militia = model.getUnitById(4);
        assertEquals(3, quebec.getPopulation());
        // A voluntary exit is still refused by the fortification rule.
        assertFalse(militia.canLeaveColony());
        assertThrows(IllegalStateException.class,
                () -> militia.placeToLocation(COLONY));
        assertTrue(quote().getColonyDefense().orElseThrow().isRevereExit());
        fight(false);
        // The battle result is a forced exit: only the last resident stays.
        assertTrue(militia.isAtPlaceLocation());
        assertEquals(COLONY, militia.getLocation());
        assertTrue(((UnitFreeColonist) militia).isHoldingGuns());
        assertEquals(2, quebec.getPopulation());
        assertEquals(400L, quebec.getLiberty());
        assertTrue(quebec.wasConstructionBuilded(ConstructionType.STOCKADE));
        // The public guard keeps protecting the remaining residents.
        final Unit remaining = quebec.getUnitsInsideColony().get(0);
        assertFalse(remaining.canLeaveColony());
        assertThrows(IllegalStateException.class,
                () -> remaining.placeToLocation(COLONY));
        // Save and Continue keep the garrison soldier and the capped liberty.
        final Colony restored = reloaded().getColonyById(0).orElseThrow();
        assertEquals(2, restored.getPopulation());
        assertEquals(400L, restored.getLiberty());
        assertTrue(restored.wasConstructionBuilded(ConstructionType.STOCKADE));
    }

    // ------------------------------------------- Continental and royal roles

    private void declareIndependence(final ModelPo data, final String nation) {
        data.getGameManager().getPlayers().stream()
                .filter(player -> nation.equals(player.getName()))
                .forEach(player -> player.setDeclaredIndependence(true));
    }

    @Test
    void aDefeatedContinentalDefenderIsDestroyedWithItsEquipment()
            throws ReflectiveOperationException {
        final ModelPo data = data(civilians(1, 0).withGarrison());
        declareIndependence(data, "France");
        data.getUnits().stream().filter(unit -> unit.getId() == 6).findFirst()
                .orElseThrow().setMilitaryRole(MilitaryRole.CONTINENTAL);
        build(data);
        final UnitFreeColonist continental = (UnitFreeColonist) model
                .getUnitById(6);
        assertTrue(continental.isHoldingGuns());
        assertEquals(4.0, quote().getCombatStrengths().orElseThrow()
                .getDefender().getBase());
        final int people = model.getAllUnits().size();
        final int muskets = quebec.getWarehouse().getGoods(GoodsType.MUSKET)
                .getAmount();
        fight(true);
        // Neither a downgraded colonist nor dropped gear remains.
        assertTrue(model.getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == 6));
        assertEquals(people - 1, model.getAllUnits().size());
        assertEquals(muskets,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertSame(france, quebec.getOwner());
        assertTrue(captures.isEmpty());
        assertTrue(attacker.isHoldingGuns());
        assertEquals(1, attacks.size());
        assertTrue(attacks.get(0).wasDestroyed());
        assertSame(continental, attacks.get(0).getDestroyed());
        assertEquals(ColonyBattleOutcome.Result.DEFENDER_DESTROYED,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        assertTrue(reloaded().getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == 6));
    }

    @Test
    void aDefeatedContinentalAttackerIsDestroyedWithItsEquipment()
            throws ReflectiveOperationException {
        final ModelPo data = data(civilians(2, 1));
        declareIndependence(data, "Dutch");
        build(data);
        attacker.assignMilitaryRole(MilitaryRole.CONTINENTAL);
        assertEquals(4.0, quote().getCombatStrengths().orElseThrow()
                .getAttacker().getBase());
        final List<Unit> residents = new ArrayList<>(
                quebec.getUnitsInsideColony());
        final int people = model.getAllUnits().size();
        final int muskets = quebec.getWarehouse().getGoods(GoodsType.MUSKET)
                .getAmount();
        fight(false);
        assertTrue(model.getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == 0));
        assertEquals(people - 1, model.getAllUnits().size());
        assertEquals(muskets,
                quebec.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        assertSame(france, quebec.getOwner());
        assertEquals(residents, quebec.getUnitsInsideColony());
        assertEquals(1, attacks.size());
        assertTrue(attacks.get(0).wasDestroyed());
        assertSame(attacker, attacks.get(0).getDestroyed());
        assertEquals(ColonyBattleOutcome.Result.ATTACKER_DESTROYED,
                attacks.get(0).getColonyOutcome().orElseThrow().getResult());
        assertTrue(reloaded().getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == 0));
    }
}
