package org.microcol.gui.colony;

import org.microcol.gui.Dialog;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
@Listener
public final class ColonyPanel implements JavaFxComponent, UpdatableLanguage {

    private final static Logger logger = LoggerFactory.getLogger(ColonyPanel.class);

    private final VBox mainPanel;

    private final Label colonyName;

    private final PanelColonyFields colonyFields;

    private final PanelColonyStructures colonyStructures;

    private final PanelColonyGoods goods;

    private final PanelDock panelDock;

    private final PaintService paintService;

    private final PanelOutsideColony panelOutsideColony;

    private final BooleanProperty propertyShiftWasPressed;

    private final PanelQueueSummary panelQueueSummary;

    private Colony colony;

    @Inject
    public ColonyPanel(final I18n i18n, final ImageProvider imageProvider,
            final PanelColonyFields panelColonyFields,
            final PanelColonyStructures panelColonyStructures,
            final PanelOutsideColony panelOutsideColony, final PanelColonyGoods panelColonyGoods,
            final PanelColonyDockBehaviour panelColonyDockBehaviour,
            final UnitMovedOutsideColonyController unitMovedOutsideColonyController,
            final PanelQueueSummary panelQueueSummary, final PaintService paintService,
            final ColonyDialogCallback colonyDialogCallback) {
        this.paintService = Preconditions.checkNotNull(paintService);
        Preconditions.checkNotNull(imageProvider);

        /**
         * Row 0
         */
        colonyName = new Label("Colony: ");
        colonyName.getStyleClass().add("label-title");

        /**
         * Row 1
         */
        colonyFields = Preconditions.checkNotNull(panelColonyFields);
        colonyStructures = Preconditions.checkNotNull(panelColonyStructures);

        this.panelQueueSummary = Preconditions.checkNotNull(panelQueueSummary);
        final VBox boxFields = new VBox();
        boxFields.getChildren().addAll(colonyFields.getContent(), panelQueueSummary.getContent());

        final HBox mapAndBuildings = new HBox();
        mapAndBuildings.getChildren().addAll(colonyStructures.getContent(), boxFields);

        /**
         * Row 2
         */
        final PanelProductionSummary panelProductionSummary = new PanelProductionSummary();

        panelDock = new PanelDock(imageProvider,
                Preconditions.checkNotNull(panelColonyDockBehaviour));

        this.panelOutsideColony = Preconditions.checkNotNull(panelOutsideColony);

        final HBox managementRow = new HBox();
        managementRow.getChildren().addAll(panelProductionSummary.getContent(),
                panelDock.getContent(), panelOutsideColony.getContent());

        /**
         * Good row - 3
         */
        goods = Preconditions.checkNotNull(panelColonyGoods);

        /**
         * Last row 4
         */
        final Button buttonOk = new Button(i18n.get(Dialog.ok));
        buttonOk.setOnAction(e -> {
            colonyDialogCallback.close();
        });
        buttonOk.requestFocus();

        mainPanel = new VBox();
        mainPanel.getChildren().addAll(colonyName, mapAndBuildings, managementRow,
                goods.getContent(), buttonOk);
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
        unitMovedOutsideColonyController.addListener(event -> repaint());
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
        colonyName.setText("Colony: " + colony.getName());
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
            panelQueueSummary.repaint();
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
