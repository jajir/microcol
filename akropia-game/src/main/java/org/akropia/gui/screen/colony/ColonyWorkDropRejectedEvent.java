package org.akropia.gui.screen.colony;

import com.google.common.eventbus.EventBus;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

/** A rejected colony drag is reported after the native drag has finished. */
public record ColonyWorkDropRejectedEvent(ColonyMsg reason) {
    static void afterDrag(final DragEvent event, final EventBus eventBus,
            final ColonyMsg reason) {
        final Node source = (Node) event.getGestureSource();
        source.addEventHandler(DragEvent.DRAG_DONE,
                new EventHandler<DragEvent>() {
                    @Override
                    public void handle(final DragEvent done) {
                        source.removeEventHandler(DragEvent.DRAG_DONE, this);
                        Platform.runLater(() -> eventBus
                                .post(new ColonyWorkDropRejectedEvent(reason)));
                    }
                });
    }
}
