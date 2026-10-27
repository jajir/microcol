package org.microcol.gui.screen.europe;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Player;
import org.microcol.model.RecruitmentOffer;
import org.microcol.model.EarnedImmigrationOffer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/** One of the model's three saved candidates, with its passage quote. */
public final class RecruiteUnitPanel extends VBox {

    public RecruiteUnitPanel(final EarnedImmigrationOffer offer,
            final Player buyer, final ImageProvider images,
            final LocalizationHelper names, final I18n i18n,
            final RecruiteUnitsDialog dialog) {
        setId("recruit-candidate-" + offer.getSlot());
        setSpacing(8);
        setPrefWidth(180);
        final Label name = new Label(names.getUnitName(offer.getType()));
        name.setId("recruit-name-" + offer.getSlot());
        name.setWrapText(true);
        final Button confirm = ActionButtons.button(ButtonIcon.CONFIRM,
                i18n.get(Europe.earnedChoose));
        confirm.setId("recruit-confirm-" + offer.getSlot());
        confirm.setDisable(!buyer.canChooseEarnedImmigrant(offer));
        confirm.setOnAction(event -> dialog.chooseEarned(offer));
        getChildren().addAll(
                new ImageView(images.getUnitImage(offer.getType())), name,
                confirm);
    }

    public RecruiteUnitPanel(final RecruitmentOffer offer, final Player buyer,
            final ImageProvider images, final LocalizationHelper names,
            final I18n i18n, final RecruiteUnitsDialog dialog) {
        setId("recruit-candidate-" + offer.getSlot());
        setSpacing(8);
        setPrefWidth(180);
        final Label name = new Label(names.getUnitName(offer.getType()));
        name.setId("recruit-name-" + offer.getSlot());
        name.setWrapText(true);
        final Button confirm = ActionButtons.button(ButtonIcon.BUY,
                i18n.get(Europe.recruitUnitDialogButton) + " "
                        + offer.getPrice());
        confirm.setId("recruit-confirm-" + offer.getSlot());
        final boolean available = buyer.canRecruit(offer);
        confirm.setDisable(!available);
        confirm.setOnAction(event -> dialog.recruit(offer));
        final Label reason = new Label(buyer.getGold() < offer.getPrice()
                ? i18n.get(Europe.buyUnitDialog_notEnoughGold)
                : available ? "" : i18n.get(Europe.recruitUnavailable));
        reason.setId("recruit-reason-" + offer.getSlot());
        reason.setWrapText(true);
        getChildren().addAll(
                new ImageView(images.getUnitImage(offer.getType())), name,
                confirm, reason);
    }
}
