package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeTributeQuote;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation of a tribute demand on a native settlement (R-0162). It
 * shows the model's frozen terms: the force, the defenders with the
 * settlement's defense, the payment or refusal that follows, and the relation
 * consequence. Back, Escape or closing changes nothing; Demand only records the
 * shown terms, and the caller runs the model operation after this window
 * closed. The strength numbers come from the quote, never from this window.
 */
public final class DialogNativeTribute extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeTributeContent";

    public static final String LINE_ID = "nativeTributeLine";

    public static final String FORCE_ID = "nativeTributeForce";

    public static final String DEFENSE_ID = "nativeTributeDefense";

    public static final String OUTCOME_ID = "nativeTributeOutcome";

    public static final String RELATIONS_ID = "nativeTributeRelations";

    public static final String CONFIRM_BUTTON_ID = "nativeTributeConfirm";

    public static final String BACK_BUTTON_ID = "nativeTributeBack";

    /** Width of the text; the lines wrap inside it, nothing is cut. */
    private static final double CONTENT_WIDTH = 560;

    private final NativeTributeQuote shown;

    private final Label line = new Label();

    private final Label force = new Label();

    private final Label defense = new Label();

    private final Label outcome = new Label();

    private final Label relations = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.GOLD);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private boolean confirmed;

    /**
     * @param shown the terms read from the model, which the player confirms
     */
    public DialogNativeTribute(final ViewUtil viewUtil, final I18n i18n,
            final NativeTributeQuote shown) {
        super(viewUtil, i18n);
        this.shown = shown;
        line.setId(LINE_ID);
        line.setWrapText(true);
        force.setId(FORCE_ID);
        force.setWrapText(true);
        defense.setId(DEFENSE_ID);
        defense.setWrapText(true);
        outcome.setId(OUTCOME_ID);
        outcome.setWrapText(true);
        relations.setId(RELATIONS_ID);
        relations.setWrapText(true);
        confirm.setId(CONFIRM_BUTTON_ID);
        confirm.setOnAction(e -> {
            confirmed = true;
            close();
        });
        back.setId(BACK_BUTTON_ID);
        back.setCancelButton(true);
        back.setOnAction(e -> close());
        final HBox buttons = new HBox(10, back, confirm);
        final VBox root = new VBox(10, line, force, defense, outcome, relations,
                buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        root.setPrefWidth(CONTENT_WIDTH);
        init(root);
    }

    /**
     * @return whether the player chose Demand; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return the exact terms the player saw and confirmed
     */
    public NativeTributeQuote getShownQuote() {
        return shown;
    }

    /**
     * Keyboard focus starts on Back, so Enter changes nothing and the demand
     * needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeTributeTitle));
        line.setText(
                i18n.get(Loc.nativeTributeLine, i18n.get(StatusBarMessageService
                        .nationLabel(shown.getSettlement().getNation()))));
        force.setText(i18n.get(Loc.nativeTributeForce, number(shown.getForce()),
                shown.getDefenderIds().size(), number(shown.getDefense())));
        defense.setText(i18n.get(Loc.nativeTributeDefense,
                shown.getDefenseBonusPercent()));
        outcome.setText(outcomeText(i18n));
        relations.setText(i18n.get(Loc.nativeTributeRelations,
                Math.round(shown.getTribalAnger()),
                Math.round(shown.getLocalAlarm())));
        confirm.setText(i18n.get(Loc.nativeTributeConfirmButton));
        back.setText(i18n.get(Loc.nativeTributeBackButton));
    }

    /**
     * @return the payment the model quoted: the refusal, an empty treasury, or
     *         the gold that the settlement gives up
     */
    private String outcomeText(final I18n i18n) {
        if (!shown.isAccepted()) {
            return i18n.get(Loc.nativeTributeOutcomeRefused);
        }
        if (shown.getPayment() == 0) {
            return i18n.get(Loc.nativeTributeOutcomeEmpty);
        }
        return i18n.get(Loc.nativeTributeOutcomePaid, shown.getCap(),
                shown.getTreasury());
    }

    /**
     * @param value strength from the quote, whole or with one half
     * @return the strength without a trailing zero, such as 2 or 2.5
     */
    private static String number(final double value) {
        return value == Math.rint(value) ? String.valueOf((long) value)
                : String.valueOf(value);
    }

}
