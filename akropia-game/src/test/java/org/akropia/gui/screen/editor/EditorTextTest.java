package org.akropia.gui.screen.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.Location;
import org.akropia.model.generation.ScenarioProblem;
import org.junit.jupiter.api.Test;

/**
 * Editor texts are formatted by I18n with {@link String#format}, so every
 * argument of a parameterized text must appear in both languages and no other
 * placeholder style may remain (R-0435).
 */
class EditorTextTest {

    private static final Locale CZECH = new Locale("cs", "CZ");

    /** Number of arguments each parameterized text receives. */
    private static final Map<EditorText, Integer> ARGUMENTS = Map.ofEntries(
            Map.entry(EditorText.titleSaved, 3),
            Map.entry(EditorText.titleModified, 3),
            Map.entry(EditorText.tile, 3), Map.entry(EditorText.unitLabel, 2),
            Map.entry(EditorText.passengers, 2),
            Map.entry(EditorText.newBounds, 2),
            Map.entry(EditorText.created, 2), Map.entry(EditorText.opened, 1),
            Map.entry(EditorText.saved, 1), Map.entry(EditorText.openFailed, 2),
            Map.entry(EditorText.saveFailed, 1),
            Map.entry(EditorText.changed, 2), Map.entry(EditorText.rejected, 1),
            Map.entry(EditorText.playRejected, 1),
            Map.entry(EditorText.playFailed, 1),
            Map.entry(EditorText.problemAt, 3),
            Map.entry(EditorText.problemDetail, 2),
            Map.entry(EditorText.reason_DIMENSION_OUT_OF_RANGE, 2));

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    @Test
    void everyArgumentAppearsAndNoPlaceholderRemainsInBothLanguages() {
        for (final Locale locale : new Locale[] { Locale.ROOT, CZECH }) {
            final I18n i18n = i18n(locale);
            for (final EditorText key : EditorText.values()) {
                final int count = ARGUMENTS.getOrDefault(key, 0);
                final Object[] arguments = new Object[count];
                for (int i = 0; i < count; i++) {
                    arguments[i] = "ARG" + i;
                }
                final String text = i18n.get(key, arguments);
                assertFalse(text.isBlank(), () -> locale + " " + key);
                assertFalse(text.matches("(?s).*\\{\\d\\}.*"),
                        () -> locale + " " + key + ": " + text);
                assertFalse(text.contains("%"),
                        () -> locale + " " + key + ": " + text);
                for (int i = 0; i < count; i++) {
                    assertTrue(text.contains("ARG" + i), locale + " " + key
                            + " lacks argument " + i + ": " + text);
                }
            }
        }
    }

    @Test
    void titlesAndBoundsReadAsSentences() {
        final I18n english = i18n(Locale.ROOT);
        final I18n czech = i18n(CZECH);
        assertEquals("Unsaved scenario, 40 × 30 squares",
                english.get(EditorText.titleSaved,
                        english.get(EditorText.untitled), 40, 30));
        assertEquals("Neuložený scénář, 12 × 10 polí, neuložené změny",
                czech.get(EditorText.titleModified,
                        czech.get(EditorText.untitled), 12, 10));
        assertEquals("Width and height must be from 10 to 100 squares.",
                english.get(EditorText.reason_DIMENSION_OUT_OF_RANGE, 10, 100));
        assertEquals("Caravel #12",
                english.get(EditorText.unitLabel, "Caravel", 12));
    }

    @Test
    void problemsNameTheirSquareInBothLanguages() {
        final ScenarioProblem problem = new ScenarioProblem(
                ScenarioProblem.Reason.NO_CAPITAL, Location.of(4, 4),
                "Scythians");
        final I18n english = i18n(Locale.ROOT);
        assertEquals(
                "A tribe with settlements needs a capital. (Scythians) [4, 4]",
                new EditorMessages(english, new LocalizationHelper(english))
                        .describe(problem));
        final I18n czech = i18n(CZECH);
        assertEquals(
                "Kmen s osadami potřebuje hlavní město. (Scythians) [4, 4]",
                new EditorMessages(czech, new LocalizationHelper(czech))
                        .describe(problem));
    }

}
