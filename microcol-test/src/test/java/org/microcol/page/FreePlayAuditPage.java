package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.Unit;
import org.microcol.model.Colony;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.unit.UnitFreeColonist;
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
        final org.microcol.gui.Point point = getContext().getArea()
                .convertToCanvasPoint(location)
                .add(getPrimaryStage().getX(), getPrimaryStage().getY())
                .add(org.microcol.gui.Tile.TILE_CENTER);
        getRobot().robotContext().getMouseRobot()
                .move(new javafx.geometry.Point2D(point.getX(), point.getY()));
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
        assertTrue(getListOfNodes(".main-title").stream()
                .filter(n -> n instanceof Label).map(n -> ((Label) n).getText())
                .anyMatch(t -> t.contains(expectedTitle)), "Report title");
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

    public void openRecoveryChooser() {
        clickOnButtonWithId("buttonRecovery");
    }

    public void verifyRecoveryMenuLabel(final String expected) {
        assertEquals(expected, getLabeledById("buttonRecovery").getText());
    }

    public void verifyRecoveryChooserLabels(final String title,
            final String load, final String cancel) {
        assertEquals(title,
                getNodeFinder().lookup(title).queryAs(Label.class).getText());
        assertEquals(load, getLabeledById("recoveryLoad").getText());
        assertEquals(cancel, getLabeledById("recoveryCancel").getText());
    }

    private java.util.Map<RecoverySlot, byte[]> recoveryBytes;

    public void rememberRecoveryBytes() throws Exception {
        recoveryBytes = new java.util.EnumMap<>(RecoverySlot.class);
        for (RecoverySlot slot : RecoverySlot.values()) {
            final File file = getContext()
                    .getClassFromGuice(PersistingTool.class)
                    .getRecoveryFile(slot);
            if (file.isFile()) {
                recoveryBytes.put(slot, Files.readAllBytes(file.toPath()));
            }
        }
    }

    public void verifyRecoveryBytesUnchanged() throws Exception {
        for (var entry : recoveryBytes.entrySet()) {
            final File file = getContext()
                    .getClassFromGuice(PersistingTool.class)
                    .getRecoveryFile(entry.getKey());
            assertArrayEquals(entry.getValue(),
                    Files.readAllBytes(file.toPath()));
        }
    }

    public void verifyRecoveryOption(final RecoverySlot slot,
            final boolean available, final String date) {
        final javafx.scene.control.RadioButton option = getNodeFinder()
                .lookup("#recovery" + slot.name())
                .queryAs(javafx.scene.control.RadioButton.class);
        assertEquals(available, !option.isDisabled());
        assertTrue(option.getText().contains(date), option.getText());
        if (available) {
            final java.io.File file = getContext()
                    .getClassFromGuice(PersistingTool.class)
                    .getRecoveryFile(slot);
            final String modified = java.time.format.DateTimeFormatter
                    .ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)
                    .withLocale(getContext()
                            .getClassFromGuice(org.microcol.i18n.I18n.class)
                            .getCurrentLocale())
                    .format(file.toPath().toFile().lastModified() > 0
                            ? java.time.Instant
                                    .ofEpochMilli(file.lastModified())
                                    .atZone(java.time.ZoneId.systemDefault())
                            : java.time.Instant.EPOCH
                                    .atZone(java.time.ZoneId.systemDefault()));
            assertTrue(option.getText().contains(modified), option.getText());
        }
    }

    public void chooseRecovery(final RecoverySlot slot) {
        getRobot().clickOn("#recovery" + slot.name());
        clickOnButtonWithId("recoveryLoad");
    }

    public void cancelRecovery() {
        clickOnButtonWithId("recoveryCancel");
    }

    public void verifyRecoveryTurn(final RecoverySlot slot,
            final int expectedTurn) {
        final java.io.File file = getContext()
                .getClassFromGuice(PersistingTool.class).getRecoveryFile(slot);
        final org.microcol.model.campaign.po.GameModelPo saved = new org.microcol.model.campaign.po.GameModelPoDao()
                .loadFromFile(file);
        assertEquals(expectedTurn,
                saved.getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void verifyLoadedRecoveryMatchesSlot(final RecoverySlot slot)
            throws Exception {
        final File source = getContext().getClassFromGuice(PersistingTool.class)
                .getRecoveryFile(slot);
        final var files = new org.microcol.model.campaign.po.GameModelPoDao();
        final var expected = files.loadFromFile(source);
        final java.nio.file.Path actualFile = Files
                .createTempFile("restored-recovery-", ".microcol");
        try {
            getContext()
                    .getClassFromGuice(
                            org.microcol.model.campaign.GameModelDao.class)
                    .saveToFile(actualFile.toString(),
                            getContext().getClassFromGuice(
                                    org.microcol.gui.event.model.GameModelController.class)
                                    .getGameModel());
            final var actual = files.loadFromFile(actualFile.toFile());
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

    public void corruptRecovery(final RecoverySlot slot) throws Exception {
        java.nio.file.Files.writeString(
                getContext().getClassFromGuice(PersistingTool.class)
                        .getRecoveryFile(slot).toPath(),
                "{");
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
    }

    private org.microcol.model.campaign.GameModel activeGameBeforeSaveFailure;
    private com.google.gson.JsonElement stateBeforeSaveFailure;

    public void rememberActiveGameBeforeSaveFailure() {
        activeGameBeforeSaveFailure = getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
                .getGameModel();
        stateBeforeSaveFailure = new com.google.gson.Gson()
                .toJsonTree(activeGameBeforeSaveFailure.getModel().save());
    }

    public void verifyActiveGameUnchangedAfterSaveFailure() {
        assertSame(activeGameBeforeSaveFailure,
                getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
                        .getGameModel());
        assertEquals(stateBeforeSaveFailure,
                new com.google.gson.Gson().toJsonTree(getModel().save()));
    }

    public void chooseLoadFile(final File file) {
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.Mockito.any(File.class)))
                .thenReturn(file);
    }

    private File invalidSave;
    private byte[] invalidSaveBytes;
    private org.microcol.model.campaign.GameModel previousGame;
    private com.google.gson.JsonElement previousWorld;

    public void prepareInvalidSavedGame(final boolean autosave)
            throws Exception {
        final org.microcol.model.campaign.po.GameModelPoDao files = new org.microcol.model.campaign.po.GameModelPoDao();
        final org.microcol.model.campaign.po.GameModelPo data = files
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies().get(1).setLocation(Location.of(24, 13));
        invalidSave = autosave
                ? getContext().getClassFromGuice(PersistingTool.class)
                        .getAutoSaveFile()
                : Files.createTempFile("invalid-game-", ".microcol").toFile();
        files.saveToFile(invalidSave.getAbsolutePath(), data);
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
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
    }

    public void prepareInvalidDifficultySave(final String difficulty)
            throws Exception {
        final File source = getContext().getClassFromGuice(PersistingTool.class)
                .getAutoSaveFile();
        final com.google.gson.JsonObject data = new com.google.gson.JsonParser()
                .parse(Files.readString(source.toPath())).getAsJsonObject();
        final com.google.gson.JsonObject model = data.getAsJsonObject("model");
        if (difficulty == null) {
            model.remove("difficulty");
        } else {
            model.addProperty("difficulty", difficulty);
        }
        invalidSave = Files.createTempFile("invalid-difficulty-", ".microcol")
                .toFile();
        Files.writeString(invalidSave.toPath(), data.toString());
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
                .getGameModel();
        previousWorld = previousGame == null ? null
                : new com.google.gson.Gson()
                        .toJsonTree(previousGame.getModel().save());
        chooseLoadFile(invalidSave);
    }

    public void prepareMalformedLoadFile(final String contents)
            throws Exception {
        invalidSave = Files.createTempFile("malformed-game-", ".microcol")
                .toFile();
        Files.writeString(invalidSave.toPath(), contents);
        invalidSaveBytes = Files.readAllBytes(invalidSave.toPath());
        previousGame = getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
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
                        org.microcol.gui.preferences.GamePreferences.class)
                .setGameInProgressSaveFile(save.getName());
    }

    public void attemptInvalidSavedGameWhilePlaying() {
        javafx.application.Platform.runLater(() -> getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameController.class)
                .loadModelFromFile(invalidSave));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyInvalidSavedGameMessage(final String expectedMessage)
            throws Exception {
        final Label message = getNodeFinder().lookup(expectedMessage)
                .queryAs(Label.class);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> message.getWidth() > 0
                        && message.getScene().getWidth() > 100
                        && message.getScene().getWindow().isShowing());
        assertEquals(expectedMessage, message.getText());
        assertTrue(message.isVisible());
    }

    public void verifyRejectedLoadPreservedGameAndFile() throws Exception {
        assertSame(previousGame,
                getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
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
                        org.microcol.gui.event.model.GameModelController.class)
                .getGameModel());
        assertNotSame(previousGame,
                getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
                        .getGameModel());
    }

    public void cancelLoadChooser() {
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.Mockito.any(java.io.File.class)))
                .thenReturn(null);
        clickOnButtonWithId("buttonLoad");
    }

    public byte[] readAutosave() throws Exception {
        return Files.readAllBytes(
                getContext().getClassFromGuice(PersistingTool.class)
                        .getAutoSaveFile().toPath());
    }

    public FileTime getAutosaveLastModifiedTime() throws Exception {
        return Files.getLastModifiedTime(
                getContext().getClassFromGuice(PersistingTool.class)
                        .getAutoSaveFile().toPath());
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
        assertFalse(unit.isHoldingGuns());
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
        assertEquals(guns, unit.isHoldingGuns());
    }

    public void verifyWagonArtworkSupported() {
        final Unit wagon = org.mockito.Mockito.mock(Unit.class);
        org.mockito.Mockito.when(wagon.getType())
                .thenReturn(org.microcol.model.UnitType.WAGON);
        final org.microcol.gui.image.ImageProvider provider = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        final javafx.scene.image.Image image = provider
                .getUnitImage(org.microcol.model.UnitType.WAGON);
        assertNotNull(image);
        assertSame(image, provider.getUnitImage(wagon,
                org.microcol.model.Direction.east));
        assertSame(image, provider.getUnitImage(wagon,
                org.microcol.model.Direction.west));
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
