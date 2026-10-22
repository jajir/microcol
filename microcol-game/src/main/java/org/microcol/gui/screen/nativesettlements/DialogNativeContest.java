package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.Loc;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeMissionContestView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation before a missionary challenges a rival mission
 * (R-0165). It names the rival, both missionaries' expertise and the natives'
 * attitude toward each side, and the exact chance of winning. Back, Escape or
 * closing the window changes nothing; Confirm only records the choice, and the
 * caller runs the model operation after this window has closed.
 */
public final class DialogNativeContest extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeContestContent";

    public static final String LINE_ID = "nativeContestLine";

    public static final String CONFIRM_BUTTON_ID = "nativeContestConfirm";

    public static final String BACK_BUTTON_ID = "nativeContestBack";

    /** Width of the confirmation; the line wraps inside it, nothing is cut. */
    private static final double CONTENT_WIDTH = 560;

    private final NativeMissionContestView contest;

    private final Label line = new Label();

    private final Button confirm = new Button();

    private final Button back = new Button();

    private boolean confirmed;

    /**
     * @param contest the exact terms the player is confirming
     */
    public DialogNativeContest(final ViewUtil viewUtil, final I18n i18n,
            final NativeMissionContestView contest) {
        super(viewUtil, i18n);
        this.contest = contest;
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
        // The wrapped line would otherwise ask for its whole unwrapped width.
        root.setPrefWidth(CONTENT_WIDTH);
        init(root);
    }

    /**
     * @return whether the player chose Challenge; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves the mission unchanged and
     * Challenge needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeContestTitle));
        line.setText(i18n.get(Loc.nativeContestLine,
                contest.getResidentOwnerName(),
                i18n.get(DialogNativeMission
                        .expertName(contest.isResidentExpert())),
                i18n.get(DialogNativeChief.moodName(
                        NativeHostility.of(contest.getResidentHostility()))),
                i18n.get(DialogNativeMission
                        .expertName(contest.isVisitorExpert())),
                i18n.get(DialogNativeChief.moodName(
                        NativeHostility.of(contest.getVisitorHostility()))),
                Math.round(contest.getChallengerChance() * 100)));
        confirm.setText(i18n.get(Loc.nativeContestConfirmButton));
        back.setText(i18n.get(Loc.nativeContestBackButton));
    }

}
