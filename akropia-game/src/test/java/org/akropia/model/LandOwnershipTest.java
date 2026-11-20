package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class LandOwnershipTest {
    @Test
    void freeLandReturnsOnlyAfterFriendshipAndOccupationEnd() {
        final LandOwnership land = LandOwnership.make(List.of(), at -> true);
        final Location at = Location.of(3, 4);
        land.acquire(at, "England", NativeNation.ARAWAK, false);
        land.releaseUnoccupied((tribe, player) -> true,
                (square, player) -> false);
        assertEquals(Optional.of("England"), land.owner(at));
        land.releaseUnoccupied((tribe, player) -> false,
                (square, player) -> true);
        assertEquals(Optional.of("England"), land.owner(at));
        final LandOwnership restored = LandOwnership.make(land.save(),
                square -> true);
        restored.releaseUnoccupied((tribe, player) -> false,
                (square, player) -> false);
        assertTrue(restored.owner(at).isEmpty());
        assertEquals(Optional.of(NativeNation.ARAWAK),
                restored.nativeClaim(at));
        final LandOwnership returned = LandOwnership.make(restored.save(),
                square -> true);
        returned.acquire(at, "France", NativeNation.ARAWAK, false);
        assertEquals(Optional.of("France"), returned.owner(at));
        assertEquals(Optional.of(NativeNation.ARAWAK),
                returned.nativeClaim(at));
    }

    @Test
    void paidTitleSurvivesHostilityAndCannotBeBoughtByAnotherPlayer() {
        final LandOwnership land = LandOwnership.make(List.of(), at -> true);
        final Location at = Location.of(3, 4);
        land.acquire(at, "England", NativeNation.ARAWAK, true);
        land.releaseUnoccupied((tribe, player) -> false,
                (square, player) -> false);
        assertEquals(Optional.of("England"), land.owner(at));
        assertEquals(Optional.of(NativeNation.ARAWAK), land.nativeClaim(at));
        assertThrows(IllegalArgumentException.class,
                () -> land.acquire(at, "France", NativeNation.ARAWAK, true));
        assertEquals(Optional.of("England"), land.owner(at));
    }

    @Test
    void unclaimedLandCanBeUnownedAndSettledLandHasNoNativeReturn() {
        final LandOwnership land = LandOwnership.make(List.of(), at -> true);
        final Location at = Location.of(3, 4);
        assertTrue(land.owner(at).isEmpty());
        land.acquire(at, "England", null, false);
        land.releaseUnoccupied((tribe, player) -> false,
                (square, player) -> false);
        assertEquals(Optional.of("England"), land.owner(at));
        assertThrows(IllegalArgumentException.class,
                () -> LandOwnership.make(land.save(), square -> false));
    }
}
