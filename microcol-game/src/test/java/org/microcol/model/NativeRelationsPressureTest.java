package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

/**
 * Live native pressure: complete saved units and colonies are loaded by the
 * real loader, and the exact local alarm and tribal anger are read after the
 * real European update. With one settlement of a nation the mean local alarm is
 * its own alarm, so each expected tribal anger is the alarm divided by 20.
 */
class NativeRelationsPressureTest {

    private static final double EXACT = 1e-9;

    private static Model updated(final NativeRelationsScene scene,
            final String owner) {
        final Model model = scene.toModel();
        model.updateNativeRelations(model.getPlayerByName(owner));
        return model;
    }

    private static Model dutchWith(final NativeNation nation,
            final UnitType type, final Consumer<UnitPo> equipment, final int dx,
            final int dy) {
        return updated(island(nation).unit(DUTCH, type, at(dx, dy), equipment),
                DUTCH);
    }

    private static void assertRelations(final Model model, final String owner,
            final NativeNation nation, final double alarm, final double anger) {
        final Player player = model.getPlayerByName(owner);
        assertEquals(alarm,
                model.getNativeLocalAlarm(player, settlement(model)), EXACT);
        assertEquals(anger, model.getNativeTribalAnger(player, nation), EXACT);
    }

    @Test
    void homeRadiusIsOneForCampAndVillageAndTwoForCity() {
        assertEquals(1, NativeSettlementClass.CAMP.getHomeRadius());
        assertEquals(1, NativeSettlementClass.VILLAGE.getHomeRadius());
        assertEquals(2, NativeSettlementClass.CITY.getHomeRadius());
    }

    @Test
    void cityCountsUnarmedResidentsQuarterEachUpToTwoSquares() {
        // Distance two orthogonal, diagonal and with tools or horses counts;
        // distance three, also on the diagonal, does not.
        final Model model = updated(
                island(NativeNation.AZTEC)
                        .unit(DUTCH, UnitType.COLONIST, at(2, 0), UNARMED)
                        .unit(DUTCH, UnitType.COLONIST, at(-2, 2), PIONEER)
                        .unit(DUTCH, UnitType.COLONIST, at(0, -2), SCOUT)
                        .unit(DUTCH, UnitType.COLONIST, at(3, 0), UNARMED)
                        .unit(DUTCH, UnitType.COLONIST, at(-3, 3), UNARMED),
                DUTCH);
        assertRelations(model, DUTCH, NativeNation.AZTEC, 0.75, 0.0375);
    }

    @Test
    void campAndVillageCountResidentsOnlyOneSquareAway() {
        for (final NativeNation nation : new NativeNation[] { NativeNation.TUPI,
                NativeNation.CHEROKEE }) {
            final Model model = updated(
                    island(nation)
                            .unit(DUTCH, UnitType.COLONIST, at(1, 1), UNARMED)
                            .unit(DUTCH, UnitType.COLONIST, at(-1, 0), UNARMED)
                            .unit(DUTCH, UnitType.COLONIST, at(2, 0), UNARMED)
                            .unit(DUTCH, UnitType.COLONIST, at(0, -2), UNARMED),
                    DUTCH);
            assertRelations(model, DUTCH, nation, 0.5, 0.025);
        }
    }

    @Test
    void equippedSoldierDragoonVeteranAndArtilleryAreResidentAndArmed() {
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, SOLDIER, 1, 0),
                DUTCH, NativeNation.AZTEC, 2.25, 0.1125);
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, DRAGOON, 0, 1),
                DUTCH, NativeNation.AZTEC, 2.25, 0.1125);
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.COLONIST,
                VETERAN, -1, 0), DUTCH, NativeNation.AZTEC, 2.25, 0.1125);
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.ARTILLERY,
                UNARMED, 1, -1), DUTCH, NativeNation.AZTEC, 2.25, 0.1125);
    }

    @Test
    void armedRadiusIsTwoWhileResidentRadiusFollowsTheClass() {
        // A camp ignores a resident two squares away but not its musket.
        assertRelations(
                dutchWith(NativeNation.TUPI, UnitType.COLONIST, SOLDIER, 2, 0),
                DUTCH, NativeNation.TUPI, 2.0, 0.1);
        assertRelations(
                dutchWith(NativeNation.TUPI, UnitType.COLONIST, SOLDIER, 1, 0),
                DUTCH, NativeNation.TUPI, 2.25, 0.1125);
        assertRelations(dutchWith(NativeNation.TUPI, UnitType.ARTILLERY,
                UNARMED, 2, -2), DUTCH, NativeNation.TUPI, 2.0, 0.1);
        // A city counts both at two squares, and neither at three.
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.COLONIST,
                SOLDIER, 2, -2), DUTCH, NativeNation.AZTEC, 2.25, 0.1125);
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, SOLDIER, 3, 0),
                DUTCH, NativeNation.AZTEC, 0, 0);
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, SOLDIER, 3, 3),
                DUTCH, NativeNation.AZTEC, 0, 0);
        assertRelations(
                dutchWith(NativeNation.TUPI, UnitType.ARTILLERY, UNARMED, 3, 0),
                DUTCH, NativeNation.TUPI, 0, 0);
    }

    @Test
    void ranksToolsAndHorsesWithoutMusketsAreOnlyResidents() {
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST,
                        UNARMED_VETERAN, 1, 0),
                DUTCH, NativeNation.AZTEC, 0.25, 0.0125);
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, SCOUT, 1, 0),
                DUTCH, NativeNation.AZTEC, 0.25, 0.0125);
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.COLONIST, PIONEER, 1, 0),
                DUTCH, NativeNation.AZTEC, 0.25, 0.0125);
    }

    @Test
    void shipsAreNeverResidentsAndOnlyArmedShipsCountAsArmed() {
        // Unarmed ships close by add nothing.
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.GALLEON, UNARMED,
                SEA_ONE.getX() - SITE.getX(), SEA_ONE.getY() - SITE.getY()),
                DUTCH, NativeNation.AZTEC, 0, 0);
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.CARAVEL, UNARMED,
                SEA_ONE.getX() - SITE.getX(), SEA_ONE.getY() - SITE.getY()),
                DUTCH, NativeNation.AZTEC, 0, 0);
        // A frigate two squares away is armed but not a resident.
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.FRIGATE, UNARMED,
                SEA_TWO.getX() - SITE.getX(), SEA_TWO.getY() - SITE.getY()),
                DUTCH, NativeNation.AZTEC, 2.0, 0.1);
        // The same frigate three squares away adds nothing.
        assertRelations(dutchWith(NativeNation.AZTEC, UnitType.FRIGATE, UNARMED,
                SEA_THREE.getX() - SITE.getX(), SEA_THREE.getY() - SITE.getY()),
                DUTCH, NativeNation.AZTEC, 0, 0);
    }

    @Test
    void wagonIsNeitherResidentNorArmed() {
        assertRelations(
                dutchWith(NativeNation.AZTEC, UnitType.WAGON, UNARMED, 1, 0),
                DUTCH, NativeNation.AZTEC, 0, 0);
        final Model withColonist = updated(
                island(NativeNation.AZTEC)
                        .unit(DUTCH, UnitType.WAGON, at(1, 0), UNARMED)
                        .unit(DUTCH, UnitType.COLONIST, at(0, 1), UNARMED),
                DUTCH);
        assertRelations(withColonist, DUTCH, NativeNation.AZTEC, 0.25, 0.0125);
    }

    @Test
    void colonyWorkerCountsOnceAndAMapUnitOnTheColonySquareIsAnotherResident() {
        // Montreal's field worker is inside the colony, two squares away from
        // the city; France halves the 0.25.
        assertRelations(updated(besideMontreal(NativeNation.AZTEC, 2), FRANCE),
                FRANCE, NativeNation.AZTEC, 0.125, 0.00625);
        // A second colonist standing on the colony square adds exactly one
        // more resident; the worker is not counted a second time.
        assertRelations(
                updated(besideMontreal(NativeNation.AZTEC, 2).unit(FRANCE,
                        UnitType.COLONIST, MONTREAL, UNARMED), FRANCE),
                FRANCE, NativeNation.AZTEC, 0.25, 0.0125);
    }

    @Test
    void fortAddsTwoAndFortressThreeOnTopOfTheWorker() {
        assertRelations(
                updated(besideMontreal(NativeNation.AZTEC, 2)
                        .montrealFortification(ConstructionType.FORT), FRANCE),
                FRANCE, NativeNation.AZTEC, 1.125, 0.05625);
        assertRelations(updated(besideMontreal(NativeNation.AZTEC, 2)
                .montrealFortification(ConstructionType.FORTRESS), FRANCE),
                FRANCE, NativeNation.AZTEC, 1.625, 0.08125);
    }

    @Test
    void colonyOutsideTheHomeRadiusAddsNothing() {
        // A city sees only two squares: Montreal at three is out of range.
        assertRelations(updated(besideMontreal(NativeNation.AZTEC, 3)
                .montrealFortification(ConstructionType.FORTRESS), FRANCE),
                FRANCE, NativeNation.AZTEC, 0, 0);
        // A camp sees one square: Montreal at two is out of range.
        assertRelations(updated(besideMontreal(NativeNation.TUPI, 2)
                .montrealFortification(ConstructionType.FORTRESS), FRANCE),
                FRANCE, NativeNation.TUPI, 0, 0);
    }

    @Test
    void anotherOwnersUnitsAndOtherEuropeansPairsAreNotTouched() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(0, 1), SOLDIER)
                .alarm(EuropeanNation.FRENCH, 7)
                .tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 3).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Player france = model.getPlayerByName(FRANCE);
        final NativeSettlement settlement = settlement(model);

        // The Dutch update sees only the Dutch colonist and leaves France as
        // is.
        model.updateNativeRelations(dutch);
        assertEquals(0.25, model.getNativeLocalAlarm(dutch, settlement), EXACT);
        assertEquals(0.0125,
                model.getNativeTribalAnger(dutch, NativeNation.AZTEC), EXACT);
        assertEquals(7, model.getNativeLocalAlarm(france, settlement), EXACT);
        assertEquals(3, model.getNativeTribalAnger(france, NativeNation.AZTEC),
                EXACT);

        // France sees only its own soldier: 7 - 1 + 2.25 / 2, then the mean.
        model.updateNativeRelations(france);
        assertEquals(7.125, model.getNativeLocalAlarm(france, settlement),
                EXACT);
        assertEquals(2.35625,
                model.getNativeTribalAnger(france, NativeNation.AZTEC), EXACT);
        assertEquals(0.25, model.getNativeLocalAlarm(dutch, settlement), EXACT);
        assertEquals(0.0125,
                model.getNativeTribalAnger(dutch, NativeNation.AZTEC), EXACT);

        // The two Europeans without a player keep their saved zeros.
        final ModelPo saved = model.save();
        saved.getNativeLocalAlarm().stream()
                .filter(entry -> entry.getEuropean() == EuropeanNation.ENGLISH
                        || entry.getEuropean() == EuropeanNation.SPANISH)
                .forEach(entry -> assertEquals(0, entry.getValue(), EXACT));
        saved.getNativeTribalAnger().stream()
                .filter(entry -> entry.getEuropean() == EuropeanNation.ENGLISH
                        || entry.getEuropean() == EuropeanNation.SPANISH)
                .forEach(entry -> assertEquals(0, entry.getValue(), EXACT));
    }

    /** Same soldier and colonist beside the city, for the owner's pairs. */
    private static NativeRelationsScene sameSources(final String owner,
            final boolean pocahontas) {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(owner, UnitType.COLONIST, at(1, 0), SOLDIER)
                .unit(owner, UnitType.COLONIST, at(0, 1), UNARMED);
        return pocahontas ? scene.pocahontasMember(owner) : scene;
    }

    @Test
    void sameSourcesGiveFranceHalfAndPocahontasStacksToAQuarterOfDutchPressure() {
        // Raw pressure: soldier 0.25 + 2, colonist 0.25 = 2.5.
        assertRelations(updated(sameSources(DUTCH, false), DUTCH), DUTCH,
                NativeNation.AZTEC, 2.5, 0.125);
        assertRelations(updated(sameSources(DUTCH, true), DUTCH), DUTCH,
                NativeNation.AZTEC, 1.25, 0.0625);
        assertRelations(updated(sameSources(FRANCE, false), FRANCE), FRANCE,
                NativeNation.AZTEC, 1.25, 0.0625);
        assertRelations(updated(sameSources(FRANCE, true), FRANCE), FRANCE,
                NativeNation.AZTEC, 0.625, 0.03125);
    }

    @Test
    void decayThenPressureThenMeanOrderKeepsFractionsExact() {
        // Dutch: alarm 20 - 1 + 2.5 = 21.5, anger 10 - 1 + 21.5 / 20.
        assertRelations(updated(
                sameSources(DUTCH, false).alarm(EuropeanNation.DUTCH, 20)
                        .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10),
                DUTCH), DUTCH, NativeNation.AZTEC, 21.5, 10.075);
        // France with Pocahontas: alarm 20 - 1 + 0.625, anger 10 - 1 + that /
        // 20.
        assertRelations(updated(
                sameSources(FRANCE, true).alarm(EuropeanNation.FRENCH, 20)
                        .tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 10),
                FRANCE), FRANCE, NativeNation.AZTEC, 19.625, 9.98125);
    }

    @Test
    void directLocalEffectIsReducedByFranceAndPocahontasButTribalIsNeverHalved() {
        final Object[][] cases = { { DUTCH, false, 10.0 }, { DUTCH, true, 5.0 },
                { FRANCE, false, 5.0 }, { FRANCE, true, 2.5 } };
        for (final Object[] entry : cases) {
            final String owner = (String) entry[0];
            final NativeRelationsScene scene = island(NativeNation.AZTEC);
            final Model model = (Boolean) entry[1]
                    ? scene.pocahontasMember(owner).toModel()
                    : scene.toModel();
            final Player player = model.getPlayerByName(owner);
            model.addNativeLocalAlarm(settlement(model), player, 10);
            model.addNativeTribalAnger(NativeNation.AZTEC, player, 10);
            assertEquals((Double) entry[2],
                    model.getNativeLocalAlarm(player, settlement(model)),
                    EXACT);
            assertEquals(10,
                    model.getNativeTribalAnger(player, NativeNation.AZTEC),
                    EXACT);
        }
    }

    @Test
    void liveHostilityReadsTheLargerMeasureAtTheBandEdge() {
        // 76 decays to exactly 75 and is hostile; 75 decays to 74 and is angry.
        final Model hostile = updated(
                island(NativeNation.AZTEC).alarm(EuropeanNation.DUTCH, 76),
                DUTCH);
        assertEquals(NativeHostility.HOSTILE, hostile.getNativeHostility(
                hostile.getPlayerByName(DUTCH), settlement(hostile)));
        final Model angry = updated(
                island(NativeNation.AZTEC).alarm(EuropeanNation.DUTCH, 75),
                DUTCH);
        assertEquals(NativeHostility.ANGRY, angry.getNativeHostility(
                angry.getPlayerByName(DUTCH), settlement(angry)));
        // Tribal anger alone also decides: 51 decays to 50, the angry edge.
        final Model tribal = updated(island(NativeNation.AZTEC)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 51), DUTCH);
        assertEquals(NativeHostility.ANGRY, tribal.getNativeHostility(
                tribal.getPlayerByName(DUTCH), settlement(tribal)));
    }

    @Test
    void aCrownHasNoNativeRelationsToUpdate() {
        final Model model = island(NativeNation.AZTEC)
                .alarm(EuropeanNation.FRENCH, 7)
                .tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 3).toModel();
        model.updateNativeRelations(model.getPlayerByName("France's King"));
        final Player france = model.getPlayerByName(FRANCE);
        assertEquals(7, model.getNativeLocalAlarm(france, settlement(model)),
                EXACT);
        assertEquals(3, model.getNativeTribalAnger(france, NativeNation.AZTEC),
                EXACT);
    }

}
