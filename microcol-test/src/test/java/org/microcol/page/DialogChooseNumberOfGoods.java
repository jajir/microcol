package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.microcol.gui.MicroColException;
import org.microcol.gui.util.ButtonsBar;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;

/**
 * Object allows to test one unit shown in right panel.
 */
public class DialogChooseNumberOfGoods extends AbstractScreen {

    public static DialogChooseNumberOfGoods of(final TestContext context) {
	return new DialogChooseNumberOfGoods(context);
    }

    private DialogChooseNumberOfGoods(final TestContext context) {
	super(context);
    }

    public void close() {
	getRobot().clickOn(getButtonOk(), MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
    }

    public void selectValueAtSlider(final int value) {
	final Slider slider = getSlider();
	/*
	 * Following code is not correct test. It get JavaFX component from screen and
	 * in JavaFX Application Thread set desired value. It should be set by mouse.
	 */
	Platform.runLater(() -> slider.setValue(value));
	WaitForAsyncUtils.waitForFxEvents();
    }

    private Slider getSlider() {
	final String cssClass = ".slider";
	final Set<Slider> sliders = getNodeFinder().lookup(cssClass).queryAll();
	assertEquals(1, sliders.size());
	return sliders.stream().findAny().orElseThrow(() -> new MicroColException("There is no such slider"));
    }

    public Button getButtonOk() {
	return getNodeFinder().lookup("#" + ButtonsBar.BUTTON_OK_ID).queryAs(Button.class);
    }
}
