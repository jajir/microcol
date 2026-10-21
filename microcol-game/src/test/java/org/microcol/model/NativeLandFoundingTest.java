package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.model.NativeRelationsScene.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Founding on native-claimed land. The Dutch founder stands one square east of
 * the Aztec capital, so the colony square and every field around it are claimed
 * by the capital at 200 gold each. The preview names the squares the founding
 * would really use; the founding then uses exactly them.
 */
class NativeLandFoundingTest {

    private static final double EXACT = 1e-9;

    /** First unit added by the scene. */
    private static final int FOUNDER_ID = 200;

    private static final Location COLONY_SITE = at(1, 0);

    private static final String NAME = "Founding Test";

    private static NativeRelationsScene founderScene(final int gold) {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, COLONY_SITE, UNARMED)
                .withMovesForLastUnit().gold(DUTCH, gold);
    }

    private static List<NativeLandQuote> quotes(final Model model,
            final List<Location> land) {
        final Player dutch = model.getPlayerByName(DUTCH);
        return land.stream()
                .map(at -> model.quoteNativeLand(dutch, at).orElseThrow())
                .collect(Collectors.toList());
    }

    private static void assertNothingChanged(final Model model,
            final int gold) {
        final Player dutch = model.getPlayerByName(DUTCH);
        assertTrue(model.getColonyAt(COLONY_SITE).isEmpty());
        assertTrue(model.getUnitById(FOUNDER_ID).isAtPlaceLocation());
        assertEquals(gold, dutch.getGold());
        assertEquals(2000, settlement(model).getTreasury());
        assertEquals(0, model.save().getNativeLandRights().size());
        assertEquals(0, model.save().getNativeLandTakings().size());
        assertEquals(0, model.getNativeLocalAlarm(dutch, settlement(model)),
                EXACT);
    }

    @Test
    void previewNamesTheColonySquareAndTheFieldTheFounderThenWorks() {
        final Model model = founderScene(500).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final List<Location> land = model.getFoundingLand(founder);
        assertEquals(2, land.size());
        assertEquals(COLONY_SITE, land.get(0));
        // The capital's own square is never a claimed square of this test.
        assertNotEquals(SITE, land.get(1));

        // Unpaid founding: the colony square and the first field are both
        // taken once, 5 each, with no update yet.
        final Colony colony = model.buildColony(dutch, founder, NAME, land,
                List.of());
        assertEquals(land.get(1),
                colony.getColonyFields().stream()
                        .filter(ColonyField::isNotEmpty).findFirst()
                        .orElseThrow().getLocation());
        assertEquals(2, model.save().getNativeLandTakings().size());
        assertEquals(10, model.getNativeLocalAlarm(dutch, settlement(model)),
                EXACT);
        assertEquals(500, dutch.getGold());
    }

    @Test
    void payingWithTheFoundingBuysEverySquareOnceAndTakesNothing() {
        final Model model = founderScene(500).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final List<Location> land = model.getFoundingLand(founder);
        final List<NativeLandQuote> purchases = quotes(model, land);
        assertTrue(model.canPayFoundingLand(founder, land, purchases));

        model.buildColony(dutch, founder, NAME, land, purchases);
        assertTrue(model.getColonyAt(COLONY_SITE).isPresent());
        assertEquals(100, dutch.getGold());
        assertEquals(2400, settlement(model).getTreasury());
        assertEquals(2, model.save().getNativeLandRights().size());
        assertEquals(0, model.save().getNativeLandTakings().size());
        assertEquals(0, model.getNativeLocalAlarm(dutch, settlement(model)),
                EXACT);
        for (final Location at : land) {
            assertEquals(NativeLandStatus.PAID,
                    model.quoteNativeLand(dutch, at).orElseThrow().getStatus());
        }
    }

    @Test
    void goldForOneSquareButNotForBothRefusesTheWholeFounding() {
        final Model model = founderScene(399).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final List<Location> land = model.getFoundingLand(founder);
        final List<NativeLandQuote> purchases = quotes(model, land);
        // Each square alone is affordable; together they are not.
        assertTrue(model.canBuyNativeLand(purchases.get(0)));
        assertFalse(model.canPayFoundingLand(founder, land, purchases));
        assertThrows(IllegalArgumentException.class,
                () -> model.buildColony(dutch, founder, NAME, land, purchases));
        assertNothingChanged(model, 399);
    }

    @Test
    void stalePreviewRefusesTheFoundingWithoutAnyChange() {
        final Model model = founderScene(500).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        // A preview that misses the first field is no longer what would be
        // used.
        assertThrows(IllegalArgumentException.class,
                () -> model.buildColony(dutch, founder, NAME,
                        List.of(COLONY_SITE), List.of()));
        assertNothingChanged(model, 500);
    }

    @Test
    void buyingASquareTheFoundingDoesNotUseIsRefused() {
        final Model model = founderScene(500).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final List<Location> land = model.getFoundingLand(founder);
        final Location unused = Stream.of(at(2, 0), at(2, 1))
                .filter(at -> !land.contains(at)).findFirst().orElseThrow();
        final List<NativeLandQuote> purchases = quotes(model, List.of(unused));
        assertFalse(model.canPayFoundingLand(founder, land, purchases));
        assertThrows(IllegalArgumentException.class,
                () -> model.buildColony(dutch, founder, NAME, land, purchases));
        assertNothingChanged(model, 500);
    }

    @Test
    void minuitFoundingTakesNothingAndCostsNothing() {
        final Model model = founderScene(500).minuitMember(DUTCH).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final List<Location> land = model.getFoundingLand(founder);
        for (final NativeLandQuote quote : quotes(model, land)) {
            assertEquals(NativeLandStatus.WAIVED, quote.getStatus());
            assertEquals(0, quote.getPrice());
        }
        model.buildColony(dutch, founder, NAME, land, List.of());
        assertTrue(model.getColonyAt(COLONY_SITE).isPresent());
        assertEquals(500, dutch.getGold());
        assertEquals(0, model.save().getNativeLandTakings().size());
        assertEquals(0, model.getNativeLocalAlarm(dutch, settlement(model)),
                EXACT);
    }

    @Test
    void aFounderThatCannotFoundHasNoPreview() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, COLONY_SITE, UNARMED).toModel();
        // The scene's ordinary units have no movement left.
        assertTrue(
                model.getFoundingLand(model.getUnitById(FOUNDER_ID)).isEmpty());
    }

}
