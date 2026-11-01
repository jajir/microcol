package org.microcol.model.generation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeNation;
import org.microcol.model.RiverSize;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.UnitWithCargo;

class NorthAmericaWorldTest {

    /** Expedition starts on the Atlantic lanes, in nation order. */
    private static final Map<EuropeanNation, Location> STARTS = Map.of(
            EuropeanNation.DUTCH, Location.of(92, 30), EuropeanNation.FRENCH,
            Location.of(91, 45), EuropeanNation.ENGLISH, Location.of(98, 54),
            EuropeanNation.SPANISH, Location.of(100, 64));

    /** Newfoundland landing used by the TC_198 player journey. */
    private static final Location SITE = Location.of(88, 30);

    private static final Location LANDING_SEA = Location.of(89, 30);

    @TempDir
    Path directory;

    @Test
    void sourceDrawsRecognizableNorthAmerica() {
        final WorldMapPo map = NorthAmericaWorld.terrain();
        final Map<Location, TerrainType> types = map.getTerrainMap();
        assertEquals(NorthAmericaWorld.WIDTH, map.getMaxX());
        assertEquals(NorthAmericaWorld.HEIGHT, map.getMaxY());
        assertTrue(NorthAmericaWorld.WIDTH > GeneratedWorld.WIDTH);
        assertTrue(NorthAmericaWorld.HEIGHT > GeneratedWorld.HEIGHT);

        // Seas and bays.
        assertSea(types, Location.of(63, 20), "Hudson Bay");
        assertSea(types, Location.of(68, 25), "James Bay");
        assertSea(types, Location.of(75, 16), "Hudson Strait");
        assertSea(types, Location.of(60, 58), "Gulf of Mexico");
        assertSea(types, Location.of(45, 54), "Gulf of California");
        assertSea(types, Location.of(20, 19), "Gulf of Alaska");
        assertSea(types, Location.of(80, 31), "Gulf of St. Lawrence");
        assertSea(types, Location.of(70, 44), "Chesapeake Bay");
        assertSea(types, Location.of(64, 66), "Caribbean Sea");
        // Lakes.
        assertEquals(TerrainType.LAKE, at(types, Location.of(62, 31)),
                "Lake Superior");
        assertEquals(TerrainType.LAKE, at(types, Location.of(63, 36)),
                "Lake Michigan");
        assertEquals(TerrainType.LAKE, at(types, Location.of(66, 34)),
                "Lake Huron");
        assertEquals(TerrainType.LAKE, at(types, Location.of(66, 39)),
                "Lake Erie");
        assertEquals(TerrainType.LAKE, at(types, Location.of(70, 36)),
                "Lake Ontario");
        assertEquals(TerrainType.LAKE, at(types, Location.of(38, 11)),
                "Great Bear Lake");
        assertEquals(TerrainType.LAKE, at(types, Location.of(43, 16)),
                "Great Slave Lake");
        assertEquals(TerrainType.LAKE, at(types, Location.of(55, 27)),
                "Lake Winnipeg");
        // Land.
        assertLand(types, Location.of(12, 5), "Point Barrow, Alaska");
        assertLand(types, Location.of(4, 12), "Seward Peninsula");
        assertLand(types, Location.of(70, 6), "Baffin Island");
        assertLand(types, Location.of(86, 31), "Newfoundland");
        assertLand(types, Location.of(79, 35), "Nova Scotia");
        assertLand(types, Location.of(67, 56), "Florida");
        assertLand(types, Location.of(60, 53), "Mississippi delta");
        assertLand(types, Location.of(42, 53), "Baja California");
        assertLand(types, Location.of(62, 63), "Yucatan");
        assertLand(types, Location.of(70, 77), "Panama");
        assertLand(types, Location.of(68, 61), "Cuba");
        assertLand(types, Location.of(75, 63), "Hispaniola");
        assertLand(types, Location.of(70, 65), "Jamaica");
        assertLand(types, Location.of(79, 65), "Puerto Rico");
        assertEquals(TerrainType.MOUNTAIN, at(types, Location.of(42, 30)),
                "Rocky Mountains");
        assertEquals(TerrainType.MOUNTAIN, at(types, Location.of(38, 40)),
                "Cascades and Sierra Nevada");
        assertEquals(TerrainType.MOUNTAIN, at(types, Location.of(50, 58)),
                "Sierra Madre Occidental");
        assertEquals(TerrainType.HILL, at(types, Location.of(68, 41)),
                "Appalachians");
        assertEquals(TerrainType.DESERT, at(types, Location.of(41, 43)),
                "Great Basin");
        assertEquals(TerrainType.PRAIRIE, at(types, Location.of(50, 30)),
                "Great Plains");
        assertEquals(TerrainType.ARCTIC, at(types, Location.of(47, 3)),
                "Victoria Island");
        assertTrue(map.getTreeSet().contains(Location.of(45, 20)),
                "Boreal forest");

        // Hudson Bay and the Gulf are the open ocean, the lakes are not.
        final Set<Location> atlantic = seaReachableFrom(types,
                STARTS.get(EuropeanNation.DUTCH));
        assertTrue(atlantic.contains(Location.of(63, 20)));
        assertTrue(atlantic.contains(Location.of(60, 58)));
        assertTrue(atlantic.contains(Location.of(20, 19)));
        assertFalse(atlantic.contains(Location.of(62, 31)));
        for (int y = 1; y <= map.getMaxY(); y++) {
            for (final int x : new int[] { 1, map.getMaxX() }) {
                assertFalse(isLand(types, Location.of(x, y)));
            }
        }
        for (int x = 1; x <= map.getMaxX(); x++) {
            for (final int y : new int[] { 1, map.getMaxY() }) {
                assertFalse(isLand(types, Location.of(x, y)));
            }
        }
        final long land = types.values().stream().filter(type -> !type.isSea())
                .count();
        assertTrue(land > 2000 && land < 4000, "Land squares: " + land);
    }

    @Test
    void lakesAreEnclosedAndRiversStayInland() {
        final WorldMapPo map = NorthAmericaWorld.terrain();
        final Map<Location, TerrainType> types = map.getTerrainMap();
        for (final Map.Entry<Location, TerrainType> entry : types.entrySet()) {
            if (entry.getValue() == TerrainType.LAKE) {
                for (final Location next : entry.getKey().getNeighbors()) {
                    final TerrainType type = at(types, next);
                    assertTrue(!type.isSea() || type == TerrainType.LAKE,
                            "Lake beside the sea at " + entry.getKey());
                }
            }
        }
        final Map<Location, RiverSize> rivers = map.getRiverMap();
        for (final Location river : rivers.keySet()) {
            final TerrainType type = at(types, river);
            assertFalse(type.isSea() || type == TerrainType.HILL
                    || type == TerrainType.MOUNTAIN, river.toString());
            assertFalse(map.getTreeSet().contains(river), river.toString());
            assertTrue(river.getNeighbors().stream().noneMatch(
                    next -> at(types, next).isSea()), river.toString());
        }
        map.getRiverSegments().forEach(segment -> assertTrue(
                segment.getFrom().isNeighbor(segment.getTo())));
        // The Mississippi crosses the continent from the lakes to the Gulf.
        assertEquals(RiverSize.LARGE, rivers.get(Location.of(57, 31)));
        assertEquals(RiverSize.LARGE, rivers.get(Location.of(60, 52)));
        assertEquals(RiverSize.SMALL, rivers.get(Location.of(55, 42)),
                "Missouri");
        assertEquals(RiverSize.SMALL, rivers.get(Location.of(61, 44)), "Ohio");
        assertEquals(RiverSize.LARGE, rivers.get(Location.of(36, 14)),
                "Mackenzie");
        assertEquals(RiverSize.LARGE, rivers.get(Location.of(18, 12)), "Yukon");
        assertEquals(RiverSize.SMALL, rivers.get(Location.of(52, 49)),
                "Rio Grande");
        assertEquals(RiverSize.SMALL, rivers.get(Location.of(43, 47)),
                "Colorado");
        final long mississippi = rivers.entrySet().stream()
                .filter(entry -> entry.getValue() == RiverSize.LARGE
                        && entry.getKey().getX() >= 57
                        && entry.getKey().getY() >= 31)
                .count();
        assertEquals(25, mississippi);
    }

    @Test
    void everyNationAndDifficultyStartsLegally() {
        for (final EuropeanNation nation : EuropeanNation.values()) {
            for (final Difficulty difficulty : Difficulty.values()) {
                final ModelPo saved = NorthAmericaWorld.create(nation,
                        difficulty);
                assertEquals(difficulty, saved.getDifficulty());
                assertEquals(STARTS.get(nation), saved.getFocusedField());
                final Model model = make(saved);
                assertTrue(model.getPlayerByName(nation.getPlayerName())
                        .isHuman());
                assertEquals(1000 - 250 * difficulty.ordinal(), model
                        .getPlayerByName(nation.getPlayerName()).getGold());
                for (final EuropeanNation other : EuropeanNation.values()) {
                    final Unit ship = model
                            .getPlayerByName(other.getPlayerName())
                            .getAllUnits().stream().filter(Unit::isShip)
                            .findFirst().orElseThrow();
                    assertEquals(STARTS.get(other), ship.getLocation());
                    assertEquals(TerrainType.HIGH_SEA, model.getMap()
                            .getTerrainTypeAt(ship.getLocation()));
                    assertEquals(2, ((UnitWithCargo) ship).getCargo().getSlots()
                            .stream().filter(slot -> slot.getUnit().isPresent())
                            .count());
                }
            }
        }
    }

    @Test
    void nativeNationsLiveInTheirRegionsWithHomeLinkedBraves() {
        final ModelPo saved = NorthAmericaWorld.create(EuropeanNation.DUTCH,
                Difficulty.CONQUISTADOR);
        final List<NativeSettlementPo> settlements = saved
                .getNativeSettlements();
        assertEquals(12, settlements.size());
        final Set<NativeNation> nations = settlements.stream()
                .map(NativeSettlementPo::getNation).collect(Collectors.toSet());
        assertEquals(Set.of(NativeNation.IROQUOIS, NativeNation.CHEROKEE,
                NativeNation.SIOUX, NativeNation.APACHE, NativeNation.AZTEC,
                NativeNation.ARAWAK), nations);
        assertEquals(nations, saved.getGameManager().getPlayers().stream()
                .map(PlayerPo::getNativeNation).filter(nation -> nation != null)
                .collect(Collectors.toSet()));
        for (final NativeNation nation : nations) {
            assertEquals(1, settlements.stream()
                    .filter(settlement -> settlement.getNation() == nation
                            && settlement.getCapital())
                    .count(), nation.name());
        }
        assertEquals(Location.of(54, 63),
                settlement(settlements, NativeNation.AZTEC).getLocation());
        assertEquals(Location.of(68, 61),
                settlement(settlements, NativeNation.ARAWAK).getLocation());
        assertEquals(Location.of(72, 39),
                settlement(settlements, NativeNation.IROQUOIS).getLocation());

        final Model model = make(saved);
        final Set<Location> settled = new HashSet<>();
        for (final NativeSettlementPo settlement : settlements) {
            final TerrainType type = model.getMap()
                    .getTerrainTypeAt(settlement.getLocation());
            assertTrue(type.isLand() && type != TerrainType.MOUNTAIN
                    && type != TerrainType.ARCTIC);
            assertEquals(RiverSize.NONE,
                    model.getMap().getRiverSizeAt(settlement.getLocation()));
            assertTrue(settled.add(settlement.getLocation()));
            final List<UnitPo> braves = saved.getUnits().stream()
                    .filter(unit -> settlement.getId()
                            .equals(unit.getNativeHomeSettlementId()))
                    .collect(Collectors.toList());
            assertEquals(GeneratedNatives.BRAVES, braves.size());
            for (final UnitPo brave : braves) {
                assertSame(UnitType.BRAVE, brave.getType());
                assertEquals(settlement.getNation().name(), brave.getOwnerId());
                final Location location = brave.getPlaceMap().getLocation();
                assertTrue(location.isNeighbor(settlement.getLocation()));
                assertTrue(model.getMap().getTerrainTypeAt(location).isLand());
            }
        }
        assertFalse(model.getMap().getTerrainTypeAt(SITE).isSea());
    }

    @Test
    void repeatedSetupGivesTheSameWorld() {
        final ModelPo first = NorthAmericaWorld.create(EuropeanNation.ENGLISH,
                Difficulty.VICEROY);
        final ModelPo second = NorthAmericaWorld.create(EuropeanNation.ENGLISH,
                Difficulty.VICEROY);
        assertArrayEquals(first.getMap().getTiles(),
                second.getMap().getTiles());
        assertArrayEquals(first.getMap().getTrees(),
                second.getMap().getTrees());
        assertArrayEquals(first.getMap().getRumors(),
                second.getMap().getRumors());
        assertEquals(first.getMap().getRiverMap(),
                second.getMap().getRiverMap());
        assertEquals(first.getMap().getResourceMap(),
                second.getMap().getResourceMap());
        assertEquals(describe(first), describe(second));
        // Another nation shares the geography and native world.
        final ModelPo other = NorthAmericaWorld.create(EuropeanNation.SPANISH,
                Difficulty.DISCOVERER);
        assertArrayEquals(first.getMap().getTiles(), other.getMap().getTiles());
        assertArrayEquals(first.getMap().getRumors(),
                other.getMap().getRumors());
    }

    @Test
    void newfoundlandLandingIsALegalColonySite() {
        final Model model = make(NorthAmericaWorld.create(EuropeanNation.DUTCH,
                Difficulty.CONQUISTADOR));
        assertEquals(TerrainType.OCEAN,
                model.getMap().getTerrainTypeAt(LANDING_SEA));
        assertTrue(LANDING_SEA.isNeighbor(SITE));
        assertFalse(model.getMap().hasLostCityRumorAt(SITE));
        final Unit founder = model.getPlayerByName("Dutch").getAllUnits()
                .stream().filter(unit -> !unit.isShip()).findFirst()
                .orElseThrow();
        assertTrue(model.canFoundColonyAt(founder, SITE));
        assertTrue(
                model.getMap().getTerrainTypeAt(Location.of(86, 33)).isLand());
    }

    @Test
    void savedGameRestoresTheWholeWorld() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo game = dao
                .loadFromClassPath(FreePlayMap.NORTH_AMERICA.getResource());
        game.setModel(NorthAmericaWorld.create(EuropeanNation.FRENCH,
                Difficulty.GOVERNOR));
        final Model model = make(game.getModel());
        model.startGame();
        game.setModel(model.save());
        final Path file = directory.resolve("north-america.microcol");
        dao.saveToFile(file.toString(), game);
        final GameModelPo loaded = dao.loadFromFile(file.toFile());
        final Model restored = make(loaded.getModel());
        restored.startGame();
        assertEquals(NorthAmericaWorld.WIDTH,
                restored.getMap().getMaxLocationX());
        assertEquals(NorthAmericaWorld.HEIGHT,
                restored.getMap().getMaxLocationY());
        assertArrayEquals(game.getModel().getMap().getTiles(),
                restored.save().getMap().getTiles());
        assertEquals(game.getModel().getMap().getRiverMap(),
                restored.save().getMap().getRiverMap());
        assertEquals(Difficulty.GOVERNOR, restored.getDifficulty());
        assertTrue(restored.getPlayerByName("France").isHuman());
        assertEquals(12, restored.save().getNativeSettlements().size());
        assertEquals(model.getAllUnits().size(), restored.getAllUnits().size());
        assertNotNull(restored.getPlayerByName(NativeNation.AZTEC.name()));
    }

    /**
     * Writes the data-derived review diagram {@code target/r0221/
     * north-america-preview.svg}: terrain colors, forest dots, river courses,
     * native settlements (red) and the four expedition starts (black).
     */
    @Test
    void fullMapPreviewShowsEverySquare() throws Exception {
        final ModelPo saved = NorthAmericaWorld.create(EuropeanNation.DUTCH,
                Difficulty.CONQUISTADOR);
        final WorldMapPo map = saved.getMap();
        final Map<Location, TerrainType> types = map.getTerrainMap();
        final Set<Location> trees = map.getTreeSet();
        final int tile = 8;
        final StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(map.getMaxX() * tile).append("\" height=\"")
                .append(map.getMaxY() * tile).append("\">\n");
        int squares = 0;
        for (int y = 1; y <= map.getMaxY(); y++) {
            for (int x = 1; x <= map.getMaxX(); x++) {
                final Location location = Location.of(x, y);
                svg.append("<rect x=\"").append((x - 1) * tile)
                        .append("\" y=\"").append((y - 1) * tile)
                        .append("\" width=\"").append(tile)
                        .append("\" height=\"").append(tile)
                        .append("\" fill=\"").append(color(at(types, location)))
                        .append("\"/>\n");
                squares++;
                if (trees.contains(location)) {
                    svg.append("<circle cx=\"").append((x - 1) * tile + 4)
                            .append("\" cy=\"").append((y - 1) * tile + 4)
                            .append("\" r=\"2\" fill=\"#1f4d1f\"/>\n");
                }
            }
        }
        map.getRiverSegments().forEach(segment -> svg.append("<line x1=\"")
                .append((segment.getFrom().getX() - 1) * tile + 4)
                .append("\" y1=\"")
                .append((segment.getFrom().getY() - 1) * tile + 4)
                .append("\" x2=\"")
                .append((segment.getTo().getX() - 1) * tile + 4)
                .append("\" y2=\"")
                .append((segment.getTo().getY() - 1) * tile + 4)
                .append("\" stroke=\"#1e5bd8\" stroke-width=\"2\"/>\n"));
        saved.getNativeSettlements().forEach(settlement -> svg
                .append("<rect x=\"")
                .append((settlement.getLocation().getX() - 1) * tile + 1)
                .append("\" y=\"")
                .append((settlement.getLocation().getY() - 1) * tile + 1)
                .append("\" width=\"6\" height=\"6\" fill=\"#d01c1c\"/>\n"));
        STARTS.values().forEach(start -> svg.append("<circle cx=\"")
                .append((start.getX() - 1) * tile + 4).append("\" cy=\"")
                .append((start.getY() - 1) * tile + 4)
                .append("\" r=\"3\" fill=\"#000000\"/>\n"));
        svg.append("</svg>\n");
        final Path output = Path.of("target", "r0221",
                "north-america-preview.svg");
        java.nio.file.Files.createDirectories(output.getParent());
        java.nio.file.Files.writeString(output, svg.toString());
        assertEquals(NorthAmericaWorld.WIDTH * NorthAmericaWorld.HEIGHT,
                squares);
        assertTrue(java.nio.file.Files.size(output) > 0);
    }

    private static String color(final TerrainType type) {
        if (type == TerrainType.HIGH_SEA) {
            return "#1d4f86";
        } else if (type == TerrainType.OCEAN) {
            return "#2f6fb0";
        } else if (type == TerrainType.LAKE) {
            return "#5aa8e0";
        } else if (type == TerrainType.ARCTIC) {
            return "#f2f5f7";
        } else if (type == TerrainType.TUNDRA) {
            return "#a9b48f";
        } else if (type == TerrainType.GRASSLAND) {
            return "#7fb45a";
        } else if (type == TerrainType.PRAIRIE) {
            return "#cfc46a";
        } else if (type == TerrainType.PLAINS) {
            return "#bfa35a";
        } else if (type == TerrainType.SAVANNAH) {
            return "#a7c25a";
        } else if (type == TerrainType.DESERT) {
            return "#e6cf8f";
        } else if (type == TerrainType.MARSH) {
            return "#6f8f6a";
        } else if (type == TerrainType.SWAMP) {
            return "#4f7a55";
        } else if (type == TerrainType.HILL) {
            return "#9a7f5a";
        }
        return "#5e4e40";
    }

    private static NativeSettlementPo settlement(
            final List<NativeSettlementPo> settlements,
            final NativeNation nation) {
        return settlements.stream()
                .filter(settlement -> settlement.getNation() == nation
                        && settlement.getCapital())
                .findFirst().orElseThrow();
    }

    private static String describe(final ModelPo model) {
        final StringBuilder out = new StringBuilder();
        model.getNativeSettlements()
                .forEach(settlement -> out.append(settlement.getId())
                        .append(settlement.getNation())
                        .append(settlement.getLocation())
                        .append(settlement.getTeachableSkill()).append(';'));
        model.getUnits()
                .forEach(unit -> out.append(unit.getId()).append(unit.getType())
                        .append(unit.getOwnerId())
                        .append(unit.getPlaceMap() == null ? "-"
                                : unit.getPlaceMap().getLocation())
                        .append(';'));
        return out.toString();
    }

    private static Model make(final ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static TerrainType at(final Map<Location, TerrainType> types,
            final Location location) {
        return types.getOrDefault(location, TerrainType.OCEAN);
    }

    private static boolean isLand(final Map<Location, TerrainType> types,
            final Location location) {
        return !at(types, location).isSea();
    }

    private static void assertSea(final Map<Location, TerrainType> types,
            final Location location, final String name) {
        assertTrue(at(types, location) == TerrainType.OCEAN
                || at(types, location) == TerrainType.HIGH_SEA, name);
    }

    private static void assertLand(final Map<Location, TerrainType> types,
            final Location location, final String name) {
        assertTrue(isLand(types, location), name);
    }

    /** Open sea reached from a square, moving in any direction. */
    private static Set<Location> seaReachableFrom(
            final Map<Location, TerrainType> types, final Location start) {
        final Set<Location> seen = new HashSet<>();
        final Deque<Location> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            for (final Location next : queue.remove().getNeighbors()) {
                if (next.getX() >= 1 && next.getX() <= NorthAmericaWorld.WIDTH
                        && next.getY() >= 1
                        && next.getY() <= NorthAmericaWorld.HEIGHT
                        && (at(types, next) == TerrainType.OCEAN
                                || at(types, next) == TerrainType.HIGH_SEA)
                        && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return seen;
    }
}
