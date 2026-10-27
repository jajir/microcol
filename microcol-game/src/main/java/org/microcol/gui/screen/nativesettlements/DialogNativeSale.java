package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.Loc;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeSaleQuote;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation of one native sale or gift (R-0160). It shows the exact
 * goods, pieces and gold of the terms the player was offered. Back, Escape or
 * closing the window changes nothing. Confirm only records the choice; the
 * caller runs the model operation after this window has closed, so no model
 * call and no second window open inside a button handler.
 */
public final class DialogNativeSale extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeSaleContent";

    public static final String LINE_ID = "nativeSaleLine";

    public static final String CONFIRM_BUTTON_ID = "nativeSaleConfirm";

    public static final String BACK_BUTTON_ID = "nativeSaleBack";

    private final NativeSaleQuote offered;

    private final Label line = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.SELL);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private boolean confirmed;

    /**
     * @param offered the exact sale or gift terms the player is confirming
     */
    public DialogNativeSale(final ViewUtil viewUtil, final I18n i18n,
            final NativeSaleQuote offered) {
        super(viewUtil, i18n);
        this.offered = offered;
        line.setId(LINE_ID);
        line.setWrapText(true);
        confirm.setId(CONFIRM_BUTTON_ID);
        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> {
            confirmed = true;
            close();
        });
        back.setId(BACK_BUTTON_ID);
        back.setCancelButton(true);
        back.setOnAction(e -> close());
        final HBox buttons = new HBox(10, back, confirm);
        final VBox root = new VBox(10, line, buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        // Handle Escape before the focused button consumes it.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
        init(root);
    }

    /**
     * @return whether the player chose Confirm; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves the terms unchanged and
     * Confirm needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeSaleConfirmTitle));
        final String goods = i18n
                .get(GoodsTypeName.getNameForGoodsType(offered.getGoods()));
        line.setText(offered.isGift()
                ? i18n.get(Loc.nativeSalePreviewGift, offered.getQuantity(),
                        goods)
                : i18n.get(Loc.nativeSalePreviewSale, offered.getQuantity(),
                        goods, offered.getGold()));
        confirm.setText(i18n.get(offered.isGift() ? Loc.nativeSaleConfirmGift
                : Loc.nativeSaleConfirmSale));
        back.setText(i18n.get(Loc.nativeSaleConfirmBack));
    }

}
