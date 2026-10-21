package org.microcol.gui.screen.menu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;
import org.microcol.i18n.I18n;

/**
 * Every menu text is formatted by I18n, so text without arguments must be a
 * valid format with none, and the generated-world preview must accept its
 * arguments. The setup screen builds these texts while the application starts.
 */
class GameMenuTextTest {

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    @Test
    void everyTextWithoutArgumentsFormatsInBothLanguages() {
        for (final Locale locale : new Locale[] { Locale.ROOT,
                new Locale("cs", "CZ") }) {
            final I18n i18n = i18n(locale);
            for (final GameMenu key : GameMenu.values()) {
                if (key != GameMenu.setupGeneratedPreview) {
                    final String text = assertDoesNotThrow(() -> i18n.get(key),
                            () -> locale + " " + key);
                    assertFalse(text.isBlank(), () -> locale + " " + key);
                }
            }
        }
    }

    @Test
    void landShareLabelsShowALiteralPercent() {
        assertEquals("Low (25 %)",
                i18n(Locale.ROOT).get(GameMenu.setupLandLow));
        assertEquals("Hodně (55 %)",
                i18n(new Locale("cs", "CZ")).get(GameMenu.setupLandHigh));
    }

    @Test
    void generatedPreviewTakesItsArgumentsInBothLanguages() {
        assertEquals(
                "Preview: map 50 × 30 squares, Cold climate, land 55 %, seed 2024.",
                i18n(Locale.ROOT).get(GameMenu.setupGeneratedPreview, 50, 30,
                        "Cold", 55, "2024"));
        assertEquals(
                "Náhled: mapa 50 × 30 polí, podnebí Mírné, pevnina 40 %, semínko 1.",
                i18n(new Locale("cs", "CZ")).get(GameMenu.setupGeneratedPreview,
                        50, 30, "Mírné", 40, "1"));
    }

}
