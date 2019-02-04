package org.microcol.gui.colony;

import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.model.Colony;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ColonyMenuPanelPresenter implements ColonyDialogCallback {

    private final static Logger logger = LoggerFactory.getLogger(ColonyMenuPanelPresenter.class);

    private final ColonyPanel colonyPanel;

    private final EventBus eventBus;

    @Inject
    public ColonyMenuPanelPresenter(final ColonyPanel colonyPanel, final EventBus eventBus) {
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        colonyPanel.getContent().addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        logger.debug("key pressed " + event.getCharacter());
        if (KeyCode.ESCAPE == event.getCode()) {
            close();
        }
    }

    @Override
    public void repaint() {
        colonyPanel.repaint();
    }

    @Override
    public void close() {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    @Override
    public Colony getColony() {
        return colonyPanel.getColony();
    }

    @Override
    public BooleanProperty getPropertyShiftWasPressed() {
        return colonyPanel.getPropertyShiftWasPressed();
    }

    @Override
    public void paintUnit(Canvas canvas, Unit unit) {
        colonyPanel.paintUnit(canvas, unit);
    }

}