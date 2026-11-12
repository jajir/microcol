package org.akropia.model;

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
    void baseSuggestionsSkipExistingColoniesAndNormalizedExclusions() {
        final Colony colony = mock(Colony.class);
        when(colony.getName()).thenReturn(" MASSALIA ");
        when(model.getColonies()).thenReturn(List.of(colony));
        assertEquals("Alalia", names.suggest(Set.of("\u2003eMPORION\u00a0")));
    }

    @Test
    void suggestionsDoNotRepeatOrMutateAndHaveNumberedFallback() {
        final Set<String> excluded = new HashSet<>();
        for (final String expected : List.of("Massalia", "Emporion", "Alalia",
                "Agathe", "Antipolis", "Nikaia", "Tauroeis", "Rhode",
                "Neapolis", "Kyme", "Pithekoussai", "Taras", "Sybaris",
                "Kroton", "Rhegion", "Poseidonia", "Metapontion", "Elea",
                "Thourioi", "Kaulonia", "Syrakousai", "Naxos", "Zankle",
                "Selinous", "Akragas", "Himera", "Kamarina", "Leontinoi",
                "Katane", "Megara Hyblaia", "Akrai", "Kasmenai", "Heloros",
                "Mylai", "Lipara", "Tauromenion", "Korkyra", "Ambrakia",
                "Leukas", "Anaktorion", "Epidamnos", "Apollonia", "Issa",
                "Pharos", "Tragyrion", "Epetion", "Ankon", "Abdera", "Maroneia",
                "Amphipolis", "Stageira", "Akanthos", "Potidaia", "Mende",
                "Torone", "Olynthos", "Aphytis", "Sermyle", "Sane", "Argilos",
                "Eion", "Byzantion", "Kalchedon", "Selymbria", "Perinthos",
                "Bisanthe", "Kardia", "Abydos", "Lampsakos", "Kyzikos",
                "Parion", "Kios", "Sinope", "Amisos", "Trapezous", "Kerasous",
                "Kotyora", "Kytoros", "Tieion", "Herakleia Pontike", "Istros",
                "Tomis", "Kallatis", "Odessos", "Mesembria", "Olbia", "Tyras",
                "Borysthenes", "Nikonion", "Pantikapaion", "Theodosia",
                "Chersonesos", "Nymphaion", "Phanagoria", "Hermonassa",
                "Kyrene", "Barke", "Euesperides", "Taucheira", "Naukratis")) {
            final Set<String> before = new HashSet<>(excluded);
            assertEquals(expected, names.suggest(excluded));
            assertEquals(before, excluded);
            excluded.add(expected);
        }
        assertEquals(100, excluded.size());
        assertEquals("Massalia 2", names.suggest(excluded));
        for (final String name : List.copyOf(excluded)) {
            excluded.add(name + " 2");
        }
        assertEquals("Massalia 3", names.suggest(excluded));
        final Colony colony = mock(Colony.class);
        when(colony.getName()).thenReturn("Massalia 3");
        when(model.getColonies()).thenReturn(List.of(colony));
        excluded.add(" emPORION 3 ");
        assertEquals("Alalia 3", names.suggest(excluded));
        verify(colony, never()).setName(anyString());
    }
}
