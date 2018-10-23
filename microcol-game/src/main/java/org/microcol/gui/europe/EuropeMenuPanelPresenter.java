package org.microcol.gui.europe;

import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class EuropeMenuPanelPresenter implements EuropeCallback {

    private final static Logger logger = LoggerFactory.getLogger(EuropeMenuPanelPresenter.class);

    private final EventBus eventBus;

    private final EuropePanel europePanel;

    @Inject
    EuropeMenuPanelPresenter(final EuropeMenuPanel europeMenuPanel, final EventBus eventBus,
            final EuropePanel europePanel) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        europeMenuPanel.getContent().addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        logger.debug("key pressed " + event.getCharacter());
        if (KeyCode.ESCAPE == event.getCode()) {
            close();
        }
    }

    @Override
    public void repaint() {
        europePanel.repaint();
    }

    @Override
    public void repaintAfterGoodMoving() {
        europePanel.repaintAfterGoodMoving();
    }

    @Override
    public final BooleanProperty getPropertyShiftWasPressed() {
        return europePanel.getPropertyShiftWasPressed();
    }

    @Override
    public void close() {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }
}
