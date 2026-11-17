package org.akropia.gui.screen.colony;

import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Closing the confirmation, including Escape, leaves the route untouched. */
final class TradeRouteDeleteConfirmation extends AbstractMessageWindow {
    private boolean confirmed;
    private final I18n i18n;

    TradeRouteDeleteConfirmation(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    boolean confirm(final String question) {
        final Label label = new Label(question);
        label.setId("tradeRouteDeleteQuestion");
        label.setWrapText(true);
        label.setMaxWidth(520);
        final Button delete = ActionButtons.button(ButtonIcon.DELETE,
                i18n.get(TradeRouteText.deleteRoute));
        delete.setId("tradeRouteDeleteConfirm");
        delete.setOnAction(event -> {
            confirmed = true;
            close();
        });
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(org.akropia.gui.dialog.Dialog.cancel));
        cancel.setId("tradeRouteDeleteCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        final VBox root = new VBox(16, label, new ModalActions(delete, cancel));
        root.setId("tradeRouteDeleteDialog");
        init(root);
        cancel.requestFocus();
        showAndWait();
        return confirmed;
    }
}
