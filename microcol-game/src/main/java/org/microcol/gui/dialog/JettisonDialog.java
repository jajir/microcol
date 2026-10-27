package org.microcol.gui.dialog;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.JettisonOffer;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Lets the player pick one goods hold and confirm the exact jettison. Cancel
 * and Escape return nothing and change nothing.
 */
final class JettisonDialog extends AbstractMessageWindow {

    private final I18n i18n;

    private boolean confirmed;

    private Optional<JettisonOffer> chosen = Optional.empty();

    @Inject
    JettisonDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * @param shipName name of the ship shown to the player
     * @param offers   one offer for each goods hold, at least two
     * @return the hold the player picked, empty when canceled
     */
    Optional<JettisonOffer> chooseHold(final String shipName,
            final List<JettisonOffer> offers) {
        chosen = Optional.empty();
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        final Label title = new Label(i18n.get(Jettison.holdTitle));
        title.setId("jettisonHoldTitle");
        final Label question = new Label(
                i18n.get(Jettison.holdQuestion, shipName));
        question.setId("jettisonHoldQuestion");
        question.setWrapText(true);
        root.getChildren().addAll(title, question);
        for (final JettisonOffer offer : offers) {
            final Button hold = ActionButtons.button(ButtonIcon.CARGO, i18n.get(
                    Jettison.holdButton, offer.getSlotIndex() + 1,
                    offer.getGoods().getAmount(), i18n.get(GoodsTypeName
                            .getNameForGoodsType(offer.getGoods().getType()))));
            hold.setId("jettisonHold-" + (offer.getSlotIndex() + 1));
            hold.setOnAction(event -> {
                chosen = Optional.of(offer);
                close();
            });
            root.getChildren().add(hold);
        }
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Dialog.cancel));
        cancel.setId("jettisonCancel");
        cancel.setOnAction(event -> onCancelDialog());
        root.getChildren().add(cancel);
        init(root);
        setTitle(i18n.get(Jettison.holdTitle));
        showAndWait();
        return chosen;
    }

    /**
     * @param shipName name of the ship shown to the player
     * @param offer    exact hold and goods the player was offered
     * @param amount   quantity the player chose from one through the held goods
     * @return <code>true</code> only when the player explicitly confirmed
     */
    boolean confirm(final String shipName, final JettisonOffer offer,
            final int amount) {
        confirmed = false;
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setPrefWidth(520);
        final Label title = new Label(i18n.get(Jettison.title));
        title.setId("jettisonTitle");
        final Label question = new Label(i18n.get(Jettison.question, amount,
                i18n.get(GoodsTypeName
                        .getNameForGoodsType(offer.getGoods().getType())),
                offer.getSlotIndex() + 1, shipName));
        question.setId("jettisonQuestion");
        question.setWrapText(true);
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Jettison.confirm), i18n.get(Dialog.cancel));
        buttons.getButtonYes().setId("jettisonConfirm");
        buttons.getButtonNo().setId("jettisonCancel");
        buttons.getButtonYes().setOnAction(event -> {
            confirmed = true;
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(title, question, buttons.getContent());
        init(root);
        setTitle(i18n.get(Jettison.title));
        showAndWait();
        return confirmed;
    }

    @Override
    protected void onCancelDialog() {
        confirmed = false;
        chosen = Optional.empty();
        close();
    }
}
