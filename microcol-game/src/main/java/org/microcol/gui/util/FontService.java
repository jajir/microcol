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
public class FontService {

	public static final String FONT_CARDO_REGULAR = "Cardo-Regular";

	public static final String FONT_CARDO_BOLD = "Cardo-Bold";

	public static final String FONT_CARDO_ITALIC = "Cardo-Italic";

	private final Map<String, Font> fonts = new HashMap<>();

	@Inject
	public FontService() {
		load("/font/Cardo-Bold.ttf", 10, FONT_CARDO_BOLD);
		load("/font/Cardo-Italic.ttf", 10, FONT_CARDO_ITALIC);
		load("/font/Cardo-Regular.ttf", 10, FONT_CARDO_REGULAR);
		load("/font/Cardo-Regular.ttf", 12, FONT_CARDO_REGULAR);
		load("/font/Cardo-Regular.ttf", 14, FONT_CARDO_REGULAR);
		load("/font/Cardo-Regular.ttf", 16, FONT_CARDO_REGULAR);
	}

	private void load(final String path, final double size, final String name) {
		addFont(name, size, Font.loadFont(MainStageBuilder.class.getResource(path).toExternalForm(), size));
	}

	private void addFont(final String name, final double size, final Font font) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(size);
		Preconditions.checkNotNull(font);
		fonts.put(getCode(name, size), font);
	}

	private String getCode(final String name, final double size) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(size);
		return name + "-" + size;
	}

	public Font getFont(final String name, final double size) {
		final Font out = fonts.get(getCode(name, size));
		Preconditions.checkState(out != null, "Unable to find font '%s' with size '%s'", name, size);
		return out;
	}

}
