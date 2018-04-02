package org.microcol.gui.util;

import java.util.function.Function;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Class help highlight panel when mouse is dragged over with some thing that
 * could be dropped.
 */
public class BackgroundHighlighter {

    private final Region region;

    private final Function<Dragboard, Boolean> shoudlByHighlighted;

    private Background background;

    public BackgroundHighlighter(final Region region,
            final Function<Dragboard, Boolean> shoudlByHighlighted) {
        this.region = Preconditions.checkNotNull(region);
        this.shoudlByHighlighted = Preconditions.checkNotNull(shoudlByHighlighted);
    }

    /**
     * Process event when mouse enter region.
     * 
     * @param event
     *            required event
     */
    public void onDragEntered(final DragEvent event) {
        background = region.getBackground();
        if (shoudlByHighlighted.apply(event.getDragboard())) {
            region.setBackground(new Background(
                    new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    /**
     * Process even when mouse leave region.
     * 
     * @param event
     *            optional event
     */
    public void onDragExited(final DragEvent event) {
        region.setBackground(background);
        background = null;
    }

}
