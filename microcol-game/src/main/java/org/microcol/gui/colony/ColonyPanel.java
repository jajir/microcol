package org.microcol.gui.colony;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Show Europe port.
 */
@Listener
public final class ColonyPanel implements JavaFxComponent, UpdatableLanguage {

    private final static Logger logger = LoggerFactory.getLogger(ColonyPanel.class);

    private final StackPane mainPanel;

    private final Label colonyName;

    private final PanelColonyFields colonyFields;

    private final PanelColonyStructures colonyStructures;

    private final PanelColonyGoods goods;

    private final PanelDock panelDock;

    private final PaintService paintService;

    private final PanelOutsideColony panelOutsideColony;

    private final BooleanProperty propertyShiftWasPressed;

    private final PanelBuildingQueue panelBuildingQueue;
    
    private final I18n i18n;

    private Colony colony;

    @Inject
    public ColonyPanel(final ImageProvider imageProvider, final PanelColonyFields panelColonyFields,
            final PanelColonyStructures panelColonyStructures,
            final PanelOutsideColony panelOutsideColony, final PanelColonyGoods panelColonyGoods,
            final PanelColonyDockBehaviour panelColonyDockBehaviour,
            final PanelBuildingQueue panelBuildingQueue, final PaintService paintService,
            final I18n i18n) {
        this.paintService = Preconditions.checkNotNull(paintService);
        Preconditions.checkNotNull(imageProvider);
        this.colonyFields = Preconditions.checkNotNull(panelColonyFields);
        this.colonyStructures = Preconditions.checkNotNull(panelColonyStructures);
        this.panelBuildingQueue = Preconditions.checkNotNull(panelBuildingQueue);
        this.panelDock = new PanelDock(imageProvider,
                Preconditions.checkNotNull(panelColonyDockBehaviour));
        this.goods = Preconditions.checkNotNull(panelColonyGoods);
        this.panelOutsideColony = Preconditions.checkNotNull(panelOutsideColony);
        this.i18n = Preconditions.checkNotNull(i18n);
        colonyName = new Label();
        colonyName.getStyleClass().add("label-title");

        mainPanel = new StackPane();
        mainPanel.getChildren().add(colonyName);
        mainPanel.getChildren().add(panelDock.getContent());
        mainPanel.getChildren().add(panelOutsideColony.getContent());
        mainPanel.getChildren().add(colonyFields.getContent());
        mainPanel.getChildren().add(panelBuildingQueue.getContent());
        mainPanel.getChildren().add(colonyStructures.getContent());
        mainPanel.getChildren().add(goods.getContent());
        mainPanel.getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);

        /**
         * TODO there is a bug, keyboard events are not send during dragging.
         * TODO copy of this code is in EuropeDialog
         */
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        mainPanel.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                propertyShiftWasPressed.set(false);
            }
        });
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            logger.debug("wasShiftPressed " + event);
            if (event.getCode() == KeyCode.SHIFT) {
                propertyShiftWasPressed.set(true);
            }
        });
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

    public void showColony(final Colony colony) {
        this.colony = Preconditions.checkNotNull(colony);
        colonyName.setText(i18n.get(ColonyMsg.colony) + colony.getName());
        goods.setColony(colony);
        repaint();
    }

    public void paintUnit(final Canvas canvas, final Unit unit) {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        paintService.paintUnit(graphics, Point.CENTER, unit);
    }

    public void repaint() {
        if (colony != null) {
            colonyFields.setColony(colony);
            goods.repaint();
            panelDock.repaint();
            colonyStructures.repaint(colony);
            panelOutsideColony.setColony(colony);
            panelBuildingQueue.repaint();
        }
    }

    public Colony getColony() {
        return colony;
    }

    public BooleanProperty getPropertyShiftWasPressed() {
        return propertyShiftWasPressed;
    }

    public void close() {
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
