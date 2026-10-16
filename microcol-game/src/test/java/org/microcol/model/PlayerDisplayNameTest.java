package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.PlayerPo;

class PlayerDisplayNameTest {
    private final Model model = mock(Model.class);

    private Player player() {
        when(model.getDifficulty()).thenReturn(Difficulty.CONQUISTADOR);
        return new PlayerHuman("Dutch", false, 2108, model, false,
                new HashMap<>(), new HashSet<>(), RecruitmentPool.initial());
    }

    @Test
    void trimsUnicodeWhitespaceAndPreservesInternalText() {
        assertEquals("Jan  Dvořák – kapitán!", Player.normalizeDisplayName(
                "\u00a0\u2003Jan  Dvořák – kapitán!\u2003\u00a0"));
        assertEquals("Éva", Player.normalizeDisplayName("E\u0301va"));
    }

    @Test
    void countsUnicodeCodePointsRatherThanUtf16Units() {
        final String name = "\ud83d\ude80".repeat(64);
        assertEquals(name, Player.normalizeDisplayName(name));
        assertThrows(IllegalArgumentException.class,
                () -> Player.normalizeDisplayName(name + "a"));
    }

    @Test
    void rejectsEmptyAndInvalidText() {
        for (String value : new String[] { "", " \u00a0\u2003", "A\nB",
                "A\u0000B", "A\ud800B", "a".repeat(65) }) {
            assertThrows(IllegalArgumentException.class,
                    () -> Player.normalizeDisplayName(value), value);
        }
        assertThrows(NullPointerException.class,
                () -> Player.normalizeDisplayName(null));
    }

    @Test
    void rejectedChangePreservesDisplayNameAndNationIdentity() {
        final Player player = player();
        assertEquals("Dutch", player.getDisplayName());
        player.setDisplayName("Jan");
        assertThrows(IllegalArgumentException.class,
                () -> player.setDisplayName(" "));
        assertEquals("Jan", player.getDisplayName());
        assertEquals("Dutch", player.getName());
    }

    @Test
    void persistsDisplayNameWithoutRenamingOwnershipKey() {
        final WorldMap map = mock(WorldMap.class);
        when(model.getMap()).thenReturn(map);
        when(map.getMaxLocationX()).thenReturn(50);
        when(map.getMaxLocationY()).thenReturn(30);
        final Player player = player();
        player.setDisplayName("\u00a0Éva\u00a0");
        final PlayerPo saved = player.save();
        assertEquals("Dutch", saved.getName());
        final Player loaded = Player.make(saved, model, new PlayerStore());
        assertEquals("Éva", loaded.getDisplayName());
        assertEquals("Dutch", loaded.getName());
    }

    @Test
    void rejectsInvalidPersistedDisplayName() {
        for (Object value : new Object[] { "", "a".repeat(65), 42 }) {
            final HashMap<String, Object> data = new HashMap<>();
            data.put("displayName", value);
            assertThrows(IllegalArgumentException.class,
                    () -> new PlayerHuman("Dutch", false, 2108, model, false,
                            data, new HashSet<>()));
        }
    }
}
