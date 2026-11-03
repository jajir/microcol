package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import org.akropia.gui.screen.menu.ButtonsPanelView;
import org.testfx.util.WaitForAsyncUtils;

public class WelcomePage extends AbstractMenuScreen {

    /** Main-menu buttons from top to bottom. */
    private static final List<String> MENU_IDS = List.of("buttonContinue",
            "buttonLoad", "buttonCampaign", "buttonStartFreeGame",
            "buttonHallOfFame", "buttonSetting", "buttonExit");

    public static WelcomePage of(final TestContext context) {
        return new WelcomePage(context);
    }

    WelcomePage(final TestContext context) {
        super(context);
        WaitForAsyncUtils.waitForFxEvents();
        verifyMainScreen();
    }

    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The center image, repeated edges and canvas fills use identical pigments.
     */
    public void verifyMenuPalette() {
        getRobot().interact(() -> {
            final javafx.scene.image.WritableImage frame = snapshotScene(
                    getContext().getPrimaryStage().getScene());
            final int cx = (int) frame.getWidth() / 2;
            final int cy = (int) frame.getHeight() / 2;
            final int[] rows = { cy - 300, cy, cy + 300 };
            final int[] colors = { 0xFFF9EC, 0x649B97, 0xC5C18A };
            for (int i = 0; i < rows.length; i++) {
                for (int x : new int[] { cx + 300, cx + 389, cx + 393,
                        (int) frame.getWidth() - 10 }) {
                    assertEquals(colors[i],
                            frame.getPixelReader().getArgb(x, rows[i])
                                    & 0xFFFFFF,
                            "Continuous menu pigment at " + x + "," + rows[i]);
                }
            }
        });
    }

    public void setMenuColumnWidth(final double width) {
        getRobot().interact(() -> {
            final VBox column = getNodeFinder().lookup(".game-menu-inner")
                    .queryAs(VBox.class);
            column.setPrefWidth(width);
            column.setMaxWidth(width);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyOpeningControls(final boolean czech,
            final boolean iconsOnly) {
        getRobot().interact(() -> {
            final List<String> ids = MENU_IDS;
            // Load names the platform shortcut: Command on macOS, Ctrl
            // elsewhere.
            final String load = isMacPlatform() ? " (⌘O)" : " (Ctrl+O)";
            final List<String> labels = czech
                    ? List.of("Pokračovat", "Nahrát hru" + load,
                            "Spustit kampaň", "Nová neomezená hra", "Síň slávy",
                            "Nastavení", "Ukončit hru")
                    : List.of("Continue", "Load saved game" + load,
                            "Play campaign", "New free play game",
                            "Hall of Fame", "Setting", "Quit Akropia");
            // Each button keeps its artwork; column 1 is the retired
            // Recovery points entry (R-0376).
            final List<Integer> columns = List.of(0, 2, 3, 4, 5, 6, 7);
            final VBox column = getNodeFinder().lookup(".game-menu-inner")
                    .queryAs(VBox.class);
            assertEquals(ids.size(), column.getChildren().size());
            Bounds previous = null;
            for (int i = 0; i < ids.size(); i++) {
                final Button button = (Button) column.getChildren().get(i);
                assertEquals(ids.get(i), button.getId());
                assertEquals(labels.get(i), button.getText());
                assertEquals(labels.get(i), button.getAccessibleText());
                assertNull(button.getTooltip());
                assertEquals("Playpen Sans Regular",
                        button.getFont().getName());
                assertEquals(14, button.getFont().getSize(), 0.01);
                assertEquals(new Insets(6, 3, 7, 3), button.getPadding());
                assertEquals(57, button.getHeight(), 0.01);
                assertTrue(button.getBorder().getImages().get(0).getImage()
                        .getUrl().endsWith("game-toolbar-frame.png"));
                final ImageView icon = (ImageView) button.getGraphic();
                assertEquals(32, icon.getFitWidth(), 0.01);
                assertEquals(32, icon.getFitHeight(), 0.01);
                assertEquals(columns.get(i) * 96, icon.getViewport().getMinX(),
                        0.01);
                assertEquals(
                        iconsOnly ? ContentDisplay.GRAPHIC_ONLY
                                : ContentDisplay.LEFT,
                        button.getContentDisplay());
                final Bounds bounds = button
                        .localToScene(button.getBoundsInLocal());
                assertEquals(80, bounds.getMinX(), 0.01);
                assertTrue(bounds.getMinY() >= 0);
                assertTrue(bounds.getMaxY() <= button.getScene().getHeight());
                if (previous != null) {
                    assertEquals(previous.getMinX(), bounds.getMinX(), 0.01);
                    assertEquals(8, bounds.getMinY() - previous.getMaxY(),
                            0.01);
                }
                if (!iconsOnly) {
                    final javafx.scene.text.Text text = (javafx.scene.text.Text) button
                            .lookup(".text");
                    assertEquals(button.getText(), text.getText());
                    assertEquals(Math.ceil(text.getLayoutBounds().getWidth()
                            + icon.getFitWidth() + button.getGraphicTextGap()
                            + button.getInsets().getLeft()
                            + button.getInsets().getRight()), button.getWidth(),
                            0.01, "Button fits its full label and icon");
                } else {
                    assertEquals(column.getWidth(), button.getWidth(), 0.01,
                            "Compact controls fit the available column");
                }
                previous = bounds;
            }
            final Label title = getNodeFinder()
                    .lookup(".welcome-screen .main-title").queryAs(Label.class);
            assertEquals("Akropia", title.getText());
            final Bounds titleBounds = title
                    .localToScene(title.getBoundsInLocal());
            final Button first = (Button) column.getChildren().get(0);
            assertTrue(
                    titleBounds.getMaxY() < first
                            .localToScene(first.getBoundsInLocal()).getMinY(),
                    "Title and waves precede the buttons");
            assertEquals(ContentDisplay.BOTTOM, title.getContentDisplay());
            assertTrue(((ImageView) title.getGraphic()).getImage().getUrl()
                    .endsWith("menu-title-waves.png"));
        });
    }

    /**
     * The menu shows exactly these labels in order, without a separate Recovery
     * points entry. Load also names the platform shortcut: Command on macOS,
     * Ctrl elsewhere.
     *
     * @param labels expected labels, Load without its shortcut
     */
    public void verifyMenuEntries(final List<String> labels) {
        final String load = isMacPlatform() ? " (⌘O)" : " (Ctrl+O)";
        getRobot().interact(() -> {
            final VBox column = getNodeFinder().lookup(".game-menu-inner")
                    .queryAs(VBox.class);
            assertEquals(MENU_IDS.size(), column.getChildren().size());
            for (int i = 0; i < MENU_IDS.size(); i++) {
                final Button button = (Button) column.getChildren().get(i);
                assertEquals(MENU_IDS.get(i), button.getId());
                assertEquals(
                        labels.get(i) + (ButtonsPanelView.BUTTON_LOAD_ID
                                .equals(MENU_IDS.get(i)) ? load : ""),
                        button.getText());
            }
            assertTrue(getNodeFinder().lookup("#buttonRecovery").queryAll()
                    .isEmpty(), "No separate Recovery points entry");
        });
    }

    /** Move keyboard focus with Tab until the given menu button has it. */
    public void tabToMenuButton(final String id) {
        for (int i = 0; i <= 2 * MENU_IDS.size() && !isFocused(id); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifyMenuButtonFocused(final String id) {
        assertTrue(isFocused(id), id + " has keyboard focus");
    }

    private boolean isFocused(final String id) {
        final javafx.scene.Node owner = getContext().getPrimaryStage()
                .getScene().getFocusOwner();
        return owner != null && id.equals(owner.getId());
    }

    public void verifyHallOfFameOpen() {
        assertTrue(isCssIdShown("hallOfFameDialog"));
    }

    public void verifyContinueAvailable(final boolean expected) {
        assertEquals(expected, !getButtonById("buttonContinue").isDisabled());
    }

    public void pressKeys(final KeyCode... keys) {
        getRobot().push(keys);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public SettingPage openSetting() {
        verifyMainScreen();
        clickOnButtonWithId(ButtonsPanelView.BUTTON_SETTING_ID);
        return SettingPage.of(getContext());
    }

    public WelcomePage verifyMainScreen() {
        verifyMainTitle(realTitle -> {
            assertEquals("Akropia", realTitle);
        });
        return this;
    }

    public GamePage loadGame() {
        clickOnButtonWithId(ButtonsPanelView.BUTTON_LOAD_ID);
        clickOnButtonWithId("importSave");
        WaitForAsyncUtils.waitForFxEvents();
        if (!getNodeFinder().lookup("#saveDiscard").queryAll().isEmpty()) {
            clickOnButtonWithId("saveDiscard");
        }
        return GamePage.of(getContext());
    }

    public GamePage startFreeGame() {
        return openFreePlaySetup().start();
    }

    public FreePlaySetupPage openFreePlaySetup() {
        clickOnButtonWithId(ButtonsPanelView.BUTTON_START_FREE_GAME_ID);
        return new FreePlaySetupPage(getContext());
    }

}
