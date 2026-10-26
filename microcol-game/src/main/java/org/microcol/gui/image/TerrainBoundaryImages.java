package org.microcol.gui.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.microcol.gui.Tile;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Shared land-colour geometry; never changes the logical map squares. */
public final class TerrainBoundaryImages {
    private static final int SIZE = Tile.TILE_WIDTH_IN_PX;
    private static final int[][] DIRECTIONS = { { 1, 0 }, { 0, 1 }, { -1, 0 },
            { 0, -1 } };
    private final WorldMap world;
    private final long seed;
    private final int maxX;
    private final int maxY;
    private final ImageProvider images;
    private final Predicate<Location> explored;
    private final Map<Location, Patch> cache = new HashMap<>();

    public TerrainBoundaryImages(final WorldMap world,
            final ImageProvider images, final Predicate<Location> explored) {
        this.world = world;
        this.seed = world.getSeed().longValue();
        this.maxX = world.getMaxLocationX();
        this.maxY = world.getMaxLocationY();
        this.images = images;
        this.explored = explored;
    }

    public Image getBackground(final Location at, final TerrainType type) {
        if (!type.isLand()) {
            return null;
        }
        final Patch patch = patch(at);
        // A standalone preview can intentionally display a different terrain.
        if (patch.type(at.getX(), at.getY()) != type) {
            return null;
        }
        if (!patch.backgroundReady) {
            patch.background = patch.background();
            patch.backgroundReady = true;
        }
        return patch.background;
    }

    public Image getCoast(final Location at, final Image mask) {
        if (mask == null) {
            return null;
        }
        final Patch patch = patch(at);
        if (patch.coast == null) {
            patch.coast = patch.coast(mask);
        }
        return patch.coast;
    }

    private Patch patch(final Location at) {
        final TerrainType[] types = new TerrainType[25];
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                final Location p = Location.of(at.getX() + x, at.getY() + y);
                if (p.getX() >= 1 && p.getY() >= 1 && p.getX() <= maxX
                        && p.getY() <= maxY && explored.test(p)) {
                    types[(y + 2) * 5 + x + 2] = world.getTerrainTypeAt(p);
                }
            }
        }
        Patch patch = cache.get(at);
        if (patch == null || !Arrays.equals(patch.types, types)) {
            patch = new Patch(at, types);
            cache.put(at, patch);
        }
        return patch;
    }

    private double noise(final int x, final int y, final int salt) {
        long value = seed ^ x * 0x9e3779b97f4a7c15L ^ y * 0xbf58476d1ce4e5b9L
                ^ salt;
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        return (value >>> 11) * 0x1.0p-53 * 2 - 1;
    }

    /** Package-visible geometry also supports tests without a JavaFX stage. */
    Path2D outline(final Location at) {
        return patch(at).outline(at.getX(), at.getY());
    }

    private final class Patch {
        private final Location at;
        private final TerrainType[] types;
        private Image background;
        private boolean backgroundReady;
        private Image coast;

        Patch(final Location at, final TerrainType[] types) {
            this.at = at;
            this.types = types;
        }

        private List<Region> regions() {
            final List<Region> regions = new ArrayList<>();
            for (int y = at.getY() - 1; y <= at.getY() + 1; y++) {
                for (int x = at.getX() - 1; x <= at.getX() + 1; x++) {
                    final TerrainType type = type(x, y);
                    if (type != null && type.isLand()) {
                        regions.add(new Region(outline(x, y), type));
                    }
                }
            }
            return regions;
        }

        TerrainType type(final int x, final int y) {
            final int dx = x - at.getX() + 2;
            final int dy = y - at.getY() + 2;
            return dx >= 0 && dx < 5 && dy >= 0 && dy < 5 ? types[dy * 5 + dx]
                    : null;
        }

        boolean interior(final int x, final int y) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dx = 0; dx <= 1; dx++) {
                    final TerrainType t = type(x + dx, y + dy);
                    if (t == null || !t.isLand()) {
                        return false;
                    }
                }
            }
            return true;
        }

        Point2D vertex(final int x, final int y) {
            final double amplitude = interior(x, y) ? SIZE * 0.08 : 0;
            return new Point2D.Double(x * SIZE + noise(x, y, 17) * amplitude,
                    y * SIZE + noise(x, y, 53) * amplitude);
        }

        boolean boundary(final int x, final int y, final int dx, final int dy) {
            if (dx != 0) {
                final int tx = x + (dx > 0 ? 1 : 0);
                return type(tx, y) != type(tx, y + 1);
            }
            final int ty = y + (dy > 0 ? 1 : 0);
            return type(x, ty) != type(x + 1, ty);
        }

        Point2D control(final int x, final int y, final int dx, final int dy) {
            final Point2D p = vertex(x, y);
            double vx = dx;
            double vy = dy;
            if (interior(x, y) && boundary(x, y, dx, dy)) {
                int count = 0;
                int[] other = null;
                for (final int[] direction : DIRECTIONS) {
                    if (boundary(x, y, direction[0], direction[1])) {
                        count++;
                        if (direction[0] != dx || direction[1] != dy) {
                            other = direction;
                        }
                    }
                }
                if (count == 2) {
                    vx -= other[0];
                    vy -= other[1];
                }
            }
            final double length = Math.hypot(vx, vy);
            double reach = SIZE * (0.25 + noise(x, y, 91) * 0.025) / length;
            // Keep the entire Bezier convex hull inside the edge's narrow band.
            final double offset = dx == 0 ? p.getX() - x * SIZE
                    : p.getY() - y * SIZE;
            final double normal = dx == 0 ? vx : vy;
            if (normal != 0) {
                reach = Math.min(reach,
                        (SIZE * 0.18 - offset * Math.signum(normal))
                                / Math.abs(normal));
            }
            return new Point2D.Double(p.getX() + vx * reach,
                    p.getY() + vy * reach);
        }

        Path2D outline(final int x, final int y) {
            final int[][] corners = { { x - 1, y - 1 }, { x, y - 1 }, { x, y },
                    { x - 1, y } };
            final Path2D path = new Path2D.Double();
            final Point2D start = vertex(x - 1, y - 1);
            path.moveTo(start.getX(), start.getY());
            for (int i = 0; i < 4; i++) {
                final int[] a = corners[i];
                final int[] b = corners[(i + 1) % 4];
                final Point2D c = control(a[0], a[1], b[0] - a[0], b[1] - a[1]);
                final Point2D d = control(b[0], b[1], a[0] - b[0], a[1] - b[1]);
                final Point2D end = vertex(b[0], b[1]);
                path.curveTo(c.getX(), c.getY(), d.getX(), d.getY(), end.getX(),
                        end.getY());
            }
            path.closePath();
            return path;
        }

        Image background() {
            final TerrainType own = type(at.getX(), at.getY());
            if (Arrays.stream(types).allMatch(
                    type -> type == null || !type.isLand() || type == own)) {
                return null;
            }
            final BufferedImage raster = new BufferedImage(SIZE * 2, SIZE * 2,
                    BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics = raster.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                graphics.scale(2, 2);
                graphics.translate(-(at.getX() - 1) * SIZE,
                        -(at.getY() - 1) * SIZE);
                graphics.setColor(awt(own));
                graphics.fillRect((at.getX() - 1) * SIZE,
                        (at.getY() - 1) * SIZE, SIZE, SIZE);
                for (final Region region : regions()) {
                    graphics.setColor(awt(region.type));
                    graphics.fill(region.path);
                }
            } finally {
                graphics.dispose();
            }
            final WritableImage result = new WritableImage(SIZE, SIZE);
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    int red = 0, green = 0, blue = 0;
                    for (int dy = 0; dy < 2; dy++) {
                        for (int dx = 0; dx < 2; dx++) {
                            final int rgb = raster.getRGB(x * 2 + dx,
                                    y * 2 + dy);
                            red += rgb >> 16 & 255;
                            green += rgb >> 8 & 255;
                            blue += rgb & 255;
                        }
                    }
                    result.getPixelWriter().setArgb(x, y, 0xff000000
                            | red / 4 << 16 | green / 4 << 8 | blue / 4);
                }
            }
            return result;
        }

        Image coast(final Image mask) {
            final List<Region> regions = regions();
            final WritableImage result = new WritableImage(SIZE, SIZE);
            final Color original = Color.web("#c5c18a");
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    final Color pixel = mask.getPixelReader().getColor(x, y);
                    if (pixel.getOpacity() == 0) {
                        continue;
                    }
                    double distance = Double.MAX_VALUE;
                    TerrainType nearest = TerrainType.GRASSLAND;
                    for (final Region region : regions) {
                        final double candidate = region.distance(
                                (at.getX() - 1) * SIZE + x + 0.5,
                                (at.getY() - 1) * SIZE + y + 0.5);
                        if (candidate < distance) {
                            distance = candidate;
                            nearest = region.type;
                        }
                    }
                    final Color land = images.getTerrainColor(nearest);
                    result.getPixelWriter().setColor(x, y,
                            new Color(
                                    Math.min(1,
                                            pixel.getRed() * land.getRed()
                                                    / original.getRed()),
                                    Math.min(1,
                                            pixel.getGreen() * land.getGreen()
                                                    / original.getGreen()),
                                    Math.min(1,
                                            pixel.getBlue() * land.getBlue()
                                                    / original.getBlue()),
                                    pixel.getOpacity()));
                }
            }
            return result;
        }
    }

    private java.awt.Color awt(final TerrainType type) {
        final Color colour = images.getTerrainColor(type);
        return new java.awt.Color((float) colour.getRed(),
                (float) colour.getGreen(), (float) colour.getBlue());
    }

    private static final class Region {
        private final Path2D path;
        private final TerrainType type;
        private final List<double[]> segments = new ArrayList<>();

        Region(final Path2D path, final TerrainType type) {
            this.path = path;
            this.type = type;
            final PathIterator iterator = path.getPathIterator(null, 0.2);
            final double[] p = new double[6];
            double x = 0, y = 0;
            while (!iterator.isDone()) {
                final int kind = iterator.currentSegment(p);
                if (kind == PathIterator.SEG_LINETO) {
                    segments.add(new double[] { x, y, p[0], p[1] });
                }
                x = p[0];
                y = p[1];
                iterator.next();
            }
        }

        double distance(final double x, final double y) {
            if (path.contains(x, y)) {
                return 0;
            }
            double closest = Double.MAX_VALUE;
            for (final double[] line : segments) {
                closest = Math.min(closest, java.awt.geom.Line2D
                        .ptSegDistSq(line[0], line[1], line[2], line[3], x, y));
            }
            return closest;
        }
    }
}
