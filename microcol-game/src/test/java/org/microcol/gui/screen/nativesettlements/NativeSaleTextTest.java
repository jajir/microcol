package org.microcol.gui.screen.nativesettlements;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.gui.Loc;
import org.microcol.gui.util.StreamReader;
import org.microcol.i18n.I18n;

/**
 * Every native sale and purchase text is formatted by I18n (R-0160, R-0161), so
 * a literal percent must be escaped and the previews must accept their
 * arguments. The visit chooser builds these texts whenever it opens, so a bad
 * format would break every chooser, teaching included.
 */
class NativeSaleTextTest {

    /**
     * Sale and purchase texts that take arguments; every other one takes none.
     */
    private static final Set<Loc> WITH_ARGUMENTS = EnumSet.of(
            Loc.nativeSalePreviewSale, Loc.nativeSalePreviewGift,
            Loc.nativePurchaseOfferLine, Loc.nativePurchasePreview,
            Loc.nativePurchaseBought);

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    @Test
    void everySaleTextWithoutArgumentsFormatsInBothLanguages() {
        for (final Locale locale : new Locale[] { Locale.ROOT,
                new Locale("cs", "CZ") }) {
            final I18n i18n = i18n(locale);
            for (final Loc key : Loc.values()) {
                if ((key.name().startsWith("nativeSale")
                        || key.name().startsWith("nativePurchase")
                        || key == Loc.nativeVisitIntro)
                        && !WITH_ARGUMENTS.contains(key)) {
                    final String text = assertDoesNotThrow(() -> i18n.get(key),
                            () -> locale + " " + key);
                    assertFalse(text.isBlank(), () -> locale + " " + key);
                }
            }
        }
    }

    @Test
    void haggleTextsShowOneLiteralTenPercent() {
        assertEquals("Haggle once (+10%)",
                i18n(Locale.ROOT).get(Loc.nativeSaleHaggleButton));
        assertEquals(
                "Haggle: the natives accepted, so the price includes the +10%.",
                i18n(Locale.ROOT).get(Loc.nativeSaleHaggleAccepted));
        assertEquals("Smlouvat jednou (+10 %)",
                i18n(new Locale("cs", "CZ")).get(Loc.nativeSaleHaggleButton));
        assertEquals("Smlouvání: domorodci souhlasili, cena včetně +10 %.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativeSaleHaggleAccepted));
    }

    @Test
    void purchaseDiscountTextsShowOneLiteralTenPercent() {
        assertEquals("Ask for 10% off (once)",
                i18n(Locale.ROOT).get(Loc.nativePurchaseHaggleButton));
        assertEquals(
                "Discount: the natives accepted, so the price is 10% lower.",
                i18n(Locale.ROOT).get(Loc.nativePurchaseHaggleAccepted));
        assertEquals("Požádat o slevu 10 % (jednou)",
                i18n(new Locale("cs", "CZ"))
                        .get(Loc.nativePurchaseHaggleButton));
        assertEquals("Sleva: domorodci souhlasili, cena je o 10 % nižší.",
                i18n(new Locale("cs", "CZ"))
                        .get(Loc.nativePurchaseHaggleAccepted));
    }

    @Test
    void purchaseTextsTakeTheirArgumentsInBothLanguages() {
        assertEquals("Buy Corn: 100 in stock at 4 gold each.", i18n(Locale.ROOT)
                .get(Loc.nativePurchaseOfferLine, "Corn", 100, 4));
        assertEquals("Buy 5 pieces of Corn for 9 gold.",
                i18n(Locale.ROOT).get(Loc.nativePurchasePreview, 5, "Corn", 9));
        assertEquals("You bought 5 pieces of Corn for 9 gold.",
                i18n(Locale.ROOT).get(Loc.nativePurchaseBought, 5, "Corn", 9));
        assertEquals("Koupit Jídlo: skladem 100, za 4 zlata za kus.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativePurchaseOfferLine,
                        "Jídlo", 100, 4));
        assertEquals("Koupíte 5 kusů zboží Jídlo za 9 zlata.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativePurchasePreview, 5,
                        "Jídlo", 9));
        assertEquals("Koupili jste 5 kusů zboží Jídlo za 9 zlata.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativePurchaseBought, 5,
                        "Jídlo", 9));
    }

    @Test
    void previewsTakeTheirArgumentsInBothLanguages() {
        assertEquals("Sell 50 pieces of Corn for 112 gold.", i18n(Locale.ROOT)
                .get(Loc.nativeSalePreviewSale, 50, "Corn", 112));
        assertEquals("Give 50 pieces of Corn as a gift. No gold is paid.",
                i18n(Locale.ROOT).get(Loc.nativeSalePreviewGift, 50, "Corn"));
        assertEquals("Prodáte 50 kusů zboží Jídlo za 112 zlata.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativeSalePreviewSale, 50,
                        "Jídlo", 112));
        assertEquals("Darujete 50 kusů zboží Jídlo. Žádné zlato se neplatí.",
                i18n(new Locale("cs", "CZ")).get(Loc.nativeSalePreviewGift, 50,
                        "Jídlo"));
    }

}
