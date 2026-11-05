package org.akropia.gui;

import java.util.List;

import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.i18n.I18n;
import org.akropia.model.Player;
import org.akropia.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.akropia.gui.screen.game.components.ButtonsGamePanel;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Display one unit description. Panel is placed on the right side of main
 * screen.
 */
public final class UnitsPanel {

    private final ImageProvider imageProvider;

    private final LocalizationHelper localizationHelper;

    private final I18n i18n;

    private final SelectedUnitManager selectedUnitManager;

    private final VBox box;
    private final Provider<ButtonsGamePanel> gameButtons;

    @Inject
    public UnitsPanel(final ImageProvider imageProvider,
            final EventBus eventBus,
            final LocalizationHelper localizationHelper, final I18n i18n,
            final SelectedUnitManager selectedUnitManager,
            final Provider<ButtonsGamePanel> gameButtons) {
        this.gameButtons = gameButtons;
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        box = new VBox();
        box.setOnMouseEntered(e -> {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Loc.unitsPanel_description), Source.GAME));
        });
        box.getStyleClass().add("scroll-pane");
    }

    public void clear() {
        gameButtons.get().hideUnitActions();
        box.getChildren().clear();
    }

    public void setUnits(final Player humanPlayer, final List<Unit> units) {
        for (final Unit unit : units) {
            final boolean selected = selectedUnitManager.getSelectedUnit()
                    .isPresent()
                    && selectedUnitManager.getSelectedUnit().get().equals(unit);
            final UnitPanel unitPanel = new UnitPanel(imageProvider, i18n,
                    localizationHelper, humanPlayer, unit, selected);
            if (unit.getOwner().equals(humanPlayer)) {
                unitPanel.getBox().setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY
                            && !event.isControlDown()) {
                        selectedUnitManager.selectFromPanel(unit);
                        event.consume();
                    }
                });
            }
            if (selected && unit.getOwner().isHuman()) {
                gameButtons.get().addUnitStatus(unitPanel.getBox(), unit);
            }
            box.getChildren().add(unitPanel.getBox());
        }
        box.getChildren().add(new Label(""));
        box.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);
    }

    public Node getNode() {
        return box;
    }

}
