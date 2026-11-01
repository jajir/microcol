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
        // Replace the prefilled suggestion with the given name.
        final javafx.scene.control.TextField field = getNodeFinder()
                .lookup("#savedGameName")
                .queryAs(javafx.scene.control.TextField.class);
        getRobot().interact(field::clear);
        getRobot().clickOn(field).write(name);
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

    /** Select the save changed by {@link #markUnsupported(String)}. */
    public void selectMarked() {
        final var list = list();
        final int index = java.util.stream.IntStream
                .range(0, list.getItems().size())
                .filter(i -> list.getItems().get(i).file.equals(marked))
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

    /** The detail beside the list describes the selected save. */
    public void verifySelectedDetails(final String player, final String year,
            final String difficulty) {
        WaitForAsyncUtils.waitForFxEvents();
        final String summary = getLabeledById("savedGameSummary").getText();
        assertNotNull(list().getSelectionModel().getSelectedItem());
        assertTrue(summary.contains(player), summary);
        assertTrue(summary.contains(year), summary);
        assertTrue(summary.contains(difficulty), summary);
        assertFalse(summary.contains("null"), summary);
    }

    /**
     * The summary lists the saved overview values with their labels.
     *
     * @param lines expected lines, each contained in the summary
     */
    public void verifySummaryLines(final String... lines) {
        WaitForAsyncUtils.waitForFxEvents();
        final String summary = getLabeledById("savedGameSummary").getText();
        for (final String line : lines) {
            assertTrue(summary.contains(line), line + " in " + summary);
        }
    }

    /** The selected save is named in full above its preview. */
    public void verifyDetailName(final String name) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(name, getLabeledById("savedGameDetailName").getText());
    }

    /**
     * @param shown <code>true</code> when the map image is shown, otherwise the
     *              explanation of a missing preview
     */
    public void verifyPreview(final boolean shown) {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.image.ImageView image = getNodeFinder()
                .lookup("#savedGamePreview")
                .queryAs(javafx.scene.image.ImageView.class);
        final javafx.scene.control.Labeled missing = getLabeledById(
                "savedGamePreviewMissing");
        assertEquals(shown, image.isVisible() && image.getImage() != null);
        assertEquals(!shown, missing.isVisible());
        if (shown) {
            assertTrue(image.getImage().getWidth() <= 960);
            assertTrue(image.getImage().getHeight() <= 540);
        }
    }

    /** Without a valid selection no detail or Load action remains. */
    public void verifyNoSelectedDetail(final String prompt) {
        WaitForAsyncUtils.waitForFxEvents();
        assertNull(list().getSelectionModel().getSelectedItem());
        assertEquals(prompt, getLabeledById("savedGameDetailName").getText());
        assertTrue(getLabeledById("savedGameSummary").getText().isEmpty());
        assertTrue(getButtonById("loadSelected").isDisabled());
    }

    /** The Load action is available exactly for a loadable selection. */
    public void verifyLoadEnabled(final boolean enabled) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(!enabled, getButtonById("loadSelected").isDisabled());
    }

    /** Save shows the state being saved apart from the overwrite target. */
    public void verifySaveTarget(final String heading, final String target) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(heading, getLabeledById("savedGameDetailName").getText());
        final String shown = getLabeledById("savedGameTarget").getText();
        assertTrue(shown.contains(target), shown);
    }

    /** The editable name starts with the game's suggestion. */
    public void verifySuggestedName(final String expectedPart) {
        WaitForAsyncUtils.waitForFxEvents();
        final String suggested = getNodeFinder().lookup("#savedGameName")
                .queryAs(javafx.scene.control.TextField.class).getText();
        assertTrue(suggested.contains(expectedPart), suggested);
        assertTrue(suggested.codePointCount(0, suggested.length()) <= 64,
                suggested);
    }

    /** The overwrite confirmation names the old save and its time. */
    public void verifyOverwriteConfirmation(final String name) {
        WaitForAsyncUtils.waitForFxEvents();
        final SavedGames.Entry entry = entry(name);
        final String time = java.time.format.DateTimeFormatter
                .ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)
                .withLocale(getContext()
                        .getClassFromGuice(org.microcol.i18n.I18n.class)
                        .getCurrentLocale())
                .format(java.time.Instant.ofEpochMilli(entry.modified)
                        .atZone(java.time.ZoneId.systemDefault()));
        final javafx.scene.Node confirm = getNodeFinder().lookup("#saveCancel")
                .query();
        assertTrue(getNodeFinder().from(confirm.getScene().getRoot()).lookup((
                javafx.scene.Node node) -> node instanceof javafx.scene.control.Labeled
                        && ((javafx.scene.control.Labeled) node)
                                .getText() != null
                        && ((javafx.scene.control.Labeled) node).getText()
                                .contains(name)
                        && ((javafx.scene.control.Labeled) node).getText()
                                .contains(time))
                .tryQuery().isPresent(), name + " " + time);
    }

    /** The stored summary of a save matches the live game when saved. */
    public void verifyStoredSummaryMatchesGame(final String name) {
        final var summary = entry(name).metadata.getSummary();
        final var expected = game().getSaveSummary();
        assertEquals(new com.google.gson.Gson().toJsonTree(expected),
                new com.google.gson.Gson().toJsonTree(summary));
    }

    private SavedGames.Entry entry(final String name) {
        return getContext().getClassFromGuice(SavedGames.class).list().stream()
                .filter(value -> name.equals(value.getName())).findFirst()
                .orElseThrow();
    }

    /** Replace the preview of a save with bytes that are not an image. */
    public void damagePreview(final String name) throws java.io.IOException {
        replaceEntry(entry(name).file, "preview.png", new byte[] { 1, 2, 3 });
    }

    /** Remove the preview of a save, keeping its game. */
    public void removePreview(final String name) throws java.io.IOException {
        replaceEntry(entry(name).file, "preview.png", null);
    }

    private java.io.File marked;

    /**
     * Mark a save with a format version this game does not support. Its name is
     * then unreadable, so {@link #selectMarked()} finds it by file.
     */
    public void markUnsupported(final String name) throws java.io.IOException {
        final java.io.File file = entry(name).file;
        marked = file;
        final String metadata = new String(
                readEntries(file).get("metadata.json"),
                java.nio.charset.StandardCharsets.UTF_8);
        replaceEntry(file, "metadata.json",
                metadata.replace("\"formatVersion\": 1", "\"formatVersion\": 9")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Remove a listed save behind the open page. */
    public void deleteBehindList(final String name) throws java.io.IOException {
        java.nio.file.Files.delete(entry(name).file.toPath());
    }

    private static java.util.Map<String, byte[]> readEntries(
            final java.io.File file) throws java.io.IOException {
        final java.util.Map<String, byte[]> out = new java.util.LinkedHashMap<>();
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                java.nio.file.Files.newInputStream(file.toPath()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                out.put(entry.getName(), zip.readAllBytes());
            }
        }
        return out;
    }

    /**
     * Rewrite one archive entry; <code>null</code> removes it. Used only to
     * prepare damaged fixtures.
     */
    static void replaceEntry(final java.io.File file, final String name,
            final byte[] bytes) throws java.io.IOException {
        final java.util.Map<String, byte[]> entries = readEntries(file);
        if (bytes == null) {
            entries.remove(name);
        } else {
            entries.put(name, bytes);
        }
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(
                out)) {
            for (final var entry : entries.entrySet()) {
                zip.putNextEntry(new java.util.zip.ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        java.nio.file.Files.write(file.toPath(), out.toByteArray());
    }
}
