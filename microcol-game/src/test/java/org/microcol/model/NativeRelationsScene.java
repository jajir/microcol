package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeChiefEncounterPo;
import org.microcol.model.store.NativeContactPo;
import org.microcol.model.store.NativeGoodsStockPo;
import org.microcol.model.store.NativeLandRecordPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Valid free-play save with one native settlement for the native relation
 * tests. Units are added as complete saved units, so the real loader and the
 * real turn code derive every pressure; nothing here calls a formula.
 */
final class NativeRelationsScene {

    static final String DUTCH = "Dutch";

    static final String FRANCE = "France";

    /** Open water in free play; the island scene turns it into land. */
    static final Location SITE = Location.of(36, 8);

    /** Sea squares inside the island, one, two and three squares from SITE. */
    static final Location SEA_ONE = Location.of(35, 9);
    static final Location SEA_TWO = Location.of(38, 10);
    static final Location SEA_THREE = Location.of(33, 5);

    /** The French colony Montreal of the free-play map and its identity. */
    static final Location MONTREAL = Location.of(17, 21);
    private static final int MONTREAL_ID = 1;

    static final Consumer<UnitPo> UNARMED = unit -> {
    };
    static final Consumer<UnitPo> SOLDIER = unit -> unit.setHoldingGuns(true);
    static final Consumer<UnitPo> DRAGOON = unit -> {
        unit.setHoldingGuns(true);
        unit.setMounted(true);
    };
    static final Consumer<UnitPo> VETERAN = unit -> {
        unit.setHoldingGuns(true);
        unit.setVeteran(true);
    };
    /** Veteran rank without muskets is not armed. */
    static final Consumer<UnitPo> UNARMED_VETERAN = unit -> unit
            .setVeteran(true);
    /** Horses without muskets is not armed. */
    static final Consumer<UnitPo> SCOUT = unit -> unit.setMounted(true);
    static final Consumer<UnitPo> PIONEER = unit -> unit.setTools(80);

    private final ModelPo po;
    private final List<NativeSettlementPo> natives = new ArrayList<>();
    private int nextUnitId = 200;

    private NativeRelationsScene() {
        po = new GameModelPoDao().loadFromClassPath("/maps/free-play.microcol")
                .getModel();
        po.setDifficulty(Difficulty.CONQUISTADOR);
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH);
    }

    /**
     * @param dx required columns east of {@link #SITE}
     * @param dy required rows south of {@link #SITE}
     * @return the square at that offset from the island settlement
     */
    static Location at(final int dx, final int dy) {
        return Location.of(SITE.getX() + dx, SITE.getY() + dy);
    }

    /**
     * Settlement on a nine by nine island far from every other saved object,
     * with the three sea squares for ships.
     */
    static NativeRelationsScene island(final NativeNation nation) {
        final NativeRelationsScene scene = new NativeRelationsScene();
        scene.requireNothingWithin(SITE, 8);
        final Map<Location, TerrainType> terrain = scene.po.getMap()
                .getTerrainMap();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                terrain.put(at(dx, dy), TerrainType.GRASSLAND);
            }
        }
        for (final Location sea : List.of(SEA_ONE, SEA_TWO, SEA_THREE)) {
            terrain.put(sea, TerrainType.OCEAN);
        }
        scene.po.getMap().setTerrainType(terrain);
        return scene.settle(nation, SITE);
    }

    /**
     * Settlement on the land east of Montreal, at least two squares away so
     * that no square worked by the colony changes its terrain.
     */
    static NativeRelationsScene besideMontreal(final NativeNation nation,
            final int distance) {
        Preconditions.checkArgument(distance >= 2,
                "A closer site would change a worked colony square");
        final NativeRelationsScene scene = new NativeRelationsScene();
        final Location site = Location.of(MONTREAL.getX() + distance,
                MONTREAL.getY());
        final Map<Location, TerrainType> terrain = scene.po.getMap()
                .getTerrainMap();
        terrain.put(site, TerrainType.GRASSLAND);
        scene.po.getMap().setTerrainType(terrain);
        return scene.settle(nation, site);
    }

    private NativeRelationsScene settle(final NativeNation nation,
            final Location at) {
        final NativeSettlementPo settlement = new NativeSettlementPo();
        settlement.setId(0);
        settlement.setNation(nation);
        settlement.setSettlementClass(nation.getSettlementClass());
        settlement.setCapital(true);
        settlement.setTreasury(2000);
        settlement.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        settlement.setOfferedGoods(List.of());
        settlement.setWantedGoods(List.of());
        settlement.setTrade(List.of());
        settlement.setLocation(at);
        natives.add(settlement);
        po.setNativeSettlements(natives);
        po.setNativeSettlementKnowledge(new ArrayList<>());
        po.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        po.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(natives));
        po.setNativeLandRights(new ArrayList<>());
        po.setNativeLandTakings(new ArrayList<>());
        // Montreal is a worked, claimed square in the Montreal fixtures. Its
        // explicit French right keeps the resident and fort expectations exact;
        // unpaid claimed use is covered by NativeLandPressureTest.
        if (at.getDistanceChebyshev(MONTREAL) <= nation.getSettlementClass()
                .getHomeRadius()) {
            return paid(EuropeanNation.FRENCH, MONTREAL);
        }
        return this;
    }

    private void requireNothingWithin(final Location center,
            final int distance) {
        for (final ColonyPo colony : po.getColonies()) {
            Preconditions.checkState(
                    colony.getLocation()
                            .getDistanceChebyshev(center) > distance,
                    "A saved colony would disturb the island");
        }
        for (final UnitPo unit : po.getUnits()) {
            Preconditions.checkState(
                    unit.getPlaceMap() == null
                            || unit.getPlaceMap().getLocation()
                                    .getDistanceChebyshev(center) > distance,
                    "A saved unit would disturb the island");
        }
    }

    /** Add a complete saved unit standing on the map. */
    NativeRelationsScene unit(final String owner, final UnitType type,
            final Location at, final Consumer<UnitPo> equipment) {
        final UnitPo unit = new UnitPo();
        unit.setId(nextUnitId++);
        unit.setType(type);
        unit.setOwnerId(owner);
        if (type == UnitType.COLONIST) {
            unit.setWorkUpdates(0);
            unit.setMissionary(false);
        }
        if (type == UnitType.ARTILLERY) {
            unit.setArtilleryDamaged(false);
        }
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        equipment.accept(unit);
        po.getUnits().add(unit);
        return this;
    }

    NativeRelationsScene tribal(final NativeNation nation,
            final EuropeanNation european, final double value) {
        po.getNativeTribalAnger().stream()
                .filter(entry -> entry.getNation() == nation
                        && entry.getEuropean() == european)
                .findFirst().orElseThrow().setValue(value);
        return this;
    }

    NativeRelationsScene alarm(final EuropeanNation european,
            final double value) {
        po.getNativeLocalAlarm().stream()
                .filter(entry -> entry.getSettlementId() == 0
                        && entry.getEuropean() == european)
                .findFirst().orElseThrow().setValue(value);
        return this;
    }

    /**
     * Explicit paid right of a European on a claimed square. The square must be
     * claimed by a settlement of this scene.
     */
    NativeRelationsScene paid(final EuropeanNation european,
            final Location at) {
        final NativeLandRecordPo record = new NativeLandRecordPo();
        record.setEuropean(european);
        record.setLocation(at);
        po.getNativeLandRights().add(record);
        return this;
    }

    /** Turn one square into land, for a saved record that needs real land. */
    NativeRelationsScene landAt(final Location at) {
        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        terrain.put(at, TerrainType.GRASSLAND);
        po.getMap().setTerrainType(terrain);
        return this;
    }

    /**
     * Make the island settlement an ordinary one with the initial treasury of
     * 1000 and give its nation a capital elsewhere, so the save stays valid:
     * every nation with settlements needs exactly one capital.
     */
    NativeRelationsScene ordinaryWithCapitalAt(final Location capitalAt) {
        final NativeSettlementPo ordinary = natives.get(0);
        ordinary.setCapital(false);
        ordinary.setTreasury(1000);
        ordinary.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        ordinary.setOfferedGoods(List.of());
        ordinary.setWantedGoods(List.of());
        ordinary.setTrade(List.of());
        final NativeSettlementPo capital = new NativeSettlementPo();
        capital.setId(1);
        capital.setNation(ordinary.getNation());
        capital.setSettlementClass(ordinary.getSettlementClass());
        capital.setCapital(true);
        capital.setTreasury(2000);
        capital.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        capital.setOfferedGoods(List.of());
        capital.setWantedGoods(List.of());
        capital.setTrade(List.of());
        capital.setLocation(capitalAt);
        natives.add(capital);
        po.setNativeSettlements(natives);
        po.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(natives));
        return this;
    }

    /** Give the unit added last its full movement, so it may act now. */
    NativeRelationsScene withMovesForLastUnit() {
        po.getUnits().get(po.getUnits().size() - 1).setMovementThirds(3);
        return this;
    }

    /**
     * Set the market of the first settlement: each offered good at one stock,
     * and the wanted goods in their order.
     */
    NativeRelationsScene market(final int stock, final List<GoodsType> offered,
            final List<GoodsType> wanted) {
        final List<NativeGoodsStockPo> stocks = new ArrayList<>();
        for (final GoodsType goods : offered) {
            final NativeGoodsStockPo entry = new NativeGoodsStockPo();
            entry.setGoods(goods.name());
            entry.setStock(stock);
            stocks.add(entry);
        }
        final List<String> names = new ArrayList<>();
        for (final GoodsType goods : wanted) {
            names.add(goods.name());
        }
        natives.get(0).setOfferedGoods(stocks);
        natives.get(0).setWantedGoods(names);
        return this;
    }

    /** Set goods the first settlement has already bought, as saved. */
    NativeRelationsScene receivedOfFirst(final GoodsType goods,
            final int amount) {
        final NativeGoodsStockPo entry = new NativeGoodsStockPo();
        entry.setGoods(goods.name());
        entry.setStock(amount);
        natives.get(0).setReceivedGoods(new ArrayList<>(List.of(entry)));
        return this;
    }

    /** Set the treasury of the first settlement, as saved. */
    NativeRelationsScene treasuryOfFirst(final int amount) {
        natives.get(0).setTreasury(amount);
        return this;
    }

    /** Save one chief encounter of a European at the first settlement. */
    NativeRelationsScene chiefOfFirst(final EuropeanNation european,
            final NativeChiefOutcome outcome) {
        final NativeChiefEncounterPo encounter = new NativeChiefEncounterPo();
        encounter.setEuropean(european);
        encounter.setOutcome(outcome);
        encounter.setInformed(outcome == NativeChiefOutcome.INFORMATION);
        natives.get(0).setChiefEncounters(List.of(encounter));
        return this;
    }

    /** Record a saved land introduction of a European to a tribe. */
    NativeRelationsScene contact(final EuropeanNation european,
            final NativeNation nation) {
        if (po.getNativeContacts() == null) {
            po.setNativeContacts(new ArrayList<>());
        }
        final NativeContactPo contact = new NativeContactPo();
        contact.setEuropean(european);
        contact.setNation(nation);
        po.getNativeContacts().add(contact);
        return this;
    }

    /** Remove every explicit paid right, leaving claimed squares unpaid. */
    NativeRelationsScene withoutRights() {
        po.getNativeLandRights().clear();
        return this;
    }

    /** Set the gold of a player of the saved game. */
    NativeRelationsScene gold(final String owner, final int amount) {
        player(owner).setGold(amount);
        return this;
    }

    /** Make the named player the current one before the model is built. */
    NativeRelationsScene currentPlayer(final String owner) {
        po.getGameManager().setCurrentPlayer(owner);
        return this;
    }

    /** Give a colonial player a Congress whose only member is Peter Minuit. */
    NativeRelationsScene minuitMember(final String owner) {
        final CongressPo congress = player(owner).getCongress();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(List.of(Founder.PETER_MINUIT));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(null);
        congress.setGeneration(2);
        congress.setJonesDeliveryPending(false);
        return this;
    }

    /**
     * Give a colonial player a Congress whose only member is Jean de Brebeuf.
     */
    NativeRelationsScene brebeufMember(final String owner) {
        final CongressPo congress = player(owner).getCongress();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(List.of(Founder.JEAN_DE_BREBEUF));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(null);
        congress.setGeneration(2);
        congress.setJonesDeliveryPending(false);
        return this;
    }

    /** Give a colonial player a Congress whose only member is Pocahontas. */
    NativeRelationsScene pocahontasMember(final String owner) {
        final CongressPo congress = player(owner).getCongress();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(List.of(Founder.POCAHONTAS));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(null);
        congress.setGeneration(2);
        congress.setJonesDeliveryPending(false);
        return this;
    }

    /**
     * Give the Dutch Congress an open objective list and enough bells. The
     * offer keeps the rule of one candidate per category: the founder takes the
     * place of the default candidate of its own category, so Pocahontas gives
     * Jones, Pocahontas, Smith, Penn and Magellan.
     */
    NativeRelationsScene dutchCongressReadyFor(final Founder founder) {
        final CongressPo congress = player(DUTCH).getCongress();
        congress.setActivated(true);
        congress.setBells(100);
        congress.setMembers(List.of());
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder fallback : List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN)) {
            candidates.add(
                    fallback.getCategory() == founder.getCategory() ? founder
                            : fallback);
        }
        congress.setCandidates(candidates);
        congress.setSelected(null);
        congress.setGeneration(1);
        congress.setJonesDeliveryPending(false);
        return this;
    }

    /** Add a fort or fortress to the French colony Montreal. */
    NativeRelationsScene montrealFortification(final ConstructionType type) {
        Preconditions.checkArgument(type == ConstructionType.FORT
                || type == ConstructionType.FORTRESS);
        final ColonyPo montreal = po.getColonies().stream()
                .filter(colony -> colony.getId() == MONTREAL_ID).findFirst()
                .orElseThrow();
        final ConstructionPo construction = new ConstructionPo();
        construction.setType(type);
        construction.setSlots(new ArrayList<>());
        montreal.getConstructions().add(construction);
        return this;
    }

    private PlayerPo player(final String name) {
        return po.getGameManager().getPlayers().stream()
                .filter(player -> name.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    ModelPo toPo() {
        return po;
    }

    Model toModel() {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** @return the only native settlement of a scene model */
    static NativeSettlement settlement(final Model model) {
        return model.getNativeSettlements().get(0);
    }

}
