package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventNativeConvert;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * The native convert on the real colony map (R-0149): the exact conversion
 * thresholds read from the controlled saved randomness, live founder membership
 * at the event, the nearest legal land route with its stable identity tie, the
 * sea and the foreign settlement that block a route, the outdoor and indoor
 * output modifiers through the production owner, the atomic manufacturing
 * refusal, expiry in cargo and in transit, and a full run of real owner turns
 * that acquires, pauses and resumes across Continue. Squares are one-based like
 * the map file.
 */
class NativeConvertColonyTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH_NAME = "Dutch";

    /** Two steps from both New Amsterdam (id 0) and Delft (id 1). */
    private static final Location TIE = Location.of(24, 12);

    /** Land beside the tie square, for a missionary who really establishes. */
    private static final Location TIE_MISSIONARY = Location.of(23, 12);

    /** The only land corridor from the tie square to New Amsterdam. */
    private static final Location CORRIDOR = Location.of(25, 13);

    /**
     * Two steps from Haarlem (id 2) and three from New Amsterdam (id 0) and
     * Delft (id 1): the shorter route must beat the lower identity.
     */
    private static final Location SHORT_ROUTE = Location.of(22, 15);

    /** Land far from every colony and every claim, west of Haarlem. */
    private static final Location REMOTE = Location.of(14, 22);

    private static final Location REMOTE_MISSIONARY = Location.of(15, 22);

    /** Land away from the settlements for missionaries who never move. */
    private static final Location FAR_LAND = Location.of(19, 13);

    private static final int NEW_AMSTERDAM = 0;

    private static final int DELFT = 1;

    private static final int HAARLEM = 2;

    /** The Dutch galleon in transit and a passenger of its cargo. */
    private static final int GALLEON = 3;

    private static final int PASSENGER = 16;

    private static final long DRAW_STEP = 0x9E3779B97F4A7C15L;

    private static final GameModelPoDao DAO = new GameModelPoDao();

    private static final List<Function<Model, GameOverResult>> EVALUATORS = List
            .of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR);

    private static final List<Founder> NO_FOUNDERS = List.of();

    private static final List<Founder> SEPULVEDA = List
            .of(Founder.JUAN_DE_SEPULVEDA);

    private static final List<Founder> BREBEUF = List
            .of(Founder.JEAN_DE_BREBEUF);

    private static final List<Founder> BOTH = List.of(Founder.JEAN_DE_BREBEUF,
            Founder.JUAN_DE_SEPULVEDA);

    private static final Consumer<ModelPo> NO_EDIT = po -> {
    };

    private static Model build(final long seed, final Location site,
            final List<Founder> founders, final boolean jesuit,
            final Location missionaryAt, final Consumer<ModelPo> edit) {
        final ModelPo po = DAO.loadFromClassPath(MAP).getModel();
        po.setCombatRandomState(seed);
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH_NAME);
        terrain(po, TerrainType.GRASSLAND, List.of(site));
        po.setNativeSettlements(new ArrayList<>(
                List.of(settlement(0, NativeNation.AZTEC, site))));
        po.setNativeSettlementKnowledge(new ArrayList<>());
        po.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        po.setNativeLandRights(new ArrayList<>());
        po.setNativeLandTakings(new ArrayList<>());
        final int id = po.getUnits().stream().mapToInt(UnitPo::getId).max()
                .orElse(0) + 1;
        po.getUnits().add(missionary(id, jesuit, missionaryAt));
        if (!founders.isEmpty()) {
            seatFounders(po, founders);
        }
        edit.accept(po);
        po.setNativeLocalAlarm(
                NativeRelations.freshLocalAlarm(po.getNativeSettlements()));
        return Model.make(po, EVALUATORS);
    }

    private static Model build(final long seed, final Location site,
            final List<Founder> founders, final boolean jesuit) {
        return build(seed, site, founders, jesuit, FAR_LAND, NO_EDIT);
    }

    /** Change the terrain of squares, dropping their trees and plowed marks. */
    private static void terrain(final ModelPo po, final TerrainType type,
            final Collection<Location> squares) {
        final Map<Location, TerrainType> types = po.getMap().getTerrainMap();
        final Set<Location> trees = po.getMap().getTreeSet();
        final Set<Location> fields = po.getMap().getFieldSet();
        for (final Location at : squares) {
            types.put(at, type);
            trees.remove(at);
            fields.remove(at);
        }
        po.getMap().setTerrainType(types);
        po.getMap().setTrees(trees);
        po.getMap().setFields(fields);
    }

    private static NativeSettlementPo settlement(final int id,
            final NativeNation nation, final Location site) {
        final NativeSettlementPo settlement = new NativeSettlementPo();
        settlement.setId(id);
        settlement.setNation(nation);
        settlement.setSettlementClass(nation.getSettlementClass());
        settlement.setCapital(true);
        settlement.setTreasury(2000);
        settlement.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        settlement.setOfferedGoods(List.of());
        settlement.setWantedGoods(List.of());
        settlement.setTrade(List.of());
        settlement.setLocation(site);
        return settlement;
    }

    private static UnitPo missionary(final int id, final boolean jesuit,
            final Location at) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setOwnerId(DUTCH_NAME);
        unit.setType(jesuit ? UnitType.JESUIT_MISSIONARY : UnitType.COLONIST);
        unit.setMissionary(!jesuit);
        unit.setWorkUpdates(0);
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        return unit;
    }

    /** The Dutch Congress with exactly these members. */
    private static void seatFounders(final ModelPo po,
            final List<Founder> founders) {
        final CongressPo congress = po.getGameManager().getPlayers().stream()
                .filter(player -> DUTCH_NAME.equals(player.getName()))
                .findFirst().orElseThrow().getCongress();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(founders);
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(null);
        congress.setGeneration(founders.size() + 1);
        congress.setJonesDeliveryPending(false);
    }

    private static void asConvert(final UnitPo unit, final int updates) {
        unit.setMissionary(false);
        unit.setNativeConvert(true);
        unit.setNativeConvertNation(NativeNation.AZTEC.name());
        unit.setNativeConvertUnassignedUpdates(updates);
    }

    /** The smallest seed whose first draw lies in [low, high). */
    private static long seedDrawing(final double low, final double high) {
        for (long seed = 1; seed < 5_000_000; seed++) {
            final double draw = Model.combatRandomAfter(seed);
            if (draw >= low && draw < high) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed draws in the band");
    }

    /** The smallest seed whose next draws all fall below the chance. */
    private static long seedWithSuccesses(final double chance,
            final int count) {
        for (long seed = 1;; seed++) {
            boolean all = true;
            for (int draw = 0; draw < count && all; draw++) {
                all = Model.combatRandomAfter(seed + draw * DRAW_STEP) < chance;
            }
            if (all) {
                return seed;
            }
        }
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH_NAME);
    }

    private static List<UnitFreeColonist> convertsOf(final Model model) {
        final List<UnitFreeColonist> converts = new ArrayList<>();
        for (final Unit unit : dutch(model).getAllUnits()) {
            if (unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isNativeConvert()) {
                converts.add((UnitFreeColonist) unit);
            }
        }
        return converts;
    }

    private static Set<Integer> unitIds(final Model model) {
        return dutch(model).getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    private static int missionaryOf(final Model model) {
        return dutch(model).getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && ((UnitFreeColonist) unit).isMissionary())
                .filter(unit -> unit.isAtPlaceLocation()
                        && unit.getId() > PASSENGER)
                .findFirst().orElseThrow().getId();
    }

    /** One mission update with its conversion event, as the owner turn runs. */
    private static void missionUpdate(final Model model) {
        model.resolveNativeConversions(dutch(model),
                model.advanceNativeMissions(dutch(model)));
    }

    private static void establishReal(final Model model) {
        final Unit missionary = model.getUnitById(missionaryOf(model));
        final NativeSettlement site = model.getNativeSettlements().get(0);
        model.establishNativeMission(missionary, site,
                model.getNativeMissionPreview(missionary, site).orElseThrow());
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(), EVALUATORS);
    }

    /** Continue after the given founders were seated in the saved Congress. */
    private static Model reloadWith(final Model model,
            final List<Founder> founders) {
        final ModelPo saved = model.save();
        seatFounders(saved, founders);
        return Model.make(saved, EVALUATORS);
    }

    /** Ends European turns until the owner's own next turn is prepared. */
    private static void ownerRound(final Model model) {
        int rounds = 0;
        do {
            model.endTurn();
            assertTrue(++rounds <= 10, "The owner's next turn never came");
        } while (model.getCurrentPlayer() != dutch(model));
    }

    /**
     * Play the mission, which is held directly, to its fifth nonhostile update
     * and report whether a convert was created.
     */
    private static boolean convertsAfterFifth(final long seed,
            final List<Founder> founders, final boolean jesuit) {
        final Model model = build(seed, TIE, founders, jesuit);
        model.getNativeSettlements().get(0)
                .establishMission(missionaryOf(model));
        for (int update = 0; update < 5; update++) {
            missionUpdate(model);
        }
        return !convertsOf(model).isEmpty();
    }

    private static void assertConversion(final double low, final double high,
            final List<Founder> founders, final boolean jesuit,
            final boolean expected) {
        final long seed = seedDrawing(low, high);
        assertEquals(expected, convertsAfterFifth(seed, founders, jesuit),
                "draw in [" + low + ", " + high + ") founders=" + founders
                        + " jesuit=" + jesuit);
    }

    /**
     * Exact ordinary 10%, expert 20%, Sepulveda 20% and 40% edges, and the
     * expertise applying once even when a Jesuit also has Brebeuf.
     */
    @Test
    void theConversionChanceHasTheExactTenTwentyTwentyAndFortyPercentEdges() {
        assertConversion(0.099, 0.100, NO_FOUNDERS, false, true);
        assertConversion(0.100, 0.101, NO_FOUNDERS, false, false);

        assertConversion(0.199, 0.200, NO_FOUNDERS, true, true);
        assertConversion(0.200, 0.201, NO_FOUNDERS, true, false);

        assertConversion(0.199, 0.200, BREBEUF, false, true);
        assertConversion(0.200, 0.201, BREBEUF, false, false);

        assertConversion(0.199, 0.200, SEPULVEDA, false, true);
        assertConversion(0.200, 0.201, SEPULVEDA, false, false);

        assertConversion(0.399, 0.400, SEPULVEDA, true, true);
        assertConversion(0.400, 0.401, SEPULVEDA, true, false);

        assertConversion(0.399, 0.400, BOTH, false, true);
        assertConversion(0.400, 0.401, BOTH, false, false);

        // Brebeuf on a Jesuit is not a second multiplier.
        assertConversion(0.199, 0.200, BREBEUF, true, true);
        assertConversion(0.200, 0.201, BREBEUF, true, false);
    }

    /**
     * The founder is read live when the fifth event happens, not when the
     * mission was established: the same position fails without a change and
     * succeeds after Brebeuf or Sepulveda joined, even across Continue.
     */
    @Test
    void founderMembershipIsReadAtTheEventAndSurvivesContinue() {
        final long seed = seedDrawing(0.15, 0.16);

        final Model unchanged = establishedAfterFourUpdates(seed);
        missionUpdate(unchanged);
        assertTrue(convertsOf(unchanged).isEmpty());

        final Model withBrebeuf = reloadWith(establishedAfterFourUpdates(seed),
                BREBEUF);
        missionUpdate(withBrebeuf);
        assertEquals(1, convertsOf(withBrebeuf).size());

        final Model withSepulveda = reloadWith(
                establishedAfterFourUpdates(seed), SEPULVEDA);
        missionUpdate(withSepulveda);
        assertEquals(1, convertsOf(withSepulveda).size());
    }

    private static Model establishedAfterFourUpdates(final long seed) {
        final Model model = build(seed, TIE, NO_FOUNDERS, false, TIE_MISSIONARY,
                NO_EDIT);
        establishReal(model);
        for (int update = 0; update < 4; update++) {
            missionUpdate(model);
        }
        assertEquals(4,
                model.getNativeSettlements().get(0).getMissionUpdates());
        return reload(model);
    }

    /** A distance tie goes to the lower stable colony identity. */
    @Test
    void aConvertGoesToTheLowerColonyIdentityOnADistanceTie() {
        final Model model = build(seedDrawing(0.0, 0.10), TIE, NO_FOUNDERS,
                true);
        model.getNativeSettlements().get(0)
                .establishMission(missionaryOf(model));
        for (int update = 0; update < 5; update++) {
            missionUpdate(model);
        }
        final List<UnitFreeColonist> converts = convertsOf(model);
        assertEquals(1, converts.size());
        assertEquals(
                model.getColonyById(NEW_AMSTERDAM).orElseThrow().getLocation(),
                converts.get(0).getLocation());
        assertNotEquals(model.getColonyById(DELFT).orElseThrow().getLocation(),
                converts.get(0).getLocation());
    }

    /**
     * The actual shortest land route wins over the lower identity: Haarlem has
     * the highest identity but is two steps away while the others are three.
     */
    @Test
    void theShortestLegalRouteBeatsTheLowerColonyIdentity() {
        final Model model = build(seedDrawing(0.0, 0.10), SHORT_ROUTE,
                NO_FOUNDERS, true);
        model.getNativeSettlements().get(0)
                .establishMission(missionaryOf(model));
        for (int update = 0; update < 5; update++) {
            missionUpdate(model);
        }
        final List<UnitFreeColonist> converts = convertsOf(model);
        assertEquals(1, converts.size());
        assertEquals(model.getColonyById(HAARLEM).orElseThrow().getLocation(),
                converts.get(0).getLocation());
    }

    /**
     * A settlement walled off by sea reaches no colony by land although
     * colonies are two squares away over the water. The fifth event draws once
     * and nobody is created or banked.
     */
    @Test
    void anIsolatedSettlementNeverConvertsAndDrawsOnlyOnce() {
        final long seed = seedDrawing(0.0, 0.10);
        final Model model = build(seed, TIE, NO_FOUNDERS, true, FAR_LAND,
                po -> terrain(po, TerrainType.OCEAN, TIE.getNeighbors()));
        assertNoConvertAfterFifth(model, seed);
    }

    /**
     * The only land corridor holds another native settlement, which a colonist
     * may not cross, so no legal route exists although the geometry is open. A
     * terrain-only search would have walked through it.
     */
    @Test
    void aForeignSettlementOnTheOnlyCorridorBlocksTheRoute() {
        final long seed = seedDrawing(0.0, 0.10);
        final Model model = build(seed, TIE, NO_FOUNDERS, true, FAR_LAND,
                po -> {
                    final List<Location> sea = new ArrayList<>(
                            TIE.getNeighbors());
                    sea.remove(CORRIDOR);
                    terrain(po, TerrainType.OCEAN, sea);
                    terrain(po, TerrainType.GRASSLAND, List.of(CORRIDOR));
                    po.getNativeSettlements().add(
                            settlement(1, NativeNation.CHEROKEE, CORRIDOR));
                });
        assertNoConvertAfterFifth(model, seed);
    }

    private static void assertNoConvertAfterFifth(final Model model,
            final long seed) {
        model.getNativeSettlements().get(0)
                .establishMission(missionaryOf(model));
        final Set<Integer> baseline = unitIds(model);
        for (int update = 0; update < 5; update++) {
            missionUpdate(model);
        }
        assertTrue(convertsOf(model).isEmpty());
        assertEquals(baseline, unitIds(model));
        assertEquals(seed + DRAW_STEP, model.save().getCombatRandomState());
        assertEquals(0,
                model.getNativeSettlements().get(0).getMissionUpdates());
        // Nothing is banked: the next cycle does not draw before its fifth.
        for (int update = 0; update < 4; update++) {
            missionUpdate(model);
        }
        assertEquals(seed + DRAW_STEP, model.save().getCombatRandomState());
    }

    /**
     * Production goes through the field and building owners: an outdoor square
     * gains one over an ordinary colonist, a square with no yield for the goods
     * stays at zero, and a town hall loses one.
     */
    @Test
    void outdoorGainsOneZeroBaseStaysZeroAndIndoorLosesOne() {
        final Model model = build(1, TIE, NO_FOUNDERS, false);
        final Colony colony = model.getColonyById(NEW_AMSTERDAM).orElseThrow();
        final UnitFreeColonist ordinary = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        final UnitFreeColonist convert = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        convert.becomeNativeConvert(NativeNation.AZTEC);

        boolean checkedField = false;
        boolean checkedZero = false;
        for (final ColonyField field : colony.getColonyFields()) {
            if (!field.isValid()) {
                continue;
            }
            for (final GoodsType goods : GoodsType.GOOD_TYPES) {
                final int base = field.getPotentialProduction(ordinary, goods)
                        .getAmount();
                final int converted = field
                        .getPotentialProduction(convert, goods).getAmount();
                if (base > 0) {
                    assertEquals(base + NativeConversion.OUTDOOR_BONUS,
                            converted);
                    checkedField = true;
                } else {
                    assertEquals(0, converted);
                    checkedZero = true;
                }
            }
        }
        assertTrue(checkedField, "The colony has no producing field");
        assertTrue(checkedZero, "The colony has no square without a yield");

        final ConstructionSlot townHall = colony.getConstructions().stream()
                .filter(construction -> construction
                        .getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow().getConstructionSlots().get(0);
        final int ordinaryBell = townHall.getPotentialProduction(ordinary)
                .getAmount();
        assertTrue(ordinaryBell > 0);
        assertEquals(ordinaryBell - NativeConversion.INDOOR_PENALTY,
                townHall.getPotentialProduction(convert).getAmount());
    }

    /**
     * A manufacturing building refuses a convert through the colony command and
     * through the job choice, without any change, while an ordinary colonist is
     * accepted by the same slot.
     */
    @Test
    void manufacturingIsRefusedAtomicallyThroughTheColonyCommands() {
        final Model model = build(1, TIE, NO_FOUNDERS, false);
        final Colony colony = model.getColonyById(NEW_AMSTERDAM).orElseThrow();
        final UnitFreeColonist ordinary = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        final UnitFreeColonist convert = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        convert.becomeNativeConvert(NativeNation.AZTEC);
        final ConstructionSlot mill = colony
                .getConstructionByType(ConstructionType.LUMBER_MILL)
                .getConstructionSlots().stream()
                .filter(ConstructionSlot::isEmpty).findFirst().orElseThrow();
        assertTrue(mill.canAccept(ordinary));
        assertFalse(mill.canAccept(convert));

        final Gson gson = new Gson();
        final var before = gson.toJsonTree(model.save());
        assertThrows(IllegalStateException.class,
                () -> convert.placeToColonyStructureSlot(mill));
        assertEquals(before, gson.toJsonTree(model.save()));
        assertTrue(convert.isAtPlaceLocation());

        final ColonyJobChoice choice = colony.getJobChoices(convert).stream()
                .filter(candidate -> candidate
                        .getKind() == ColonyJobChoice.Kind.INDOOR
                        && candidate.getConstructionType()
                                .orElse(null) == ConstructionType.LUMBER_MILL)
                .findFirst().orElseThrow();
        assertEquals(
                Optional.of(ColonyJobChoice.UnavailableReason.WORK_FORBIDDEN),
                choice.getUnavailableReason());
        assertThrows(IllegalStateException.class, choice::execute);
        assertEquals(before, gson.toJsonTree(model.save()));
    }

    /**
     * A convert in the cargo of a ship in transit still counts every owner
     * update, keeps its place and count through Continue, and at the eighth
     * update leaves the ship's cargo slot empty with no dangling passenger.
     */
    @Test
    void aConvertInCargoAndTransitCountsAndLeavesNoDanglingPassenger() {
        final Model model = build(1, TIE, NO_FOUNDERS, false, FAR_LAND,
                po -> asConvert(po.getUnits().stream()
                        .filter(unit -> unit.getId() == PASSENGER).findFirst()
                        .orElseThrow(), 5));
        assertTrue(model.getUnitById(PASSENGER).isAtCargoSlot());
        assertTrue(model.getUnitById(GALLEON).isAtHighSea());

        final Model restored = reload(model);
        UnitFreeColonist convert = (UnitFreeColonist) restored
                .getUnitById(PASSENGER);
        assertTrue(convert.isAtCargoSlot());
        assertEquals(5, convert.getConvertUnassignedUpdates());
        restored.advanceNativeConverts(dutch(restored));
        restored.advanceNativeConverts(dutch(restored));
        convert = (UnitFreeColonist) restored.getUnitById(PASSENGER);
        assertEquals(7, convert.getConvertUnassignedUpdates());
        assertTrue(convert.isAtCargoSlot());

        restored.advanceNativeConverts(dutch(restored));

        assertTrue(restored.tryGetUnitById(PASSENGER).isEmpty());
        final UnitWithCargo galleon = (UnitWithCargo) restored
                .getUnitById(GALLEON);
        assertTrue(galleon.getCargo().getSlots().stream()
                .noneMatch(slot -> slot.getUnit()
                        .map(unit -> unit.getId() == PASSENGER).orElse(false)));
        assertEquals(3, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).count());
        // The saved game has no reference to the removed person.
        assertTrue(reload(restored).tryGetUnitById(PASSENGER).isEmpty());
    }

    /**
     * Real owner turns: the fifth update of a mission acquires one convert that
     * starts at zero, the report is stored, Continue keeps the person, work
     * pauses the count without resetting it, and leaving the job resumes it
     * across Continue.
     */
    @Test
    void realOwnerTurnsAcquirePauseAndResumeAcrossContinue() {
        // Other events may take draws first; several successes in a row keep
        // the conversion deterministic whichever of them comes first.
        final long seed = seedWithSuccesses(0.10, 6);
        Model model = build(seed, REMOTE, NO_FOUNDERS, false, REMOTE_MISSIONARY,
                NO_EDIT);
        establishReal(model);
        for (int round = 0; round < 4; round++) {
            ownerRound(model);
        }
        final NativeSettlement site = model.getNativeSettlements().get(0);
        assertEquals(4, site.getMissionUpdates());
        assertTrue(convertsOf(model).isEmpty());

        // The fifth owner update acquires the convert after the unit phase.
        ownerRound(model);
        assertEquals(0, site.getMissionUpdates());
        assertEquals(1, convertsOf(model).size());
        UnitFreeColonist convert = convertsOf(model).get(0);
        final int id = convert.getId();
        assertEquals(0, convert.getConvertUnassignedUpdates());
        assertEquals(Optional.of(NativeNation.AZTEC),
                convert.getConvertNation());
        assertEquals(model.getColonyById(HAARLEM).orElseThrow().getLocation(),
                convert.getLocation());
        assertTrue(model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                .anyMatch(event -> event instanceof TurnEventNativeConvert
                        && ((TurnEventNativeConvert) event)
                                .getKind() == TurnEventNativeConvert.Kind.ACQUIRED));

        // Continue keeps the person and its affiliation; an unassigned owner
        // update then counts one.
        model = reload(model);
        convert = (UnitFreeColonist) model.getUnitById(id);
        assertEquals(Optional.of(NativeNation.AZTEC),
                convert.getConvertNation());
        assertEquals(0, convert.getConvertUnassignedUpdates());
        ownerRound(model);
        assertEquals(1, ((UnitFreeColonist) model.getUnitById(id))
                .getConvertUnassignedUpdates());

        // Work pauses the count without resetting it, across Continue too.
        final Colony haarlem = model.getColonyById(HAARLEM).orElseThrow();
        convert = (UnitFreeColonist) model.getUnitById(id);
        final Unit worker = convert;
        final ColonyField field = haarlem.getColonyFields().stream().filter(
                candidate -> candidate.canAccept(worker, GoodsType.CORN))
                .findFirst().orElseThrow();
        convert.setActionPoints(convert.getSpeed());
        convert.placeToColonyField(field, GoodsType.CORN);
        ownerRound(model);
        model = reload(model);
        ownerRound(model);
        convert = (UnitFreeColonist) model.getUnitById(id);
        assertTrue(convert.isAtPlaceColonyField());
        assertEquals(1, convert.getConvertUnassignedUpdates());

        // Leaving the job resumes the same count.
        convert.placeToLocation(haarlem.getLocation());
        ownerRound(model);
        assertEquals(2, ((UnitFreeColonist) model.getUnitById(id))
                .getConvertUnassignedUpdates());
    }
}
