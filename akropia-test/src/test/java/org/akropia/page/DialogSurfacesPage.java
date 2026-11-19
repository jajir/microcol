package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** Rendered dialog surfaces, including empty space around content. */
public final class DialogSurfacesPage extends AbstractScreen {
    public DialogSurfacesPage(final TestContext context) {
        super(context);
    }

    public void verifyScrollSurface(final String contentId) {
        getRobot().interact(() -> {
            final Region frame = (Region) getNodeFinder()
                    .lookup("#" + contentId).query().getScene().getRoot();
            frame.applyCss();
            frame.layout();
            assertTrue(frame.getStyleClass().contains("modal-frame"));
            final var scrolls = frame.lookupAll(".scroll-pane");
            assertFalse(scrolls.isEmpty());
            for (final var node : scrolls) {
                final ScrollPane scroll = (ScrollPane) node;
                verifyTransparentSurface(scroll);
                verifyTransparentSurface((Region) scroll.lookup(".viewport"));
            }
            assertEquals(Color.web("#FFF9EC"),
                    frame.getBackground().getFills().get(0).getFill());
        });
    }

    private void verifyTransparentSurface(final Region region) {
        assertTrue(region.getBackground().getFills().stream()
                .allMatch(fill -> Color.TRANSPARENT.equals(fill.getFill())));
        assertTrue(region.getBorder() == null
                || region.getBorder().getStrokes().isEmpty());
    }

    public void verifySettlementListSurface() {
        getRobot().interact(() -> {
            final Region list = (Region) getNodeFinder()
                    .lookup("#nativeSettlementsList").query();
            list.applyCss();
            list.layout();
            assertEquals(Color.web("#FFF9EC"),
                    list.getBackground().getFills().get(0).getFill());
            final var cells = list.lookupAll(".list-cell");
            assertFalse(cells.isEmpty());
            for (final var node : cells) {
                final ListCell<?> cell = (ListCell<?>) node;
                if (!cell.isSelected()) {
                    assertEquals(Color.web("#FFF9EC"),
                            cell.getBackground().getFills().get(0).getFill());
                }
            }
        });
    }
}
