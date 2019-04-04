package org.microcol.gui.util;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.MicroColException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import javafx.scene.text.Font;

/**
 * Provide fonts. Some fonts have to loaded manually because have to be
 * available from .css files. Some fonts can be created on demand.
 */
public final class FontService {

    private static final String FONT_DIRECTORY = "/fonts/";

    public static final String FONT_CARDO_REGULAR = "Cardo/Cardo-Regular.ttf";

    public static final String FONT_CARDO_BOLD = "Cardo/Cardo-Bold.ttf";

    public static final String FONT_CARDO_ITALIC = "Cardo/Cardo-Italic.ttf";

    public static final String FONT_BILBO_REGULAR = "Bilbo/Bilbo-Regular.ttf";

    public static final String FONT_TILLANA_REGULAR = "Tillana/Tillana-Regular.ttf";

    private static final List<Integer> SIZES = ImmutableList.of(10, 12, 14, 16);

    private final Map<String, Font> fonts = new HashMap<>();

    /**
     * Default constructor. All font used in .css should be loaded here.
     */
    @Inject
    public FontService() {
        load(FONT_CARDO_BOLD, 10);
        load(FONT_CARDO_ITALIC, 10);

        SIZES.forEach(size -> load(FONT_CARDO_REGULAR, size));
        SIZES.forEach(size -> load(FONT_BILBO_REGULAR, size));
        SIZES.forEach(size -> load(FONT_TILLANA_REGULAR, size));
    }

    /**
     * Allows to load from specific file font. It's supposed that path point to
     * font specification.
     * <p>
     * When font is loaded than could be used even in .css files.
     * </p>
     *
     * @param relativePath
     *            required path for font definition file
     */
    private void load(final String relativePath, final double size) {
        final String path = FONT_DIRECTORY + relativePath;
        final URL fontURL = MainStageBuilder.class.getResource(path);
        if (fontURL == null) {
            throw new MicroColException(String.format("Unable to find font at '%s'", path));
        }
        addFont(relativePath, size, Font.loadFont(fontURL.toExternalForm(), size));
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

    /**
     * Most of game UI should be painted with this font.
     *
     * @param size
     *            required font size
     * @return font instance
     */
    public Font getDefault(final double size) {
        return getFont(FONT_BILBO_REGULAR, size);
    }

}
