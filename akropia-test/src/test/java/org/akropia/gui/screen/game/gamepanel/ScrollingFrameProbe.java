package org.akropia.gui.screen.game.gamepanel;

import org.akropia.gui.Point;
import org.akropia.gui.WasdController;
import org.akropia.page.TestContext;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Capture a moving frame and a stationary reference at its final camera. */
public final class ScrollingFrameProbe {
    public Point before;
    public Point after;
    public WritableImage moving;
    public WritableImage stationary;

    public void capture(final TestContext context, final KeyCode heldKey) {
        final GamePanelPainter painter = context
                .getClassFromGuice(GamePanelPainter.class);
        final VisibleAreaService view = context.getAkropia().getInjector()
                .getInstance(com.google.inject.Key.get(VisibleAreaService.class,
                        com.google.inject.name.Names.named("game")));
        final Canvas canvas = context
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        before = view.getTopLeft();
        painter.paint(canvas.getGraphicsContext2D(), 0);
        after = view.getTopLeft();
        moving = FogFrameProbe.captureCanvas(context);
        // Release inside the same FX callback so no extra camera step can
        // intervene between the two captures. The robot releases its key too.
        context.getClassFromGuice(WasdController.class)
                .onKeyReleased(new KeyEvent(KeyEvent.KEY_RELEASED, "", "",
                        heldKey, false, false, false, false));
        painter.paint(canvas.getGraphicsContext2D(), 0);
        stationary = FogFrameProbe.captureCanvas(context);
    }
}
