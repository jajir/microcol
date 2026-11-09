package org.akropia.gui.screen.editor;

import org.akropia.gui.Point;
import org.akropia.gui.screen.game.gamepanel.Area;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.gui.util.Listener;
import org.akropia.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.input.MouseEvent;

/**
 * Primary click selects a square and applies the chosen tool; dragging paints
 * with a painting tool. Secondary drag moves the view.
 */
@Singleton
@Listener
public class EditorMouseListener {

    private final VisibleAreaService visibleAreaService;

    private final MouseOperationManager mouseOperationManager;

    private final ModelService modelService;

    private Point previousPosition = Point.ZERO;

    private Location painted;

    @Inject
    EditorMouseListener(final EditorPanel editorPanel,
            final @Named("editor") VisibleAreaService visibleAreaService,
            final MouseOperationManager mouseOperationManager,
            final ModelService modelService) {
        this.visibleAreaService = Preconditions
                .checkNotNull(visibleAreaService);
        this.mouseOperationManager = Preconditions
                .checkNotNull(mouseOperationManager);
        this.modelService = Preconditions.checkNotNull(modelService);
        editorPanel.getCanvas().setOnMouseDragged(this::onMouseDragged);
        editorPanel.getCanvas().setOnMouseMoved(this::onMouseMoved);
        editorPanel.getCanvas().setOnMousePressed(this::onMousePressed);
    }

    private void onMouseDragged(final MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            if (previousPosition != null) {
                final Point currentPosition = Point.of(event.getX(),
                        event.getY());
                final Point delta = previousPosition.substract(currentPosition);
                visibleAreaService.addDeltaToTopLeftPoint(delta);
            }
        } else if (event.isPrimaryButtonDown()) {
            final Location location = toLocation(event);
            if (!location.equals(painted)) {
                mouseOperationManager.getMouseOperation()
                        .filter(MouseOperation::isPainting)
                        .ifPresent(operation -> apply(operation, location));
            }
        }
        previousPosition = Point.of(event.getX(), event.getY());
    }

    private void onMouseMoved(final MouseEvent event) {
        previousPosition = Point.of(event.getX(), event.getY());
    }

    private void onMousePressed(final MouseEvent event) {
        // Keyboard editing continues from the clicked map.
        ((javafx.scene.Node) event.getSource()).requestFocus();
        if (event.isPrimaryButtonDown()) {
            final Location location = toLocation(event);
            modelService.select(location);
            mouseOperationManager.getMouseOperation()
                    .ifPresent(operation -> apply(operation, location));
        }
    }

    private void apply(final MouseOperation operation,
            final Location location) {
        painted = location;
        if (modelService.getDraft().contains(location)) {
            operation.execute(location);
        }
    }

    private Location toLocation(final MouseEvent event) {
        return new Area(visibleAreaService)
                .convertToLocation(Point.of(event.getX(), event.getY()));
    }

}
