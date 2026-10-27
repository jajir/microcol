package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.NativeTeachingCourse;
import org.microcol.model.UnitType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Confirmation before a colonist starts learning at a native settlement
 * (R-0148). It names the skill and the owner updates it takes. Back, Escape or
 * closing the window changes nothing; only the start button runs the callback.
 */
public final class DialogNativeTeaching extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeTeachingContent";

    public static final String LINE_ID = "nativeTeachingLine";

    public static final String CONFIRM_BUTTON_ID = "nativeTeachingConfirm";

    public static final String BACK_BUTTON_ID = "nativeTeachingBack";

    private final UnitType visitorType;

    private final UnitType skill;

    private final Label line = new Label();

    private final Button confirm = ActionButtons.button(ButtonIcon.EDUCATION);

    private final Button back = ActionButtons.button(ButtonIcon.BACK);

    /**
     * @param onConfirm run after the player confirms, after this window closed
     */
    public DialogNativeTeaching(final ViewUtil viewUtil, final I18n i18n,
            final UnitType visitorType, final UnitType skill,
            final Runnable onConfirm) {
        super(viewUtil, i18n);
        this.visitorType = visitorType;
        this.skill = skill;
        line.setId(LINE_ID);
        line.setWrapText(true);
        confirm.setId(CONFIRM_BUTTON_ID);
        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> {
            close();
            onConfirm.run();
        });
        back.setId(BACK_BUTTON_ID);
        back.setCancelButton(true);
        back.setOnAction(e -> close());
        final HBox buttons = new HBox(10, back, confirm);
        final VBox root = new VBox(10, line, buttons);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        // Handle Escape before the focused button consumes it.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
        init(root);
    }

    /**
     * Keyboard focus starts on Back, so Enter leaves the confirmation unchanged
     * and Start needs a deliberate Tab or click.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(back::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeTeachingTitle));
        line.setText(i18n.get(Loc.nativeTeachingLine,
                i18n.get(UnitTypes.getUnitName(visitorType)),
                i18n.get(UnitTypes.getUnitName(skill)),
                NativeTeachingCourse.REQUIRED_UPDATES));
        confirm.setText(i18n.get(Loc.nativeTeachingConfirm));
        back.setText(i18n.get(Loc.nativeTeachingBack));
    }

}
