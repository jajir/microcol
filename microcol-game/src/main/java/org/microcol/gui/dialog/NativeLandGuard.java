package org.microcol.gui.dialog;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeLandQuote;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Asks the player before founding, field work or pioneer work uses
 * native-claimed land that is not paid for. Paid, waived, unclaimed and
 * unknown-claimant squares need no question: the model's quote is empty unless
 * the claimant was learned, so nothing hidden is revealed.
 */
public final class NativeLandGuard {

    private final Provider<NativeLandDialog> dialog;

    private final I18n i18n;

    @Inject
    NativeLandGuard(final Provider<NativeLandDialog> dialog, final I18n i18n) {
        this.dialog = Preconditions.checkNotNull(dialog);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Decide about one square before field or pioneer work. Paying buys the
     * square at once, so the right stays even if the work is then refused.
     *
     * @param model  required current model
     * @param player required human player who would use the square
     * @param at     required square the action uses
     * @return the decision; the caller must not start the action when it is
     *         {@link NativeLandDialog.Result#CANCELED}
     */
    public NativeLandDialog.Result proceed(final Model model,
            final Player player, final Location at) {
        final Optional<NativeLandQuote> quote = model.quoteNativeLand(player,
                at);
        if (quote.isEmpty()
                || quote.get().getStatus() != NativeLandStatus.UNPAID) {
            return NativeLandDialog.Result.NOT_ASKED;
        }
        return dialog.get().ask(model, quote.get());
    }

    /**
     * Decide about the land a founding would use, before naming the colony.
     * Nothing is paid here; the chosen purchases go to the founding itself.
     *
     * @param model   required current model
     * @param player  required human player
     * @param founder required founder
     * @param land    squares from {@code Model.getFoundingLand}
     * @return the quotes to buy with the founding, possibly empty, or empty
     *         when the player canceled
     */
    public Optional<List<NativeLandQuote>> decideFounding(final Model model,
            final Player player, final Unit founder,
            final List<Location> land) {
        final List<NativeLandQuote> known = land.stream()
                .map(at -> model.quoteNativeLand(player, at))
                .flatMap(Optional::stream).collect(Collectors.toList());
        final List<NativeLandQuote> unpaid = known.stream()
                .filter(quote -> quote.getStatus() == NativeLandStatus.UNPAID)
                .collect(Collectors.toList());
        if (unpaid.isEmpty()) {
            return Optional.of(List.of());
        }
        switch (dialog.get().askFounding(model, founder, land, known, unpaid,
                player.getGold())) {
        case PAID:
            return Optional.of(unpaid);
        case UNPAID:
            return Optional.of(List.of());
        default:
            return Optional.empty();
        }
    }

    /**
     * Rejection text after the land question. A payment already made is kept,
     * so the player is told that the right stays theirs.
     *
     * @param decision decision from {@link #proceed}
     * @param reason   required localized reason the action was refused
     * @return text for the status bar
     */
    public String rejection(final NativeLandDialog.Result decision,
            final String reason) {
        return decision == NativeLandDialog.Result.PAID
                ? reason + " " + i18n.get(Loc.nativeLandPaidKept)
                : reason;
    }

}
