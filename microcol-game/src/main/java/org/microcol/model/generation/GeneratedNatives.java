package org.microcol.model.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.microcol.model.Direction;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.MovementPoints;
import org.microcol.model.NativeMarketRules;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeRelations;
import org.microcol.model.NativeTeachableSkills;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeGoodsStockPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * Native nations of a generated world, saved with the current identity schema:
 * one computer participant per nation, one capital and optionally ordinary
 * settlements, and home-linked braves beside each settlement. Nothing is an
 * empty placeholder and no identity is invented outside that schema.
 */
final class GeneratedNatives {

    /** Smallest distance in squares between two native settlements. */
    static final int SETTLEMENT_SPACING = 7;

    /** Nations with settlements: between this many and two more. */
    static final int MIN_NATIONS = 3;

    static final int EXTRA_NATIONS = 3;

    /** Braves that start beside every settlement. */
    static final int BRAVES = 2;

    /** Brave IDs start above the expedition's units. */
    static final int FIRST_BRAVE_ID = 100;

    static final int CAPITAL_TREASURY = 2000;

    static final int ORDINARY_TREASURY = 1000;

    private GeneratedNatives() {
    }

    /**
     * Add the native nations, settlements and braves to an expedition model
     * whose terrain is already generated.
     *
     * @param model  required model with the expedition and terrain
     * @param random required generator of this attempt
     * @throws IllegalStateException when the land has no room for the natives
     */
    static void place(final ModelPo model, final Random random) {
        final GeneratedGrid grid = new GeneratedGrid(
                model.getMap().getTerrainMap());
        final List<Location> sites = grid.squaresWith(
                terrain -> !terrain.isSea() && terrain != TerrainType.MOUNTAIN
                        && terrain != TerrainType.ARCTIC);
        Collections.shuffle(sites, random);
        final List<NativeNation> nations = new ArrayList<>(
                List.of(NativeNation.values()));
        Collections.shuffle(nations, random);
        final int count = MIN_NATIONS + random.nextInt(EXTRA_NATIONS);
        final List<NativeSettlementPo> settlements = new ArrayList<>();
        final List<PlayerPo> players = new ArrayList<>();
        final List<Location> taken = new ArrayList<>();
        for (final NativeNation nation : nations.subList(0, count)) {
            players.add(player(nation));
            settlements.add(settlement(settlements.size(), nation, true,
                    site(sites, taken)));
            if (random.nextBoolean()) {
                settlements.add(settlement(settlements.size(), nation, false,
                        site(sites, taken)));
            }
        }
        final Set<Location> occupied = new HashSet<>(taken);
        final List<UnitPo> braves = new ArrayList<>();
        int braveId = FIRST_BRAVE_ID;
        for (final NativeSettlementPo settlement : settlements) {
            final List<Location> around = new ArrayList<>();
            for (final Location neighbour : grid
                    .neighbours(settlement.getLocation())) {
                if (grid.isLand(neighbour)) {
                    around.add(neighbour);
                }
            }
            Collections.shuffle(around, random);
            int placed = 0;
            for (final Location at : around) {
                if (placed == BRAVES) {
                    break;
                }
                if (occupied.add(at)) {
                    braves.add(brave(braveId++, settlement, at));
                    placed++;
                }
            }
            if (placed < BRAVES) {
                throw new IllegalStateException(
                        "No room for the braves of a settlement");
            }
        }
        // Drawn after the layout, so the braves of a seed never move. The
        // market
        // draws nothing, so the teaching skill of a seed is the same as before.
        final WorldMapPo map = model.getMap();
        for (final NativeSettlementPo settlement : settlements) {
            final List<Terrain> around = surroundings(map, grid,
                    settlement.getLocation());
            settlement.setTeachableSkill(
                    NativeTeachableSkills.choose(around, random).name());
            settlement.setOfferedGoods(offeredStock(around));
            settlement.setWantedGoods(wantedGoods(around));
            settlement.setTrade(new ArrayList<>());
        }
        model.getGameManager().getPlayers().addAll(players);
        model.setNativeSettlements(settlements);
        model.setNativeSettlementKnowledge(new ArrayList<>());
        model.setNativeContacts(new ArrayList<>());
        model.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(settlements));
        model.getUnits().addAll(braves);
    }

    /**
     * @return the first free site at least {@link #SETTLEMENT_SPACING} away
     *         from every taken site; the site is then taken
     */
    private static Location site(final List<Location> sites,
            final List<Location> taken) {
        for (final Location candidate : sites) {
            if (taken.stream().allMatch(other -> other
                    .getDistanceChebyshev(candidate) >= SETTLEMENT_SPACING)) {
                taken.add(candidate);
                return candidate;
            }
        }
        throw new IllegalStateException("No room for a native settlement");
    }

    /**
     * @return the square of a settlement and its on-map neighbours, with the
     *         saved trees and resources that change its natural output
     */
    private static List<Terrain> surroundings(final WorldMapPo map,
            final GeneratedGrid grid, final Location centre) {
        final Set<Location> trees = map.getTreeSet();
        final Map<Location, ResourceOverlay> resources = map.getResourceMap();
        final List<Terrain> out = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                final Location at = Location.of(centre.getX() + dx,
                        centre.getY() + dy);
                if (!GeneratedGrid.isOnMap(at)) {
                    continue;
                }
                final Terrain terrain = new Terrain(at, grid.terrainAt(at));
                if (trees.contains(at)) {
                    terrain.setHasTrees(true);
                }
                terrain.setResourceOverlay(
                        resources.getOrDefault(at, ResourceOverlay.NONE));
                out.add(terrain);
            }
        }
        return out;
    }

    /**
     * @return the offered goods of a new settlement, each at its initial stock
     */
    private static List<NativeGoodsStockPo> offeredStock(
            final List<Terrain> surroundings) {
        final List<NativeGoodsStockPo> out = new ArrayList<>();
        for (final GoodsType goods : NativeMarketRules
                .offeredGoods(surroundings)) {
            final NativeGoodsStockPo stock = new NativeGoodsStockPo();
            stock.setGoods(goods.name());
            stock.setStock(NativeMarketRules.INITIAL_STOCK);
            out.add(stock);
        }
        return out;
    }

    /**
     * @return the goods names a new settlement wants to buy, in demand order
     */
    private static List<String> wantedGoods(final List<Terrain> surroundings) {
        final List<String> out = new ArrayList<>();
        for (final GoodsType goods : NativeMarketRules
                .wantedGoods(surroundings)) {
            out.add(goods.name());
        }
        return out;
    }

    private static NativeSettlementPo settlement(final int id,
            final NativeNation nation, final boolean capital,
            final Location at) {
        final NativeSettlementPo out = new NativeSettlementPo();
        out.setId(id);
        out.setNation(nation);
        out.setSettlementClass(nation.getSettlementClass());
        out.setCapital(capital);
        out.setLocation(at);
        out.setTreasury(capital ? CAPITAL_TREASURY : ORDINARY_TREASURY);
        return out;
    }

    private static PlayerPo player(final NativeNation nation) {
        final PlayerPo player = new PlayerPo();
        player.setName(nation.name());
        player.setComputer(true);
        player.setGold(0);
        player.setDeclaredIndependence(false);
        player.setNativeNation(nation);
        player.setUnitPurchaseCounts(new HashMap<>());
        final VisibilityPo visibility = new VisibilityPo();
        visibility.setVisibility(Set.of(), GeneratedWorld.WIDTH,
                GeneratedWorld.HEIGHT);
        player.setVisible(visibility);
        return player;
    }

    private static UnitPo brave(final int id,
            final NativeSettlementPo settlement, final Location at) {
        final UnitPo brave = new UnitPo();
        brave.setId(id);
        brave.setType(UnitType.BRAVE);
        brave.setOwnerId(settlement.getNation().name());
        brave.setAction(new UnitActionNoActionPo());
        brave.setMovementThirds(
                MovementPoints.ofPoints(UnitType.BRAVE.getSpeed()));
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        brave.setPlaceMap(place);
        brave.setNativeHomeSettlementId(settlement.getId());
        brave.setNativeHomeless(false);
        brave.setNativeRole(UnitNativeBrave.Role.BRAVE.name());
        return brave;
    }

}
