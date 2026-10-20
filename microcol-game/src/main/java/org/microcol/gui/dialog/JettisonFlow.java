package org.microcol.gui.dialog;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.microcol.gui.LocalizationHelper;
import org.microcol.i18n.I18n;
import org.microcol.model.JettisonOffer;
import org.microcol.model.JettisonProblem;
import org.microcol.model.Model;
import org.microcol.model.unit.Ship;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * The single player path for discarding goods from an owned ship at sea, shared
 * by the map and the Europe high-seas list. The model offers the holds and
 * limits; the player picks a hold when there are several, a quantity, and then
 * confirms. Cancel and Escape change nothing, and a refusal explains itself.
 */
public final class JettisonFlow {

    private final JettisonDialog jettisonDialog;

    private final ChooseGoodsDialog chooseGoods;

    private final DialogMessage dialogMessage;

    private final LocalizationHelper localizationHelper;

    private final I18n i18n;

    @Inject
    JettisonFlow(final JettisonDialog jettisonDialog,
            final ChooseGoodsDialog chooseGoods,
            final DialogMessage dialogMessage,
            final LocalizationHelper localizationHelper, final I18n i18n) {
        this.jettisonDialog = Preconditions.checkNotNull(jettisonDialog);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Ask the player for the terms and discard goods only after confirmation.
     *
     * @param model required game model
     * @param ship  required ship of the human player
     * @return <code>true</code> when goods were discarded
     */
    public boolean jettison(final Model model, final Ship ship) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(ship);
        final Optional<JettisonProblem> problem = model
                .getJettisonProblem(ship);
        if (problem.isPresent()) {
            showMessage(i18n.get(problemKey(problem.get())));
            return false;
        }
        final String shipName = localizationHelper.getUnitName(ship.getType());
        final List<JettisonOffer> offers = IntStream
                .range(0, ship.getCargo().getSlots().size())
                .filter(index -> ship.getCargo().getSlotByIndex(index)
                        .getGoods().isPresent())
                .mapToObj(index -> model.offerJettison(ship, index))
                .collect(Collectors.toList());
        final Optional<JettisonOffer> chosen = offers.size() == 1
                ? Optional.of(offers.get(0))
                : jettisonDialog.chooseHold(shipName, offers);
        if (chosen.isEmpty()) {
            return false;
        }
        final JettisonOffer offer = chosen.get();
        chooseGoods.init(offer.getGoods(), i18n.get(Jettison.quantityReason,
                offer.getSlotIndex() + 1, shipName));
        final int amount = chooseGoods.getActualValue().getAmount();
        if (amount == 0) {
            return false;
        }
        if (!jettisonDialog.confirm(shipName, offer, amount)) {
            return false;
        }
        try {
            model.jettison(offer, amount);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            // The ship, its place or its hold changed after the offer.
            showMessage(i18n.get(Jettison.changed));
            return false;
        }
        return true;
    }

    private void showMessage(final String text) {
        dialogMessage.setTitle(i18n.get(Jettison.title));
        dialogMessage.setText(text);
        dialogMessage.showAndWait();
    }

    private static Jettison problemKey(final JettisonProblem problem) {
        switch (problem) {
        case WRONG_PHASE:
            return Jettison.wrongPhase;
        case NO_GOODS:
            return Jettison.noGoods;
        default:
            return Jettison.unavailable;
        }
    }
}
