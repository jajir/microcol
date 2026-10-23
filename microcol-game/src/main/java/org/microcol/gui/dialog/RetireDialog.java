package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.BaseScore;
import org.microcol.model.ResultScore;
import org.microcol.model.RetirementQuote;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation of one retirement (R-0194). It shows the exact score that the
 * model previewed and what the confirmation does to the recorded result: a live
 * retirement records the new result, an unscored continuation keeps the frozen
 * one. Canceling or Escape changes nothing.
 */
public final class RetireDialog extends AbstractMessageWindow {

    private final I18n i18n;

    private boolean answer;

    @Inject
    RetireDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param quote the preview the player is asked to confirm
     * @return whether the player confirmed; false when the player canceled
     */
    public boolean confirm(final RetirementQuote quote) {
        answer = false;
        final BaseScore base = quote.getBaseScore();
        final ResultScore score = quote.getScore();
        final Label summary = wrapped(
                new Label(i18n
                        .get(quote.isContinued() ? RetireText.continuedSummary
                                : RetireText.liveSummary, score.getTotal())),
                "retireSummary");
        final Label parts = wrapped(new Label(i18n.get(RetireText.parts,
                base.getSubtotal(), score.getDestructionDeduction(),
                score.getBellsAfterTarget(), score.getDifficultyPercent())),
                "retireParts");
        final Label consequence = wrapped(
                new Label(i18n.get(
                        quote.isContinued() ? RetireText.continuedConsequence
                                : RetireText.liveConsequence)),
                "retireConsequence");
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(RetireText.retireConfirm),
                i18n.get(RetireText.keepPlaying));
        buttons.getButtonYes().setId("retireConfirm");
        buttons.getButtonNo().setId("retireCancel");
        buttons.getButtonYes().setOnAction(event -> {
            answer = true;
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        final VBox root = new VBox(12, summary, parts, consequence,
                buttons.getContent());
        root.setId("retireDialog");
        root.setPadding(new Insets(12));
        root.setPrefWidth(560);
        init(root);
        setTitle(i18n.get(RetireText.title));
        showAndWait();
        return answer;
    }

    private static Label wrapped(final Label label, final String id) {
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    @Override
    protected void onCancelDialog() {
        answer = false;
        close();
    }

}
