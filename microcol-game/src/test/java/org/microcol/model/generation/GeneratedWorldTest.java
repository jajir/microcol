package org.microcol.model.generation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeNation;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.RiverSize;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.RiverSegmentPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;

class GeneratedWorldTest {

    private static final int AREA = GeneratedWorld.WIDTH
            * GeneratedWorld.HEIGHT;

    private static final int[] SEEDS = { 1, 7, 2024 };

    private static final int MAIN_MASS_MIN = 30;

    private static final int ISLAND_MAX = 12;

    private static ModelPo generate(final GeneratedClimate climate,
            final GeneratedLandShare share, final int seed) {
        return GeneratedWorld.create(
                new GeneratedWorldSettings(seed, climate, share),
                EuropeanNation.DUTCH, Difficulty.CONQUISTADOR);
    }

    /**
     * Every generated settlement saves one accepted native-teachable skill, and
     * the same seed reproduces the skills of every settlement.
     */
    @Test
    void generatedSettlementsOfferAcceptedSkillsAndRepeatWithSeed() {
        final Set<String> accepted = UnitType.NATIVE_TEACHABLE_SKILLS.stream()
                .map(UnitType::name).collect(Collectors.toSet());
        for (final int seed : SEEDS) {
            final List<String> first = generate(GeneratedClimate.TEMPERATE,
                    GeneratedLandShare.MEDIUM, seed).getNativeSettlements()
                    .stream().map(NativeSettlementPo::getTeachableSkill)
                    .collect(Collectors.toList());
            final List<String> second = generate(GeneratedClimate.TEMPERATE,
                    GeneratedLandShare.MEDIUM, seed).getNativeSettlements()
                    .stream().map(NativeSettlementPo::getTeachableSkill)
                    .collect(Collectors.toList());
            assertFalse(first.isEmpty());
            assertTrue(accepted.containsAll(first), first.toString());
            assertEquals(first, second);
        }
    }

    /**
     * @return orthogonal neighbours on the map only; squares beyond the edge
     *         are not sea, so a flood fill stays inside the map
     */
    private static List<Location> neighbours(final Location at) {
        final List<Location> out = new ArrayList<>();
        final Location[] around = { Location.of(at.getX() + 1, at.getY()),
                Location.of(at.getX() - 1, at.getY()),
                Location.of(at.getX(), at.getY() + 1),
                Location.of(at.getX(), at.getY() - 1) };
        for (final Location next : around) {
            if (next.getX() >= 1 && next.getX() <= GeneratedWorld.WIDTH
                    && next.getY() >= 1
                    && next.getY() <= GeneratedWorld.HEIGHT) {
                out.add(next);
            }
        }
        return out;
    }

    private static TerrainType terrainAt(final WorldMapPo map,
            final Location at) {
        return map.getTerrainMap().getOrDefault(at, TerrainType.OCEAN);
    }

    private static Set<Location> landOf(final WorldMapPo map) {
        final Set<Location> out = new HashSet<>();
        map.getTerrainMap().forEach((at, type) -> {
            if (!type.isSea()) {
                out.add(at);
            }
        });
        return out;
    }

    /** Connected groups of squares with orthogonal steps. */
    private static List<Set<Location>> groups(final Set<Location> squares,
            final Map<Location, List<Location>> links) {
        final List<Set<Location>> out = new ArrayList<>();
        final Set<Location> seen = new HashSet<>();
        for (final Location start : squares) {
            if (!seen.add(start)) {
                continue;
            }
            final Set<Location> group = new HashSet<>();
            final Deque<Location> queue = new ArrayDeque<>();
            group.add(start);
            queue.add(start);
            while (!queue.isEmpty()) {
                for (final Location next : links.getOrDefault(queue.remove(),
                        List.of())) {
                    if (squares.contains(next) && group.add(next)) {
                        seen.add(next);
                        queue.add(next);
                    }
                }
            }
            out.add(group);
        }
        return out;
    }

    private static Map<Location, List<Location>> landLinks(
            final Set<Location> land) {
        final Map<Location, List<Location>> out = new HashMap<>();
        for (final Location at : land) {
            out.put(at, neighbours(at));
        }
        return out;
    }

    @Test
    void everyCombinationMatchesShareSizeAndConnectedMasses() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final GeneratedLandShare share : GeneratedLandShare.values()) {
                for (final int seed : SEEDS) {
                    final ModelPo po = generate(climate, share, seed);
                    final WorldMapPo map = po.getMap();
                    assertEquals(GeneratedWorld.WIDTH, map.getMaxX());
                    assertEquals(GeneratedWorld.HEIGHT, map.getMaxY());
                    assertEquals(seed, map.getSeed().intValue());
                    final Set<Location> land = landOf(map);
                    final double fraction = (double) land.size() / AREA;
                    assertTrue(Math.abs(fraction - share.getPercent()
                            / 100.0) <= GeneratedLandShare.TOLERANCE_PERCENT
                                    / 100.0,
                            () -> climate + " " + share + " " + seed);
                    final List<Set<Location>> parts = groups(land,
                            landLinks(land));
                    final List<Set<Location>> mains = parts.stream()
                            .filter(part -> part.size() >= MAIN_MASS_MIN)
                            .collect(Collectors.toList());
                    assertTrue(mains.size() >= 1 && mains.size() <= 3);
                    final int mainLand = mains.stream().mapToInt(Set::size)
                            .sum();
                    assertTrue(mainLand >= 0.75 * land.size());
                    parts.stream().filter(part -> part.size() < MAIN_MASS_MIN)
                            .forEach(part -> assertTrue(
                                    part.size() <= ISLAND_MAX));
                }
            }
        }
    }

    @Test
    void latitudeBandsFollowTheClimate() {
        for (final int seed : SEEDS) {
            final WorldMapPo warm = generate(GeneratedClimate.WARM,
                    GeneratedLandShare.MEDIUM, seed).getMap();
            assertTrue(warm.getTerrainMap().values().stream()
                    .noneMatch(type -> type == TerrainType.ARCTIC));
            final WorldMapPo temperate = generate(GeneratedClimate.TEMPERATE,
                    GeneratedLandShare.MEDIUM, seed).getMap();
            temperate.getTerrainMap().forEach((at, type) -> {
                if (type == TerrainType.ARCTIC) {
                    assertTrue(at.getY() <= 3 || at.getY() >= 28);
                }
            });
            final WorldMapPo cold = generate(GeneratedClimate.COLD,
                    GeneratedLandShare.MEDIUM, seed).getMap();
            cold.getTerrainMap().forEach((at, type) -> {
                if (type == TerrainType.ARCTIC) {
                    assertTrue(at.getY() <= 6 || at.getY() >= 25);
                }
            });
        }
    }

    @Test
    void riversStayOnInlandLowlandsWithoutLoops() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final GeneratedLandShare share : GeneratedLandShare.values()) {
                for (final int seed : SEEDS) {
                    final WorldMapPo map = generate(climate, share, seed)
                            .getMap();
                    final Map<Location, RiverSize> rivers = map.getRiverMap();
                    final Map<Location, List<Location>> links = new HashMap<>();
                    for (final RiverSegmentPo segment : map
                            .getRiverSegments()) {
                        links.computeIfAbsent(segment.getFrom(),
                                key -> new ArrayList<>()).add(segment.getTo());
                        links.computeIfAbsent(segment.getTo(),
                                key -> new ArrayList<>())
                                .add(segment.getFrom());
                    }
                    assertFalse(rivers.isEmpty());
                    for (final Location at : rivers.keySet()) {
                        assertFalse(terrainAt(map, at) == TerrainType.HILL
                                || terrainAt(map, at) == TerrainType.MOUNTAIN,
                                at.toString());
                        assertTrue(at.getNeighbors().stream()
                                .filter(GeneratedGrid::isOnMap).noneMatch(
                                        next -> terrainAt(map, next).isSea()),
                                at.toString());
                    }
                    assertEquals(rivers.keySet(), links.keySet());
                    final Set<Location> land = landOf(map);
                    assertTrue(land.containsAll(rivers.keySet()));
                    for (final Set<Location> course : groups(rivers.keySet(),
                            links)) {
                        final int edges = course.stream()
                                .mapToInt(at -> links.get(at).size()).sum() / 2;
                        // A simple path: one fewer link than squares, none
                        // above
                        // two.
                        assertEquals(course.size() - 1, edges);
                        assertTrue(course.stream()
                                .allMatch(at -> links.get(at).size() <= 2));
                        final List<Location> ends = course.stream()
                                .filter(at -> links.get(at).size() == 1)
                                .collect(Collectors.toList());
                        assertEquals(2, ends.size());
                        assertTrue(ends.stream().anyMatch(
                                at -> neighbours(at).stream().anyMatch(next -> {
                                    final TerrainType type = terrainAt(map,
                                            next);
                                    return type == TerrainType.HILL
                                            || type == TerrainType.MOUNTAIN;
                                })));
                        assertTrue(ends.stream().anyMatch(at -> neighbours(at)
                                .stream()
                                .anyMatch(next -> !terrainAt(map, next).isSea()
                                        && next.getNeighbors().stream()
                                                .filter(GeneratedGrid::isOnMap)
                                                .anyMatch(water -> terrainAt(
                                                        map, water).isSea()))));
                        final boolean main = course.stream().anyMatch(
                                at -> rivers.get(at) == RiverSize.LARGE);
                        if (main) {
                            assertTrue(
                                    course.size() >= 8 && course.size() <= 16);
                            assertTrue(course.stream().allMatch(
                                    at -> rivers.get(at) == RiverSize.LARGE));
                        } else {
                            assertTrue(
                                    course.size() >= 3 && course.size() <= 7);
                            assertTrue(course.stream().allMatch(
                                    at -> rivers.get(at) == RiverSize.SMALL));
                        }
                    }
                }
            }
        }
    }

    @Test
    void resourcesFitTerrainAndCoverAboutTenPercent() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final int seed : SEEDS) {
                final WorldMapPo map = generate(climate, GeneratedLandShare.LOW,
                        seed).getMap();
                final Set<Location> land = landOf(map);
                final Set<Location> suitable = new HashSet<>(land);
                for (int x = 1; x <= GeneratedWorld.WIDTH; x++) {
                    for (int y = 1; y <= GeneratedWorld.HEIGHT; y++) {
                        final Location at = Location.of(x, y);
                        if (terrainAt(map, at) == TerrainType.OCEAN
                                && neighbours(at).stream()
                                        .anyMatch(land::contains)) {
                            suitable.add(at);
                        }
                    }
                }
                final Map<Location, ResourceOverlay> resources = map
                        .getResourceMap();
                suitable.removeAll(map.getRiverMap().keySet());
                final double ratio = (double) resources.keySet().stream()
                        .filter(at -> !map.getRiverMap().containsKey(at))
                        .count() / suitable.size();
                assertTrue(ratio >= 0.095 && ratio <= 0.105, () -> "" + ratio);
                final Set<Location> trees = map.getTreeSet();
                assertTrue(map.getRiverMap().keySet().stream()
                        .noneMatch(trees::contains));
                resources.forEach((at, overlay) -> {
                    final TerrainType type = terrainAt(map, at);
                    assertTrue(overlay.canBeOn(type, map.getRiverMap()
                            .getOrDefault(at, RiverSize.NONE)));
                    if (map.getRiverMap().containsKey(at)) {
                        assertFalse(trees.contains(at));
                        assertTrue(overlay == ResourceOverlay.SILVER
                                || overlay == ResourceOverlay.ORE);
                        return;
                    }
                    switch (overlay) {
                    case SILVER:
                    case ORE:
                    case MIXED_MINERALS:
                        assertTrue(type == TerrainType.HILL
                                || type == TerrainType.MOUNTAIN);
                        break;
                    case PRIME_TIMBER:
                    case TRAPPING:
                    case GAME:
                        // Forest-only families are the only ones on trees.
                        break;
                    case OASIS:
                        assertEquals(TerrainType.DESERT, type);
                        break;
                    case FISHERY:
                        assertEquals(TerrainType.OCEAN, type);
                        break;
                    default:
                        assertTrue(type == TerrainType.GRASSLAND
                                || type == TerrainType.PRAIRIE
                                || type == TerrainType.SAVANNAH
                                || type == TerrainType.PLAINS);
                    }
                    if (overlay == ResourceOverlay.PRIME_TIMBER
                            || overlay == ResourceOverlay.TRAPPING
                            || overlay == ResourceOverlay.GAME) {
                        assertTrue(trees.contains(at));
                    }
                });
                final Map<Location, org.microcol.model.MineralReserve> reserves = map
                        .getMineralReserveMap();
                resources.forEach((at, overlay) -> {
                    if (overlay.isFinite()) {
                        assertEquals(
                                overlay.getInitialReserve(GoodsType.SILVER),
                                reserves.get(at)
                                        .getRemaining(GoodsType.SILVER));
                        assertEquals(overlay.getInitialReserve(GoodsType.ORE),
                                reserves.get(at).getRemaining(GoodsType.ORE));
                    }
                });
                assertEquals(resources.entrySet().stream()
                        .filter(entry -> entry.getValue().isFinite()).count(),
                        reserves.size());
            }
        }
    }

    @Test
    void expeditionAndNativesUseTheCurrentSchema() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final int seed : SEEDS) {
                final ModelPo po = generate(climate, GeneratedLandShare.MEDIUM,
                        seed);
                final WorldMapPo map = po.getMap();
                for (final EuropeanNation nation : EuropeanNation.values()) {
                    final UnitPo ship = po.getUnits().stream()
                            .filter(unit -> unit.getType().isShip() && nation
                                    .getPlayerName().equals(unit.getOwnerId()))
                            .findFirst().orElseThrow();
                    final Location start = ship.getPlaceMap().getLocation();
                    assertEquals(TerrainType.HIGH_SEA, terrainAt(map, start));
                    assertTrue(outwardCoastSites(map, start) >= 4);
                }
                final List<NativeSettlementPo> settlements = po
                        .getNativeSettlements();
                final Set<NativeNation> nations = settlements.stream()
                        .map(NativeSettlementPo::getNation)
                        .collect(Collectors.toSet());
                assertTrue(nations.size() >= 3 && nations.size() <= 5);
                for (final NativeNation nation : nations) {
                    assertEquals(1, settlements.stream().filter(
                            settlement -> settlement.getNation() == nation
                                    && settlement.getCapital())
                            .count());
                }
                for (final NativeSettlementPo settlement : settlements) {
                    assertEquals(settlement.getNation().getSettlementClass(),
                            settlement.getSettlementClass());
                    assertFalse(
                            terrainAt(map, settlement.getLocation()).isSea());
                    assertEquals(settlement.getCapital() ? 2000 : 1000,
                            settlement.getTreasury().intValue());
                }
                for (final NativeSettlementPo settlement : settlements) {
                    for (final NativeSettlementPo other : settlements) {
                        if (settlement != other) {
                            assertTrue(settlement.getLocation()
                                    .getDistanceChebyshev(
                                            other.getLocation()) >= 7);
                        }
                    }
                }
                final List<PlayerPo> natives = po.getGameManager().getPlayers()
                        .stream()
                        .filter(player -> player.getNativeNation() != null)
                        .collect(Collectors.toList());
                assertEquals(nations.size(), natives.size());
                for (final PlayerPo player : natives) {
                    assertTrue(player.isComputer());
                    assertEquals(player.getNativeNation().name(),
                            player.getName());
                }
                final List<UnitPo> braves = po.getUnits().stream()
                        .filter(unit -> unit.getType() == UnitType.BRAVE)
                        .collect(Collectors.toList());
                assertEquals(2 * settlements.size(), braves.size());
                assertEquals(braves.size(),
                        braves.stream().map(UnitPo::getId).distinct().count());
                assertEquals(po.getUnits().size(), po.getUnits().stream()
                        .map(UnitPo::getId).distinct().count());
                for (final UnitPo brave : braves) {
                    final NativeSettlementPo home = settlements.stream()
                            .filter(settlement -> settlement.getId()
                                    .equals(brave.getNativeHomeSettlementId()))
                            .findFirst().orElseThrow();
                    assertEquals(home.getNation().name(), brave.getOwnerId());
                    assertFalse(brave.getNativeHomeless());
                    final Location at = brave.getPlaceMap().getLocation();
                    assertTrue(
                            home.getLocation().getDistanceChebyshev(at) == 1);
                    assertFalse(terrainAt(map, at).isSea());
                }
                assertEquals(po.getNativeLocalAlarm().size(),
                        settlements.size() * EuropeanNation.values().length);
            }
        }
    }

    /**
     * @return number of land squares that a ship from this start reaches by
     *         sailing through ocean, excluding mountains
     */
    private static int outwardCoastSites(final WorldMapPo map,
            final Location start) {
        final Set<Location> seen = new HashSet<>();
        final Set<Location> sites = new HashSet<>();
        final Deque<Location> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            for (final Location next : neighbours(queue.remove())) {
                final TerrainType type = terrainAt(map, next);
                if (type.isSea()) {
                    if (seen.add(next)) {
                        queue.add(next);
                    }
                } else if (type != TerrainType.MOUNTAIN) {
                    sites.add(next);
                }
            }
        }
        return sites.size();
    }

    @Test
    void sameSettingsReproduceTheSameWorld() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final GeneratedLandShare share : GeneratedLandShare.values()) {
                final ModelPo first = generate(climate, share, 7);
                final ModelPo second = generate(climate, share, 7);
                assertArrayEquals(first.getMap().getTiles(),
                        second.getMap().getTiles());
                assertEquals(first.getMap().getRiverMap(),
                        second.getMap().getRiverMap());
                assertEquals(first.getMap().getResourceMap(),
                        second.getMap().getResourceMap());
                assertEquals(first.getMap().getTreeSet(),
                        second.getMap().getTreeSet());
                assertEquals(snapshot(first), snapshot(second));
                assertEquals(summary(first.getNativeSettlements()),
                        summary(second.getNativeSettlements()));
            }
        }
        assertFalse(java.util.Arrays.equals(
                generate(GeneratedClimate.TEMPERATE, GeneratedLandShare.MEDIUM,
                        1).getMap().getTiles(),
                generate(GeneratedClimate.TEMPERATE, GeneratedLandShare.MEDIUM,
                        2).getMap().getTiles()));
    }

    @Test
    void generatedWorldStartsSavesStopsAndContinuesWithItsSettings() {
        final ModelPo po = generate(GeneratedClimate.COLD,
                GeneratedLandShare.HIGH, 42);
        final Model model = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        final ModelPo saved = model.save();
        model.stop();
        assertSettings(saved, GeneratedClimate.COLD, GeneratedLandShare.HIGH,
                42);
        assertArrayEquals(po.getMap().getTiles(), saved.getMap().getTiles());
        assertEquals(po.getMap().getRiverMap(), saved.getMap().getRiverMap());

        // Continue loads the started game as saved; nothing is generated again.
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.isGameStarted());
        final ModelPo again = restored.save();
        assertSettings(again, GeneratedClimate.COLD, GeneratedLandShare.HIGH,
                42);
        assertArrayEquals(saved.getMap().getTiles(), again.getMap().getTiles());
        assertEquals(snapshot(saved), snapshot(again));
        assertEquals(summary(saved.getNativeSettlements()),
                summary(again.getNativeSettlements()));
        restored.stop();
    }

    private static void assertSettings(final ModelPo po,
            final GeneratedClimate climate, final GeneratedLandShare share,
            final int seed) {
        assertEquals(seed, po.getGeneratedWorld().getSeed().intValue());
        assertEquals(climate, po.getGeneratedWorld().getClimate());
        assertEquals(share, po.getGeneratedWorld().getLandShare());
        assertEquals(seed, po.getMap().getSeed().intValue());
    }

    /**
     * @return one line per unit: identity, type, owner, every placement form it
     *         holds, and the units in its cargo, so passengers are compared by
     *         their cargo relation instead of a map square
     */
    private static List<String> snapshot(final ModelPo po) {
        return po.getUnits().stream().map(unit -> {
            final StringBuilder out = new StringBuilder();
            out.append(unit.getId()).append(' ').append(unit.getType())
                    .append(' ').append(unit.getOwnerId());
            if (unit.getPlaceMap() != null) {
                final Location at = unit.getPlaceMap().getLocation();
                out.append(" map=").append(at.getX()).append(',')
                        .append(at.getY());
            }
            if (unit.getPlaceCargoSlot() != null) {
                out.append(" cargo");
            }
            if (unit.getPlaceEuropePort() != null) {
                out.append(" europePort");
            }
            if (unit.getPlaceHighSeas() != null) {
                out.append(" highSeas");
            }
            if (unit.getPlaceColonyField() != null) {
                out.append(" colonyField");
            }
            if (unit.getPlaceConstructionSlot() != null) {
                out.append(" construction");
            }
            if (unit.getPlaceShipRepair() != null) {
                out.append(" shipRepair");
            }
            unit.getCargo().getSlots().forEach(
                    slot -> out.append(" hold=").append(slot.getUnitId()));
            return out.toString();
        }).collect(Collectors.toList());
    }

    private static List<String> summary(
            final List<NativeSettlementPo> settlements) {
        return settlements.stream().map(settlement -> settlement.getId() + " "
                + settlement.getNation() + " " + settlement.getLocation().getX()
                + "," + settlement.getLocation().getY() + " capital="
                + settlement.getCapital()).collect(Collectors.toList());
    }

    @Test
    void invalidSettingsAndSeedsAreRejected() {
        for (final String text : new String[] { "", "  ", "-1", "+1",
                "1000000000", "12a", "1 2", "١٢" }) {
            assertThrows(IllegalArgumentException.class,
                    () -> GeneratedWorldSettings.parseSeed(text));
        }
        assertEquals(42, GeneratedWorldSettings.parseSeed(" 42 "));
        assertEquals(0, GeneratedWorldSettings.parseSeed("0"));
        assertEquals(999_999_999,
                GeneratedWorldSettings.parseSeed("999999999"));
        assertThrows(NullPointerException.class,
                () -> GeneratedWorldSettings.parseSeed(null));
        assertThrows(IllegalArgumentException.class,
                () -> new GeneratedWorldSettings(-1, GeneratedClimate.TEMPERATE,
                        GeneratedLandShare.MEDIUM));
        assertThrows(IllegalArgumentException.class,
                () -> new GeneratedWorldSettings(1_000_000_000,
                        GeneratedClimate.TEMPERATE, GeneratedLandShare.MEDIUM));
        assertThrows(NullPointerException.class,
                () -> new GeneratedWorldSettings(1, null,
                        GeneratedLandShare.MEDIUM));
        assertThrows(NullPointerException.class, () -> GeneratedWorld
                .create(null, EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
        assertThrows(NullPointerException.class,
                () -> GeneratedWorld.create(
                        new GeneratedWorldSettings(1, GeneratedClimate.COLD,
                                GeneratedLandShare.LOW),
                        null, Difficulty.CONQUISTADOR));
    }

    @Test
    void savedSettingsThatDoNotMatchTheWorldAreRejected() {
        final ModelPo mismatched = generate(GeneratedClimate.WARM,
                GeneratedLandShare.LOW, 5);
        mismatched.getGeneratedWorld().setSeed(6);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(mismatched,
                        MissionImpl.getPredefinedGameOverEvaluators()));
        assertEquals(6, mismatched.getGeneratedWorld().getSeed().intValue());
        final ModelPo incomplete = generate(GeneratedClimate.WARM,
                GeneratedLandShare.LOW, 5);
        incomplete.getGeneratedWorld().setClimate(null);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(incomplete,
                        MissionImpl.getPredefinedGameOverEvaluators()));
        assertFalse(incomplete.getGeneratedWorld().isComplete());
    }

    @Test
    void riverResourceSharesMatchTheAgreedDistribution() {
        final Map<Location, TerrainType> terrain = new java.util.HashMap<>();
        for (int x = 1; x <= GeneratedWorld.WIDTH; x++) {
            for (int y = 1; y <= GeneratedWorld.HEIGHT; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        final Map<Location, ResourceOverlay> resources = GeneratedWorldResources
                .assign(new GeneratedGrid(terrain), Set.of(), terrain.keySet(),
                        new java.util.Random(290));
        final long silver = resources.values().stream()
                .filter(r -> r == ResourceOverlay.SILVER).count();
        final long ore = resources.values().stream()
                .filter(r -> r == ResourceOverlay.ORE).count();
        assertEquals(resources.size(), silver + ore);
        assertEquals(0.15, (double) silver / terrain.size(), 0.015);
        assertEquals(0.05, (double) ore / terrain.size(), 0.01);
    }
}
