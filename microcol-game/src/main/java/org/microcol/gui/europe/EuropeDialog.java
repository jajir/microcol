package org.microcol.gui.europe;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Show Europe port dialog.
 */
public final class EuropeDialog extends AbstractMessageWindow implements EuropeCallback {

    private final static Logger logger = LoggerFactory.getLogger(EuropeDialog.class);

    final EuropePanel europePanel;

    @Inject
    public EuropeDialog(final ViewUtil viewUtil, final I18n i18n, final EuropePanel europePanel) {
        super(viewUtil, i18n);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        setTitle(i18n.getMessage(Europe.title));

        init(europePanel.getContent());
        addStyleSheet(EuropeMenuPanel.STYLE_SHEET_EUROPE);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);

        /**
         * TODO there is a bug, keyboard events are not send during dragging.
         * TODO copy of this code is n colonyDialog
         */
        getScene().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                getPropertyShiftWasPressed().set(false);
            }
        });
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            logger.debug("wasShiftPressed " + event);
            if (event.getCode() == KeyCode.SHIFT) {
                getPropertyShiftWasPressed().set(true);
            }
        });
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        europePanel.updateLanguage(i18n);
    }

    public void show() {
        repaint();
        showAndWait();
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

}
