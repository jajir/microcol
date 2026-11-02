package org.akropia.gui.screen.europe;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.image.ImageProvider;
import org.akropia.i18n.I18n;
import org.akropia.model.Player;
import org.akropia.model.RecruitmentOffer;
import org.akropia.model.EarnedImmigrationOffer;

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
