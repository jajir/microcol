package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.setting.ScreenKeyboardShortcuts;
import org.akropia.gui.screen.setting.SettingButtonsView;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutController;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/**
 * The Settings editor of keyboard shortcuts: its draft rows, capture state and
 * transactional buttons.
 */
public class KeyboardShortcutsPage extends AbstractScreen {

    public KeyboardShortcutsPage(final TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId(SettingButtonsView.BUTTON_SHORTCUTS_ID);
    }

    public void startAdd(final ShortcutAction action) {
        clickInEditor(ScreenKeyboardShortcuts.id(action) + "-add");
    }

    public void startChange(final ShortcutAction action, final int index) {
        clickInEditor(ScreenKeyboardShortcuts.id(action) + "-binding-" + index);
    }

    public void remove(final ShortcutAction action, final int index) {
        clickInEditor(ScreenKeyboardShortcuts.id(action) + "-remove-" + index);
    }

    /**
     * Press the keys together, modifiers first, as a player does.
     *
     * @param keys modifiers followed by the key
     */
    public void pressKeys(final KeyCode... keys) {
        getRobot().push(keys);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * @param key key pressed with Command on macOS or Ctrl elsewhere
     */
    public void pressPlatformShortcut(final KeyCode key) {
        pressKeys(isMacPlatform() ? KeyCode.COMMAND : KeyCode.CONTROL, key);
    }

    public void restoreDefaults() {
        clickInEditor(ScreenKeyboardShortcuts.RESET_ID);
    }

    public void cancel() throws java.util.concurrent.TimeoutException {
        clickInEditor(ScreenKeyboardShortcuts.CANCEL_ID);
        waitForMainWindowFocus();
    }

    public void saveAndBack() throws java.util.concurrent.TimeoutException {
        clickInEditor(ScreenKeyboardShortcuts.SAVE_ID);
        waitForMainWindowFocus();
    }

    /**
     * When the editor closes, the window manager gives the keyboard back to the
     * main window asynchronously; the next keys go to it only then. A failed
     * save keeps the editor open and focused.
     */
    private void waitForMainWindowFocus()
            throws java.util.concurrent.TimeoutException {
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS,
                () -> getNodeFinder()
                        .lookup("#" + ScreenKeyboardShortcuts.SAVE_ID)
                        .tryQuery().isPresent()
                        || getPrimaryStage().isFocused());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickInEditor(final String id) {
        final Node node = getNodeFinder().lookup("#" + id).query();
        getRobot().interact(() -> {
            // Bring a row below the fold into the editor's viewport.
            final javafx.scene.control.ScrollPane scroll = getNodeFinder()
                    .lookup("#shortcutsRows")
                    .queryAs(javafx.scene.control.ScrollPane.class);
            final javafx.geometry.Bounds content = scroll.getContent()
                    .getLayoutBounds();
            final javafx.geometry.Bounds target = scroll.getContent()
                    .sceneToLocal(node.localToScene(node.getBoundsInLocal()));
            final double hidden = content.getHeight()
                    - scroll.getViewportBounds().getHeight();
            if (hidden > 0 && scroll.getContent() != node
                    && isInside(node, scroll.getContent())) {
                scroll.setVvalue(Math.max(0,
                        Math.min(1, (target.getMinY() - 20) / hidden)));
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().clickOn(node);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static boolean isInside(final Node node, final Node parent) {
        for (Node current = node; current != null; current = current
                .getParent()) {
            if (current == parent) {
                return true;
            }
        }
        return false;
    }

    private List<String> bindings(final ShortcutAction action) {
        final List<String> out = new ArrayList<>();
        for (int i = 0;; i++) {
            final Button binding = getNodeFinder()
                    .lookup("#" + ScreenKeyboardShortcuts.id(action)
                            + "-binding-" + i)
                    .tryQueryAs(Button.class).orElse(null);
            if (binding == null) {
                return out;
            }
            out.add((String) binding.getUserData());
        }
    }

    /**
     * @param action   checked action
     * @param expected displayed combinations in order; none means unbound
     */
    public void verifyBindings(final ShortcutAction action,
            final String... expected) {
        assertEquals(List.of(expected), bindings(action), action.getKey());
        if (expected.length == 0) {
            assertNotNull(
                    getNodeFinder().lookup("#"
                            + ScreenKeyboardShortcuts.id(action) + "-unbound")
                            .tryQuery().orElse(null),
                    action.getKey() + " shows Unbound");
        }
    }

    /**
     * @param action checked action
     * @param key    key shown with Command on macOS or Ctrl elsewhere
     */
    public void verifyPlatformBinding(final ShortcutAction action,
            final KeyCode key) {
        verifyBindings(action, isMacPlatform() ? "⌘" + key.getName()
                : "Ctrl+" + key.getName());
    }

    public void verifyStatus(final String expected) {
        assertEquals(expected,
                getLabeledById(ScreenKeyboardShortcuts.STATUS_ID).getText());
    }

    public void verifySaveEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById(ScreenKeyboardShortcuts.SAVE_ID).isDisabled());
    }

    public void verifyEditorOpen(final boolean expected) {
        assertEquals(expected,
                getNodeFinder().lookup("#" + ScreenKeyboardShortcuts.SAVE_ID)
                        .tryQuery()
                        .map(node -> node.getScene().getWindow().isShowing())
                        .orElse(false));
    }

    public void verifyFocused(final ShortcutAction action,
            final String control) {
        final String id = ScreenKeyboardShortcuts.id(action) + "-" + control;
        final Node node = getNodeFinder().lookup("#" + id).query();
        assertTrue(node.isFocused(), id + " keeps the keyboard focus");
    }

    /**
     * @param expected saved action keys and written bindings of the running
     *                 platform
     */
    public void verifySavedDeviations(final Map<String, String> expected) {
        final String platform = getContext()
                .getClassFromGuice(ShortcutController.class).getPlatform()
                .getId();
        assertEquals(expected,
                getContext().getClassFromGuice(GamePreferences.class)
                        .getShortcutOverrides(platform));
    }

    /**
     * Click into a text field, move to its end and press one combination as
     * real key events rather than typed text.
     *
     * @param fieldId text field
     * @param keys    modifiers followed by the key
     */
    public void pressKeysIn(final String fieldId, final KeyCode... keys) {
        getRobot().clickOn("#" + fieldId).push(KeyCode.END).push(keys);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private java.io.File settingFile() {
        return new java.io.File(getContext()
                .getClassFromGuice(
                        org.akropia.model.campaign.po.PersistingTool.class)
                .getRootSaveDirectory(), ".setting.json");
    }

    private java.io.File settingBackup() {
        return new java.io.File(settingFile().getParentFile(),
                "r0432-setting-backup.json");
    }

    /**
     * Replace the settings file with a folder of the same name, so the next
     * write fails while the running game keeps its last known settings.
     */
    public void breakSettingsStorage() throws java.io.IOException {
        java.nio.file.Files.move(settingFile().toPath(),
                settingBackup().toPath());
        java.nio.file.Files.createDirectory(settingFile().toPath());
    }

    /**
     * Put the settings file back; does nothing when storage is not broken.
     */
    public void restoreSettingsStorage() throws java.io.IOException {
        if (!settingBackup().exists()) {
            return;
        }
        if (settingFile().isDirectory()) {
            java.nio.file.Files.delete(settingFile().toPath());
        }
        java.nio.file.Files.move(settingBackup().toPath(),
                settingFile().toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @param action   checked action
     * @param expected label of its first active binding, empty when unbound
     */
    public void verifyActiveBinding(final ShortcutAction action,
            final String expected) {
        assertEquals(expected, getContext()
                .getClassFromGuice(ShortcutController.class).label(action));
    }

    /**
     * @param title    dialog heading
     * @param platform platform caption
     * @param buttons  Restore defaults, Cancel and Save and back texts
     */
    public void verifyEditorTexts(final String title, final String platform,
            final String... buttons) {
        assertEquals(title, getLabeledById("shortcutsTitle").getText());
        assertEquals(platform,
                getLabeledById(ScreenKeyboardShortcuts.PLATFORM_ID).getText());
        assertEquals(List.of(buttons), List.of(
                getButtonById(ScreenKeyboardShortcuts.RESET_ID).getText(),
                getButtonById(ScreenKeyboardShortcuts.CANCEL_ID).getText(),
                getButtonById(ScreenKeyboardShortcuts.SAVE_ID).getText()));
    }

    /**
     * @param action   checked action
     * @param label    its localized name
     * @param addLabel text of its Add button
     */
    public void verifyActionTexts(final ShortcutAction action,
            final String label, final String addLabel) {
        final String id = ScreenKeyboardShortcuts.id(action);
        assertEquals(label, getLabeledById(id + "-label").getText());
        assertEquals(addLabel, getButtonById(id + "-add").getText());
    }

    /**
     * Every row, group heading and button shows its whole text inside the main
     * window, without ellipsis, overlap or horizontal scrolling; the fixed
     * header stays above the rows.
     */
    public void verifyCompleteLayout() {
        verifyOnlyMainWindowShowing();
        getRobot().interact(() -> {
            final Node editor = getNodeFinder()
                    .lookup("#keyboardShortcutsScreen").query();
            final javafx.geometry.Bounds editorBounds = editor
                    .localToScene(editor.getBoundsInLocal());
            assertEquals(getPrimaryStage().getScene().getWidth(),
                    editorBounds.getWidth(), 1,
                    "Editor uses the main window width");
            assertEquals(getPrimaryStage().getScene().getHeight(),
                    editorBounds.getHeight(), 1,
                    "Editor uses the main window height");
            final Node save = getNodeFinder()
                    .lookup("#" + ScreenKeyboardShortcuts.SAVE_ID).query();
            final javafx.scene.Scene scene = save.getScene();
            final javafx.stage.Window window = scene.getWindow();
            final javafx.geometry.Rectangle2D screen = javafx.stage.Screen
                    .getScreensForRectangle(window.getX(), window.getY(),
                            window.getWidth(), window.getHeight())
                    .get(0).getVisualBounds();
            assertTrue(
                    window.getWidth() <= screen.getWidth() + 0.5
                            && window.getHeight() <= screen.getHeight() + 0.5,
                    "The editor fits the screen");
            final javafx.scene.control.ScrollPane scroll = getNodeFinder()
                    .lookup("#shortcutsRows")
                    .queryAs(javafx.scene.control.ScrollPane.class);
            assertTrue(
                    scroll.getContent().getLayoutBounds().getWidth() <= scroll
                            .getViewportBounds().getWidth() + 0.5,
                    "Rows never need horizontal scrolling");
            final List<javafx.scene.control.Labeled> labels = new ArrayList<>();
            collectLabels(scene.getRoot(), labels);
            assertTrue(labels.size() > ShortcutAction.values().length * 2,
                    "Every action row is laid out");
            for (final javafx.scene.control.Labeled labeled : labels) {
                if (labeled.getText() == null || labeled.getText().isEmpty()) {
                    continue;
                }
                final javafx.scene.text.Text text = (javafx.scene.text.Text) labeled
                        .lookup(".text");
                assertNotNull(text, labeled.getText());
                assertEquals(labeled.getText(), text.getText(),
                        "Complete text of " + labeled.getId());
                final javafx.geometry.Bounds bounds = labeled
                        .localToScene(labeled.getBoundsInLocal());
                assertTrue(
                        bounds.getMinX() >= -0.5
                                && bounds.getMaxX() <= scene.getWidth() + 0.5,
                        labeled.getText() + " stays inside the editor");
            }
            double previousRight = 0;
            for (final String id : new String[] {
                    ScreenKeyboardShortcuts.RESET_ID,
                    ScreenKeyboardShortcuts.CANCEL_ID,
                    ScreenKeyboardShortcuts.SAVE_ID }) {
                final Node button = getNodeFinder().lookup("#" + id).query();
                final javafx.geometry.Bounds bounds = button
                        .localToScene(button.getBoundsInLocal());
                assertTrue(bounds.getMinX() >= previousRight,
                        "Header actions keep reset, cancel, save order without overlap");
                previousRight = bounds.getMaxX();
                assertTrue(bounds.getMaxY() < scroll
                        .localToScene(scroll.getBoundsInLocal()).getMinY(),
                        id + " stays above the scrolling rows");
                assertTrue(
                        bounds.getMaxX() <= scene.getWidth() + 0.5
                                && bounds.getMaxY() <= scene.getHeight() + 0.5,
                        id + " is fully visible");
            }
        });
    }

    private static void collectLabels(final Node node,
            final List<javafx.scene.control.Labeled> out) {
        if (!node.isVisible()) {
            return;
        }
        if (node instanceof javafx.scene.control.Labeled) {
            out.add((javafx.scene.control.Labeled) node);
        }
        if (node instanceof javafx.scene.Parent) {
            for (final Node child : ((javafx.scene.Parent) node)
                    .getChildrenUnmodifiable()) {
                collectLabels(child, out);
            }
        }
    }

    /**
     * No dialog or popup opened: only the main window is showing.
     */
    public void verifyOnlyMainWindowShowing() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(List.of(getPrimaryStage()),
                new ArrayList<>(javafx.stage.Window.getWindows()
                        .filtered(javafx.stage.Window::isShowing)));
    }

}
