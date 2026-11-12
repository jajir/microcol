package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.model.campaign.GameModel;
import org.akropia.model.campaign.po.SavedGames;
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
        // A prior Save action may leave the outer small-window page scrolled.
        final javafx.scene.control.ScrollPane outer = (javafx.scene.control.ScrollPane) list()
                .getScene().lookup(".screen-scroll");
        if (outer != null) {
            getRobot().moveTo(outer);
            for (int step = 0; step < 40 && outer.getVvalue() > 0; step++) {
                getRobot().scroll(3, javafx.geometry.VerticalDirection.UP);
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
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

    public void verifyFullScreenPaper(final String title) {
        getRobot().interact(() -> {
            final var scene = getContext().getPrimaryStage().getScene();
            final var frame = (javafx.scene.layout.Region) scene
                    .lookup(".saved-games-screen");
            assertNotNull(frame, "Save/Load must be in the primary window");
            assertTrue(frame.lookupAll(".screen-title").stream()
                    .anyMatch(node -> node instanceof javafx.scene.control.Label
                            && title.equals(((javafx.scene.control.Label) node)
                                    .getText())));
            final var paper = javafx.scene.paint.Color.web("#FFF9EC");
            for (final String selector : java.util.List.of(".screen-body",
                    ".screen-scroll", ".viewport")) {
                final var region = (javafx.scene.layout.Region) frame
                        .lookup(selector);
                assertEquals(paper,
                        region.getBackground().getFills().get(0).getFill(),
                        selector);
            }
            assertEquals(paper,
                    frame.getBackground().getFills().get(0).getFill());
            assertEquals(1,
                    javafx.stage.Window.getWindows().stream()
                            .filter(javafx.stage.Window::isShowing).count(),
                    "Save/Load must not open a modal window");
        });
    }

    public void verifyMenu() {
        for (final String id : java.util.List.of("menuResume", "menuSave",
                "menuLoad", "menuQuickSave", "menuQuickLoad", "menuSettings",
                "menuMain", "menuQuit")) {
            assertFalse(getNodeFinder().lookup("#" + id).queryAll().isEmpty(),
                    id);
        }
    }

    /** Visible rows contain the full physical filename and no metadata. */
    public void verifyFilenameList() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(getNodeFinder().lookup("#savedGamesFilter").queryAll()
                .isEmpty());
        for (final javafx.scene.Node node : list().lookupAll(".list-cell")) {
            final javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
            if (!cell.isEmpty()) {
                final SavedGames.Entry entry = (SavedGames.Entry) cell
                        .getItem();
                assertEquals(entry.file.getName(),
                        ((javafx.scene.control.Label) cell.lookup(".label"))
                                .getText());
                assertNotNull(cell.lookup("#deleteSaveRow"));
            }
        }
    }

    /**
     * Prepare filenames that differ from metadata plus ignored legacy copies.
     */
    public void prepareFilenameCatalogue(final String name)
            throws java.io.IOException {
        final java.nio.file.Path original = entry(name).file.toPath();
        final java.nio.file.Path visible = original
                .resolveSibling("Actual file.akropia");
        java.nio.file.Files.move(original, visible);
        final java.nio.file.Path nested = java.nio.file.Files
                .createDirectories(visible.getParent().resolve("hashed-game"));
        java.nio.file.Files.copy(visible, nested.resolve("Nested.akropia"));
        java.nio.file.Files.copy(visible,
                visible.getParent().getParent().resolve("Legacy.akropia"));
    }

    public void verifyCatalogueFilenames(final String... expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(java.util.List.of(expected),
                list().getItems().stream().map(entry -> entry.file.getName())
                        .collect(java.util.stream.Collectors.toList()));
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

    public void verifySavedFilename(final String name, final String filename) {
        final var entry = entry(name);
        assertEquals(filename, entry.file.getName());
        assertEquals(
                getContext().getClassFromGuice(
                        org.akropia.model.campaign.po.PersistingTool.class)
                        .getRootSaveDirectory().toPath().resolve("saves"),
                entry.file.toPath().getParent());
    }

    public void verifyQuickSave() {
        assertTrue(getContext().getClassFromGuice(SavedGames.class).quickFile()
                .isFile());
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

    /**
     * The leave confirmation with its three choices is open exactly when
     * leaving would lose gameplay (R-0387).
     */
    public void verifyLeaveConfirmation(final boolean shown) {
        WaitForAsyncUtils.waitForFxEvents();
        for (final String id : java.util.List.of("saveConfirm", "saveDiscard",
                "saveCancel")) {
            assertEquals(shown,
                    !getNodeFinder().lookup("#" + id).queryAll().isEmpty(), id);
        }
    }

    /**
     * The regular manual Save dialog is open exactly when expected, e.g. after
     * Save and continue (R-0387).
     */
    public void verifySaveDialogOpen(final boolean open) {
        WaitForAsyncUtils.waitForFxEvents();
        // Only the Save browser has Save game; the Load page has Load.
        assertEquals(open,
                !getNodeFinder().lookup("#saveNew").queryAll().isEmpty());
    }

    /** The open leave confirmation explains what leaving would lose. */
    public void verifyLeaveMessage(final String expectedPart) {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node cancel = getNodeFinder().lookup("#saveCancel")
                .query();
        assertTrue(getNodeFinder().from(cancel.getScene().getRoot()).lookup((
                javafx.scene.Node node) -> node instanceof javafx.scene.control.Label
                        && ((javafx.scene.control.Label) node).getText() != null
                        && ((javafx.scene.control.Label) node).getText()
                                .contains(expectedPart))
                .tryQuery().isPresent(), expectedPart);
    }

    /** The live game holds the gameplay of the named save. */
    public void verifyLoadedSave(final String name) throws Exception {
        final var files = new org.akropia.model.campaign.po.GameModelPoDao();
        final var expected = files.loadPlayerGame(entry(name).file);
        final java.nio.file.Path actualFile = java.nio.file.Files
                .createTempFile("loaded-save-", ".akropia");
        try {
            getContext()
                    .getClassFromGuice(
                            org.akropia.model.campaign.GameModelDao.class)
                    .saveToFile(actualFile.toString(), game());
            final var actual = files.loadPlayerGame(actualFile.toFile());
            final var gson = new com.google.gson.Gson();
            assertEquals(expected.getPlaythroughId(),
                    actual.getPlaythroughId());
            assertEquals(gson.toJsonTree(expected.getModel()),
                    gson.toJsonTree(actual.getModel()));
        } finally {
            java.nio.file.Files.deleteIfExists(actualFile);
        }
    }

    public void prepareCampaignImport() throws java.io.IOException {
        final var reader = new org.akropia.model.campaign.po.GameModelPoDao();
        final java.io.File file = java.io.File.createTempFile("campaign-menu-",
                ".akropia");
        file.deleteOnExit();
        reader.saveToFile(file.getAbsolutePath(), reader.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia"));
        org.mockito.Mockito.when(getContext()
                .getClassFromGuice(org.akropia.gui.FileSelectingService.class)
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
            final String gameType) {
        WaitForAsyncUtils.waitForFxEvents();
        final String summary = getLabeledById("savedGameSummary").getText();
        assertNotNull(list().getSelectionModel().getSelectedItem());
        assertTrue(summary.contains(player), summary);
        assertTrue(summary.contains(year), summary);
        assertTrue(summary.contains(gameType), summary);
        assertFalse(summary.contains("null"), summary);
        assertFalse(summary.contains("Type:"), summary);
        assertFalse(summary.contains("Discoverer"), summary);
        assertTrue(summary.contains(" AD"), summary);
        assertTrue(summary.contains("\nRebel support:"), summary);
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

    /** The preview title is hidden while its accessible text is retained. */
    public void verifyHiddenDetailName(final String name) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(name, getLabeledById("savedGameDetailName").getText());
        assertFalse(getLabeledById("savedGameDetailName").isVisible());
        assertFalse(getLabeledById("savedGameDetailName").isManaged());
    }

    /**
     * Load keeps the filename in its row and starts detail with the preview.
     */
    public void verifyLoadLayout(final String name) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(name,
                list().getSelectionModel().getSelectedItem().getName());
        final javafx.scene.Node title = getNodeFinder()
                .lookup("#savedGameDetailName").query();
        assertFalse(title.isVisible());
        assertFalse(title.isManaged());
        for (final String id : java.util.List.of("deleteSave", "importSave")) {
            assertTrue(getNodeFinder().lookup("#" + id).queryAll().isEmpty());
        }
        assertTrue(getNodeFinder()
                .lookup(node -> node instanceof javafx.scene.control.Label
                        && "Saved games".equals(
                                ((javafx.scene.control.Label) node).getText()))
                .queryAll().isEmpty());
        final javafx.scene.Node preview = getNodeFinder()
                .lookup(".saved-game-preview").query();
        final javafx.scene.layout.Region detail = getNodeFinder()
                .lookup("#savedGameDetail")
                .queryAs(javafx.scene.layout.Region.class);
        assertEquals(
                detail.localToScene(detail.getBoundsInLocal()).getMinY()
                        + detail.getInsets().getTop(),
                preview.localToScene(preview.getBoundsInLocal()).getMinY(), 2);
        verifyFilenameList();
        verifyPaperFileControls();
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
    public void verifySimplifiedSaveControls() {
        for (final String id : java.util.List.of("overwriteSave", "exportSave",
                "deleteSave", "savedGameTarget")) {
            assertTrue(getNodeFinder().lookup("#" + id).queryAll().isEmpty(),
                    id);
        }
        final javafx.scene.Node save = getNodeFinder().lookup("#saveNew")
                .query();
        final javafx.scene.Node folder = getNodeFinder()
                .lookup("#openSaveFolder").query();
        assertNotSame(save.getParent(), folder.getParent());
        assertEquals(
                list().localToScene(list().getLayoutBounds()).getMaxY() + 10,
                folder.localToScene(folder.getLayoutBounds()).getMinY(), 2);
    }

    /**
     * List follows the shared left edge and fills the body above its folder
     * action.
     */
    public void verifyExpandedLayout() {
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            final javafx.scene.Scene scene = list().getScene();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            final javafx.scene.Node header = scene
                    .lookup(".screen-toolbar .game-bar-content");
            final javafx.scene.Node footer = scene.lookup("#statusBar");
            final javafx.scene.Node folder = scene.lookup("#openSaveFolder");
            final javafx.geometry.Bounds files = list()
                    .localToScene(list().getLayoutBounds());
            final javafx.geometry.Bounds action = folder
                    .localToScene(folder.getLayoutBounds());
            assertEquals(
                    header.localToScene(header.getLayoutBounds()).getMinX(),
                    files.getMinX(), 2);
            assertEquals(files.getMinX(), action.getMinX(), 2);
            assertEquals(files.getMaxY() + 10, action.getMinY(), 2);
            assertEquals(footer.localToScene(footer.getLayoutBounds()).getMinY()
                    - 16, action.getMaxY(), 2);
            assertTrue(files.getHeight() > 200);
        });
    }

    public void verifyFilename(final String expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, getNodeFinder().lookup("#savedGameName")
                .queryAs(javafx.scene.control.TextField.class).getText());
    }

    public void doubleClickFile(final String name) {
        select(name);
        getRobot().doubleClickOn(fileCell(name).lookup(".label"));
    }

    public void deleteFile(final String name) {
        select(name);
        getRobot().clickOn(fileCell(name).lookup("#deleteSaveRow"));
    }

    private javafx.scene.control.ListCell<?> fileCell(final String name) {
        return getNodeFinder()
                .lookup(node -> node instanceof javafx.scene.control.ListCell
                        && ((javafx.scene.control.ListCell<?>) node)
                                .getItem() instanceof SavedGames.Entry
                        && ((SavedGames.Entry) ((javafx.scene.control.ListCell<?>) node)
                                .getItem()).getName().equals(name))
                .queryAs(javafx.scene.control.ListCell.class);
    }

    public void verifyConfirmationOverSaveScreen() {
        final javafx.scene.Node confirm = getNodeFinder().lookup("#saveCancel")
                .query();
        final javafx.stage.Stage modal = (javafx.stage.Stage) confirm.getScene()
                .getWindow();
        assertTrue(modal.isShowing());
        assertEquals(javafx.stage.Modality.WINDOW_MODAL, modal.getModality());
        final javafx.scene.Scene owner = ((javafx.stage.Stage) modal.getOwner())
                .getScene();
        assertNotNull(owner.lookup("#saveNew"));
        assertTrue(owner.lookup("#savedGamesList").isVisible());
    }

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
                        .getClassFromGuice(org.akropia.i18n.I18n.class)
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

    public void verifyManagedWarning() {
        final javafx.scene.control.Label warning = getNodeFinder()
                .lookup("#savedGameManagedWarning")
                .queryAs(javafx.scene.control.Label.class);
        assertTrue(warning.isVisible());
        assertTrue(
                warning.getText().contains("You may lose this saved position"));
    }

    /** Short filenames must fit on the first display without scrolling. */
    public void verifyCompactFileRows(final int expectedRows) {
        WaitForAsyncUtils.waitForFxEvents();
        int visibleRows = 0;
        final javafx.geometry.Bounds viewport = list()
                .localToScene(list().getLayoutBounds());
        for (final javafx.scene.Node node : list().lookupAll(".list-cell")) {
            final javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
            if (!cell.isEmpty()) {
                final javafx.geometry.Bounds bounds = cell
                        .localToScene(cell.getLayoutBounds());
                assertTrue(cell.getHeight() <= 100,
                        "Short filename row must not expand: "
                                + cell.getItem());
                assertTrue(bounds.getMinY() >= viewport.getMinY());
                assertTrue(bounds.getMaxY() <= viewport.getMaxY());
                visibleRows++;
            }
        }
        assertEquals(expectedRows, visibleRows);
    }

    public void hoverFilenameField() {
        getRobot().moveTo("#savedGameName");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverFile(final String name) {
        getRobot().moveTo(fileCell(name).lookup(".label"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void leaveFiles() {
        getRobot().moveTo("#savedGamesCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyFileUnderline(final String name, final boolean visible,
            final boolean selected) {
        final javafx.scene.control.ListCell<?> cell = fileCell(name);
        assertEquals(visible, cell.lookup(".saved-game-selection").isVisible());
        assertEquals(selected, cell.isSelected());
        assertEquals(7, cell.getPadding().getTop(), 0.1);
        assertEquals(7, cell.getPadding().getBottom(), 0.1);
    }

    public void verifyPaperFileControls() {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.control.ListCell<?> selected = fileCell(
                list().getSelectionModel().getSelectedItem().getName());
        final javafx.scene.layout.Region underline = (javafx.scene.layout.Region) selected
                .lookup(".saved-game-selection");
        assertTrue(underline.isVisible());
        assertEquals(3, underline.getHeight(), 0.1);
        assertFalse(underline.getShape() == null);
        assertEquals(javafx.scene.paint.Color.web("#FFF9EC"),
                selected.getBackground().getFills().get(0).getFill());
        final javafx.scene.control.Button delete = (javafx.scene.control.Button) selected
                .lookup("#deleteSaveRow");
        assertTrue(delete.getBorder() == null
                || delete.getBorder().getImages().isEmpty());
        assertTrue(list().localToScene(list().getBoundsInLocal()).getMaxX()
                - delete.localToScene(delete.getBoundsInLocal())
                        .getMaxX() >= 24);
        for (final javafx.scene.Node node : list().lookupAll(".list-cell")) {
            final javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
            if (!cell.isEmpty()) {
                assertEquals(
                        cell.isSelected()
                                || cell.lookup(".saved-game-selection")
                                        .getParent().isHover(),
                        cell.lookup(".saved-game-selection").isVisible());
            }
        }
        final javafx.scene.Node field = list().getScene()
                .lookup("#savedGameName");
        if (field != null) {
            final javafx.scene.control.TextField input = (javafx.scene.control.TextField) field;
            assertEquals(1, input.getBorder().getImages().size());
            assertTrue(input.getBorder().getImages().get(0).getImage().getUrl()
                    .endsWith("trade-route-name-frame.png"));
            assertFalse(getLabeledById("savedGameDetailName").isManaged());
        }
    }

    public void verifyBorderlessDetails() {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.layout.Region detail = getNodeFinder()
                .lookup("#savedGameDetail")
                .queryAs(javafx.scene.layout.Region.class);
        final javafx.scene.layout.Region preview = getNodeFinder()
                .lookup(".saved-game-preview")
                .queryAs(javafx.scene.layout.Region.class);
        for (final javafx.scene.layout.Region region : java.util.List.of(list(),
                detail, preview)) {
            assertTrue(region.getBorder().getStrokes().stream()
                    .allMatch(stroke -> javafx.scene.paint.Color.TRANSPARENT
                            .equals(stroke.getTopStroke())
                            && javafx.scene.paint.Color.TRANSPARENT
                                    .equals(stroke.getRightStroke())
                            && javafx.scene.paint.Color.TRANSPARENT
                                    .equals(stroke.getBottomStroke())
                            && javafx.scene.paint.Color.TRANSPARENT
                                    .equals(stroke.getLeftStroke())));
        }
        assertTrue(preview.getBackground().getFills().stream()
                .allMatch(fill -> javafx.scene.paint.Color.TRANSPARENT
                        .equals(fill.getFill())));
        final javafx.scene.Node image = getNodeFinder()
                .lookup("#savedGamePreview").query();
        final javafx.scene.Node summary = getNodeFinder()
                .lookup("#savedGameSummary").query();
        assertEquals(summary.localToScene(summary.getBoundsInLocal()).getMinX(),
                image.localToScene(image.getBoundsInLocal()).getMinX(), 2);
    }
}
