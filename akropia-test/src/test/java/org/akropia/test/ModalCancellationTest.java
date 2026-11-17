package org.akropia.test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

import java.util.concurrent.atomic.AtomicInteger;

import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Shared modal cancellation, including controls that consume Escape. */
@ExtendWith(ApplicationExtension.class)
@Tag("local")
public class ModalCancellationTest extends AbstractAkropiaTest {
    private AbstractMessageWindow dialog;
    private final AtomicInteger cancellations = new AtomicInteger();
    private final AtomicInteger confirmations = new AtomicInteger();
    private Button cancel;
    private Button confirm;
    private ListView<String> list;
    private TextField input;
    private ComboBox<String> choices;

    @Start
    void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F221-game-toolbar.akropia"));
    }

    private void prepareDialog() {
        final ViewUtil view = getContext().getClassFromGuice(ViewUtil.class);
        dialog = new AbstractMessageWindow(view,
                getContext().getClassFromGuice(I18n.class)) {
        };
        list = new ListView<>();
        list.getItems().addAll("First", "Second");
        input = new TextField("Unconfirmed draft");
        choices = new ComboBox<>();
        choices.getItems().addAll("First", "Second");
        cancel = new Button("Back");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> {
            cancellations.incrementAndGet();
            dialog.close();
        });
        confirm = new Button("Confirm");
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> confirmations.incrementAndGet());
        dialog.init(new VBox(10, input, list, choices, cancel, confirm));
    }

    @Test
    void escape_runs_cancel_once_from_list_text_and_buttons(
            final FxRobot robot) {
        open();
        robot.clickOn(list);
        robot.push(KeyCode.ESCAPE);
        verifyCanceled(1);
        open();
        robot.clickOn(input);
        robot.write(" changed");
        robot.push(KeyCode.ESCAPE);
        verifyCanceled(2);
        open();
        robot.interact(() -> confirm.requestFocus());
        robot.push(KeyCode.ESCAPE);
        verifyCanceled(3);
        open();
        robot.interact(() -> cancel.requestFocus());
        robot.push(KeyCode.ESCAPE);
        verifyCanceled(4);
    }

    @Test
    void escape_closes_open_choice_before_canceling_dialog(
            final FxRobot robot) {
        open();
        robot.clickOn(choices);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(choices.isShowing());
        robot.push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(choices.isShowing());
        assertTrue(dialog.getScene().getWindow().isShowing());
        assertEquals(0, cancellations.get());
        robot.push(KeyCode.ESCAPE);
        verifyCanceled(1);
    }

    private void open() {
        if (dialog == null) {
            org.akropia.page.WelcomePage.of(getContext()).loadGame();
            getContext().getRobot().interact(this::prepareDialog);
        }
        Platform.runLater(() -> dialog.showAndWait());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void verifyCanceled(final int expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(dialog.getScene().getWindow().isShowing());
        assertEquals(expected, cancellations.get());
        assertEquals(0, confirmations.get());
    }
}
