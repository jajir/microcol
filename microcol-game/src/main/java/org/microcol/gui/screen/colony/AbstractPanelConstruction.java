package org.microcol.gui.screen.colony;

import org.microcol.gui.MicroColException;
import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Construction;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

abstract class AbstractPanelConstruction implements JavaFxComponent {

    private final EventBus eventBus;
    private final I18n i18n;
    private final ImageProvider imageProvider;
    private final Canvas canvas;
    private final StackPane mainPane = new StackPane();
    private final Construction construction;

    AbstractPanelConstruction(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider, final Construction construction,
            final Point canvasSize) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.construction = Preconditions.checkNotNull(construction);
        canvas = new Canvas(canvasSize.getX(), canvasSize.getY());
        canvas.getStyleClass().add("canvas");
        mainPane.setOnMouseEntered(this::onMouseEntered);
        mainPane.setOnMouseExited(this::onMouseExited);
        mainPane.getChildren().add(canvas);
    }

    @Override
    public Region getContent() {
        return getMainPane();
    }

    abstract void paint();

    /**
     * Allows t set css construction id. It allows to place construction at
     * specific place.
     * 
     * @param id
     *            required css id
     */
    void setCssId(final String id) {
        getMainPane().setId(id);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(getConstructionName(), Source.COLONY));
    }

    /**
     * Get localized construction name.
     * 
     * @return
     */
    private String getConstructionName() {
        return i18n.get(ConstructionTypeName.getNameForType(construction.getType()));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    void paintConstruction(final Point point) {
        paintConstruction(canvas.getGraphicsContext2D(), getConstruction(), point);
    }

    private void paintConstruction(final GraphicsContext gc, final Construction construction,
            final Point point) {
        final Image image = imageProvider.getConstructionImage(construction.getType())
                .orElseThrow(() -> new MicroColException(
                        String.format("Unable to find image for %s", construction)));
        gc.drawImage(image, point.getX(), point.getY());
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    protected I18n getI18n() {
        return i18n;
    }

    protected Canvas getCanvas() {
        return canvas;
    }

    protected StackPane getMainPane() {
        return mainPane;
    }

    protected Construction getConstruction() {
        return construction;
    }

    protected ImageProvider getImageProvider() {
        return imageProvider;
    }

}
