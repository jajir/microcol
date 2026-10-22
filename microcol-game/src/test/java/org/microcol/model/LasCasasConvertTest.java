package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventNativeConvert;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Las Casas's one-time join (R-0182) through the real owner turn. Every convert
 * the Dutch hold at the join becomes a free colonist once: its place in the
 * map, a field, a building, a ship's cargo keeps the same person, while its
 * restrictions, yield modifiers and lifetime count end. Other owners' converts,
 * ordinary colonists and converts acquired later are unchanged. A canceled or
 * underfunded join changes nothing, and Save and Continue never replay it.
 */
class LasCasasConvertTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH_NAME = "Dutch";

    private static final String KING_NAME = "Dutch's King";

    /**
     * The Crown's convert. Two owners may not share a square, so it stands
     * alone on land that no colony, claim or other saved unit of this map uses;
     * the same square holds a Dutch unit in NativeConvertColonyTest.
     */
    private static final int FOREIGN_ID = 100;

    private static final Location FOREIGN_AT = Location.of(19, 13);

    private static final int NEW_AMSTERDAM = 0;

    /** Passenger of the Dutch galleon in the saved map. */
    private static final int PASSENGER = 16;

    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
            Founder.BARTOLOME_DE_LAS_CASAS, Founder.FERDINAND_MAGELLAN);

    private static final List<Founder> LAS_CASAS = List
            .of(Founder.BARTOLOME_DE_LAS_CASAS);

    /**
     * The Dutch converts of every placement, one ordinary colonist and a
     * convert of another owner, all in one saved game.
     */
    private static final class Scene {
        private final Model model;
        private final UnitFreeColonist onMap;
        private final UnitFreeColonist inField;
        private final UnitFreeColonist inBuilding;
        private final UnitFreeColonist inCargo;
        private final UnitFreeColonist foreign;
        private final UnitFreeColonist ordinary;
        private final ColonyField field;
        private final ConstructionSlot slot;

        private Scene(final Model model, final UnitFreeColonist onMap,
                final UnitFreeColonist inField,
                final UnitFreeColonist inBuilding,
                final UnitFreeColonist inCargo, final UnitFreeColonist foreign,
                final UnitFreeColonist ordinary, final ColonyField field,
                final ConstructionSlot slot) {
            this.model = model;
            this.onMap = onMap;
            this.inField = inField;
            this.inBuilding = inBuilding;
            this.inCargo = inCargo;
            this.foreign = foreign;
            this.ordinary = ordinary;
            this.field = field;
            this.slot = slot;
        }
    }

    private static CongressPo congress(final Founder selected,
            final int bells) {
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setBells(bells);
        po.setMembers(List.of());
        po.setCandidates(OFFER);
        po.setSelected(selected);
        po.setGeneration(1);
        return po;
    }

    private static Model load(final Founder selected, final int bells,
            final Consumer<ModelPo> edit) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getName().equals(DUTCH_NAME)) {
                player.setCongress(congress(selected, bells));
            }
        }
        edit.accept(data.getModel());
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** The passenger is saved as a convert of the Aztecs, unassigned so far. */
    private static void asConvert(final ModelPo po, final int id) {
        final UnitPo unit = po.getUnits().stream()
                .filter(candidate -> candidate.getId() == id).findFirst()
                .orElseThrow();
        unit.setMissionary(false);
        unit.setNativeConvert(true);
        unit.setNativeConvertNation(NativeNation.AZTEC.name());
        unit.setNativeConvertUnassignedUpdates(0);
    }

    /**
     * A saved convert of the Crown on its own land square: a legal world, not a
     * unit moved into another owner's colony.
     */
    private static UnitPo foreignConvert() {
        final UnitPo unit = new UnitPo();
        unit.setId(FOREIGN_ID);
        unit.setOwnerId(KING_NAME);
        unit.setType(UnitType.COLONIST);
        unit.setMissionary(false);
        unit.setWorkUpdates(0);
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(FOREIGN_AT);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        unit.setNativeConvert(true);
        unit.setNativeConvertNation(NativeNation.AZTEC.name());
        unit.setNativeConvertUnassignedUpdates(0);
        return unit;
    }

    private static Scene scene(final Founder selected, final int bells) {
        final Model model = load(selected, bells, po -> {
            asConvert(po, PASSENGER);
            po.getUnits().add(foreignConvert());
        });
        assertTrue(model.getMap().getTerrainTypeAt(FOREIGN_AT).isLand());
        final Colony colony = model.getColonyById(NEW_AMSTERDAM).orElseThrow();
        final UnitFreeColonist ordinary = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        final UnitFreeColonist onMap = convertAt(model, colony);
        final UnitFreeColonist inField = convertAt(model, colony);
        inField.setActionPoints(inField.getSpeed());
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.isValid()
                        && candidate.canAccept(inField, GoodsType.CORN)
                        && candidate.getPotentialProduction(ordinary,
                                GoodsType.CORN, PoliticalProduction.NONE)
                                .getAmount() > 0)
                .findFirst().orElseThrow();
        inField.placeToColonyField(field, GoodsType.CORN);
        final UnitFreeColonist inBuilding = convertAt(model, colony);
        inBuilding.setActionPoints(inBuilding.getSpeed());
        final ConstructionSlot slot = colony
                .getConstructionByType(ConstructionType.TOWN_HALL)
                .getFirstEmptySlot().orElseThrow();
        inBuilding.placeToColonyStructureSlot(slot);
        final UnitFreeColonist foreign = (UnitFreeColonist) model
                .getUnitById(FOREIGN_ID);
        return new Scene(model, onMap, inField, inBuilding,
                (UnitFreeColonist) model.getUnitById(PASSENGER), foreign,
                ordinary, field, slot);
    }

    /**
     * A convert created the way a mission delivers one: at the colony square.
     */
    private static UnitFreeColonist convertAt(final Model model,
            final Colony colony) {
        final UnitFreeColonist convert = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        convert.becomeNativeConvert(NativeNation.AZTEC);
        return convert;
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH_NAME);
    }

    private static Set<Integer> unitIds(final Player owner) {
        return owner.getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    private static long freedNotices(final Model model) {
        return model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                .filter(event -> event instanceof TurnEventNativeConvert
                        && ((TurnEventNativeConvert) event)
                                .getKind() == TurnEventNativeConvert.Kind.FREED)
                .count();
    }

    private static String gson(final Object value) {
        return new Gson().toJson(value);
    }

    @Test
    void joinFreesEveryOwnedConvertOnceAndKeepsEachPlacementAndIdentity() {
        final Scene scene = scene(Founder.BARTOLOME_DE_LAS_CASAS, 1000);
        final Model model = scene.model;
        final Player dutch = dutch(model);
        final Set<Integer> units = unitIds(dutch);
        assertTrue(scene.onMap.isNativeConvert());
        assertTrue(scene.inField.isNativeConvert());
        assertTrue(scene.inBuilding.isNativeConvert());
        assertTrue(scene.inCargo.isNativeConvert());

        assertTrue(dutch.startTurn());

        assertEquals(LAS_CASAS, dutch.getCongressMembers());
        // No person is duplicated, lost or moved between owners.
        assertEquals(units, unitIds(dutch));
        assertFalse(scene.onMap.isNativeConvert());
        assertFalse(scene.inField.isNativeConvert());
        assertFalse(scene.inBuilding.isNativeConvert());
        assertFalse(scene.inCargo.isNativeConvert());
        assertTrue(scene.onMap.getConvertNation().isEmpty());
        assertTrue(scene.onMap.isAtPlaceLocation());
        assertSame(scene.inField, scene.field.getUnit().orElseThrow());
        assertTrue(scene.inBuilding.isAtPlaceConstructionSlot());
        assertFalse(scene.slot.isEmpty());
        assertSame(scene.inBuilding, scene.slot.getUnit());
        assertTrue(scene.inCargo.isAtCargoSlot());
        // Other owners and the ordinary colonist stay as they were.
        assertTrue(scene.foreign.isNativeConvert());
        assertSame(model.getPlayerByName(KING_NAME), scene.foreign.getOwner());
        assertTrue(scene.foreign.isAtPlaceLocation());
        assertEquals(FOREIGN_AT, scene.foreign.getLocation());
        assertEquals(0, scene.foreign.getConvertUnassignedUpdates());
        assertFalse(scene.ordinary.isNativeConvert());
        assertEquals(1, freedNotices(model));
    }

    @Test
    void theLifetimeCountEndsWithTheAffiliationAndTheSaveHoldsNoConvertFields() {
        final Scene scene = scene(Founder.BARTOLOME_DE_LAS_CASAS, 1000);
        final Model model = scene.model;
        final Player dutch = dutch(model);
        // Seven unassigned updates before the join; the eighth would expire it.
        for (int update = 0; update < 7; update++) {
            model.advanceNativeConverts(dutch);
        }
        assertEquals(7, scene.onMap.getConvertUnassignedUpdates());

        assertTrue(dutch.startTurn());

        assertTrue(scene.onMap.isAtPlaceLocation());
        for (int update = 0; update < 8; update++) {
            model.advanceNativeConverts(dutch);
        }
        assertTrue(model.tryGetUnitById(scene.onMap.getId()).isPresent());
        final UnitPo saved = model.save().getUnits().stream()
                .filter(unit -> unit.getId() == scene.onMap.getId()).findFirst()
                .orElseThrow();
        assertNull(saved.getNativeConvert());
        assertNull(saved.getNativeConvertNation());
        assertNull(saved.getNativeConvertUnassignedUpdates());
    }

    @Test
    void aFreedConvertYieldsAsAnOrdinaryColonistAndLosesItsWorkRestriction() {
        final Scene scene = scene(Founder.BARTOLOME_DE_LAS_CASAS, 1000);
        final Model model = scene.model;
        final Player dutch = dutch(model);
        final Colony colony = model.getColonyById(NEW_AMSTERDAM).orElseThrow();
        // The ordinary reference stands outside the colony and the converts
        // are residents, and the default preview counts a new resident, which
        // moves the support bonus. One explicit snapshot for every reading
        // leaves the convert rule as the only difference.
        PoliticalProduction political = colony.getPoliticalProduction();
        final int base = scene.field.getPotentialProduction(scene.ordinary,
                GoodsType.CORN, political).getAmount();
        final int ordinaryBell = scene.slot
                .getPotentialProduction(scene.ordinary, political).getAmount();
        assertTrue(ordinaryBell > 0);
        // Before the join: outdoor +1 and indoor -1 apply to the convert, in
        // the preview and in the live output of the resident worker.
        assertEquals(base + NativeConversion.OUTDOOR_BONUS,
                scene.field.getPotentialProduction(scene.inField,
                        GoodsType.CORN, political).getAmount());
        assertEquals(base + NativeConversion.OUTDOOR_BONUS,
                scene.field.getProduction().orElseThrow().getAmount());
        assertEquals(ordinaryBell - NativeConversion.INDOOR_PENALTY,
                scene.slot.getPotentialProduction(scene.inBuilding, political)
                        .getAmount());
        assertFalse(ConstructionSlot.isWorkAllowed(scene.inBuilding,
                ConstructionType.LUMBER_MILL));

        assertTrue(dutch.startTurn());

        // The update credited its own bells, so read the snapshot again; both
        // workers are measured against it.
        political = colony.getPoliticalProduction();
        final int ordinaryField = scene.field.getPotentialProduction(
                scene.ordinary, GoodsType.CORN, political).getAmount();
        assertSame(scene.inField, scene.field.getUnit().orElseThrow());
        assertEquals(ordinaryField,
                scene.field.getPotentialProduction(scene.inField,
                        GoodsType.CORN, political).getAmount());
        assertEquals(ordinaryField,
                scene.field.getProduction().orElseThrow().getAmount());
        assertSame(scene.inBuilding, scene.slot.getUnit());
        assertEquals(
                scene.slot.getPotentialProduction(scene.ordinary, political)
                        .getAmount(),
                scene.slot.getPotentialProduction(scene.inBuilding, political)
                        .getAmount());
        assertTrue(ConstructionSlot.isWorkAllowed(scene.inBuilding,
                ConstructionType.LUMBER_MILL));
    }

    @Test
    void aConvertAcquiredAfterTheJoinStaysAConvertAndCountsOnItsOwn() {
        final Scene scene = scene(Founder.BARTOLOME_DE_LAS_CASAS, 1000);
        final Model model = scene.model;
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());

        final UnitFreeColonist future = convertAt(model,
                model.getColonyById(NEW_AMSTERDAM).orElseThrow());
        model.advanceNativeConverts(dutch);

        assertTrue(future.isNativeConvert());
        assertEquals(1, future.getConvertUnassignedUpdates());
        assertFalse(scene.onMap.isNativeConvert());
        assertEquals(1, freedNotices(model));
    }

    @Test
    void anUnearnedOrUnderfundedJoinFreesNobodyAndAddsNoNotice() {
        for (final Scene scene : List.of(scene(null, 1000),
                scene(Founder.BARTOLOME_DE_LAS_CASAS, 0))) {
            final Model model = scene.model;
            final Player dutch = dutch(model);
            assertTrue(dutch.startTurn());

            assertFalse(dutch.getCongressMembers()
                    .contains(Founder.BARTOLOME_DE_LAS_CASAS));
            assertTrue(scene.onMap.isNativeConvert());
            assertTrue(scene.inField.isNativeConvert());
            assertTrue(scene.inBuilding.isNativeConvert());
            assertTrue(scene.inCargo.isNativeConvert());
            assertTrue(scene.foreign.isNativeConvert());
            assertEquals(0, freedNotices(model));
        }
    }

    @Test
    void saveAndContinueKeepTheFreedPersonWithoutReplayingTheJoin() {
        final Scene scene = scene(Founder.BARTOLOME_DE_LAS_CASAS, 1000);
        final Model model = scene.model;
        assertTrue(dutch(model).startTurn());
        final String saved = gson(model.save());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertEquals(saved, gson(restored.save()));
        assertEquals(LAS_CASAS, dutch(restored).getCongressMembers());
        assertFalse(
                ((UnitFreeColonist) restored.getUnitById(scene.onMap.getId()))
                        .isNativeConvert());
        assertEquals(1, freedNotices(restored));
        // The Crown's convert was saved and loaded as it stood.
        final UnitFreeColonist foreign = (UnitFreeColonist) restored
                .getUnitById(FOREIGN_ID);
        assertTrue(foreign.isNativeConvert());
        assertSame(restored.getPlayerByName(KING_NAME), foreign.getOwner());
        assertEquals(FOREIGN_AT, foreign.getLocation());
    }
}
