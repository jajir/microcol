package org.microcol.gui.screen.europe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class QuickBuyShortcutTest {
    @Test
    void controlBuysExceptForTheExistingQuantityGesture() {
        assertTrue(QuickBuyShortcut.isQuickBuy(false, false, true, false));
        assertFalse(QuickBuyShortcut.isQuickBuy(false, false, false, false));
        assertFalse(QuickBuyShortcut.isQuickBuy(false, false, true, true));
        assertFalse(QuickBuyShortcut.isQuickBuy(false, true, false, false));
    }

    @Test
    void macRequiresCommandInsteadOfControl() {
        assertTrue(QuickBuyShortcut.isQuickBuy(true, true, false, false));
        assertFalse(QuickBuyShortcut.isQuickBuy(true, false, true, false));
        assertFalse(QuickBuyShortcut.isQuickBuy(true, false, false, false));
    }
}
