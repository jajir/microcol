package org.microcol.gui.util;

import java.util.HashMap;
import java.util.Map;

import org.microcol.gui.MainStageBuilder;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.text.Font;

/**
 * Provide fonts. Some fonts have to loaded manually because have to be
 * available from .css files. Some fonts can be created on demand.
 */
public final class FontService {

    public static final String FONT_CARDO_REGULAR = "Cardo-Regular";

    public static final String FONT_CARDO_BOLD = "Cardo-Bold";

    public static final String FONT_CARDO_ITALIC = "Cardo-Italic";

    private static final int SIZE_10 = 10;

    private static final int SIZE_12 = 12;

    private static final int SIZE_14 = 14;

    private static final int SIZE_16 = 16;

    private final Map<String, Font> fonts = new HashMap<>();

    /**
     * Default constructor.
     */
    @Inject
    public FontService() {
        load("/font/Cardo-Bold.ttf", SIZE_10, FONT_CARDO_BOLD);
        load("/font/Cardo-Italic.ttf", SIZE_10, FONT_CARDO_ITALIC);
        load("/font/Cardo-Regular.ttf", SIZE_10, FONT_CARDO_REGULAR);
        load("/font/Cardo-Regular.ttf", SIZE_12, FONT_CARDO_REGULAR);
        load("/font/Cardo-Regular.ttf", SIZE_14, FONT_CARDO_REGULAR);
        load("/font/Cardo-Regular.ttf", SIZE_16, FONT_CARDO_REGULAR);
    }

    /**
     * Allows to load from specific file font. It's supposed that path point to font
     * specification.
     *
     * @param path
     *            required path for font definition file
     * @param size
     *            required font size
     * @param name
     *            required font family name
     */
    private void load(final String path, final double size, final String name) {
        addFont(name, size,
                Font.loadFont(MainStageBuilder.class.getResource(path).toExternalForm(), size));
    }

    /**
     * Add specific font instance under some font family name and size.
     *
     * @param name
     *            required font family name
     * @param size
     *            required font size
     * @param font
     *            required font instance
     */
    private void addFont(final String name, final double size, final Font font) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(size);
        Preconditions.checkNotNull(font);
        fonts.put(getCode(name, size), font);
    }

    /**
     * Get font identification string. This string is used as key for font map.
     *
     * @param name
     *            required font family name
     * @param size
     *            required font size
     * @return font code
     */
    private String getCode(final String name, final double size) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(size);
        return name + "-" + size;
    }

    /**
     * For specific requirements return font instance.
     *
     * @param name
     *            required font family name
     * @param size
     *            required font size
     * @return font instance
     */
    public Font getFont(final String name, final double size) {
        final Font out = fonts.get(getCode(name, size));
        Preconditions.checkState(out != null, "Unable to find font '%s' with size '%s'", name,
                size);
        return out;
    }

}
