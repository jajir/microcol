package org.microcol.gui.screen.colony;

import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Panel with colon name.
 */
@Singleton
public class PanelColonyName implements JavaFxComponent {

    public static final String COLONY_NAME_ID = "colonyName";

    private final Label label = new Label();
    private final EventBus eventBus;
    private final I18n i18n;

    private String colonyName;

    @Inject
    PanelColonyName(final EventBus eventBus, final I18n i18n) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);

        label.setId(COLONY_NAME_ID);
        label.getStyleClass().add("label-title");
        label.setOnMouseEntered(this::onMouseEntered);
        label.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(ColonyMsg.colonyName, colonyName),
                Source.COLONY));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    void setColonyName(final String colonyName) {
        this.colonyName = colonyName;
        label.setText(colonyName);
    }

    @Override
    public Region getContent() {
        return label;
    }

}
