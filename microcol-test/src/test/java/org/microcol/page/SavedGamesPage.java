package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.campaign.GameModel;
import org.microcol.model.campaign.po.SavedGames;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/** Session-menu actions and explicit verification of save/load outcomes. */
public final class SavedGamesPage extends AbstractScreen {
    private GameModel original;
    private com.google.gson.JsonElement snapshot;

    public SavedGamesPage(final TestContext context) {
        super(context);
    }

    public void openMenu() {
        clickOnButtonWithId("exit");
    }

    public void chooseMenu(final String id) {
        getRobot().clickOn("#" + id);
    }

    public void openSave() {
        openMenu();
        chooseMenu("menuSave");
    }

    public void openLoad() {
        openMenu();
        chooseMenu("menuLoad");
    }

    public void cancelBrowser() {
        clickOnButtonWithId("savedGamesCancel");
    }

    public void saveName(final String name) {
        getRobot().clickOn("#savedGameName").write(name);
        clickOnButtonWithId("saveNew");
    }

    public void action(final String id) {
        clickOnButtonWithId(id);
    }

    public void rememberGame() {
        original = game();
        snapshot = new com.google.gson.Gson()
                .toJsonTree(original.getModel().save());
    }

    private GameModel game() {
        return getContext().getClassFromGuice(GameModelController.class)
                .getGameModel();
    }

    @SuppressWarnings("unchecked")
    private ListView<SavedGames.Entry> list() {
        return getNodeFinder().lookup("#savedGamesList")
                .queryAs(ListView.class);
    }

    public void select(final String name) {
        final var list = list();
        final int index = java.util.stream.IntStream
                .range(0, list.getItems().size())
                .filter(i -> list.getItems().get(i).getName().equals(name))
                .findFirst().orElseThrow();
        getRobot().clickOn(list).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
    }

    public void quickSave() {
        getRobot().push(KeyCode.F5);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void quickLoad() {
        getRobot().push(KeyCode.F9);
    }

    public void closeWindow() {
        WaitForAsyncUtils.asyncFx(() -> {
            final var stage = getContext().getPrimaryStage();
            stage.fireEvent(new javafx.stage.WindowEvent(stage,
                    javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMenu() {
        for (final String id : java.util.List.of("menuResume", "menuSave",
                "menuLoad", "menuQuickSave", "menuQuickLoad", "menuSettings",
                "menuMain", "menuQuit")) {
            assertFalse(getNodeFinder().lookup("#" + id).queryAll().isEmpty(),
                    id);
        }
    }

    public void verifyInvalidName() {
        assertFalse(getLabeledById("savedGameError").getText().isEmpty());
    }

    public void verifyGameUnchanged() {
        assertSame(original, game());
        assertEquals(snapshot, new com.google.gson.Gson()
                .toJsonTree(game().getModel().save()));
    }

    public void verifySavedName(final String name, final int expected) {
        assertEquals(expected,
                getContext().getClassFromGuice(SavedGames.class).list().stream()
                        .filter(entry -> name.equals(entry.getName())).count());
    }

    public void verifyQuickSave() {
        assertTrue(getContext().getClassFromGuice(SavedGames.class)
                .quickFile(game().getPlaythroughId()).isFile());
    }

    public void verifyRestoredGame() {
        assertNotSame(original, game());
        assertEquals(original.getPlaythroughId(), game().getPlaythroughId());
        assertEquals(snapshot, new com.google.gson.Gson()
                .toJsonTree(game().getModel().save()));
    }

    public void verifyWindowOpen() {
        assertTrue(getContext().getPrimaryStage().isShowing());
    }

    public void prepareCampaignImport() throws java.io.IOException {
        final var reader = new org.microcol.model.campaign.po.GameModelPoDao();
        final java.io.File file = java.io.File.createTempFile("campaign-menu-",
                ".microcol");
        file.deleteOnExit();
        reader.saveToFile(file.getAbsolutePath(), reader.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol"));
        org.mockito.Mockito.when(getContext()
                .getClassFromGuice(org.microcol.gui.FileSelectingService.class)
                .loadFile(org.mockito.ArgumentMatchers.any(java.io.File.class)))
                .thenReturn(file);
    }

    private com.google.gson.JsonElement initialMission;
    private String initialPlaythrough;

    public void rememberMissionStart() {
        initialMission = new com.google.gson.Gson()
                .toJsonTree(game().getModel().save());
        initialPlaythrough = game().getPlaythroughId();
    }

    public void verifyRestartAvailable(final boolean expected) {
        assertEquals(expected,
                !getNodeFinder().lookup("#menuRestart").queryAll().isEmpty());
    }

    public void verifyRestartedMission() {
        assertNotEquals(initialPlaythrough, game().getPlaythroughId());
        assertEquals(initialMission, new com.google.gson.Gson()
                .toJsonTree(game().getModel().save()));
    }

    public void verifySelectedDetails(final String player, final String year,
            final String difficulty) {
        final var selected = list().getSelectionModel().getSelectedItem();
        final var cell = getNodeFinder().lookup(".list-cell")
                .queryAllAs(javafx.scene.control.ListCell.class).stream()
                .filter(value -> value.getItem() == selected).findFirst()
                .orElseThrow();
        assertTrue(cell.getText().contains(player), cell.getText());
        assertTrue(cell.getText().contains(year), cell.getText());
        assertTrue(cell.getText().contains(difficulty), cell.getText());
        assertFalse(cell.getText().contains("null"), cell.getText());
    }
}
