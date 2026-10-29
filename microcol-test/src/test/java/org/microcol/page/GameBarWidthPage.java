package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/** Shared bar geometry on the map and every in-game page. */
public final class GameBarWidthPage extends AbstractScreen {

    public GameBarWidthPage(final TestContext context) {
        super(context);
    }

    public void resizeContent(final double width, final double height) {
        getRobot().interact(() -> {
            final Stage stage = getContext().getPrimaryStage();
            stage.setWidth(
                    width + stage.getWidth() - stage.getScene().getWidth());
            stage.setHeight(
                    height + stage.getHeight() - stage.getScene().getHeight());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyContents(final double expectedWindowWidth,
            final double expectedContentWidth) {
        getRobot().interact(() -> {
            final javafx.scene.Scene scene = getContext().getPrimaryStage()
                    .getScene();
            assertEquals(expectedWindowWidth, scene.getWidth(), 1,
                    "The window can grow and shrink after the content is capped");
            final Region page = (Region) scene.lookup(".in-game-page");
            final Region top = (Region) scene
                    .lookup(page == null ? ".game-screen .screen-toolbar"
                            : ".screen-toolbar");
            final Region bottom = (Region) scene.lookup("#statusBar");
            verifyBar(top, expectedWindowWidth, expectedContentWidth);
            verifyBar(bottom, expectedWindowWidth, expectedContentWidth);
            final Node body = scene
                    .lookup(page == null ? "#mainBox" : ".screen-scroll");
            assertEquals(expectedWindowWidth, body.getLayoutBounds().getWidth(),
                    1, "The main content still uses the full window width");
            assertEquals(72, top.getHeight(), 0.01);
        });
    }

    private void verifyBar(final Region frame, final double expectedWindowWidth,
            final double expectedContentWidth) {
        final Bounds outer = frame.localToScene(frame.getLayoutBounds());
        final Region content = (Region) frame.lookup(".game-bar-content");
        final Bounds inner = content.localToScene(content.getLayoutBounds());
        assertEquals(0, outer.getMinX(), 1);
        assertEquals(expectedWindowWidth, outer.getWidth(), 1,
                "The background and frame still span the window");
        assertEquals(expectedContentWidth, inner.getWidth(), 1);
        assertEquals((expectedWindowWidth - expectedContentWidth) / 2,
                inner.getMinX(), 1, "The content is centered");
        assertTrue(!frame.getBackground().getFills().isEmpty());
        assertEquals(1, frame.getBorder().getImages().size());
        for (Node button : frame.lookupAll(".button")) {
            if (button.isVisible() && button.isManaged()) {
                final Bounds bounds = button
                        .localToScene(button.getLayoutBounds());
                assertTrue(bounds.getMinX() >= inner.getMinX() - 1);
                assertTrue(bounds.getMaxX() <= inner.getMaxX() + 1);
                assertTrue(bounds.getMinY() >= outer.getMinY() - 1);
                assertTrue(bounds.getMaxY() <= outer.getMaxY() + 1);
            }
        }
    }
}
