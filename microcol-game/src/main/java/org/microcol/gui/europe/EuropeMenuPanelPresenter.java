package org.microcol.gui.europe;

import org.microcol.gui.MainPanelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class EuropeMenuPanelPresenter implements EuropeCallback {

    private final static Logger logger = LoggerFactory.getLogger(EuropeMenuPanelPresenter.class);

    private final MainPanelView mainPanelView;

    private final EuropePanel europePanel;

    @Inject
    EuropeMenuPanelPresenter(final EuropeMenuPanel europeMenuPanel,
            final MainPanelView mainPanelView, final EuropePanel europePanel) {
        this.mainPanelView = Preconditions.checkNotNull(mainPanelView);
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
        mainPanelView.showGamePanel();
    }
}
