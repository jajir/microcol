package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.ForeignPowerComparison.Disclosure;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Foreign report gate, earned contact and exact aggregates on the free-play map
 * with a third nation. Dutch is the viewer; France owns Quebec (resident 4,
 * soldier 6) and Montreal (resident 5), Spain owns nothing until a test gives
 * it units. Dutch unit 0 is the scout, unit 3 the Dutch galleon at sea.
 *
 * <p>
 * Field mapping (R-0168): population is the sum of colony residents, colony
 * count is the number of owned colonies, the average is population divided by
 * colony count (zero without colonies), military is the sum of
 * {@code CombatStrengths.armedLandBase} of every living armed land unit, naval
 * is the sum of {@code CombatStrengths.shipBase} of every living ship whose
 * type is armed (a ship under repair still counts), and trade is the net
 * European sale revenue of the power's last ten owner turns.
 * </p>
 */
class ForeignPowerReportTest {

    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location SCOUT_AT_QUEBEC = Location.of(19, 10);
    private static final Location FAR_FROM_MONTREAL = Location.of(21, 21);
    private static final Location ENTRY = Location.of(21, 12);
    private static final Location EXIT = Location.of(23, 12);
    private static final Location NEAR_QUEBEC_SEA = Location.of(17, 9);
    private static final double EXACT = 0.0001;

    private final Gson gson = new Gson();
    private Model model;
    private Player dutch;
    private Player france;
    private Player spain;

    /** Free play with Spain as a third colonial nation at peace with both. */
    private GameModelPo data(final boolean deWitt) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        final List<PlayerPo> players = data.getModel().getGameManager()
                .getPlayers();
        final PlayerPo copy = gson.fromJson(gson.toJson(
                players.stream().filter(p -> p.getName().equals("France"))
                        .findFirst().orElseThrow()),
                PlayerPo.class);
        copy.setName("Spain");
        players.add(copy);
        for (final String first : List.of("Dutch", "France")) {
            final EuropeanRelationPo pair = new EuropeanRelationPo();
            pair.setFirstPlayer(first);
            pair.setSecondPlayer("Spain");
            pair.setState(EuropeanRelationState.PEACE);
            data.getModel().getEuropeanRelations().add(pair);
        }
        if (deWitt) {
            recruitDeWitt(
                    players.stream().filter(p -> p.getName().equals("Dutch"))
                            .findFirst().orElseThrow());
        }
        return data;
    }

    /** One member and five offers, one per remaining category. */
    private void recruitDeWitt(final PlayerPo player) {
        final CongressPo congress = new CongressPo();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setGeneration(1);
        congress.setMembers(List.of(Founder.JAN_DE_WITT));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setJonesDeliveryPending(false);
        player.setCongress(congress);
    }

    private void start(final GameModelPo data) {
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        dutch = model.getPlayerByName("Dutch");
        france = model.getPlayerByName("France");
        spain = model.getPlayerByName("Spain");
    }

    private Model reload() {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static ForeignPowerComparison report(final Model from,
            final String viewer, final String power) {
        return from.getForeignPowerComparisons(from.getPlayerByName(viewer))
                .stream().filter(row -> row.getName().equals(power)).findFirst()
                .orElseThrow();
    }

    private static ForeignPowerComparison.Figures revealed(final Model from,
            final String viewer, final String power) {
        final ForeignPowerComparison row = report(from, viewer, power);
        assertEquals(Disclosure.REVEALED, row.getDisclosure());
        return row.getFigures().orElseThrow();
    }

    private static void assertWithheld(final Model from, final String viewer,
            final String power, final Disclosure disclosure) {
        final ForeignPowerComparison row = report(from, viewer, power);
        assertEquals(disclosure, row.getDisclosure());
        assertTrue(row.getFigures().isEmpty());
    }

    private static List<String> names(
            final List<ForeignPowerComparison> powers) {
        return powers.stream().map(ForeignPowerComparison::getName)
                .collect(Collectors.toList());
    }

    /**
     * A colonist cloned from French soldier 6, so every field is valid, created
     * directly at its own square. The square must be legal for that owner: its
     * own colony or an unoccupied land square, never a rival's.
     */
    private Unit colonist(final int id, final String owner, final Location at,
            final boolean veteran, final boolean mounted,
            final boolean holdingGuns) {
        final UnitPo po = model.getUnitById(6).save();
        po.setId(id);
        po.setOwnerId(owner);
        po.getPlaceMap().setLocation(at);
        po.setVeteran(veteran);
        po.setMounted(mounted);
        po.setHoldingGuns(holdingGuns);
        return model.createUnit(model.save(), po);
    }

    private Unit artillery(final int id, final String owner,
            final boolean damaged) {
        final UnitPo po = model.getUnitById(6).save();
        po.setId(id);
        po.setOwnerId(owner);
        po.setType(UnitType.ARTILLERY);
        po.setHoldingGuns(false);
        po.setMounted(false);
        po.setMissionary(null);
        po.setWorkUpdates(null);
        po.setWorkSkill(null);
        po.setArtilleryDamaged(damaged);
        return model.createUnit(model.save(), po);
    }

    private Ship ship(final int id, final String owner, final UnitType type,
            final Location at) {
        final UnitPo po = new UnitPo();
        po.setId(id);
        po.setType(type);
        po.setOwnerId(owner);
        po.setAction(new UnitActionNoAction().save());
        po.setAvailableMoves(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        po.setPlaceMap(place);
        return (Ship) model.createUnit(model.save(), po);
    }

    private void moveScout(final Location to) {
        final UnitFreeColonist scout = (UnitFreeColonist) model.getUnitById(0);
        scout.setMounted(true);
        scout.placeToLocation(to);
        dutch.revealMapForUnit(scout);
    }

    /**
     * An unoccupied sea square next to the vessel, found from the map itself,
     * so a rival ship is never created on another owner's square.
     */
    private Location emptySeaBeside(final Unit vessel) {
        return vessel.getLocation().getNeighbors().stream()
                .filter(model.getMap()::isValid)
                .filter(at -> model.getMap().getTerrainTypeAt(at).isSea())
                .filter(at -> model.getUnitsAt(at).isEmpty()).findFirst()
                .orElseThrow();
    }

    @Test
    void test_withoutDeWittEveryRivalIsLockedAndOnlyTheRelationIsShown() {
        start(data(false));
        final List<ForeignPowerComparison> powers = model
                .getForeignPowerComparisons(dutch);

        assertEquals(List.of("France", "Spain"), names(powers));
        for (final ForeignPowerComparison row : powers) {
            assertEquals(Disclosure.LOCKED, row.getDisclosure());
            assertTrue(row.getFigures().isEmpty());
            assertEquals(EuropeanRelationState.PEACE, row.getRelation());
        }
    }

    @Test
    void test_lockedStaysLockedEvenWithContact() {
        start(data(false));
        moveScout(SCOUT_AT_QUEBEC);

        assertTrue(dutch.hasContactWith(france));
        assertWithheld(model, "Dutch", "France", Disclosure.LOCKED);
    }

    @Test
    void test_viewerMustBeARegisteredColonialNation() {
        start(data(false));
        assertThrows(IllegalArgumentException.class,
                () -> model.getForeignPowerComparisons(
                        model.getPlayerByName("Dutch's King")));
    }

    @Test
    void test_founderMembershipBelongsToTheViewerOnly() {
        start(data(true));
        moveScout(SCOUT_AT_QUEBEC);

        // The Dutch recruited de Witt, so the Dutch report is not locked ...
        assertEquals(Disclosure.REVEALED,
                report(model, "Dutch", "France").getDisclosure());
        // ... while the French and Spanish viewpoints see everything locked
        // and no figure of the Dutch leaks to them.
        for (final String viewer : List.of("France", "Spain")) {
            for (final ForeignPowerComparison row : model
                    .getForeignPowerComparisons(
                            model.getPlayerByName(viewer))) {
                assertEquals(Disclosure.LOCKED, row.getDisclosure());
                assertTrue(row.getFigures().isEmpty());
            }
        }
        assertEquals(List.of("Dutch", "Spain"), names(model
                .getForeignPowerComparisons(model.getPlayerByName("France"))));
    }

    @Test
    void test_unknownPowersStayWithheldEvenWithDeWittAndReadingChangesNothing() {
        start(data(true));
        final String before = gson.toJson(model.save());

        assertWithheld(model, "Dutch", "France", Disclosure.UNCONTACTED);
        assertWithheld(model, "Dutch", "Spain", Disclosure.UNCONTACTED);
        assertEquals(names(model.getForeignPowerComparisons(dutch)),
                names(model.getForeignPowerComparisons(dutch)));

        // Reading opens no sighting, contact, random draw or treasury change.
        assertEquals(before, gson.toJson(model.save()));
        assertTrue(model.save().getGameManager().getPlayers().stream()
                .filter(p -> p.getName().equals("Dutch")).findFirst()
                .orElseThrow().getKnownPowers().isEmpty());
        // The relation overview stays available for unknown nations.
        assertEquals(EuropeanRelationState.PEACE,
                report(model, "Dutch", "Spain").getRelation());
    }

    @Test
    void test_unitOnlyContactSurvivesMovingAwayAndReload() {
        start(data(true));
        // Created directly on the empty land square ENTRY, outside Dutch sight.
        assertTrue(model.getUnitsAt(ENTRY).isEmpty());
        final Unit soldier = colonist(100, "Spain", ENTRY, false, false, true);
        spain.revealMapForUnit(soldier);
        // Nothing of Dutch sight covers the entry square yet.
        assertWithheld(model, "Dutch", "Spain", Disclosure.UNCONTACTED);

        // The scout meets the lone Spanish soldier; Spain owns no colony.
        moveScout(SCOUT_AT_QUEBEC);
        assertTrue(dutch.isCurrentlyVisible(ENTRY));
        final ForeignPowerComparison.Figures met = revealed(model, "Dutch",
                "Spain");
        assertEquals(0, met.getPopulation());
        assertEquals(0, met.getColonyCount());
        assertEquals(0.0, met.getAverageColonySize(), EXACT);
        assertEquals(2.0, met.getMilitaryStrength(), EXACT);
        assertEquals(0.0, met.getNavalStrength(), EXACT);
        assertEquals(0, met.getTradeRevenue());

        // Both leave each other's sight: the earned contact stays.
        moveScout(FAR_FROM_MONTREAL);
        soldier.placeToLocation(EXIT);
        spain.revealMapForUnit(soldier);
        assertFalse(dutch.isCurrentlyVisible(ENTRY));
        assertFalse(dutch.isCurrentlyVisible(EXIT));
        assertEquals(2.0,
                revealed(model, "Dutch", "Spain").getMilitaryStrength(), EXACT);
        assertTrue(model.save().getGameManager().getPlayers().stream()
                .filter(p -> p.getName().equals("Dutch")).findFirst()
                .orElseThrow().getKnownPowers().contains("Spain"));

        // Saving and loading keeps it without any new sighting.
        final Model loaded = reload();
        assertEquals(2.0,
                revealed(loaded, "Dutch", "Spain").getMilitaryStrength(),
                EXACT);
        assertFalse(loaded.getPlayerByName("Dutch").isCurrentlyVisible(ENTRY));
    }

    @Test
    void test_savedContactsAreValidatedNotGuessed() {
        start(data(true));
        for (final String invalid : List.of("Nowhere", "Dutch",
                "Dutch's King")) {
            final ModelPo saved = model.save();
            saved.getGameManager().getPlayers().stream()
                    .filter(p -> p.getName().equals("Dutch")).findFirst()
                    .orElseThrow().setKnownPowers(List.of(invalid));
            assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                    MissionImpl.getPredefinedGameOverEvaluators()));
        }
        final ModelPo twice = model.save();
        twice.getGameManager().getPlayers().stream()
                .filter(p -> p.getName().equals("Dutch")).findFirst()
                .orElseThrow().setKnownPowers(List.of("Spain", "Spain"));
        assertThrows(IllegalArgumentException.class, () -> Model.make(twice,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void test_shipContactIsEarnedFromSightAndOutlivesTheShip() {
        start(data(true));
        final Unit galleon = model.getUnitById(3);
        assertEquals(UnitType.GALLEON, galleon.getType());
        final Ship frigate = ship(101, "Spain", UnitType.FRIGATE,
                emptySeaBeside(galleon));
        assertWithheld(model, "Dutch", "Spain", Disclosure.UNCONTACTED);

        spain.revealMapForUnit(frigate);
        ForeignPowerComparison.Figures met = revealed(model, "Dutch", "Spain");
        assertEquals(16.0, met.getNavalStrength(), EXACT);
        assertEquals(0.0, met.getMilitaryStrength(), EXACT);

        // A sunk ship adds nothing, yet the contact is not forgotten.
        model.destroyUnit(frigate);
        met = revealed(model, "Dutch", "Spain");
        assertEquals(0.0, met.getNavalStrength(), EXACT);
        assertEquals(0.0,
                revealed(reload(), "Dutch", "Spain").getNavalStrength(), EXACT);
    }

    @Test
    void test_aggregatesSumCanonicalBaseOfLivingArmedUnitsOnly() {
        final GameModelPo data = data(true);
        // Quebec becomes a repair port: ocean beside it and a drydock.
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        QUEBEC.getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        terrain.put(Location.of(16, 9), TerrainType.OCEAN);
        terrain.put(NEAR_QUEBEC_SEA, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        final ConstructionPo drydock = new ConstructionPo();
        drydock.setType(ConstructionType.DRYDOCK);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(drydock);
        start(data);

        // Armed land: soldier 6 (2), foot veteran (3), dragoon (3), veteran
        // dragoon (4.5), artillery (7) and damaged artillery (5) = 24.5.
        // Every French land unit stands on France's own Quebec square.
        colonist(100, "France", QUEBEC, true, false, true);
        colonist(101, "France", QUEBEC, false, true, true);
        colonist(102, "France", QUEBEC, true, true, true);
        artillery(103, "France", false);
        artillery(104, "France", true);
        // Unarmed or gone: a scout, a plain colonist and a dead soldier.
        colonist(105, "France", QUEBEC, false, true, false);
        colonist(106, "France", QUEBEC, false, false, false);
        final Unit dead = colonist(107, "France", QUEBEC, true, false, true);
        model.destroyUnit(dead);

        // Armed ships: a ship under repair (16), privateer (8) and man-of-war
        // (24) = 48. Unarmed ships and a sunk frigate add nothing. The French
        // fleet shares one unoccupied sea square beside the Dutch galleon,
        // never the galleon's own square.
        final Location sea = emptySeaBeside(model.getUnitById(3));
        final Ship repaired = ship(110, "France", UnitType.FRIGATE,
                NEAR_QUEBEC_SEA);
        model.damageShip(repaired);
        assertTrue(repaired.isDamaged());
        ship(111, "France", UnitType.PRIVATEER, sea);
        ship(112, "France", UnitType.MAN_OF_WAR, sea);
        ship(113, "France", UnitType.GALLEON, sea);
        ship(114, "France", UnitType.CARAVEL, sea);
        ship(115, "France", UnitType.MERCHANTMAN, sea);
        model.destroyUnit(ship(116, "France", UnitType.FRIGATE, sea));

        // Net sales count, purchases do not.
        final int sold = france
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 10), false)
                .getTotal();
        france.sellGoods(Goods.of(GoodsType.CORN, 10));
        france.buyGoods(Goods.of(GoodsType.TOBACCO, 5));
        assertTrue(sold > 0);

        moveScout(SCOUT_AT_QUEBEC);
        final String before = gson.toJson(model.save());
        ForeignPowerComparison.Figures figures = revealed(model, "Dutch",
                "France");
        assertEquals(before, gson.toJson(model.save()));
        assertEquals(2, figures.getPopulation());
        assertEquals(2, figures.getColonyCount());
        assertEquals(1.0, figures.getAverageColonySize(), EXACT);
        assertEquals(24.5, figures.getMilitaryStrength(), EXACT);
        assertEquals(48.0, figures.getNavalStrength(), EXACT);
        assertEquals(sold, figures.getTradeRevenue());

        // Losing Montreal to the Crown changes the next report, not a cache.
        final Unit force = model.getUnitById(2);
        force.takeOver(model.getPlayerByName("France's King"));
        ColonyBattles.capture(model,
                model.getColonyByName("Montreal").orElseThrow(), force);
        figures = revealed(model, "Dutch", "France");
        assertEquals(1, figures.getColonyCount());
        assertEquals(
                model.getColonyByName("Quebec").orElseThrow().getPopulation(),
                figures.getPopulation());
        assertEquals(figures.getPopulation(), figures.getAverageColonySize(),
                EXACT);
        assertEquals(24.5, figures.getMilitaryStrength(), EXACT);
        assertEquals(48.0, figures.getNavalStrength(), EXACT);
        assertEquals(sold, figures.getTradeRevenue());
    }

}
