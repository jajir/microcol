package org.microcol.gui.screen.colony;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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

    private Colony colony;

    @Inject
    PanelColonyName(final EventBus eventBus, final I18n i18n,
            final GameModelController controller,
            final DialogColonyName dialog) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);

        label.setId(COLONY_NAME_ID);
        label.setMaxWidth(380);
        label.getStyleClass().add("label-title");
        label.setOnMouseEntered(this::onMouseEntered);
        label.setOnMouseExited(this::onMouseExited);
        label.setFocusTraversable(true);
        label.setCursor(Cursor.HAND);
        label.setAccessibleText(i18n.get(ColonyMsg.nameRename));
        final Runnable rename = () -> {
            if (controller.getModel()
                    .canRenameColony(controller.getHumanPlayer(), colony)) {
                dialog.showForRename(controller.getModel(),
                        controller.getHumanPlayer(), colony);
                setColony(colony);
                label.requestFocus();
            }
        };
        label.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                rename.run();
            }
        });
        label.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    || event.getCode() == KeyCode.SPACE) {
                rename.run();
                event.consume();
            }
        });
    }

    private void onMouseEntered(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(ColonyMsg.colonyName, colony.getName()),
                Source.COLONY));
    }

    private void onMouseExited(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    void setColony(final Colony colony) {
        this.colony = colony;
        label.setText(colony.getName());
        label.setTooltip(new Tooltip(
                colony.getName() + " — " + i18n.get(ColonyMsg.nameRename)));
    }

    @Override
    public Region getContent() {
        return label;
    }

}
