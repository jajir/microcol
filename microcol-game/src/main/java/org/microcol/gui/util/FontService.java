package org.microcol.gui.util;

import java.net.URL;
import org.microcol.model.MicroColException;
import com.google.inject.Inject;
import javafx.scene.text.Font;

/** Register the bundled UI faces and use the same outlines on canvases. */
public final class FontService {
    private final Font face;

    @Inject
    public FontService() {
        face = load("Light");
        load("Regular");
        load("Medium");
        load("SemiBold");
    }

    private Font load(final String weight) {
        final URL resource = FontService.class.getResource(
                "/fonts/PlaypenSans/PlaypenSans-" + weight + ".ttf");
        if (resource == null) {
            throw new MicroColException(
                    "Missing bundled Playpen Sans font: " + weight);
        }
        final Font loaded = Font.loadFont(resource.toExternalForm(), 12);
        if (loaded == null) {
            throw new MicroColException(
                    "Unable to load Playpen Sans font: " + weight);
        }
        return loaded;
    }

    public Font getDefault(final double size) {
        return new Font(face.getName(), size);
    }
}
