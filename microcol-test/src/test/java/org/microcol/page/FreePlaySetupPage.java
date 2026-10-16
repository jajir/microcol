package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Difficulty;
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

    public void selectDifficulty(final Difficulty difficulty) {
        final String name = getContext()
                .getClassFromGuice(org.microcol.gui.LocalizationHelper.class)
                .getDifficultyName(difficulty);
        getRobot().clickOn("#freePlayDifficulty");
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node item = getListOfNodes(".context-menu .label")
                .stream()
                .filter(node -> name.equals(
                        ((javafx.scene.control.Labeled) node).getText()))
                .findFirst().orElseThrow();
        getRobot().clickOn(item);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDifficultySelection(final Difficulty expected,
            final String expectedLabel) {
        final ChoiceBox<?> choice = getNodeFinder()
                .lookup("#freePlayDifficulty").queryAs(ChoiceBox.class);
        assertTrue(choice.isVisible());
        assertEquals(5, choice.getItems().size());
        assertEquals(expected.ordinal(),
                choice.getSelectionModel().getSelectedIndex());
        final String selected = choice.getConverter() == null
                ? String.valueOf(choice.getSelectionModel().getSelectedItem())
                : difficultyLabel(choice);
        assertEquals(expectedLabel, selected);
    }

    private <T> String difficultyLabel(final ChoiceBox<T> choice) {
        return choice.getConverter()
                .toString(choice.getSelectionModel().getSelectedItem());
    }

    public void verifyGameDifficulty(final Difficulty expected,
            final String expectedLabel) {
        assertEquals(expected, getModel().getDifficulty());
        assertEquals(expected, getModel().save().getDifficulty());
        final javafx.scene.control.Labeled label = getLabeledById(
                "gameDifficulty");
        assertTrue(label.isVisible());
        assertTrue(label.getText().contains(expectedLabel), label.getText());
    }

    public void verifySavedDifficulty(final Difficulty expected) {
        final java.io.File file = getContext()
                .getClassFromGuice(
                        org.microcol.model.campaign.po.PersistingTool.class)
                .getAutoSaveFile();
        final org.microcol.model.campaign.po.GameModelPo data = new org.microcol.model.campaign.po.GameModelPoDao()
                .loadFromFile(file);
        assertEquals(expected, data.getModel().getDifficulty());
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
