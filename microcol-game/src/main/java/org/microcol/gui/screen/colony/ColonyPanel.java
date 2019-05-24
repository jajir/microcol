package org.microcol.gui.screen.colony;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Show Europe port.
 */
@Listener
public final class ColonyPanel implements JavaFxComponent, UpdatableLanguage {

    public static final String COLONY_NAME_ID = "colonyName";

    private final StackPane mainPanel;

    private final Label colonyName;

    private final PanelColonyFields colonyFields;

    private final PanelColonyStructures colonyStructures;

    private final PanelColonyGoods goods;

    private final PanelDockColony panelDock;

    private final PanelOutsideColony panelOutsideColony;

    private final PanelBuildingQueue panelBuildingQueue;

    private final I18n i18n;

    private Colony colony;

    @Inject
    public ColonyPanel(final PanelColonyFields panelColonyFields,
            final PanelColonyStructures panelColonyStructures,
            final PanelOutsideColony panelOutsideColony, final PanelColonyGoods panelColonyGoods,
            final PanelBuildingQueue panelBuildingQueue,
            final ColonyButtonsPanel colonyButtonsPanel, final I18n i18n,
            final PanelDockColony panelDockColony) {
        this.colonyFields = Preconditions.checkNotNull(panelColonyFields);
        this.colonyStructures = Preconditions.checkNotNull(panelColonyStructures);
        this.panelBuildingQueue = Preconditions.checkNotNull(panelBuildingQueue);
        this.panelDock = Preconditions.checkNotNull(panelDockColony);
        this.goods = Preconditions.checkNotNull(panelColonyGoods);
        this.panelOutsideColony = Preconditions.checkNotNull(panelOutsideColony);
        this.i18n = Preconditions.checkNotNull(i18n);
        colonyName = new Label();
        colonyName.setId(COLONY_NAME_ID);
        colonyName.getStyleClass().add("label-title");

        mainPanel = new StackPane();
        mainPanel.getChildren().add(colonyName);
        mainPanel.getChildren().add(panelDock.getContent());
        mainPanel.getChildren().add(panelOutsideColony.getContent());
        mainPanel.getChildren().add(colonyFields.getContent());
        mainPanel.getChildren().add(panelBuildingQueue.getContent());
        mainPanel.getChildren().add(colonyStructures.getContent());
        mainPanel.getChildren().add(goods.getContent());
        mainPanel.getChildren().add(colonyButtonsPanel.getContent());
        mainPanel.getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Subscribe
    private void onUnitMovedOutsideColony(
            @SuppressWarnings("unused") final UnitMovedOutsideColonyEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToConstruction(final UnitMovedToConstructionEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToField(final UnitMovedToColonyFieldEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onRepainColonyEvent(final RepaintColonyEvent event) {
        repaint();
    }

    /**
     * Method should show given colony.
     * 
     * @param colony
     *            required colony to show
     */
    void showColony(final Colony colony) {
        this.colony = Preconditions.checkNotNull(colony);
        colonyName.setText(i18n.get(ColonyMsg.colony) + colony.getName());
        goods.setColony(colony);
        repaint();
    }

    void repaint() {
        if (colony != null) {
            colonyFields.setColony(colony);
            goods.repaint();
            panelDock.repaint();
            colonyStructures.repaint(colony);
            panelOutsideColony.setColony(colony);
            panelOutsideColony.repaint();
            panelBuildingQueue.repaint();
        }
    }

    public Colony getColony() {
        return colony;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        panelDock.updateLanguage(i18n);
    }
}
