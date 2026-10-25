package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import org.microcol.model.Calendar.Season;
import org.testfx.util.WaitForAsyncUtils;

/** Interactions and observations of the real 07B game toolbar. */
public final class GameToolbarPage extends AbstractScreen {
    private static final List<String> IDS = List.of("coloniesMenu", "congress",
            "center", "statistics", "turnReport", "goals", "help", "europe",
            "nextTurn", "retire", "exit");

    public GameToolbarPage(final TestContext context) {
        super(context);
    }

    public void hover(final String id) {
        getRobot().moveTo("#" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void focusCenterWithTab() {
        getRobot().push(KeyCode.O);
        getRobot().push(KeyCode.ESCAPE);
        getRobot().push(KeyCode.TAB);
        getRobot().push(KeyCode.TAB);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void activateFocusedButton() {
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyUnitUnchanged(final int id, final int movement,
            final int turn) {
        assertEquals(id, getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.SelectedUnitManager.class)
                .getSelectedUnit().orElseThrow().getId());
        assertEquals(movement, getModel().getUnitById(id).getActionPoints());
        assertEquals(turn, getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void openColoniesWithKeyboard() {
        getRobot().moveTo("#labelEra");
        getRobot().push(KeyCode.O);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dismissColonies(final boolean outside) {
        if (outside) {
            getRobot().clickOn("#labelEra");
        } else {
            getRobot().push(KeyCode.ESCAPE);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseFirstColony() {
        getRobot().push(KeyCode.DOWN, KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyControls(final boolean czech, final boolean wrapped) {
        getRobot().interact(() -> {
            final String[] labels = czech
                    ? new String[] { "Kolonie", "Kongres", "Vycentrovat",
                            "Statistiky", "Hlášení", "Cíle", "Nápověda",
                            "Evropa", "Další tah", "Ukončit", "Odejít" }
                    : new String[] { "Colonies", "Congress", "Center",
                            "Statistics", "Report", "Goals", "Help", "Europe",
                            "Next turn", "Retire", "Leave" };
            final FlowPane bar = getNodeFinder().lookup(".buttonPanel")
                    .queryAs(FlowPane.class);
            final double firstY = getLabeledById("coloniesMenu").getLayoutY();
            boolean anotherRow = false;
            for (int i = 0; i < IDS.size(); i++) {
                final Labeled control = getLabeledById(IDS.get(i));
                assertEquals(labels[i], control.getText(), control.getId());
                assertEquals("Playpen Sans Regular",
                        control.getFont().getName());
                assertEquals(14, control.getFont().getSize(), 0.01);
                assertTrue(control.getGraphic() instanceof ImageView);
                assertTrue(control.getStyleClass()
                        .contains("game-toolbar-control"));
                if (!control.isManaged()) {
                    continue;
                }
                assertEquals(ContentDisplay.LEFT, control.getContentDisplay(),
                        control.getId());
                final Bounds iconBounds = control.getGraphic()
                        .localToScene(control.getGraphic().getBoundsInLocal());
                final Node text = control.lookup(".text");
                final Bounds textBounds = text
                        .localToScene(text.getBoundsInLocal());
                assertTrue(iconBounds.getMaxX() < textBounds.getMinX(),
                        control.getId() + " name must follow its icon");
                assertEquals(iconBounds.getCenterY(), textBounds.getCenterY(),
                        2,
                        control.getId() + " icon and name must share one row");
                assertTrue(control.getHeight() <= 64,
                        control.getId() + " must keep the toolbar compact");
                assertTrue(control.getWidth() + 1 >= control.prefWidth(-1),
                        control.getId());
                final Bounds bounds = control
                        .localToScene(control.getBoundsInLocal());
                assertTrue(bounds.getMinX() >= 0
                        && bounds.getMaxX() <= bar.getScene().getWidth(),
                        control.getId());
                assertTrue(bounds.getMaxY() <= bar
                        .localToScene(bar.getBoundsInLocal()).getMaxY() + 1);
                anotherRow |= Math.abs(control.getLayoutY() - firstY) > 20;
            }
            assertEquals(wrapped, anotherRow);
            final Node map = getNodeFinder().lookup("#canvas").query();
            assertTrue(map.localToScene(map.getBoundsInLocal()).getMinY() >= bar
                    .localToScene(bar.getBoundsInLocal()).getMaxY());
        });
    }

    public void verifySeason(final Season expected, final int accent) {
        getRobot().interact(() -> {
            assertEquals(expected,
                    getModel().getCalendar().getCurrentSeason().orElse(null));
            for (final String id : IDS) {
                final Image icon = ((ImageView) getLabeledById(id).getGraphic())
                        .getImage();
                assertEquals(48, icon.getWidth());
                assertEquals(48, icon.getHeight());
                int inkPixels = 0;
                int accentPixels = 0;
                for (int y = 0; y < 48; y++) {
                    for (int x = 0; x < 48; x++) {
                        final int pixel = icon.getPixelReader().getArgb(x, y);
                        inkPixels += pixel == 0xff302d27 ? 1 : 0;
                        accentPixels += pixel == accent ? 1 : 0;
                    }
                }
                assertTrue(inkPixels > 0, id + " retains ink");
                assertTrue(accentPixels > 0, id + " has the current accent");
                assertEquals(0, icon.getPixelReader().getArgb(0, 0));
            }
        });
    }

    public void verifyExplanation(final String expected,
            final String focusedId) {
        assertEquals(expected, getLabeledById("statusBarLabel").getText());
        if (focusedId != null) {
            assertEquals(focusedId,
                    getPrimaryStage().getScene().getFocusOwner().getId());
            final Labeled focused = getLabeledById(focusedId);
            assertTrue(focused.getBorder().getImages().isEmpty(),
                    "A border image would suppress the visible focus stroke");
            assertFalse(focused.getBorder().getStrokes().get(0).getTopStyle()
                    .getDashArray().isEmpty());
        }
    }

    public void verifyColonies(final boolean showing, final boolean empty,
            final String firstLabel) {
        final MenuButton menu = (MenuButton) getLabeledById("coloniesMenu");
        assertEquals(showing, menu.isShowing());
        assertEquals(firstLabel, menu.getItems().get(0).getText());
        assertEquals(empty, menu.getItems().get(0).isDisable());
        if (empty) {
            assertEquals(1, menu.getItems().size());
        }
    }
}
