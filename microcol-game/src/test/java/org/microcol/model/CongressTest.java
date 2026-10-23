package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventFounderArrived;
import org.microcol.model.event.RoundStartedEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** National Congress accounting and frozen-offer boundaries. */
class CongressTest {
    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    private CongressPo active(final int bells, final Founder selected) {
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setBells(bells);
        po.setCandidates(OFFER);
        po.setSelected(selected);
        po.setGeneration(1);
        return po;
    }

    private GameModelPo fixture(final int bells, final Founder selected) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() == null) {
                player.setCongress(player.getName().equals("Dutch")
                        ? active(bells, selected)
                        : Congress.initial());
            }
        }
        return data;
    }

    private Model model(final int bells, final Founder selected) {
        return Model.make(fixture(bells, selected).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private int oneUpdateBellProduction() {
        final Model preview = model(0, null);
        final Player owner = preview.getCurrentPlayer();
        return owner.getColonies().stream()
                .mapToInt(colony -> Math.max(0, colony.getGoodsStats()
                        .getStatsByType(GoodsType.BELL).getRowProduction()))
                .sum();
    }

    private JsonObject savedWithUnitsOrderedById(final Model model) {
        final JsonObject saved = new Gson().toJsonTree(model.save())
                .getAsJsonObject();
        final List<JsonElement> units = new ArrayList<>();
        saved.getAsJsonArray("units").forEach(units::add);
        units.sort(Comparator.comparingInt(
                unit -> unit.getAsJsonObject().get("id").getAsInt()));
        final JsonArray ordered = new JsonArray();
        units.forEach(ordered::add);
        saved.add("units", ordered);
        return saved;
    }

    private Map<Location, TerrainType> isolateSavedVoyageOrigin(
            final GameModelPo data) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        final Map<Location, TerrainType> original = new java.util.HashMap<>(
                terrain);
        final Location origin = data.getModel().getUnits().stream()
                .filter(unit -> unit.getPlaceHighSeas() != null).findFirst()
                .orElseThrow().getDepartureLocation();
        terrain.replaceAll((location, type) -> type == TerrainType.HIGH_SEA
                ? TerrainType.OCEAN
                : type);
        terrain.put(origin, TerrainType.HIGH_SEA);
        origin.getNeighbors().stream()
                .filter(at -> at.getX() >= 1
                        && at.getX() <= data.getModel().getMap().getMaxX()
                        && at.getY() >= 1
                        && at.getY() <= data.getModel().getMap().getMaxY())
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        data.getModel().getMap().setTerrainType(terrain);
        return original;
    }

    @Test
    void catalogueHasFiveDistinctMembersPerCategory() {
        assertEquals(25, Founder.values().length);
        for (final Founder.Category category : Founder.Category.values()) {
            assertEquals(5,
                    Arrays.stream(Founder.values()).filter(
                            founder -> founder.getCategory() == category)
                            .count());
        }
    }

    @Test
    void firstActivationDrawsOnceAndReadsNeverReroll() {
        final Congress congress = new Congress(Congress.initial(),
                Difficulty.CONQUISTADOR);
        assertTrue(congress.offers(null).isEmpty());
        congress.activate();
        final List<Founder> first = congress.offers(null).stream()
                .map(FounderOffer::getFounder)
                .collect(java.util.stream.Collectors.toList());
        assertEquals(5, first.size());
        assertEquals(first,
                congress.offers(null).stream().map(FounderOffer::getFounder)
                        .collect(java.util.stream.Collectors.toList()));
        congress.activate();
        assertEquals(first, congress.save().getCandidates());
    }

    @ParameterizedTest
    @CsvSource({ "DISCOVERER,75", "EXPLORER,90", "CONQUISTADOR,100",
            "GOVERNOR,115", "VICEROY,130" })
    void firstObjectiveUsesDifficultyFactor(final Difficulty difficulty,
            final int expected) {
        final Congress congress = new Congress(active(0, null), difficulty);
        assertEquals(expected, congress.target(difficulty));
    }

    @Test
    void selectionIsFrozenBoundAndReadOnly() {
        final Model model = model(20, null);
        final Player dutch = model.getCurrentPlayer();
        final String before = new Gson().toJson(model.save());
        final List<FounderOffer> offers = dutch.getFounderOffers();
        assertEquals(5, offers.size());
        assertEquals(new HashSet<>(Arrays.asList(Founder.Category.values())),
                offers.stream().map(offer -> offer.getFounder().getCategory())
                        .collect(java.util.stream.Collectors.toSet()));
        assertEquals(before, new Gson().toJson(model.save()));
        final FounderOffer choice = offers.stream()
                .filter(offer -> offer.getFounder() == Founder.JOHN_PAUL_JONES)
                .findFirst().orElseThrow();
        assertTrue(dutch.canChooseFounder(choice));
        dutch.chooseFounder(choice);
        assertEquals(Founder.JOHN_PAUL_JONES,
                dutch.getSelectedFounder().orElseThrow());
        assertTrue(dutch.getFounderOffers().isEmpty());
        final String selected = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> dutch.chooseFounder(choice));
        assertEquals(selected, new Gson().toJson(model.save()));
        final Model other = model(20, null);
        assertFalse(other.getCurrentPlayer().canChooseFounder(choice));
        assertThrows(IllegalStateException.class,
                () -> other.getCurrentPlayer().chooseFounder(choice));
    }

    @Test
    void turnPreparationRejectsChoosingWithoutChangingOffer() {
        final Model model = model(20, null);
        final Player owner = model.getCurrentPlayer();
        final FounderOffer offer = owner.getFounderOffers().get(0);
        final String before = new Gson().toJson(model.save());
        final boolean[] checked = { false };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onRoundStarted(final RoundStartedEvent event) {
                assertFalse(owner.canChooseFounder(offer));
                assertThrows(TurnPreparationException.class,
                        () -> owner.chooseFounder(offer));
                checked[0] = true;
            }
        });
        model.startGame();
        assertTrue(checked[0]);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void ordinaryBellProductionJoinsJonesOnceAndPreservesLocalBells() {
        final int produced = oneUpdateBellProduction();
        final Model model = model(100 - produced, Founder.JOHN_PAUL_JONES);
        final Player owner = model.getCurrentPlayer();
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final int localBefore = colony.getWarehouse().getGoods(GoodsType.BELL)
                .getAmount();
        final long shipsBefore = model.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == owner
                        && unit.getType() == UnitType.FRIGATE)
                .count();
        owner.startTurn();
        assertEquals(0, owner.getCongressBells());
        assertEquals(List.of(Founder.JOHN_PAUL_JONES),
                owner.getCongressMembers());
        assertEquals(200, owner.getCongressTarget());
        assertEquals(localBefore + 1,
                colony.getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertEquals(shipsBefore + 1,
                model.getAllUnits().stream()
                        .filter(unit -> unit.getOwner() == owner
                                && unit.getType() == UnitType.FRIGATE
                                && unit.isAtEuropePort())
                        .count());
        assertFalse(owner.isJonesDeliveryPending());
        assertEquals(1,
                model.getTurnEventStore().getForPlayer(owner).stream().filter(
                        event -> event instanceof TurnEventFounderArrived)
                        .count());
        final JsonObject completed = savedWithUnitsOrderedById(model);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertEquals(completed, savedWithUnitsOrderedById(restored));
    }

    @Test
    void surplusIsCarriedToNextObjectiveAndOnlyOneJoins() {
        final int produced = oneUpdateBellProduction();
        final Model model = model(120 - produced, Founder.JOHN_PAUL_JONES);
        final Player owner = model.getCurrentPlayer();
        owner.startTurn();
        assertEquals(20, owner.getCongressBells());
        assertEquals(200, owner.getCongressTarget());
        assertEquals(1, owner.getCongressMembers().size());
    }

    @Test
    void exactNinetyNinePlusOneAndOneHundredTwentyCarry() {
        final Congress first = new Congress(active(99, Founder.JOHN_PAUL_JONES),
                Difficulty.CONQUISTADOR);
        first.credit(1);
        assertTrue(first.canJoin(Difficulty.CONQUISTADOR));
        assertEquals(Founder.JOHN_PAUL_JONES,
                first.join(Difficulty.CONQUISTADOR));
        assertEquals(0, first.bells());

        final Congress surplus = new Congress(
                active(120, Founder.JOHN_PAUL_JONES), Difficulty.CONQUISTADOR);
        assertEquals(Founder.JOHN_PAUL_JONES,
                surplus.join(Difficulty.CONQUISTADOR));
        assertEquals(20, surplus.bells());
        assertEquals(200, surplus.target(Difficulty.CONQUISTADOR));
    }

    @Test
    void malformedStateAndFinalCategoryExhaustion() {
        final CongressPo malformed = active(0, null);
        malformed.setCandidates(
                List.of(Founder.JOHN_PAUL_JONES, Founder.FRANCIS_DRAKE));
        assertThrows(IllegalArgumentException.class,
                () -> new Congress(malformed, Difficulty.CONQUISTADOR));

        final CongressPo last = active(2500, Founder.HERNANDO_DE_SOTO);
        final List<Founder> members = new ArrayList<>(
                Arrays.asList(Founder.values()));
        members.remove(Founder.HERNANDO_DE_SOTO);
        last.setMembers(members);
        last.setCandidates(List.of(Founder.HERNANDO_DE_SOTO));
        last.setGeneration(25);
        final Congress congress = new Congress(last, Difficulty.CONQUISTADOR);
        assertEquals(2500, congress.target(Difficulty.CONQUISTADOR));
        assertEquals(Founder.HERNANDO_DE_SOTO,
                congress.join(Difficulty.CONQUISTADOR));
        assertEquals(25, congress.members().size());
        assertEquals(0, congress.target(Difficulty.CONQUISTADOR));
        assertTrue(congress.offers(null).isEmpty());
    }

    @Test
    void independentJonesWithoutReachablePortKeepsOnePendingDelivery() {
        final GameModelPo data = fixture(100, Founder.JOHN_PAUL_JONES);
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow();
        RebellionFixtures.declare(data.getModel(), dutch);
        isolateSavedVoyageOrigin(data);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player owner = model.getPlayerStore().getPlayerByName("Dutch");
        assertFalse(owner.getColonies().isEmpty());
        owner.startTurn();
        assertEquals(List.of(Founder.JOHN_PAUL_JONES),
                owner.getCongressMembers());
        assertTrue(owner.isJonesDeliveryPending());
        assertEquals(0,
                model.getAllUnits().stream()
                        .filter(unit -> unit.getOwner() == owner
                                && unit.getType() == UnitType.FRIGATE)
                        .count());
        final String saved = new Gson().toJson(model.save());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertEquals(saved, new Gson().toJson(restored.save()));
        assertTrue(restored.getPlayerStore().getPlayerByName("Dutch")
                .isJonesDeliveryPending());
    }

    @Test
    void foundingFirstColonyFreezesOffersImmediately() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() == null)
                .forEach(player -> player.setCongress(Congress.initial()));
        final Location site = Location.of(26, 15);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        site.getNeighbors().stream()
                .filter(at -> at.getX() >= 1 && at.getX() <= 50
                        && at.getY() >= 1 && at.getY() <= 30)
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        terrain.put(site, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(java.util.Collections.emptySet());
        final UnitPo founderPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 4).findFirst().orElseThrow();
        founderPo.setPlaceCargoSlot(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(site);
        place.setOrientation(Direction.west);
        founderPo.setPlaceMap(place);
        founderPo.setAvailableMoves(1);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 5)
                .findFirst().orElseThrow().getCargo().getSlots().get(0)
                .setUnitId(null);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getPlayerStore().getPlayerByName("France");
        assertTrue(france.getFounderOffers().isEmpty());
        model.buildColony(france, model.getUnitById(4), "Quebec");
        assertEquals(5, france.getFounderOffers().size());
        final List<Founder> offers = france.getFounderOffers().stream()
                .map(FounderOffer::getFounder)
                .collect(java.util.stream.Collectors.toList());
        assertEquals(offers,
                france.getFounderOffers().stream().map(FounderOffer::getFounder)
                        .collect(java.util.stream.Collectors.toList()));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(offers, restored.getPlayerStore().getPlayerByName("France")
                .getFounderOffers().stream().map(FounderOffer::getFounder)
                .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void pendingJonesDeliversOnceWhenSeaRouteReturns() {
        final GameModelPo data = fixture(100, Founder.JOHN_PAUL_JONES);
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow();
        RebellionFixtures.declare(data.getModel(), dutch);
        final Map<Location, TerrainType> original = isolateSavedVoyageOrigin(
                data);
        final Model blocked = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player owner = blocked.getCurrentPlayer();
        owner.startTurn();
        assertTrue(owner.isJonesDeliveryPending());
        assertEquals(1, owner.getCongressMembers().size());
        final ModelPo repairedPo = blocked.save();
        repairedPo.getMap().setTerrainType(original);
        final Model repaired = Model.make(repairedPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player repairedOwner = repaired.getCurrentPlayer();
        repairedOwner.startTurn();
        assertFalse(repairedOwner.isJonesDeliveryPending());
        final List<Unit> frigates = repaired.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == repairedOwner
                        && unit.getType() == UnitType.FRIGATE)
                .collect(java.util.stream.Collectors.toList());
        assertEquals(1, frigates.size());
        assertEquals(
                repairedOwner.getColonies().stream()
                        .filter(colony -> repaired.isOwnedCoastalPort(
                                repairedOwner, colony.getLocation()))
                        .mapToInt(Colony::getId).min().orElseThrow(),
                repaired.getColonyAt(frigates.get(0).getLocation())
                        .orElseThrow().getId());
        repairedOwner.startTurn();
        assertEquals(1,
                repaired.getAllUnits().stream()
                        .filter(unit -> unit.getOwner() == repairedOwner
                                && unit.getType() == UnitType.FRIGATE)
                        .count());
    }
}
