package org.akropia.model.generation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.MineralReserve;
import org.akropia.model.NativeNation;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.campaign.ReferenceExpedition;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.RiverSegmentPo;
import org.akropia.model.store.WorldMapPo;

import com.google.common.base.Preconditions;

/**
 * Assembles the authored North America world: the stylized geography of
 * {@link #SOURCE}, the four-nation expedition and the authored native nations.
 * Terrain, rivers and settlement squares are fixed by the source; resources,
 * braves, markets and rumors use the generated-world helpers with a fixed seed,
 * so every new game starts from the same world.
 */
public final class NorthAmericaWorld {

    /** Editable geography: run-length terrain rows, rivers and settlements. */
    public static final String SOURCE = "/maps/north-america.txt";

    public static final int WIDTH = 100;

    public static final int HEIGHT = 80;

    /** Seed saved with the map and used for resources and natives. */
    public static final int SEED = 1492;

    /** Separates the rumor random stream from resources and natives. */
    private static final long RUMOR_STREAM = 0x1057C17AL;

    private NorthAmericaWorld() {
    }

    /**
     * @param nation     required nation the player controls
     * @param difficulty required difficulty
     * @return complete unsaved model state of the North America world
     * @throws IllegalStateException when the bundled source is invalid
     */
    public static ModelPo create(final EuropeanNation nation,
            final Difficulty difficulty) {
        Preconditions.checkNotNull(nation, "Nation is null");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        final Source source = Source.read();
        final Random random = new Random(SEED);
        final ModelPo model = ReferenceExpedition.create(source.toMap(random),
                nation, difficulty);
        GeneratedNatives.placeAt(model, source.settlements, random);
        GeneratedRumors.place(model, new Random(SEED ^ RUMOR_STREAM));
        GeneratedWorld.requireLegalStarts(model);
        return model;
    }

    /**
     * @return authored terrain, forests and rivers with the deterministic
     *         resources, without units or native settlements
     */
    static WorldMapPo terrain() {
        return Source.read().toMap(new Random(SEED));
    }

    /** Parsed geography source. */
    private static final class Source {

        private final Map<Location, TerrainType> types = new HashMap<>();

        private final Set<Location> trees = new HashSet<>();

        private final Map<Location, RiverSize> rivers = new HashMap<>();

        private final List<RiverSegmentPo> segments = new ArrayList<>();

        private final Set<Set<Location>> joined = new HashSet<>();

        private final List<GeneratedNatives.NativeSite> settlements = new ArrayList<>();

        private int rows = 0;

        static Source read() {
            final InputStream in = NorthAmericaWorld.class
                    .getResourceAsStream(SOURCE);
            if (in == null) {
                throw new IllegalStateException("Missing " + SOURCE);
            }
            final Source source = new Source();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                int number = 0;
                while ((line = reader.readLine()) != null) {
                    number++;
                    final String text = line.trim();
                    if (text.isEmpty() || text.startsWith("#")) {
                        continue;
                    }
                    try {
                        source.parse(text.split("\\s+"));
                    } catch (IllegalArgumentException invalid) {
                        throw new IllegalStateException(SOURCE + " line "
                                + number + ": " + invalid.getMessage(),
                                invalid);
                    }
                }
            } catch (IOException failed) {
                throw new IllegalStateException("Unreadable " + SOURCE, failed);
            }
            if (source.rows != HEIGHT) {
                throw new IllegalStateException(
                        SOURCE + " has " + source.rows + " rows");
            }
            source.verifyRivers();
            return source;
        }

        private void parse(final String[] words) {
            if ("row".equals(words[0])) {
                parseRow(words);
            } else if ("river".equals(words[0])) {
                parseRiver(words);
            } else if ("settlement".equals(words[0])) {
                parseSettlement(words);
            } else {
                throw new IllegalArgumentException(
                        "Unknown directive " + words[0]);
            }
        }

        /**
         * One map row from west to east as runs: a terrain save code followed
         * by its length. An upper-case letter is the same terrain with forest.
         */
        private void parseRow(final String[] words) {
            Preconditions.checkArgument(rows < HEIGHT, "Too many rows");
            final int y = ++rows;
            int x = 0;
            for (int i = 1; i < words.length; i++) {
                final String code = words[i].substring(0, 1);
                final int length = words[i].length() == 1 ? 1
                        : Integer.parseInt(words[i].substring(1));
                final boolean forest = !code
                        .equals(code.toLowerCase(Locale.ROOT));
                final TerrainType type = TerrainType
                        .valueOfCode(code.toLowerCase(Locale.ROOT));
                Preconditions.checkArgument(!forest || type.isCanHaveTree(),
                        "%s can't have forest", type);
                for (int j = 0; j < length; j++) {
                    final Location at = Location.of(++x, y);
                    if (type != TerrainType.OCEAN) {
                        types.put(at, type);
                    }
                    if (forest) {
                        trees.add(at);
                    }
                }
            }
            Preconditions.checkArgument(x == WIDTH, "Row %s has %s squares", y,
                    x);
        }

        /**
         * A course from source to mouth through corner points; consecutive
         * points share a column or a row. A course may join an earlier one at
         * its last square.
         */
        private void parseRiver(final String[] words) {
            final RiverSize size = RiverSize.valueOf(words[1]);
            Location previous = null;
            for (int i = 2; i < words.length; i++) {
                final String[] xy = words[i].split(",");
                final Location corner = Location.of(Integer.parseInt(xy[0]),
                        Integer.parseInt(xy[1]));
                if (previous == null) {
                    addRiver(corner, size);
                    previous = corner;
                    continue;
                }
                Preconditions.checkArgument(
                        corner.getX() == previous.getX()
                                || corner.getY() == previous.getY(),
                        "River corner %s is not in line", corner);
                while (!previous.equals(corner)) {
                    final Location next = Location.of(
                            previous.getX() + Integer
                                    .signum(corner.getX() - previous.getX()),
                            previous.getY() + Integer
                                    .signum(corner.getY() - previous.getY()));
                    addRiver(next, size);
                    Preconditions.checkArgument(
                            joined.add(Set.of(previous, next)),
                            "Duplicate river segment at %s", next);
                    segments.add(new RiverSegmentPo(previous, next));
                    previous = next;
                }
            }
        }

        private void addRiver(final Location at, final RiverSize size) {
            if (rivers.get(at) != RiverSize.LARGE) {
                rivers.put(at, size);
            }
        }

        private void parseSettlement(final String[] words) {
            final NativeNation nation = NativeNation.valueOf(words[1]);
            final boolean capital = words.length == 4
                    && "capital".equals(words[2]);
            final String[] xy = words[words.length - 1].split(",");
            settlements.add(new GeneratedNatives.NativeSite(nation, capital,
                    Location.of(Integer.parseInt(xy[0]),
                            Integer.parseInt(xy[1]))));
        }

        /**
         * Rivers keep the inland rule of every bundled world: flat land only,
         * with no water beside them, not even at a corner.
         */
        private void verifyRivers() {
            final GeneratedGrid grid = new GeneratedGrid(types, WIDTH, HEIGHT);
            for (final Location at : rivers.keySet()) {
                final TerrainType type = grid.terrainAt(at);
                if (!grid.isLand(at) || type == TerrainType.HILL
                        || type == TerrainType.MOUNTAIN
                        || at.getNeighbors().stream().anyMatch(grid::isWater)) {
                    throw new IllegalStateException(
                            SOURCE + " river square " + at + " is not inland");
                }
            }
        }

        WorldMapPo toMap(final Random random) {
            final GeneratedGrid grid = new GeneratedGrid(types, WIDTH, HEIGHT);
            final Set<Location> forests = new HashSet<>(trees);
            forests.removeAll(rivers.keySet());
            final Map<Location, ResourceOverlay> resources = GeneratedWorldResources
                    .assign(grid, forests, rivers.keySet(), random);
            final Map<Location, MineralReserve> reserves = new HashMap<>();
            resources.forEach((at, overlay) -> {
                if (overlay.isFinite()) {
                    reserves.put(at, MineralReserve.initial(overlay));
                }
            });
            final WorldMapPo map = new WorldMapPo();
            map.setMaxX(WIDTH);
            map.setMaxY(HEIGHT);
            map.setSeed(SEED);
            map.setTerrainType(types);
            map.setTrees(forests);
            map.setFields(new HashSet<>());
            map.setRivers(rivers);
            map.setRiverSegments(segments);
            map.setResources(resources);
            map.setMineralReserves(reserves);
            return map;
        }
    }

}
