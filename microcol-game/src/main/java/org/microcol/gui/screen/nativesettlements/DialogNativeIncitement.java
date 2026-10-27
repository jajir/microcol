package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeIncitementQuote;
import org.microcol.model.NativeNation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Target picker and explicit confirmation of a paid incitement (R-0166). The
 * player chooses one selectable European power; the price, the mission count,
 * the missionary's expertise, the chance and the gold are the model's terms for
 * that power, read again for each choice. Confirm is disabled when the purse
 * cannot pay the price. Back, Escape or closing the window changes nothing;
 * Confirm only records the shown terms, and the caller runs the model operation
 * after this window has closed.
 */
public final class DialogNativeIncitement extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeIncitementContent";

    public static final String TARGET_ID = "nativeIncitementTarget";

    public static final String TERMS_ID = "nativeIncitementTerms";

    public static final String NOTICE_ID = "nativeIncitementNotice";

    public static final String CONFIRM_BUTTON_ID = "nativeIncitementConfirm";

    public static final String BACK_BUTTON_ID = "nativeIncitementBack";

    /** Width of the picker; the terms wrap inside it, nothing is cut. */
    private static final double CONTENT_WIDTH = 560;

    private final NativeNation tribe;

    private final List<EuropeanNation> targets;

    /** Model terms for one selectable power, empty when none is offered. */
    private final Function<EuropeanNation, Optional<NativeIncitementQuote>> quotes;

    /** Display name of a power, as the game names its player. */
    private final Function<EuropeanNation, String> names;

    private final Label line = new Label();

    private final Label targetLabel = new Label();

    private final ComboBox<EuropeanNation> target = new ComboBox<>();

    private final Label terms = new Label();

    private final Label notice = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.MILITARY);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private NativeIncitementQuote shown;

    private boolean confirmed;

    private I18n texts;

    /**
     * @param tribe   the native tribe that would be incited
     * @param targets the selectable powers, from the model
     * @param quotes  the model terms of one power, read live for each choice
     * @param names   the display name of one power
     */
    public DialogNativeIncitement(final ViewUtil viewUtil, final I18n i18n,
            final NativeNation tribe, final List<EuropeanNation> targets,
            final Function<EuropeanNation, Optional<NativeIncitementQuote>> quotes,
            final Function<EuropeanNation, String> names) {
        super(viewUtil, i18n);
        this.tribe = tribe;
        this.targets = List.copyOf(targets);
        this.quotes = quotes;
        this.names = names;
        line.setWrapText(true);
        target.setId(TARGET_ID);
        target.getItems().setAll(this.targets);
        target.setConverter(new StringConverter<EuropeanNation>() {

            @Override
            public String toString(final EuropeanNation nation) {
                return nation == null ? "" : names.apply(nation);
            }

            @Override
            public EuropeanNation fromString(final String text) {
                return null;
            }

        });
        target.setOnAction(e -> refresh());
        terms.setId(TERMS_ID);
        terms.setWrapText(true);
        notice.setId(NOTICE_ID);
        notice.setWrapText(true);
        confirm.setId(CONFIRM_BUTTON_ID);
        confirm.setOnAction(e -> {
            confirmed = true;
            close();
        });
        back.setId(BACK_BUTTON_ID);
        back.setCancelButton(true);
        back.setOnAction(e -> close());
        final HBox choice = new HBox(10, targetLabel, target);
        final HBox buttons = new HBox(10, back, confirm);
        final VBox root = new VBox(10, line, choice, terms, notice, buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        // The wrapped lines would otherwise ask for their whole unwrapped
        // width.
        root.setPrefWidth(CONTENT_WIDTH);
        init(root);
    }

    /**
     * @return whether the player chose Confirm; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return the exact terms the player confirmed, or null when none was shown
     */
    public NativeIncitementQuote getShownQuote() {
        return shown;
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves everything unchanged and
     * the payment needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        texts = i18n;
        setTitle(i18n.get(Loc.nativeIncitementTitle));
        line.setText(i18n.get(Loc.nativeIncitementLine,
                i18n.get(StatusBarMessageService.nationLabel(tribe))));
        targetLabel.setText(i18n.get(Loc.nativeIncitementTarget));
        if (target.getValue() == null && !targets.isEmpty()) {
            target.setValue(targets.get(0));
        }
        confirm.setText(i18n.get(Loc.nativeIncitementConfirmButton));
        back.setText(i18n.get(Loc.nativeIncitementBackButton));
        refresh();
    }

    /**
     * Read the model terms of the chosen power again. Confirm needs terms the
     * purse can pay; the model repeats the check when the payment is made.
     */
    private void refresh() {
        final EuropeanNation chosen = target.getValue();
        shown = chosen == null ? null : quotes.apply(chosen).orElse(null);
        if (texts == null) {
            return;
        }
        if (shown == null) {
            terms.setText(texts.get(Loc.nativeIncitementUnavailable));
            notice.setText("");
            confirm.setDisable(true);
            return;
        }
        terms.setText(texts.get(Loc.nativeIncitementTerms, shown.getPrice(),
                texts.get(DialogNativeMission
                        .expertName(shown.isVisitorExpert())),
                texts.get(DialogNativeChief.moodName(
                        NativeHostility.of(shown.getVisitorHostility()))),
                shown.getMissions(), Math.round(shown.getChance() * 100),
                shown.getGold()));
        final boolean affordable = shown.getGold() >= shown.getPrice();
        notice.setText(affordable ? "" : texts.get(Loc.nativeIncitementNoGold));
        confirm.setDisable(!affordable);
    }

}
