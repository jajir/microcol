package org.akropia.gui.screen.europe;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.input.MouseEvent;

/** Reads the quick-buy modifier from the actual drop event. */
@Singleton
public final class QuickBuyShortcut {
    private final boolean mac;

    @Inject
    QuickBuyShortcut() {
        this(System.getProperty("os.name", "").startsWith("Mac"));
    }

    QuickBuyShortcut(final boolean mac) {
        this.mac = mac;
    }

    public boolean isMac() {
        return mac;
    }

    public boolean isQuickBuy(final MouseEvent event) {
        return isQuickBuy(mac, event.isMetaDown(), event.isControlDown(),
                event.isShiftDown());
    }

    static boolean isQuickBuy(final boolean mac, final boolean meta,
            final boolean control, final boolean shift) {
        // Ctrl+Shift retains the existing quantity-choice gesture.
        return mac ? meta : control && !shift;
    }
}
