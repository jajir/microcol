package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventPeaceOffer;

import com.google.gson.Gson;

/** Franklin's fresh-turn negotiation prompt and saved report notice. */
class FranklinPeaceOfferTest {
    private GameModelPo fixture(final boolean hasFranklin,
            final boolean joinsThisUpdate, final boolean war) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        if (war) {
            data.getModel().getEuropeanRelations().get(0)
                    .setState(EuropeanRelationState.WAR);
        }
        final CongressPo congress = data.getModel().getGameManager()
                .getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().getCongress();
        if (hasFranklin || joinsThisUpdate) {
            congress.setActivated(true);
            congress.setGeneration(hasFranklin ? 2 : 1);
            congress.setMembers(hasFranklin ? List.of(Founder.BENJAMIN_FRANKLIN)
                    : List.of());
            congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                    hasFranklin ? Founder.THOMAS_JEFFERSON
                            : Founder.BENJAMIN_FRANKLIN,
                    Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
                    Founder.FERDINAND_MAGELLAN));
            congress.setSelected(
                    joinsThisUpdate ? Founder.BENJAMIN_FRANKLIN : null);
            congress.setBells(joinsThisUpdate ? 1000 : 0);
        }
        return data;
    }

    private Model model(final boolean hasFranklin,
            final boolean joinsThisUpdate, final boolean war,
            final boolean contact) {
        final Model model = Model.make(
                fixture(hasFranklin, joinsThisUpdate, war).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit visitor = model.getUnitById(0);
        visitor.placeToLocation(
                contact ? Location.of(18, 10) : Location.of(30, 10));
        visitor.getOwner().revealMapForUnit(visitor);
        return model;
    }

    private void nextFreshDutchTurn(final Model model) {
        int advanced = 0;
        do {
            model.endTurn();
            advanced++;
            assertTrue(advanced <= model.getPlayers().size(),
                    "Dutch turn did not return");
        } while (!model.getCurrentPlayer().getName().equals("Dutch"));
    }

    private long notices(final Model model) {
        return model.getTurnEventStore()
                .getForPlayer(model.getPlayerByName("Dutch")).stream()
                .filter(TurnEventPeaceOffer.class::isInstance).count();
    }

    private String saved(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void freshOwnerTurnCreatesDiscountedOfferAndReportWithoutPayment() {
        final Model model = model(true, false, true, true);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        dutch.setGold(1234);
        france.setGold(444);
        france.setDisplayName("Nouvelle France");
        assertTrue(model.getPendingPeaceOffer(dutch, france).isEmpty());
        nextFreshDutchTurn(model);
        final PeaceOffer offer = model.getPendingPeaceOffer(dutch, france)
                .orElseThrow();
        assertEquals(150, offer.getPrice());
        assertEquals(dutch.getGold(), offer.getActorGold());
        assertEquals(france.getGold(), offer.getRivalGold());
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertEquals(1, notices(model));
        final TurnEventPeaceOffer notice = (TurnEventPeaceOffer) model
                .getTurnEventStore().getForPlayer(dutch).stream()
                .filter(TurnEventPeaceOffer.class::isInstance).findFirst()
                .orElseThrow();
        assertEquals("Nouvelle France", notice.getRivalName());
        assertEquals(offer.getPrice(), notice.getPrice());
        final String pending = saved(model);
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        resumed.startGame();
        assertEquals(pending, saved(resumed));
        assertEquals(1, notices(resumed));
        assertEquals(offer.getPrice(),
                resumed.getPendingPeaceOffer(resumed.getPlayerByName("Dutch"),
                        resumed.getPlayerByName("France")).orElseThrow()
                        .getPrice());

        nextFreshDutchTurn(resumed);
        assertEquals(0, notices(resumed));
        assertTrue(
                resumed.getPendingPeaceOffer(resumed.getPlayerByName("Dutch"),
                        resumed.getPlayerByName("France")).isPresent());
    }

    /** Spain is a second computer nation at war with the Dutch. */
    private GameModelPo withSpainAtWar() {
        final GameModelPo data = fixture(true, false, true);
        final List<PlayerPo> players = data.getModel().getGameManager()
                .getPlayers();
        final Gson gson = new Gson();
        final PlayerPo spain = gson.fromJson(gson.toJson(players.stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow()), PlayerPo.class);
        spain.setName("Spain");
        players.add(spain);
        for (final String first : List.of("Dutch", "France")) {
            final EuropeanRelationPo pair = new EuropeanRelationPo();
            pair.setFirstPlayer(first);
            pair.setSecondPlayer("Spain");
            pair.setState(first.equals("Dutch") ? EuropeanRelationState.WAR
                    : EuropeanRelationState.PEACE);
            data.getModel().getEuropeanRelations().add(pair);
        }
        return data;
    }

    /** An unarmed Spanish colonist on open land beside the Dutch visitor. */
    private void placeSpanishNeighbor(final Model model) {
        final Location visitorAt = Location.of(18, 10);
        final Location at = visitorAt.getNeighbors().stream()
                .filter(model.getMap()::isValid)
                .filter(square -> UnitType.COLONIST.canMoveAtTerrain(
                        model.getMap().getTerrainTypeAt(square)))
                .filter(square -> model.getUnitsAt(square).isEmpty())
                .filter(square -> !square.equals(Location.of(17, 10)))
                .findFirst().orElseThrow();
        final UnitPo po = model.getUnitById(6).save();
        po.setId(900);
        po.setOwnerId("Spain");
        po.getPlaceMap().setLocation(at);
        po.setHoldingGuns(false);
        po.setMounted(false);
        model.createUnit(model.save(), po);
    }

    @Test
    void everyContactedWarRivalGetsItsOwnDiscountedOffer() {
        final Model model = Model.make(withSpainAtWar().getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit visitor = model.getUnitById(0);
        visitor.placeToLocation(Location.of(18, 10));
        placeSpanishNeighbor(model);
        visitor.getOwner().revealMapForUnit(visitor);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final Player spain = model.getPlayerByName("Spain");
        final int gold = dutch.getGold();

        nextFreshDutchTurn(model);
        // France: ceil(200 * 1.5) = 300 halved to 150, as in the single-rival
        // case. Spain has no armed unit: ceil(200 * 1) = 200 halved to 100.
        final PeaceOffer withFrance = model.getPendingPeaceOffer(dutch, france)
                .orElseThrow();
        final PeaceOffer withSpain = model.getPendingPeaceOffer(dutch, spain)
                .orElseThrow();
        assertEquals(150, withFrance.getPrice());
        assertEquals(100, withSpain.getPrice());
        assertTrue(withFrance.hasFranklin());
        assertTrue(withSpain.hasFranklin());
        assertEquals(2, notices(model));
        // Nothing is paid or agreed: only the offers exist.
        assertEquals(gold, dutch.getGold());
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, spain));

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(150,
                resumed.getPendingPeaceOffer(resumed.getPlayerByName("Dutch"),
                        resumed.getPlayerByName("France")).orElseThrow()
                        .getPrice());
        assertEquals(100,
                resumed.getPendingPeaceOffer(resumed.getPlayerByName("Dutch"),
                        resumed.getPlayerByName("Spain")).orElseThrow()
                        .getPrice());
    }

    @Test
    void noMembershipWarOrVisibleContactMeansNoAutomaticOffer() {
        for (final boolean[] setup : new boolean[][] { { false, true, true },
                { true, false, true }, { true, true, false } }) {
            final Model model = model(setup[0], false, setup[1], setup[2]);
            nextFreshDutchTurn(model);
            assertTrue(
                    model.getPendingPeaceOffer(model.getPlayerByName("Dutch"),
                            model.getPlayerByName("France")).isEmpty());
            assertEquals(0, notices(model));
        }
    }

    @Test
    void joiningFranklinDuringUpdateWaitsUntilFollowingOwnerUpdate() {
        final Model model = model(false, true, true, true);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        nextFreshDutchTurn(model);
        assertTrue(
                dutch.getCongressMembers().contains(Founder.BENJAMIN_FRANKLIN));
        assertTrue(model.getPendingPeaceOffer(dutch, france).isEmpty());
        assertEquals(0, notices(model));
        nextFreshDutchTurn(model);
        assertTrue(model.getPendingPeaceOffer(dutch, france).isPresent());
        assertEquals(1, notices(model));
    }

    @Test
    void existingPendingOfferIsRetainedWithoutNewNotice() {
        final Model model = model(true, false, true, true);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final PeaceOffer manual = model.preparePeaceOffer(model.getUnitById(0),
                france);
        nextFreshDutchTurn(model);
        assertSame(manual,
                model.getPendingPeaceOffer(dutch, france).orElseThrow());
        assertEquals(0, notices(model));
    }

    @Test
    void reverseDirectionPendingOfferSurvivesAiFranklinUpdate() {
        final GameModelPo data = fixture(false, false, true);
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().setComputer(true);
        final CongressPo frenchCongress = data.getModel().getGameManager()
                .getPlayers().stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow().getCongress();
        frenchCongress.setActivated(true);
        frenchCongress.setGeneration(2);
        frenchCongress.setMembers(List.of(Founder.BENJAMIN_FRANKLIN));
        frenchCongress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        frenchCongress.setSelected(null);
        frenchCongress.setBells(0);

        final Model initial = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit dutchVisitor = initial.getUnitById(0);
        dutchVisitor.placeToLocation(Location.of(18, 10));
        dutchVisitor.getOwner().revealMapForUnit(dutchVisitor);
        initial.preparePeaceOffer(dutchVisitor,
                initial.getPlayerByName("France"));
        final ModelPo franceTurn = initial.save();
        franceTurn.getGameManager().setCurrentPlayer("France");
        final Model model = Model.make(franceTurn,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        assertFalse(model.getPeaceVisitors(france, dutch).isEmpty());
        final PeaceOffer original = model.getPendingPeaceOffer(dutch, france)
                .orElseThrow();
        assertTrue(model.getPendingPeaceOffer(france, dutch).isEmpty());

        int advanced = 0;
        do {
            model.endTurn();
            advanced++;
            assertTrue(advanced <= model.getPlayers().size());
        } while (model.getCurrentPlayer() != france);
        assertSame(original,
                model.getPendingPeaceOffer(dutch, france).orElseThrow());
        assertTrue(model.getPendingPeaceOffer(france, dutch).isEmpty());
        assertEquals(0, model.getTurnEventStore().getForPlayer(france).stream()
                .filter(TurnEventPeaceOffer.class::isInstance).count());
    }

    @Test
    void malformedPeaceOfferNoticeIsRejectedOnRestore() {
        final Model model = model(true, false, true, true);
        nextFreshDutchTurn(model);
        final ModelPo missingPrice = model.save();
        missingPrice.getTurnEvents().stream()
                .filter(event -> event.getType()
                        .equals(TurnEventPeaceOffer.class.getSimpleName()))
                .findFirst().orElseThrow().setPeacePrice(null);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(missingPrice,
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
