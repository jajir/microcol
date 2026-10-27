package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeConquestQuote;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation of an attack on a native settlement (R-0174). It shows
 * the model's frozen terms: the actual defenders, the settlement's defense, the
 * first battle's strengths, the Spanish bonus separately, and the relation
 * consequence. Back, Escape or closing changes nothing; Attack only records the
 * shown terms, and the caller runs the model operation after this window
 * closed.
 */
public final class DialogNativeAttack extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeAttackContent";

    public static final String LINE_ID = "nativeAttackLine";

    public static final String DEFENDERS_ID = "nativeAttackDefenders";

    public static final String SPANISH_ID = "nativeAttackSpanish";

    public static final String RELATIONS_ID = "nativeAttackRelations";

    public static final String CONFIRM_BUTTON_ID = "nativeAttackConfirm";

    public static final String BACK_BUTTON_ID = "nativeAttackBack";

    /** Width of the text; the lines wrap inside it, nothing is cut. */
    private static final double CONTENT_WIDTH = 560;

    private final NativeConquestQuote shown;

    private final Label line = new Label();

    private final Label defenders = new Label();

    private final Label spanish = new Label();

    private final Label relations = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.MILITARY);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private boolean confirmed;

    /**
     * @param shown the terms read from the model, which the player confirms
     */
    public DialogNativeAttack(final ViewUtil viewUtil, final I18n i18n,
            final NativeConquestQuote shown) {
        super(viewUtil, i18n);
        this.shown = shown;
        line.setId(LINE_ID);
        line.setWrapText(true);
        defenders.setId(DEFENDERS_ID);
        defenders.setWrapText(true);
        spanish.setId(SPANISH_ID);
        spanish.setWrapText(true);
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
        final VBox root = new VBox(10, line, defenders, spanish, relations,
                buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        root.setPrefWidth(CONTENT_WIDTH);
        init(root);
    }

    /**
     * @return whether the player chose Attack; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return the exact terms the player saw and confirmed
     */
    public NativeConquestQuote getShownQuote() {
        return shown;
    }

    /**
     * Keyboard focus starts on Back, so Enter changes nothing and the attack
     * needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeAttackTitle));
        final String nation = i18n.get(StatusBarMessageService
                .nationLabel(shown.getSettlement().getNation()));
        line.setText(i18n.get(Loc.nativeAttackLine, nation));
        final var first = shown.getFirstBattle();
        defenders.setText(i18n.get(Loc.nativeAttackDefenders,
                shown.getDefenders().size(), shown.getDefenseBonusPercent(),
                Math.round(first.getAttacker().getTotal()),
                Math.round(first.getDefender().getTotal()),
                Math.round(first.getAttackerWinProbability() * 100)));
        spanish.setText(shown.getSpanishBonusPercent() > 0
                ? i18n.get(Loc.nativeAttackSpanishBonus,
                        shown.getSpanishBonusPercent())
                : i18n.get(Loc.nativeAttackNoSpanish));
        relations.setText(i18n.get(Loc.nativeAttackRelations));
        confirm.setText(i18n.get(Loc.nativeAttackConfirmButton));
        back.setText(i18n.get(Loc.nativeAttackBackButton));
    }

}
