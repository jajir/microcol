package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.microcol.model.EuropeanRelationState.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.EuropeanRelationPo;
import com.google.gson.Gson;

class EuropeanRelationsTest {
    private final Model model = mock(Model.class);
    private final Player dutch = player("Dutch");
    private final Player french = player("France");
    private final Player spanish = player("Spain");
    private final Player crown = new PlayerKing("Crown", true, 0, model, false,
            dutch, Map.of(), Set.of(),
            new RoyalRoster(RoyalRoster.initial(Difficulty.CONQUISTADOR),
                    Difficulty.CONQUISTADOR));
    private final List<Player> players = List.of(dutch, french, spanish, crown);

    private Player player(final String key) {
        return new PlayerHuman(key, false, 0, model, false, Map.of(), Set.of());
    }

    private static EuropeanRelationPo row(final String first,
            final String second, final EuropeanRelationState state) {
        EuropeanRelationPo row = new EuropeanRelationPo();
        row.setFirstPlayer(first);
        row.setSecondPlayer(second);
        row.setState(state);
        return row;
    }

    private List<EuropeanRelationPo> peace() {
        return new ArrayList<>(List.of(row("Dutch", "France", PEACE),
                row("Dutch", "Spain", PEACE), row("France", "Spain", PEACE)));
    }

    private String snapshot(final EuropeanRelations relations) {
        return new Gson().toJson(relations.save((pair, at) -> false));
    }

    @Test
    void declarationIsSymmetricAndChangesOnlyOneOfThreePairs() {
        EuropeanRelations relations = new EuropeanRelations(players, peace());
        relations.transition(dutch, french, PEACE, WAR);
        assertEquals(WAR, relations.get(dutch, french));
        assertEquals(WAR, relations.get(french, dutch));
        assertEquals(PEACE, relations.get(dutch, spanish));
        assertEquals(PEACE, relations.get(french, spanish));
        assertEquals(3, relations.save((pair, at) -> false).size());
    }

    @Test
    void acceptedPeaceEndsOnlyThatWarAndCanLaterBeBroken() {
        EuropeanRelations relations = new EuropeanRelations(players, peace());
        relations.transition(dutch, french, PEACE, WAR);
        relations.transition(dutch, spanish, PEACE, WAR);
        relations.transition(french, dutch, WAR, PEACE);
        assertEquals(PEACE, relations.get(dutch, french));
        assertEquals(WAR, relations.get(dutch, spanish));
        relations.transition(dutch, french, PEACE, WAR);
        assertEquals(WAR, relations.get(french, dutch));
    }

    @Test
    void renamedLabelsAndReorderedDataRetainIdentity() {
        EuropeanRelations relations = new EuropeanRelations(players, peace());
        relations.transition(dutch, french, PEACE, WAR);
        dutch.setDisplayName("  Žluťoučký  hráč  ");
        french.setDisplayName("Žluťoučký  hráč");
        assertEquals("Žluťoučký  hráč", dutch.getDisplayName());
        assertEquals("Dutch", dutch.getName());
        List<EuropeanRelationPo> saved = new ArrayList<>(
                relations.save((pair, at) -> false));
        Collections.reverse(saved);
        saved.forEach(r -> {
            String first = r.getFirstPlayer();
            r.setFirstPlayer(r.getSecondPlayer());
            r.setSecondPlayer(first);
        });
        EuropeanRelations restored = new EuropeanRelations(
                List.of(crown, spanish, french, dutch), saved);
        assertEquals(WAR, restored.get(dutch, french));
        assertEquals(PEACE, restored.get(spanish, french));
        assertEquals(PEACE, restored.get(spanish, dutch));
    }

    @Test
    void inputAndSavedDataCannotMutateLiveRelations() {
        List<Player> registry = new ArrayList<>(players);
        List<EuropeanRelationPo> source = peace();
        EuropeanRelations relations = new EuropeanRelations(registry, source);
        source.get(0).setState(WAR);
        source.clear();
        registry.clear();
        assertEquals(PEACE, relations.get(dutch, french));
        List<EuropeanRelationPo> copy = new ArrayList<>(
                relations.save((pair, at) -> false));
        copy.get(0).setState(WAR);
        copy.clear();
        assertEquals(3, relations.save((pair, at) -> false).size());
        assertEquals(PEACE, relations.get(dutch, french));
    }

    @Test
    void missingIncompleteDuplicateAndMalformedSavedPairsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(players, null));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(players, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(players, peace().subList(0, 2)));
        List<EuropeanRelationPo> duplicate = peace();
        duplicate.add(row("France", "Dutch", WAR));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(players, duplicate));
        duplicate.set(3, row("Dutch", "France", PEACE));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(players, duplicate));
        List<EuropeanRelationPo> invalidRows = new ArrayList<>();
        invalidRows.add(null);
        invalidRows.add(row(null, "France", PEACE));
        invalidRows.add(row("Dutch", "", PEACE));
        invalidRows.add(row("Dutch", "Unknown", PEACE));
        invalidRows.add(row("Dutch", "Dutch", PEACE));
        invalidRows.add(row("Dutch", "Crown", PEACE));
        invalidRows.add(row("Dutch", "France", null));
        for (EuropeanRelationPo invalid : invalidRows) {
            List<EuropeanRelationPo> source = peace();
            source.set(0, invalid);
            String before = new Gson().toJson(source);
            assertThrows(IllegalArgumentException.class,
                    () -> new EuropeanRelations(players, source));
            assertEquals(before, new Gson().toJson(source));
        }
    }

    @Test
    void explicitEmptyPairsAreValidForFewerThanTwoColonizers() {
        assertTrue(new EuropeanRelations(List.of(), List.of())
                .save((pair, at) -> false).isEmpty());
        assertTrue(new EuropeanRelations(List.of(dutch, crown), List.of())
                .save((pair, at) -> false).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeanRelations(List.of(dutch, crown), null));
    }

    @Test
    void invalidLiveEndpointsNeverChangeAnyPair() {
        EuropeanRelations relations = new EuropeanRelations(players, peace());
        List<Player> invalid = new ArrayList<>();
        invalid.add(null);
        invalid.add(dutch);
        invalid.add(crown);
        invalid.add(player("France"));
        String before = snapshot(relations);
        for (Player other : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> relations.get(dutch, other));
            assertThrows(IllegalArgumentException.class,
                    () -> relations.transition(dutch, other, PEACE, WAR));
            assertThrows(IllegalArgumentException.class,
                    () -> relations.transition(other, dutch, PEACE, WAR));
            assertEquals(before, snapshot(relations));
        }
    }

    @Test
    void invalidDuplicateAndStaleTransitionsAreAtomic() {
        EuropeanRelations relations = new EuropeanRelations(players, peace());
        String before = snapshot(relations);
        assertThrows(IllegalArgumentException.class,
                () -> relations.transition(dutch, french, WAR, PEACE));
        assertThrows(IllegalArgumentException.class,
                () -> relations.transition(dutch, french, PEACE, PEACE));
        assertThrows(IllegalArgumentException.class,
                () -> relations.transition(dutch, french, null, WAR));
        assertThrows(IllegalArgumentException.class,
                () -> relations.transition(dutch, french, PEACE, null));
        assertEquals(before, snapshot(relations));
        relations.transition(dutch, french, PEACE, WAR);
        String atWar = snapshot(relations);
        assertThrows(IllegalArgumentException.class,
                () -> relations.transition(dutch, french, PEACE, WAR));
        assertEquals(atWar, snapshot(relations));
    }
}
