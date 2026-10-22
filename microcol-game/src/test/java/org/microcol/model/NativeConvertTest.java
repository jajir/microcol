package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * The native convert (R-0149): the controlled conversion chance, the single
 * saved draw of each fifth nonhostile update, the no-colony skip without
 * banking, the saved identity and unassigned count, the eighth-update return
 * with its cleanup, the founding and manufacturing refusals, education that
 * keeps the identity, and rejected malformed saves. Colony-level cases (nearest
 * land route, production, assignment) are in NativeConvertColonyTest.
 */
class NativeConvertTest {

    private static final int MISSIONARY_ID = 200;

    private static final int CONVERT_ID = 200;

    private static final int SECOND_ID = 201;

    /** The saved combat randomness advances by this step on every draw. */
    private static final long DRAW_STEP = 0x9E3779B97F4A7C15L;

    private static final Consumer<UnitPo> ORDINARY = unit -> unit
            .setMissionary(true);

    private static final Consumer<UnitPo> PLAIN = unit -> unit
            .setMissionary(false);

    /** A saved convert of the Aztecs, unassigned for the given updates. */
    private static Consumer<UnitPo> convert(final int updates) {
        return unit -> {
            unit.setMissionary(false);
            unit.setNativeConvert(true);
            unit.setNativeConvertNation(NativeNation.AZTEC.name());
            unit.setNativeConvertUnassignedUpdates(updates);
        };
    }

    private static Model missionScene() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), ORDINARY).toModel();
    }

    private static Model convertScene(final int updates) {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), convert(updates))
                .toModel();
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static void establish(final Model model) {
        final Unit missionary = model.getUnitById(MISSIONARY_ID);
        model.establishNativeMission(missionary, settlement(model),
                model.getNativeMissionPreview(missionary, settlement(model))
                        .orElseThrow());
    }

    /** One owner update of the mission, resolving any fifth-update event. */
    private static void missionUpdate(final Model model, final Player owner) {
        model.resolveNativeConversions(owner,
                model.advanceNativeMissions(owner));
    }

    private static UnitFreeColonist convertOf(final Model model) {
        return (UnitFreeColonist) model.getUnitById(CONVERT_ID);
    }

    /**
     * Identities of every unit the owner holds, for exact before/after sets.
     */
    private static Set<Integer> unitIds(final Player owner) {
        return owner.getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    @Test
    void theConversionChanceIsTenOrTwentyPercentDoubledBySepulveda() {
        assertEquals(0.10, NativeConversion.chance(false, false), 1e-12);
        assertEquals(0.20, NativeConversion.chance(true, false), 1e-12);
        assertEquals(0.20, NativeConversion.chance(false, true), 1e-12);
        assertEquals(0.40, NativeConversion.chance(true, true), 1e-12);
    }

    @Test
    void onlyTheFifthNonhostileUpdateDrawsTheSavedRandomnessOnce() {
        final Model model = missionScene();
        establish(model);
        final Player dutch = model.getPlayerByName(DUTCH);
        final long before = randomState(model);
        for (int update = 0; update < 4; update++) {
            missionUpdate(model, dutch);
            assertEquals(before, randomState(model));
        }
        missionUpdate(model, dutch);
        assertEquals(before + DRAW_STEP, randomState(model));
        // Reopening and Continue read the saved state and draw nothing.
        assertEquals(before + DRAW_STEP, randomState(reload(model)));
    }

    /**
     * The fifth event consumes its draw even when no owned colony can receive a
     * successful convert, so the skip banks nothing and creates nobody. The
     * island has no Dutch colony at all.
     */
    @Test
    void aSuccessWithoutAReachableColonyCreatesNothingAndBanksNothing() {
        final Model model = missionScene();
        establish(model);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Set<Integer> baseline = unitIds(dutch);
        for (int update = 0; update < 10; update++) {
            missionUpdate(model, dutch);
        }
        // Inherited units stay; no convert appears and no success is banked.
        assertEquals(baseline, unitIds(dutch));
        assertFalse(convertOf(model).isNativeConvert());
        assertEquals(0, settlement(model).getMissionUpdates());
    }

    @Test
    void aConvertKeepsItsNativeNationAndCountAcrossSaveAndContinue() {
        final Model model = convertScene(3);
        final Model restored = reload(model);
        final UnitFreeColonist convert = convertOf(restored);
        assertTrue(convert.isNativeConvert());
        assertEquals(Optional.of(NativeNation.AZTEC),
                convert.getConvertNation());
        assertEquals(3, convert.getConvertUnassignedUpdates());
    }

    @Test
    void aConvertReturnsOnTheEighthUnassignedOwnerUpdateOnly() {
        final Model model = convertScene(0);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Set<Integer> withConvert = unitIds(dutch);
        final Set<Integer> withoutConvert = new HashSet<>(withConvert);
        withoutConvert.remove(CONVERT_ID);
        for (int update = 1; update <= 7; update++) {
            model.advanceNativeConverts(dutch);
            assertTrue(model.tryGetUnitById(CONVERT_ID).isPresent(),
                    "Expired too early at update " + update);
        }
        assertEquals(7, convertOf(model).getConvertUnassignedUpdates());
        model.advanceNativeConverts(dutch);
        assertTrue(model.tryGetUnitById(CONVERT_ID).isEmpty());
        // Only the convert leaves; unrelated inherited units remain.
        assertEquals(withoutConvert, unitIds(dutch));
        // The return happened once; a later update finds nobody to return.
        model.advanceNativeConverts(dutch);
        assertEquals(withoutConvert, unitIds(dutch));
    }

    @Test
    void aSavedAccumulatedCountSurvivesContinueAndStillExpiresOnTheEighth() {
        final Model restored = reload(convertScene(5));
        final Player dutch = restored.getPlayerByName(DUTCH);
        restored.advanceNativeConverts(dutch);
        restored.advanceNativeConverts(dutch);
        assertTrue(restored.tryGetUnitById(CONVERT_ID).isPresent());
        restored.advanceNativeConverts(dutch);
        assertTrue(restored.tryGetUnitById(CONVERT_ID).isEmpty());
    }

    /**
     * Founding is refused for a convert before any change, while an ordinary
     * colonist at a comparable free site is allowed, so the refusal comes from
     * the convert role and not from the site.
     */
    @Test
    void foundingByAConvertIsRefusedWithoutAnyChange() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(3, 3), convert(0))
                .withMovesForLastUnit()
                .unit(DUTCH, UnitType.COLONIST, at(-3, 3), PLAIN)
                .withMovesForLastUnit().toModel();
        final UnitFreeColonist convert = convertOf(model);
        final Unit ordinary = model.getUnitById(SECOND_ID);
        assertTrue(model.canUnitBuildColony(ordinary));
        assertFalse(model.canUnitBuildColony(convert));

        final Gson gson = new Gson();
        final var before = gson.toJsonTree(model.save());
        final int colonies = model.getColonies().size();
        assertThrows(RuntimeException.class,
                () -> model.buildColony(model.getPlayerByName(DUTCH), convert,
                        "Tenochtitlan"));
        assertEquals(before, gson.toJsonTree(model.save()));
        assertEquals(colonies, model.getColonies().size());
        assertTrue(convert.isAtPlaceLocation());
        assertTrue(convert.isNativeConvert());
    }

    @Test
    void aConvertTakesNoManufacturingButMayStaffANonManufacturingBuilding() {
        final UnitFreeColonist convert = convertOf(convertScene(0));
        final UnitFreeColonist ordinary = (UnitFreeColonist) missionScene()
                .getUnitById(MISSIONARY_ID);
        assertFalse(ConstructionSlot.isWorkAllowed(convert,
                ConstructionType.LUMBER_MILL));
        assertTrue(ConstructionSlot.isWorkAllowed(convert,
                ConstructionType.TOWN_HALL));
        assertTrue(ConstructionSlot.isWorkAllowed(ordinary,
                ConstructionType.LUMBER_MILL));
    }

    /**
     * Education may change the specialty of a convert, but it stays the same
     * person with the same native affiliation and count, and the saved game
     * still loads.
     */
    @Test
    void educationKeepsTheConvertIdentityAndTheSaveValid() {
        final Model model = convertScene(4);
        final UnitFreeColonist convert = convertOf(model);
        convert.completeEducation(UnitType.EXPERT_FARMER);
        assertEquals(UnitType.EXPERT_FARMER, convert.getType());
        assertTrue(convert.isNativeConvert());
        assertEquals(Optional.of(NativeNation.AZTEC),
                convert.getConvertNation());
        assertEquals(4, convert.getConvertUnassignedUpdates());

        final UnitFreeColonist restored = convertOf(reload(model));
        assertEquals(UnitType.EXPERT_FARMER, restored.getType());
        assertTrue(restored.isNativeConvert());
        assertEquals(Optional.of(NativeNation.AZTEC),
                restored.getConvertNation());
        assertEquals(4, restored.getConvertUnassignedUpdates());
        // The restriction travels with the person.
        assertFalse(model.canUnitBuildColony(convert) || ConstructionSlot
                .isWorkAllowed(convert, ConstructionType.LUMBER_MILL));
    }

    /**
     * A convert in a native course is unassigned and keeps counting. At the
     * eighth update it leaves through the shared cleanup, the settlement drops
     * its pupil and a later teaching update finds nothing dangling.
     */
    @Test
    void anExpiringPupilLeavesNoCourseBehind() {
        final Model model = convertScene(7);
        final Player dutch = model.getPlayerByName(DUTCH);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(convertOf(model), site,
                site.getTeachableSkill());
        assertTrue(convertOf(model).isAtPlaceNativeCourse());

        model.advanceNativeConverts(dutch);

        assertTrue(model.tryGetUnitById(CONVERT_ID).isEmpty());
        assertTrue(site.getTeachingPupilId().isEmpty());
        // The teaching update and Continue must not meet a missing pupil.
        model.advanceNativeTeaching(dutch);
        assertTrue(reload(model).tryGetUnitById(CONVERT_ID).isEmpty());
    }

    @Test
    void aMalformedSavedConvertIsRejectedWhenTheModelIsBuilt() {
        final ModelPo noNation = convertScene(0).save();
        noNation.getUnits().stream().filter(unit -> unit.getId() == CONVERT_ID)
                .findFirst().orElseThrow().setNativeConvertNation(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(noNation,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo fullCount = convertScene(0).save();
        fullCount.getUnits().stream().filter(unit -> unit.getId() == CONVERT_ID)
                .findFirst().orElseThrow().setNativeConvertUnassignedUpdates(8);
        assertThrows(IllegalArgumentException.class, () -> Model.make(fullCount,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo strayFields = missionScene().save();
        strayFields.getUnits().stream()
                .filter(unit -> unit.getId() == MISSIONARY_ID).findFirst()
                .orElseThrow()
                .setNativeConvertNation(NativeNation.AZTEC.name());
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(strayFields,
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
