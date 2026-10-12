package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ColonyNamesTest {
    private final Model model = mock(Model.class);
    private final ColonyNames names = new ColonyNames(model);

    @Test
    void unicodeSpacesNormalizationAndCodePointLimits() {
        assertEquals("Café 🌊",
                ColonyNames.normalize("\u00a0 Cafe\u0301 🌊 \u2003"));
        assertEquals("A  B", ColonyNames.normalize(" A  B "));
        assertEquals(Optional.of(ColonyNameProblem.EMPTY),
                names.getProblem("\u00a0\u2003 ", null));
        assertTrue(names.getProblem("🌊".repeat(64), null).isEmpty());
        assertTrue(
                names.getProblem(" " + "A".repeat(64) + " ", null).isEmpty());
        assertEquals(Optional.of(ColonyNameProblem.TOO_LONG),
                names.getProblem("🌊".repeat(65), null));
        assertThrows(NullPointerException.class,
                () -> ColonyNames.normalize(null));
    }

    @Test
    void controlsAndUnpairedSurrogatesAreInvalid() {
        for (final String value : List.of("A\nB", "A\u0000B", "A\uD800",
                "A\uDC00")) {
            assertEquals(Optional.of(ColonyNameProblem.INVALID),
                    names.getProblem(value, null));
        }
    }

    @Test
    void duplicatesAreGlobalNormalizedAndCaseInsensitiveButExcludeSelf() {
        final Colony colony = mock(Colony.class);
        when(colony.getName()).thenReturn("Café");
        when(model.getColonies()).thenReturn(List.of(colony));
        assertEquals(Optional.of(ColonyNameProblem.DUPLICATE),
                names.getProblem(" CAFE\u0301 ", null));
        assertTrue(names.getProblem("Café", colony).isEmpty());
    }

    @Test
    void suggestionsDoNotRepeatOrMutateAndHaveNumberedFallback() {
        final Set<String> excluded = new HashSet<>();
        for (final String expected : List.of("New Amsterdam", "Delft",
                "Haarlem", "Leiden", "Rotterdam", "Utrecht", "Eindhoven",
                "Tilburg", "Groningen", "Almere Stad", "Breda")) {
            final Set<String> before = new HashSet<>(excluded);
            assertEquals(expected, names.suggest(excluded));
            assertEquals(before, excluded);
            excluded.add(expected);
        }
        final Colony colony = mock(Colony.class);
        when(colony.getName()).thenReturn("New Amsterdam 2");
        when(model.getColonies()).thenReturn(List.of(colony));
        excluded.add(" new AMSTERDAM 3 ");
        assertEquals("New Amsterdam 4", names.suggest(excluded));
        verify(colony, never()).setName(anyString());
    }
}
