package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.Loc;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeMissionView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation before a missionary establishes a mission (R-0164). It
 * names the owner, whether the missionary acts as an expert, the natives'
 * current attitude and the progress of the mission. Back, Escape or closing the
 * window changes nothing; Confirm only records the choice, and the caller runs
 * the model operation after this window has closed.
 */
public final class DialogNativeMission extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeMissionContent";

    public static final String LINE_ID = "nativeMissionLine";

    public static final String CONFIRM_BUTTON_ID = "nativeMissionConfirm";

    public static final String BACK_BUTTON_ID = "nativeMissionBack";

    /** Width of the confirmation; the line wraps inside it, nothing is cut. */
    private static final double CONTENT_WIDTH = 560;

    private final NativeMissionView mission;

    private final Label line = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.RELIGION);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    private boolean confirmed;

    /**
     * @param mission the exact terms the player is confirming
     */
    public DialogNativeMission(final ViewUtil viewUtil, final I18n i18n,
            final NativeMissionView mission) {
        super(viewUtil, i18n);
        this.mission = mission;
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
     * @return whether the player chose Confirm; false after Back or Escape
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves the mission unchanged and
     * Confirm needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeMissionTitle));
        line.setText(i18n.get(Loc.nativeMissionLine, mission.getOwnerName(),
                i18n.get(expertName(mission.isEffectiveExpert())),
                i18n.get(DialogNativeChief.moodName(mission.getHostility())),
                mission.getCompletedUpdates(),
                NativeMissionView.REQUIRED_UPDATES));
        confirm.setText(i18n.get(Loc.nativeMissionConfirmButton));
        back.setText(i18n.get(Loc.nativeMissionBackButton));
    }

    /**
     * @param expert whether the missionary acts as an expert
     * @return the word that names that expertise
     */
    static Loc expertName(final boolean expert) {
        return expert ? Loc.nativeMissionExpert : Loc.nativeMissionOrdinary;
    }

}
