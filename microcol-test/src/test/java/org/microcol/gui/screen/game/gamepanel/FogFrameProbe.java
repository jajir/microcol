package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.model.Location;
import org.microcol.page.TestContext;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Draw a diagnostic animation through the real frame compositor on the FX
 * thread.
 */
public final class FogFrameProbe {
    private boolean completed;
    private WritableImage frame;

    public void paint(final TestContext context, final Location known,
            final Location hidden) {
        final AnimationManager manager = context
                .getClassFromGuice(AnimationManager.class);
        final GraphicsContext graphics = context
                .getClassFromGuice(GamePanelComponent.class).getCanvas()
                .getGraphicsContext2D();
        manager.addAnimation(new Animation() {
            private boolean running = true;

            @Override
            public boolean hasNextStep() {
                return running;
            }

            @Override
            public void nextStep() {
                running = false;
            }

            @Override
            public boolean canBePainted(final Area area) {
                return true;
            }

            @Override
            public void paint(final GraphicsContext g, final Area area) {
                g.setFill(Color.MAGENTA);
                for (Location location : new Location[] { known, hidden }) {
                    final Point point = area.convertToCanvasPoint(location);
                    g.fillRect(point.getX(), point.getY(),
                            Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
                }
            }
        }, animation -> completed = true);
        context.getClassFromGuice(GamePanelPainter.class).paint(graphics, 0);
        frame = captureCanvas(context);
    }

    public static WritableImage captureCanvas(final TestContext context) {
        final javafx.scene.canvas.Canvas canvas = context
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final javafx.geometry.Point2D origin = canvas.localToScene(0, 0);
        final int width = (int) canvas.getWidth();
        final int height = (int) canvas.getHeight();
        final WritableImage scene = org.microcol.page.AbstractScreen
                .snapshotScene(context.getPrimaryStage().getScene());
        return new WritableImage(scene.getPixelReader(), (int) origin.getX(),
                (int) origin.getY(), width, height);
    }

    public WritableImage getFrame() {
        return frame;
    }

    public boolean isCompleted() {
        return completed;
    }
}
