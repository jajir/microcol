package org.microcol.gui.util;

import java.util.List;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;

import javafx.scene.layout.HBox;

public final class PanelDockCratesView extends HBox {

    final static int MAX_NUMBER_OF_CRATES = 6;

    PanelDockCratesView(final ImageProvider imageProvider,
            final PanelDockBehavior panelDockBehavior, final I18n i18n, final EventBus eventBus, final Source source) {
        for (int i = 0; i < MAX_NUMBER_OF_CRATES; i++) {
            final PanelDockCrate paneCrate = new PanelDockCrate(imageProvider, panelDockBehavior,
                    i18n, eventBus, source);
            getChildren().add(paneCrate);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    List<PanelDockCrate> getCrates() {
        return (List) getChildren();
    }
}
