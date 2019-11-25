package org.microcol.gui.screen.editor;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.input.MouseEvent;

@Singleton
@Listener
public class EditorMouseListener {

    private final VisibleAreaService visibleAreaService;

    private final MouseOperationManager mouseOperationManager;

    private final ModelService modelService;

    private Point previousPosition = Point.ZERO;

    @Inject
    EditorMouseListener(final EditorPanel editorPanel,
            final @Named("editor") VisibleAreaService visibleAreaService,
            final MouseOperationManager mouseOperationManager, final ModelService modelService) {
        this.visibleAreaService = Preconditions.checkNotNull(visibleAreaService);
        this.mouseOperationManager = Preconditions.checkNotNull(mouseOperationManager);
        this.modelService = Preconditions.checkNotNull(modelService);
        editorPanel.getCanvas().setOnMouseDragged(this::onMouseDragged);
        editorPanel.getCanvas().setOnMouseMoved(this::onMouseMoved);
        editorPanel.getCanvas().setOnMousePressed(this::onMousePressed);
    }

    private final void onMouseDragged(final MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            if (previousPosition != null) {
                final Point currentPosition = Point.of(event.getX(), event.getY());
                final Point delta = previousPosition.substract(currentPosition);
                visibleAreaService.addDeltaToTopLeftPoint(delta);
            }
        }
        previousPosition = Point.of(event.getX(), event.getY());
    }

    private final void onMouseMoved(final MouseEvent event) {
        previousPosition = Point.of(event.getX(), event.getY());
    }

    private final void onMousePressed(final MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            final Point pressedAt = Point.of(event.getX(), event.getY());
            final Area area = new Area(visibleAreaService);
            final Location location = area.convertToLocation(pressedAt);
            final MouseOperationContext context = new MouseOperationContext(modelService);
            mouseOperationManager.getMouseOperation()
                    .ifPresent(operation -> operation.execute(context, location));
            modelService.initCache();
        }
    }

}
