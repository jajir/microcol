package org.microcol.gui.util;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;

/** The selected single pen contour, shared by every application modal. */
final class ModalFrame extends StackPane {

    private static final String OUTLINE = "M19 15 C82 10 135 18 197 14 "
            + "S317 17 379 13 S452 14 481 16 Q491 15 489 28 "
            + "C486 77 493 119 488 164 S491 215 486 229 "
            + "Q488 240 473 237 C407 234 361 241 300 237 "
            + "S176 239 118 235 S46 240 20 237 Q10 238 12 226 "
            + "C16 183 9 139 13 97 S9 42 12 26 Q10 16 19 15 Z";

    private final SVGPath silhouette = new SVGPath();
    private final Region ink = new Region();
    private final Bounds outlineBounds;

    ModalFrame(final Parent content) {
        final SVGPath outline = new SVGPath();
        outline.setContent(OUTLINE);
        setShape(outline);
        setScaleShape(true);
        setCenterShape(true);
        silhouette.setContent(OUTLINE);
        outlineBounds = silhouette.getBoundsInLocal();
        // Child backgrounds must not restore the rectangular window corners.
        setClip(silhouette);
        getStyleClass().add("modal-frame");
        setPadding(new Insets(6));
        content.getStyleClass().add("modal-content");
        final SVGPath inkShape = new SVGPath();
        inkShape.setContent(OUTLINE);
        ink.setShape(inkShape);
        ink.setScaleShape(true);
        ink.setCenterShape(true);
        ink.setManaged(false);
        ink.setMouseTransparent(true);
        ink.getStyleClass().add("modal-outline");
        // Opaque page panels must stay behind the pen stroke.
        getChildren().addAll(content, ink);
    }

    @Override
    protected void layoutChildren() {
        // Reserve the contour's inward waves even on tall, full-page dialogs.
        final Insets padding = new Insets(Math.max(6, getHeight() * 0.04),
                Math.max(6, getWidth() * 0.025),
                Math.max(6, getHeight() * 0.04),
                Math.max(6, getWidth() * 0.025));
        if (!padding.equals(getPadding())) {
            setPadding(padding);
        }
        super.layoutChildren();
        ink.resizeRelocate(0, 0, getWidth(), getHeight());
        final double scaleX = getWidth() / outlineBounds.getWidth();
        final double scaleY = getHeight() / outlineBounds.getHeight();
        silhouette.getTransforms()
                .setAll(new Affine(scaleX, 0, -outlineBounds.getMinX() * scaleX,
                        0, scaleY, -outlineBounds.getMinY() * scaleY));
    }
}
