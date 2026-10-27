package org.microcol.gui.image;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Explicit action meanings; shared artwork is independent of translated labels.
 */
public enum ButtonIcon {
    COLONIES("game-toolbar.png", 0), CONGRESS("game-toolbar.png", 1),
    CENTER("game-toolbar.png", 2), STATISTICS("game-toolbar.png", 3),
    REPORT("game-toolbar.png", 4), GOALS("game-toolbar.png", 5),
    HELP("game-toolbar.png", 6), EUROPE("game-toolbar.png", 7),
    NEXT("game-toolbar.png", 8), RETIRE("game-toolbar.png", 9),
    EXIT("game-toolbar.png", 10), INDEPENDENCE("game-toolbar.png", 11),
    PLAY("menu-icons.png", 0), RECOVER("menu-icons.png", 1),
    FOLDER("menu-icons.png", 2), CAMPAIGN("menu-icons.png", 3),
    NEW_GAME("menu-icons.png", 4), TROPHY("menu-icons.png", 5),
    SETTINGS("menu-icons.png", 6), CONFIRM("action-icons.png", 0),
    CANCEL("action-icons.png", 1), BACK("action-icons.png", 2),
    SAVE("action-icons.png", 3), DELETE("action-icons.png", 4),
    ADD("action-icons.png", 5), REMOVE("action-icons.png", 6),
    UP("action-icons.png", 7), DOWN("action-icons.png", 8),
    BUY("action-icons.png", 9), SELL("action-icons.png", 10),
    CARGO("action-icons.png", 11), LOAD("action-icons.png", 12),
    UNLOAD("action-icons.png", 13), REPAIR("action-icons.png", 14),
    TAX("action-icons.png", 15), GOLD("action-icons.png", 16),
    MILITARY("action-icons.png", 17), LABOR("action-icons.png", 18),
    RELIGION("action-icons.png", 19), SHIP("action-icons.png", 20),
    TRADE("action-icons.png", 21), DIPLOMACY("action-icons.png", 22),
    EDUCATION("action-icons.png", 23), GIFT("action-icons.png", 24),
    HAGGLE("action-icons.png", 25), WAIT("action-icons.png", 26),
    SENTRY("action-icons.png", 27), FORTIFY("action-icons.png", 28),
    DISBAND("action-icons.png", 29), JETTISON("action-icons.png", 30),
    ROAD("action-icons.png", 31), PLOW("action-icons.png", 32),
    CUT_TREE("action-icons.png", 33), TREE("action-icons.png", 34),
    LAND("action-icons.png", 35), OCEAN("action-icons.png", 36),
    MOUNTAIN("action-icons.png", 37), FILTER("action-icons.png", 38),
    IMPORT("action-icons.png", 39), EXPORT("action-icons.png", 40),
    RENAME("action-icons.png", 41), REPLACE("action-icons.png", 42),
    STOP("action-icons.png", 43);

    private static final Map<String, Image> IMAGES = new HashMap<>();
    private final String resource;
    private final int column;

    ButtonIcon(final String resource, final int column) {
        this.resource = resource;
        this.column = column;
    }

    public ImageView createView() {
        final Image image = IMAGES.computeIfAbsent(resource,
                name -> new Image(ButtonIcon.class
                        .getResource("/images/" + name).toExternalForm()));
        final ImageView view = new ImageView(image);
        view.setViewport(new Rectangle2D(column * 96, 0, 96, 96));
        view.setFitWidth(32);
        view.setFitHeight(32);
        view.setMouseTransparent(true);
        view.getStyleClass().add("action-icon");
        return view;
    }
}
