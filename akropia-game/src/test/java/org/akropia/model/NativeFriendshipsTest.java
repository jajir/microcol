package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class NativeFriendshipsTest {
    private static final NativeNation TRIBE = NativeNation.ARAWAK;
    private static final EuropeanNation US = EuropeanNation.ENGLISH;
    private static final EuropeanNation RIVAL = EuropeanNation.FRENCH;
    private static final Set<EuropeanNation> PLAYERS = Set.of(US, RIVAL);

    private NativeFriendships fresh() {
        return NativeFriendships.make(NativeFriendships.fresh(), PLAYERS);
    }

    @Test
    void missionaryIsOneNationalChangeAndAnAttackEndsFriendship() {
        final NativeFriendships relations = fresh();
        assertEquals(5, relations.favour(TRIBE, US));
        assertTrue(relations.friend(TRIBE).isEmpty());
        relations.sendMissionary(TRIBE, US, 2);
        assertEquals(7, relations.favour(TRIBE, US));
        assertEquals(3, relations.favour(TRIBE, RIVAL));
        assertEquals(5, relations.favour(NativeNation.INCA, US));
        relations.sendMissionary(TRIBE, US, 1);
        assertEquals(Optional.of(US), relations.friend(TRIBE));
        relations.attacked(TRIBE, US);
        assertEquals(5, relations.favour(TRIBE, US));
        assertTrue(relations.friend(TRIBE).isEmpty());
    }

    @Test
    void repeatedMissionariesCanWinOverAnEnemyFriendAndValuesStayBounded() {
        final NativeFriendships relations = fresh();
        for (int i = 0; i < 10; i++) {
            relations.sendMissionary(TRIBE, US, 2);
        }
        assertEquals(10, relations.favour(TRIBE, US));
        assertEquals(1, relations.favour(TRIBE, RIVAL));
        for (int i = 0; i < 5; i++) {
            relations.sendMissionary(TRIBE, RIVAL, 2);
        }
        assertEquals(Optional.of(RIVAL), relations.friend(TRIBE));
        assertEquals(1, relations.favour(TRIBE, US));
        relations.attacked(TRIBE, US);
        assertEquals(1, relations.favour(TRIBE, US));
    }

    @Test
    void tiedLeadersHaveNoFriendAndInactiveNationsDoNotCompete() {
        final var rows = NativeFriendships.fresh();
        rows.get(0).getFavour().put(US, 8);
        rows.get(0).getFavour().put(RIVAL, 8);
        assertTrue(
                NativeFriendships.make(rows, PLAYERS).friend(TRIBE).isEmpty());
        rows.get(0).setFriend(US);
        assertEquals(Optional.of(US),
                NativeFriendships.make(rows, Set.of(US)).friend(TRIBE));
    }

    @Test
    void exactlyOneArrivalPerFortyTurnsAndContinueDoesNotReroll() {
        NativeFriendships relations = fresh();
        relations.sendMissionary(TRIBE, US, 2);
        relations.sendMissionary(TRIBE, US, 2);
        final AtomicInteger draws = new AtomicInteger();
        int arrivals = 0;
        for (int turn = 1; turn <= 80; turn++) {
            if (relations.advance(TRIBE, () -> {
                draws.incrementAndGet();
                return 7;
            }).isPresent()) {
                arrivals++;
                assertTrue(turn == 7 || turn == 47);
            }
            relations = NativeFriendships.make(relations.save(), PLAYERS);
        }
        assertEquals(2, arrivals);
        assertEquals(2, draws.get());
    }

    @Test
    void losingFriendshipDiscardsThePreviousConversionPeriod() {
        final NativeFriendships relations = fresh();
        relations.sendMissionary(TRIBE, US, 2);
        relations.sendMissionary(TRIBE, US, 2);
        assertTrue(relations.advance(TRIBE, () -> 2).isEmpty());
        relations.attacked(TRIBE, US);
        assertTrue(relations.advance(TRIBE, () -> {
            fail("No draw without friendship");
            return 1;
        }).isEmpty());
        relations.sendMissionary(TRIBE, US, 2);
        assertTrue(relations.advance(TRIBE, () -> 2).isEmpty());
        assertEquals(Optional.of(US), relations.advance(TRIBE, () -> {
            fail("Must retain draw");
            return 1;
        }));
    }

    @Test
    void malformedSaveIsRejectedAndSaveCopiesCannotMutateLiveRelations() {
        assertThrows(IllegalArgumentException.class,
                () -> NativeFriendships.make(null, PLAYERS));
        assertThrows(IllegalArgumentException.class,
                () -> NativeFriendships.make(List.of(), PLAYERS));
        final NativeFriendships relations = fresh();
        final var rows = relations.save();
        rows.get(0).getFavour().put(US, 11);
        assertThrows(IllegalArgumentException.class,
                () -> NativeFriendships.make(rows, PLAYERS));
        assertEquals(5, relations.favour(TRIBE, US));
    }
}
