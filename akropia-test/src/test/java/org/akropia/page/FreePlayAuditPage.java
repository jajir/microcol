package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.akropia.diagnostics.DiagnosticEvent;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.model.Location;
import org.akropia.model.Player;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.PlaceEuropePier;
import org.akropia.model.PlaceCargoSlot;
import org.akropia.model.Unit;
import org.akropia.model.Colony;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.model.campaign.po.PersistingTool;
import org.akropia.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.eventbus.EventBus;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Player actions and explicit observations for requirement refinement. Gap
 * observations describe the baseline, not acceptance of the missing rule.
 */
public final class FreePlayAuditPage extends AbstractScreen {
    public FreePlayAuditPage(final TestContext context) {
        super(context);
    }

    public void press(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openColonyMenu() {
        getRobot().clickOn("#coloniesMenu");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseColony(final String name) {
        getRobot().clickOn(name);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyColonyMenu(final String... expectedNames) {
        assertEquals(java.util.Set.of(expectedNames),
                getListOfNodes(".context-menu .label").stream()
                        .filter(node -> node instanceof Label)
                        .map(node -> ((Label) node).getText())
                        .filter(text -> !text.isEmpty())
                        .collect(Collectors.toSet()));
    }

    public void verifyCalendarAndGold(final int expectedTurns,
            final int expectedGold) {
        assertEquals(expectedTurns,
                getModel().getCalendar().getNumberOfPlayedTurns());
        assertEquals(expectedGold,
                getModel().getPlayerByName("Dutch").getGold());
    }

    public void verifyDisplayedDate(final String expectedDate) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expectedDate,
                getRobot().lookup("#labelEra").queryAs(Label.class).getText());
    }

    public void deliverTurnStartedNotification(final boolean fresh,
            final boolean human) {
        final Player player = getModel().getPlayers().stream()
                .filter(candidate -> candidate.isHuman() == human).findFirst()
                .orElseThrow();
        getRobot().interact(() -> getContext().getClassFromGuice(EventBus.class)
                .post(new TurnStartedEvent(getModel(), player, fresh)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyAutosaveExists(final boolean expected) {
        assertEquals(expected,
                getContext().getClassFromGuice(PersistingTool.class)
                        .getAutoSaveFile().exists());
    }

    public void deliverPreviousComputerTurnNotification() {
        final Player computer = getModel().getPlayers().stream()
                .filter(player -> !player.isHuman()).findFirst().orElseThrow();
        getRobot().interact(() -> getContext().getClassFromGuice(EventBus.class)
                .post(new TurnStartedEvent(getModel(), computer, true)));
    }

    public void verifyCurrentPlayerIsHuman() {
        assertTrue(getModel().getCurrentPlayer().isHuman());
    }

    public void hoverTileDirectly(final Location location) {
        final org.akropia.gui.Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        final javafx.scene.canvas.Canvas canvas = getContext()
                .getClassFromGuice(
                        org.akropia.gui.screen.game.gamepanel.GamePanelComponent.class)
                .getCanvas();
        final javafx.geometry.Point2D point = canvas.localToScreen(
                tile.getX() + org.akropia.gui.Tile.TILE_CENTER.getX(),
                tile.getY() + org.akropia.gui.Tile.TILE_CENTER.getY());
        getRobot().robotContext().getMouseRobot().move(point);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverNextTurnDirectly() {
        final javafx.scene.control.Button button = getButtonById("nextTurn");
        final javafx.geometry.Bounds bounds = button
                .localToScreen(button.getBoundsInLocal());
        getRobot().robotContext().getMouseRobot()
                .move(new javafx.geometry.Point2D(
                        bounds.getMinX() + bounds.getWidth() / 2,
                        bounds.getMinY() + bounds.getHeight() / 2));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openStatistics() {
        clickOnButtonWithId("statistics");
    }

    public GamePage closeWithEscape() {
        press(KeyCode.ESCAPE);
        return GamePage.of(getContext());
    }

    public GamePage closeReportWithClick() {
        clickOnButtonWithId("closeButtonId");
        return GamePage.of(getContext());
    }

    public void verifyReportScreen(final String expectedTitle) {
        assertEquals(expectedTitle, getNodeFinder().lookup("#atlasReportTitle")
                .queryAs(Label.class).getText(), "Report title");
        assertTrue(isCssIdVisible("closeButtonId"));
    }

    public void verifyFreePlayButtonEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("buttonStartFreeGame").isDisabled());
    }

    public void verifyContinueButtonEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("buttonContinue").isDisabled());
    }

    public GamePage continueGame() {
        clickOnButtonWithId("buttonContinue");
        return GamePage.of(getContext());
    }

    /** Open Load game from the main menu to select a recovery point. */
    public void openRecoveryChooser() {
        clickOnButtonWithId("buttonLoad");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyRecoveryChooserLabels(final String title,
            final String load, final String cancel) {
        assertEquals(title,
                getNodeFinder()
                        .lookup(node -> node instanceof Label
                                && title.equals(((Label) node).getText()))
                        .queryAs(Label.class).getText());
        assertEquals(load, getLabeledById("loadSelected").getText());
        assertEquals(cancel, getLabeledById("savedGamesCancel").getText());
    }

    private java.util.Map<File, byte[]> recoveryBytes;

    private File corruptedTurnSnapshot;

    private File recoveryFile(final RecoverySlot slot) {
        if (slot == RecoverySlot.LATEST_TURN && corruptedTurnSnapshot != null) {
            return corruptedTurnSnapshot;
        }
        final List<File> snapshots = turnSnapshots();
        if (slot == RecoverySlot.LATEST_TURN && !snapshots.isEmpty()) {
            return snapshots.get(snapshots.size() - 1);
        }
        return getContext().getClassFromGuice(PersistingTool.class)
                .getRecoveryFile(slot);
    }

    public void rememberRecoveryBytes() throws Exception {
        recoveryBytes = new java.util.HashMap<>();
        for (final File file : turnSnapshots()) {
            recoveryBytes.put(file, Files.readAllBytes(file.toPath()));
        }
        for (final RecoverySlot slot : RecoverySlot.values()) {
            final File file = recoveryFile(slot);
            if (file.isFile()) {
                recoveryBytes.put(file, Files.readAllBytes(file.toPath()));
            }
        }
    }

    public void verifyRecoveryBytesUnchanged() throws Exception {
        for (final var entry : recoveryBytes.entrySet()) {
            assertArrayEquals(entry.getValue(),
                    Files.readAllBytes(entry.getKey().toPath()));
        }
    }

    public void verifyRecoveryOption(final RecoverySlot slot,
            final boolean available, final String date) {
        final java.io.File file = recoveryFile(slot);
        final javafx.scene.control.ListView<org.akropia.model.campaign.po.SavedGames.Entry> list = savedList();
        final var entry = list.getItems().stream()
                .filter(value -> value.file.equals(file)).findFirst();
        assertEquals(available, entry.isPresent());
        if (available) {
            getRobot().interact(() -> list.scrollTo(entry.get()));
            org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
            final var cell = getNodeFinder().lookup(".list-cell")
                    .queryAllAs(javafx.scene.control.ListCell.class).stream()
                    .filter(value -> value.getItem() == entry.get()).findFirst()
                    .orElseThrow();
            // The row contains only its filename; time, game year and problems
            // belong to the selected detail.
            final String modified = java.time.format.DateTimeFormatter
                    .ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)
                    .withLocale(getContext()
                            .getClassFromGuice(org.akropia.i18n.I18n.class)
                            .getCurrentLocale())
                    .format(java.time.Instant.ofEpochMilli(entry.get().modified)
                            .atZone(java.time.ZoneId.systemDefault()));
            assertEquals(file.getName(),
                    ((Label) cell.lookup(".label")).getText());
            getRobot().interact(
                    () -> list.getSelectionModel().select(entry.get()));
            org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
            final String detail = getLabeledById("savedGameSummary").getText();
            assertTrue(detail.contains(date), detail);
            assertTrue(detail.contains(modified), detail);
        }
    }

    /** The selected save cannot be confirmed; its detail names the reason. */
    public void verifyLoadDisabled(final String reason) {
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        assertTrue(getButtonById("loadSelected").isDisabled());
        assertEquals(reason, getLabeledById("savedGameSummary").getText()
                .lines().findFirst().orElse(""));
    }

    @SuppressWarnings("unchecked")
    private javafx.scene.control.ListView<org.akropia.model.campaign.po.SavedGames.Entry> savedList() {
        return getNodeFinder().lookup("#savedGamesList")
                .queryAs(javafx.scene.control.ListView.class);
    }

    public void chooseRecovery(final RecoverySlot slot) {
        final java.io.File file = recoveryFile(slot);
        final var list = savedList();
        final var entry = list.getItems().stream()
                .filter(value -> value.file.equals(file)).findFirst()
                .orElseThrow();
        getRobot().interact(() -> list.getSelectionModel().select(entry));
        clickOnButtonWithId("loadSelected");
    }

    /** Select a recovery point with the keyboard without loading it. */
    public void selectRecovery(final RecoverySlot slot) {
        final java.io.File file = recoveryFile(slot);
        final var list = savedList();
        final int index = java.util.stream.IntStream
                .range(0, list.getItems().size())
                .filter(i -> list.getItems().get(i).file.equals(file))
                .findFirst().orElseThrow();
        getRobot().clickOn(list).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelRecovery() {
        clickOnButtonWithId("savedGamesCancel");
    }

    public void verifyRecoveryTurn(final RecoverySlot slot,
            final int expectedTurn) {
        final java.io.File file = recoveryFile(slot);
        final org.akropia.model.campaign.po.GameModelPo saved = new org.akropia.model.campaign.po.GameModelPoDao()
                .loadPlayerGame(file);
        assertEquals(expectedTurn,
                saved.getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void verifyLoadedRecoveryMatchesSlot(final RecoverySlot slot)
            throws Exception {
        final File source = recoveryFile(slot);
        final var files = new org.akropia.model.campaign.po.GameModelPoDao();
        final var expected = files.loadPlayerGame(source);
        final java.nio.file.Path actualFile = Files
                .createTempFile("restored-recovery-", ".akropia");
        try {
            getContext()
                    .getClassFromGuice(
                            org.akropia.model.campaign.GameModelDao.class)
                    .saveToFile(actualFile.toString(),
                            getContext().getClassFromGuice(
                                    org.akropia.gui.event.model.GameModelController.class)
                                    .getGameModel());
            final var actual = files.loadPlayerGame(actualFile.toFile());
            final var gson = new com.google.gson.Gson();
            assertEquals(gson.toJsonTree(expected), gson.toJsonTree(actual));
        } finally {
            Files.deleteIfExists(actualFile);
        }
    }

    public void verifyRecoveryAbsent(final RecoverySlot slot) {
        assertFalse(getContext().getClassFromGuice(PersistingTool.class)
                .getRecoveryFile(slot).exists());
    }

    public void verifyCurrentTurn(final int expectedTurn) {
        assertEquals(expectedTurn,
                getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void prepareCorruptTurnSnapshot() throws Exception {
        corruptedTurnSnapshot = getContext()
                .getClassFromGuice(
                        org.akropia.model.campaign.po.SavedGames.class)
                .nextTurnSnapshot(1);
        Files.writeString(corruptedTurnSnapshot.toPath(), "{");
    }

    /**
     * Keep the readable metadata of a recovery point but damage its game, so
     * the problem is found only when the point is loaded.
     */
    public void corruptRecoveryGame(final RecoverySlot slot) throws Exception {
        SavedGamesPage.replaceEntry(
                getContext().getClassFromGuice(PersistingTool.class)
                        .getRecoveryFile(slot),
                "game.json",
                "{".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public void obstructLatestPositionSave() throws Exception {
        java.nio.file.Files.createDirectory(getContext()
                .getClassFromGuice(PersistingTool.class)
                .getRecoveryFile(RecoverySlot.LATEST_POSITION).toPath());
    }

    public void removeLatestPositionObstruction() throws Exception {
        java.nio.file.Files.delete(getContext()
                .getClassFromGuice(PersistingTool.class)
                .getRecoveryFile(RecoverySlot.LATEST_POSITION).toPath());
    }

    public void attemptExitToMenu() {
        clickOnButtonWithId("exit");
        getRobot().clickOn("#menuMain");
        clickOnButtonWithId("saveConfirm");
    }

    private org.akropia.model.campaign.GameModel activeGameBeforeSaveFailure;
    private com.google.gson.JsonElement stateBeforeSaveFailure;

    public void rememberActiveGameBeforeSaveFailure() {
        activeGameBeforeSaveFailure = getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel();
        stateBeforeSaveFailure = new com.google.gson.Gson()
                .toJsonTree(activeGameBeforeSaveFailure.getModel().save());
    }

    public void verifyActiveGameUnchangedAfterSaveFailure() {
        assertSame(activeGameBeforeSaveFailure,
                getContext().getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                        .getGameModel());
        assertEquals(stateBeforeSaveFailure,
                new com.google.gson.Gson().toJsonTree(getModel().save()));
    }

    public void replaceSaveFolderWithFile() throws Exception {
        final Path folder = saveFolder();
        java.nio.file.Files.move(folder, savedFolderBackup(folder));
        java.nio.file.Files.writeString(folder, "not a directory");
    }

    public void restoreSaveFolder() throws Exception {
        final Path folder = saveFolder();
        java.nio.file.Files.delete(folder);
        java.nio.file.Files.move(savedFolderBackup(folder), folder);
    }

    public void blockDiagnosticFolder() throws Exception {
        final Path folder = diagnosticFolder();
        if (java.nio.file.Files.exists(folder)) {
            java.nio.file.Files.move(folder, savedFolderBackup(folder));
        }
        java.nio.file.Files.createDirectories(folder.getParent());
        java.nio.file.Files.writeString(folder, "not a directory");
    }

    public void unblockDiagnosticFolder() throws Exception {
        final Path folder = diagnosticFolder();
        java.nio.file.Files.delete(folder);
        if (java.nio.file.Files.exists(savedFolderBackup(folder))) {
            java.nio.file.Files.move(savedFolderBackup(folder), folder);
        }
    }

    public boolean isGridShown() {
        return getContext().getClassFromGuice(GamePreferences.class)
                .isGridShown();
    }

    public void verifyGridShown(final boolean expected) {
        assertEquals(expected, isGridShown(),
                "Settings were lost while the save location was unusable");
    }

    public String diagnosticFolderLine() {
        return "Diagnostic log folder: " + diagnosticFolder();
    }

    public void verifyDiagnosticRecord(final DiagnosticEvent event,
            final String forbiddenPlayerText) throws Exception {
        final Path log = getContext().getClassFromGuice(DiagnosticLog.class)
                .getActiveFile();
        assertTrue(java.nio.file.Files.exists(log),
                "Diagnostic log is missing: " + log);
        final String record = java.nio.file.Files.readString(log);
        assertTrue(record.contains(" build="), "Build identity is missing");
        assertTrue(record.contains(" event=" + event.name()),
                "Event " + event + " is missing");
        assertTrue(record.contains("\n    at org.akropia."),
                "Akropia frame is missing");
        assertFalse(record.contains("{"), "Raw JSON was recorded");
        assertFalse(record.contains(forbiddenPlayerText),
                "Player text was recorded");
        assertFalse(record.contains(System.getProperty("user.home")),
                "Home location was recorded");
    }

    private static Path saveFolder() {
        return Path.of(System.getProperty("user.home"), ".akropia");
    }

    private static Path savedFolderBackup(final Path folder) {
        return folder.resolveSibling(folder.getFileName() + ".saved");
    }

    private Path diagnosticFolder() {
        return getContext().getClassFromGuice(DiagnosticLog.class)
                .getDirectory();
    }

    public void chooseLoadFile(final File file) {
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.akropia.gui.FileSelectingService.class)
                        .loadFile(org.mockito.Mockito.any(File.class)))
                .thenReturn(file);
    }

    private File invalidSave;
    private byte[] invalidSaveBytes;
    private org.akropia.model.campaign.GameModel previousGame;
    private com.google.gson.JsonElement previousWorld;

    public void prepareInvalidSavedGame(final boolean autosave)
            throws Exception {
        final org.akropia.model.campaign.po.GameModelPoDao files = new org.akropia.model.campaign.po.GameModelPoDao();
        final org.akropia.model.campaign.po.GameModelPo data = files
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.akropia");
        data.getModel().getColonies().get(1).setLocation(Location.of(24, 13));
        invalidSave = autosave ? continueFile()
                : Files.createTempFile("invalid-game-", ".akropia").toFile();
        // A player save with valid metadata whose game data are invalid.
        PlayerSaveFixture.writeInvalidGame(invalidSave, data,
                "/maps/default-campaign-mission-2-thrive.akropia");
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel();
        previousWorld = previousGame == null ? null
                : new com.google.gson.Gson()
                        .toJsonTree(previousGame.getModel().save());
        if (!autosave) {
            chooseLoadFile(invalidSave);
        }
    }

    public void attemptInvalidSavedGame(final boolean autosave) {
        clickOnButtonWithId(autosave ? "buttonContinue" : "buttonLoad");
        if (!autosave) {
            javafx.application.Platform.runLater(
                    () -> org.akropia.gui.dialog.SavedGameFixtureLoader
                            .load(getContext().getClassFromGuice(
                                    org.akropia.gui.dialog.PersistingService.class)));
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void prepareInvalidDifficultySave(final String difficulty)
            throws Exception {
        // Copy the save Continue opens: the last successful save or load
        // (R-0387), which need not be the autosave slot.
        final File source = continueFile();
        // The recovery point is an archive; its game entry is edited as plain
        // scenario JSON.
        final com.google.gson.JsonObject data = new com.google.gson.JsonParser()
                .parse(new org.akropia.model.campaign.po.GameModelPoDao()
                        .loadGameJson(source))
                .getAsJsonObject();
        final com.google.gson.JsonObject model = data.getAsJsonObject("model");
        if (difficulty == null) {
            model.remove("difficulty");
        } else {
            model.addProperty("difficulty", difficulty);
        }
        invalidSave = Files.createTempFile("invalid-difficulty-", ".akropia")
                .toFile();
        // Keep the save's own metadata and replace only its game data.
        Files.copy(source.toPath(), invalidSave.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        SavedGamesPage.replaceEntry(invalidSave, "game.json", data.toString()
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel();
        previousWorld = previousGame == null ? null
                : new com.google.gson.Gson()
                        .toJsonTree(previousGame.getModel().save());
        chooseLoadFile(invalidSave);
    }

    public void prepareMalformedLoadFile(final String contents)
            throws Exception {
        invalidSave = Files.createTempFile("malformed-game-", ".akropia")
                .toFile();
        Files.writeString(invalidSave.toPath(), contents);
        // Deliberately malformed: it reaches the game as chosen.
        PlayerSaveFixture.chooseAsRejected(invalidSave);
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel();
        previousWorld = previousGame == null ? null
                : new com.google.gson.Gson()
                        .toJsonTree(previousGame.getModel().save());
        chooseLoadFile(invalidSave);
    }

    public void prepareMalformedContinueWithoutGame() throws Exception {
        final File save = getContext().getClassFromGuice(PersistingTool.class)
                .getAutoSaveFile();
        Files.writeString(save.toPath(), "{");
        invalidSave = save;
        invalidSaveBytes = Files.readAllBytes(save.toPath());
        previousGame = null;
        previousWorld = null;
        getContext()
                .getClassFromGuice(
                        org.akropia.gui.preferences.GamePreferences.class)
                .setGameInProgressSaveFile(save.getAbsolutePath());
    }

    public void attemptInvalidSavedGameWhilePlaying() {
        javafx.application.Platform.runLater(() -> getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameController.class)
                .loadModelFromFile(invalidSave));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyInvalidSavedGameMessage(final String expectedMessage)
            throws Exception {
        final String expectedText = expectedMessage + "\n\n"
                + diagnosticFolderLine();
        final Label message = getNodeFinder()
                .lookup((javafx.scene.Node node) -> node instanceof Label
                        && expectedText.equals(((Label) node).getText()))
                .queryAs(Label.class);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> message.getWidth() > 0
                        && message.getScene().getWidth() > 100
                        && message.getScene().getWindow().isShowing());
        assertEquals(expectedText, message.getText());
        assertTrue(message.isVisible());
    }

    public void verifyRejectedLoadPreservedGameAndFile() throws Exception {
        assertSame(previousGame,
                getContext().getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                        .getGameModel());
        assertArrayEquals(invalidSaveBytes,
                Files.readAllBytes(invalidSave.toPath()));
        if (previousGame != null) {
            assertEquals(previousWorld, new com.google.gson.Gson()
                    .toJsonTree(previousGame.getModel().save()));
        }
    }

    public void verifyRejectedSourceUnchanged() throws Exception {
        assertArrayEquals(invalidSaveBytes,
                Files.readAllBytes(invalidSave.toPath()));
    }

    public void verifyValidRetryReplacedGame() {
        assertNotNull(getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel());
        assertNotSame(previousGame,
                getContext().getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                        .getGameModel());
    }

    /** Leave the Load page with its visible return control. */
    public void closeLoadPage() {
        clickOnButtonWithId("savedGamesCancel");
    }

    /** The Load page stays open in the main window for another choice. */
    public void verifyLoadPageOpen() {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node list = getNodeFinder().lookup("#savedGamesList")
                .query();
        assertNotNull(list);
        assertSame(getContext().getPrimaryStage().getScene(), list.getScene());
        assertTrue(getButtonById("loadSelected").isVisible());
        assertTrue(getButtonById("savedGamesCancel").isVisible());
    }

    public void openLoadWithoutSelection() {
        clickOnButtonWithId("buttonLoad");
    }

    /**
     * @return bytes of the recovery save Continue opens: the last successful
     *         save or load (R-0387)
     */
    public byte[] readAutosave() throws Exception {
        return Files.readAllBytes(continueFile().toPath());
    }

    public FileTime getAutosaveLastModifiedTime() throws Exception {
        return Files.getLastModifiedTime(continueFile().toPath());
    }

    private File continueFile() {
        return getContext()
                .getClassFromGuice(
                        org.akropia.gui.dialog.PersistingService.class)
                .continueFile();
    }

    private String playthroughId;

    /** Remember the active playthrough for checks made from the menu. */
    public void rememberPlaythrough() {
        playthroughId = getContext()
                .getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                .getGameModel().getPlaythroughId();
    }

    private List<File> turnSnapshots() {
        return getContext()
                .getClassFromGuice(
                        org.akropia.model.campaign.po.SavedGames.class)
                .turnSnapshots();
    }

    /**
     * The remembered playthrough keeps turn snapshots of exactly these turns,
     * oldest write first (R-0388).
     */
    public void verifyTurnSnapshots(final Integer... expectedTurns) {
        final var files = new org.akropia.model.campaign.po.GameModelPoDao();
        assertEquals(List.of(expectedTurns),
                turnSnapshots().stream()
                        .map(file -> files.loadPlayerGame(file).getModel()
                                .getCalendar().getNumberOfPlayedTurns())
                        .collect(Collectors.toList()));
    }

    public void verifyCircularFilenames(final String newest) {
        final List<File> files = turnSnapshots();
        assertEquals(10, files.size());
        assertEquals(newest, files.get(files.size() - 1).getName());
        final var root = getContext().getClassFromGuice(PersistingTool.class)
                .getRootSaveDirectory().toPath().resolve("saves");
        for (final File file : files) {
            assertEquals(root, file.toPath().getParent());
            assertTrue(file.getName().matches("autosave-00[0-9]\\.akropia"));
        }
    }

    public void verifyTurnSnapshotCount(final int expected) {
        assertEquals(expected, turnSnapshots().size());
    }

    /** Continue opens the newest turn snapshot of the playthrough. */
    public void verifyContinueOpensNewestTurnSnapshot() {
        final List<File> snapshots = turnSnapshots();
        assertFalse(snapshots.isEmpty());
        assertEquals(snapshots.get(snapshots.size() - 1).getAbsoluteFile(),
                continueFile().getAbsoluteFile());
    }

    /**
     * The open Load page lists the turn snapshots as recovery points, newest
     * write first, each with its game year in the detail.
     */
    public void verifyTurnSnapshotOptions(final String... expectedYears) {
        final List<File> alphabetical = new java.util.ArrayList<>(
                turnSnapshots());
        alphabetical.sort(java.util.Comparator.comparing(File::getName));
        final var list = savedList();
        final var listed = list.getItems().stream()
                .filter(entry -> alphabetical.contains(entry.file))
                .collect(Collectors.toList());
        assertEquals(alphabetical, listed.stream().map(entry -> entry.file)
                .collect(Collectors.toList()));
        assertEquals(expectedYears.length, listed.size());
        for (int i = 0; i < listed.size(); i++) {
            final var entry = listed.get(i);
            assertEquals(
                    org.akropia.model.campaign.po.SavedGames.Kind.AUTOMATIC,
                    entry.kind);
            getRobot().interact(() -> list.getSelectionModel().select(entry));
            WaitForAsyncUtils.waitForFxEvents();
            final String detail = getLabeledById("savedGameSummary").getText();
            assertTrue(detail.contains(expectedYears[i]), detail);
        }
    }

    /** Load the oldest turn snapshot from the open Load page. */
    public void chooseOldestTurnSnapshot() {
        final File oldest = turnSnapshots().get(0);
        final var list = savedList();
        final var entry = list.getItems().stream()
                .filter(value -> value.file.equals(oldest)).findFirst()
                .orElseThrow();
        getRobot().interact(() -> list.getSelectionModel().select(entry));
        clickOnButtonWithId("loadSelected");
    }

    /**
     * Damage the game data of the newest turn snapshot, keeping its metadata.
     */
    public void corruptNewestTurnSnapshotGame() throws Exception {
        final List<File> snapshots = turnSnapshots();
        SavedGamesPage.replaceEntry(snapshots.get(snapshots.size() - 1),
                "game.json",
                "{".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Make the managed save folder unusable by placing a file there. */
    public void obstructManagedSaves() throws Exception {
        if (Files.isDirectory(managedSaves())) {
            Files.move(managedSaves(),
                    managedSaves().resolveSibling("saves-before-failure"));
        }
        Files.writeString(managedSaves(), "not a folder");
    }

    public void removeManagedSavesObstruction() throws Exception {
        Files.delete(managedSaves());
        final var backup = managedSaves()
                .resolveSibling("saves-before-failure");
        if (Files.exists(backup)) {
            Files.move(backup, managedSaves());
        }
    }

    private java.nio.file.Path managedSaves() {
        return getContext().getClassFromGuice(PersistingTool.class)
                .getRootSaveDirectory().toPath().resolve("saves");
    }

    public void verifyAutosaveLastModifiedTime(final FileTime expected)
            throws Exception {
        assertEquals(expected, getAutosaveLastModifiedTime(),
                "Continue must not rewrite the opened save");
    }

    public void verifyAutosaveChanged(final byte[] previous) throws Exception {
        assertFalse(java.util.Arrays.equals(previous, readAutosave()),
                "Menu entry must save the latest position");
    }

    public void verifyAutosaveUnchanged(final byte[] previous)
            throws Exception {
        assertArrayEquals(previous, readAutosave(),
                "Navigation must preserve the recovery save");
    }

    public void clickPierColonist(final MouseButton button) {
        getRobot().clickOn(getListOfNodes(".panel-port-pier .unit-icon").get(0),
                button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPierEquipmentBaseline(final int expectedUnits,
            final int expectedTools) {
        assertEquals(expectedUnits,
                getListOfNodes(".panel-port-pier .unit-icon").size());
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(100);
        assertTrue(PlaceEuropePier.class.isInstance(unit.getPlace()));
        assertEquals(expectedTools, unit.getTools());
        assertFalse(unit.isHoldingWeapons());
        assertFalse(unit.isMounted());
        assertTrue(getListOfNodes(".context-menu").isEmpty(),
                "No equipment menu appears");
    }

    public void verifyPurchaseOffers(final List<String> expected) {
        final List<String> names = getListOfNodes(".unitPanel .name").stream()
                .map(n -> ((Label) n).getText()).collect(Collectors.toList());
        assertEquals(expected, names, "Complete purchase roster");
    }

    public void closePurchaseDialog() {
        clickOnButtonWithId("buttonOk");
    }

    public void embarkPierColonist() throws Exception {
        getRobot()
                .drag(getListOfNodes(".panel-port-pier .unit-icon").get(0),
                        MouseButton.PRIMARY)
                .dropTo(PageComponentPanelDock.of(getContext())
                        .getListOfCrates().get(0));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(100).getPlace() instanceof PlaceCargoSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void disembarkToPier() throws Exception {
        getRobot()
                .drag(PageComponentPanelDock.of(getContext()).getListOfCrates()
                        .get(0), MouseButton.PRIMARY)
                .dropTo(getListOfNodes(".panel-port-pier").get(0));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(100).getPlace() instanceof PlaceEuropePier);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPassengerPlace(final int id, final Class<?> placeType,
            final int tools, final boolean guns) {
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(id);
        assertTrue(placeType.isInstance(unit.getPlace()));
        assertEquals(tools, unit.getTools());
        assertEquals(guns, unit.isHoldingWeapons());
    }

    public void verifyWagonArtworkSupported() {
        final Unit wagon = org.mockito.Mockito.mock(Unit.class);
        org.mockito.Mockito.when(wagon.getType())
                .thenReturn(org.akropia.model.UnitType.WAGON);
        final org.akropia.gui.image.ImageProvider provider = getContext()
                .getClassFromGuice(org.akropia.gui.image.ImageProvider.class);
        final javafx.scene.image.Image image = provider
                .getUnitImage(org.akropia.model.UnitType.WAGON);
        assertNotNull(image);
        assertSame(image,
                provider.getUnitImage(wagon, org.akropia.model.Direction.east));
        assertSame(image,
                provider.getUnitImage(wagon, org.akropia.model.Direction.west));
    }

    public void verifyConstructionQueueAvailable(final int population) {
        final Colony colony = getModel().getColonyAt(Location.of(24, 16)).get();
        assertEquals((long) population,
                colony.getColonyFields().stream()
                        .filter(field -> field.getUnit().isPresent()).count()
                        + colony.getConstructions().stream()
                                .flatMap(construction -> construction
                                        .getConstructionSlots().stream())
                                .filter(slot -> !slot.isEmpty()).count());
        assertTrue(getNodeFinder().lookup(".constructionPaneCarpenter").query()
                .isVisible(), "Construction must be discoverable");
        assertTrue(getButtonById("constructionOpen").isFocusTraversable());
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }
}
