package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ButtonPresentationContractTest {
    @Test
    void applicationButtonsMustChooseAnExplicitIcon() throws Exception {
        final Pattern constructor = Pattern.compile(
                "new\\s+(?:javafx\\.scene\\.control\\.)?(?:Button|ToggleButton|MenuButton|SplitMenuButton)\\s*\\(");
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java"))) {
            final List<Path> sources = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            for (final Path source : sources) {
                if (!source.getFileName().toString()
                        .equals("ActionButtons.java")) {
                    assertFalse(
                            constructor.matcher(Files.readString(source))
                                    .find(),
                            "Use the shared factory and an explicit icon: "
                                    + source);
                }
            }
        }
    }

    @Test
    void exportedActionIconsHaveInkAndTransparentMargins() throws Exception {
        final String svg = Files
                .readString(Path.of("src/graphics/action-icons.svg"));
        final long cells = svg.lines().filter(line -> line.contains("<g id="))
                .count();
        final BufferedImage image = ImageIO.read(
                Path.of("src/main/resources/images/action-icons.png").toFile());
        assertEquals(cells * 96, image.getWidth());
        assertEquals(96, image.getHeight());
        for (int cell = 0; cell < cells; cell++) {
            int ink = 0;
            for (int x = cell * 96; x < (cell + 1) * 96; x++) {
                for (int y = 0; y < 96; y++) {
                    final int pixel = image.getRGB(x, y);
                    if ((pixel >>> 24) == 255 && (pixel & 0xffffff) == 0x302d27)
                        ink++;
                }
            }
            assertTrue(ink > 40,
                    "Empty or missing action icon in cell " + cell);
            assertEquals(0, image.getRGB(cell * 96, 0) >>> 24);
            assertEquals(0, image.getRGB((cell + 1) * 96 - 1, 95) >>> 24);
        }
    }
}
