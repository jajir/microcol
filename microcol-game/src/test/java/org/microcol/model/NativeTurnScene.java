package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.VisibilityPo;

import com.google.common.base.Preconditions;

/**
 * Free-play world with a nine by nine grassland island far from every other
 * saved object, so native settlements and braves stand on valid land. The stock
 * free-play map is ocean there; building the land in the fixture keeps the
 * production land validation untouched.
 */
final class NativeTurnScene {

    /** Centre of the island. */
    static final Location ORIGIN = Location.of(36, 8);

    /** Square of the island, at a column and row offset from the centre. */
    static Location at(final int dx, final int dy) {
        return Location.of(ORIGIN.getX() + dx, ORIGIN.getY() + dy);
    }

    private final ModelPo po;
    private final List<NativeSettlementPo> settlements = new ArrayList<>();
    private int nextUnitId = 5000;

    private NativeTurnScene() {
        po = new GameModelPoDao().loadFromClassPath("/maps/free-play.microcol")
                .getModel();
        po.setDifficulty(Difficulty.CONQUISTADOR);
        for (final var colony : po.getColonies()) {
            Preconditions.checkState(
                    colony.getLocation().getDistanceChebyshev(ORIGIN) > 8,
                    "A saved colony would disturb the island");
        }
        for (final UnitPo unit : po.getUnits()) {
            Preconditions.checkState(
                    unit.getPlaceMap() == null || unit.getPlaceMap()
                            .getLocation().getDistanceChebyshev(ORIGIN) > 8,
                    "A saved unit would disturb the island");
        }
        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                terrain.put(at(dx, dy), TerrainType.GRASSLAND);
            }
        }
        po.getMap().setTerrainType(terrain);
        po.setNativeSettlements(settlements);
        po.setNativeSettlementKnowledge(new ArrayList<>());
        po.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        po.setNativeLandRights(new ArrayList<>());
        po.setNativeLandTakings(new ArrayList<>());
    }

    /** Free-play world with the island, no natives and the game not started. */
    static NativeTurnScene island() {
        return new NativeTurnScene();
    }

    /** Turn an island square into ocean, for placement-rejection cases. */
    NativeTurnScene sea(final int dx, final int dy) {
        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        terrain.put(at(dx, dy), TerrainType.OCEAN);
        po.getMap().setTerrainType(terrain);
        return this;
    }

    /** Native settlement of a nation, at an island offset. */
    NativeTurnScene settlement(final int id, final NativeNation nation,
            final boolean capital, final int dx, final int dy) {
        final NativeSettlementPo settlement = new NativeSettlementPo();
        settlement.setId(id);
        settlement.setNation(nation);
        settlement.setSettlementClass(nation.getSettlementClass());
        settlement.setCapital(capital);
        settlement.setTreasury(capital ? 2000 : 1000);
        settlement.setLocation(at(dx, dy));
        settlements.add(settlement);
        return this;
    }

    /** Explicit native participant, saved in the order this is called. */
    NativeTurnScene nativePlayer(final NativeNation nation) {
        po.getGameManager().getPlayers().add(nativePlayerPo(nation));
        return this;
    }

    /** Same participant inserted before every saved player. */
    NativeTurnScene nativePlayerFirst(final NativeNation nation) {
        po.getGameManager().getPlayers().add(0, nativePlayerPo(nation));
        return this;
    }

    private static PlayerPo nativePlayerPo(final NativeNation nation) {
        final PlayerPo player = new PlayerPo();
        player.setName(nation.name());
        player.setComputer(true);
        player.setGold(0);
        player.setDeclaredIndependence(false);
        player.setNativeNation(nation);
        player.setUnitPurchaseCounts(new HashMap<>());
        player.setVisible(new VisibilityPo());
        return player;
    }

    /** Brave of a native nation; a null home means an explicit survivor. */
    NativeTurnScene brave(final NativeNation owner, final Integer homeId,
            final int dx, final int dy) {
        return rawBrave(owner.name(), homeId, homeId == null, dx, dy);
    }

    /** Brave with every home field as given, for malformed-state tests. */
    NativeTurnScene rawBrave(final String ownerName, final Integer homeId,
            final Boolean homeless, final int dx, final int dy) {
        return rawUnit(nextUnitId++, UnitType.BRAVE, ownerName, homeId,
                homeless, at(dx, dy));
    }

    /** Saved unit with fully chosen identity and place, no cargo or work. */
    NativeTurnScene rawUnit(final int id, final UnitType type,
            final String ownerName, final Integer homeId,
            final Boolean homeless, final Location where) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(ownerName);
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(where);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        unit.setNativeHomeSettlementId(homeId);
        unit.setNativeHomeless(homeless);
        if (type == UnitType.COLONIST) {
            unit.setWorkUpdates(0);
            unit.setMissionary(false);
        }
        po.getUnits().add(unit);
        return this;
    }

    /** Dutch colonist standing on an island square. */
    NativeTurnScene dutchColonist(final int dx, final int dy) {
        return rawUnit(nextUnitId++, UnitType.COLONIST, "Dutch", null, null,
                at(dx, dy));
    }

    /** Final saved form, with the per-settlement alarm filled in. */
    ModelPo po() {
        po.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(settlements));
        return po;
    }

    /** Model loaded through the ordinary save path. */
    Model model() {
        return make(po());
    }

    static Model make(final ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

}
