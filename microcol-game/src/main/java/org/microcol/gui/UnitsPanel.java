package org.microcol.gui;

import java.util.List;

import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Text;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Display one unit description. Panel is placed on the right side of main
 * screen.
 */
public final class UnitsPanel {

    private final ImageProvider imageProvider;

    private final LocalizationHelper localizationHelper;

    private final Text text;

    private final SelectedUnitManager selectedUnitManager;

    private final VBox box;

    @Inject
    public UnitsPanel(final ImageProvider imageProvider, final EventBus eventBus,
            final LocalizationHelper localizationHelper, final Text text,
            final SelectedUnitManager selectedUnitManager) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.text = Preconditions.checkNotNull(text);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        box = new VBox();
        box.setOnMouseEntered(e -> {
            eventBus.post(
                    new StatusBarMessageEvent(text.get("unitsPanel.description"), Source.GAME));
        });
        box.getStyleClass().add("scroll-pane");
    }

    public void clear() {
        Platform.runLater(() -> {
            box.getChildren().clear();
        });
    }

    public void setUnits(final Player humanPlayer, final List<Unit> units) {
        Platform.runLater(() -> {
            for (final Unit unit : units) {
                final boolean selected = selectedUnitManager.getSelectedUnit().isPresent()
                        && selectedUnitManager.getSelectedUnit().get().equals(unit);
                final UnitPanel unitPanel = new UnitPanel(imageProvider, text, localizationHelper,
                        humanPlayer, unit, selected);
                box.getChildren().add(unitPanel.getBox());
                unitPanel.setOnMouseClicked(event -> {
                    selectedUnitManager.setSelectedUnit(unit);
                });

            }
            box.getChildren().add(new Label(""));
            box.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL_VIEW);
        });
    }

    public Node getNode() {
        return box;
    }

}
