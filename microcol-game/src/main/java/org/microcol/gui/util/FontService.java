package org.microcol.gui.util;

import java.net.URL;
import org.microcol.model.MicroColException;
import com.google.inject.Inject;
import javafx.scene.text.Font;

/** Register the bundled UI face and use the same outlines on canvases. */
public final class FontService {
    private final Font face;

    @Inject
    public FontService() {
        final URL resource = FontService.class
                .getResource("/fonts/PlaypenSans/PlaypenSans-Light.ttf");
        if (resource == null) {
            throw new MicroColException("Missing bundled Playpen Sans font");
        }
        face = Font.loadFont(resource.toExternalForm(), 12);
        if (face == null) {
            throw new MicroColException("Unable to load Playpen Sans font");
        }
    }

    public Font getDefault(final double size) {
        return new Font(face.getName(), size);
    }
}
