package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public final class FreePlaySetupPage extends AbstractScreen {
    public FreePlaySetupPage(final TestContext context) {
        super(context);
    }

    public void selectMap(final int index) {
        select("freePlayMap", index);
    }

    public void selectNationPreview(final int index) {
        select("freePlayNation", index);
    }

    private void select(final String id, final int index) {
        getRobot().clickOn("#" + id).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void enterName(final String value) {
        final TextField input = getNodeFinder().lookup("#freePlayPlayerName")
                .queryAs(TextField.class);
        getRobot().clickOn(input).push(KeyCode.END)
                .eraseText(input.getText().length()).write(value);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public GamePage start() {
        clickOnButtonWithId("freePlayStart");
        return GamePage.of(getContext());
    }

    public GamePage startWithEnter() {
        getRobot().clickOn("#freePlayPlayerName").push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        return GamePage.of(getContext());
    }

    public WelcomePage back() {
        clickOnButtonWithId("freePlayBack");
        return WelcomePage.of(getContext());
    }

    public WelcomePage escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        return WelcomePage.of(getContext());
    }

    public void verifySetup(final int expectedMapIndex) {
        assertTrue(isCssIdVisible("freePlaySetup"));
        final ChoiceBox<?> map = getNodeFinder().lookup("#freePlayMap")
                .queryAs(ChoiceBox.class);
        assertEquals(3, map.getItems().size());
        assertEquals(expectedMapIndex,
                map.getSelectionModel().getSelectedIndex());
        assertEquals("Preview only. You will play as the Netherlands.",
                getLabeledById("freePlayNationNote").getText());
    }

    public void verifySelectedMap(final int expectedIndex,
            final String expectedName) {
        final ChoiceBox<?> map = getNodeFinder().lookup("#freePlayMap")
                .queryAs(ChoiceBox.class);
        assertEquals(3, map.getItems().size());
        assertEquals(expectedIndex, map.getSelectionModel().getSelectedIndex());
        assertEquals(expectedName, map.getSelectionModel().getSelectedItem());
    }

    public void verifySetupLanguage(final String expectedStart,
            final String expectedNote) {
        assertEquals(expectedStart, getButtonById("freePlayStart").getText());
        assertEquals(expectedNote,
                getLabeledById("freePlayNationNote").getText());
    }

    public void verifyNameAccepted(final boolean expected) {
        assertEquals(expected, !getButtonById("freePlayStart").isDisabled());
        assertEquals(expected,
                getLabeledById("freePlayNameError").getText().isEmpty());
    }

    public void verifyNoActiveGame() {
        assertFalse(getContext().getClassFromGuice(GameModelController.class)
                .isGameModelReady());
    }

    public void verifyWorld(final int expectedSeed, final int expectedCityX,
            final String expectedPlayerName) {
        assertEquals(expectedSeed, getModel().getMap().getSeed());
        assertEquals(50, getModel().getMap().getMaxLocationX());
        assertEquals(30, getModel().getMap().getMaxLocationY());
        final Player human = getModel().getPlayerByName("Dutch");
        final Player france = getModel().getPlayerByName("France");
        assertTrue(human.isHuman());
        assertEquals(expectedPlayerName, human.getDisplayName());
        assertSame(human, getModel().getCurrentPlayer());
        assertTrue(france.isComputer());
        assertFalse(france.isKing());
        assertEquals(2, france.getColonies().size());
        assertEquals(3, france.getAllUnits().size());
        assertEquals(Location.of(expectedCityX, 10),
                getModel().getColonyById(0).orElseThrow().getLocation());
        assertEquals(Location.of(expectedCityX, expectedCityX == 17 ? 21 : 20),
                getModel().getColonyById(1).orElseThrow().getLocation());
        assertTrue(france.getColonies().stream()
                .allMatch(c -> c.getOwner() == france));
        assertSame(human, getModel().getUnitById(3).getOwner());
        assertEquals(Location.of(45, 15),
                getModel().getUnitById(3).getLocation());
        assertEquals(TerrainType.OCEAN,
                getModel().getMap().getTerrainTypeAt(Location.of(45, 15)));
    }
}
