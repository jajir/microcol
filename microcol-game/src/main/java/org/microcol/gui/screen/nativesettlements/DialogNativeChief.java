package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.Loc;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeChiefPreview;
import org.microcol.model.NativeHostility;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation before a scout visits a native chief (R-0158). It names
 * the settlement's mood and the exact chance of each result, so the death risk
 * is known before the one draw. A repeat visit names only the mood, because
 * nothing is drawn. Back, Escape or closing the window changes nothing; Confirm
 * only records the choice, and the caller runs the model operation after this
 * window has closed.
 */
public final class DialogNativeChief extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeChiefContent";

    public static final String LINE_ID = "nativeChiefLine";

    public static final String CONFIRM_BUTTON_ID = "nativeChiefConfirm";

    public static final String BACK_BUTTON_ID = "nativeChiefBack";

    private final NativeChiefPreview preview;

    private final Label line = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.DIPLOMACY);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private boolean confirmed;

    /**
     * @param preview the exact terms the player is confirming
     */
    public DialogNativeChief(final ViewUtil viewUtil, final I18n i18n,
            final NativeChiefPreview preview) {
        super(viewUtil, i18n);
        this.preview = preview;
        line.setId(LINE_ID);
        line.setWrapText(true);
        confirm.setId(CONFIRM_BUTTON_ID);
        confirm.setOnAction(e -> {
            confirmed = true;
            close();
        });
        back.setId(BACK_BUTTON_ID);
        back.setCancelButton(true);
        back.setOnAction(e -> close());
        final HBox buttons = new HBox(10, back, confirm);
        final VBox root = new VBox(10, line, buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        init(root);
    }

    /**
     * @return whether the player chose Confirm; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves the visit unchanged and
     * Confirm needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeChiefTitle));
        final String mood = i18n.get(moodName(preview.getHostility()));
        if (preview.isRepeat()) {
            line.setText(i18n.get(Loc.nativeChiefRepeatLine, mood));
        } else {
            line.setText(i18n.get(Loc.nativeChiefLine, mood,
                    percentText(preview.getInformationPerMille()),
                    percentText(preview.getGoldPerMille()),
                    percentText(preview.getDeathPerMille())));
        }
        confirm.setText(i18n.get(Loc.nativeChiefConfirmButton));
        back.setText(i18n.get(Loc.nativeChiefBackButton));
    }

    /**
     * @param perMille whole per-mille chance
     * @return the percentage with one decimal, written without locale-specific
     *         separators, for example 72.5 for 725
     */
    private static String percentText(final int perMille) {
        return (perMille / 10) + "." + (perMille % 10);
    }

    /**
     * @param hostility required attitude of the natives
     * @return the word of that attitude, shared by the chief and mission
     *         dialogs
     */
    static Loc moodName(final NativeHostility hostility) {
        switch (hostility) {
        case CONTENT:
            return Loc.nativeChiefMoodContent;
        case UNEASY:
            return Loc.nativeChiefMoodUneasy;
        case ANGRY:
            return Loc.nativeChiefMoodAngry;
        case HOSTILE:
            return Loc.nativeChiefMoodHostile;
        default:
            throw new IllegalStateException("Unknown native hostility");
        }
    }

}
