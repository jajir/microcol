package org.microcol.gui.screen.colony;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.QuickCargoAction;
import org.microcol.model.QuickCargoOffer;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation for the exact, model-provided terms of one quick load or unload.
 */
final class DialogQuickCargo extends AbstractMessageWindow {

    private final I18n i18n;

    private boolean confirmed;

    @Inject
    DialogQuickCargo(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param offer terms to confirm
     * @return <code>true</code> only when the player explicitly confirmed
     */
    boolean confirm(final QuickCargoOffer offer) {
        confirmed = false;
        final boolean loading = offer.getAction() == QuickCargoAction.LOAD;
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setPrefWidth(520);
        final String title = i18n
                .get(loading ? ColonyMsg.quickLoad : ColonyMsg.quickUnload);
        final Label titleLabel = new Label(title);
        titleLabel.setId("quickCargoTitle");
        final Label question = new Label(i18n.get(
                loading ? ColonyMsg.quickLoadQuestion
                        : ColonyMsg.quickUnloadQuestion,
                offer.getGoods().getAmount(),
                i18n.get(GoodsTypeName
                        .getNameForGoodsType(offer.getGoods().getType())),
                offer.getSlotIndex() + 1));
        question.setId("quickCargoQuestion");
        question.setWrapText(true);
        question.setMinHeight(Region.USE_PREF_SIZE);
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(ColonyMsg.quickConfirm),
                i18n.get(ColonyMsg.quickCancel));
        buttons.getButtonYes().setId("quickCargoConfirm");
        buttons.getButtonNo().setId("quickCargoCancel");
        buttons.getButtonYes().setOnAction(event -> {
            confirmed = true;
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(titleLabel, question, buttons.getContent());
        init(root);
        setTitle(title);
        showAndWait();
        return confirmed;
    }

    @Override
    protected void onCancelDialog() {
        confirmed = false;
        close();
    }

}
