package org.microcol.gui.dialog;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeLandQuote;
import org.microcol.model.Unit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shows the model's quotes for native-claimed land before the player uses it:
 * who claims it, its paid or waived status, the price and the unpaid
 * consequence. The player pays, continues unpaid or cancels. Every rule and
 * payment stays in the model; this dialog never computes whether gold suffices.
 */
@Singleton
public final class NativeLandDialog extends AbstractMessageWindow {

    /** What the player decided. */
    public enum Result {
        /** No question was needed: nothing unpaid and known is used. */
        NOT_ASKED,
        /** Pay; for founding, payment follows when the colony is confirmed. */
        PAID,
        /** Continue knowingly without paying. */
        UNPAID,
        /** Nothing was paid and the action must not start. */
        CANCELED
    }

    private final I18n i18n;

    private final StatusBarMessageService labels;

    private Result result;

    @Inject
    NativeLandDialog(final ViewUtil viewUtil, final I18n i18n,
            final StatusBarMessageService labels) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.labels = labels;
    }

    /**
     * Ask about one square of field or pioneer work. Paying buys the square at
     * once through the model; the action follows only after the dialog closes.
     *
     * @param model required current model
     * @param quote required unpaid quote from the model
     * @return the decision; closing or Escape cancels
     */
    public Result ask(final Model model, final NativeLandQuote quote) {
        final boolean payable = model.canBuyNativeLand(quote);
        return show(
                i18n.get(Loc.nativeLandClaimedBy,
                        labels.nativeClaimantText(quote.getClaimant())),
                quote.getPrice(), quote.getPlayer().getGold(), payable, "",
                () -> model.buyNativeLand(quote));
    }

    /**
     * Ask about all known claimed land a founding would use. Choosing to pay
     * changes nothing yet: the model charges the listed squares together with
     * founding, only when the player confirms the colony.
     *
     * @param model   required current model
     * @param founder required founder
     * @param land    squares from {@code Model.getFoundingLand}
     * @param known   quotes for the known claimed squares among them
     * @param unpaid  the unpaid quotes among them, not empty
     * @param gold    the payer's current gold
     * @return the decision; closing or Escape cancels
     */
    public Result askFounding(final Model model, final Unit founder,
            final List<Location> land, final List<NativeLandQuote> known,
            final List<NativeLandQuote> unpaid, final int gold) {
        final boolean payable = model.canPayFoundingLand(founder, land, unpaid);
        return show(labels.foundingLandText(land, known),
                unpaid.stream().mapToInt(NativeLandQuote::getPrice).sum(), gold,
                payable, i18n.get(Loc.nativeLandFoundingNote), null);
    }

    /**
     * @param payment buys at once, or <code>null</code> when only the decision
     *                is recorded
     */
    private Result show(final String claimantText, final int price,
            final int gold, final boolean payable, final String noteText,
            final Runnable payment) {
        result = Result.CANCELED;
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setPrefWidth(520);
        final Label title = new Label(i18n.get(Loc.nativeLandTitle));
        title.setId("nativeLandTitle");
        final Label claimant = text("nativeLandClaimant", claimantText);
        final Label priceLabel = text("nativeLandPrice",
                i18n.get(Loc.nativeLandPrice, price, gold));
        final Label consequence = text("nativeLandConsequence",
                i18n.get(Loc.nativeLandConsequence));
        final Label note = text("nativeLandNote", noteText);
        final Label message = text("nativeLandMessage", "");
        final Button pay = ActionButtons.button(ButtonIcon.GOLD,
                i18n.get(Loc.nativeLandPay));
        pay.setId("nativeLandPay");
        final Button proceed = ActionButtons.button(ButtonIcon.NEXT,
                i18n.get(Loc.nativeLandProceed));
        proceed.setId("nativeLandProceed");
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Loc.nativeLandCancel));
        cancel.setId("nativeLandCancel");
        cancel.setCancelButton(true);
        if (!payable) {
            pay.setDisable(true);
            message.setText(i18n.get(Loc.nativeLandCannotPay));
        }
        pay.setOnAction(event -> {
            try {
                if (payment != null) {
                    payment.run();
                }
                result = Result.PAID;
                close();
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                // The live state changed after the quote: nothing was charged.
                pay.setDisable(true);
                message.setText(i18n.get(Loc.nativeLandChanged));
            }
        });
        proceed.setOnAction(event -> {
            result = Result.UNPAID;
            close();
        });
        cancel.setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(title, claimant, priceLabel, consequence,
                note, message, new HBox(12, cancel, proceed, pay));
        init(root);
        setTitle(i18n.get(Loc.nativeLandTitle));
        showAndWait();
        return result;
    }

    private static Label text(final String id, final String value) {
        final Label label = new Label(value);
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    @Override
    protected void onCancelDialog() {
        result = Result.CANCELED;
        close();
    }

}
