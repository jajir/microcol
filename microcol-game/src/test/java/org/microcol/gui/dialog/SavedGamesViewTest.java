package org.microcol.gui.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** A long save name is shortened only in its list row (R-0383). */
class SavedGamesViewTest {

    @Test
    void rowNamesKeepShortNamesAndShortenLongOnesByCodePoints() {
        assertEquals("Přístav / před bitvou",
                SavedGamesView.shortened("Přístav / před bitvou"));
        final String limit = "a".repeat(SavedGamesView.ROW_NAME_LENGTH);
        assertEquals(limit, SavedGamesView.shortened(limit));
        final String row = SavedGamesView.shortened("😀".repeat(64));
        assertEquals(SavedGamesView.ROW_NAME_LENGTH,
                row.codePointCount(0, row.length()));
        assertEquals("…", row.substring(row.length() - 1));
    }
}
